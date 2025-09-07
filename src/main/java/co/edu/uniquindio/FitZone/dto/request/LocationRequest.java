package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Clase que representa una solicitud para crear o actualizar una ubicación.
 * Contiene validaciones para los campos de nombre, dirección y número de teléfono.
 */
public record LocationRequest(

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String name,

        @NotBlank(message = "la dirección no puede estar vacía")
        @Size(min = 2, max = 100, message = "La dirección debe tener entre 2 y 100 caracteres")
        String address,

        @NotBlank(message = "El número de teléfono no puede estar vacío")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "El número de teléfono debe ser válido")
        @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres")
        String phoneNumber
) {
}
