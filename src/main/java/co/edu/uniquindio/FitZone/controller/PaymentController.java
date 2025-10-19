package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.*;
import co.edu.uniquindio.FitZone.dto.response.*;
import co.edu.uniquindio.FitZone.service.interfaces.IStripePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar pagos con Stripe.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final IStripePaymentService stripePaymentService;

    /**
     * Crea un Payment Intent de Stripe.
     * POST /api/v1/payments/create-intent
     */
    @PostMapping("/create-intent")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        try {
            log.info("Solicitud para crear Payment Intent recibida");
            PaymentIntentResponse response = stripePaymentService.createPaymentIntent(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al crear Payment Intent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * Crea una sesión de Stripe Checkout.
     * POST /api/v1/payments/create-checkout-session
     */
    @PostMapping("/create-checkout-session")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request) {
        try {
            log.info("Solicitud para crear Checkout Session recibida");
            CheckoutSessionResponse response = stripePaymentService.createCheckoutSession(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error al crear Checkout Session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CheckoutSessionResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Verifica el estado de un pago.
     * GET /api/v1/payments/{paymentId}/status
     */
    @GetMapping("/{paymentId}/status")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentId) {
        try {
            log.info("Solicitud para obtener estado del pago: {}", paymentId);
            PaymentStatusResponse response = stripePaymentService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener estado del pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * Confirma un pago.
     * POST /api/v1/payments/{paymentIntentId}/confirm
     */
    @PostMapping("/{paymentIntentId}/confirm")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<GenericResponse> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            log.info("Solicitud para confirmar pago: {}", paymentIntentId);
            GenericResponse response = stripePaymentService.confirmPayment(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al confirmar pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * Obtiene los métodos de pago guardados de un usuario.
     * GET /api/v1/users/{userId}/payment-methods
     */
    @GetMapping("/users/{userId}/payment-methods")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<?> getSavedPaymentMethods(@PathVariable Long userId) {
        try {
            log.info("Solicitud para obtener métodos de pago del usuario: {}", userId);
            SavedPaymentMethodsResponse response = stripePaymentService.getSavedPaymentMethods(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener métodos de pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * Guarda un método de pago para un usuario.
     * POST /api/v1/users/{userId}/payment-methods
     */
    @PostMapping("/users/{userId}/payment-methods")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<GenericResponse> savePaymentMethod(
            @PathVariable Long userId,
            @Valid @RequestBody SavePaymentMethodRequest request) {
        try {
            log.info("Solicitud para guardar método de pago del usuario: {}", userId);
            GenericResponse response = stripePaymentService.savePaymentMethod(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al guardar método de pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * Elimina un método de pago.
     * DELETE /api/v1/users/{userId}/payment-methods/{paymentMethodId}
     */
    @DeleteMapping("/users/{userId}/payment-methods/{paymentMethodId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<GenericResponse> deletePaymentMethod(
            @PathVariable Long userId,
            @PathVariable String paymentMethodId) {
        try {
            log.info("Solicitud para eliminar método de pago {} del usuario: {}", paymentMethodId, userId);
            GenericResponse response = stripePaymentService.deletePaymentMethod(userId, paymentMethodId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al eliminar método de pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * Activa la membresía después de verificar que el pago fue exitoso.
     * Este endpoint NO requiere webhook. El frontend llama aquí después de confirmar el pago.
     * POST /api/v1/payments/{paymentIntentId}/activate-membership
     */
    @PostMapping("/{paymentIntentId}/activate-membership")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<?> activateMembershipAfterPayment(
            @PathVariable String paymentIntentId,
            @RequestParam Long userId,
            @RequestParam String membershipType) {
        try {
            log.info("Solicitud para activar membresía sin webhook - PaymentIntent: {}, Usuario: {}",
                    paymentIntentId, userId);

            GenericResponse response = stripePaymentService.activateMembershipAfterPayment(
                    paymentIntentId, userId, membershipType);

            if (response.isSuccess()) {
                log.info("Membresía activada exitosamente para usuario: {}", userId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("No se pudo activar membresía - Usuario: {}, Razón: {}",
                        userId, response.getError());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error al activar membresía - PaymentIntent: {}, Error: {}",
                    paymentIntentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .error("Error al activar membresía: " + e.getMessage())
                            .build());
        }
    }
}
