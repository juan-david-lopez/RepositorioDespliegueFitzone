package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.TimeslotRequest;
import co.edu.uniquindio.FitZone.dto.response.FranchiseResponse;
import co.edu.uniquindio.FitZone.model.entity.Timeslot;

import java.util.Set;

/**
 * Interfaz que define los servicios relacionados con las franquicias.
 */
public interface IFranchiseService {

    /**
     * Actualiza los intervalos de tiempo (horarios) de una franquicia.
     * @param timeslots conjunto de solicitudes de intervalos de tiempo a actualizar.
     * @return la respuesta de la franquicia actualizada.
     */
    FranchiseResponse updateTimeslots(Set<TimeslotRequest> timeslots);

}
