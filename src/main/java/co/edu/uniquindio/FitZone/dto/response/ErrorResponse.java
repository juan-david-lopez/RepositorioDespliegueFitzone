package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de error estandarizadas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private String path;
    private Integer status;
    private LocalDateTime timestamp;
    private Object details;

    public static ErrorResponse notFound(String message, String path) {
        return ErrorResponse.builder()
                .error("NOT_FOUND")
                .message(message)
                .path(path)
                .status(404)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse noMembership(Long userId) {
        return ErrorResponse.builder()
                .error("NO_MEMBERSHIP_FOUND")
                .message("El usuario no tiene una membresía activa")
                .status(404)
                .timestamp(LocalDateTime.now())
                .details(new MembershipErrorDetails(userId, "El usuario debe adquirir una membresía para acceder a este servicio"))
                .build();
    }

    public static ErrorResponse internalError(String message) {
        return ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message(message)
                .status(500)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MembershipErrorDetails {
        private Long userId;
        private String suggestion;
    }
}

