package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.CreateNotificationRequest;
import co.edu.uniquindio.FitZone.dto.response.NotificationResponse;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de notificaciones de membresía.
 */
@Service
public class MembershipNotificationServiceImpl implements IMembershipNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipNotificationServiceImpl.class);

    @Override
    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        logger.info("Obteniendo notificaciones para usuario ID: {}", userId);

        // Implementación básica - puedes expandir con base de datos
        List<NotificationResponse> notifications = new ArrayList<>();

        NotificationResponse notification = NotificationResponse.builder()
                .id(1L)
                .userId(userId)
                .message("Tu membresía está próxima a vencer")
                .type("EXPIRATION_WARNING")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notifications.add(notification);
        return notifications;
    }

    @Override
    public NotificationResponse sendNotification(NotificationResponse notificationRequest) {
        logger.info("Enviando notificación al usuario ID: {}", notificationRequest.getUserId());

        // Implementación de envío de notificación
        return NotificationResponse.builder()
                .id(System.currentTimeMillis()) // ID temporal
                .userId(notificationRequest.getUserId())
                .message(notificationRequest.getMessage())
                .type(notificationRequest.getType())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        logger.info("Creando notificación para usuario ID: {}", request.userId());

        // Implementación de creación de notificación
        return NotificationResponse.builder()
                .id(System.currentTimeMillis()) // ID temporal
                .userId(request.userId())
                .message(request.message())
                .type(request.type())
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public void markAsRead(Long notificationId) {
        logger.info("Marcando notificación como leída: ID={}", notificationId);
        // Implementación básica para marcar como leída
        // TODO: Implementar persistencia con base de datos
    }

    @Override
    public void deleteNotification(Long notificationId) {
        logger.info("Eliminando notificación: ID={}", notificationId);
        // Implementación básica para eliminar notificación
        // TODO: Implementar persistencia con base de datos
    }

    /**
     * Método adicional para enviar recordatorios de expiración
     */
    public void sendExpirationReminder(Long userId, int daysUntilExpiration) {
        logger.info("Enviando recordatorio de expiración al usuario {} - Días restantes: {}",
                   userId, daysUntilExpiration);

        NotificationResponse notification = NotificationResponse.builder()
                .userId(userId)
                .message(String.format("Tu membresía vence en %d días", daysUntilExpiration))
                .type("EXPIRATION_REMINDER")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        sendNotification(notification);
    }
}
