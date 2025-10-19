package co.edu.uniquindio.FitZone.dto.request;

import co.edu.uniquindio.FitZone.model.enums.ActivityType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para solicitudes de registro de actividades de fidelización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyActivityRequest {

    private Long userId;

    @NotNull(message = "El tipo de actividad es obligatorio")
    private ActivityType activityType;

    private Integer pointsEarned;

    private String description;

    private Long referenceId;

    private Boolean isBonusActivity;

    private LocalDateTime activityDate;

    private LocalDateTime expirationDate;
}
