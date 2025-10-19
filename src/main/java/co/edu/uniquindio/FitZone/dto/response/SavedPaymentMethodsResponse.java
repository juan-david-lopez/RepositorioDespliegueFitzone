package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta de métodos de pago guardados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedPaymentMethodsResponse {
    private boolean success;
    private List<PaymentMethodResponse> paymentMethods;
}

