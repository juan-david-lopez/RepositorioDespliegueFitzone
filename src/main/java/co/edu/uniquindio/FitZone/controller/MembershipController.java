package co.edu.uniquindio.FitZone.controller;


import co.edu.uniquindio.FitZone.dto.request.CreateMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.PaymentIntentRequest;
import co.edu.uniquindio.FitZone.dto.request.SuspendMembershipRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;
import co.edu.uniquindio.FitZone.integration.payment.StripeService;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar las membresías de los usuarios.
 * Proporciona endpoints para crear, consultar, suspender, reactivar y cancelar membresías
 * así como para crear intentos de pago utilizando Stripe.
 */
@RestController
@RequestMapping("/memberships")
public class MembershipController {

    private static final Logger logger = LoggerFactory.getLogger(MembershipController.class);

    private final IMembershipService membershipService;
    private final StripeService stripeService;

    public MembershipController(IMembershipService membershipService, StripeService stripeService) {
        this.membershipService = membershipService;
        this.stripeService = stripeService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentIntentRequest request) {
        logger.info("POST /memberships/create-payment-intent - Creación de intento de pago solicitada");
        logger.debug("Datos de pago recibidos - Monto: {}, Moneda: {}, Descripción: {}",
                request.amount(), request.currency(), request.description());

        try {
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(request.amount().longValue(),
                    request.currency(), request.description());

            logger.info("Intento de pago creado exitosamente - Client Secret generado");
            logger.debug("Intento de pago creado - Monto: {}, Moneda: {}",
                    request.amount(), request.currency());

            return ResponseEntity.ok(paymentIntent.getClientSecret());
        } catch (StripeException e) {
            logger.error("Error al crear intento de pago con Stripe - Monto: {}, Error: {}",
                    request.amount(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear intento de pago: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<MembershipResponse> createMembership(@RequestBody CreateMembershipRequest request) {
        logger.info("POST /memberships/create - Creación de membresía solicitada por usuario autorizado");
        logger.debug("Datos de membresía recibidos - Usuario ID: {}, Tipo: {}, Sede: {}, PaymentIntent: {}", 
            request.userId(), request.MembershipTypeId(), request.mainLocationId(), request.paymentIntentId());
        
        try {
            MembershipResponse response = membershipService.createMembership(request);
            logger.info("Membresía creada exitosamente - ID: {}, Usuario: {}, Tipo: {}", 
                response.id(), response.userId(), response.membershipTypeName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error al crear membresía - Usuario ID: {}, Error: {}", 
                request.userId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<MembershipResponse> getMembershipByUserId(@PathVariable Long userId) {
        logger.debug("GET /memberships/{} - Consulta de membresía por ID de usuario", userId);
        
        try {
            MembershipResponse response = membershipService.getMembershipByUserId(userId);
            logger.debug("Membresía encontrada para usuario - ID: {}, Usuario: {}, Tipo: {}", 
                userId, response.userId(), response.membershipTypeName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar membresía por ID de usuario - ID: {}, Error: {}", 
                userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PatchMapping("/suspend")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<MembershipResponse> suspendMembership(@RequestBody SuspendMembershipRequest request) {
        logger.info("PATCH /memberships/suspend - Suspensión de membresía solicitada por usuario autorizado");
        logger.debug("Datos de suspensión recibidos - Usuario ID: {}, Razón: {}, Fecha fin: {}", 
            request.userId(), request.suspensionReason(), request.suspensionEnd());
        
        try {
            MembershipResponse response = membershipService.suspendMembership(request);
            logger.info("Membresía suspendida exitosamente - ID: {}, Usuario: {}, Estado: {}", 
                response.id(), response.userId(), response.status());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al suspender membresía - Usuario ID: {}, Error: {}", 
                request.userId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PatchMapping("/reactivate/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<MembershipResponse> reactivateMembership(@PathVariable Long userId) {
        logger.info("PATCH /memberships/reactivate/{} - Reactivación de membresía solicitada por usuario autorizado", userId);
        
        try {
            MembershipResponse response = membershipService.reactivateMembership(userId);
            logger.info("Membresía reactivada exitosamente - ID: {}, Usuario: {}, Estado: {}", 
                response.id(), response.userId(), response.status());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al reactivar membresía - Usuario ID: {}, Error: {}", 
                userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/cancel/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Void> cancelMembership(@PathVariable Long userId) {
        logger.info("DELETE /memberships/cancel/{} - Cancelación de membresía solicitada por usuario autorizado", userId);
        
        try {
            membershipService.cancelMembership(userId);
            logger.info("Membresía cancelada exitosamente - Usuario ID: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al cancelar membresía - Usuario ID: {}, Error: {}", 
                userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}