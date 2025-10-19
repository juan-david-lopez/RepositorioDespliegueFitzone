package co.edu.uniquindio.FitZone.exception;

/**
 * Excepción para indicar que el identificador de pago recibido es inválido
 * (por ejemplo, cuando el front envía un pm_* en lugar de un pi_* o client_secret).
 */
public class InvalidPaymentIdentifierException extends RuntimeException {
    public InvalidPaymentIdentifierException(String message) {
        super(message);
    }
}
