package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

import java.math.BigDecimal;

/**
 * DTO RESPONSE - Para las respuestas REST
 * Este DTO se utiliza para enviar información sobre un tipo de membresía.
 * @param idMembershipType
 * @param name
 * @param description
 * @param monthlyPrice
 * @param accessToAllLocation
 * @param groupClassesSessionsIncluded
 * @param personalTrainingIncluded
 * @param specializedClassesIncluded
 */
public record MembershipTypeResponse(
        Long idMembershipType,
        MembershipTypeName name,
        String description,
        BigDecimal monthlyPrice,
        Boolean accessToAllLocation,
        Integer groupClassesSessionsIncluded,
        Integer personalTrainingIncluded,
        Boolean specializedClassesIncluded
) {
}
