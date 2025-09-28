package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.LoginRequest;
import co.edu.uniquindio.FitZone.dto.response.AuthResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipInfo;
import co.edu.uniquindio.FitZone.dto.response.UserResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.service.impl.UserDetailsServiceImpl;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import co.edu.uniquindio.FitZone.service.interfaces.IAuthService;
import co.edu.uniquindio.FitZone.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final IMembershipService membershipService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsServiceImpl userDetailsService,
                          JwtUtil jwtUtil,
                          IAuthService authService,
                          IMembershipService membershipService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.membershipService = membershipService;
        this.userRepository = userRepository;
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
                        .body(createErrorResponse("Credenciales inválidas","detalles"));
            }

            // Generar OTP y enviarlo por email
            String otp = authService.generateOTP(request.email());
            authService.sendOTPEmail(request.email(), otp);

            logger.info("OTP generado y enviado para usuario: {}", request.email());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "OTP_REQUIRED");
            response.put("message", "Se ha enviado un código de verificación a tu correo electrónico");
            response.put("email", request.email());
            response.put("step", 1);
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (Exception e) {
            logger.error("Error en login-2fa para usuario {}: {}", request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error procesando el login", e.getMessage()));
        }
    }

    // PASO 2: Verificar OTP y generar JWT
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        logger.info("POST /auth/verify-otp - Verificación de OTP para: {}", email);

        try {
            boolean valid = authService.validateOTP(email, otp);
            if (valid) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "OTP verificado correctamente");
                return ResponseEntity.ok(createSuccessResponse(response));
            } else {
                logger.warn("Intento fallido de verificación de OTP para: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Error de autenticación", "OTP inválido o expirado"));
            }
        } catch (Exception e) {
            logger.error("Error al verificar OTP para: {} - Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Error verificando OTP", e.getMessage()));
        }
    }

    // Endpoint para reenviar OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        logger.info("POST /auth/resend-otp - Reenviando OTP para usuario: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Email es requerido","detalles"));
        }

        try {
            String otp = authService.generateOTP(email);
            authService.sendOTPEmail(email, otp);

            logger.info("OTP reenviado para usuario: {}", email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Código de verificación reenviado exitosamente");
            response.put("email", email);
            return ResponseEntity.ok(createSuccessResponse(response));

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
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Se ha enviado un email con las instrucciones para restablecer la contraseña");
            response.put("email", email);
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (Exception e) {
            logger.error("Error al procesar solicitud de restablecimiento para: {} - Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error procesando solicitud de restablecimiento", e.getMessage()));
        }
    }
    
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        response.put("data", data);
        return response;
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
    
    /**
     * Obtiene la información detallada de la membresía de un usuario
     */
    private MembershipInfo getMembershipInfo(User user) {
        if (user == null || user.getMembership() == null) {
            return MembershipInfo.builder()
                    .isActive(false)
                    .status("INACTIVE")
                    .statusMessage("No tiene una membresía activa")
                    .build();
        }

        Membership membership = user.getMembership();
        LocalDate today = LocalDate.now();
        boolean isActive = membership.getStatus() == MembershipStatus.ACTIVE && 
                         !today.isAfter(membership.getEndDate());
        
        long daysRemaining = 0;
        if (isActive) {
            daysRemaining = ChronoUnit.DAYS.between(today, membership.getEndDate());
        }

        return MembershipInfo.builder()
                .id(membership.getIdMembership())
                .type(membership.getType())
                .startDate(membership.getStartDate())
                .endDate(membership.getEndDate())
                .status(membership.getStatus().name())
                .statusMessage(getStatusMessage(membership))
                .daysRemaining(daysRemaining)
                .isActive(isActive)
                .build();
    }
    
    /**
     * Obtiene un mensaje descriptivo del estado de la membresía
     */
    private String getStatusMessage(Membership membership) {
        if (membership == null) {
            return "No tiene una membresía activa";
        }
        
        switch (membership.getStatus()) {
            case ACTIVE:
                return "Membresía activa hasta el " + membership.getEndDate();
            case SUSPENDED:
                return "Membresía suspendida: " + 
                       (membership.getSuspensionReason() != null ? 
                        membership.getSuspensionReason() : "Razón no especificada");
            case EXPIRED:
                return "Membresía expirada el " + membership.getEndDate();
            case CANCELLED:
                return "Membresía cancelada";
            default:
                return "Estado de membresía desconocido";
        }
    }
}