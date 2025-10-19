package co.edu.uniquindio.FitZone.dto.request;

import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

/**
 * DTO para la solicitud de renovación de membresía.
 * @param userId ID del usuario que renueva.
 * @param newMembershipType Tipo de membresía a renovar.
 * @param durationMonths Duración en meses de la renovación.
 * @param hasStudentDiscount Indica si tiene descuento estudiantil.
 * @param paymentIntentId ID del payment intent de Stripe.
 */
public record RenewMembershipRequest(
        Long userId,
        MembershipTypeName newMembershipType,
        int durationMonths,
        boolean hasStudentDiscount,
        String paymentIntentId
) {}
