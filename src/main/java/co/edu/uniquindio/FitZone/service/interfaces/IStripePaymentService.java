package co.edu.uniquindio.FitZone.service.interfaces;

import co.edu.uniquindio.FitZone.dto.request.*;
import co.edu.uniquindio.FitZone.dto.response.*;

/**
 * Interfaz del servicio de pagos con Stripe.
 */
public interface IStripePaymentService {

    /**
     * Crea un Payment Intent de Stripe.
     */
    PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) throws Exception;

    /**
     * Crea una sesión de Stripe Checkout.
     */
    CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request) throws Exception;

    /**
     * Obtiene el estado de un pago.
     */
    PaymentStatusResponse getPaymentStatus(String paymentId) throws Exception;

    /**
     * Confirma un pago.
     */
    GenericResponse confirmPayment(String paymentIntentId) throws Exception;

    /**
     * Obtiene los métodos de pago guardados de un usuario.
     */
    SavedPaymentMethodsResponse getSavedPaymentMethods(Long userId) throws Exception;

    /**
     * Guarda un método de pago para un usuario.
     */
    GenericResponse savePaymentMethod(Long userId, SavePaymentMethodRequest request) throws Exception;

    /**
     * Elimina un método de pago.
     */
    GenericResponse deletePaymentMethod(Long userId, String paymentMethodId) throws Exception;

    /**
     * Activa la membresía después de verificar que el pago fue exitoso.
     * Este método NO requiere webhook - se llama directamente desde el frontend.
     */
    GenericResponse activateMembershipAfterPayment(String paymentIntentId, Long userId, String membershipType) throws Exception;
}
