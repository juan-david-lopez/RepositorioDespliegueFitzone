package co.edu.uniquindio.FitZone.dto.request;

/**
 * DTO para la verificación de OTP
 */
public record VerifyOtpRequest(
        String email,
        String code,
        String verificationType,
        String type
) {
}
