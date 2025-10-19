package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.CreateMembershipRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.UserBaseRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para manejar webhooks de Stripe.
 *
 * ⚠️ DESHABILITADO: Este controlador solo se activa si 'stripe.webhook.enabled=true'
 *
 * Para el flujo actual SIN webhook, este controlador NO es necesario.
 * El pago se procesa directamente en MembershipController.processPaymentAndCreateMembership()
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "stripe.webhook.enabled", havingValue = "true", matchIfMissing = false)
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.api.key.secret}")
    private String stripeSecretKey;

    private final IMembershipService membershipService;
    private final UserBaseRepository userBaseRepository;
    private final MembershipTypeRepository membershipTypeRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Webhook de Stripe inicializado correctamente");
    }

    /**
     * Endpoint para recibir webhooks de Stripe.
     * POST /api/v1/webhooks/stripe
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Webhook de Stripe recibido");

        Event event;

        try {
            // Verificar la firma del webhook
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Error de verificación de firma del webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma inválida");
        } catch (Exception e) {
            log.error("Error al procesar webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al procesar webhook");
        }

        // Manejar el evento según su tipo
        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;

                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;

                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;

                case "customer.subscription.created":
                    handleSubscriptionCreated(event);
                    break;

                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event);
                    break;

                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(event);
                    break;

                default:
                    log.info("Evento no manejado: {}", event.getType());
                    break;
            }
        } catch (Exception e) {
            log.error("Error al manejar evento de Stripe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar evento");
        }

        return ResponseEntity.ok("Webhook recibido");
    }

    /**
     * Maneja el evento payment_intent.succeeded.
     */
    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent == null) {
            log.error("No se pudo deserializar el PaymentIntent");
            return;
        }

        log.info("Pago exitoso - Payment Intent ID: {}", paymentIntent.getId());
        log.info("Monto: {} {}", paymentIntent.getAmount(), paymentIntent.getCurrency());
        log.info("Metadata: {}", paymentIntent.getMetadata());

        String userIdStr = paymentIntent.getMetadata().get("userId");
        String membershipTypeStr = paymentIntent.getMetadata().get("membershipType");

        if (userIdStr == null || membershipTypeStr == null) {
            log.error("Metadata incompleta en PaymentIntent: userId={}, membershipType={}", userIdStr, membershipTypeStr);
            return;
        }

        try {
            Long userId = Long.parseLong(userIdStr);
            log.info("Procesando activación de membresía - Usuario: {}, Tipo: {}", userId, membershipTypeStr);

            // Validar que el usuario existe
            UserBase user = userBaseRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

            // Validar ubicación principal
            if (user.getMainLocation() == null) {
                log.error("El usuario {} no tiene ubicación principal asignada. No se puede activar la membresía.", userId);
                return;
            }

            // Validar tipo de membresía
            MembershipTypeName membershipTypeName = MembershipTypeName.valueOf(membershipTypeStr);
            MembershipType membershipType = membershipTypeRepository.findByName(membershipTypeName)
                    .orElseThrow(() -> new RuntimeException("Tipo de membresía no encontrado: " + membershipTypeStr));

            // Crear la membresía
            CreateMembershipRequest membershipRequest = new CreateMembershipRequest(
                    userId,
                    membershipType.getIdMembershipType(),
                    user.getMainLocation().getIdLocation(),
                    paymentIntent.getId()
            );

            MembershipResponse membership = membershipService.createMembership(membershipRequest);

            log.info("✅ Membresía creada y activada exitosamente - Usuario: {}, Membership ID: {}, Transaction: {}",
                    userId, membership.id(), paymentIntent.getId());

        } catch (NumberFormatException e) {
            log.error("Error al parsear userId: {}", userIdStr, e);
        } catch (IllegalArgumentException e) {
            log.error("Tipo de membresía inválido: {}", membershipTypeStr, e);
        } catch (Exception e) {
            log.error("Error al crear membresía después del pago exitoso. PaymentIntent: {}, Error: {}",
                    paymentIntent.getId(), e.getMessage(), e);
        }
    }

    /**
     * Maneja el evento payment_intent.payment_failed.
     */
    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (paymentIntent == null) {
            log.error("No se pudo deserializar el PaymentIntent");
            return;
        }

        log.error("Pago fallido - Payment Intent ID: {}", paymentIntent.getId());
        log.error("Razón: {}", paymentIntent.getLastPaymentError() != null ?
                paymentIntent.getLastPaymentError().getMessage() : "Desconocida");

        // Aquí puedes agregar lógica para:
        // - Notificar al usuario del fallo
        // - Registrar el intento fallido
        // - Ofrecer métodos de pago alternativos

        String userId = paymentIntent.getMetadata().get("userId");
        log.info("Usuario afectado: {}", userId);

        // TODO: Implementar lógica de notificación
    }

    /**
     * Maneja el evento charge.refunded.
     */
    private void handleChargeRefunded(Event event) {
        Charge charge = (Charge) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (charge == null) {
            log.error("No se pudo deserializar el Charge");
            return;
        }

        log.info("Reembolso procesado - Charge ID: {}", charge.getId());
        log.info("Monto reembolsado: {} {}", charge.getAmountRefunded(), charge.getCurrency());

        // Aquí puedes agregar lógica para:
        // - Actualizar el estado de la membresía (cancelar o suspender)
        // - Notificar al usuario del reembolso
        // - Registrar en el historial de transacciones

        // TODO: Implementar lógica de reembolso
    }

    /**
     * Maneja el evento checkout.session.completed.
     */
    private void handleCheckoutSessionCompleted(Event event) {
        com.stripe.model.checkout.Session session =
                (com.stripe.model.checkout.Session) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (session == null) {
            log.error("No se pudo deserializar la Session");
            return;
        }

        log.info("Checkout Session completada - Session ID: {}", session.getId());
        log.info("Payment Status: {}", session.getPaymentStatus());
        log.info("Metadata: {}", session.getMetadata());

        // Aquí puedes agregar lógica para:
        // - Crear la membresía
        // - Enviar email de bienvenida
        // - Activar acceso del usuario

        String userId = session.getMetadata().get("userId");
        String membershipType = session.getMetadata().get("membershipType");

        log.info("Usuario: {}, Tipo de membresía: {}", userId, membershipType);

        // TODO: Implementar lógica de activación de membresía
    }

    /**
     * Maneja el evento customer.subscription.created.
     */
    private void handleSubscriptionCreated(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (subscription == null) {
            log.error("No se pudo deserializar la Subscription");
            return;
        }

        log.info("Suscripción creada - Subscription ID: {}", subscription.getId());
        log.info("Estado: {}", subscription.getStatus());

        // TODO: Implementar lógica de suscripción
    }

    /**
     * Maneja el evento customer.subscription.updated.
     */
    private void handleSubscriptionUpdated(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (subscription == null) {
            log.error("No se pudo deserializar la Subscription");
            return;
        }

        log.info("Suscripción actualizada - Subscription ID: {}", subscription.getId());
        log.info("Nuevo estado: {}", subscription.getStatus());

        // TODO: Implementar lógica de actualización de suscripción
    }

    /**
     * Maneja el evento customer.subscription.deleted.
     */
    private void handleSubscriptionDeleted(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (subscription == null) {
            log.error("No se pudo deserializar la Subscription");
            return;
        }

        log.info("Suscripción eliminada - Subscription ID: {}", subscription.getId());

        // Aquí puedes agregar lógica para:
        // - Desactivar la membresía
        // - Notificar al usuario
        // - Registrar la cancelación

        // TODO: Implementar lógica de cancelación de suscripción
    }
}
