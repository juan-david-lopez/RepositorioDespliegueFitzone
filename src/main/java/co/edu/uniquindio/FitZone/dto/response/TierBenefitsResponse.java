package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para responder con los beneficios de un tier de fidelización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierBenefitsResponse {
    private String tierName;
    private Integer renewalDiscountPercentage;
    private Integer additionalClassesPerMonth;
    private Integer freeGuestPassesPerMonth;
    private Boolean priorityReservations;
    private String description;
}

