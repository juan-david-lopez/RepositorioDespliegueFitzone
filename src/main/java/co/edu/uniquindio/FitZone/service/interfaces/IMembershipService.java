package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.CreateMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.SuspendMembershipRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;

/**
 * Servicio para la gestión de membresías de gimnasio.
 * Proporciona métodos para crear, consultar, suspender, reactivar y cancelar membresías.
 * Las implementaciones deben manejar la lógica de negocio y validaciones relacionadas con el ciclo de vida de la membresía.
 */
public interface IMembershipService {

    /**
     * Crea una nueva membresía para un usuario.
     * Válida la existencia del usuario, tipo de membresía, sede y estado del pago.
     *
     * @param request Datos necesarios para crear la membresía.
     * @return MembershipResponse con los detalles de la membresía creada.
     */
    MembershipResponse createMembership(CreateMembershipRequest request);

    /**
     * Obtiene los detalles de la membresía por el ID del usuario.
     *
     * @param userId ID del usuario.
     * @return MembershipResponse con los detalles de la membresía.
     */
    MembershipResponse getMembershipByUserId(Long userId);

    /**
     * Obtiene los detalles de la membresía por el número de documento del usuario.
     *
     * @param documentNumber Número de documento del usuario.
     * @return Detalles de la membresía.
     */
    MembershipResponse getMembershipByDocumentNumber(String documentNumber);

    /**
     * Suspende una membresía.
     * Requiere una razón y una fecha fin de suspensión.
     *
     * @param request Datos necesarios para suspender la membresía.
     * @return Detalles de la membresía suspendida.
     */
    MembershipResponse suspendMembership(SuspendMembershipRequest request);

    /**
     * Reactiva una membresía suspendida de un usuario.
     * Extiende la fecha de finalización por la duración de la suspensión.
     *
     * @param userId ID del usuario.
     * @return MembershipResponse con los detalles de la membresía reactivada.
     * @throws RuntimeException si la membresía no está suspendida.
     */
    MembershipResponse reactivateMembership(Long userId);

    /**
     * Cancela una membresía activa o suspendida de un usuario.
     * Cambia el estado de la membresía a CANCELLED.
     *
     * @param userId ID del usuario.
     */
    void cancelMembership(Long userId);

}
