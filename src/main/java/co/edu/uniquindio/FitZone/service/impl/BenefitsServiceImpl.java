package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.response.BenefitsResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IBenefitsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de beneficios según el tipo de membresía.
 */
@Service
public class BenefitsServiceImpl implements IBenefitsService {

    private static final Logger logger = LoggerFactory.getLogger(BenefitsServiceImpl.class);
    private final UserRepository userRepository;

    public BenefitsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public BenefitsResponse getBenefitsByMembershipType(MembershipTypeName membershipType) {
        logger.debug("Obteniendo beneficios para tipo de membresía: {}", membershipType);

        return switch (membershipType) {
            case BASIC -> BenefitsResponse.createBasicBenefits();
            case PREMIUM -> BenefitsResponse.createPremiumBenefits();
            case ELITE -> BenefitsResponse.createEliteBenefits();
        };
    }

    @Override
    public boolean canAccessBenefit(Long userId, String benefitCode) {
        logger.debug("Verificando acceso a beneficio - Usuario ID: {}, Beneficio: {}", userId, benefitCode);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Membership membership = user.getMembership();
        if (membership == null || membership.getStatus() != MembershipStatus.ACTIVE) {
            logger.warn("Usuario sin membresía activa - ID: {}", userId);
            return false;
        }

        BenefitsResponse benefits = getBenefitsByMembershipType(membership.getType().getName());
        boolean hasAccess = benefits.benefits().stream()
                .anyMatch(benefit -> benefit.code().equals(benefitCode) && benefit.isActive());

        logger.debug("Acceso a beneficio {} para usuario {}: {}", benefitCode, userId, hasAccess);
        return hasAccess;
    }

    @Override
    public BenefitsResponse getUserBenefits(Long userId) {
        logger.debug("Obteniendo beneficios del usuario ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Membership membership = user.getMembership();
        if (membership == null || membership.getStatus() != MembershipStatus.ACTIVE) {
            logger.warn("Usuario sin membresía activa - ID: {}", userId);
            return BenefitsResponse.createBasicBenefits(); // Beneficios mínimos si no tiene membresía activa
        }

        return getBenefitsByMembershipType(membership.getType().getName());
    }

    @Override
    public boolean canAccessFacility(Long userId, String facilityCode) {
        logger.debug("Verificando acceso a instalación - Usuario ID: {}, Instalación: {}", userId, facilityCode);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Membership membership = user.getMembership();
        if (membership == null || membership.getStatus() != MembershipStatus.ACTIVE) {
            logger.warn("Usuario sin membresía activa - ID: {}", userId);
            return false;
        }

        BenefitsResponse benefits = getBenefitsByMembershipType(membership.getType().getName());
        boolean hasAccess = benefits.accessibleFacilities().stream()
                .anyMatch(facility -> facility.code().equals(facilityCode));

        logger.debug("Acceso a instalación {} para usuario {}: {}", facilityCode, userId, hasAccess);
        return hasAccess;
    }
}

