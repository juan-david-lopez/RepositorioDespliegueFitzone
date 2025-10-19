package co.edu.uniquindio.FitZone.quality;

import co.edu.uniquindio.FitZone.dto.request.CreateReservationRequest;
import co.edu.uniquindio.FitZone.dto.response.ReservationResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.PersonalInformation;
import co.edu.uniquindio.FitZone.model.entity.Reservation;
import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.ReservationRepository;
import co.edu.uniquindio.FitZone.repository.UserBaseRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IReservationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🎯 PRUEBAS DE CALIDAD - Sistema de Reservas
 *
 * Estas pruebas validan aspectos de calidad como rendimiento,
 * concurrencia, validación de datos y seguridad.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de Calidad - Sistema de Reservas")
class ReservationQualityTest {

    @Autowired
    private IReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserBaseRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MembershipTypeRepository membershipTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserBase adminUser;
    private UserBase eliteUser;
    private UserBase premiumUser;
    private MembershipType eliteType;
    private MembershipType premiumType;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpiar datos
        reservationRepository.deleteAll();
        membershipRepository.deleteAll();
        userRepository.deleteAll();
        membershipTypeRepository.deleteAll();

        // Crear tipos de membresía
        setupMembershipTypes();

        // Crear usuarios de prueba
        setupTestUsers();
    }

    private void setupMembershipTypes() {
        eliteType = new MembershipType();
        eliteType.setName(MembershipTypeName.ELITE);
        eliteType.setDescription("Elite Membership");
        eliteType.setPrice(70000.0);
        eliteType = membershipTypeRepository.save(eliteType);

        premiumType = new MembershipType();
        premiumType.setName(MembershipTypeName.PREMIUM);
        premiumType.setDescription("Premium Membership");
        premiumType.setPrice(50000.0);
        premiumType = membershipTypeRepository.save(premiumType);
    }

    private void setupTestUsers() {
        // Admin
        adminUser = new UserBase();
        adminUser.setEmail("admin.quality@fitzone.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setIsActive(true);

        PersonalInformation adminInfo = new PersonalInformation();
        adminInfo.setFirstName("Admin");
        adminInfo.setLastName("Quality");
        adminInfo.setPhoneNumber("3001111111");
        adminUser.setPersonalInformation(adminInfo);

        adminUser = userRepository.save(adminUser);

        Membership adminMembership = new Membership();
        adminMembership.setUserId(adminUser.getIdUser());
        adminMembership.setMembershipTypeId(eliteType.getIdMembershipType());
        adminMembership.setStartDate(LocalDate.now());
        adminMembership.setEndDate(LocalDate.now().plusMonths(1));
        membershipRepository.save(adminMembership);

        // Usuario Elite
        eliteUser = new UserBase();
        eliteUser.setEmail("elite.quality@fitzone.com");
        eliteUser.setPassword(passwordEncoder.encode("elite123"));
        eliteUser.setRole(UserRole.USER);
        eliteUser.setIsActive(true);

        PersonalInformation eliteInfo = new PersonalInformation();
        eliteInfo.setFirstName("Elite");
        eliteInfo.setLastName("User");
        eliteInfo.setPhoneNumber("3002222222");
        eliteUser.setPersonalInformation(eliteInfo);

        eliteUser = userRepository.save(eliteUser);

        Membership eliteMembership = new Membership();
        eliteMembership.setUserId(eliteUser.getIdUser());
        eliteMembership.setMembershipTypeId(eliteType.getIdMembershipType());
        eliteMembership.setStartDate(LocalDate.now());
        eliteMembership.setEndDate(LocalDate.now().plusMonths(1));
        membershipRepository.save(eliteMembership);

        // Usuario Premium
        premiumUser = new UserBase();
        premiumUser.setEmail("premium.quality@fitzone.com");
        premiumUser.setPassword(passwordEncoder.encode("premium123"));
        premiumUser.setRole(UserRole.USER);
        premiumUser.setIsActive(true);

        PersonalInformation premiumInfo = new PersonalInformation();
        premiumInfo.setFirstName("Premium");
        premiumInfo.setLastName("User");
        premiumInfo.setPhoneNumber("3003333333");
        premiumUser.setPersonalInformation(premiumInfo);

        premiumUser = userRepository.save(premiumUser);

        Membership premiumMembership = new Membership();
        premiumMembership.setUserId(premiumUser.getIdUser());
        premiumMembership.setMembershipTypeId(premiumType.getIdMembershipType());
        premiumMembership.setStartDate(LocalDate.now());
        premiumMembership.setEndDate(LocalDate.now().plusMonths(1));
        membershipRepository.save(premiumMembership);
    }

    // ==================== PRUEBAS DE CALIDAD ====================

    @Test
    @Order(1)
    @DisplayName("1️⃣ QUALITY: Rendimiento - Crear 100 reservas en menos de 5 segundos")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testPerformanceCreate100Reservations() throws Exception {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act - Crear 100 clases grupales
        for (int i = 0; i < 100; i++) {
            CreateReservationRequest request = new CreateReservationRequest();
            request.setReservationType("GROUP_CLASS");
            request.setClassName("Performance Test Class " + i);
            request.setStartDateTime(LocalDateTime.now().plusDays(i + 1).toString());
            request.setEndDateTime(LocalDateTime.now().plusDays(i + 1).plusHours(1).toString());
            request.setMaxCapacity(20);
            request.setLocationId(1L);

            reservationService.createReservation(adminUser.getIdUser(), request);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert
        List<Reservation> allReservations = reservationRepository.findAll();
        assertEquals(100, allReservations.size(), "Debe haber creado 100 reservas");
        assertTrue(duration < 5000, "Debe completarse en menos de 5 segundos. Duración: " + duration + "ms");

        System.out.println("✅ Rendimiento: 100 reservas creadas en " + duration + "ms");
    }

    @Test
    @Order(2)
    @DisplayName("2️⃣ QUALITY: Concurrencia - 50 usuarios uniéndose simultáneamente a clase grupal")
    void testConcurrency50UsersJoiningClass() throws Exception {
        // Arrange - Crear clase grupal con cupo de 50
        Reservation groupClass = Reservation.builder()
                .user(adminUser)
                .reservationType("GROUP_CLASS")
                .className("Concurrency Test")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status("CONFIRMED")
                .isGroup(true)
                .maxCapacity(50)
                .participantUserIds(new ArrayList<>())
                .build();
        groupClass = reservationRepository.save(groupClass);
        final Long classId = groupClass.getId();

        // Crear 50 usuarios
        List<UserBase> users = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            UserBase user = createTestUser("concurrent" + i + "@test.com", eliteType);
            users.add(user);
        }

        // Act - 50 usuarios intentan unirse simultáneamente
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (UserBase user : users) {
            Future<Boolean> future = executor.submit(() -> {
                try {
                    reservationService.joinGroupClass(user.getIdUser(), classId);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
            futures.add(future);
        }

        // Esperar a que todos terminen
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(10, TimeUnit.SECONDS)) {
                successCount++;
            }
        }
        executor.shutdown();

        // Assert
        Reservation updated = reservationRepository.findById(classId).orElseThrow();
        assertEquals(50, successCount, "Los 50 usuarios deben unirse exitosamente");
        assertEquals(50, updated.getParticipantUserIds().size(),
                "Debe haber exactamente 50 participantes");
        assertFalse(updated.hasAvailableCapacity(), "La clase debe estar llena");

        System.out.println("✅ Concurrencia: 50 usuarios se unieron correctamente");
    }

    @Test
    @Order(3)
    @DisplayName("3️⃣ QUALITY: Validación de Datos - Rechazar fechas inválidas")
    void testDataValidationInvalidDates() {
        // Test 1: Fecha en el pasado
        CreateReservationRequest pastRequest = new CreateReservationRequest();
        pastRequest.setReservationType("GROUP_CLASS");
        pastRequest.setStartDateTime(LocalDateTime.now().minusDays(1).toString());
        pastRequest.setEndDateTime(LocalDateTime.now().toString());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(adminUser.getIdUser(), pastRequest),
                "Debe rechazar fechas en el pasado");

        // Test 2: Fecha de fin antes de inicio
        CreateReservationRequest invalidOrderRequest = new CreateReservationRequest();
        invalidOrderRequest.setReservationType("GROUP_CLASS");
        invalidOrderRequest.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(2).toString());
        invalidOrderRequest.setEndDateTime(LocalDateTime.now().plusDays(1).toString());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(adminUser.getIdUser(), invalidOrderRequest),
                "Debe rechazar cuando fecha fin es antes de fecha inicio");

        System.out.println("✅ Validación: Fechas inválidas rechazadas correctamente");
    }

    @Test
    @Order(4)
    @DisplayName("4️⃣ QUALITY: Integridad de Datos - Verificar consistencia en transacciones")
    void testDataIntegrityTransactions() throws Exception {
        // Arrange - Crear clase grupal
        CreateReservationRequest request = new CreateReservationRequest();
        request.setReservationType("GROUP_CLASS");
        request.setClassName("Integrity Test");
        request.setStartDateTime(LocalDateTime.now().plusDays(1).toString());
        request.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(1).toString());
        request.setMaxCapacity(10);

        // Act
        ReservationResponse created = reservationService.createReservation(adminUser.getIdUser(), request);

        // Assert - Verificar que los datos persisten correctamente
        Reservation fromDb = reservationRepository.findById(created.getId()).orElseThrow();

        assertEquals(created.getId(), fromDb.getId());
        assertEquals(created.getClassName(), fromDb.getClassName());
        assertEquals(created.getMaxCapacity(), fromDb.getMaxCapacity());
        assertEquals(created.getIsGroup(), fromDb.getIsGroup());
        assertNotNull(fromDb.getCreatedAt(), "CreatedAt debe estar establecido");
        assertNotNull(fromDb.getParticipantUserIds(), "ParticipantUserIds no debe ser null");
        assertTrue(fromDb.getParticipantUserIds().contains(adminUser.getIdUser()),
                "El creador debe estar en la lista de participantes");

        System.out.println("✅ Integridad: Datos persisten correctamente");
    }

    @Test
    @Order(5)
    @DisplayName("5️⃣ QUALITY: Límites del Sistema - Clase con capacidad máxima (100 personas)")
    void testSystemLimitsMaxCapacity() throws Exception {
        // Arrange - Crear clase con capacidad máxima
        Reservation largeClass = Reservation.builder()
                .user(adminUser)
                .reservationType("GROUP_CLASS")
                .className("Large Class")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status("CONFIRMED")
                .isGroup(true)
                .maxCapacity(100)
                .participantUserIds(new ArrayList<>())
                .build();
        largeClass = reservationRepository.save(largeClass);
        final Long classId = largeClass.getId();

        // Act - Crear 100 usuarios y unirlos
        List<UserBase> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            UserBase user = createTestUser("maxcap" + i + "@test.com", eliteType);
            users.add(user);
            reservationService.joinGroupClass(user.getIdUser(), classId);
        }

        // Assert
        Reservation fullClass = reservationRepository.findById(classId).orElseThrow();
        assertEquals(100, fullClass.getParticipantUserIds().size());
        assertFalse(fullClass.hasAvailableCapacity(), "No debe tener cupo disponible");

        // Intentar unir un usuario más (debe fallar)
        UserBase extraUser = createTestUser("extra@test.com", eliteType);
        assertThrows(IllegalStateException.class,
                () -> reservationService.joinGroupClass(extraUser.getIdUser(), classId),
                "No debe permitir exceder la capacidad máxima");

        System.out.println("✅ Límites: Sistema maneja correctamente capacidad máxima de 100");
    }

    @Test
    @Order(6)
    @DisplayName("6️⃣ QUALITY: Seguridad - Usuario no puede ver reservas de otros")
    void testSecurityUserCannotSeeOthersReservations() throws Exception {
        // Arrange - Admin crea reserva
        CreateReservationRequest adminRequest = new CreateReservationRequest();
        adminRequest.setReservationType("PERSONAL_TRAINING");
        adminRequest.setStartDateTime(LocalDateTime.now().plusDays(1).toString());
        adminRequest.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(1).toString());
        adminRequest.setInstructorId(2L);

        ReservationResponse adminReservation = reservationService.createReservation(
                adminUser.getIdUser(), adminRequest);

        // Act - Elite user intenta obtener sus reservas
        List<ReservationResponse> eliteReservations = reservationService.getUpcomingReservations(
                eliteUser.getIdUser());

        // Assert - Elite user NO debe ver la reserva del admin
        assertTrue(eliteReservations.stream()
                        .noneMatch(r -> r.getId().equals(adminReservation.getId())),
                "Usuario no debe ver reservas de otros usuarios");

        System.out.println("✅ Seguridad: Aislamiento de datos verificado");
    }

    @Test
    @Order(7)
    @DisplayName("7️⃣ QUALITY: Rendimiento de Consulta - Obtener 1000 reservas en menos de 1 segundo")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testQueryPerformance1000Reservations() throws Exception {
        // Arrange - Crear 1000 reservas
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Reservation res = Reservation.builder()
                    .user(eliteUser)
                    .reservationType("GROUP_CLASS")
                    .className("Query Test " + i)
                    .startDateTime(LocalDateTime.now().plusDays(i % 30 + 1))
                    .endDateTime(LocalDateTime.now().plusDays(i % 30 + 1).plusHours(1))
                    .status("CONFIRMED")
                    .isGroup(true)
                    .maxCapacity(20)
                    .participantUserIds(List.of(eliteUser.getIdUser()))
                    .build();
            reservations.add(res);
        }
        reservationRepository.saveAll(reservations);

        long startTime = System.currentTimeMillis();

        // Act
        List<ReservationResponse> upcoming = reservationService.getUpcomingReservations(eliteUser.getIdUser());

        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertFalse(upcoming.isEmpty(), "Debe retornar reservas");
        assertTrue(duration < 1000, "Consulta debe completarse en menos de 1 segundo. Duración: " + duration + "ms");

        System.out.println("✅ Rendimiento de Consulta: " + upcoming.size() + " reservas obtenidas en " + duration + "ms");
    }

    @Test
    @Order(8)
    @DisplayName("8️⃣ QUALITY: Validación de Negocio - Verificar reglas de membresía")
    void testBusinessRulesMembershipValidation() throws Exception {
        // Arrange
        Reservation groupClass = Reservation.builder()
                .user(adminUser)
                .reservationType("GROUP_CLASS")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status("CONFIRMED")
                .isGroup(true)
                .maxCapacity(20)
                .participantUserIds(new ArrayList<>())
                .build();
        groupClass = reservationRepository.save(groupClass);
        final Long classId = groupClass.getId();

        // Test 1: Usuario ELITE puede unirse gratis
        assertDoesNotThrow(() -> reservationService.joinGroupClass(eliteUser.getIdUser(), classId),
                "Usuario ELITE debe poder unirse gratis");

        // Test 2: Usuario PREMIUM debe pagar
        Exception premiumException = assertThrows(IllegalArgumentException.class,
                () -> reservationService.joinGroupClass(premiumUser.getIdUser(), classId),
                "Usuario PREMIUM debe ser redirigido a endpoint con pago");
        assertTrue(premiumException.getMessage().contains("$15,000"),
                "Mensaje debe indicar el monto a pagar");

        System.out.println("✅ Reglas de Negocio: Validación de membresía funciona correctamente");
    }

    @Test
    @Order(9)
    @DisplayName("9️⃣ QUALITY: Manejo de Errores - Mensajes descriptivos y apropiados")
    void testErrorHandlingDescriptiveMessages() {
        // Test 1: Usuario no encontrado
        Exception userNotFound = assertThrows(Exception.class,
                () -> reservationService.getUpcomingReservations(99999L));
        assertNotNull(userNotFound.getMessage());

        // Test 2: Clase no encontrada
        Exception classNotFound = assertThrows(Exception.class,
                () -> reservationService.joinGroupClass(eliteUser.getIdUser(), 99999L));
        assertTrue(classNotFound.getMessage().contains("no encontrada") ||
                   classNotFound.getMessage().contains("not found"),
                "Mensaje debe indicar que la clase no fue encontrada");

        // Test 3: Validación de fecha inválida
        CreateReservationRequest invalidRequest = new CreateReservationRequest();
        invalidRequest.setReservationType("GROUP_CLASS");
        invalidRequest.setStartDateTime(LocalDateTime.now().minusDays(1).toString());
        invalidRequest.setEndDateTime(LocalDateTime.now().toString());

        Exception invalidDate = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(adminUser.getIdUser(), invalidRequest));
        assertTrue(invalidDate.getMessage().contains("pasado") ||
                   invalidDate.getMessage().contains("futuras"),
                "Mensaje debe explicar el problema con la fecha");

        System.out.println("✅ Manejo de Errores: Mensajes descriptivos verificados");
    }

    @Test
    @Order(10)
    @DisplayName("🔟 QUALITY: Escalabilidad - Sistema maneja múltiples clases concurrentes")
    void testScalabilityMultipleClassesConcurrent() throws Exception {
        // Arrange & Act - Crear 50 clases grupales simultáneamente
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<ReservationResponse>> futures = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            final int index = i;
            Future<ReservationResponse> future = executor.submit(() -> {
                CreateReservationRequest request = new CreateReservationRequest();
                request.setReservationType("GROUP_CLASS");
                request.setClassName("Concurrent Class " + index);
                request.setStartDateTime(LocalDateTime.now().plusDays(index + 1).toString());
                request.setEndDateTime(LocalDateTime.now().plusDays(index + 1).plusHours(1).toString());
                request.setMaxCapacity(20);
                return reservationService.createReservation(adminUser.getIdUser(), request);
            });
            futures.add(future);
        }

        // Esperar resultados
        List<ReservationResponse> created = new ArrayList<>();
        for (Future<ReservationResponse> future : futures) {
            created.add(future.get(10, TimeUnit.SECONDS));
        }
        executor.shutdown();

        // Assert
        assertEquals(50, created.size(), "Deben crearse 50 clases");
        List<Reservation> allClasses = reservationRepository.findAll();
        assertTrue(allClasses.size() >= 50, "Debe haber al menos 50 clases en BD");

        System.out.println("✅ Escalabilidad: Sistema maneja " + allClasses.size() + " clases correctamente");
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private UserBase createTestUser(String email, MembershipType membershipType) {
        UserBase user = new UserBase();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("test123"));
        user.setRole(UserRole.USER);
        user.setIsActive(true);

        PersonalInformation info = new PersonalInformation();
        info.setFirstName("Test");
        info.setLastName("User");
        info.setPhoneNumber("3009999999");
        user.setPersonalInformation(info);

        user = userRepository.save(user);

        Membership membership = new Membership();
        membership.setUserId(user.getIdUser());
        membership.setMembershipTypeId(membershipType.getIdMembershipType());
        membership.setStartDate(LocalDate.now());
        membership.setEndDate(LocalDate.now().plusMonths(1));
        membershipRepository.save(membership);

        return user;
    }
}

