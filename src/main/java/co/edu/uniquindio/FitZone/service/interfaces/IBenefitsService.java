package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.response.BenefitsResponse;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;

/**
 * Servicio para gestionar los beneficios según el tipo de membresía.
 */
public interface IBenefitsService {

    /**
     * Obtiene los beneficios disponibles para un tipo de membresía específico.
     *
     * @param membershipType Tipo de membresía
     * @return Respuesta con los beneficios disponibles
     */
    BenefitsResponse getBenefitsByMembershipType(MembershipTypeName membershipType);

    /**
     * Verifica si un usuario puede acceder a un beneficio específico.
     *
     * @param userId ID del usuario
     * @param benefitCode Código del beneficio
     * @return true si puede acceder, false en caso contrario
     */
    boolean canAccessBenefit(Long userId, String benefitCode);

    /**
     * Obtiene los beneficios de un usuario basado en su membresía actual.
     *
     * @param userId ID del usuario
     * @return Respuesta con los beneficios del usuario
     */
    BenefitsResponse getUserBenefits(Long userId);

    /**
     * Valida el acceso a instalaciones específicas según el tipo de membresía.
     *
     * @param userId ID del usuario
     * @param facilityCode Código de la instalación
     * @return true si puede acceder, false en caso contrario
     */
    boolean canAccessFacility(Long userId, String facilityCode);
}
