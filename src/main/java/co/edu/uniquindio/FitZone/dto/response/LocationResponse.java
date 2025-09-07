package co.edu.uniquindio.FitZone.dto.response;

/**
 * DTO RESPONSE - Para las respuestas REST
 * Contiene los datos de una sede que se quieren enviar después de una operación
 * @param idLocation
 * @param name
 * @param address
 * @param phoneNumber
 * @param isActive
 */
public record LocationResponse(

        Long idLocation,
        String name,
        String address,
        String phoneNumber,
        Boolean isActive
) {
}
