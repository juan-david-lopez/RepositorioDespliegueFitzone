package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request para procesar un pago y crear membresía.
 * El frontend envía el PaymentMethod ID y el backend crea el PaymentIntent.
 *
 * @param userId ID del usuario que adquiere la membresía
 * @param membershipTypeId ID del tipo de membresía
 * @param mainLocationId ID de la sede principal
 * @param paymentMethodId ID del método de pago de Stripe (pm_xxx)
 */
public record ProcessPaymentRequest(

    @NotNull(message = "El ID del usuario no puede ser nulo")
    Long userId,

    @NotNull(message = "El ID del tipo de membresía no puede ser nulo")
    Long membershipTypeId,

    @NotNull(message = "El ID de la sede principal no puede ser nulo")
    Long mainLocationId,

    @NotNull(message = "El ID del método de pago no puede ser nulo")
    @Pattern(regexp = "pm_.*", message = "paymentMethodId debe tener formato pm_xxx")
    String paymentMethodId
) {
}

