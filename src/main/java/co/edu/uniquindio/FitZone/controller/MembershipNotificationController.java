package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.CreateNotificationRequest;
import co.edu.uniquindio.FitZone.dto.response.NotificationResponse;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
public class MembershipNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(MembershipNotificationController.class);
    private final IMembershipNotificationService notificationService;

    public MembershipNotificationController(IMembershipNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint para obtener notificaciones por usuario (ruta alternativa para compatibilidad con frontend)
     */
    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable Long userId) {
        logger.debug("GET /api/v1/users/{}/notifications - Consultando notificaciones", userId);
        try {
            List<NotificationResponse> notifications = notificationService.getNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error al consultar notificaciones del usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(@PathVariable Long userId) {
        logger.debug("GET /api/v1/notifications/{} - Consultando notificaciones", userId);
        try {
            List<NotificationResponse> notifications = notificationService.getNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error al consultar notificaciones del usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody CreateNotificationRequest request) {
        logger.info("POST /api/v1/notifications - Creando notificación");
        try {
            NotificationResponse notification = notificationService.createNotification(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (Exception e) {
            logger.error("Error al crear notificación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        logger.debug("PATCH /api/v1/notifications/{}/read - Marcando como leída", notificationId);
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error al marcar notificación {} como leída: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        logger.debug("DELETE /api/v1/notifications/{} - Eliminando notificación", notificationId);
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al eliminar notificación {}: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
