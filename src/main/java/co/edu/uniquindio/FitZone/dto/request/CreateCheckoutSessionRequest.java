package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para crear una sesión de Stripe Checkout.
 */
@Data
public class CreateCheckoutSessionRequest {

    @NotNull(message = "El tipo de membresía no puede ser nulo")
    private String membershipType;

    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Long userId;

    @NotNull(message = "La URL de éxito no puede ser nula")
    private String successUrl;

    @NotNull(message = "La URL de cancelación no puede ser nula")
    private String cancelUrl;

    private BillingInfoRequest billingInfo;
}

