package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.CreateReservationRequest;
import co.edu.uniquindio.FitZone.dto.response.ReservationResponse;
import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.repository.UserBaseRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IReservationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar las reservaciones de los usuarios.
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final IReservationService reservationService;
    private final UserBaseRepository userRepository;

    public ReservationController(IReservationService reservationService, UserBaseRepository userRepository) {
        this.reservationService = reservationService;
        this.userRepository = userRepository;
    }

    /**
     * Obtiene todas las reservaciones del usuario autenticado
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            logger.info("GET /api/reservations/my - Usuario: {}, Status: {}, StartDate: {}, EndDate: {}",
                    email, status, startDate, endDate);

            UserBase user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            List<ReservationResponse> reservations = reservationService.listReservationsByUserId(user.getIdUser());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reservations);
            response.put("message", "Reservaciones del usuario");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al obtener reservaciones del usuario autenticado - Error: {}",
                    e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener reservaciones");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene las próximas reservaciones del usuario autenticado (alineado con frontend)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingReservations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            logger.info("📅 GET /api/reservations/upcoming - Usuario: {}", email);

            UserBase user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            List<ReservationResponse> upcomingReservations = reservationService.getUpcomingReservations(user.getIdUser());

            logger.info("✅ Próximas reservas encontradas: {}", upcomingReservations.size());

            // Formato esperado por frontend
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", upcomingReservations);
            response.put("count", upcomingReservations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error al obtener próximas reservaciones - Error: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener próximas reservaciones");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", List.of()); // Retornar array vacío en caso de error

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            UserBase user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            logger.info("🎯 POST /api/reservations - Usuario: {}, Tipo: {}, PaymentMethod: {}",
                    user.getIdUser(), request.getReservationType(),
                    request.getPaymentMethodId() != null ? "Proporcionado" : "No proporcionado");

            ReservationResponse created = reservationService.createReservation(user.getIdUser(), request);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", created);
            resp.put("message", created.getRequiresPayment() ?
                    "Reserva creada y pago procesado exitosamente" :
                    "Reserva creada exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (IllegalArgumentException e) {
            // Errores de validación (fechas, membresía, etc.)
            logger.warn("⚠️ Error de validación al crear reserva: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Validación fallida");
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);

        } catch (IllegalStateException e) {
            // Errores de estado (pago fallido, sin cupo, etc.)
            logger.error("❌ Error de estado al crear reserva: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Error de estado");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);

        } catch (DateTimeParseException e) {
            logger.error("❌ Fecha en formato inválido: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Formato de fecha inválido. Use ISO-8601 (ej: 2025-10-20T14:00:00)");
            return ResponseEntity.badRequest().body(resp);

        } catch (Exception e) {
            logger.error("❌ Error al crear reserva: {}", e.getMessage(), e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Error interno del servidor");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable("id") Long reservationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            UserBase user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            ReservationResponse cancelled = reservationService.cancelReservation(user.getIdUser(), reservationId);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", cancelled);
            resp.put("message", "Reserva cancelada");

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            logger.error("Error al cancelar reserva: {}", e.getMessage(), e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        try {
            logger.info("📅 GET /api/reservations/availability - type: {}, date: {}, targetId: {}",
                    type, date, targetId);

            // Si el frontend envía type y date (nuevo formato)
            if (type != null && date != null) {
                // Por ahora retornamos slots disponibles genéricos
                // El frontend puede implementar lógica más compleja después
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("available", true);
                resp.put("slots", generateDefaultSlots(date)); // Slots por defecto
                resp.put("message", "Horarios disponibles para " + type);
                return ResponseEntity.ok(resp);
            }

            // Si el frontend envía targetId, start, end (formato anterior)
            if (targetId != null && start != null && end != null) {
                LocalDateTime startDt = LocalDateTime.parse(start);
                LocalDateTime endDt = LocalDateTime.parse(end);

                boolean available = reservationService.checkAvailability(targetId, startDt, endDt);

                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("available", available);
                return ResponseEntity.ok(resp);
            }

            // Si no se proporcionan parámetros válidos
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Parámetros inválidos. Se requiere (type y date) o (targetId, start, end)");
            return ResponseEntity.badRequest().body(resp);

        } catch (DateTimeParseException e) {
            logger.error("❌ Formato de fecha inválido: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Formato de fecha inválido (ISO-8601 esperado)");
            return ResponseEntity.badRequest().body(resp);
        } catch (Exception e) {
            logger.error("❌ Error al verificar disponibilidad: {}", e.getMessage(), e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /**
     * Genera slots de horario por defecto para una fecha dada
     */
    private List<Map<String, Object>> generateDefaultSlots(String date) {
        List<Map<String, Object>> slots = new ArrayList<>();

        // Horarios típicos de gimnasio: 6:00 AM - 10:00 PM
        String[] hours = {
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00", "21:00", "22:00"
        };

        for (String hour : hours) {
            Map<String, Object> slot = new HashMap<>();
            slot.put("time", hour);
            slot.put("available", true); // Por ahora todos disponibles
            slot.put("capacity", 20);
            slot.put("enrolled", 0);
            slots.add(slot);
        }

        return slots;
    }

    /**
     * ✅ NUEVO: Obtener todas las clases grupales disponibles
     * Endpoint público para que todos los miembros vean las clases grupales
     */
    @GetMapping("/group-classes")
    public ResponseEntity<?> getAvailableGroupClasses() {
        try {
            logger.info("📋 GET /api/reservations/group-classes - Obteniendo clases grupales disponibles");

            List<ReservationResponse> groupClasses = reservationService.getAvailableGroupClasses();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", groupClasses);
            response.put("count", groupClasses.size());
            response.put("message", "Clases grupales disponibles");

            logger.info("✅ Encontradas {} clases grupales disponibles", groupClasses.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error al obtener clases grupales: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener clases grupales");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", List.of());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * ✅ NUEVO: Unirse a una clase grupal existente
     * Los miembros con membresía ELITE pueden unirse sin costo adicional
     */
    @PostMapping("/group-classes/{id}/join")
    public ResponseEntity<?> joinGroupClass(@PathVariable("id") Long groupClassId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            logger.info("👥 POST /api/reservations/group-classes/{}/join - Usuario: {}", groupClassId, email);

            UserBase user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            ReservationResponse joined = reservationService.joinGroupClass(user.getIdUser(), groupClassId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", joined);
            response.put("message", "Te has unido exitosamente a la clase grupal");

            logger.info("✅ Usuario {} se unió exitosamente a clase grupal {}", user.getIdUser(), groupClassId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Errores de validación (ya inscrito, no es grupal, etc.)
            logger.warn("⚠️ Error de validación al unirse a clase grupal: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Validación fallida");
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);

        } catch (IllegalStateException e) {
            // Errores de estado (sin cupo, clase llena, etc.)
            logger.error("❌ Error de estado al unirse a clase grupal: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Error de estado");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);

        } catch (Exception e) {
            logger.error("❌ Error al unirse a clase grupal: {}", e.getMessage(), e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Error interno del servidor");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /**
     * ✅ NUEVO: Unirse a una clase grupal con pago (PREMIUM/BASIC)
     * Endpoint para miembros que no tienen membresía ELITE
     */
    @PostMapping("/group-classes/{id}/join-with-payment")
    public ResponseEntity<?> joinGroupClassWithPayment(
            @PathVariable("id") Long groupClassId,
            @RequestBody Map<String, String> requestBody) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            String paymentMethodId = requestBody.get("paymentMethodId");

            logger.info("💳 POST /api/reservations/group-classes/{}/join-with-payment - Usuario: {}",
                    groupClassId, email);

            if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("error", "Validación fallida");
                resp.put("message", "❌ Se requiere paymentMethodId para procesar el pago");
                return ResponseEntity.badRequest().body(resp);
            }

            UserBase user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

            ReservationResponse joined = reservationService.joinGroupClassWithPayment(
                    user.getIdUser(), groupClassId, paymentMethodId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", joined);
            response.put("message", "Te has unido exitosamente a la clase grupal y el pago fue procesado");
            response.put("paymentAmount", 15000.00);
            response.put("currency", "COP");

            logger.info("✅ Usuario {} se unió exitosamente a clase grupal {} con pago",
                    user.getIdUser(), groupClassId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Error de validación al unirse con pago: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Validación fallida");
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);

        } catch (IllegalStateException e) {
            logger.error("❌ Error de estado al unirse con pago: {}", e.getMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Error de estado");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);

        } catch (Exception e) {
            logger.error("❌ Error al unirse a clase grupal con pago: {}", e.getMessage(), e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("error", "Error interno del servidor");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}
