package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.LoginRequest;
import co.edu.uniquindio.FitZone.dto.request.RefreshTokenRequest;
import co.edu.uniquindio.FitZone.dto.request.ResetPasswordRequest;
import co.edu.uniquindio.FitZone.service.impl.UserDetailsServiceImpl;
import co.edu.uniquindio.FitZone.service.interfaces.IAuthService;
import co.edu.uniquindio.FitZone.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para manejar las operaciones de autenticación y autorización.
 * Proporciona endpoints para iniciar sesión, solicitar restablecimiento de contraseña y restablecer la contraseña.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final IAuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsServiceImpl userDetailsService,
                          JwtUtil jwtUtil,
                          IAuthService authService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    // PASO 1: Login inicial (valida credenciales y envía OTP)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return login2FA(request);
    }

    @PostMapping("/login-2fa")
    public ResponseEntity<?> login2FA(@RequestBody LoginRequest request) {
        logger.info("POST /auth/login-2fa - Login inicial para usuario: {}", request.email());

        try {
            // Validar credenciales usando el servicio
            boolean valid = authService.validateCredentials(request);
            if (!valid) {
                logger.warn("Credenciales inválidas para usuario: {}", request.email());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Credenciales inválidas"));
            }

            // Generar OTP y enviarlo por email
            String otp = authService.generateOTP(request.email());
            authService.sendOTPEmail(request.email(), otp);

            logger.info("OTP generado y enviado para usuario: {}", request.email());

            return ResponseEntity.ok(createSuccessResponse(Map.of(
                    "status", "OTP_REQUIRED",
                    "message", "Se ha enviado un código de verificación a tu correo electrónico",
                    "email", request.email(),
                    "step", 1
            )));

        } catch (Exception e) {
            logger.error("Error en login-2fa para usuario {}: {}", request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error procesando el login", e.getMessage()));
        }
    }

    // PASO 2: Verificar OTP y generar JWT
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        logger.info("POST /auth/verify-otp - Verificando OTP para usuario: {}", email);

        // Validar parámetros
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Email es requerido"));
        }

        if (otp == null || otp.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("OTP es requerido"));
        }

        try {
            // Validar OTP
            boolean validOtp = authService.validateOTP(email, otp);
            if (!validOtp) {
                logger.warn("OTP inválido o expirado para usuario: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Código de verificación inválido o expirado"));
            }

            // Generar JWT final
            String token = authService.loginAfterOTP(email);

            // Validar que el token se generó correctamente
            if (token == null || token.trim().isEmpty()) {
                logger.error("Error: Token JWT generado está vacío para usuario: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error generando token de autenticación"));
            }

            // Verificar formato básico del JWT
            if (!isValidJwtFormat(token)) {
                logger.error("Error: Token JWT generado tiene formato inválido para usuario: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error en formato del token de autenticación"));
            }

            // 🔹 NUEVO: Generar refresh token
            String refreshToken = authService.generateRefreshToken(email);

            // Validar que el refresh token se generó correctamente
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                logger.error("Error: Refresh token generado está vacío para usuario: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error generando refresh token"));
            }

            logger.info("Login exitoso para usuario: {} - JWT y refresh token generados correctamente", email);

            return ResponseEntity.ok(createSuccessResponse(Map.of(
                    "accessToken", token,
                    "refreshToken", refreshToken, // 🔹 Incluir refresh token
                    "email", email,
                    "message", "Login exitoso",
                    "step", 2
            )));

        } catch (Exception e) {
            logger.error("Error al verificar OTP para usuario {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error verificando código de verificación", e.getMessage()));
        }
    }

    // Endpoint para reenviar OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        logger.info("POST /auth/resend-otp - Reenviando OTP para usuario: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Email es requerido"));
        }

        try {
            String otp = authService.generateOTP(email);
            authService.sendOTPEmail(email, otp);

            logger.info("OTP reenviado para usuario: {}", email);

            return ResponseEntity.ok(createSuccessResponse(Map.of(
                    "message", "Código de verificación reenviado exitosamente",
                    "email", email
            )));

        } catch (Exception e) {
            logger.error("Error reenviando OTP para usuario {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error reenviando código de verificación", e.getMessage()));
        }
    }

    // FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        logger.info("POST /auth/forgot-password - Solicitud de restablecimiento de contraseña para: {}", email);

        try {
            authService.requestPasswordReset(email);
            return ResponseEntity.ok(createSuccessResponse(Map.of(
                    "message", "Se ha enviado un email con las instrucciones para restablecer la contraseña",
                    "email", email
            )));

        } catch (Exception e) {
            logger.error("Error al procesar solicitud de restablecimiento para: {} - Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error procesando solicitud de restablecimiento", e.getMessage()));
        }
    }

    // RESET PASSWORD
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.info("POST /auth/reset-password - Restablecimiento de contraseña con token: {}", request.token());

        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(createSuccessResponse(Map.of(
                    "message", "Contraseña restablecida exitosamente"
            )));

        } catch (Exception e) {
            logger.error("Error al restablecer contraseña con token: {} - Error: {}", request.token(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Error restableciendo contraseña", e.getMessage()));
        }
    }
    // 🔹 NUEVO ENDPOINT: Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        logger.info("POST /auth/refresh - Refrescando token");

        if (request.refreshToken() == null || request.refreshToken().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Refresh token es requerido"));
        }

        try {
            // Validar y refrescar el token usando el servicio
            String newAccessToken = authService.refreshAccessToken(request.refreshToken());

            logger.info("Token refrescado exitosamente");

            return ResponseEntity.ok(createSuccessResponse(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", request.refreshToken(), // Mismo refresh token
                    "tokenType", "Bearer",
                    "message", "Token refrescado exitosamente"
            )));

        } catch (RuntimeException e) {
            logger.warn("Error refrescando token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado refrescando token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno refrescando token", e.getMessage()));
        }
    }

    // Métodos de utilidad
    private Map<String, Object> createSuccessResponse(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        response.putAll(data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String error) {
        return createErrorResponse(error, null);
    }

    private Map<String, Object> createErrorResponse(String error, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", System.currentTimeMillis());
        if (details != null && !details.trim().isEmpty()) {
            response.put("details", details);
        }
        return response;
    }

    private boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        return parts.length == 3 &&
                !parts[0].isEmpty() &&
                !parts[1].isEmpty() &&
                !parts[2].isEmpty();
    }
}