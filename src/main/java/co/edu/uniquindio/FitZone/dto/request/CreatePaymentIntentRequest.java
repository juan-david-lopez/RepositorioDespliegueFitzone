package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO para crear un Payment Intent de Stripe.
 */
@Data
public class CreatePaymentIntentRequest {

    @NotNull(message = "El monto no puede ser nulo")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    private String currency = "cop"; // Por defecto pesos colombianos

    @NotNull(message = "El tipo de membresía no puede ser nulo")
    private String membershipType;

    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Long userId;

    private String description;

    private Map<String, String> metadata;
}

