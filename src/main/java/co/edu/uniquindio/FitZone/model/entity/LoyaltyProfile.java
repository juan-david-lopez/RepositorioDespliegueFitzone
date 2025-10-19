package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidad LoyaltyProfile - Representa el perfil de fidelización de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "loyalty_profiles_base")
public class LoyaltyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loyalty_profile")
    private Long idLoyaltyProfile;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints;

    @Column(name = "lifetime_points", nullable = false)
    private Integer lifetimePoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_tier", nullable = false)
    private LoyaltyTier currentTier;

    @Column(name = "tier_achieved_date")
    private LocalDate tierAchievedDate;

    @Column(name = "member_since")
    private LocalDate memberSince;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Column(name = "consecutive_login_days")
    private Integer consecutiveLoginDays;

    @Column(name = "total_activities_logged")
    private Integer totalActivitiesLogged;

    @Column(name = "classes_attended")
    private Integer classesAttended;

    @Column(name = "renewals_completed")
    private Integer renewalsCompleted;

    @Column(name = "total_referrals")
    private Integer totalReferrals;

    @Column(name = "total_redemptions")
    private Integer totalRedemptions;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos adicionales requeridos por el código
    public User getUser() {
        // Este método debería ser manejado por el servicio para cargar el usuario
        return null;
    }

    public boolean deductPoints(Integer points) {
        if (availablePoints >= points) {
            availablePoints -= points;
            return true;
        }
        return false;
    }

    public void addPoints(Integer points) {
        availablePoints += points;
        totalPoints += points;
        lifetimePoints += points;
    }

    /**
     * Actualiza el tier del usuario basado en la antigüedad como miembro.
     */
    public void updateTierBasedOnSeniority() {
        if (memberSince == null) {
            return;
        }

        long monthsAsMember = ChronoUnit.MONTHS.between(memberSince, LocalDate.now());
        LoyaltyTier newTier;

        if (monthsAsMember >= 24) {
            newTier = LoyaltyTier.PLATINUM;
        } else if (monthsAsMember >= 12) {
            newTier = LoyaltyTier.GOLD;
        } else if (monthsAsMember >= 6) {
            newTier = LoyaltyTier.SILVER;
        } else {
            newTier = LoyaltyTier.BRONZE;
        }

        if (newTier != this.currentTier) {
            this.currentTier = newTier;
            this.tierAchievedDate = LocalDate.now();
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (totalPoints == null) {
            totalPoints = 0;
        }
        if (availablePoints == null) {
            availablePoints = 0;
        }
        if (lifetimePoints == null) {
            lifetimePoints = 0;
        }
        if (currentTier == null) {
            currentTier = LoyaltyTier.BRONZE;
        }
        if (memberSince == null) {
            memberSince = LocalDate.now();
        }
        if (consecutiveLoginDays == null) {
            consecutiveLoginDays = 0;
        }
        if (totalActivitiesLogged == null) {
            totalActivitiesLogged = 0;
        }
        if (classesAttended == null) {
            classesAttended = 0;
        }
        if (renewalsCompleted == null) {
            renewalsCompleted = 0;
        }
        if (totalReferrals == null) {
            totalReferrals = 0;
        }
        if (totalRedemptions == null) {
            totalRedemptions = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
