package co.edu.uniquindio.FitZone.dto.request;

import co.edu.uniquindio.FitZone.model.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 *  DTO para manejar las solicitudes de creación o actualización de intervalos de tiempo (horarios).
 * @param day
 * @param openTime
 * @param closeTime
 */
public record TimeslotRequest(

        @NotNull(message = "El día de la semana no puede ser nulo")
        DayOfWeek day,

        @NotNull(message = "La hora de apertura no puede ser nula")
        LocalTime openTime,

        @NotNull(message = "La hora de cierre no puede ser nula")
        LocalTime closeTime
) {
}
