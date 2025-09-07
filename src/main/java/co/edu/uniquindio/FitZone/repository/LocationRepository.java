package co.edu.uniquindio.FitZone.repository;

import co.edu.uniquindio.FitZone.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

/**
 * LocationRepository - Interfaz para operaciones CRUD de la entidad Location.
 * Extiende JpaRepository para proporcionar métodos de acceso a datos.
 */
public interface LocationRepository extends JpaRepository<Location,Long> {

    /**
     * Verifica si una sede con el nombre dado existe.
     * @param name El nombre de la ubicación a verificar.
     * @return true si la ubicación existe, false en caso contrario.
     */
    boolean existsByName(String name);

    /**
     * Verifica si una sede con el número de teléfono dado existe.
     * @param phoneNumber El número de teléfono a verificar.
     * @return true si la ubicación existe, false en caso contrario.
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Verifica si una sede con la dirección dada existe.
     * @param address La dirección a verificar.
     * @return true si la ubicación existe, false en caso contrario.
     */
    boolean existsByAddress(String address);

    /**
     * Busca una sede por su nombre.
     * @param name El nombre de la ubicación.
     * @return Un objeto Optional que contiene la ubicación si se encuentra.
     */
    Optional<Location> findByName(String name);

    /**
     * Busca una lista de sedes que pertenezcan a una franquicia específica.
     * @param franchiseId El ID de la franquicia.
     * @return Una lista de objetos Location que pertenecen a la franquicia dada.
     */
    List<Location> findByFranchiseIdFranchise(Long franchiseId);

    /**
     * Busca una lista de sedes que estén activas.
     * @return Una lista de objetos Location que estén activas.
     */
    List<Location> findByIsActiveTrue();

    /**
     * Busca una sede por su número de teléfono.
     * @param phoneNumber El número de teléfono de la ubicación.
     * @return Un objeto Optional que contiene la ubicación si se encuentra.
     */
    Optional<Location> findByPhoneNumber(String phoneNumber);

    /**
     * Busca una sede por su dirección.
     * @param address La dirección de la ubicación.
     * @return Un objeto Optional que contiene la ubicación si se encuentra.
     */
    Optional<Location> findByAddress(String address);
}
