package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.PaymentIntentRequest;
import co.edu.uniquindio.FitZone.dto.response.PaymentIntentResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(StripePaymentService.class);

    @Value("${stripe.api.key.secret}")
    private String stripeSecretKey;

    private final MembershipRepository membershipRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("Stripe API initialized successfully");
    }

    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException {
        logger.info("Creating Stripe PaymentIntent for membership: {}", request.getMembershipId());

        // Obtener información de la membresía
        Membership membership = membershipRepository.findById(request.getMembershipId())
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        // Convertir el precio a centavos (Stripe maneja montos en centavos)
        long amountInCents = membership.getPrice().multiply(BigDecimal.valueOf(100)).longValue();

        // Crear metadata para el PaymentIntent
        Map<String, String> metadata = new HashMap<>();
        metadata.put("membership_id", membership.getIdMembership().toString());
        metadata.put("user_id", membership.getUser().getIdUser().toString());
        metadata.put("membership_type", membership.getType().getName().toString());
        metadata.put("location", membership.getLocation().getName());

        // Configurar parámetros del PaymentIntent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("cop") // Peso colombiano
                .setDescription("Pago de membresía FitZone - " + membership.getType().getName())
                .putAllMetadata(metadata)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        // Crear el PaymentIntent en Stripe
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        logger.info("PaymentIntent created successfully: {}", paymentIntent.getId());

        return PaymentIntentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(membership.getPrice())
                .currency("COP")
                .status(paymentIntent.getStatus())
                .membershipId(membership.getIdMembership())
                .build();
    }

    public PaymentIntentResponse confirmPayment(String paymentIntentId) throws StripeException {
        logger.info("Confirming payment for PaymentIntent: {}", paymentIntentId);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new RuntimeException("El pago no se completó exitosamente. Estado: " + paymentIntent.getStatus());
        }

        // Obtener información de la metadata
        String membershipIdStr = paymentIntent.getMetadata().get("membership_id");
        Long membershipId = Long.valueOf(membershipIdStr);

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        logger.info("Payment confirmed successfully for membership: {}", membershipId);

        return PaymentIntentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(membership.getPrice())
                .currency("COP")
                .status(paymentIntent.getStatus())
                .membershipId(membership.getIdMembership())
                .build();
    }

    public void refundPayment(String paymentIntentId, String reason) throws StripeException {
        logger.info("Processing refund for PaymentIntent: {} with reason: {}", paymentIntentId, reason);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new RuntimeException("No se puede reembolsar un pago que no fue exitoso");
        }

        // Crear el reembolso
        Map<String, Object> refundParams = new HashMap<>();
        refundParams.put("payment_intent", paymentIntentId);
        refundParams.put("reason", "requested_by_customer");

        com.stripe.model.Refund refund = com.stripe.model.Refund.create(refundParams);

        logger.info("Refund processed successfully: {}", refund.getId());
    }

    public PaymentIntentResponse getPaymentIntentStatus(String paymentIntentId) throws StripeException {
        logger.info("Retrieving status for PaymentIntent: {}", paymentIntentId);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        String membershipIdStr = paymentIntent.getMetadata().get("membership_id");
        Long membershipId = membershipIdStr != null ? Long.valueOf(membershipIdStr) : null;

        return PaymentIntentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(BigDecimal.valueOf(paymentIntent.getAmount()).divide(BigDecimal.valueOf(100))) // Convertir de centavos
                .currency(paymentIntent.getCurrency().toUpperCase())
                .status(paymentIntent.getStatus())
                .membershipId(membershipId)
                .build();
    }
}
