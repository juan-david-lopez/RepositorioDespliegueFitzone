package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.response.PricingCalculationResponse;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.service.interfaces.IPricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el cálculo automático de precios de membresías.
 */
@RestController
@RequestMapping("/pricing")
public class PricingController {

    private static final Logger logger = LoggerFactory.getLogger(PricingController.class);
    private final IPricingService pricingService;

    public PricingController(IPricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<PricingCalculationResponse> calculatePrice(
            @RequestParam MembershipTypeName membershipType,
            @RequestParam(defaultValue = "1") int durationMonths,
            @RequestParam(defaultValue = "false") boolean isRenewal,
            @RequestParam(defaultValue = "false") boolean hasStudentDiscount) {

        logger.info("GET /pricing/calculate - Cálculo de precio solicitado");
        logger.debug("Parámetros - Tipo: {}, Duración: {} meses, Renovación: {}, Descuento estudiantil: {}",
                membershipType, durationMonths, isRenewal, hasStudentDiscount);

        try {
            PricingCalculationResponse response = pricingService.calculateMembershipPrice(
                    membershipType, durationMonths, isRenewal, hasStudentDiscount);

            logger.info("Cálculo de precio completado - Precio final: {}", response.finalPrice());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al calcular precio - Tipo: {}, Error: {}", membershipType, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
