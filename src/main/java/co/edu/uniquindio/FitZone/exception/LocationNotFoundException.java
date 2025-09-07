package co.edu.uniquindio.FitZone.exception;

/**
 * Excepción personalizada que se lanza cuando no se encuentra una sede.
 */
public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}
