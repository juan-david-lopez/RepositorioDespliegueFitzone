package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.LoginRequest;
import co.edu.uniquindio.FitZone.dto.request.ResetPasswordRequest;
import co.edu.uniquindio.FitZone.exception.UserNotFoundException;
import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IAuthService;
import co.edu.uniquindio.FitZone.util.EmailService;
import co.edu.uniquindio.FitZone.util.JwtUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Almacenamiento temporal de OTPs para LOGIN
    private final Map<String, String> loginOtpStorage = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> loginOtpExpiry = new ConcurrentHashMap<>();

    // Almacenamiento temporal de OTPs para REGISTRO
    private final Map<String, String> registrationOtpStorage = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> registrationOtpExpiry = new ConcurrentHashMap<>();

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserDetailsServiceImpl userDetailsService,
                           JwtUtil jwtUtil,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ---------------- REGISTRO CON OTP ----------------

    @Override
    public String generateRegistrationOTP(String email) {
        String otp = RandomStringUtils.randomNumeric(6);
        registrationOtpStorage.put(email, otp);
        registrationOtpExpiry.put(email, LocalDateTime.now().plusMinutes(10)); // 10 minutos para registro
        logger.info("OTP de registro generado para {}: {}", email, otp);
        return otp;
    }

    @Override
    public boolean validateRegistrationOTP(String email, String otp) {
        logger.info("Validando OTP de registro para email: {} con código: {}", email, otp);
        logger.info("OTPs de registro almacenados: {}", registrationOtpStorage.keySet());

        if (!registrationOtpStorage.containsKey(email)) {
            logger.warn("No se encontró OTP de registro para: {}", email);
            logger.info("Emails con OTP de registro disponibles: {}", registrationOtpStorage.keySet());
            return false;
        }

        String storedOtp = registrationOtpStorage.get(email);
        LocalDateTime expiry = registrationOtpExpiry.get(email);

        logger.info("OTP almacenado para {}: {}, OTP recibido: {}", email, storedOtp, otp);
        logger.info("Fecha de expiración: {}, Fecha actual: {}", expiry, LocalDateTime.now());

        if (expiry.isBefore(LocalDateTime.now())) {
            registrationOtpStorage.remove(email);
            registrationOtpExpiry.remove(email);
            logger.warn("OTP de registro expirado para: {}", email);
            return false;
        }

        boolean valid = storedOtp.equals(otp);
        if (valid) {
            registrationOtpStorage.remove(email);
            registrationOtpExpiry.remove(email);
            logger.info("OTP de registro válido para: {}", email);
        } else {
            logger.warn("OTP de registro inválido para: {}. Esperado: {}, Recibido: {}", email, storedOtp, otp);
        }
        return valid;
    }

    @Override
    public void sendRegistrationOTPEmail(String email, String otp) {
        Context context = new Context();
        context.setVariable("userName", "Nuevo usuario");
        context.setVariable("otp", otp);
        context.setVariable("purpose", "verificar tu registro");
        String subject = "Verificación de registro - FitZone";

        try {
            emailService.sendTemplatedEmail(email, subject, "otp-template", context);
            logger.info("OTP de registro enviado a {}", email);
        } catch (IOException e) {
            logger.error("Error enviando OTP de registro a {}: {}", email, e.getMessage());
            throw new RuntimeException("Error enviando OTP de registro: " + e.getMessage(), e);
        }
    }

    // ---------------- LOGIN CON 2FA ----------------

    /**
     * Paso 1: Inicia login, valida credenciales y envía OTP por correo.
     */
    @Override
    public void initiateLogin(LoginRequest request) {
        logger.info("Iniciando login para {}", request.email());

        // 1. Validar credenciales
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales incorrectas", e);
        }

        // 2. Generar OTP
        String otp = generateOTP(request.email());

        // 3. Enviar OTP por correo
        sendOTPEmail(request.email(), otp);

        logger.info("OTP enviado a {}. El usuario debe validarlo para completar el login.", request.email());
    }

    /**
     * Paso 2: Valida OTP y devuelve JWT si es correcto.
     */
    @Override
    public String completeLogin(String email, String otp) {
        if (!validateOTP(email, otp)) {
            logger.warn("OTP inválido o expirado para usuario: {}", email);
            throw new RuntimeException("OTP inválido o expirado");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userDetails);

        logger.info("Login completado para {}. JWT generado.", email);
        return token;
    }

    // ---------------- MÉTODOS DE OTP ----------------
    @Override
    public String generateOTP(String email) {
        String otp = RandomStringUtils.randomNumeric(6);
        loginOtpStorage.put(email, otp);
        loginOtpExpiry.put(email, LocalDateTime.now().plusMinutes(5));
        logger.info("OTP generado para {}: {}", email, otp);
        return otp;
    }

    @Override
    public void sendOTPEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Context context = new Context();

        // Manejar el caso donde PersonalInformation puede ser null
        String userName = "Usuario";
        if (user.getPersonalInformation() != null &&
            user.getPersonalInformation().getFirstName() != null) {
            userName = user.getPersonalInformation().getFirstName();
        }

        context.setVariable("userName", userName);
        context.setVariable("otp", otp);
        String subject = "Código de verificación - FitZone";

        try {
            emailService.sendTemplatedEmail(email, subject, "otp-template", context);
            logger.info("OTP enviado a {}", email);
        } catch (IOException e) {
            logger.error("Error enviando OTP a {}: {}", email, e.getMessage());
            throw new RuntimeException("Error enviando OTP: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateOTP(String email, String otp) {
        if (!loginOtpStorage.containsKey(email)) return false;

        if (loginOtpExpiry.get(email).isBefore(LocalDateTime.now())) {
            loginOtpStorage.remove(email);
            loginOtpExpiry.remove(email);
            return false;
        }

        boolean valid = loginOtpStorage.get(email).equals(otp);
        if (valid) {
            loginOtpStorage.remove(email);
            loginOtpExpiry.remove(email);
        }
        return valid;
    }

    @Override
    public String loginAfterOTP(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userDetails); // ✅ ahora sí es un JWT real

        logger.info("Login exitoso con OTP para usuario {}. JWT generado.", email);
        return token;
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        try {
            // 1. Validar el refresh token
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("Refresh token inválido o expirado");
            }

            // 2. Extraer el username del refresh token
            String username = jwtUtil.extractUsername(refreshToken);

            // 3. Cargar UserDetails para verificar que el usuario existe
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                throw new RuntimeException("Usuario no encontrado");
            }

            // 4. Validar que el refresh token corresponde al usuario
            // (opcional, dependiendo de tu estrategia)
            if (!username.equals(userDetails.getUsername())) {
                throw new RuntimeException("Refresh token no válido para este usuario");
            }

            // 5. Generar nuevo access token
            return jwtUtil.generateToken(userDetails);

        } catch (Exception e) {
            throw new RuntimeException("Error refrescando token: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateRefreshToken(String email) {
        try {
            // Cargar UserDetails para generar el refresh token
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return jwtUtil.generateRefreshToken(userDetails);
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("Usuario no encontrado: " + email, e);
        } catch (Exception e) {
            throw new RuntimeException("Error generando refresh token: " + e.getMessage(), e);
        }
    }

    // ---------------- FORGOT PASSWORD ----------------
    @Override
    public void requestPasswordReset(String email) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        String token = RandomStringUtils.randomNumeric(6);
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        Context context = new Context();

        // Manejar el caso donde PersonalInformation puede ser null
        String userName = "Usuario";
        if (user.getPersonalInformation() != null &&
            user.getPersonalInformation().getFirstName() != null) {
            userName = user.getPersonalInformation().getFirstName();
        }

        context.setVariable("userName", userName);
        context.setVariable("verificationCode", token);
        context.setVariable("expiryDate", user.getPasswordResetTokenExpiryDate());
        context.setVariable("gymEmail", "fitzoneuq@gmail.com");

        String subject = "Recuperación de contraseña - FitZone";
        emailService.sendTemplatedEmail(user.getEmail(), subject, "password-reset", context);

        logger.info("Correo de recuperación enviado a {}", email);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiryDate(null);
        userRepository.save(user);

        logger.info("Contraseña restablecida para {}", user.getEmail());
    }

    @Override
    public boolean validateCredentials(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            return true;
        } catch (BadCredentialsException e) {
            return false;
        }
    }

}
