package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoRenewalResponse {
    private Long userId;
    private boolean autoRenewalEnabled;
    private String preferredPaymentMethod;
    private String paymentMethodId;
    private int daysBeforeNotification; // Días antes de vencimiento para notificar
    private LocalDate nextRenewalDate;
    private String renewalStatus; // ACTIVE | PAUSED | CANCELLED
    private boolean emailNotifications;
    private boolean smsNotifications;
}

