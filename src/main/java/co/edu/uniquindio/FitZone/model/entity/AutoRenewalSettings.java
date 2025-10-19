package co.edu.uniquindio.FitZone.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "auto_renewal_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoRenewalSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Builder.Default
    @Column(nullable = false)
    private boolean autoRenewalEnabled = false;

    private String preferredPaymentMethod;

    private String paymentMethodId;

    @Builder.Default
    @Column(nullable = false)
    private int daysBeforeNotification = 7;

    private LocalDate nextRenewalDate;

    @Builder.Default
    @Column(nullable = false)
    private String renewalStatus = "ACTIVE";

    @Builder.Default
    @Column(nullable = false)
    private boolean emailNotifications = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean smsNotifications = false;
}
