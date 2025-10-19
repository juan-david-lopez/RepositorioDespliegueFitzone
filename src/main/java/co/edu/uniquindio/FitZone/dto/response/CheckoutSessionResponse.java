package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de creación de sesión de checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionResponse {
    private boolean success;
    private String sessionId;
    private String sessionUrl;
    private String message;
}

