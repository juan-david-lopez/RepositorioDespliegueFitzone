package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.AutoRenewalSettingsRequest;
import co.edu.uniquindio.FitZone.dto.response.AutoRenewalResponse;
import co.edu.uniquindio.FitZone.service.interfaces.IAutoRenewalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/membership")
@PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
public class AutoRenewalController {

    private static final Logger logger = LoggerFactory.getLogger(AutoRenewalController.class);
    private final IAutoRenewalService autoRenewalService;

    public AutoRenewalController(IAutoRenewalService autoRenewalService) {
        this.autoRenewalService = autoRenewalService;
    }

    @GetMapping("/auto-renewal-preferences")
    public ResponseEntity<AutoRenewalResponse> getAutoRenewalSettings(@PathVariable Long userId) {
        logger.debug("GET /api/v1/users/{}/membership/auto-renewal-preferences", userId);
        try {
            AutoRenewalResponse response = autoRenewalService.getAutoRenewalSettings(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar preferencias de auto-renovación para usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/auto-renewal-preferences")
    public ResponseEntity<AutoRenewalResponse> updateAutoRenewalSettings(
            @PathVariable Long userId,
            @RequestBody AutoRenewalSettingsRequest request) {
        logger.info("PUT /api/v1/users/{}/membership/auto-renewal-preferences", userId);
        try {
            AutoRenewalResponse response = autoRenewalService.updateAutoRenewalSettings(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al actualizar preferencias de auto-renovación para usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check-expiration")
    public ResponseEntity<Void> checkExpiration(@PathVariable Long userId) {
        logger.debug("GET /api/v1/users/{}/membership/check-expiration", userId);
        try {
            // Trigger manual check for this user's membership expiration
            autoRenewalService.checkExpiringMemberships();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error al verificar expiración para usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

