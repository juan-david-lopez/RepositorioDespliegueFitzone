package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para la solicitud de creación de un Payment Intent con Stripe.
 */
@Data
public class PaymentIntentRequest {

    @NotNull(message = "El ID de la membresía no puede ser nulo")
    private Long membershipId;

    private String paymentMethodId; // Opcional: método de pago específico

    private String customerEmail; // Email del cliente

    private Boolean savePaymentMethod = false; // Para guardar el método de pago
}
