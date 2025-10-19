package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.ActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad LoyaltyActivity - Representa una actividad que genera puntos de fidelización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "loyalty_activities_base")
public class LoyaltyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loyalty_activity")
    private Long idLoyaltyActivity;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "loyalty_profile_id", nullable = false)
    private Long loyaltyProfileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "activity_date", nullable = false)
    private LocalDateTime activityDate;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_bonus_activity")
    private Boolean isBonusActivity;

    @Column(name = "is_cancelled")
    private Boolean isCancelled;

    @Column(name = "is_expired")
    private Boolean isExpired;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Método adicional requerido por el código
    public LoyaltyProfile getLoyaltyProfile() {
        // Este método debería ser manejado por el servicio para cargar el perfil
        return null;
    }

    public User getUser() {
        // Este método debería ser manejado por el servicio para cargar el usuario
        return null;
    }

    // Métodos para compatibilidad con el builder
    public static class LoyaltyActivityBuilder {
        public LoyaltyActivityBuilder user(User user) {
            if (user != null) {
                this.userId = user.getIdUser();
            }
            return this;
        }

        public LoyaltyActivityBuilder loyaltyProfile(LoyaltyProfile profile) {
            if (profile != null) {
                this.loyaltyProfileId = profile.getIdLoyaltyProfile();
            }
            return this;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (activityDate == null) {
            activityDate = LocalDateTime.now();
        }
        if (isBonusActivity == null) {
            isBonusActivity = false;
        }
        if (isCancelled == null) {
            isCancelled = false;
        }
        if (isExpired == null) {
            isExpired = false;
        }
        if (pointsEarned == null) {
            pointsEarned = 0;
        }
    }
}
