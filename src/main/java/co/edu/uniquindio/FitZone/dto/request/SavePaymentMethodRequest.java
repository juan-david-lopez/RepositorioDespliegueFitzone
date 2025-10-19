package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para guardar un método de pago.
 */
@Data
public class SavePaymentMethodRequest {

    @NotNull(message = "El ID del método de pago no puede ser nulo")
    private String paymentMethodId;
}

