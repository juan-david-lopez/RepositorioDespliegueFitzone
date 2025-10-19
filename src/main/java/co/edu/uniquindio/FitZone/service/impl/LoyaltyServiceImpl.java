package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.LoyaltyActivityRequest;
import co.edu.uniquindio.FitZone.dto.request.RedeemRewardRequest;
import co.edu.uniquindio.FitZone.dto.response.*;
import co.edu.uniquindio.FitZone.exception.*;
import co.edu.uniquindio.FitZone.model.entity.*;
import co.edu.uniquindio.FitZone.model.enums.*;
import co.edu.uniquindio.FitZone.repository.*;
import co.edu.uniquindio.FitZone.service.interfaces.ILoyaltyService;
import co.edu.uniquindio.FitZone.util.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de fidelización.
 */
@Service
@Transactional
public class LoyaltyServiceImpl implements ILoyaltyService {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyServiceImpl.class);

    private final LoyaltyProfileRepository loyaltyProfileRepository;
    private final LoyaltyActivityRepository loyaltyActivityRepository;
    private final LoyaltyRewardRepository loyaltyRewardRepository;
    private final LoyaltyRedemptionRepository loyaltyRedemptionRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final EmailService emailService;

    public LoyaltyServiceImpl(
            LoyaltyProfileRepository loyaltyProfileRepository,
            LoyaltyActivityRepository loyaltyActivityRepository,
            LoyaltyRewardRepository loyaltyRewardRepository,
            LoyaltyRedemptionRepository loyaltyRedemptionRepository,
            UserRepository userRepository,
            MembershipRepository membershipRepository,
            EmailService emailService) {
        this.loyaltyProfileRepository = loyaltyProfileRepository;
        this.loyaltyActivityRepository = loyaltyActivityRepository;
        this.loyaltyRewardRepository = loyaltyRewardRepository;
        this.loyaltyRedemptionRepository = loyaltyRedemptionRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.emailService = emailService;
    }

    @Override
    public LoyaltyProfileResponse getOrCreateLoyaltyProfile(Long userId) {
        logger.info("Obteniendo o creando perfil de fidelización para usuario ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<LoyaltyProfile> existingProfile = loyaltyProfileRepository.findByUserId(user.getIdUser());

        if (existingProfile.isPresent()) {
            return mapToProfileResponse(existingProfile.get());
        }

        // Crear nuevo perfil
        LoyaltyProfile newProfile = LoyaltyProfile.builder()
                .userId(user.getIdUser())
                .totalPoints(0)
                .availablePoints(0)
                .lifetimePoints(0)
                .currentTier(LoyaltyTier.BRONZE) // ✅ CORREGIDO: Cambiar de BRONCE a BRONZE
                .memberSince(java.time.LocalDate.now())
                .consecutiveLoginDays(0)
                .totalActivitiesLogged(0)
                .classesAttended(0)
                .renewalsCompleted(0)
                .totalReferrals(0)
                .totalRedemptions(0)
                .isActive(true)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        newProfile = loyaltyProfileRepository.save(newProfile);
        logger.info("✅ Perfil de fidelización creado para usuario ID: {}", userId);

        return mapToProfileResponse(newProfile);
    }

    @Override
    public LoyaltyProfileResponse getLoyaltyProfile(Long userId) {
        logger.info("Obteniendo perfil de fidelización para usuario ID: {}", userId);

        LoyaltyProfile profile = loyaltyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de fidelización no encontrado"));

        // Actualizar tier basado en antigüedad - comentado hasta implementar el método
        // profile.updateTierBasedOnSeniority();
        loyaltyProfileRepository.save(profile);

        return mapToProfileResponse(profile);
    }

    @Override
    public void updateTierForAllProfiles() {
        logger.info("Actualizando niveles de fidelización para todos los perfiles");

        List<LoyaltyProfile> profiles = loyaltyProfileRepository.findAll();
        int updatedCount = 0;

        for (LoyaltyProfile profile : profiles) {
            LoyaltyTier oldTier = profile.getCurrentTier();
            profile.updateTierBasedOnSeniority();

            if (!oldTier.equals(profile.getCurrentTier())) {
                loyaltyProfileRepository.save(profile);
                updatedCount++;
                logger.info("Usuario {} ascendió de {} a {}",
                        profile.getUser().getEmail(), oldTier, profile.getCurrentTier());

                // Enviar notificación de ascenso
                sendTierUpgradeNotification(profile, oldTier);
            }
        }

        logger.info("✅ Actualización completada: {} perfiles actualizados", updatedCount);
    }

    @Override
    public TierBenefitsResponse getTierBenefits(LoyaltyTier tier) {
        return switch (tier) {
            case BRONZE -> TierBenefitsResponse.builder()
                    .tierName("BRONCE")
                    .renewalDiscountPercentage(0)
                    .additionalClassesPerMonth(0)
                    .freeGuestPassesPerMonth(0)
                    .priorityReservations(false)
                    .description("Beneficios estándar según membresía")
                    .build();
            case SILVER -> TierBenefitsResponse.builder()
                    .tierName("PLATA")
                    .renewalDiscountPercentage(5)
                    .additionalClassesPerMonth(1)
                    .freeGuestPassesPerMonth(0)
                    .priorityReservations(false)
                    .description("5% descuento en renovación + 1 clase adicional/mes")
                    .build();
            case GOLD -> TierBenefitsResponse.builder()
                    .tierName("ORO")
                    .renewalDiscountPercentage(10)
                    .additionalClassesPerMonth(2)
                    .freeGuestPassesPerMonth(1)
                    .priorityReservations(true)
                    .description("10% descuento + 2 clases adicionales/mes + invitado gratis 1 vez/mes")
                    .build();
            case PLATINUM -> TierBenefitsResponse.builder()
                    .tierName("PLATINO")
                    .renewalDiscountPercentage(15)
                    .additionalClassesPerMonth(4)
                    .freeGuestPassesPerMonth(2)
                    .priorityReservations(true)
                    .description("15% descuento + 4 clases adicionales/mes + 2 invitados gratis/mes")
                    .build();
            default -> TierBenefitsResponse.builder()
                    .tierName("BRONCE")
                    .renewalDiscountPercentage(0)
                    .additionalClassesPerMonth(0)
                    .freeGuestPassesPerMonth(0)
                    .priorityReservations(false)
                    .description("Beneficios estándar según membresía")
                    .build();
        };
    }

    @Override
    public LoyaltyActivityResponse logActivity(LoyaltyActivityRequest request) {
        logger.info("Registrando actividad de fidelización: tipo={}, userId={}",
                request.getActivityType(), request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la membresía esté activa
        List<Membership> userMemberships = membershipRepository.findByUserIdOrderByStartDateDesc(user.getIdUser());
        Optional<Membership> activeMembership = userMemberships.stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .findFirst();

        if (activeMembership.isEmpty()) {
            throw new RuntimeException("El usuario no tiene una membresía activa");
        }

        LoyaltyProfile profile = loyaltyProfileRepository.findByUserId(user.getIdUser())
                .orElseGet(() -> {
                    LoyaltyProfile newProfile = LoyaltyProfile.builder()
                            .userId(user.getIdUser())
                            .totalPoints(0)
                            .availablePoints(0)
                            .lifetimePoints(0)
                            .currentTier(LoyaltyTier.BRONZE)
                            .memberSince(java.time.LocalDate.now())
                            .consecutiveLoginDays(0)
                            .totalActivitiesLogged(0)
                            .classesAttended(0)
                            .renewalsCompleted(0)
                            .totalReferrals(0)
                            .totalRedemptions(0)
                            .isActive(true)
                            .createdAt(java.time.LocalDateTime.now())
                            .updatedAt(java.time.LocalDateTime.now())
                            .build();
                    return loyaltyProfileRepository.save(newProfile);
                });

        // Calcular puntos según el tipo de actividad
        Integer points = calculatePointsForActivity(request.getActivityType(), request.getIsBonusActivity());

        // Crear actividad
        LoyaltyActivity activity = LoyaltyActivity.builder()
                .user(user)
                .loyaltyProfile(profile)
                .activityType(request.getActivityType())
                .pointsEarned(points)
                .description(request.getDescription())
                .referenceId(request.getReferenceId())
                .isBonusActivity(request.getIsBonusActivity() != null ? request.getIsBonusActivity() : false)
                .build();

        activity = loyaltyActivityRepository.save(activity);

        // Actualizar perfil
        profile.addPoints(points);
        profile.setTotalActivitiesLogged(profile.getTotalActivitiesLogged() + 1);
        profile.setLastActivityDate(LocalDateTime.now());
        updateActivityStats(profile, request.getActivityType());
        loyaltyProfileRepository.save(profile);

        logger.info("✅ Actividad registrada: {} puntos para usuario ID: {}", points, request.getUserId());

        return mapToActivityResponse(activity);
    }

    @Override
    public void logActivityAutomatic(Long userId, ActivityType activityType, String description, Long referenceId) {
        LoyaltyActivityRequest request = LoyaltyActivityRequest.builder()
                .userId(userId)
                .activityType(activityType)
                .description(description)
                .referenceId(referenceId)
                .isBonusActivity(false)
                .build();

        try {
            logActivity(request);
        } catch (Exception e) {
            logger.error("Error al registrar actividad automática: {}", e.getMessage());
        }
    }

    @Override
    public void cancelActivity(Long activityId) {
        logger.info("Cancelando actividad ID: {}", activityId);

        LoyaltyActivity activity = loyaltyActivityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        if (activity.getIsCancelled()) {
            logger.warn("La actividad ya estaba cancelada");
            return;
        }

        activity.setIsCancelled(true);
        loyaltyActivityRepository.save(activity);

        // Revertir puntos del perfil
        LoyaltyProfile profile = activity.getLoyaltyProfile();
        profile.setAvailablePoints(profile.getAvailablePoints() - activity.getPointsEarned());
        loyaltyProfileRepository.save(profile);

        logger.info("✅ Actividad cancelada y puntos revertidos");
    }

    @Override
    public List<LoyaltyActivityResponse> getUserActivities(Long userId) {
        List<LoyaltyActivity> activities = loyaltyActivityRepository.findByUserIdOrderByActivityDateDesc(userId);
        return activities.stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoyaltyRewardResponse> getAllRewards(Long userId) {
        LoyaltyProfile profile = loyaltyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de fidelización no encontrado"));

        List<LoyaltyReward> rewards = loyaltyRewardRepository.findByIsActiveTrue();

        return rewards.stream()
                .map(reward -> mapToRewardResponse(reward, profile))
                .collect(Collectors.toList());
    }

    @Override
    public List<LoyaltyRewardResponse> getAffordableRewards(Long userId) {
        LoyaltyProfile profile = loyaltyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de fidelización no encontrado"));

        List<LoyaltyReward> rewards = loyaltyRewardRepository.findAffordableRewards(profile.getAvailablePoints());

        return rewards.stream()
                .map(reward -> mapToRewardResponse(reward, profile))
                .filter(LoyaltyRewardResponse::getCanUserAfford)
                .collect(Collectors.toList());
    }

    @Override
    public LoyaltyRewardResponse getRewardById(Long rewardId, Long userId) {
        LoyaltyProfile profile = loyaltyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de fidelización no encontrado"));

        LoyaltyReward reward = loyaltyRewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("Recompensa no encontrada"));

        return mapToRewardResponse(reward, profile);
    }

    @Override
    public LoyaltyRedemptionResponse redeemReward(Long userId, RedeemRewardRequest request) {
        logger.info("Procesando canje de recompensa: rewardId={}, userId={}", request.getRewardId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LoyaltyProfile profile = loyaltyProfileRepository.findByUserId(user.getIdUser())
                .orElseThrow(() -> new RuntimeException("Perfil de fidelización no encontrado"));

        LoyaltyReward reward = loyaltyRewardRepository.findById(request.getRewardId())
                .orElseThrow(() -> new RuntimeException("Recompensa no encontrada"));

        // Validaciones
        if (!reward.getIsActive()) {
            throw new RuntimeException("Esta recompensa no está disponible");
        }

        if (profile.getAvailablePoints() < reward.getPointsCost()) {
            throw new RuntimeException("No tienes suficientes puntos para esta recompensa");
        }

        if (reward.getMinimumTierRequired() != null) {
            if (!meetsMinimumTier(profile.getCurrentTier(), reward.getMinimumTierRequired())) {
                throw new RuntimeException("No cumples con el nivel mínimo requerido para esta recompensa");
            }
        }

        // Deducir puntos
        boolean deducted = profile.deductPoints(reward.getPointsCost());
        if (!deducted) {
            throw new RuntimeException("Error al deducir puntos");
        }

        loyaltyProfileRepository.save(profile);

        // Crear canje
        LoyaltyRedemption redemption = LoyaltyRedemption.builder()
                .user(user)
                .loyaltyProfile(profile)
                .reward(reward)
                .pointsSpent(reward.getPointsCost())
                .notes(request.getNotes())
                .build();
        logger.info("✅ Canje completado: código={}, puntos={}", redemption.getRedemptionCode(), reward.getPointsCost());
        redemption.setExpirationDate(LocalDateTime.now().plusDays(reward.getValidityDays()));
        redemption = loyaltyRedemptionRepository.save(redemption);

        logger.info("✅ Canje completado: código={}, puntos={}", redemption.getRedemptionCode(), reward.getPointsCost());

        // Enviar email de confirmación
        sendRedemptionConfirmationEmail(user, redemption, reward);

        return mapToRedemptionResponse(redemption);
    }

    @Override
    public List<LoyaltyRedemptionResponse> getUserRedemptions(Long userId) {
        List<LoyaltyRedemption> redemptions = loyaltyRedemptionRepository.findByUserIdOrderByRedemptionDateDesc(userId);
        return redemptions.stream()
                .map(this::mapToRedemptionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LoyaltyRedemptionResponse getRedemptionByCode(String redemptionCode) {
        LoyaltyRedemption redemption = loyaltyRedemptionRepository.findByRedemptionCode(redemptionCode)
                .orElseThrow(() -> new RuntimeException("Código de canje no encontrado"));

        return mapToRedemptionResponse(redemption);
    }

    @Override
    public void markRedemptionAsUsed(String redemptionCode, Long referenceId) {
        logger.info("Marcando canje como usado: código={}", redemptionCode);

        LoyaltyRedemption redemption = loyaltyRedemptionRepository.findByRedemptionCode(redemptionCode)
                .orElseThrow(() -> new RuntimeException("Código de canje no encontrado"));

        if (redemption.getStatus() == RedemptionStatus.USED) {
            throw new RuntimeException("Este código ya fue utilizado");
        }

        if (redemption.isExpired()) {
            redemption.markAsExpired();
            loyaltyRedemptionRepository.save(redemption);
            throw new RuntimeException("Este código ha expirado");
        }

        redemption.markAsUsed(referenceId);
        loyaltyRedemptionRepository.save(redemption);

        logger.info("✅ Canje marcado como usado");
    }

    @Override
    public LoyaltyDashboardResponse getLoyaltyDashboard(Long userId) {
        logger.info("Generando dashboard de fidelización para usuario ID: {}", userId);

        LoyaltyProfileResponse profile = getLoyaltyProfile(userId);
        List<LoyaltyActivityResponse> recentActivities = getUserActivities(userId).stream()
                .limit(10)
                .collect(Collectors.toList());

        List<LoyaltyRedemptionResponse> activeRedemptions = getUserRedemptions(userId).stream()
                .filter(r -> r.getStatus() == RedemptionStatus.ACTIVE)
                .collect(Collectors.toList());

        List<LoyaltyRewardResponse> recommendedRewards = getAffordableRewards(userId).stream()
                .limit(5)
                .collect(Collectors.toList());

        // Calcular puntos que expiran pronto
        Integer pointsExpiringSoon = calculatePointsExpiringInNext30Days(userId);

        String motivationalMessage = generateMotivationalMessage(profile);

        return LoyaltyDashboardResponse.builder()
                .profile(profile)
                .recentActivities(recentActivities)
                .activeRedemptions(activeRedemptions)
                .recommendedRewards(recommendedRewards)
                .pointsExpiringInNext30Days(pointsExpiringSoon)
                .motivationalMessage(motivationalMessage)
                .build();
    }

    @Override
    public void processExpiredPoints() {
        logger.info("Procesando puntos expirados");

        List<LoyaltyActivity> expiredActivities = loyaltyActivityRepository.findExpiredActivities(LocalDateTime.now());
        int expiredCount = 0;

        for (LoyaltyActivity activity : expiredActivities) {
            if (!activity.getIsExpired() && !activity.getIsCancelled()) {
                activity.setIsExpired(true);
                loyaltyActivityRepository.save(activity);

                // Deducir puntos del perfil
                LoyaltyProfile profile = activity.getLoyaltyProfile();
                profile.setAvailablePoints(Math.max(0, profile.getAvailablePoints() - activity.getPointsEarned()));
                loyaltyProfileRepository.save(profile);

                expiredCount++;
            }
        }

        logger.info("✅ Puntos expirados procesados: {} actividades", expiredCount);
    }

    @Override
    public void processExpiredRedemptions() {
        logger.info("Procesando canjes expirados");

        List<LoyaltyRedemption> expiredRedemptions = loyaltyRedemptionRepository.findExpiredRedemptions(LocalDateTime.now());
        int expiredCount = 0;

        for (LoyaltyRedemption redemption : expiredRedemptions) {
            redemption.markAsExpired();
            loyaltyRedemptionRepository.save(redemption);
            expiredCount++;

            logger.info("Canje expirado: código={}", redemption.getRedemptionCode());
        }

        logger.info("✅ Canjes expirados procesados: {}", expiredCount);
    }

    @Override
    public void checkAndNotifyUpcomingRewards(Long userId) {
        LoyaltyProfile profile = loyaltyProfileRepository.findByUserId(userId)
                .orElse(null);

        if (profile == null) return;

        List<LoyaltyReward> affordableRewards = loyaltyRewardRepository.findAffordableRewards(profile.getAvailablePoints());

        if (!affordableRewards.isEmpty()) {
            // Enviar notificación de recompensas disponibles
            sendRewardsAvailableNotification(profile.getUser(), affordableRewards.size(), profile.getAvailablePoints());
        }
    }

    // Métodos auxiliares privados

    private Integer calculatePointsForActivity(ActivityType activityType, Boolean isBonus) {
        int basePoints = switch (activityType) {
            case MEMBERSHIP_PURCHASE -> 100;
            case MEMBERSHIP_RENEWAL -> 80;
            case MEMBERSHIP_UPGRADE -> 150;
            case CLASS_ATTENDANCE -> 10;
            case REFERRAL -> 200;
            case LOGIN_STREAK -> 5;
            case EARLY_RENEWAL -> 50;
            case PAYMENT_ON_TIME -> 20;
            case SOCIAL_SHARE -> 15;
            case PROFILE_COMPLETION -> 30;
        };

        return isBonus != null && isBonus ? basePoints * 2 : basePoints;
    }

    private void updateActivityStats(LoyaltyProfile profile, ActivityType activityType) {
        switch (activityType) {
            case CLASS_ATTENDANCE -> profile.setClassesAttended(profile.getClassesAttended() + 1);
            case MEMBERSHIP_RENEWAL, MEMBERSHIP_UPGRADE ->
                profile.setRenewalsCompleted(profile.getRenewalsCompleted() + 1);
            case REFERRAL -> profile.setTotalReferrals(profile.getTotalReferrals() + 1);
            case LOGIN_STREAK -> profile.setConsecutiveLoginDays(profile.getConsecutiveLoginDays() + 1);
        }
    }

    private boolean meetsMinimumTier(LoyaltyTier currentTier, String minimumTierRequired) {
        if (minimumTierRequired == null) return true;

        LoyaltyTier requiredTier = LoyaltyTier.valueOf(minimumTierRequired);
        int currentOrder = currentTier.ordinal();
        int requiredOrder = requiredTier.ordinal();

        return currentOrder >= requiredOrder;
    }

    private Integer calculatePointsExpiringInNext30Days(Long userId) {
        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        List<LoyaltyActivity> activities = loyaltyActivityRepository.findByUserIdOrderByActivityDateDesc(userId);

        return activities.stream()
                .filter(a -> !a.getIsExpired() && !a.getIsCancelled())
                .filter(a -> a.getExpirationDate() != null && a.getExpirationDate().isBefore(thirtyDaysFromNow))
                .mapToInt(LoyaltyActivity::getPointsEarned)
                .sum();
    }

    private String generateMotivationalMessage(LoyaltyProfileResponse profile) {
        return switch (profile.getCurrentTier()) {
            case BRONZE -> "¡Sigue así! Estás a " + profile.getMonthsToNextTier() + " meses de alcanzar el nivel Plata 🥈";
            case SILVER -> "¡Excelente progreso! Nivel Oro a solo " + profile.getMonthsToNextTier() + " meses de distancia 🥇";
            case GOLD -> "¡Casi en la cima! " + profile.getMonthsToNextTier() + " meses para Platino 💎";
            case PLATINUM -> "¡Eres un miembro Platino! 💎 Gracias por tu fidelidad excepcional";
            default -> "¡Sigue entrenando! 💪";
        };
    }

    // Métodos de mapeo

    private LoyaltyProfileResponse mapToProfileResponse(LoyaltyProfile profile) {
        long monthsSinceMember = ChronoUnit.MONTHS.between(profile.getMemberSince(), LocalDateTime.now());

        String nextTier = switch (profile.getCurrentTier()) {
            case BRONZE -> "PLATA";
            case SILVER -> "ORO";
            case GOLD -> "PLATINO";
            case PLATINUM -> "MÁXIMO NIVEL";
            default -> "N/A";
        };

        int monthsToNextTier = switch (profile.getCurrentTier()) {
            case BRONZE -> Math.max(0, 6 - (int) monthsSinceMember);
            case SILVER -> Math.max(0, 12 - (int) monthsSinceMember);
            case GOLD -> Math.max(0, 24 - (int) monthsSinceMember);
            case PLATINUM -> 0;
            default -> 0;
        };

        return LoyaltyProfileResponse.builder()
                .idLoyaltyProfile(profile.getIdLoyaltyProfile())
                .userId(profile.getUser().getIdUser())
                .userEmail(profile.getUser().getEmail())
                .userName(profile.getUser().getPersonalInformation().getFirstName() + " " +
                         profile.getUser().getPersonalInformation().getLastName())
                .currentTier(profile.getCurrentTier())
                .tierDisplayName(profile.getCurrentTier().name())
                .totalPoints(profile.getTotalPoints())
                .availablePoints(profile.getAvailablePoints())
                .memberSince(profile.getMemberSince() != null ? profile.getMemberSince().atStartOfDay() : null)
                .monthsAsMember((int) monthsSinceMember)
                .lastActivityDate(profile.getLastActivityDate())
                .totalActivitiesLogged(profile.getTotalActivitiesLogged())
                .consecutiveLoginDays(profile.getConsecutiveLoginDays())
                .totalReferrals(profile.getTotalReferrals())
                .classesAttended(profile.getClassesAttended())
                .renewalsCompleted(profile.getRenewalsCompleted())
                .tierBenefits(getTierBenefits(profile.getCurrentTier()))
                .monthsToNextTier(monthsToNextTier)
                .nextTier(nextTier)
                .build();
    }

    private LoyaltyActivityResponse mapToActivityResponse(LoyaltyActivity activity) {
        return LoyaltyActivityResponse.builder()
                .idLoyaltyActivity(activity.getIdLoyaltyActivity())
                .activityType(activity.getActivityType())
                .activityTypeDisplayName(getActivityTypeDisplayName(activity.getActivityType()))
                .pointsEarned(activity.getPointsEarned())
                .description(activity.getDescription())
                .referenceId(activity.getReferenceId())
                .activityDate(activity.getActivityDate())
                .isBonusActivity(activity.getIsBonusActivity())
                .expirationDate(activity.getExpirationDate())
                .isExpired(activity.getIsExpired())
                .isCancelled(activity.getIsCancelled())
                .build();
    }

    private LoyaltyRewardResponse mapToRewardResponse(LoyaltyReward reward, LoyaltyProfile profile) {
        boolean canAfford = profile.getAvailablePoints() >= reward.getPointsCost();
        boolean meetsTier = meetsMinimumTier(profile.getCurrentTier(), reward.getMinimumTierRequired());

        return LoyaltyRewardResponse.builder()
                .idLoyaltyReward(reward.getIdLoyaltyReward())
                .name(reward.getName())
                .description(reward.getDescription())
                .rewardType(reward.getRewardType())
                .rewardTypeDisplayName(getRewardTypeDisplayName(reward.getRewardType()))
                .pointsCost(reward.getPointsCost())
                .minimumTierRequired(reward.getMinimumTierRequired())
                .validityDays(reward.getValidityDays())
                .rewardValue(reward.getRewardValue())
                .termsAndConditions(reward.getTermsAndConditions())
                .canUserAfford(canAfford)
                .meetsMinimumTier(meetsTier)
                .build();
    }

    private LoyaltyRedemptionResponse mapToRedemptionResponse(LoyaltyRedemption redemption) {
        boolean isExpired = redemption.isExpired();
        boolean canBeUsed = redemption.getStatus() == RedemptionStatus.ACTIVE && !isExpired;

        return LoyaltyRedemptionResponse.builder()
                .idLoyaltyRedemption(redemption.getIdLoyaltyRedemption())
                .redemptionCode(redemption.getRedemptionCode())
                .rewardName(redemption.getReward().getName())
                .rewardType(redemption.getReward().getRewardType())
                .pointsSpent(redemption.getPointsSpent())
                .status(redemption.getStatus())
                .statusDisplayName(redemption.getStatus().name())
                .redemptionDate(redemption.getRedemptionDate())
                .expirationDate(redemption.getExpirationDate())
                .usedDate(redemption.getUsedDate())
                .notes(redemption.getNotes())
                .appliedReferenceId(redemption.getAppliedReferenceId())
                .isExpired(isExpired)
                .canBeUsed(canBeUsed)
                .build();
    }

    private String getActivityTypeDisplayName(ActivityType type) {
        return switch (type) {
            case MEMBERSHIP_PURCHASE -> "Compra de Membresía";
            case MEMBERSHIP_RENEWAL -> "Renovación de Membresía";
            case MEMBERSHIP_UPGRADE -> "Upgrade de Membresía";
            case CLASS_ATTENDANCE -> "Asistencia a Clase";
            case REFERRAL -> "Referido Exitoso";
            case LOGIN_STREAK -> "Racha de Logins";
            case EARLY_RENEWAL -> "Renovación Anticipada";
            case PAYMENT_ON_TIME -> "Pago Puntual";
            case SOCIAL_SHARE -> "Compartir en Redes";
            case PROFILE_COMPLETION -> "Completar Perfil";
        };
    }

    private String getRewardTypeDisplayName(RewardType type) {
        return switch (type) {
            case FREE_CLASS -> "Clase Gratis";
            case RENEWAL_DISCOUNT -> "Descuento en Renovación";
            case TEMPORARY_UPGRADE -> "Upgrade Temporal";
            case PERSONAL_TRAINING -> "Entrenamiento Personal";
            case GUEST_PASS -> "Pase para Invitado";
            case MERCHANDISE_DISCOUNT -> "Descuento en Mercancía";
            case NUTRITIONAL_CONSULTATION -> "Consulta Nutricional";
            case EXTENSION_DAYS -> "Días Adicionales";
        };
    }

    // Métodos de notificación por email

    private void sendTierUpgradeNotification(LoyaltyProfile profile, LoyaltyTier oldTier) {
        try {
            String subject = "🎉 ¡Felicidades! Has ascendido a " + profile.getCurrentTier();
            String body = String.format(
                "¡Increíble logro! Has ascendido de %s a %s.\n\n" +
                "Tus nuevos beneficios incluyen:\n%s\n\n" +
                "Gracias por tu fidelidad.",
                oldTier, profile.getCurrentTier(),
                getTierBenefits(profile.getCurrentTier()).getDescription()
            );

            emailService.sendEmail(profile.getUser().getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Error al enviar email de ascenso de tier: {}", e.getMessage());
        }
    }

    private void sendRedemptionConfirmationEmail(User user, LoyaltyRedemption redemption, LoyaltyReward reward) {
        try {
            String subject = "✅ Canje Confirmado - " + reward.getName();
            String body = String.format(
                "Tu canje ha sido procesado exitosamente.\n\n" +
                "Código: %s\n" +
                "Recompensa: %s\n" +
                "Puntos gastados: %d\n" +
                "Válido hasta: %s\n\n" +
                "Presenta este código para utilizar tu recompensa.",
                redemption.getRedemptionCode(),
                reward.getName(),
                redemption.getPointsSpent(),
                redemption.getExpirationDate()
            );

            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Error al enviar email de confirmación de canje: {}", e.getMessage());
        }
    }

    private void sendRewardsAvailableNotification(User user, int rewardsCount, int availablePoints) {
        try {
            String subject = "🎁 ¡Tienes recompensas disponibles!";
            String body = String.format(
                "Tienes %d puntos acumulados y hay %d recompensas que puedes canjear.\n\n" +
                "Visita tu perfil de fidelización para ver las opciones disponibles.",
                availablePoints, rewardsCount
            );

            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Error al enviar email de recompensas disponibles: {}", e.getMessage());
        }
    }
}
