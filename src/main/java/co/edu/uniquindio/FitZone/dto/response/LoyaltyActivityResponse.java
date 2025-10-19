package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para responder con información de una actividad de fidelización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyActivityResponse {
    private Long idLoyaltyActivity;
    private ActivityType activityType;
    private String activityTypeDisplayName;
    private Integer pointsEarned;
    private String description;
    private Long referenceId;
    private LocalDateTime activityDate;
    private Boolean isBonusActivity;
    private LocalDateTime expirationDate;
    private Boolean isExpired;
    private Boolean isCancelled;
}

