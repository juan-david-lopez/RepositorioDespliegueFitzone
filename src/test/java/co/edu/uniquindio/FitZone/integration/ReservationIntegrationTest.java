package co.edu.uniquindio.FitZone.integration;

import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.PersonalInformation;
import co.edu.uniquindio.FitZone.model.entity.Reservation;
import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.ReservationRepository;
import co.edu.uniquindio.FitZone.repository.UserBaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🔗 PRUEBAS DE INTEGRACIÓN - Sistema de Reservas
 * Estas pruebas validan la integración completa entre controladores,
 * servicios, repositorios y base de datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de Integración - Sistema de Reservas Completo")
class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserBaseRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MembershipTypeRepository membershipTypeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String eliteUserToken;
    private String premiumUserToken;
    private Long adminUserId;
    private Long eliteUserId;
    private Long premiumUserId;
    private Long groupClassId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // Limpiar datos de prueba anteriores
        reservationRepository.deleteAll();
        membershipRepository.deleteAll();
        userRepository.deleteAll();
        membershipTypeRepository.deleteAll();

        // Crear tipos de membresía
        createMembershipTypes();

        // Crear usuarios de prueba
        createTestUsers();

        // Obtener tokens JWT (simulados)
        adminToken = "Bearer mock_admin_token";
        eliteUserToken = "Bearer mock_elite_token";
        premiumUserToken = "Bearer mock_premium_token";
    }

    private void createMembershipTypes() {
        MembershipType elite = new MembershipType();
        elite.setName(MembershipTypeName.ELITE);
        elite.setDescription("Membresía Elite - Acceso ilimitado");
        elite.setMonthlyPrice(new java.math.BigDecimal("70000.0"));
        elite.setAccessToAllLocation(true);
        elite.setGroupClassesSessionsIncluded(-1);
        elite.setPersonalTrainingIncluded(10);
        elite.setSpecializedClassesIncluded(true);
        membershipTypeRepository.save(elite);

        MembershipType premium = new MembershipType();
        premium.setName(MembershipTypeName.PREMIUM);
        premium.setDescription("Membresía Premium");
        premium.setMonthlyPrice(new java.math.BigDecimal("50000.0"));
        premium.setAccessToAllLocation(true);
        premium.setGroupClassesSessionsIncluded(8);
        premium.setPersonalTrainingIncluded(4);
        premium.setSpecializedClassesIncluded(true);
        membershipTypeRepository.save(premium);

        MembershipType basic = new MembershipType();
        basic.setName(MembershipTypeName.BASIC);
        basic.setDescription("Membresía Básica");
        basic.setMonthlyPrice(new java.math.BigDecimal("30000.0"));
        basic.setAccessToAllLocation(false);
        basic.setGroupClassesSessionsIncluded(4);
        basic.setPersonalTrainingIncluded(0);
        basic.setSpecializedClassesIncluded(false);
        membershipTypeRepository.save(basic);
    }

    private void createTestUsers() {
        // Usuario ADMIN
        UserBase admin = new UserBase();
        admin.setEmail("admin@fitzone.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        PersonalInformation adminInfo = new PersonalInformation();
        adminInfo.setFirstName("Admin");
        adminInfo.setLastName("FitZone");
        adminInfo.setPhoneNumber("3001234567");
        adminInfo.setDocumentType(DocumentType.CC);
        adminInfo.setDocumentNumber("1000000000");
        admin.setPersonalInformation(adminInfo);

        admin = userRepository.save(admin);
        adminUserId = admin.getIdUser();

        // Membresía ELITE para admin
        MembershipType eliteType = membershipTypeRepository.findByName(MembershipTypeName.ELITE)
                .orElseThrow();
        Membership adminMembership = new Membership();
        adminMembership.setUserId(admin.getIdUser());
        adminMembership.setMembershipTypeId(eliteType.getIdMembershipType());
        adminMembership.setStartDate(LocalDate.now());
        adminMembership.setEndDate(LocalDate.now().plusMonths(1));
        membershipRepository.save(adminMembership);

        // Usuario ELITE
        UserBase eliteUser = new UserBase();
        eliteUser.setEmail("elite@fitzone.com");
        eliteUser.setPassword(passwordEncoder.encode("elite123"));
        eliteUser.setRole(UserRole.MEMBER);
        eliteUser.setActive(true);

        PersonalInformation eliteInfo = new PersonalInformation();
        eliteInfo.setFirstName("Juan");
        eliteInfo.setLastName("Elite");
        eliteInfo.setPhoneNumber("3007654321");
        eliteInfo.setDocumentType(DocumentType.CC);
        eliteInfo.setDocumentNumber("1000000001");
        eliteUser.setPersonalInformation(eliteInfo);

        eliteUser = userRepository.save(eliteUser);
        eliteUserId = eliteUser.getIdUser();

        Membership eliteMembership = new Membership();
        eliteMembership.setUserId(eliteUser.getIdUser());
        eliteMembership.setMembershipTypeId(eliteType.getIdMembershipType());
        eliteMembership.setStartDate(LocalDate.now());
        eliteMembership.setEndDate(LocalDate.now().plusMonths(1));
        membershipRepository.save(eliteMembership);

        // Usuario PREMIUM
        UserBase premiumUser = new UserBase();
        premiumUser.setEmail("premium@fitzone.com");
        premiumUser.setPassword(passwordEncoder.encode("premium123"));
        premiumUser.setRole(UserRole.MEMBER);
        premiumUser.setActive(true);

        PersonalInformation premiumInfo = new PersonalInformation();
        premiumInfo.setFirstName("Maria");
        premiumInfo.setLastName("Premium");
        premiumInfo.setPhoneNumber("3009876543");
        premiumInfo.setDocumentType(DocumentType.CC);
        premiumInfo.setDocumentNumber("1000000002");
        premiumUser.setPersonalInformation(premiumInfo);

        premiumUser = userRepository.save(premiumUser);
        premiumUserId = premiumUser.getIdUser();

        MembershipType premiumType = membershipTypeRepository.findByName(MembershipTypeName.PREMIUM)
                .orElseThrow();
        Membership premiumMembership = new Membership();
        premiumMembership.setUserId(premiumUser.getIdUser());
        premiumMembership.setMembershipTypeId(premiumType.getIdMembershipType());
        premiumMembership.setStartDate(LocalDate.now());
        premiumMembership.setEndDate(LocalDate.now().plusMonths(1));
        membershipRepository.save(premiumMembership);
    }

    // ==================== PRUEBAS DE INTEGRACIÓN ====================

    @Test
    @Order(1)
    @DisplayName("1️⃣ INTEGRATION: Admin puede crear clase grupal exitosamente")
    void testAdminCanCreateGroupClass() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("reservationType", "GROUP_CLASS");
        request.put("className", "Yoga Matutino");
        request.put("startDateTime", LocalDateTime.now().plusDays(2).toString());
        request.put("endDateTime", LocalDateTime.now().plusDays(2).plusHours(1).toString());
        request.put("maxCapacity", 20);
        request.put("locationId", 1);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.className").value("Yoga Matutino"))
                .andExpect(jsonPath("$.data.isGroup").value(true))
                .andExpect(jsonPath("$.data.maxCapacity").value(20))
                .andReturn();

        // Guardar ID para pruebas posteriores
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        groupClassId = ((Number) ((Map<String, Object>) response.get("data")).get("id")).longValue();
    }

    @Test
    @Order(2)
    @DisplayName("2️⃣ INTEGRATION: Usuario normal NO puede crear clase grupal")
    void testUserCannotCreateGroupClass() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("reservationType", "GROUP_CLASS");
        request.put("className", "CrossFit");
        request.put("startDateTime", LocalDateTime.now().plusDays(1).toString());
        request.put("endDateTime", LocalDateTime.now().plusDays(1).plusHours(1).toString());

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", eliteUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Solo los administradores")));
    }

    @Test
    @Order(3)
    @DisplayName("3️⃣ INTEGRATION: Obtener lista de clases grupales disponibles")
    void testGetAvailableGroupClasses() throws Exception {
        // Primero crear una clase grupal
        createGroupClassForTesting();

        // Act & Assert
        mockMvc.perform(get("/api/reservations/group-classes")
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.count").exists());
    }

    @Test
    @Order(4)
    @DisplayName("4️⃣ INTEGRATION: Usuario ELITE puede unirse gratis a clase grupal")
    void testEliteUserJoinsGroupClassFree() throws Exception {
        // Arrange
        Long classId = createGroupClassForTesting();

        // Act & Assert
        mockMvc.perform(post("/api/reservations/group-classes/{id}/join", classId)
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("exitosamente")))
                .andExpect(jsonPath("$.data.currentParticipants").value(greaterThan(0)));
    }

    @Test
    @Order(5)
    @DisplayName("5️⃣ INTEGRATION: Usuario PREMIUM necesita pagar para unirse")
    void testPremiumUserNeedsPayment() throws Exception {
        // Arrange
        Long classId = createGroupClassForTesting();

        // Act & Assert
        mockMvc.perform(post("/api/reservations/group-classes/{id}/join", classId)
                        .header("Authorization", premiumUserToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("deben pagar")))
                .andExpect(jsonPath("$.message").value(containsString("$15,000")));
    }

    @Test
    @Order(6)
    @DisplayName("6️⃣ INTEGRATION: Usuario puede ver sus propias reservas")
    void testGetUserUpcomingReservations() throws Exception {
        // Arrange - Usuario ELITE se une a una clase
        Long classId = createGroupClassForTesting();
        joinGroupClass(eliteUserId, classId);

        // Act & Assert
        mockMvc.perform(get("/api/reservations/upcoming")
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(7)
    @DisplayName("7️⃣ INTEGRATION: No se puede unir a clase que ya comenzó")
    void testCannotJoinPastClass() throws Exception {
        // Arrange - Crear clase en el pasado
        Reservation pastClass = createPastGroupClass();

        // Act & Assert
        mockMvc.perform(post("/api/reservations/group-classes/{id}/join", pastClass.getId())
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("ya comenzó")));
    }

    @Test
    @Order(8)
    @DisplayName("8️⃣ INTEGRATION: No se puede unir dos veces a la misma clase")
    void testCannotJoinTwice() throws Exception {
        // Arrange
        Long classId = createGroupClassForTesting();
        joinGroupClass(eliteUserId, classId);

        // Act & Assert - Intentar unirse de nuevo
        mockMvc.perform(post("/api/reservations/group-classes/{id}/join", classId)
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Ya estás inscrito")));
    }

    @Test
    @Order(9)
    @DisplayName("9️⃣ INTEGRATION: Usuario puede crear entrenamiento personal")
    void testUserCanCreatePersonalTraining() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("reservationType", "PERSONAL_TRAINING");
        request.put("startDateTime", LocalDateTime.now().plusDays(1).toString());
        request.put("endDateTime", LocalDateTime.now().plusDays(1).plusHours(1).toString());
        request.put("instructorId", adminUserId);
        request.put("locationId", 1);

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", eliteUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("PERSONAL_TRAINING"))
                .andExpect(jsonPath("$.data.isGroup").value(false));
    }

    @Test
    @Order(10)
    @DisplayName("🔟 INTEGRATION: Flujo completo - Crear clase, listar y unirse")
    void testCompleteFlowCreateListAndJoin() throws Exception {
        // 1. Admin crea clase grupal
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("reservationType", "GROUP_CLASS");
        createRequest.put("className", "Spinning Intenso");
        createRequest.put("startDateTime", LocalDateTime.now().plusDays(3).toString());
        createRequest.put("endDateTime", LocalDateTime.now().plusDays(3).plusHours(1).toString());
        createRequest.put("maxCapacity", 15);
        createRequest.put("locationId", 1);

        MvcResult createResult = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createBody = createResult.getResponse().getContentAsString();
        Map<String, Object> createResponse = objectMapper.readValue(createBody, Map.class);
        Long createdClassId = ((Number) ((Map<String, Object>) createResponse.get("data")).get("id")).longValue();

        // 2. Listar clases disponibles
        mockMvc.perform(get("/api/reservations/group-classes")
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + createdClassId + ")]").exists());

        // 3. Usuario ELITE se une
        mockMvc.perform(post("/api/reservations/group-classes/{id}/join", createdClassId)
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. Verificar en mis reservas
        mockMvc.perform(get("/api/reservations/upcoming")
                        .header("Authorization", eliteUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + createdClassId + ")]").exists());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Long createGroupClassForTesting() {
        UserBase admin = userRepository.findById(adminUserId).orElseThrow();

        Reservation groupClass = Reservation.builder()
                .user(admin)
                .reservationType("GROUP_CLASS")
                .className("Test Class")
                .startDateTime(LocalDateTime.now().plusDays(2))
                .endDateTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .status("CONFIRMED")
                .isGroup(true)
                .maxCapacity(20)
                .participantUserIds(new ArrayList<>(List.of(adminUserId)))
                .locationId(1L)
                .build();

        return reservationRepository.save(groupClass).getId();
    }

    private Reservation createPastGroupClass() {
        UserBase admin = userRepository.findById(adminUserId).orElseThrow();

        Reservation pastClass = Reservation.builder()
                .user(admin)
                .reservationType("GROUP_CLASS")
                .className("Past Class")
                .startDateTime(LocalDateTime.now().minusHours(1))
                .endDateTime(LocalDateTime.now())
                .status("CONFIRMED")
                .isGroup(true)
                .maxCapacity(20)
                .participantUserIds(new ArrayList<>())
                .build();

        return reservationRepository.save(pastClass);
    }

    private void joinGroupClass(Long userId, Long classId) {
        Reservation groupClass = reservationRepository.findById(classId).orElseThrow();
        List<Long> participants = groupClass.getParticipantUserIds();
        if (participants == null) {
            participants = new ArrayList<>();
        }
        if (!participants.contains(userId)) {
            participants.add(userId);
            groupClass.setParticipantUserIds(participants);
            reservationRepository.save(groupClass);
        }
    }
}
