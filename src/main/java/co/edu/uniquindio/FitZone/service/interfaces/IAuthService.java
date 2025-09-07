package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.LoginRequest;
import co.edu.uniquindio.FitZone.dto.request.ResetPasswordRequest;

import java.io.IOException;

/**
 * Interfaz que define los métodos del servicio de autenticación
 * incluyendo autenticación de 2 pasos (2FA/OTP)
 */
public interface IAuthService {

    // ------------------ LOGIN CON 2FA ------------------

    /**
     * Paso 1: Inicia el login, valida credenciales y envía OTP al correo.
     * @param request contiene el email y la contraseña del usuario
     */
    void initiateLogin(LoginRequest request);

    /**
     * Paso 2: Valida el OTP ingresado por el usuario y genera JWT.
     * @param email del usuario
     * @param otp código OTP recibido por correo
     * @return token JWT si OTP es válido
     */
    String completeLogin(String email, String otp);

    // ------------------ FORGOT PASSWORD ------------------

    /**
     * Solicita el reseteo de contraseña enviando un token al correo
     * @param email del usuario que solicita el reseteo
     */
    void requestPasswordReset(String email) throws IOException;

    /**
     * Resetea la contraseña usando el token de verificación
     * @param request contiene el token y la nueva contraseña
     */
    void resetPassword(ResetPasswordRequest request);

    // ------------------ 2FA / OTP AUXILIARES ------------------

    /**
     * Valida las credenciales de usuario sin generar JWT
     * @param request LoginRequest con email y password
     * @return true si las credenciales son correctas
     */
    boolean validateCredentials(LoginRequest request);

    /**
     * Genera un OTP temporal para el usuario
     * @param email del usuario
     * @return OTP generado
     */
    String generateOTP(String email);

    /**
     * Envía el OTP al correo del usuario
     * @param email del usuario
     * @param otp código OTP
     */
    void sendOTPEmail(String email, String otp);

    /**
     * Valida el OTP ingresado por el usuario
     * @param email del usuario
     * @param otp OTP ingresado
     * @return true si el OTP es válido y no expiró
     */
    boolean validateOTP(String email, String otp);
    // En IAuthService.java
    /**
     * Genera el JWT final después de validar OTP
     * @param email del usuario
     * @return token JWT
     */
    String loginAfterOTP(String email);

    /**
     * Genera un nuevo token de acceso usando el token de refresco
     * @param refreshToken
     * @return
     */
    String refreshAccessToken(String refreshToken);

    String generateRefreshToken(String email);
}
