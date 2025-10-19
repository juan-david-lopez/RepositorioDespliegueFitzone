package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Respuesta del procesamiento de pago y creación de membresía.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessPaymentResponse {
    private boolean success;
    private Long membershipId;
    private Long userId;
    private String membershipTypeName;
    private String paymentIntentId;
    private MembershipStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String message;
    private String error;  // Campo para mensajes de error
    private String transactionId;  // Alias para paymentIntentId
    private String receiptId;  // ID del recibo
}
