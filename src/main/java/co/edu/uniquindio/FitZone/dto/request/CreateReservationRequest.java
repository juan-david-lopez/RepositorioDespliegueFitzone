package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para crear una reserva.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    // Tipo de reserva: GROUP_CLASS, PERSONAL_TRAINING, SPECIALIZED_SPACE
    @NotBlank(message = "El tipo de reserva es obligatorio")
    private String reservationType;

    // id del objetivo (clase, espacio o instructor) opcional
    private Long targetId;

    @NotBlank(message = "La fecha/hora de inicio es obligatoria")
    private String startDateTime; // ISO-8601 string

    @NotBlank(message = "La fecha/hora de fin es obligatoria")
    private String endDateTime;   // ISO-8601 string

    // ========== NUEVOS CAMPOS ==========

    // Payment Method ID de Stripe (para clases grupales que requieren pago)
    private String paymentMethodId;

    // Nombre de la clase o actividad
    private String className;

    // Ubicación de la clase
    private Long locationId;

    // Para entrenamientos personales: ID del instructor
    private Long instructorId;

    // Para clases grupales: cupo máximo
    private Integer maxCapacity;

    // Lista de IDs de usuarios participantes (para clases grupales)
    // El usuario que crea la reserva se añade automáticamente
    private List<Long> additionalParticipantIds;

    // Monto a pagar (solo para validación, se calculará en backend)
    private BigDecimal amount;
}
