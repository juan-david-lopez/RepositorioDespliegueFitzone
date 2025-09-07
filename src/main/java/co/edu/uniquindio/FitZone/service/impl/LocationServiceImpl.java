package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.LocationRequest;
import co.edu.uniquindio.FitZone.dto.response.LocationResponse;
import co.edu.uniquindio.FitZone.exception.FranchiseNotFoundException;
import co.edu.uniquindio.FitZone.exception.LocationNotFoundException;
import co.edu.uniquindio.FitZone.exception.ResourceAlreadyExistsException;
import co.edu.uniquindio.FitZone.model.entity.Franchise;
import co.edu.uniquindio.FitZone.model.entity.Location;
import co.edu.uniquindio.FitZone.repository.FranchiseRepository;
import co.edu.uniquindio.FitZone.repository.LocationRepository;
import co.edu.uniquindio.FitZone.service.interfaces.ILocationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio para gestionar las sedes de la franquicia FitZone.
 * Proporciona métodos para registrar, actualizar, eliminar y consultar sedes.
 * Utiliza repositorios para interactuar con la base de datos y maneja excepciones
 * específicas para casos como sede no encontrada o nombre ya registrado.
 */
@Service
public class LocationServiceImpl implements ILocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final LocationRepository locationRepository;
    private final FranchiseRepository franchiseRepository;

    public LocationServiceImpl(LocationRepository locationRepository, FranchiseRepository franchiseRepository) {
        this.locationRepository = locationRepository;
        this.franchiseRepository = franchiseRepository;
    }


    @Override
    public LocationResponse registerLocation(LocationRequest request) {
        logger.info("Iniciando registro de nueva sede: {}", request.name());
        logger.debug("Datos de la sede a registrar - Dirección: {}, Teléfono: {}", 
            request.address(), request.phoneNumber());

        Franchise defaultFranchise = franchiseRepository.findByName("FitZone")
                .orElseThrow(()-> {
                    logger.error("Franquicia 'FitZone' no encontrada para registrar la sede: {}", request.name());
                    return new FranchiseNotFoundException("Franquicia 'FitZone' no encontrada");
                });

        logger.debug("Franquicia FitZone encontrada, verificando duplicados");

        if(locationRepository.existsByName(request.name())){
            logger.warn("Intento de registro de sede con nombre duplicado: {}", request.name());
            throw new ResourceAlreadyExistsException("El nombre de la sede ya se encuentra registrado");
        }

        if(locationRepository.existsByAddress(request.address())){
            logger.warn("Intento de registro de sede con dirección duplicada: {}", request.address());
            throw  new ResourceAlreadyExistsException("La dirección ingresada ya corresponde a una sede registrada");
        }

        logger.debug("Validaciones de duplicados exitosas, creando nueva sede");
        Location location = new Location();
        location.setName(request.name());
        location.setAddress(request.address());
        location.setPhoneNumber(request.phoneNumber());

        //Asignamos la franquicia por defecto a la nueva sede
        location.setFranchise(defaultFranchise);
        location.setMembers(new ArrayList<>());

        logger.debug("Guardando nueva sede en la base de datos");
        Location savedLocation = locationRepository.save(location);
        logger.info("Sede registrada exitosamente con ID: {}", savedLocation.getIdLocation());

        return new LocationResponse(
                savedLocation.getIdLocation(),
                savedLocation.getName(),
                savedLocation.getAddress(),
                savedLocation.getPhoneNumber(),
                savedLocation.getIsActive()
        );
    }


    @Override
    public LocationResponse updateLocation(Long idLocation, LocationRequest request) {
        logger.info("Iniciando actualización de sede con ID: {}", idLocation);
        logger.debug("Nuevos datos de la sede - Nombre: {}, Dirección: {}, Teléfono: {}", 
            request.name(), request.address(), request.phoneNumber());

        Location existingLocation = locationRepository.findById(idLocation)
                .orElseThrow(() -> {
                    logger.error("Sede no encontrada para actualización con ID: {}", idLocation);
                    return new LocationNotFoundException("Sede no encontrada");
                });

        logger.debug("Sede encontrada: {} (ID: {})", existingLocation.getName(), idLocation);

        if(!existingLocation.getName().equals(request.name()) && locationRepository.existsByName(request.name())){
            logger.warn("Intento de actualización con nombre duplicado: {}", request.name());
            throw new ResourceAlreadyExistsException("El nuevo nombre de la sede ya se encuentra registrado");
        }

        if(!existingLocation.getAddress().equals(request.address()) && locationRepository.existsByAddress(request.address())){
            logger.warn("Intento de actualización con dirección duplicada: {}", request.address());
            throw new ResourceAlreadyExistsException("La nueva dirección de la sede ya se encuentra asignada a otra sede");
        }

        if(!existingLocation.getPhoneNumber().equals(request.phoneNumber()) && locationRepository.existsByPhoneNumber(request.phoneNumber())){
            logger.warn("Intento de actualización con teléfono duplicado: {}", request.phoneNumber());
            throw new ResourceAlreadyExistsException("El nuevo teléfono de la sede ya se encuentra asignado a otra sede");
        }

        logger.debug("Validaciones de duplicados exitosas, actualizando datos de la sede");
        existingLocation.setName(request.name());
        existingLocation.setAddress(request.address());
        existingLocation.setPhoneNumber(request.phoneNumber());

        logger.debug("Guardando sede actualizada en la base de datos");
        Location updatedLocation = locationRepository.save(existingLocation);
        logger.info("Sede actualizada exitosamente - ID: {}, Nombre: {}", 
            updatedLocation.getIdLocation(), updatedLocation.getName());

        return new LocationResponse(
                updatedLocation.getIdLocation(),
                updatedLocation.getName(),
                updatedLocation.getAddress(),
                updatedLocation.getPhoneNumber(),
                updatedLocation.getIsActive()
        );
    }

    @Override
    public void deleteLocation(Long idLocation) {
        logger.info("Iniciando eliminación lógica de sede con ID: {}", idLocation);

        Location location = locationRepository.findById(idLocation)
                .orElseThrow( () -> {
                    logger.error("Sede no encontrada para eliminación con ID: {}", idLocation);
                    return new LocationNotFoundException("Sede no encontrada");
                });

        logger.debug("Sede encontrada para eliminación: {} (ID: {})", location.getName(), idLocation);
        location.setIsActive(false);
        
        logger.debug("Desactivando sede y guardando cambios");
        locationRepository.save(location);
        logger.info("Sede desactivada exitosamente - ID: {}, Nombre: {}", idLocation, location.getName());
    }

    @Override
    public LocationResponse getLocationById(Long idLocation) {
        logger.debug("Consultando sede por ID: {}", idLocation);

        Location location = locationRepository.findById(idLocation)
                .orElseThrow( () -> {
                    logger.error("Sede no encontrada con ID: {}", idLocation);
                    return new LocationNotFoundException("Sede no encontrada");
                });

        logger.debug("Sede encontrada: {} (ID: {})", location.getName(), idLocation);
        return new LocationResponse(
                location.getIdLocation(),
                location.getName(),
                location.getAddress(),
                location.getPhoneNumber(),
                location.getIsActive()
        );
    }

    @Override
    public List<LocationResponse> getAllLocations() {
        logger.debug("Consultando todas las sedes activas");
        
        List<Location> locations = locationRepository.findByIsActiveTrue();
        logger.debug("Se encontraron {} sedes activas", locations.size());

        return locations.stream()
                .map(location -> new LocationResponse(
                        location.getIdLocation(),
                        location.getName(),
                        location.getAddress(),
                        location.getPhoneNumber(),
                        location.getIsActive()
                )).toList();
    }

    @Override
    public LocationResponse getLocationByPhoneNumber(String phoneNumber) {
        logger.debug("Consultando sede por número de teléfono: {}", phoneNumber);

        Location location = locationRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow( () -> {
                    logger.error("Sede no encontrada con número de teléfono: {}", phoneNumber);
                    return new LocationNotFoundException("Sede no encontrada");
                });

        logger.debug("Sede encontrada por teléfono: {} (ID: {})", location.getName(), location.getIdLocation());
        return new LocationResponse(
                location.getIdLocation(),
                location.getName(),
                location.getAddress(),
                location.getPhoneNumber(),
                location.getIsActive()
        );
    }

    @Override
    public LocationResponse getLocationAddress(String address) {
        logger.debug("Consultando sede por dirección: {}", address);

        Location location = locationRepository.findByAddress(address)
                .orElseThrow( () -> {
                    logger.error("Sede no encontrada con dirección: {}", address);
                    return new LocationNotFoundException("Sede no encontrada");
                });

        logger.debug("Sede encontrada por dirección: {} (ID: {})", location.getName(), location.getIdLocation());
        return new LocationResponse(
                location.getIdLocation(),
                location.getName(),
                location.getAddress(),
                location.getPhoneNumber(),
                location.getIsActive()
        );
    }

    @Override
    public LocationResponse getByName(String name) {
        logger.debug("Consultando sede por nombre: {}", name);

        Location location = locationRepository.findByName(name)
                .orElseThrow( () -> {
                    logger.error("Sede no encontrada con nombre: {}", name);
                    return new LocationNotFoundException("Sede no encontrada");
                });

        logger.debug("Sede encontrada por nombre: {} (ID: {})", name, location.getIdLocation());
        return new LocationResponse(
                location.getIdLocation(),
                location.getName(),
                location.getAddress(),
                location.getPhoneNumber(),
                location.getIsActive()
        );
    }
}
