package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Interfaz para operaciones CRUD de la entidad User.
 * Extiende JpaRepository para proporcionar métodos de acceso a datos.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Verifica si un usuario con el correo electrónico dado existe.
     * @param email La dirección de correo eletrónico a verificar
     * @return true si el usuario existe, false en caso contrario.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si un usuario con el número de documento dado existe.
     * @param documentNumber El número de documento a verificar.
     * @return true si el usuario existe, false en caso contrario.
     */
    boolean existsByPersonalInformation_DocumentNumber(String documentNumber);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * @param email La dirección de correo electrónico del usuario.
     * @return Un objeto Optional que contiene al usuario si se encuentra.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su número de documento.
     * @param documentNumber El número de documento del usuario.
     * @return Un objeto Optional que contiene el usuario si se encuentra.
     */
    Optional<User> findByPersonalInformation_DocumentNumber(String documentNumber);

    /**
     * Busca una lista de usuarios que estén activos.
     * @return Una lista de objetos User que estén activos.
     */
    List<User>findByIsActiveTrue();

    /**
     * Busca un usuario por su token de restablecimiento de contraseña.
     * @param passwordResetToken El token de restablecimiento de contraseña del usuario.
     * @return Un objeto Optional que contiene al usuario si se encuentra.
     */
    Optional<User> findByPasswordResetToken(String passwordResetToken);

}
