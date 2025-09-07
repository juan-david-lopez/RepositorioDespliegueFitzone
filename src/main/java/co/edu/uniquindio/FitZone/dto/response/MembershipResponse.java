package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.time.LocalDate;

/**
 * Representación de la respuesta para una membresía.
 * Incluye detalles como el ID de la membresía, ID del usuario,
 * tipo de membresía, ID de la ubicación, fechas de inicio y fin,
 * y el estado de la membresía.
 * @param id
 * @param userId
 * @param membershipTypeName
 * @param locationId
 * @param startDate
 * @param endDate
 * @param status
 */
public record MembershipResponse(

        Long id,
        Long userId,
        MembershipTypeName membershipTypeName,
        Long locationId,
        LocalDate startDate,
        LocalDate endDate,
        MembershipStatus status
) {
}
