package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad MembershipType.
 * Proporciona métodos para realizar operaciones CRUD y consultas personalizadas.
 */
public interface MembershipTypeRepository extends CrudRepository<MembershipType, Long> {

    /**
     * Busca un tipo de membresía por su nombre.
     * @param name El nombre del tipo de membresía a buscar.
     * @return Un objeto Optional que contiene el tipo de membresía si se encuentra.
     */
    Optional<MembershipType> findByName(MembershipTypeName name);

    /**
     * Verifica si existe un tipo de membresía con el nombre dado existe.
     * @param name El nombre del tipo de membresía a verificar.
     * @return true si el tipo de membresía existe, false en caso contrario.
     */
    boolean existsByName(MembershipTypeName name);


}
