package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.LocationRequest;
import co.edu.uniquindio.FitZone.dto.response.LocationResponse;
import co.edu.uniquindio.FitZone.service.interfaces.ILocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestionar las ubicaciones.
 * Proporciona endpoints para crear, actualizar y obtener ubicaciones.
 */
@RestController
@RequestMapping("/locations")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final ILocationService locationService;

    public LocationController(ILocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Solo los administradores pueden crear ubicaciones
    public ResponseEntity<LocationResponse> registerLocation(@RequestBody LocationRequest request) {
        logger.info("POST /locations - Registro de nueva ubicación solicitado por administrador");
        logger.debug("Datos de ubicación recibidos - Nombre: {}, Dirección: {}, Teléfono: {}", 
            request.name(), request.address(), request.phoneNumber());
        
        try {
            LocationResponse response = locationService.registerLocation(request);
            logger.info("Ubicación registrada exitosamente - ID: {}, Nombre: {}, Dirección: {}", 
                response.idLocation(), response.name(), response.address());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error al registrar ubicación - Nombre: {}, Error: {}", 
                request.name(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{idLocation}")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo los administradores pueden actualizar ubicaciones
    public ResponseEntity<LocationResponse> updateLocation(@PathVariable Long idLocation, @RequestBody LocationRequest request) {
        logger.info("PUT /locations/{} - Actualización de ubicación solicitada por administrador", idLocation);
        logger.debug("Datos de actualización recibidos - Nombre: {}, Dirección: {}, Teléfono: {}", 
            request.name(), request.address(), request.phoneNumber());
        
        try {
            LocationResponse response = locationService.updateLocation(idLocation, request);
            logger.info("Ubicación actualizada exitosamente - ID: {}, Nombre: {}, Dirección: {}", 
                response.idLocation(), response.name(), response.address());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al actualizar ubicación - ID: {}, Error: {}", 
                idLocation, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{idLocation}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deactivateLocation(@PathVariable Long idLocation) {
        logger.info("PUT /locations/{}/deactivate - Desactivación de ubicación solicitada por administrador", idLocation);
        
        try {
            locationService.deleteLocation(idLocation);
            logger.info("Ubicación desactivada exitosamente - ID: {}", idLocation);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al desactivar ubicación - ID: {}, Error: {}", 
                idLocation, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{idLocation}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long idLocation) {
        logger.debug("GET /locations/{} - Consulta de ubicación por ID", idLocation);
        
        try {
            LocationResponse response = locationService.getLocationById(idLocation);
            logger.debug("Ubicación encontrada por ID - ID: {}, Nombre: {}", 
                idLocation, response.name());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar ubicación por ID - ID: {}, Error: {}", 
                idLocation, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        logger.debug("GET /locations - Consulta de todas las ubicaciones activas");
        
        try {
            List<LocationResponse> locations = locationService.getAllLocations();
            logger.debug("Se encontraron {} ubicaciones activas", locations.size());
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            logger.error("Error al consultar todas las ubicaciones - Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/by-phone")
    public ResponseEntity<LocationResponse> getLocationByPhoneNumber(@RequestParam String phoneNumber) {
        logger.debug("GET /locations/by-phone - Consulta de ubicación por número de teléfono: {}", phoneNumber);
        
        try {
            LocationResponse response = locationService.getLocationByPhoneNumber(phoneNumber);
            logger.debug("Ubicación encontrada por teléfono - Teléfono: {}, Nombre: {}, ID: {}", 
                phoneNumber, response.name(), response.idLocation());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar ubicación por teléfono - Teléfono: {}, Error: {}", 
                phoneNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/by-address")
    public ResponseEntity<LocationResponse> getLocationByAddress(@RequestParam String address) {
        logger.debug("GET /locations/by-address - Consulta de ubicación por dirección: {}", address);
        
        try {
            LocationResponse response = locationService.getLocationAddress(address);
            logger.debug("Ubicación encontrada por dirección - Dirección: {}, Nombre: {}, ID: {}", 
                address, response.name(), response.idLocation());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar ubicación por dirección - Dirección: {}, Error: {}", 
                address, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/by-name")
    public ResponseEntity<LocationResponse> getLocationByName(@RequestParam String name) {
        logger.debug("GET /locations/by-name - Consulta de ubicación por nombre: {}", name);
        
        try {
            LocationResponse response = locationService.getByName(name);
            logger.debug("Ubicación encontrada por nombre - Nombre: {}, ID: {}", 
                name, response.idLocation());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar ubicación por nombre - Nombre: {}, Error: {}", 
                name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
