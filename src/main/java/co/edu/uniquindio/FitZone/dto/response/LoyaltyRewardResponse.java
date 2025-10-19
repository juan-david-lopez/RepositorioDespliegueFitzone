package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.RewardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para responder con información de una recompensa disponible.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyRewardResponse {
    private Long idLoyaltyReward;
    private String name;
    private String description;
    private RewardType rewardType;
    private String rewardTypeDisplayName;
    private Integer pointsCost;
    private String minimumTierRequired;
    private Integer validityDays;
    private String rewardValue;
    private String termsAndConditions;
    private Boolean canUserAfford;
    private Boolean meetsMinimumTier;
}

