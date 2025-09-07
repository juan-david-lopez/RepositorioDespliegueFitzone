package co.edu.uniquindio.FitZone.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * DTO para la solicitud de suspensión de membresía.
 * Contiene el ID del usuario, la razón de la suspensión y la fecha de fin de la suspensión.
 * @param userId ID del usuario cuya membresía será suspendida.
 * @param suspensionReason Razón de la suspensión.
 * @param suspensionEnd Fecha de fin de la suspensión.
 */
public record SuspendMembershipRequest(

        @NotNull(message = "El ID del usuario no puede ser nulo")
        Long userId,

        @NotBlank(message = "La razón de la suspension no puede estar vacía")
        String suspensionReason,

        @NotNull(message = "La fecha de fin de suspensión no puede ser nula")
        @FutureOrPresent(message = "La fecha de fin de suspensión debe ser igual o posterior a la fecha actual")
        LocalDate suspensionEnd
) {
}
