package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para respuesta de detalles de membresía que incluye el caso de usuarios sin membresía.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipDetailsResponse {
    private boolean hasMembership;
    private Long membershipId;
    private Long userId;
    private MembershipTypeName membershipTypeName;
    private Long locationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private MembershipStatus status;
    private String message;
    private boolean needsLocation;

    /**
     * Crea una respuesta para usuarios sin membresía.
     */
    public static MembershipDetailsResponse noMembership(Long userId, boolean needsLocation) {
        return MembershipDetailsResponse.builder()
                .hasMembership(false)
                .userId(userId)
                .needsLocation(needsLocation)
                .message(needsLocation
                    ? "El usuario debe asignar una ubicación principal antes de adquirir una membresía"
                    : "El usuario no tiene una membresía activa. Puede adquirir una membresía.")
                .build();
    }

    /**
     * Crea una respuesta para usuarios con membresía.
     */
    public static MembershipDetailsResponse withMembership(MembershipResponse membership) {
        return MembershipDetailsResponse.builder()
                .hasMembership(true)
                .membershipId(membership.id())
                .userId(membership.userId())
                .membershipTypeName(membership.membershipTypeName())
                .locationId(membership.locationId())
                .startDate(membership.startDate())
                .endDate(membership.endDate())
                .status(membership.status())
                .needsLocation(false)
                .message("Membresía activa")
                .build();
    }
}

