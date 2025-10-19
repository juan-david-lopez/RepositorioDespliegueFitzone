package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * UserResponse record que representa la respuesta de un usuario.
 */
public record UserResponse(
        @JsonProperty("idUser") // Nombre principal
        Long idUser,
        String name,
        String email,
        String role,
        String membershipType, // ✅ AGREGADO: Tipo de membresía activa
        Boolean isActive,
        String phoneNumber,
        String mainLocation
) {
    /**
     * Alias para compatibilidad con frontend que espera "id" en lugar de "idUser"
     */
    @JsonProperty("id")
    public Long id() {
        return idUser;
    }

    /**
     * Creates a simplified UserResponse for authentication purposes
     * @param user The user entity
     * @return A simplified UserResponse with only the necessary fields
     */
    public static UserResponse fromUser(User user) {
        return fromUser(user, null);
    }

    /**
     * Creates a complete UserResponse with membership information
     * @param user The user entity
     * @param membershipType The active membership type (can be null)
     * @return A complete UserResponse with all fields
     */
    public static UserResponse fromUser(User user, String membershipType) {
        if (user == null) {
            return null;
        }
        
        String fullName = user.getPersonalInformation() != null ? 
                user.getPersonalInformation().getFirstName() + " " + 
                (user.getPersonalInformation().getLastName() != null ? 
                        user.getPersonalInformation().getLastName() : "") : "";
        
        // ✅ CORREGIDO: mainLocation es un Long, no un objeto Location
        String locationName = user.getMainLocation() != null ?
                "Location ID: " + user.getMainLocation() : null;

        String phone = user.getPersonalInformation() != null ?
                user.getPersonalInformation().getPhoneNumber() : null;

        return new UserResponse(
                user.getIdUser(),
                fullName.trim(),
                user.getEmail(),
                user.getRole().name(),
                membershipType, // ✅ Incluir tipo de membresía
                user.getIsActive(),
                phone,
                locationName
        );
    }
}
