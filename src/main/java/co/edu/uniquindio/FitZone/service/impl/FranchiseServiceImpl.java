package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.TimeslotRequest;
import co.edu.uniquindio.FitZone.dto.response.FranchiseResponse;
import co.edu.uniquindio.FitZone.exception.FranchiseNotFoundException;
import co.edu.uniquindio.FitZone.model.entity.Franchise;
import co.edu.uniquindio.FitZone.model.entity.Timeslot;
import co.edu.uniquindio.FitZone.repository.FranchiseRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IFranchiseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Implementación del servicio para gestionar franquicias.
 * Proporciona métodos para actualizar los horarios de una franquicia.
 */
@Service
public class FranchiseServiceImpl implements IFranchiseService {

    private static final Logger logger = LoggerFactory.getLogger(FranchiseServiceImpl.class);
    
    private final FranchiseRepository franchiseRepository;

    public FranchiseServiceImpl(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }


    @Override
    public FranchiseResponse updateTimeslots(Set<TimeslotRequest> timeslots) {
        
        logger.info("Iniciando proceso de actualización de horarios de la franquicia FitZone");
        logger.debug("Número de horarios a actualizar: {}", timeslots.size());
        
        Franchise franchise = franchiseRepository.findByName("FitZone")
                .orElseThrow(() -> {
                    logger.error("Franquicia FitZone no encontrada en la base de datos");
                    return new FranchiseNotFoundException(" Franquicia no encontrada");
                });
        
        logger.debug("Franquicia FitZone encontrada con ID: {}", franchise.getIdFranchise());
        logger.debug("Horarios actuales de la franquicia: {}", franchise.getTimeslots().size());
                
        // Limpiar los horarios antiguos y agregar los nuevos
        logger.debug("Limpiando horarios antiguos de la franquicia");
        franchise.getTimeslots().clear();

        logger.debug("Procesando {} nuevos horarios", timeslots.size());
        for(TimeslotRequest request : timeslots) {
            logger.debug("Creando horario para el día: {}, horario: {} - {}", 
                request.day(), request.openTime(), request.closeTime());
            
            Timeslot timeslot = new Timeslot();
            timeslot.setDay(request.day());
            timeslot.setOpenTime(request.openTime());
            timeslot.setCloseTime(request.closeTime());
            timeslot.setFranchise(franchise);
            franchise.getTimeslots().add(timeslot);
        }

        logger.debug("Guardando franquicia actualizada en la base de datos");
        Franchise updatedFranchise = franchiseRepository.save(franchise);
        logger.info("Horarios de la franquicia FitZone actualizados exitosamente. Total de horarios: {}", 
            updatedFranchise.getTimeslots().size());

        return new FranchiseResponse(
                updatedFranchise.getIdFranchise(),
                updatedFranchise.getName(),
                updatedFranchise.getTimeslots()
        );
    }


}
