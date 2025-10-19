package co.edu.uniquindio.FitZone.config;

import co.edu.uniquindio.FitZone.model.entity.LoyaltyReward;
import co.edu.uniquindio.FitZone.model.enums.RewardType;
import co.edu.uniquindio.FitZone.repository.LoyaltyRewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Inicializador para crear recompensas por defecto en el sistema de fidelización.
 */
@Component
@Order(3)
public class LoyaltyRewardsInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyRewardsInitializer.class);
    private final LoyaltyRewardRepository rewardRepository;

    public LoyaltyRewardsInitializer(LoyaltyRewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("🎁 Verificando e inicializando recompensas de fidelización...");

        if (rewardRepository.count() > 0) {
            logger.info("✅ Ya existen recompensas en el sistema");
            return;
        }

        // Crear recompensas predeterminadas
        createDefaultRewards();

        logger.info("🎉 Inicialización de recompensas completada. Total: {}", rewardRepository.count());
    }

    private void createDefaultRewards() {
        // 1. Clase Gratis (100 puntos)
        LoyaltyReward freeClass = LoyaltyReward.builder()
                .name("Clase Gratis")
                .description("Una clase grupal completamente gratis en cualquier sucursal")
                .rewardType(RewardType.FREE_CLASS)
                .pointsCost(100)
                .minimumTierRequired("BRONCE")
                .validityDays(30)
                .rewardValue("1")
                .termsAndConditions("Válido para clases grupales regulares. No incluye clases especiales o eventos.")
                .build();
        rewardRepository.save(freeClass);
        logger.info("✅ Recompensa creada: Clase Gratis");

        // 2. Descuento 10% en Renovación (150 puntos)
        LoyaltyReward discount10 = LoyaltyReward.builder()
                .name("10% Descuento en Renovación")
                .description("Obtén 10% de descuento en tu próxima renovación de membresía")
                .rewardType(RewardType.RENEWAL_DISCOUNT)
                .pointsCost(150)
                .minimumTierRequired("BRONCE")
                .validityDays(60)
                .rewardValue("10")
                .termsAndConditions("Aplicable solo en renovaciones. No acumulable con otras promociones.")
                .build();
        rewardRepository.save(discount10);
        logger.info("✅ Recompensa creada: 10% Descuento");

        // 3. Pase para Invitado (80 puntos)
        LoyaltyReward guestPass = LoyaltyReward.builder()
                .name("Pase para Invitado")
                .description("Invita a un amigo a entrenar contigo por un día")
                .rewardType(RewardType.GUEST_PASS)
                .pointsCost(80)
                .minimumTierRequired("BRONCE")
                .validityDays(30)
                .rewardValue("1")
                .termsAndConditions("El invitado debe presentar identificación. Un solo uso.")
                .build();
        rewardRepository.save(guestPass);
        logger.info("✅ Recompensa creada: Pase para Invitado");

        // 4. Sesión de Entrenamiento Personal (250 puntos)
        LoyaltyReward personalTraining = LoyaltyReward.builder()
                .name("Sesión de Entrenamiento Personal")
                .description("Una sesión de 1 hora con un entrenador personal certificado")
                .rewardType(RewardType.PERSONAL_TRAINING)
                .pointsCost(250)
                .minimumTierRequired("PLATA")
                .validityDays(45)
                .rewardValue("1")
                .termsAndConditions("Sujeto a disponibilidad del entrenador. Reserva con anticipación.")
                .build();
        rewardRepository.save(personalTraining);
        logger.info("✅ Recompensa creada: Entrenamiento Personal");

        // 5. Upgrade Temporal a Premium (200 puntos)
        LoyaltyReward upgradePremium = LoyaltyReward.builder()
                .name("Upgrade Temporal a Premium")
                .description("Disfruta de todos los beneficios Premium por 7 días")
                .rewardType(RewardType.TEMPORARY_UPGRADE)
                .pointsCost(200)
                .minimumTierRequired("BRONCE")
                .validityDays(7)
                .rewardValue("PREMIUM-7")
                .termsAndConditions("El upgrade se aplica inmediatamente por 7 días consecutivos.")
                .build();
        rewardRepository.save(upgradePremium);
        logger.info("✅ Recompensa creada: Upgrade Temporal Premium");

        // 6. Consulta Nutricional (300 puntos)
        LoyaltyReward nutritionalConsult = LoyaltyReward.builder()
                .name("Consulta Nutricional")
                .description("Consulta personalizada con nutricionista deportivo")
                .rewardType(RewardType.NUTRITIONAL_CONSULTATION)
                .pointsCost(300)
                .minimumTierRequired("PLATA")
                .validityDays(60)
                .rewardValue("1")
                .termsAndConditions("Incluye plan nutricional básico. Duración 45 minutos.")
                .build();
        rewardRepository.save(nutritionalConsult);
        logger.info("✅ Recompensa creada: Consulta Nutricional");

        // 7. 5 Días Extra de Membresía (120 puntos)
        LoyaltyReward extensionDays = LoyaltyReward.builder()
                .name("5 Días Extra de Membresía")
                .description("Extiende tu membresía actual por 5 días adicionales")
                .rewardType(RewardType.EXTENSION_DAYS)
                .pointsCost(120)
                .minimumTierRequired("BRONCE")
                .validityDays(90)
                .rewardValue("5")
                .termsAndConditions("Se suman a tu fecha de vencimiento actual.")
                .build();
        rewardRepository.save(extensionDays);
        logger.info("✅ Recompensa creada: Extensión de Membresía");

        // 8. 20% Descuento en Renovación (Premium) (350 puntos)
        LoyaltyReward discount20 = LoyaltyReward.builder()
                .name("20% Descuento en Renovación")
                .description("Descuento especial del 20% en tu próxima renovación")
                .rewardType(RewardType.RENEWAL_DISCOUNT)
                .pointsCost(350)
                .minimumTierRequired("ORO")
                .validityDays(60)
                .rewardValue("20")
                .termsAndConditions("Solo para miembros Oro y Platino. No acumulable.")
                .build();
        rewardRepository.save(discount20);
        logger.info("✅ Recompensa creada: 20% Descuento Premium");

        // 9. Upgrade Temporal a Elite (400 puntos)
        LoyaltyReward upgradeElite = LoyaltyReward.builder()
                .name("Upgrade Temporal a Elite")
                .description("Experimenta el nivel Elite por 7 días con todos sus beneficios")
                .rewardType(RewardType.TEMPORARY_UPGRADE)
                .pointsCost(400)
                .minimumTierRequired("ORO")
                .validityDays(7)
                .rewardValue("ELITE-7")
                .termsAndConditions("Acceso a todas las sucursales y beneficios Elite por 7 días.")
                .build();
        rewardRepository.save(upgradeElite);
        logger.info("✅ Recompensa creada: Upgrade Temporal Elite");

        // 10. Pack 3 Clases Gratis (250 puntos)
        LoyaltyReward classPackage = LoyaltyReward.builder()
                .name("Pack de 3 Clases Gratis")
                .description("Tres clases grupales gratis para usar cuando quieras")
                .rewardType(RewardType.FREE_CLASS)
                .pointsCost(250)
                .minimumTierRequired("PLATA")
                .validityDays(60)
                .rewardValue("3")
                .termsAndConditions("Las 3 clases deben usarse dentro del periodo de validez.")
                .build();
        rewardRepository.save(classPackage);
        logger.info("✅ Recompensa creada: Pack 3 Clases");
    }
}

