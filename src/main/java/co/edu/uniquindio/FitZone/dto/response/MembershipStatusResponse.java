package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la respuesta del estado de una membresía
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipStatusResponse {
    private boolean isActive;
    private LocalDate expirationDate;
    private MembershipStatus status;
    private String message;

    public static MembershipStatusResponse createActiveResponse(LocalDate expirationDate) {
        return MembershipStatusResponse.builder()
                .isActive(true)
                .expirationDate(expirationDate)
                .status(MembershipStatus.ACTIVE)
                .message("Membresía activa hasta: " + expirationDate)
                .build();
    }

    public static MembershipStatusResponse createInactiveResponse(String message) {
        return createInactiveResponse(message, MembershipStatus.EXPIRED);
    }

    public static MembershipStatusResponse createInactiveResponse(String message, MembershipStatus status) {
        return MembershipStatusResponse.builder()
                .isActive(false)
                .status(status)
                .message(message)
                .build();
    }
}
