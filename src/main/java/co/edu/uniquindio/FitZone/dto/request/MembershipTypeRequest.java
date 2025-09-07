package co.edu.uniquindio.FitZone.dto.request;

import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO REQUEST - Para las peticiones REST
 * Este DTO se utiliza para crear o actualizar un tipo de membresía en el sistema.
 *
 * @param name                           Nombre del tipo de membresía (enum MembershipTypeName).
 * @param description                    Descripción del tipo de membresía.
 * @param monthlyPrice                   Precio mensual de la membresía.
 * @param accessToAllLocation            Indica si la membresía ofrece acceso a todas las ubicaciones.
 * @param groupClassesSessionsIncluded   Número de sesiones de clases grupales incluidas (-1 para ilimitadas).
 * @param personalTrainingIncluded       Número de entrenamientos personales incluidos.
 * @param specializedClassesIncluded     Indica si la membresía incluye clases especializadas.
 */
public record MembershipTypeRequest(

        @NotBlank(message = "Se debe especificar el nombre del tipo de membresía")
        MembershipTypeName name,

        @NotBlank(message = "La descripción no puede estar vacía")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
        String description,

        @NotBlank(message = "El precio mensual no puede estar vacío")
        BigDecimal monthlyPrice,

        @NotNull(message = "Se debe especificar si tiene acceso a todas las ubicaciones")
        Boolean accessToAllLocation,

        @NotNull(message = "Debe especificar la cantidad de sesiones de clases grupales incluidas")
        @Min(value = -1, message = "Las sesiones deben ser -1 (ilimitadas) o más")
        Integer groupClassesSessionsIncluded,

        @NotNull(message = "Debe especificar la cantidad de entrenamientos personales incluidos")
        @Min(value = 0, message = "El entrenamiento personal debe ser 0 o más")
        Integer personalTrainingIncluded,

        @NotNull(message = "Debe especificar si incluye clases especializadas")
        Boolean specializedClassesIncluded
) {
}


