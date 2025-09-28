package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.entity.User;
import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UserResponse record que representa la respuesta de un usuario.
 */
public record UserResponse(
        Long idUser,
        String name,
        String email,
        String role
) {
    
    /**
     * Creates a simplified UserResponse for authentication purposes
     * @param user The user entity
     * @return A simplified UserResponse with only the necessary fields
     */
    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        String fullName = user.getPersonalInformation() != null ? 
                user.getPersonalInformation().getFirstName() + " " + 
                (user.getPersonalInformation().getLastName() != null ? 
                        user.getPersonalInformation().getLastName() : "") : "";
        
        return new UserResponse(
                user.getIdUser(),
                fullName.trim(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
