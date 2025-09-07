package co.edu.uniquindio.FitZone.dto.response;

import co.edu.uniquindio.FitZone.model.entity.Timeslot;

import java.util.List;
import java.util.Set;

/**
 * Representación de la respuesta de una franquicia.
 * @param idFranchise
 * @param name
 * @param timeslots
 */
public record FranchiseResponse(

        Long idFranchise,
        String name,
        Set<Timeslot> timeslots
) {
}
