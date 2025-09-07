package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.TimeslotRequest;
import co.edu.uniquindio.FitZone.dto.response.FranchiseResponse;
import co.edu.uniquindio.FitZone.model.entity.Timeslot;
import co.edu.uniquindio.FitZone.service.interfaces.IFranchiseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Controlador para gestionar las franquicias.
 * Permite actualizar los horarios de una franquicia.
 * Solo los usuarios con rol ADMIN pueden acceder a este endpoint.
 */
@RestController
@RequestMapping("/franchises")
public class FranchiseController {

    private static final Logger logger = LoggerFactory.getLogger(FranchiseController.class);

    private final IFranchiseService franchiseService;
    
    public FranchiseController(IFranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    @PutMapping("/timeslots")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FranchiseResponse> updateTimeslots(@RequestBody Set<TimeslotRequest> timeslots) {
        logger.info("PUT /franchises/timeslots - Actualización de horarios de franquicia solicitada por administrador");
        logger.debug("Datos de horarios recibidos - Cantidad de horarios: {}", timeslots.size());
        
        try {
            logger.debug("Procesando solicitud de actualización de horarios");
            FranchiseResponse response = franchiseService.updateTimeslots(timeslots);
            
            logger.info("Horarios de franquicia actualizados exitosamente - ID Franquicia: {}, Nombre: {}, Total horarios: {}", 
                response.idFranchise(), response.name(), response.timeslots().size());
            logger.debug("Actualización de horarios completada exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al actualizar horarios de franquicia - Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
}
