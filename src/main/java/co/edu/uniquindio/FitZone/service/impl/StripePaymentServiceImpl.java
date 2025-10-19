package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.*;
import co.edu.uniquindio.FitZone.dto.response.*;
import co.edu.uniquindio.FitZone.model.entity.MembershipType;
import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.repository.MembershipTypeRepository;
import co.edu.uniquindio.FitZone.repository.UserBaseRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IMembershipService;
import co.edu.uniquindio.FitZone.service.interfaces.IReceiptService;
import co.edu.uniquindio.FitZone.service.interfaces.IStripePaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de pagos con Stripe.
 * Flujo simplificado SIN webhook:
 * 1. Frontend crea PaymentMethod (pm_xxx) con Publishable Key
 * 2. Backend recibe pm_xxx y crea/confirma PaymentIntent con Secret Key
 * 3. Backend crea membresía inmediatamente si el pago es exitoso
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentServiceImpl implements IStripePaymentService {

    @Value("${stripe.api.key.secret}")
    private String stripeSecretKey;

    private final UserBaseRepository userRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final IMembershipService membershipService;
    private final IReceiptService receiptService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;

        // Mostrar la Secret Key parcialmente oculta para verificación
        String maskedKey = stripeSecretKey != null && stripeSecretKey.length() > 20
                ? stripeSecretKey.substring(0, 20) + "..." + stripeSecretKey.substring(stripeSecretKey.length() - 4)
                : "NO_KEY_SET";

        log.info("✅ Stripe API inicializada correctamente");
        log.info("🔑 Secret Key configurada: {}", maskedKey);
        log.info("📏 Secret Key length: {}", stripeSecretKey != null ? stripeSecretKey.length() : 0);
        log.info("🏁 Secret Key empieza con 'sk_test_': {}", stripeSecretKey != null && stripeSecretKey.startsWith("sk_test_"));
    }

    @Override
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) throws Exception {
        try {
            log.info("📝 Creando Payment Intent para usuario: {}", request.getUserId());

            // Validar usuario
            UserBase user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            // Validar tipo de membresía
            MembershipTypeName membershipTypeName = MembershipTypeName.valueOf(request.getMembershipType());
            MembershipType membershipType = membershipTypeRepository.findByName(membershipTypeName)
                    .orElseThrow(() -> new Exception("Tipo de membresía no encontrado"));

            // Convertir el monto a centavos (Stripe trabaja en la unidad más pequeña)
            long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

            // Crear metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", request.getUserId().toString());
            metadata.put("membershipType", request.getMembershipType());
            metadata.put("userEmail", user.getEmail());
            if (request.getMetadata() != null) {
                metadata.putAll(request.getMetadata());
            }

            // Crear Payment Intent (sin confirmar - el frontend lo confirmará)
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency())
                    .setDescription(request.getDescription() != null ?
                            request.getDescription() :
                            "Membresía " + request.getMembershipType() + " - FitZone")
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("✅ Payment Intent creado: {} - Status: {}", paymentIntent.getId(), paymentIntent.getStatus());

            return PaymentIntentResponse.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(paymentIntent.getStatus())
                    .build();

        } catch (StripeException e) {
            log.error("❌ Error de Stripe al crear Payment Intent: {}", e.getMessage());
            throw new Exception("Error al crear Payment Intent: " + e.getMessage());
        }
    }

    @Override
    public CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request) throws Exception {
        try {
            log.info("📝 Creando Checkout Session para usuario: {}", request.getUserId());

            // Validar usuario
            UserBase user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            // Validar tipo de membresía
            MembershipTypeName membershipTypeName = MembershipTypeName.valueOf(request.getMembershipType());
            MembershipType membershipType = membershipTypeRepository.findByName(membershipTypeName)
                    .orElseThrow(() -> new Exception("Tipo de membresía no encontrado"));

            // Convertir el precio a centavos
            long priceInCents = membershipType.getMonthlyPrice().multiply(BigDecimal.valueOf(100)).longValue();

            // Crear metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", request.getUserId().toString());
            metadata.put("membershipType", request.getMembershipType());

            // Crear Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(request.getSuccessUrl())
                    .setCancelUrl(request.getCancelUrl())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("cop")
                                                    .setUnitAmount(priceInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Membresía " + request.getMembershipType())
                                                                    .setDescription(membershipType.getDescription())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putAllMetadata(metadata)
                    .setCustomerEmail(request.getBillingInfo() != null ?
                            request.getBillingInfo().getEmail() : user.getEmail())
                    .build();

            Session session = Session.create(params);

            log.info("✅ Checkout Session creada: {}", session.getId());

            return CheckoutSessionResponse.builder()
                    .success(true)
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .message("Sesión de checkout creada exitosamente")
                    .build();

        } catch (StripeException e) {
            log.error("❌ Error de Stripe al crear Checkout Session: {}", e.getMessage());
            return CheckoutSessionResponse.builder()
                    .success(false)
                    .message("Error al crear sesión de checkout: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(String paymentId) throws Exception {
        try {
            log.info("🔍 Obteniendo estado del pago: {}", paymentId);

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);

            return PaymentStatusResponse.builder()
                    .success(true)
                    .status(paymentIntent.getStatus())
                    .message("Estado del pago obtenido exitosamente")
                    .build();

        } catch (StripeException e) {
            log.error("❌ Error de Stripe al obtener estado del pago: {}", e.getMessage());
            throw new Exception("Error al obtener estado del pago: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public GenericResponse confirmPayment(String paymentIntentId) throws Exception {
        try {
            log.info("✅ Confirmando pago: {}", paymentIntentId);

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equals(paymentIntent.getStatus())) {
                return GenericResponse.builder()
                        .success(false)
                        .message("El pago no ha sido completado exitosamente")
                        .build();
            }

            log.info("✅ Pago confirmado exitosamente: {}", paymentIntentId);

            return GenericResponse.builder()
                    .success(true)
                    .message("Pago confirmado exitosamente")
                    .build();

        } catch (StripeException e) {
            log.error("❌ Error de Stripe al confirmar pago: {}", e.getMessage());
            throw new Exception("Error al confirmar pago: " + e.getMessage());
        }
    }

    @Override
    public SavedPaymentMethodsResponse getSavedPaymentMethods(Long userId) throws Exception {
        try {
            log.info("📋 Obteniendo métodos de pago guardados para usuario: {}", userId);

            // Validar usuario
            userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            // Por ahora, retornamos una lista vacía
            List<PaymentMethodResponse> paymentMethods = new ArrayList<>();

            return SavedPaymentMethodsResponse.builder()
                    .success(true)
                    .paymentMethods(paymentMethods)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error al obtener métodos de pago: {}", e.getMessage());
            throw new Exception("Error al obtener métodos de pago: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public GenericResponse savePaymentMethod(Long userId, SavePaymentMethodRequest request) throws Exception {
        try {
            log.info("💾 Guardando método de pago para usuario: {}", userId);

            // Validar usuario
            userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            log.info("✅ Método de pago guardado exitosamente para usuario: {}", userId);

            return GenericResponse.builder()
                    .success(true)
                    .message("Método de pago guardado exitosamente")
                    .build();

        } catch (Exception e) {
            log.error("❌ Error al guardar método de pago: {}", e.getMessage());
            throw new Exception("Error al guardar método de pago: " + e.getMessage());
        }
    }

    @Override
    public GenericResponse deletePaymentMethod(Long userId, String paymentMethodId) throws Exception {
        try {
            log.info("🗑️ Eliminando método de pago {} para usuario: {}", paymentMethodId, userId);

            // Validar usuario
            userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));

            // Eliminar el payment method de Stripe
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            paymentMethod.detach();

            log.info("✅ Método de pago eliminado exitosamente");

            return GenericResponse.builder()
                    .success(true)
                    .message("Método de pago eliminado exitosamente")
                    .build();

        } catch (StripeException e) {
            log.error("❌ Error de Stripe al eliminar método de pago: {}", e.getMessage());
            throw new Exception("Error al eliminar método de pago: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public GenericResponse activateMembershipAfterPayment(String paymentIntentId, Long userId, String membershipType) throws Exception {
        try {
            log.info("🔄 Activando membresía SIN webhook - PaymentIntent: {}, Usuario: {}, Tipo: {}",
                    paymentIntentId, userId, membershipType);

            // 1. Validar que el pago existe y fue exitoso
            PaymentIntent paymentIntent;
            try {
                paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            } catch (StripeException e) {
                log.error("❌ Error al recuperar PaymentIntent de Stripe: {}", e.getMessage());
                return GenericResponse.builder()
                        .success(false)
                        .message("No se pudo verificar el pago con Stripe: " + e.getMessage())
                        .build();
            }

            // 2. Verificar que el pago fue exitoso
            if (!"succeeded".equals(paymentIntent.getStatus())) {
                log.warn("⚠️ El pago no fue exitoso. Estado: {}", paymentIntent.getStatus());
                return GenericResponse.builder()
                        .success(false)
                        .message("El pago no fue completado exitosamente. Estado: " + paymentIntent.getStatus())
                        .build();
            }

            // 3. Validar que el usuario existe
            UserBase user = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + userId));

            // 4. Validar que el usuario tiene ubicación principal
            if (user.getMainLocation() == null) {
                log.error("❌ Usuario {} no tiene ubicación principal asignada", userId);
                return GenericResponse.builder()
                        .success(false)
                        .message("El usuario no tiene una ubicación principal asignada. Por favor, selecciona una sede antes de continuar.")
                        .build();
            }

            // 5. Validar tipo de membresía
            MembershipTypeName membershipTypeName;
            try {
                membershipTypeName = MembershipTypeName.valueOf(membershipType);
            } catch (IllegalArgumentException e) {
                log.error("❌ Tipo de membresía inválido: {}", membershipType);
                return GenericResponse.builder()
                        .success(false)
                        .message("Tipo de membresía inválido: " + membershipType)
                        .build();
            }

            MembershipType membershipTypeEntity = membershipTypeRepository.findByName(membershipTypeName)
                    .orElseThrow(() -> new Exception("Tipo de membresía no encontrado: " + membershipType));

            // 6. Crear la membresía
            try {
                CreateMembershipRequest membershipRequest = new CreateMembershipRequest(
                        userId,
                        membershipTypeEntity.getIdMembershipType(),
                        user.getMainLocation().getIdLocation(),
                        paymentIntentId
                );

                MembershipResponse membership = membershipService.createMembership(membershipRequest);

                log.info("✅ Membresía activada exitosamente - Usuario: {}, Membership ID: {}, Transaction: {}",
                        userId, membership.id(), paymentIntentId);

                // Recargar el usuario desde la base de datos para obtener la relación actualizada
                UserBase updatedUser = userRepository.findById(userId)
                        .orElseThrow(() -> new Exception("Usuario no encontrado después de crear membresía"));

                log.info("✅ Usuario recargado con membresía ID: {}",
                        updatedUser.getMembership() != null ? updatedUser.getMembership().getIdMembership() : "null");

                // Crear el Map antes de usarlo en el builder
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("membershipId", membership.id());
                responseData.put("transactionId", paymentIntentId);
                responseData.put("membershipType", membershipType);
                responseData.put("startDate", membership.startDate());
                responseData.put("endDate", membership.endDate());

                return GenericResponse.builder()
                        .success(true)
                        .message("¡Membresía activada exitosamente! Ya puedes disfrutar del gimnasio.")
                        .data(responseData)
                        .build();

            } catch (Exception e) {
                log.error("❌ Error al crear membresía: {}", e.getMessage(), e);
                return GenericResponse.builder()
                        .success(false)
                        .message("El pago fue exitoso pero hubo un error al activar la membresía: " + e.getMessage() +
                               ". Por favor, contacta con soporte con el ID de transacción: " + paymentIntentId)
                        .build();
            }

        } catch (Exception e) {
            log.error("❌ Error general al activar membresía: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .success(false)
                    .message("Error al activar membresía: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Método auxiliar para convertir PaymentMethod de Stripe a nuestro DTO.
     */
    private PaymentMethodResponse convertToPaymentMethodResponse(PaymentMethod pm) {
        return PaymentMethodResponse.builder()
                .id(pm.getId())
                .type(pm.getType())
                .last4(pm.getCard() != null ? pm.getCard().getLast4() : null)
                .brand(pm.getCard() != null ? pm.getCard().getBrand() : null)
                .expiryMonth(pm.getCard() != null ? Math.toIntExact(pm.getCard().getExpMonth()) : null)
                .expiryYear(pm.getCard() != null ? Math.toIntExact(pm.getCard().getExpYear()) : null)
                .build();
    }
}
