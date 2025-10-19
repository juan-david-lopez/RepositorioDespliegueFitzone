package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.LoyaltyActivityRequest;
import co.edu.uniquindio.FitZone.dto.request.RedeemRewardRequest;
import co.edu.uniquindio.FitZone.dto.response.*;
import co.edu.uniquindio.FitZone.model.enums.LoyaltyTier;
import co.edu.uniquindio.FitZone.service.interfaces.ILoyaltyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el sistema de fidelización y canje de beneficios.
 */
@RestController
@RequestMapping("/api/loyalty")
@CrossOrigin(origins = "*")
public class LoyaltyController {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyController.class);

    private final ILoyaltyService loyaltyService;

    public LoyaltyController(ILoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    /**
     * Obtiene el perfil de fidelización del usuario autenticado.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getMyLoyaltyProfile(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("GET /api/loyalty/profile - Usuario ID: {}", userId);

            LoyaltyProfileResponse profile = loyaltyService.getOrCreateLoyaltyProfile(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Perfil de fidelización obtenido exitosamente",
                "data", profile
            ));
        } catch (Exception e) {
            logger.error("Error al obtener perfil de fidelización: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtiene el dashboard completo de fidelización del usuario.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getLoyaltyDashboard(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("GET /api/loyalty/dashboard - Usuario ID: {}", userId);

            LoyaltyDashboardResponse dashboard = loyaltyService.getLoyaltyDashboard(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dashboard de fidelización obtenido exitosamente",
                "data", dashboard
            ));
        } catch (Exception e) {
            logger.error("Error al obtener dashboard de fidelización: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtiene los beneficios de un nivel específico de fidelización.
     */
    @GetMapping("/tiers/{tier}/benefits")
    public ResponseEntity<?> getTierBenefits(@PathVariable String tier) {
        try {
            logger.info("GET /api/loyalty/tiers/{}/benefits", tier);

            LoyaltyTier loyaltyTier = LoyaltyTier.valueOf(tier.toUpperCase());
            TierBenefitsResponse benefits = loyaltyService.getTierBenefits(loyaltyTier);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Beneficios del nivel obtenidos exitosamente",
                "data", benefits
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Nivel de fidelización inválido"));
        } catch (Exception e) {
            logger.error("Error al obtener beneficios del nivel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtiene todas las actividades de fidelización del usuario.
     */
    @GetMapping("/activities")
    public ResponseEntity<?> getMyActivities(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("GET /api/loyalty/activities - Usuario ID: {}", userId);

            List<LoyaltyActivityResponse> activities = loyaltyService.getUserActivities(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Actividades obtenidas exitosamente",
                "data", activities,
                "total", activities.size()
            ));
        } catch (Exception e) {
            logger.error("Error al obtener actividades: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Registra manualmente una actividad de fidelización (solo para testing o casos especiales).
     */
    @PostMapping("/activities")
    public ResponseEntity<?> logActivity(@RequestBody LoyaltyActivityRequest request,
                                         Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            request.setUserId(userId);

            logger.info("POST /api/loyalty/activities - Usuario ID: {}, Tipo: {}",
                    userId, request.getActivityType());

            LoyaltyActivityResponse activity = loyaltyService.logActivity(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Actividad registrada exitosamente",
                "data", activity
            ));
        } catch (Exception e) {
            logger.error("Error al registrar actividad: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtiene todas las recompensas disponibles.
     */
    @GetMapping("/rewards")
    public ResponseEntity<?> getAllRewards(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("GET /api/loyalty/rewards - Usuario ID: {}", userId);

            List<LoyaltyRewardResponse> rewards = loyaltyService.getAllRewards(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recompensas obtenidas exitosamente",
                "data", rewards,
                "total", rewards.size()
            ));
        } catch (Exception e) {
            logger.error("Error al obtener recompensas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtiene las recompensas que el usuario puede costear con sus puntos actuales.
     */
    @GetMapping("/rewards/affordable")
    public ResponseEntity<?> getAffordableRewards(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("GET /api/loyalty/rewards/affordable - Usuario ID: {}", userId);

            List<LoyaltyRewardResponse> rewards = loyaltyService.getAffordableRewards(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recompensas alcanzables obtenidas exitosamente",
                "data", rewards,
                "total", rewards.size()
            ));
        } catch (Exception e) {
            logger.error("Error al obtener recompensas alcanzables: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtiene los detalles de una recompensa específica.
     */
    @GetMapping("/rewards/{rewardId}")
    public ResponseEntity<?> getRewardById(@PathVariable Long rewardId,
                                           Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("GET /api/loyalty/rewards/{} - Usuario ID: {}", rewardId, userId);

            LoyaltyRewardResponse reward = loyaltyService.getRewardById(rewardId, userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recompensa obtenida exitosamente",
                "data", reward
            ));
        } catch (Exception e) {
            logger.error("Error al obtener recompensa: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Canjea una recompensa.
     */
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(@RequestBody RedeemRewardRequest request,
                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("POST /api/loyalty/redeem - Usuario ID: {}, Reward ID: {}",
                    userId, request.getRewardId());

            LoyaltyRedemptionResponse redemption = loyaltyService.redeemReward(userId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Recompensa canjeada exitosamente",
                "data", redemption
            ));
        } catch (Exception e) {
            logger.error("Error al canjear recompensa: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtiene todos los canjes del usuario.
     */
    @GetMapping("/redemptions")
    public ResponseEntity<?> getMyRedemptions(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("GET /api/loyalty/redemptions - Usuario ID: {}", userId);

            List<LoyaltyRedemptionResponse> redemptions = loyaltyService.getUserRedemptions(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Canjes obtenidos exitosamente",
                "data", redemptions,
                "total", redemptions.size()
            ));
        } catch (Exception e) {
            logger.error("Error al obtener canjes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Valida un código de canje.
     */
    @GetMapping("/redemptions/validate/{code}")
    public ResponseEntity<?> validateRedemptionCode(@PathVariable String code) {
        try {
            logger.info("GET /api/loyalty/redemptions/validate/{}", code);

            LoyaltyRedemptionResponse redemption = loyaltyService.getRedemptionByCode(code);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", redemption);

            if (redemption.getCanBeUsed()) {
                response.put("message", "Código válido y listo para usar");
            } else if (redemption.getIsExpired()) {
                response.put("message", "Código expirado");
            } else if (redemption.getStatus().name().equals("USED")) {
                response.put("message", "Código ya utilizado");
            } else {
                response.put("message", "Código no disponible");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al validar código de canje: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Código de canje no encontrado"));
        }
    }

    /**
     * Marca un canje como utilizado (usado internamente por otros módulos).
     */
    @PutMapping("/redemptions/{code}/use")
    public ResponseEntity<?> markRedemptionAsUsed(@PathVariable String code,
                                                   @RequestParam(required = false) Long referenceId) {
        try {
            logger.info("PUT /api/loyalty/redemptions/{}/use", code);

            loyaltyService.markRedemptionAsUsed(code, referenceId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Canje marcado como utilizado exitosamente"
            ));
        } catch (Exception e) {
            logger.error("Error al marcar canje como utilizado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Endpoint para notificar al usuario sobre recompensas disponibles.
     */
    @PostMapping("/notify-rewards")
    public ResponseEntity<?> notifyAvailableRewards(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            logger.info("POST /api/loyalty/notify-rewards - Usuario ID: {}", userId);

            loyaltyService.checkAndNotifyUpcomingRewards(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notificación enviada exitosamente"
            ));
        } catch (Exception e) {
            logger.error("Error al enviar notificación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Método auxiliar para extraer el ID del usuario autenticado
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        // Asumiendo que el nombre del principal es el email del usuario
        String email = authentication.getName();

        // Aquí deberías obtener el ID del usuario desde el repositorio o del token JWT
        // Por ahora, esto es un placeholder que necesitarás ajustar según tu implementación
        try {
            return Long.parseLong(email);
        } catch (NumberFormatException e) {
            // Si el nombre no es un número, necesitarás buscar por email
            throw new RuntimeException("No se pudo obtener el ID del usuario");
        }
    }
}

