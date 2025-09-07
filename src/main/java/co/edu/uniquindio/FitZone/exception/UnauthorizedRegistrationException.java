package co.edu.uniquindio.FitZone.exception;

/**
 * Excepción personalizada para manejar intentos de registro no autorizados.
 * Se lanza cuando un usuario intenta registrarse sin los permisos adecuados.
 */
public class UnauthorizedRegistrationException extends RuntimeException {
    public UnauthorizedRegistrationException(String message) {
        super(message);
    }
}
