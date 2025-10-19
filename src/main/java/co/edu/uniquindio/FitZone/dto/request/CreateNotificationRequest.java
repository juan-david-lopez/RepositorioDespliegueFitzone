package co.edu.uniquindio.FitZone.dto.request;

public record CreateNotificationRequest(
    Long userId,
    String type,
    String title,
    String message,
    String priority,
    String actionUrl,
    String actionLabel
) {
}
