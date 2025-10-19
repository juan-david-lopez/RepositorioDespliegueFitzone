package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.RedemptionStatus;
import co.edu.uniquindio.FitZone.model.enums.RewardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para responder con información de un canje de recompensa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyRedemptionResponse {
    private Long idLoyaltyRedemption;
    private String redemptionCode;
    private String rewardName;
    private RewardType rewardType;
    private Integer pointsSpent;
    private RedemptionStatus status;
    private String statusDisplayName;
    private LocalDateTime redemptionDate;
    private LocalDateTime expirationDate;
    private LocalDateTime usedDate;
    private String notes;
    private Long appliedReferenceId;
    private Boolean isExpired;
    private Boolean canBeUsed;
}

