package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de configuración de auto-renovación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoRenewalSettingsResponse {
    private Long userId;
    private boolean enabled;
    private int daysBeforeExpiration;
}

