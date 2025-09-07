package co.edu.uniquindio.FitZone.exception;

/**
 * Excepción personalizada que se lanza cuando no se encuentra un tipo de membresía específico.
 */
public class MembershipTypeNotFoundException extends RuntimeException {
    public MembershipTypeNotFoundException(String message) {
        super(message);
    }
}
