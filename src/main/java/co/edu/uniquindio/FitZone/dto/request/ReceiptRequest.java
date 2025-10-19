package co.edu.uniquindio.FitZone.dto.request;

import lombok.Data;

@Data
public class ReceiptRequest {
    private Long membershipId;
    private String paymentIntentId;
}
