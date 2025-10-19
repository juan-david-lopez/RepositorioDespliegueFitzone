package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.response.BenefitsResponse;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.service.interfaces.IBenefitsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar los beneficios según el tipo de membresía.
 */
@RestController
@RequestMapping("/benefits")
public class BenefitsController {

    private static final Logger logger = LoggerFactory.getLogger(BenefitsController.class);
    private final IBenefitsService benefitsService;

    public BenefitsController(IBenefitsService benefitsService) {
        this.benefitsService = benefitsService;
    }

    @GetMapping("/membership-type/{membershipType}")
    public ResponseEntity<BenefitsResponse> getBenefitsByMembershipType(@PathVariable MembershipTypeName membershipType) {
        logger.debug("GET /benefits/membership-type/{} - Consultando beneficios por tipo", membershipType);

        try {
            BenefitsResponse benefits = benefitsService.getBenefitsByMembershipType(membershipType);
            return ResponseEntity.ok(benefits);
        } catch (Exception e) {
            logger.error("Error al consultar beneficios por tipo - Tipo: {}, Error: {}", membershipType, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BenefitsResponse> getUserBenefits(@PathVariable Long userId) {
        logger.debug("GET /benefits/user/{} - Consultando beneficios del usuario", userId);

        try {
            BenefitsResponse benefits = benefitsService.getUserBenefits(userId);
            return ResponseEntity.ok(benefits);
        } catch (Exception e) {
            logger.error("Error al consultar beneficios del usuario - ID: {}, Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/benefit/{benefitCode}/access")
    public ResponseEntity<Boolean> canAccessBenefit(@PathVariable Long userId, @PathVariable String benefitCode) {
        logger.debug("GET /benefits/user/{}/benefit/{}/access - Verificando acceso a beneficio", userId, benefitCode);

        try {
            boolean canAccess = benefitsService.canAccessBenefit(userId, benefitCode);
            return ResponseEntity.ok(canAccess);
        } catch (Exception e) {
            logger.error("Error al verificar acceso a beneficio - Usuario: {}, Beneficio: {}, Error: {}",
                    userId, benefitCode, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/facility/{facilityCode}/access")
    public ResponseEntity<Boolean> canAccessFacility(@PathVariable Long userId, @PathVariable String facilityCode) {
        logger.debug("GET /benefits/user/{}/facility/{}/access - Verificando acceso a instalación", userId, facilityCode);

        try {
            boolean canAccess = benefitsService.canAccessFacility(userId, facilityCode);
            return ResponseEntity.ok(canAccess);
        } catch (Exception e) {
            logger.error("Error al verificar acceso a instalación - Usuario: {}, Instalación: {}, Error: {}",
                    userId, facilityCode, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
