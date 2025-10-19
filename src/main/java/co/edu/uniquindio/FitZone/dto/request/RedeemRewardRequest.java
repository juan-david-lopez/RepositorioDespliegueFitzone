package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar el canje de una recompensa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemRewardRequest {

    @NotNull(message = "El ID de la recompensa es obligatorio")
    private Long rewardId;

    private String notes;
}
