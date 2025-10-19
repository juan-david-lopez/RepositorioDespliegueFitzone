package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta con el cálculo detallado de precios de membresía.
 */
public record PricingCalculationResponse(
        MembershipTypeName membershipType,
        int durationMonths,
        BigDecimal basePrice,
        BigDecimal subtotal,
        List<DiscountDetail> appliedDiscounts,
        BigDecimal totalDiscount,
        BigDecimal finalPrice,
        String currency
) {

    public record DiscountDetail(
            String discountType,
            String description,
            BigDecimal discountAmount,
            BigDecimal discountPercentage
    ) {}

    public static PricingCalculationResponse of(
            MembershipTypeName membershipType,
            int durationMonths,
            BigDecimal basePrice,
            BigDecimal finalPrice,
            List<DiscountDetail> discounts
    ) {
        BigDecimal subtotal = basePrice.multiply(BigDecimal.valueOf(durationMonths));
        BigDecimal totalDiscount = subtotal.subtract(finalPrice);

        return new PricingCalculationResponse(
                membershipType,
                durationMonths,
                basePrice,
                subtotal,
                discounts,
                totalDiscount,
                finalPrice,
                "COP"
        );
    }
}
