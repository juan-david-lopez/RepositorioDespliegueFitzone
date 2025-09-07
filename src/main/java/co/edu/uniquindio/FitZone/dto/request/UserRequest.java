package co.edu.uniquindio.FitZone.dto.request;

import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO REQUEST - Para las peticiones REST
 * Este DTO se utiliza para crear un nuevo usuario en el sistema.
 * @param firstName
 * @param lastName
 * @param email
 * @param documentType
 * @param documentNumber
 * @param password
 * @param phoneNumber
 * @param birthDate
 * @param emergencyContactPhone
 * @param medicalConditions
 * @param mainLocationId
 * @param role
 */
public record UserRequest(

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido no puede estar vacío")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String lastName,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El email debe ser válido")
        @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
        String email,

        @NotBlank(message = "Se debe especificar el tipo de documento")
        DocumentType documentType,

        @NotBlank(message = "El número de documento no puede estar vacío")
        @Size(max = 50, message = "El número de documento no puede exceder los 50 caracteres")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Se debe ingresar un número de documento válido")
        String documentNumber,

        @NotBlank(message = "La contraseña no puede estar vacía")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&)")
        String password,

        @NotBlank(message = "El número de teléfono no puede estar vacío")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "El número de teléfono debe ser válido")
        @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres")
        String phoneNumber,

        @NotNull(message = "La fecha de cumpleaños no puede ser nula")
        @Past(message = "La fecha de cumpleaños no puede ser en el futuro ")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @NotBlank(message = "El número de teléfono no puede estar vacío")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "El número de teléfono debe ser válido")
        @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres")
        String emergencyContactPhone,

        String medicalConditions,

        Long mainLocationId,

        @NotBlank(message = "El rol del usuario no puede estar vacío")
        UserRole role
) {
}
