package co.edu.uniquindio.FitZone.config;

import co.edu.uniquindio.FitZone.model.entity.*;
import co.edu.uniquindio.FitZone.model.enums.*;
import co.edu.uniquindio.FitZone.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Carga datos iniciales en la base de datos al iniciar la aplicación.
 * Se ejecuta solo si las tablas están vacías.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(
            FranchiseRepository franchiseRepository,
            LocationRepository locationRepository,
            MembershipTypeRepository membershipTypeRepository,
            LoyaltyRewardRepository loyaltyRewardRepository,
            UserRepository userRepository
    ) {
        return args -> {
            log.info("🔄 Verificando datos iniciales en la base de datos...");

            // 1. FRANQUICIAS
            if (franchiseRepository.count() == 0) {
                log.info("📍 Creando franquicias...");
                Franchise f1 = franchiseRepository.save(Franchise.builder()
                        .name("FitZone Central")
                        .build());

                Franchise f2 = franchiseRepository.save(Franchise.builder()
                        .name("FitZone Norte")
                        .build());

                Franchise f3 = franchiseRepository.save(Franchise.builder()
                        .name("FitZone Sur")
                        .build());

                log.info("✅ Franquicias creadas: {}", franchiseRepository.count());
            } else {
                log.info("ℹ️ Las franquicias ya existen. Omitiendo...");
            }

            // 2. UBICACIONES
            if (locationRepository.count() == 0) {
                log.info("🏢 Creando ubicaciones/sedes...");

                // Obtener franquicias existentes por nombre
                Franchise f1 = franchiseRepository.findByName("FitZone Central").orElse(null);
                Franchise f2 = franchiseRepository.findByName("FitZone Norte").orElse(null);
                Franchise f3 = franchiseRepository.findByName("FitZone Sur").orElse(null);

                if (f1 != null && f2 != null && f3 != null) {
                    locationRepository.save(Location.builder()
                            .name("Sede Centro")
                            .address("Calle 10 #15-20, Centro")
                            .phoneNumber("+57 300 123 4567")
                            .franchise(f1)
                            .isActive(true)
                            .build());

                    locationRepository.save(Location.builder()
                            .name("Sede Norte Principal")
                            .address("Carrera 45 #80-30, Norte")
                            .phoneNumber("+57 300 234 5678")
                            .franchise(f2)
                            .isActive(true)
                            .build());

                    locationRepository.save(Location.builder()
                            .name("Sede Norte Secundaria")
                            .address("Avenida 100 #50-15, Norte")
                            .phoneNumber("+57 300 345 6789")
                            .franchise(f2)
                            .isActive(true)
                            .build());

                    locationRepository.save(Location.builder()
                            .name("Sede Sur Principal")
                            .address("Calle 5 Sur #25-40, Sur")
                            .phoneNumber("+57 300 456 7890")
                            .franchise(f3)
                            .isActive(true)
                            .build());

                    locationRepository.save(Location.builder()
                            .name("Sede Centro Comercial")
                            .address("CC Unicentro Local 201")
                            .phoneNumber("+57 300 567 8901")
                            .franchise(f1)
                            .isActive(true)
                            .build());

                    log.info("✅ Ubicaciones creadas: {}", locationRepository.count());
                } else {
                    log.warn("⚠️ No se pueden crear ubicaciones: faltan franquicias.");
                    log.warn("⚠️ Franquicias encontradas: f1={}, f2={}, f3={}", f1, f2, f3);
                }
            } else {
                log.info("ℹ️ Las ubicaciones ya existen. Omitiendo...");
            }

            // 3. TIPOS DE MEMBRESÍA
            if (membershipTypeRepository.count() == 0) {
                log.info("💳 Creando tipos de membresía...");
                membershipTypeRepository.save(MembershipType.builder()
                        .name(MembershipTypeName.BASIC)
                        .description("Membresía básica con acceso a gimnasio y clases grupales limitadas")
                        .monthlyPrice(new BigDecimal("50000.00"))
                        .accessToAllLocation(false)
                        .groupClassesSessionsIncluded(4)
                        .personalTrainingIncluded(0)
                        .specializedClassesIncluded(false)
                        .build());

                membershipTypeRepository.save(MembershipType.builder()
                        .name(MembershipTypeName.PREMIUM)
                        .description("Membresía premium con acceso a todas las sedes y clases ilimitadas")
                        .monthlyPrice(new BigDecimal("120000.00"))
                        .accessToAllLocation(true)
                        .groupClassesSessionsIncluded(999)
                        .personalTrainingIncluded(2)
                        .specializedClassesIncluded(true)
                        .build());

                membershipTypeRepository.save(MembershipType.builder()
                        .name(MembershipTypeName.ELITE)
                        .description("Membresía elite con todos los beneficios y entrenamiento personalizado ilimitado")
                        .monthlyPrice(new BigDecimal("200000.00"))
                        .accessToAllLocation(true)
                        .groupClassesSessionsIncluded(999)
                        .personalTrainingIncluded(8)
                        .specializedClassesIncluded(true)
                        .build());

                log.info("✅ Tipos de membresía creados: {}", membershipTypeRepository.count());
            } else {
                log.info("ℹ️ Los tipos de membresía ya existen. Omitiendo...");
            }

            // 4. RECOMPENSAS DE FIDELIZACIÓN
            if (loyaltyRewardRepository.count() == 0) {
                log.info("🎁 Creando recompensas de fidelización...");
                loyaltyRewardRepository.save(LoyaltyReward.builder()
                        .name("Descuento 10% en Mensualidad")
                        .description("Obtén un 10% de descuento en tu próxima mensualidad")
                        .rewardType(RewardType.RENEWAL_DISCOUNT)
                        .pointsCost(500)
                        .rewardValue("10")
                        .minimumTierRequired("BRONZE")
                        .validityDays(30)
                        .isActive(true)
                        .termsAndConditions("Válido solo para la siguiente mensualidad. No acumulable con otras promociones.")
                        .build());

                loyaltyRewardRepository.save(LoyaltyReward.builder()
                        .name("Clase de Entrenamiento Personal Gratis")
                        .description("Una sesión gratuita de entrenamiento personalizado")
                        .rewardType(RewardType.PERSONAL_TRAINING)
                        .pointsCost(300)
                        .rewardValue("1")
                        .minimumTierRequired("BRONZE")
                        .validityDays(60)
                        .isActive(true)
                        .termsAndConditions("Debe reservarse con 48 horas de anticipación.")
                        .build());

                loyaltyRewardRepository.save(LoyaltyReward.builder()
                        .name("Descuento 20% en Mensualidad")
                        .description("Obtén un 20% de descuento en tu próxima mensualidad")
                        .rewardType(RewardType.RENEWAL_DISCOUNT)
                        .pointsCost(1000)
                        .rewardValue("20")
                        .minimumTierRequired("SILVER")
                        .validityDays(30)
                        .isActive(true)
                        .termsAndConditions("Válido solo para la siguiente mensualidad. No acumulable con otras promociones.")
                        .build());

                loyaltyRewardRepository.save(LoyaltyReward.builder()
                        .name("Upgrade a Premium por 1 mes")
                        .description("Disfruta de todos los beneficios Premium durante un mes")
                        .rewardType(RewardType.TEMPORARY_UPGRADE)
                        .pointsCost(1500)
                        .rewardValue("PREMIUM")
                        .minimumTierRequired("SILVER")
                        .validityDays(30)
                        .isActive(true)
                        .termsAndConditions("Válido por 30 días desde la activación.")
                        .build());

                loyaltyRewardRepository.save(LoyaltyReward.builder()
                        .name("Días Extra de Membresía")
                        .description("Obtén 30 días adicionales de membresía")
                        .rewardType(RewardType.EXTENSION_DAYS)
                        .pointsCost(2500)
                        .rewardValue("30")
                        .minimumTierRequired("GOLD")
                        .validityDays(90)
                        .isActive(true)
                        .termsAndConditions("Se aplicará después del período de facturación current.")
                        .build());

                loyaltyRewardRepository.save(LoyaltyReward.builder()
                        .name("Pase para Invitado")
                        .description("Trae un amigo al gimnasio")
                        .rewardType(RewardType.GUEST_PASS)
                        .pointsCost(200)
                        .rewardValue("1")
                        .minimumTierRequired("BRONZE")
                        .validityDays(30)
                        .isActive(true)
                        .termsAndConditions("Tu amigo puede acceder por un día completo.")
                        .build());

                log.info("✅ Recompensas creadas: {}", loyaltyRewardRepository.count());
            } else {
                log.info("ℹ️ Las recompensas ya existen. Omitiendo...");
            }

            // 5. USUARIOS DE PRUEBA
            if (userRepository.count() == 0) {
                log.info("👥 Creando usuarios de prueba...");
                String encodedPassword = passwordEncoder.encode("password123");

                // Obtener ubicaciones existentes dinámicamente
                var locations = locationRepository.findAll();

                if (locations.size() >= 3) {
                    Location loc1 = locations.get(0);
                    Location loc2 = locations.get(1);
                    Location loc3 = locations.get(2);

                    log.info("📍 Ubicaciones encontradas: {} - Total: {}",
                            locations.stream().map(Location::getName).toList(), locations.size());

                    // Admin
                    userRepository.save(User.builder()
                            .email("admin@fitzone.com")
                            .password(encodedPassword)
                            .role(UserRole.ADMIN)
                            .isActive(true)
                            .firstName("Carlos")
                            .lastName("Administrador")
                            .documentType(DocumentType.CC)
                            .documentNumber("1234567890")
                            .phoneNumber("+57 300 111 1111")
                            .birthDate(LocalDate.of(1985, 5, 15))
                            .emergencyContactName("María Admin")
                            .mainLocation(loc1.getIdLocation())
                            .build());

                    // Entrenadores
                    userRepository.save(User.builder()
                            .email("entrenador1@fitzone.com")
                            .password(encodedPassword)
                            .role(UserRole.INSTRUCTOR)
                            .isActive(true)
                            .firstName("Juan")
                            .lastName("Pérez")
                            .documentType(DocumentType.CC)
                            .documentNumber("2345678901")
                            .phoneNumber("+57 300 222 2222")
                            .birthDate(LocalDate.of(1990, 8, 20))
                            .emergencyContactName("Laura Pérez")
                            .mainLocation(loc1.getIdLocation())
                            .build());

                    userRepository.save(User.builder()
                            .email("entrenador2@fitzone.com")
                            .password(encodedPassword)
                            .role(UserRole.INSTRUCTOR)
                            .isActive(true)
                            .firstName("Ana")
                            .lastName("Martínez")
                            .documentType(DocumentType.CC)
                            .documentNumber("3456789012")
                            .phoneNumber("+57 300 333 3333")
                            .birthDate(LocalDate.of(1988, 3, 12))
                            .emergencyContactName("Pedro Martínez")
                            .mainLocation(loc2.getIdLocation())
                            .build());

                    // Miembros sin membresía (para probar registro)
                    userRepository.save(User.builder()
                            .email("usuario1@gmail.com")
                            .password(encodedPassword)
                            .role(UserRole.MEMBER)
                            .isActive(true)
                            .firstName("María")
                            .lastName("García")
                            .documentType(DocumentType.CC)
                            .documentNumber("4567890123")
                            .phoneNumber("+57 300 444 4444")
                            .birthDate(LocalDate.of(1995, 11, 25))
                            .emergencyContactName("José García")
                            .mainLocation(loc1.getIdLocation())
                            .build());

                    userRepository.save(User.builder()
                            .email("usuario2@gmail.com")
                            .password(encodedPassword)
                            .role(UserRole.MEMBER)
                            .isActive(true)
                            .firstName("Pedro")
                            .lastName("Rodríguez")
                            .documentType(DocumentType.CC)
                            .documentNumber("5678901234")
                            .phoneNumber("+57 300 555 5555")
                            .birthDate(LocalDate.of(1992, 7, 8))
                            .emergencyContactName("Carmen Rodríguez")
                            .medicalConditions("Hipertensión controlada")
                            .mainLocation(loc2.getIdLocation())
                            .build());

                    userRepository.save(User.builder()
                            .email("usuario3@gmail.com")
                            .password(encodedPassword)
                            .role(UserRole.MEMBER)
                            .isActive(true)
                            .firstName("Laura")
                            .lastName("Sánchez")
                            .documentType(DocumentType.CC)
                            .documentNumber("6789012345")
                            .phoneNumber("+57 300 666 6666")
                            .birthDate(LocalDate.of(1998, 2, 14))
                            .emergencyContactName("Antonio Sánchez")
                            .mainLocation(loc1.getIdLocation())
                            .build());

                    userRepository.save(User.builder()
                            .email("usuario4@gmail.com")
                            .password(encodedPassword)
                            .role(UserRole.MEMBER)
                            .isActive(true)
                            .firstName("Roberto")
                            .lastName("López")
                            .documentType(DocumentType.CC)
                            .documentNumber("7890123456")
                            .phoneNumber("+57 300 777 7777")
                            .birthDate(LocalDate.of(1987, 9, 30))
                            .emergencyContactName("Elena López")
                            .medicalConditions("Asma leve")
                            .mainLocation(loc3.getIdLocation())
                            .build());

                    userRepository.save(User.builder()
                            .email("usuario5@gmail.com")
                            .password(encodedPassword)
                            .role(UserRole.MEMBER)
                            .isActive(true)
                            .firstName("Sofía")
                            .lastName("Hernández")
                            .documentType(DocumentType.CC)
                            .documentNumber("8901234567")
                            .phoneNumber("+57 300 888 8888")
                            .birthDate(LocalDate.of(2000, 12, 5))
                            .emergencyContactName("Miguel Hernández")
                            .mainLocation(loc2.getIdLocation())
                            .build());

                    log.info("✅ Usuarios creados: {}", userRepository.count());
                    log.info("📝 Credenciales de prueba - Email: admin@fitzone.com | Password: password123");
                    log.info("📝 Credenciales de prueba - Email: usuario1@gmail.com | Password: password123");
                } else {
                    log.warn("⚠️ No se pueden crear usuarios: faltan ubicaciones. Encontradas: {}", locations.size());
                }
            } else {
                log.info("ℹ️ Los usuarios ya existen. Omitiendo...");
            }

            log.info("✅ Verificación de datos iniciales completada.");
        };
    }
}
