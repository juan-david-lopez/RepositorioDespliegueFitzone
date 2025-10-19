package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserBaseRepository - Para operaciones de ESCRITURA en users_base.
 * Usar este repositorio para: Login, Registro, Cambio de contraseña, etc.
 */
@Repository
public interface UserBaseRepository extends JpaRepository<UserBase, Long> {

    /**
     * Verifica si un usuario con el correo electrónico dado existe.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si un usuario con el número de documento dado existe.
     */
    boolean existsByPersonalInformation_DocumentNumber(String documentNumber);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     */
    Optional<UserBase> findByEmail(String email);

    /**
     * Busca un usuario por su número de documento.
     */
    Optional<UserBase> findByPersonalInformation_DocumentNumber(String documentNumber);

    /**
     * Busca un usuario por su token de reseteo de contraseña.
     */
    Optional<UserBase> findByPasswordResetToken(String token);

    /**
     * Busca todos los usuarios con un rol específico que estén activos.
     */
    List<UserBase> findByRoleAndIsActiveTrue(UserRole role);
}
