package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private Long userId;

    // Tipo de reserva (alineado con frontend)
    private String type; // "GROUP_CLASS", "PERSONAL_TRAINING", "SPECIALIZED_SPACE"

    // Fecha y horas separadas (formato frontend)
    private String scheduledDate;      // "2025-10-21"
    private String scheduledStartTime; // "08:00"
    private String scheduledEndTime;   // "09:00"

    // Campos legacy (mantener por compatibilidad)
    private String reservationType;
    private Long targetId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private String status;
    private String paymentIntentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== NUEVOS CAMPOS ==========

    // Indica si la reserva requiere/requirió pago
    private Boolean requiresPayment;

    // Monto pagado
    private BigDecimal paymentAmount;

    // Indica si es una reserva grupal
    private Boolean isGroup;

    // Cupo máximo (para clases grupales)
    private Integer maxCapacity;

    // Número actual de participantes
    private Integer currentParticipants;

    // Lista de IDs de participantes
    private List<Long> participantUserIds;

    // ID del instructor (entrenamientos personales)
    private Long instructorId;

    // Nombre del instructor
    private String instructorName;

    // Nombre de la clase
    private String className;

    // ID de ubicación
    private Long locationId;

    // Nombre de ubicación
    private String locationName;

    // Indica si hay cupo disponible
    private Boolean hasAvailableCapacity;

    // Objetos relacionados (frontend espera estos)
    private GroupClassDTO groupClass;
    private SpecializedSpaceDTO specializedSpace;
    private InstructorDTO instructor;

    // DTOs anidados
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupClassDTO {
        private Long id;
        private String name;
        private String description;
        private Integer maxCapacity;
        private Integer currentParticipants;
        private BigDecimal price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecializedSpaceDTO {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorDTO {
        private Long id;
        private String name;
        private String specialization;
    }
}
