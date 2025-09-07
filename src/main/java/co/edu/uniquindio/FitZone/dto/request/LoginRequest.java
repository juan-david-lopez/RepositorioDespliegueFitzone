package co.edu.uniquindio.FitZone.dto.request;

/**
 * Representa una solicitud de inicio de sesión.
 * Contiene el correo electrónico y la contraseña del usuario.
 * @param email el correo electrónico del usuario
 * @param password la contraseña del usuario
 */
public record LoginRequest(
        String email,
        String password
) {
}
