package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para la solicitud de creación de un Payment Intent.
 * @param amount Cantidad a cobrar.
 * @param currency Divisa (Moneda) en la que se realizará el cobro.
 * @param description Descripción del pago.
 */
public record PaymentIntentRequest(

        @NotNull(message = "El monto no puede ser nulo")
        BigDecimal amount,

        @NotBlank(message = "La divisa no puede estar vacía")
        String currency,

        String description
) {
}
