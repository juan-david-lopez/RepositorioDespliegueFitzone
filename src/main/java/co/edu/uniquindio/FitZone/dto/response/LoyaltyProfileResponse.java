package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para responder con información del perfil de fidelización de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyProfileResponse {
    private Long idLoyaltyProfile;
    private Long userId;
    private String userEmail;
    private String userName;
    private LoyaltyTier currentTier;
    private String tierDisplayName;
    private Integer totalPoints;
    private Integer availablePoints;
    private LocalDateTime memberSince;
    private Integer monthsAsMember;
    private LocalDateTime lastActivityDate;
    private Integer totalActivitiesLogged;
    private Integer consecutiveLoginDays;
    private Integer totalReferrals;
    private Integer classesAttended;
    private Integer renewalsCompleted;

    // Beneficios del tier actual
    private TierBenefitsResponse tierBenefits;

    // Progreso al siguiente nivel
    private Integer monthsToNextTier;
    private String nextTier;
}

