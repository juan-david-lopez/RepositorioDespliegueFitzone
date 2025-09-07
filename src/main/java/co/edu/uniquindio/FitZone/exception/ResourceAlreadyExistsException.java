package co.edu.uniquindio.FitZone.exception;

/**
 * Excepción que se lanza cuando se intenta crear un recurso que ya existe.
 * Por ejemplo, al intentar registrar un usuario con un correo electrónico ya registrado.
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
