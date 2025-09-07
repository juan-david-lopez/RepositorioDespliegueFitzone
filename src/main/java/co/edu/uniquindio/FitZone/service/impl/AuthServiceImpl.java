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

    // Almacenamiento temporal de OTPs
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new ConcurrentHashMap<>();

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
        otpStorage.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));
        logger.info("OTP generado para {}: {}", email, otp);
        return otp;
    }

    @Override
    public void sendOTPEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Context context = new Context();
        context.setVariable("userName", user.getPersonalInformation().getFirstName());
        context.setVariable("otp", otp);
        String subject = "Código de verificación - FitZone";

        try {
            emailService.sendTemplatedEmail(email, subject, "otp-template", context);
        } catch (IOException e) {
            throw new RuntimeException("Error enviando OTP: " + e.getMessage(), e);
        }

        logger.info("OTP enviado a {}", email);
    }

    @Override
    public boolean validateOTP(String email, String otp) {
        if (!otpStorage.containsKey(email)) return false;

        if (otpExpiry.get(email).isBefore(LocalDateTime.now())) {
            otpStorage.remove(email);
            otpExpiry.remove(email);
            return false;
        }

        boolean valid = otpStorage.get(email).equals(otp);
        if (valid) {
            otpStorage.remove(email);
            otpExpiry.remove(email);
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
        context.setVariable("userName", user.getPersonalInformation().getFirstName());
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
