package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.CreateReservationRequest;
import co.edu.uniquindio.FitZone.dto.response.ReservationResponse;
import co.edu.uniquindio.FitZone.integration.payment.StripeService;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.Reservation;
import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.ReservationRepository;
import co.edu.uniquindio.FitZone.repository.UserBaseRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements co.edu.uniquindio.FitZone.service.interfaces.IReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Precio por defecto para clases grupales (si no es ELITE)
    private static final BigDecimal GROUP_CLASS_PRICE = new BigDecimal("15000.00"); // $15,000 COP

    private final ReservationRepository reservationRepository;
    private final UserBaseRepository userRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final StripeService stripeService;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                 UserBaseRepository userRepository,
                                 MembershipTypeRepository membershipTypeRepository,
                                 StripeService stripeService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.membershipTypeRepository = membershipTypeRepository;
        this.stripeService = stripeService;
    }

    @Override
    public List<ReservationResponse> listReservationsByUserId(Long userId) throws Exception {
        // ✅ FIX: Usar nueva consulta que incluye clases grupales donde el usuario es participante
        List<Reservation> list = reservationRepository.findAllByUserIdOrParticipant(userId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getUpcomingReservations(Long userId) throws Exception {
        log.info("📅 Obteniendo próximas reservas para usuario: {}", userId);

        // ✅ FIX: Usar nueva consulta que incluye clases grupales donde el usuario es participante
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> upcoming = reservationRepository.findUpcomingByUserIdOrParticipant(userId, now);

        List<ReservationResponse> responses = upcoming.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        log.info("✅ Encontradas {} reservas próximas (incluyendo clases grupales como participante)", responses.size());
        return responses;
    }

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, CreateReservationRequest request) throws Exception {
        log.info("🎯 Creando reserva para usuario: {}, tipo: {}", userId, request.getReservationType());

        UserBase user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Usuario no encontrado: " + userId));

        String reservationType = request.getReservationType().toUpperCase();

        // ========== VALIDACIÓN DE PERMISOS POR TIPO DE RESERVA ==========
        if ("GROUP_CLASS".equals(reservationType) || "CLASS".equals(reservationType)) {
            // ✅ Solo ADMIN e INSTRUCTOR pueden CREAR clases grupales
            if (user.getRole() != co.edu.uniquindio.FitZone.model.enums.UserRole.ADMIN &&
                user.getRole() != co.edu.uniquindio.FitZone.model.enums.UserRole.INSTRUCTOR) {
                throw new IllegalArgumentException("❌ Solo los administradores e instructores pueden crear clases grupales. " +
                        "Los miembros deben unirse a clases grupales existentes usando el endpoint /api/reservations/group-classes/{id}/join");
            }
            log.info("✅ Usuario {} ({}) autorizado para crear clase grupal", user.getEmail(), user.getRole());
        } else {
            // ✅ Los miembros SÍ pueden crear entrenamientos personales y reservar espacios especializados
            log.info("✅ Usuario {} creando reserva privada de tipo {}", user.getEmail(), reservationType);
        }

        LocalDateTime start = LocalDateTime.parse(request.getStartDateTime());
        LocalDateTime end = LocalDateTime.parse(request.getEndDateTime());
        LocalDateTime now = LocalDateTime.now();

        // ========== VALIDACIÓN 1: Solo fechas futuras ==========
        if (start.isBefore(now) || start.isEqual(now)) {
            throw new IllegalArgumentException("❌ No puedes reservar clases en el pasado o en el momento actual. " +
                    "Solo se permiten reservas para fechas futuras.");
        }

        // ========== VALIDACIÓN 2: End debe ser después de Start ==========
        if (end.isBefore(start) || end.equals(start)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // ========== VALIDACIÓN 3: Verificar disponibilidad ==========
        Long targetId = request.getTargetId();
        if (targetId != null) {
            boolean available = checkAvailability(targetId, start, end);
            if (!available) {
                throw new IllegalStateException("❌ El espacio/clase no está disponible en el intervalo solicitado");
            }
        }

        // ========== DETERMINAR SI REQUIERE PAGO ==========
        boolean requiresPayment = false;
        BigDecimal paymentAmount = BigDecimal.ZERO;
        String paymentIntentId = null;

        if ("GROUP_CLASS".equals(reservationType) || "CLASS".equals(reservationType)) {
            // Verificar membresía del usuario
            Membership membership = user.getMembership();

            if (membership == null || membership.getMembershipTypeId() == null) {
                throw new IllegalStateException("❌ Debes tener una membresía activa para reservar clases");
            }

            // ✅ FIX: Cargar el MembershipType desde el repositorio
            MembershipType membershipType = membershipTypeRepository.findById(membership.getMembershipTypeId())
                    .orElseThrow(() -> new IllegalStateException("❌ Tipo de membresía no encontrado"));

            MembershipTypeName membershipTypeName = membershipType.getName();

            // Solo ELITE puede reservar gratis clases grupales
            if (membershipTypeName != MembershipTypeName.ELITE) {
                log.info("💳 Usuario con membresía {} debe pagar por clase grupal", membershipTypeName);
                requiresPayment = true;
                paymentAmount = GROUP_CLASS_PRICE;

                // Validar que se proporcionó paymentMethodId
                if (request.getPaymentMethodId() == null || request.getPaymentMethodId().isEmpty()) {
                    throw new IllegalArgumentException("❌ Se requiere un método de pago para usuarios con membresía " +
                            membershipTypeName + ". Solo usuarios ELITE pueden reservar sin pago.");
                }

                // Procesar pago con Stripe
                try {
                    log.info("💳 Procesando pago de ${} COP para reserva de clase grupal", paymentAmount);

                    // ✅ FIX: Convertir BigDecimal a Long (centavos) y usar solo 4 parámetros
                    long amountInCents = paymentAmount.multiply(new BigDecimal("100")).longValue();

                    PaymentIntent paymentIntent = stripeService.createAndConfirmPaymentIntent(
                        amountInCents,  // Long en centavos
                        "cop",
                        request.getPaymentMethodId(),
                        "Reserva de clase grupal - " + (request.getClassName() != null ? request.getClassName() : "FitZone")
                    );

                    if (!"succeeded".equals(paymentIntent.getStatus())) {
                        throw new IllegalStateException("❌ El pago no fue exitoso. Estado: " + paymentIntent.getStatus());
                    }

                    paymentIntentId = paymentIntent.getId();
                    log.info("✅ Pago procesado exitosamente. PaymentIntent: {}", paymentIntentId);

                } catch (StripeException e) {
                    log.error("❌ Error procesando pago con Stripe: {}", e.getMessage());
                    throw new Exception("Error al procesar el pago: " + e.getMessage());
                }
            } else {
                log.info("✅ Usuario ELITE - reserva gratuita para clase grupal");
            }
        }

        // ========== VALIDACIONES ESPECÍFICAS POR TIPO ==========
        boolean isGroup = false;
        Integer maxCapacity = null;
        Long instructorId = null;
        List<Long> participantIds = new ArrayList<>();

        if ("GROUP_CLASS".equals(reservationType) || "CLASS".equals(reservationType)) {
            // Clase grupal - múltiples participantes
            isGroup = true;
            maxCapacity = request.getMaxCapacity() != null ? request.getMaxCapacity() : 20; // Default 20

            // Añadir al usuario creador como primer participante
            participantIds.add(userId);

            // Añadir participantes adicionales si se especificaron
            if (request.getAdditionalParticipantIds() != null) {
                participantIds.addAll(request.getAdditionalParticipantIds());
            }

            log.info("👥 Clase grupal - Participantes: {}, Cupo máximo: {}", participantIds.size(), maxCapacity);

        } else if ("PERSONAL_TRAINING".equals(reservationType) || "TRAINING".equals(reservationType)) {
            // Entrenamiento personal - 1 instructor + 1 usuario
            isGroup = false;

            if (request.getInstructorId() == null) {
                throw new IllegalArgumentException("❌ Se requiere asignar un instructor para entrenamientos personales");
            }

            instructorId = request.getInstructorId();
            participantIds.add(userId); // Solo el usuario que reserva

            log.info("🏋️ Entrenamiento personal - Usuario: {}, Instructor: {}", userId, instructorId);

        } else if ("SPECIALIZED_SPACE".equals(reservationType) || "SPACE".equals(reservationType)) {
            // Espacio especializado
            isGroup = false;
            participantIds.add(userId);

            log.info("🏢 Reserva de espacio especializado - Usuario: {}", userId);
        }

        // ========== CREAR LA RESERVA ==========
        Reservation reservation = Reservation.builder()
                .user(user)
                .reservationType(reservationType)
                .targetId(targetId)
                .startDateTime(start)
                .endDateTime(end)
                .status(requiresPayment && paymentIntentId != null ? "CONFIRMED" : "PENDING")
                .paymentIntentId(paymentIntentId)
                .requiresPayment(requiresPayment)
                .paymentAmount(paymentAmount)
                .isGroup(isGroup)
                .maxCapacity(maxCapacity)
                .participantUserIds(participantIds)
                .instructorId(instructorId)
                .className(request.getClassName())
                .locationId(request.getLocationId())
                .build();

        reservation = reservationRepository.save(reservation);

        log.info("✅ Reserva creada exitosamente: id={}, tipo={}, requierePago={}, monto={}",
                reservation.getId(), reservationType, requiresPayment, paymentAmount);

        return toDto(reservation);
    }

    @Override
    public List<ReservationResponse> getAvailableGroupClasses() throws Exception {
        log.info("📋 Obteniendo todas las clases grupales disponibles");

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> groupClasses = reservationRepository.findUpcomingGroupClasses(now);

        // Filtrar solo las que tienen cupo disponible
        List<ReservationResponse> available = groupClasses.stream()
                .filter(r -> r.hasAvailableCapacity())
                .map(this::toDto)
                .collect(Collectors.toList());

        log.info("✅ Encontradas {} clases grupales con cupo disponible", available.size());
        return available;
    }

    @Override
    @Transactional
    public ReservationResponse joinGroupClass(Long userId, Long groupClassId) throws Exception {
        log.info("👥 Usuario {} intentando unirse a clase grupal {}", userId, groupClassId);

        // Validar que el usuario existe y tiene membresía activa
        UserBase user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Usuario no encontrado: " + userId));

        Membership membership = user.getMembership();
        if (membership == null || membership.getMembershipTypeId() == null) {
            throw new IllegalStateException("❌ Debes tener una membresía activa para unirte a clases grupales");
        }

        // Buscar la clase grupal
        Reservation groupClass = reservationRepository.findById(groupClassId)
                .orElseThrow(() -> new Exception("Clase grupal no encontrada: " + groupClassId));

        // Validar que es una clase grupal
        if (!groupClass.getIsGroup()) {
            throw new IllegalArgumentException("❌ Esta reserva no es una clase grupal");
        }

        // Validar que la clase está confirmada
        if (!"CONFIRMED".equals(groupClass.getStatus())) {
            throw new IllegalStateException("❌ Esta clase no está disponible (estado: " + groupClass.getStatus() + ")");
        }

        // Validar que la clase es futura
        if (groupClass.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("❌ No puedes unirte a una clase que ya comenzó");
        }

        // Validar que hay cupo disponible
        if (!groupClass.hasAvailableCapacity()) {
            throw new IllegalStateException("❌ Esta clase ya está llena (cupo: " +
                    groupClass.getMaxCapacity() + "/" + groupClass.getMaxCapacity() + ")");
        }

        // Validar que el usuario no esté ya inscrito
        if (groupClass.getParticipantUserIds() != null && groupClass.getParticipantUserIds().contains(userId)) {
            throw new IllegalArgumentException("❌ Ya estás inscrito en esta clase");
        }

        // ========== ✅ NUEVO: SISTEMA DE PAGOS PARA PREMIUM/BASIC ==========
        String paymentIntentId = null;
        BigDecimal paymentAmount = BigDecimal.ZERO;

        MembershipType membershipType = membershipTypeRepository.findById(membership.getMembershipTypeId())
                .orElseThrow(() -> new IllegalStateException("❌ Tipo de membresía no encontrado"));

        MembershipTypeName membershipTypeName = membershipType.getName();

        // Solo ELITE puede unirse gratis, PREMIUM y BASIC deben pagar
        if (membershipTypeName != MembershipTypeName.ELITE) {
            log.info("💳 Usuario con membresía {} debe pagar $15,000 COP por unirse a clase grupal", membershipTypeName);

            // Por ahora lanzamos error indicando que deben usar el endpoint con pago
            // En el futuro se puede crear un endpoint separado que acepte paymentMethodId
            throw new IllegalArgumentException("❌ Los miembros " + membershipTypeName +
                    " deben pagar $15,000 COP por clase grupal. " +
                    "Por favor, usa el endpoint POST /api/reservations/group-classes/" + groupClassId +
                    "/join-with-payment con tu método de pago, o actualiza tu membresía a ELITE para acceso ilimitado.");
        }

        log.info("✅ Usuario ELITE - unión gratuita a clase grupal");

        // Agregar usuario a la lista de participantes
        List<Long> participants = groupClass.getParticipantUserIds();
        if (participants == null) {
            participants = new ArrayList<>();
        }
        participants.add(userId);
        groupClass.setParticipantUserIds(participants);

        // Guardar cambios
        Reservation updated = reservationRepository.save(groupClass);

        log.info("✅ Usuario {} agregado exitosamente a clase grupal {}. Participantes: {}/{}",
                userId, groupClassId, participants.size(), groupClass.getMaxCapacity());

        // ========== ✅ NUEVO: ENVIAR NOTIFICACIÓN ==========
        sendNotificationToParticipants(updated, user);

        return toDto(updated);
    }

    /**
     * ✅ NUEVO: Envía notificaciones cuando alguien se une a una clase
     */
    private void sendNotificationToParticipants(Reservation groupClass, UserBase newUser) {
        try {
            log.info("📧 Enviando notificaciones para clase grupal {} - Nuevo participante: {}",
                    groupClass.getId(), newUser.getEmail());

            // Obtener el creador de la clase (instructor/admin)
            UserBase creator = groupClass.getUser();
            if (creator != null && creator.getIdUser() != null) {
                String creatorName = "Instructor";
                if (creator.getPersonalInformation() != null) {
                    creatorName = creator.getPersonalInformation().getFirstName() + " " +
                                 (creator.getPersonalInformation().getLastName() != null ?
                                  creator.getPersonalInformation().getLastName() : "");
                }

                log.info("📧 Notificación para creador: {} - Nuevo participante: {}",
                        creator.getEmail(), newUser.getEmail());

                // TODO: Integrar con servicio de email/notificaciones
                // emailService.sendGroupClassJoinNotification(creator, groupClass, newUser);
            }

            // Obtener otros participantes
            if (groupClass.getParticipantUserIds() != null && groupClass.getParticipantUserIds().size() > 1) {
                List<Long> otherParticipants = groupClass.getParticipantUserIds().stream()
                        .filter(id -> !id.equals(newUser.getIdUser()))
                        .collect(Collectors.toList());

                log.info("📧 Notificando a {} participantes existentes sobre el nuevo miembro",
                        otherParticipants.size());

                // TODO: Integrar con servicio de email/notificaciones
                // for (Long participantId : otherParticipants) {
                //     emailService.sendGroupClassUpdateNotification(participantId, groupClass, newUser);
                // }
            }

            // Enviar confirmación al usuario que se unió
            String userName = "Usuario";
            if (newUser.getPersonalInformation() != null) {
                userName = newUser.getPersonalInformation().getFirstName() + " " +
                          (newUser.getPersonalInformation().getLastName() != null ?
                           newUser.getPersonalInformation().getLastName() : "");
            }

            log.info("✅ Confirmación enviada a {}: Te has unido a la clase '{}'",
                    newUser.getEmail(), groupClass.getClassName());

            // TODO: Integrar con servicio de email/notificaciones
            // emailService.sendJoinConfirmation(newUser, groupClass);

        } catch (Exception e) {
            // No lanzar excepción si falla el envío de notificaciones
            // Solo registrar el error
            log.error("⚠️ Error al enviar notificaciones: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReservationResponse joinGroupClassWithPayment(Long userId, Long groupClassId, String paymentMethodId) throws Exception {
        log.info("💳 Usuario {} intentando unirse a clase grupal {} con pago", userId, groupClassId);

        // Validar que el usuario existe y tiene membresía activa
        UserBase user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Usuario no encontrado: " + userId));

        Membership membership = user.getMembership();
        if (membership == null || membership.getMembershipTypeId() == null) {
            throw new IllegalStateException("❌ Debes tener una membresía activa para unirte a clases grupales");
        }

        // Buscar la clase grupal
        Reservation groupClass = reservationRepository.findById(groupClassId)
                .orElseThrow(() -> new Exception("Clase grupal no encontrada: " + groupClassId));

        // Validar que es una clase grupal
        if (!groupClass.getIsGroup()) {
            throw new IllegalArgumentException("❌ Esta reserva no es una clase grupal");
        }

        // Validar que la clase está confirmada
        if (!"CONFIRMED".equals(groupClass.getStatus())) {
            throw new IllegalStateException("❌ Esta clase no está disponible (estado: " + groupClass.getStatus() + ")");
        }

        // Validar que la clase es futura
        if (groupClass.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("❌ No puedes unirte a una clase que ya comenzó");
        }

        // Validar que hay cupo disponible
        if (!groupClass.hasAvailableCapacity()) {
            throw new IllegalStateException("❌ Esta clase ya está llena (cupo: " +
                    groupClass.getMaxCapacity() + "/" + groupClass.getMaxCapacity() + ")");
        }

        // Validar que el usuario no esté ya inscrito
        if (groupClass.getParticipantUserIds() != null && groupClass.getParticipantUserIds().contains(userId)) {
            throw new IllegalArgumentException("❌ Ya estás inscrito en esta clase");
        }

        // Validar paymentMethodId
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Se requiere un método de pago para unirse a esta clase");
        }

        // Verificar tipo de membresía
        MembershipType membershipType = membershipTypeRepository.findById(membership.getMembershipTypeId())
                .orElseThrow(() -> new IllegalStateException("❌ Tipo de membresía no encontrado"));

        MembershipTypeName membershipTypeName = membershipType.getName();

        // Si es ELITE, redirigir al método gratuito
        if (membershipTypeName == MembershipTypeName.ELITE) {
            log.info("ℹ️ Usuario ELITE intentando usar endpoint con pago - redirigiendo a método gratuito");
            return joinGroupClass(userId, groupClassId);
        }

        // ========== PROCESAR PAGO ==========
        log.info("💳 Procesando pago de $15,000 COP para miembro {} unirse a clase grupal", membershipTypeName);

        String paymentIntentId;
        try {
            long amountInCents = GROUP_CLASS_PRICE.multiply(new BigDecimal("100")).longValue();

            PaymentIntent paymentIntent = stripeService.createAndConfirmPaymentIntent(
                amountInCents,
                "cop",
                paymentMethodId,
                "Unirse a clase grupal - " + (groupClass.getClassName() != null ? groupClass.getClassName() : "FitZone")
            );

            if (!"succeeded".equals(paymentIntent.getStatus())) {
                throw new IllegalStateException("❌ El pago no fue exitoso. Estado: " + paymentIntent.getStatus());
            }

            paymentIntentId = paymentIntent.getId();
            log.info("✅ Pago procesado exitosamente. PaymentIntent: {}", paymentIntentId);

        } catch (StripeException e) {
            log.error("❌ Error procesando pago con Stripe: {}", e.getMessage());
            throw new Exception("Error al procesar el pago: " + e.getMessage());
        }

        // Agregar usuario a la lista de participantes
        List<Long> participants = groupClass.getParticipantUserIds();
        if (participants == null) {
            participants = new ArrayList<>();
        }
        participants.add(userId);
        groupClass.setParticipantUserIds(participants);

        // Guardar cambios
        Reservation updated = reservationRepository.save(groupClass);

        log.info("✅ Usuario {} agregado exitosamente a clase grupal {} con pago. Participantes: {}/{}",
                userId, groupClassId, participants.size(), groupClass.getMaxCapacity());

        // Enviar notificaciones
        sendNotificationToParticipants(updated, user);

        return toDto(updated);
    }

    @Override
    public boolean checkAvailability(Long targetId, LocalDateTime start, LocalDateTime end) throws Exception {
        List<Reservation> overlapping = reservationRepository.findByTargetIdAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqual(
                targetId, end, start);
        return overlapping == null || overlapping.isEmpty();
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long userId, Long reservationId) throws Exception {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) throw new Exception("Reserva no encontrada: " + reservationId);
        Reservation reservation = opt.get();
        if (!reservation.getUser().getIdUser().equals(userId)) {
            throw new Exception("No autorizado para cancelar esta reserva");
        }
        reservation.setStatus("CANCELLED");
        Reservation saved = reservationRepository.save(reservation);
        return toDto(saved);
    }

    /**
     * Convierte Reservation entity a DTO alineado con frontend
     */
    private ReservationResponse toDto(Reservation r) {
        // Extraer fecha y horas por separado
        String scheduledDate = r.getStartDateTime().format(DATE_FORMATTER);
        String scheduledStartTime = r.getStartDateTime().format(TIME_FORMATTER);
        String scheduledEndTime = r.getEndDateTime().format(TIME_FORMATTER);

        // Mapear tipo de reserva al formato frontend
        String frontendType = mapReservationTypeToFrontend(r.getReservationType());

        ReservationResponse.ReservationResponseBuilder builder = ReservationResponse.builder()
                .id(r.getId())
                .userId(r.getUser() != null ? r.getUser().getIdUser() : null)

                // Campos frontend (formato esperado)
                .type(frontendType)
                .scheduledDate(scheduledDate)
                .scheduledStartTime(scheduledStartTime)
                .scheduledEndTime(scheduledEndTime)

                // Campos legacy (compatibilidad)
                .reservationType(r.getReservationType())
                .targetId(r.getTargetId())
                .startDateTime(r.getStartDateTime())
                .endDateTime(r.getEndDateTime())

                .status(r.getStatus())
                .paymentIntentId(r.getPaymentIntentId())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())

                // Nuevos campos
                .requiresPayment(r.getRequiresPayment())
                .paymentAmount(r.getPaymentAmount())
                .isGroup(r.getIsGroup())
                .maxCapacity(r.getMaxCapacity())
                .currentParticipants(r.getParticipantUserIds() != null ? r.getParticipantUserIds().size() : 0)
                .participantUserIds(r.getParticipantUserIds())
                .instructorId(r.getInstructorId())
                .className(r.getClassName())
                .locationId(r.getLocationId())
                .hasAvailableCapacity(r.hasAvailableCapacity());

        return builder.build();
    }

    /**
     * Mapea el tipo de reserva de BD al formato frontend
     */
    private String mapReservationTypeToFrontend(String dbType) {
        if (dbType == null) return "GROUP_CLASS";

        switch (dbType.toUpperCase()) {
            case "CLASS":
            case "GROUP_CLASS":
                return "GROUP_CLASS";
            case "PERSONAL_TRAINING":
            case "TRAINING":
                return "PERSONAL_TRAINING";
            case "SPACE":
            case "SPECIALIZED_SPACE":
                return "SPECIALIZED_SPACE";
            default:
                return "GROUP_CLASS";
        }
    }
}
