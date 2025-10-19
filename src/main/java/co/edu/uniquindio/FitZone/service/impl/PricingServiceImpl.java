package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.response.PricingCalculationResponse;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IPricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de cálculo automático de precios.
 * Incluye descuentos por duración, renovación y estudiante.
 */
@Service
public class PricingServiceImpl implements IPricingService {

    private static final Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);

    // Constantes para descuentos
    private static final BigDecimal STUDENT_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(0.15); // 15%
    private static final BigDecimal RENEWAL_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(0.10); // 10%
    private static final BigDecimal THREE_MONTH_DISCOUNT = BigDecimal.valueOf(0.05); // 5%
    private static final BigDecimal SIX_MONTH_DISCOUNT = BigDecimal.valueOf(0.10); // 10%
    private static final BigDecimal TWELVE_MONTH_DISCOUNT = BigDecimal.valueOf(0.20); // 20%

    private final MembershipTypeRepository membershipTypeRepository;

    public PricingServiceImpl(MembershipTypeRepository membershipTypeRepository) {
        this.membershipTypeRepository = membershipTypeRepository;
    }

    @Override
    public PricingCalculationResponse calculateMembershipPrice(
            MembershipTypeName membershipType,
            int durationMonths,
            boolean isRenewal,
            boolean hasStudentDiscount) {

        logger.info("Calculando precio para membresía - Tipo: {}, Duración: {} meses, Renovación: {}, Descuento estudiantil: {}",
                membershipType, durationMonths, isRenewal, hasStudentDiscount);

        // Obtener precio base del tipo de membresía
        MembershipType type = membershipTypeRepository.findByName(membershipType)
                .orElseThrow(() -> new RuntimeException("Tipo de membresía no encontrado: " + membershipType));

        BigDecimal basePrice = type.getMonthlyPrice();
        BigDecimal subtotal = basePrice.multiply(BigDecimal.valueOf(durationMonths));
        BigDecimal finalPrice = subtotal;

        List<PricingCalculationResponse.DiscountDetail> appliedDiscounts = new ArrayList<>();

        // Aplicar descuento por duración
        BigDecimal durationDiscountAmount = calculateDurationDiscountAmount(subtotal, durationMonths);
        if (durationDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal durationDiscountPercentage = getDurationDiscountPercentage(durationMonths);
            appliedDiscounts.add(new PricingCalculationResponse.DiscountDetail(
                    "DURATION",
                    "Descuento por " + durationMonths + " meses",
                    durationDiscountAmount,
                    durationDiscountPercentage.multiply(BigDecimal.valueOf(100))
            ));
            finalPrice = finalPrice.subtract(durationDiscountAmount);
        }

        // Aplicar descuento estudiantil
        if (hasStudentDiscount) {
            BigDecimal studentDiscountAmount = finalPrice.multiply(STUDENT_DISCOUNT_PERCENTAGE);
            appliedDiscounts.add(new PricingCalculationResponse.DiscountDetail(
                    "STUDENT",
                    "Descuento estudiantil",
                    studentDiscountAmount,
                    STUDENT_DISCOUNT_PERCENTAGE.multiply(BigDecimal.valueOf(100))
            ));
            finalPrice = finalPrice.subtract(studentDiscountAmount);
        }

        // Aplicar descuento por renovación
        if (isRenewal) {
            BigDecimal renewalDiscountAmount = finalPrice.multiply(RENEWAL_DISCOUNT_PERCENTAGE);
            appliedDiscounts.add(new PricingCalculationResponse.DiscountDetail(
                    "RENEWAL",
                    "Descuento por renovación",
                    renewalDiscountAmount,
                    RENEWAL_DISCOUNT_PERCENTAGE.multiply(BigDecimal.valueOf(100))
            ));
            finalPrice = finalPrice.subtract(renewalDiscountAmount);
        }

        // Redondear a 2 decimales
        finalPrice = finalPrice.setScale(2, RoundingMode.HALF_UP);

        logger.info("Cálculo completado - Precio base: {}, Precio final: {}, Descuentos aplicados: {}",
                basePrice, finalPrice, appliedDiscounts.size());

        return PricingCalculationResponse.of(
                membershipType,
                durationMonths,
                basePrice,
                finalPrice,
                appliedDiscounts
        );
    }

    @Override
    public BigDecimal calculateDurationDiscount(BigDecimal basePrice, int months) {
        return calculateDurationDiscountAmount(basePrice, months);
    }

    @Override
    public BigDecimal calculateStudentDiscount(BigDecimal totalPrice) {
        return totalPrice.multiply(STUDENT_DISCOUNT_PERCENTAGE);
    }

    @Override
    public BigDecimal calculateRenewalDiscount(BigDecimal totalPrice) {
        return totalPrice.multiply(RENEWAL_DISCOUNT_PERCENTAGE);
    }

    private BigDecimal calculateDurationDiscountAmount(BigDecimal subtotal, int months) {
        BigDecimal discountPercentage = getDurationDiscountPercentage(months);
        return subtotal.multiply(discountPercentage);
    }

    private BigDecimal getDurationDiscountPercentage(int months) {
        if (months >= 12) {
            return TWELVE_MONTH_DISCOUNT;
        } else if (months >= 6) {
            return SIX_MONTH_DISCOUNT;
        } else if (months >= 3) {
            return THREE_MONTH_DISCOUNT;
        }
        return BigDecimal.ZERO;
    }
}
