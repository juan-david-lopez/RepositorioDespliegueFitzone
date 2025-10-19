package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.dto.request.CreateMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.PaymentIntentRequest;
import co.edu.uniquindio.FitZone.dto.request.ProcessPaymentRequest;
import co.edu.uniquindio.FitZone.dto.request.SuspendMembershipRequest;
import co.edu.uniquindio.FitZone.dto.request.RenewMembershipRequest;
import co.edu.uniquindio.FitZone.dto.response.MembershipResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipStatusResponse;
import co.edu.uniquindio.FitZone.dto.response.MembershipDetailsResponse;
import co.edu.uniquindio.FitZone.dto.response.PaymentIntentResponse;
import co.edu.uniquindio.FitZone.dto.response.ProcessPaymentResponse;
import co.edu.uniquindio.FitZone.dto.response.ErrorResponse;
import co.edu.uniquindio.FitZone.exception.*;
import co.edu.uniquindio.FitZone.service.impl.StripePaymentService;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import co.edu.uniquindio.FitZone.repository.UserRepository;
import co.edu.uniquindio.FitZone.model.entity.User;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar las membresías de los usuarios.
 * Proporciona endpoints para crear, consultar, suspender, reactivar y cancelar membresías
 * así como para crear intentos de pago utilizando Stripe.
 */
@Slf4j
@RestController
@RequestMapping("/memberships")
@CrossOrigin(origins = "*")
public class MembershipController {

    private final IMembershipService membershipService;
    private final StripePaymentService stripePaymentService;
    private final UserRepository userRepository;

