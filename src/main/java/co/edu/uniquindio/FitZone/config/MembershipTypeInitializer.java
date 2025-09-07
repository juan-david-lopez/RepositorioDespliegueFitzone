package co.edu.uniquindio.FitZone.config;

import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Clase que inicializa los tipos de membresía en la base de datos al iniciar la aplicación.
 * Implementa CommandLineRunner para ejecutar el código después de que el contexto de la aplicación se haya cargado.
 */
@Component
public class MembershipTypeInitializer implements CommandLineRunner {

    private final MembershipTypeRepository membershipTypeRepository;

    public MembershipTypeInitializer(MembershipTypeRepository membershipTypeRepository) {
        this.membershipTypeRepository = membershipTypeRepository;
    }


    @Override
    public void run(String... args) throws Exception {

        if(membershipTypeRepository.count() == 0) {

            // Membresía básica
            MembershipType basic = new MembershipType();
            basic.setName(MembershipTypeName.BASIC);
            basic.setDescription("Acceso a una sola sede con clases grupales limitadas. Ideal para quienes inician en el entrenamiento físico y desean una opción económica.");
            basic.setMonthlyPrice(new BigDecimal("50000"));
            basic.setAccessToAllLocation(false);
            basic.setGroupClassesSessionsIncluded(4);
            basic.setPersonalTrainingIncluded(0);
            basic.setSpecializedClassesIncluded(false);

            // Membresía premium
            MembershipType premium = new MembershipType();
            premium.setName(MembershipTypeName.PREMIUM);
            premium.setDescription("Acceso a todas nuestras sedes y clases grupales ilimitadas. Incluye 4 sesiones de entrenamiento personal al mes para potenciar tus resultados.");
            premium.setMonthlyPrice(new BigDecimal("70000"));
            premium.setAccessToAllLocation(true);
            premium.setGroupClassesSessionsIncluded(-1); // -1 para ilimitado
            premium.setPersonalTrainingIncluded(4);
            premium.setSpecializedClassesIncluded(false);

            // Membresía VIP
            MembershipType vip = new MembershipType();
            vip.setName(MembershipTypeName.VIP);
            vip.setDescription("Membresía exclusiva con acceso total a todas nuestras sedes, entrenamientos personales ilimitados y clases especializadas incluidas. Diseñada para quienes buscan una experiencia de fitness completa y personalizada.");
            vip.setMonthlyPrice(new BigDecimal("120000"));
            vip.setAccessToAllLocation(true);
            vip.setGroupClassesSessionsIncluded(-1);
            vip.setPersonalTrainingIncluded(-1); // -1 para ilimitado
            vip.setSpecializedClassesIncluded(true);


            membershipTypeRepository.saveAll(java.util.Set.of(basic, premium, vip));
        }
    }


}
