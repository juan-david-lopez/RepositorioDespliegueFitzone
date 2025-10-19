package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.CreateNotificationRequest;
import co.edu.uniquindio.FitZone.dto.response.NotificationResponse;

import java.util.List;

public interface IMembershipNotificationService {
    List<NotificationResponse> getNotificationsByUser(Long userId);
    NotificationResponse sendNotification(NotificationResponse notificationRequest);
    NotificationResponse createNotification(CreateNotificationRequest request);
    void markAsRead(Long notificationId);
    void deleteNotification(Long notificationId);
}

