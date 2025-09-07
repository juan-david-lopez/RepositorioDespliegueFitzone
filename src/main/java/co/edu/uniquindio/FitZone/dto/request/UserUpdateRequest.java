package co.edu.uniquindio.FitZone.dto.request;

import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO para actualizar la información de un usuario.
 *
 * @param firstName             Nombre del usuario.
 * @param lastName              Apellidos del usuario.
 * @param email                 Correo electrónico del usuario.
 * @param documentType          Tipo de documento del usuario.
 * @param documentNumber        Número de documento del usuario.
 * @param phoneNumber           Número de teléfono del usuario.
 * @param birthDate             Fecha de nacimiento del usuario.
 * @param emergencyContactPhone Número de teléfono de contacto de emergencia.
 * @param medicalConditions     Condiciones médicas del usuario (opcional).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserUpdateRequest(

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String firstName,

        @NotBlank(message = "Los apellidos no pueden estar vacíos")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
        String lastName,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El email debe ser válido")
        @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
        String email,

        @NotNull(message = "Se debe especificar el tipo de documento")
        DocumentType documentType,

        @NotBlank(message = "El número de documento no puede estar vacío")
        @Size(max = 50, message = "El número de documento no puede exceder los 50 caracteres")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Se debe ingresar un número de documento válido")
        String documentNumber,

        @NotBlank(message = "El número de teléfono no puede estar vacío")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "El número de teléfono debe ser válido")
        @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres")
        String phoneNumber,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser en el pasado")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @NotBlank(message = "El número de teléfono no puede estar vacío")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "El número de teléfono debe ser válido")
        @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres")
        String emergencyContactPhone,

        String medicalConditions

) {
}
