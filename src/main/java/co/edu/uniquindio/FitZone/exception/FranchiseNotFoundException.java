package co.edu.uniquindio.FitZone.exception;

/**
 * Excepción personalizada que se lanza cuando no se encuentra una franquicia.
 */
public class FranchiseNotFoundException extends RuntimeException {
    public FranchiseNotFoundException(String message) {
        super(message);
    }
}
