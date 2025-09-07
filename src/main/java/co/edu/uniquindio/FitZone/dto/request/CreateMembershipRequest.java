package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Clase que representa la solicitud para crear una membresía.
 * Contiene los datos necesarios para crear una nueva membresía en el sistema.
 *
 * @param userId            ID del usuario que adquiere la membresía.
 * @param MembershipTypeId  ID del tipo de membresía.
 * @param mainLocationId    ID de la sede principal asociada a la membresía.
 * @param paymentIntentId   ID de la intención de pago asociada a la membresía.
 */
public record CreateMembershipRequest(

        @NotNull(message = "El ID del usuario no puede ser nulo")
        Long userId,

        @NotNull(message = "El ID del tipo de membresía no puede ser nulo")
        Long MembershipTypeId,

        @NotNull(message = "El ID de la sede principal no puede ser nulo")
        Long mainLocationId,

        @NotBlank(message = "El ID de la intención de pago no puede estar vacío")
        String paymentIntentId
) {
}
