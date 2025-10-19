package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.RedemptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad LoyaltyRedemption - Representa el canje de una recompensa por puntos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "loyalty_redemptions_base")
public class LoyaltyRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loyalty_redemption")
    private Long idLoyaltyRedemption;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "loyalty_profile_id", nullable = false)
    private Long loyaltyProfileId;

    @Column(name = "loyalty_reward_id", nullable = false)
    private Long loyaltyRewardId;

    @Column(name = "redemption_code", nullable = false, unique = true)
    private String redemptionCode;

    @Column(name = "redemption_date", nullable = false)
    private LocalDateTime redemptionDate;

    @Column(name = "points_spent", nullable = false)
    private Integer pointsSpent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RedemptionStatus status;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "used_date")
    private LocalDateTime usedDate;

    @Column(name = "applied_reference_id")
    private Long appliedReferenceId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos adicionales requeridos por el código
    public boolean isExpired() {
        return expirationDate != null && LocalDateTime.now().isAfter(expirationDate);
    }

    public void markAsExpired() {
        this.status = RedemptionStatus.EXPIRED;
    }

    public void markAsUsed(Long referenceId) {
        this.status = RedemptionStatus.USED;
        this.usedDate = LocalDateTime.now();
        this.appliedReferenceId = referenceId;
    }

    public LoyaltyReward getReward() {
        // Este método debería ser manejado por el servicio para cargar la recompensa
        return null;
    }

    public User getUser() {
        // Este método debería ser manejado por el servicio para cargar el usuario
        return null;
    }

    public LoyaltyProfile getLoyaltyProfile() {
        // Este método debería ser manejado por el servicio para cargar el perfil
        return null;
    }

    // Métodos para compatibilidad con el builder
    public static class LoyaltyRedemptionBuilder {
        public LoyaltyRedemptionBuilder user(User user) {
            if (user != null) {
                this.userId = user.getIdUser();
            }
            return this;
        }

        public LoyaltyRedemptionBuilder loyaltyProfile(LoyaltyProfile profile) {
            if (profile != null) {
                this.loyaltyProfileId = profile.getIdLoyaltyProfile();
            }
            return this;
        }

        public LoyaltyRedemptionBuilder reward(LoyaltyReward reward) {
            if (reward != null) {
                this.loyaltyRewardId = reward.getIdLoyaltyReward();
            }
            return this;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (redemptionDate == null) {
            redemptionDate = LocalDateTime.now();
        }
        if (status == null) {
            status = RedemptionStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