    public MembershipController(IMembershipService membershipService, StripePaymentService stripePaymentService, UserRepository userRepository) {
        this.membershipService = membershipService;
        this.stripePaymentService = stripePaymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentIntentRequest request) {
        log.info("POST /memberships/create-payment-intent - Creación de intento de pago solicitada");
        log.debug("Datos de pago recibidos - Membership ID: {}", request.getMembershipId());

        try {
            PaymentIntentResponse response = stripePaymentService.createPaymentIntent(request);

            log.info("Intento de pago creado exitosamente - Payment Intent ID: {}", response.getPaymentIntentId());
            log.debug("Intento de pago creado - Client Secret generado para membership: {}", request.getMembershipId());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error al crear intento de pago con Stripe - Membership ID: {}, Error: {}",
                    request.getMembershipId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("STRIPE_ERROR")
                            .message("Error al crear intento de pago: " + e.getMessage())
                            .status(500)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error inesperado al crear intento de pago - Membership ID: {}, Error: {}",
                    request.getMembershipId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.internalError("Error interno del servidor"));
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> createMembership(@RequestBody CreateMembershipRequest request) {
        log.info("POST /memberships/create recibido - userId: {}, MembershipTypeId: {}, paymentIntentId: {}",
            request.userId(), request.MembershipTypeId(), request.paymentIntentId());

        try {
            // ============================================
            // PASO 1: Validaciones básicas
            // ============================================
            if (request.paymentIntentId() == null || request.paymentIntentId().isEmpty()) {
                log.error("❌ paymentIntentId está vacío");
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .error("BAD_REQUEST")
                                .message("paymentIntentId no puede estar vacío")
                                .status(400)
                                .timestamp(java.time.LocalDateTime.now())
                                .build());
            }

            // ============================================
            // PASO 2: Validar formato del PaymentIntent
            // ============================================
            if (!request.paymentIntentId().startsWith("pi_")) {
                log.error("❌ Formato incorrecto de paymentIntentId: {}", request.paymentIntentId());
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .error("INVALID_PAYMENT_IDENTIFIER")
                                .message("paymentIntentId debe tener formato pi_xxx (PaymentIntent), no pm_xxx (PaymentMethod). Recibido: " + request.paymentIntentId())
                                .status(400)
                                .timestamp(java.time.LocalDateTime.now())
                                .build());
            }

            log.info("✅ Validaciones iniciales pasadas, procesando creación de membresía");

            // ============================================
            // PASO 3: Llamar al servicio
            // ============================================
            MembershipResponse response = membershipService.createMembership(request);

            log.info("✅ Membresía creada exitosamente - ID: {}, Usuario: {}, Tipo: {}",
                response.id(), response.userId(), response.membershipTypeName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InvalidPaymentIdentifierException e) {
            log.error("❌ Error de validación de PaymentIntent: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.builder()
                            .error("INVALID_PAYMENT_IDENTIFIER")
                            .message(e.getMessage())
                            .status(400)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());

        } catch (UserNotFoundException | MembershipTypeNotFoundException | LocationNotFoundException e) {
            log.error("❌ Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("RESOURCE_NOT_FOUND")
                            .message(e.getMessage())
                            .status(404)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());

        } catch (ResourceAlreadyExistsException e) {
            log.error("❌ Conflicto con recurso existente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.builder()
                            .error("RESOURCE_CONFLICT")
                            .message(e.getMessage())
                            .status(409)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());

        } catch (Exception e) {
            log.error("❌ Error inesperado al crear membresía - Usuario ID: {}, Error: {}",
                request.userId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("INTERNAL_SERVER_ERROR")
                            .message("Error creando membresía: " + e.getMessage())
                            .status(500)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getMembershipByUserId(@PathVariable Long userId) {
        log.debug("GET /memberships/{} - Consulta de membresía por ID de usuario", userId);
        
        try {
            MembershipResponse response = membershipService.getMembershipByUserId(userId);
            log.debug("Membresía encontrada para usuario - ID: {}, Usuario: {}, Tipo: {}",
                userId, response.userId(), response.membershipTypeName());
            return ResponseEntity.ok(response);
        } catch (MembershipTypeNotFoundException e) {
            log.error("Membresía no encontrada para usuario - ID: {}, Error: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("MEMBERSHIP_NOT_FOUND")
                            .message(e.getMessage())
                            .status(404)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error al consultar membresía por ID de usuario - ID: {}, Error: {}",
                userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.internalError("Error consultando membresía"));
        }
    }

    @PatchMapping("/suspend")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> suspendMembership(@RequestBody SuspendMembershipRequest request) {
        log.info("PATCH /memberships/suspend - Suspensión de membresía solicitada por usuario autorizado");
        log.debug("Datos de suspensión recibidos - Usuario ID: {}, Razón: {}, Fecha fin: {}",
            request.userId(), request.suspensionReason(), request.suspensionEnd());
        
        try {
            MembershipResponse response = membershipService.suspendMembership(request);
            log.info("Membresía suspendida exitosamente - ID: {}, Usuario: {}, Estado: {}",
                response.id(), response.userId(), response.status());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al suspender membresía - Usuario ID: {}, Error: {}",
                request.userId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.internalError("Error suspendiendo membresía"));
        }
    }

    @PatchMapping("/reactivate/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> reactivateMembership(@PathVariable Long userId) {
        log.info("PATCH /memberships/reactivate/{} - Reactivación de membresía solicitada por usuario autorizado", userId);
        
        try {
            MembershipResponse response = membershipService.reactivateMembership(userId);
            log.info("Membresía reactivada exitosamente - ID: {}, Usuario: {}",
                response.id(), response.userId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al reactivar membresía - Usuario ID: {}, Error: {}",
                userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.internalError("Error reactivando membresía"));
        }
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<MembershipStatusResponse> checkMembershipStatus(@PathVariable Long userId) {
        log.debug("GET /memberships/status/{} - Verificando estado de membresía", userId);
        
        try {
            MembershipStatusResponse response = membershipService.checkMembershipStatus(userId);
            log.debug("Estado de membresía verificado - Usuario ID: {}, Activa: {}, Estado: {}",
                userId, response.isActive(), response.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al verificar estado de membresía - Usuario ID: {}, Error: {}",
                userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MembershipStatusResponse.createInactiveResponse("Error al verificar el estado de la membresía"));
        }
    }

    /**
     * Endpoint para verificar el estado de membresía del usuario autenticado
     * Obtiene automáticamente el ID del usuario desde el JWT token
     */
    @GetMapping("/my-status")
    public ResponseEntity<MembershipStatusResponse> checkMyMembershipStatus() {
        log.debug("GET /memberships/my-status - Verificando estado de membresía del usuario autenticado");

        try {
            // Obtener el email del usuario autenticado desde el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            log.debug("Usuario autenticado: {}", email);

            // Buscar el usuario por email
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            log.debug("ID del usuario encontrado: {}", user.getIdUser());

            // Verificar el estado de la membresía
            MembershipStatusResponse response = membershipService.checkMembershipStatus(user.getIdUser());
            log.debug("Estado de membresía verificado - Usuario: {}, Activa: {}, Estado: {}",
                email, response.isActive(), response.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al verificar estado de membresía del usuario autenticado - Error: {}",
                e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MembershipStatusResponse.createInactiveResponse("Error al verificar el estado de la membresía"));
        }
    }

    @PostMapping("/renew")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> renewMembership(@RequestBody RenewMembershipRequest request) {
        log.info("POST /memberships/renew - Renovación de membresía solicitada por usuario autorizado");
        log.debug("Datos de renovación - Usuario ID: {}, Nuevo tipo: {}, Duración: {} meses",
            request.userId(), request.newMembershipType(), request.durationMonths());

        try {
            MembershipResponse response = membershipService.renewMembership(request);
            log.info("Membresía renovada exitosamente - ID: {}, Usuario: {}, Nuevo tipo: {}",
                response.id(), response.userId(), response.membershipTypeName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al renovar membresía - Usuario ID: {}, Error: {}",
                request.userId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.internalError("Error renovando membresía"));
        }
    }

    /**
     * Endpoint para obtener detalles completos de la membresía de un usuario
     * Retorna información estructurada incluso si el usuario no tiene membresía
     */
    @GetMapping("/details/{userId}")
    public ResponseEntity<MembershipDetailsResponse> getMembershipDetails(@PathVariable Long userId) {
        log.debug("GET /memberships/details/{} - Consulta de detalles de membresía", userId);

        try {
            // Verificar si el usuario existe
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Intentar obtener la membresía
            try {
                MembershipResponse membership = membershipService.getMembershipByUserId(userId);
                log.debug("Detalles de membresía encontrados - Usuario ID: {}, Tipo: {}, Estado: {}",
                    userId, membership.membershipTypeName(), membership.status());
                return ResponseEntity.ok(MembershipDetailsResponse.withMembership(membership));
            } catch (Exception e) {
                // Usuario existe pero no tiene membresía
                log.warn("Usuario sin membresía activa - ID: {}", userId);
                boolean needsLocation = (user.getMainLocation() == null);
                return ResponseEntity.ok(MembershipDetailsResponse.noMembership(userId, needsLocation));
            }
        } catch (Exception e) {
            log.error("Error al consultar detalles de membresía - Usuario ID: {}, Error: {}",
                userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MembershipDetailsResponse.builder()
                        .hasMembership(false)
                        .userId(userId)
                        .message("Usuario no encontrado")
                        .build());
        }
    }

    /**
     * Endpoint para procesar pago y crear membresía en un solo paso.
     * El backend crea el PaymentIntent usando el PaymentMethod del frontend.
     */
    @PostMapping("/process-payment")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> processPaymentAndCreateMembership(@RequestBody ProcessPaymentRequest request) {
        log.info("📝 POST /memberships/process-payment - Procesamiento de pago solicitado");
        log.debug("Datos recibidos - userId: {}, membershipTypeId: {}, mainLocationId: {}, paymentMethodId: {}",
            request.userId(), request.membershipTypeId(), request.mainLocationId(), request.paymentMethodId());

        try {
            // ============================================
            // PASO 1: Validaciones básicas
            // ============================================
            if (request.paymentMethodId() == null || request.paymentMethodId().isEmpty()) {
                log.error("❌ paymentMethodId está vacío");
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .error("BAD_REQUEST")
                                .message("paymentMethodId no puede estar vacío")
                                .status(400)
                                .timestamp(java.time.LocalDateTime.now())
                                .build());
            }

            // ============================================
            // PASO 2: Validar formato del PaymentMethod
            // ============================================
            if (!request.paymentMethodId().startsWith("pm_")) {
                log.error("❌ Formato incorrecto de paymentMethodId: {}", request.paymentMethodId());
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .error("INVALID_PAYMENT_METHOD")
                                .message("paymentMethodId debe tener formato pm_xxx (PaymentMethod). Recibido: " + request.paymentMethodId())
                                .status(400)
                                .timestamp(java.time.LocalDateTime.now())
                                .build());
            }

            log.info("✅ Validaciones iniciales pasadas, procesando pago y membresía");

            // ============================================
            // PASO 3: Llamar al servicio
            // ============================================
            ProcessPaymentResponse response = membershipService.processPaymentAndCreateMembership(request);

            log.info("✅ Pago procesado y membresía creada exitosamente - Membership ID: {}, PaymentIntent: {}, Usuario: {}",
                response.getMembershipId(), response.getPaymentIntentId(), response.getUserId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.builder()
                            .error("INVALID_REQUEST")
                            .message(e.getMessage())
                            .status(400)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());

        } catch (UserNotFoundException | MembershipTypeNotFoundException | LocationNotFoundException e) {
            log.error("❌ Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("RESOURCE_NOT_FOUND")
                            .message(e.getMessage())
                            .status(404)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());

        } catch (ResourceAlreadyExistsException e) {
            log.error("❌ Conflicto con recurso existente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.builder()
                            .error("RESOURCE_CONFLICT")
                            .message(e.getMessage())
                            .status(409)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());

        } catch (StripeException e) {
            log.error("❌ Error al procesar pago con Stripe: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(ErrorResponse.builder()
                            .error("PAYMENT_FAILED")
                            .message("Error procesando pago: " + e.getMessage())
                            .status(402)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());

        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar pago y crear membresía - Usuario ID: {}, Error: {}",
                request.userId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("INTERNAL_SERVER_ERROR")
                            .message("Error procesando pago y creando membresía: " + e.getMessage())
                            .status(500)
                            .timestamp(java.time.LocalDateTime.now())
                            .build());
        }
    }
}
