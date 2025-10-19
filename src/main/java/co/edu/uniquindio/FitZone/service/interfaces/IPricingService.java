package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.response.PricingCalculationResponse;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.math.BigDecimal;

/**
 * Servicio para el cálculo automático de precios de membresías.
 * Incluye descuentos, promociones y cálculos basados en duración.
 */
public interface IPricingService {

    /**
     * Calcula el precio total de una membresía incluyendo descuentos y promociones.
     *
     * @param membershipType Tipo de membresía
     * @param durationMonths Duración en meses
     * @param isRenewal Si es una renovación
     * @param hasStudentDiscount Si aplica descuento estudiantil
     * @return Respuesta con el cálculo detallado del precio
     */
    PricingCalculationResponse calculateMembershipPrice(
            MembershipTypeName membershipType,
            int durationMonths,
            boolean isRenewal,
            boolean hasStudentDiscount
    );

    /**
     * Calcula descuento por duración (descuentos por pagar varios meses).
     *
     * @param basePrice Precio base mensual
     * @param months Número de meses
     * @return Precio con descuento aplicado
     */
    BigDecimal calculateDurationDiscount(BigDecimal basePrice, int months);

    /**
     * Calcula descuento estudiantil.
     *
     * @param totalPrice Precio total
     * @return Precio con descuento estudiantil aplicado
     */
    BigDecimal calculateStudentDiscount(BigDecimal totalPrice);

    /**
     * Calcula descuento por renovación.
     *
     * @param totalPrice Precio total
     * @return Precio con descuento por renovación aplicado
     */
    BigDecimal calculateRenewalDiscount(BigDecimal totalPrice);
}
