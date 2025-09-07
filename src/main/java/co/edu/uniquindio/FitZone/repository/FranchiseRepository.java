package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * FranchiseRepository - Interfaz para operaciones CRUD de la entidad Franchise.
 * Extiende JpaRepository para proporcionar métodos de acceso a datos.
 */
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    /**
     * Busca una franquicia por su nombre.
     * @param name El nombre de la franquicia.
     * @return Un objeto Optional que contiene la franquicia si se encuentra.
     */
    Optional<Franchise> findByName(String name);

}
