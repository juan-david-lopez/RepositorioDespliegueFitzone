package co.edu.uniquindio.FitZone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Clase que representa la solicitud para restablecer la contraseña de un usuario.
 * Contiene el token de verificación y la nueva contraseña.
 */
public record ResetPasswordRequest(
        String token,

        @NotBlank(message = "La contraseña no puede estar vacía")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&)")
        String newPassword
) {
}
