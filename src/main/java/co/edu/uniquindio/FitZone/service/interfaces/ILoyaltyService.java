package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.LoyaltyActivityRequest;
import co.edu.uniquindio.FitZone.dto.request.RedeemRewardRequest;
import co.edu.uniquindio.FitZone.dto.response.*;
import co.edu.uniquindio.FitZone.model.enums.ActivityType;
import co.edu.uniquindio.FitZone.model.enums.LoyaltyTier;

import java.util.List;

/**
 * Interfaz para el servicio de fidelización.
 */
public interface ILoyaltyService {

    // Gestión de perfiles de fidelización
    LoyaltyProfileResponse getOrCreateLoyaltyProfile(Long userId);
    LoyaltyProfileResponse getLoyaltyProfile(Long userId);
    void updateTierForAllProfiles();
    TierBenefitsResponse getTierBenefits(LoyaltyTier tier);

    // Registro de actividades y puntos
    LoyaltyActivityResponse logActivity(LoyaltyActivityRequest request);
    void logActivityAutomatic(Long userId, ActivityType activityType, String description, Long referenceId);
    void cancelActivity(Long activityId);
    List<LoyaltyActivityResponse> getUserActivities(Long userId);

    // Catálogo de recompensas
    List<LoyaltyRewardResponse> getAllRewards(Long userId);
    List<LoyaltyRewardResponse> getAffordableRewards(Long userId);
    LoyaltyRewardResponse getRewardById(Long rewardId, Long userId);

    // Canje de recompensas
    LoyaltyRedemptionResponse redeemReward(Long userId, RedeemRewardRequest request);
    List<LoyaltyRedemptionResponse> getUserRedemptions(Long userId);
    LoyaltyRedemptionResponse getRedemptionByCode(String redemptionCode);
    void markRedemptionAsUsed(String redemptionCode, Long referenceId);

    // Dashboard y estadísticas
    LoyaltyDashboardResponse getLoyaltyDashboard(Long userId);

    // Tareas automáticas
    void processExpiredPoints();
    void processExpiredRedemptions();
    void checkAndNotifyUpcomingRewards(Long userId);
}

