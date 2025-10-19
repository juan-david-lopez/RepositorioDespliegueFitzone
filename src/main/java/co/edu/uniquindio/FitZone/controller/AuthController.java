package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.LoginRequest;
import co.edu.uniquindio.FitZone.dto.request.VerifyOtpRequest;
import co.edu.uniquindio.FitZone.dto.response.AuthResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipInfo;
import co.edu.uniquindio.FitZone.dto.response.UserResponse;
import co.edu.uniquindio.FitZone.exception.UserNotFoundException;
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
 * Proporciona endpoints para registro con OTP, login con 2FA y restablecimiento de contraseña.
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

    // ============== ENDPOINTS DE LOGIN CON OTP ==============

    /**
     * Paso 1: Login inicial (valida credenciales y envía OTP)
     */
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

    /**
     * Paso 2: Verificar OTP (maneja tanto RequestBody JSON como RequestParams)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody(required = false) VerifyOtpRequest bodyRequest,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String otp,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String verificationType) {

        // Determinar si usar datos del body o de los parámetros
        VerifyOtpRequest request;
        if (bodyRequest != null) {
            request = bodyRequest;
            logger.info("POST /auth/verify-otp - Usando RequestBody JSON");
        } else if (email != null && otp != null) {
            // Crear request desde parámetros
            String finalVerificationType = verificationType != null ? verificationType : type;
            request = new VerifyOtpRequest(email, otp, finalVerificationType, type);
            logger.info("POST /auth/verify-otp - Usando RequestParams");
        } else {
            logger.error("No se proporcionaron datos válidos para verificar OTP");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Datos incompletos", "Se requiere email, otp y tipo de verificación"));
        }

        try {
            // DETECTAR AUTOMÁTICAMENTE EL TIPO DE OTP basándose en si el usuario está registrado
            boolean isRegistered = userRepository.existsByEmail(request.email());
            String actualVerificationType = isRegistered ? "login" : "registration";

            logger.info("POST /auth/verify-otp - Email: {}, Registrado: {}, Tipo de OTP detectado: {}",
                       request.email(), isRegistered, actualVerificationType);

            boolean valid;

            if ("login".equals(actualVerificationType)) {
                // Usuario registrado -> validar OTP de login
                logger.info("Verificando OTP de LOGIN para usuario registrado: {}", request.email());
                valid = authService.validateOTP(request.email(), request.code());
            } else {
                // Usuario no registrado -> validar OTP de registro
                logger.info("Verificando OTP de REGISTRO para nuevo usuario: {} con código: {}", request.email(), request.code());
                valid = authService.validateRegistrationOTP(request.email(), request.code());
            }

            if (valid) {
                if ("login".equals(actualVerificationType)) {
                    // Para login: generar JWT token completo
                    String token = authService.loginAfterOTP(request.email());
                    String refreshToken = authService.generateRefreshToken(request.email());

                    // Obtener información del usuario
                    User user = userRepository.findByEmail(request.email())
                            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

                    // Obtener información de membresía
                    MembershipInfo membershipInfo = null;
                    if (user.getMembership() != null) {
                        Membership membership = user.getMembership();
                        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), membership.getEndDate());
                        boolean isActive = membership.getStatus() == MembershipStatus.ACTIVE &&
                                         !LocalDate.now().isAfter(membership.getEndDate());

                        membershipInfo = MembershipInfo.builder()
                                .id(membership.getIdMembership())
                                .type(membership.getType())
                                .startDate(membership.getStartDate())
                                .endDate(membership.getEndDate())
                                .status(membership.getStatus().name())
                                .statusMessage(getStatusMessage(membership))
                                .daysRemaining(daysUntilExpiry)
                                .isActive(isActive)
                                .build();
                    }

                    // Construir el nombre completo de manera segura
                    String fullName = "Usuario";
                    if (user.getPersonalInformation() != null) {
                        StringBuilder nameBuilder = new StringBuilder();
                        if (user.getPersonalInformation().getFirstName() != null) {
                            nameBuilder.append(user.getPersonalInformation().getFirstName());
                        }
                        if (user.getPersonalInformation().getLastName() != null) {
                            if (!nameBuilder.isEmpty()) {
                                nameBuilder.append(" ");
                            }
                            nameBuilder.append(user.getPersonalInformation().getLastName());
                        }
                        if (!nameBuilder.isEmpty()) {
                            fullName = nameBuilder.toString().trim();
                        }
                    }

                    // ✅ CORREGIDO: Usar el nuevo constructor con todos los campos
                    UserResponse userResponse = UserResponse.fromUser(user,
                            membershipInfo != null ? String.valueOf(membershipInfo.getType()) : null);

                    AuthResponse authResponse = AuthResponse.builder()
                            .accessToken(token)
                            .refreshToken(refreshToken)
                            .user(userResponse)
                            .membership(membershipInfo)
                            .build();

                    logger.info("Login completado exitosamente para usuario: {}", request.email());
                    logger.info("Datos del usuario en respuesta - ID: {}, Email: {}, Nombre: {}, Role: {}",
                               userResponse.idUser(), userResponse.email(), userResponse.name(), userResponse.role());
                    return ResponseEntity.ok(createSuccessResponse(authResponse));

                } else {
                    // Para registro: solo confirmar que el OTP es válido
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "REGISTRATION_OTP_VERIFIED");
                    response.put("message", "Código de verificación válido. Ahora puedes completar tu registro");
                    response.put("email", request.email());
                    response.put("step", 2);
                    logger.info("OTP de registro verificado exitosamente para: {}", request.email());
                    return ResponseEntity.ok(createSuccessResponse(response));
                }
            } else {
                logger.warn("Intento fallido de verificación de OTP de {} para: {}", actualVerificationType, request.email());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Error de autenticación", "OTP inválido o expirado"));
            }
        } catch (Exception e) {
            logger.error("Error al verificar OTP para: {} - Error: {}", request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error verificando OTP", e.getMessage()));
        }
    }

    /**
     * Reenviar OTP - Detecta automáticamente si es para login o registro
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        logger.info("POST /auth/resend-otp - Reenviando OTP para: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Email es requerido","detalles"));
        }

        try {
            // Detectar si el email ya está registrado
            boolean isRegistered = userRepository.existsByEmail(email);

            String otp;
            if (isRegistered) {
                // Usuario registrado -> OTP de login
                logger.info("Email registrado - Generando OTP de LOGIN para: {}", email);
                otp = authService.generateOTP(email);
                authService.sendOTPEmail(email, otp);
                logger.info("OTP de LOGIN reenviado para usuario: {}", email);
            } else {
                // Email no registrado -> OTP de registro
                logger.info("Email NO registrado - Generando OTP de REGISTRO para: {}", email);
                otp = authService.generateRegistrationOTP(email);
                authService.sendRegistrationOTPEmail(email, otp);
                logger.info("OTP de REGISTRO reenviado para: {}", email);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Código de verificación reenviado exitosamente");
            response.put("email", email);
            response.put("type", isRegistered ? "login" : "registration");
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (Exception e) {
            logger.error("Error reenviando OTP para {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error reenviando código de verificación", e.getMessage()));
        }
    }

    // ============== ENDPOINTS DE REGISTRO CON OTP ==============

    /**
     * Paso 1: Envía OTP para verificación de email durante el registro
     */
    @PostMapping("/send-registration-otp")
    public ResponseEntity<?> sendRegistrationOtp(@RequestParam String email) {
        logger.info("POST /auth/send-registration-otp - Enviando OTP de registro para: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Email es requerido", "El email no puede estar vacío"));
        }

        try {
            // Verificar que el email no esté ya registrado
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Email ya registrado", "Este email ya está registrado en el sistema"));
            }

            // Generar y enviar OTP de registro
            String otp = authService.generateRegistrationOTP(email);
            authService.sendRegistrationOTPEmail(email, otp);

            logger.info("OTP de registro generado y enviado para: {}", email);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "REGISTRATION_OTP_SENT");
            response.put("message", "Se ha enviado un código de verificación a tu correo electrónico");
            response.put("email", email);
            response.put("step", 1);
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (Exception e) {
            logger.error("Error enviando OTP de registro para {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error enviando código de verificación", e.getMessage()));
        }
    }

    /**
     * Paso 2: Verifica el OTP de registro
     */
    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOtp(@RequestParam String email, @RequestParam String otp) {
        logger.info("POST /auth/verify-registration-otp - Verificación de OTP de registro para: {}", email);

        try {
            boolean valid = authService.validateRegistrationOTP(email, otp);
            if (valid) {
                logger.info("OTP de registro verificado exitosamente para: {}", email);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "REGISTRATION_OTP_VERIFIED");
                response.put("message", "Código de verificación válido. Ahora puedes completar tu registro");
                response.put("email", email);
                response.put("step", 2);
                return ResponseEntity.ok(createSuccessResponse(response));
            } else {
                logger.warn("Intento fallido de verificación de OTP de registro para: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Error de verificación", "Código de verificación inválido o expirado"));
            }
        } catch (Exception e) {
            logger.error("Error al verificar OTP de registro para: {} - Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Error verificando código", e.getMessage()));
        }
    }

    /**
     * Reenviar OTP de registro
     */
    @PostMapping("/resend-registration-otp")
    public ResponseEntity<?> resendRegistrationOtp(@RequestParam String email) {
        logger.info("POST /auth/resend-registration-otp - Reenviando OTP de registro para: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Email es requerido", "El email no puede estar vacío"));
        }

        try {
            // Verificar que el email no esté ya registrado
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Email ya registrado", "Este email ya está registrado en el sistema"));
            }

            String otp = authService.generateRegistrationOTP(email);
            authService.sendRegistrationOTPEmail(email, otp);

            logger.info("OTP de registro reenviado para: {}", email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Código de verificación reenviado exitosamente");
            response.put("email", email);
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (Exception e) {
            logger.error("Error reenviando OTP de registro para {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error reenviando código de verificación", e.getMessage()));
        }
    }

    // ============== FORGOT PASSWORD ==============

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

    // ============== MÉTODOS AUXILIARES ==============

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
