package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.CreateMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.ProcessPaymentRequest;
import co.edu.uniquindio.FitZone.dto.request.RenewMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.SuspendMembershipRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipStatusResponse;
import co.edu.uniquindio.FitZone.dto.response.ProcessPaymentResponse;
import com.stripe.exception.StripeException;

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

    /**
     * Verifica el estado de la membresía de un usuario.
     *
     * @param userId ID del usuario.
     * @return MembershipStatusResponse con el estado actual de la membresía.
     */
    MembershipStatusResponse checkMembershipStatus(Long userId);

    /**
     * Renueva una membresía existente.
     * Permite cambiar el tipo de membresía y duración.
     *
     * @param request Datos necesarios para renovar la membresía.
     * @return MembershipResponse con los detalles de la membresía renovada.
     */
    MembershipResponse renewMembership(RenewMembershipRequest request);

    /**
     * Procesa un pago y crea una membresía en un solo paso.
     * El backend crea el PaymentIntent usando el PaymentMethod del frontend.
     *
     * @param request Datos del pago y membresía (incluye paymentMethodId)
     * @return ProcessPaymentResponse con los detalles del pago y membresía
     * @throws StripeException Si hay error al procesar el pago con Stripe
     */
    ProcessPaymentResponse processPaymentAndCreateMembership(ProcessPaymentRequest request) throws StripeException;
}
