package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.LocationRequest;
import co.edu.uniquindio.FitZone.dto.response.LocationResponse;

import java.util.List;

/**
 * Define los contratos del servicio de sede.
 * Este servicio maneja la lógica de negocio relacionada con las sedes.
 * Aquí se definen métodos para registrar, actualizar, eliminar y consultar sedes.
 */
public interface ILocationService {

    /**
     * Registra una nueva sede en el sistema.
     * Este método recibe un objeto LocationRequest que contiene la información necesaria para crear una sede.
     * @param request objeto que contiene los datos de la sede a registrar
     * @return LocationResponse objeto que representa la respuesta del registro de la sede
     */
    LocationResponse registerLocation(LocationRequest request);

    /**
     * Actualiza la información de una sede existente.
     * Este método recibe un ID de sede y un objeto LocationRequest que contiene los datos actualizados de la sede.
     * @param idLocation ID de la sede a actualizar
     * @param request objeto que contiene los datos actualizados de la sede
     * @return LocationResponse objeto que contiene la información actualizada de la sede
     */
    LocationResponse updateLocation(Long idLocation, LocationRequest request);

    /**
     * Elimina una sede del sistema.
     * Este método elimina la sede de manera lógica, cambiando su estado a inactivo.
     * @param idLocation ID de la sede a eliminar
     */
    void deleteLocation(Long idLocation);

    /**
     * Obtiene una sede por su ID.
     * Este método recibe el ID de la sede y devuelve un objeto LocationResponse que contiene la información de la sede.
     * @param idLocation ID de la sede a buscar
     * @return LocationResponse objeto que representa a la sede encontrada
     */
    LocationResponse getLocationById(Long idLocation);

    /**
     * Obtiene una lista de todas las sedes activas en el sistema.
     * Este método devuelve una lista de objetos LocationResponse que representan a las sedes activas.
     * @return List<LocationResponse> lista de sedes activas
     */
    List<LocationResponse> getAllLocations();

    /**
     * Obtiene una sede por su número de teléfono.
     * Este método recibe el número de teléfono de la sede y devuelve un objeto LocationResponse que contiene la información de la sede.
     * @param phoneNumber número de teléfono de la sede a buscar
     * @return LocationResponse objeto que representa a la sede encontrada
     */
    LocationResponse getLocationByPhoneNumber(String phoneNumber);

    /**
     * Obtiene una sede por su dirección.
     * Este método recibe la dirección de la sede y devuelve un objeto LocationResponse que contiene la información de la sede.
     * @param address dirección de la sede a buscar
     * @return LocationResponse objeto que representa a la sede encontrada
     */
    LocationResponse getLocationAddress(String address);

    /**
     * Obtiene una sede por su nombre.
     * Este método recibe el nombre de la sede y devuelve un objeto LocationResponse que contiene la información de la sede.
     * @param name nombre de la sede a buscar
     * @return LocationResponse objeto que representa a la sede encontrada
     */
    LocationResponse getByName(String name);

}
