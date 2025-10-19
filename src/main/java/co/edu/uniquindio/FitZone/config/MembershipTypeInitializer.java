package co.edu.uniquindio.FitZone.config;

import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Inicializador para crear tipos de membresía por defecto en el sistema.
 * Se ejecuta automáticamente al arrancar la aplicación si no existen tipos de membresía.
 */
@Component
@Order(2) // Se ejecuta después de la migración
public class MembershipTypeInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MembershipTypeInitializer.class);
    private final MembershipTypeRepository membershipTypeRepository;

    public MembershipTypeInitializer(MembershipTypeRepository membershipTypeRepository) {
        this.membershipTypeRepository = membershipTypeRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("Verificando e inicializando tipos de membresía...");

        // Verificar y crear cada tipo de membresía individualmente
        createMembershipTypeIfNotExists(MembershipTypeName.BASIC);
        createMembershipTypeIfNotExists(MembershipTypeName.PREMIUM);
        createMembershipTypeIfNotExists(MembershipTypeName.ELITE);

        long totalTypes = membershipTypeRepository.count();
        logger.info("🎉 Inicialización completada. Total de tipos de membresía en el sistema: {}", totalTypes);
    }

    private void createMembershipTypeIfNotExists(MembershipTypeName typeName) {
        if (!membershipTypeRepository.existsByName(typeName)) {
            logger.info("Creando tipo de membresía: {}", typeName);

            MembershipType membershipType = switch (typeName) {
                case BASIC -> createBasicMembershipType();
                case PREMIUM -> createPremiumMembershipType();
                case ELITE -> createEliteMembershipType();
            };

            membershipTypeRepository.save(membershipType);
            logger.info("✅ Tipo de membresía {} creado exitosamente", typeName);
        } else {
            logger.info("✅ Tipo de membresía {} ya existe", typeName);
        }
    }

    private MembershipType createBasicMembershipType() {
        MembershipType basic = new MembershipType();
        basic.setName(MembershipTypeName.BASIC);
        basic.setDescription("Acceso al área de pesas, 2 horas por día de entrenamiento, máquinas cardiovasculares, vestuarios y duchas. Solo puede estar en una sucursal");
        basic.setMonthlyPrice(new BigDecimal("50000")); // $50,000 COP
        basic.setAccessToAllLocation(false); // Solo una sucursal
        basic.setGroupClassesSessionsIncluded(0); // Sin clases grupales específicas mencionadas
        basic.setPersonalTrainingIncluded(0); // Sin entrenamiento personal
        basic.setSpecializedClassesIncluded(false); // Sin clases especializadas
        return basic;
    }

    private MembershipType createPremiumMembershipType() {
        MembershipType premium = new MembershipType();
        premium.setName(MembershipTypeName.PREMIUM);
        premium.setDescription("Todo lo del plan básico + Acceso 24/7, entrenador personal (2 sesiones/mes), evaluación nutricional, invitaciones para amigos (1/mes)");
        premium.setMonthlyPrice(new BigDecimal("70000")); // $70,000 COP
        premium.setAccessToAllLocation(false); // Solo una sucursal (hereda de básico)
        premium.setGroupClassesSessionsIncluded(-1); // Ilimitadas (acceso 24/7)
        premium.setPersonalTrainingIncluded(2); // 2 sesiones de entrenamiento personal por mes
        premium.setSpecializedClassesIncluded(true); // Evaluación nutricional considerada como especializada
        return premium;
    }

    private MembershipType createEliteMembershipType() {
        MembershipType elite = new MembershipType();
        elite.setName(MembershipTypeName.ELITE);
        elite.setDescription("Todo lo del plan Premium + Puede ingresar a cualquier sucursal, entrenador personal (4 sesiones/mes), plan nutricional personalizado, invitaciones para amigos (3/mes), nutricionista incluido");
        elite.setMonthlyPrice(new BigDecimal("90000")); // $90,000 COP
        elite.setAccessToAllLocation(true); // Puede ingresar a cualquier sucursal
        elite.setGroupClassesSessionsIncluded(-1); // Ilimitadas
        elite.setPersonalTrainingIncluded(4); // 4 sesiones de entrenamiento personal por mes
        elite.setSpecializedClassesIncluded(true); // Plan nutricional + nutricionista
        return elite;
    }
}
