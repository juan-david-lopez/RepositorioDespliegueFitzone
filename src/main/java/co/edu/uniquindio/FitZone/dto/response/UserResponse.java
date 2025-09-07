package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UserResponse record que representa la respuesta de un usuario.
 * @param idUser
 * @param firstName
 * @param lastName
 * @param email
 * @param documentType
 * @param documentNumber
 * @param phoneNumber
 * @param birthDate
 * @param emergencyContactPhone
 * @param medicalConditions
 * @param userRole
 * @param createdAt
 */
public record UserResponse(

        Long idUser,
        String firstName,
        String lastName,
        String email,
        DocumentType documentType,
        String documentNumber,
        String phoneNumber,
        LocalDate birthDate,
        String emergencyContactPhone,
        String medicalConditions,
        UserRole userRole,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt

) {
}
