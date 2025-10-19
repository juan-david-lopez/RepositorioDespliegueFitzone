package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para responder con el dashboard completo de fidelización del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyDashboardResponse {
    private LoyaltyProfileResponse profile;
    private List<LoyaltyActivityResponse> recentActivities;
    private List<LoyaltyRedemptionResponse> activeRedemptions;
    private List<LoyaltyRewardResponse> recommendedRewards;
    private Integer pointsExpiringInNext30Days;
    private String motivationalMessage;
}
