package co.edu.uniquindio.FitZone.service.impl;

import co.edu.uniquindio.FitZone.dto.request.PaymentIntentRequest;
import co.edu.uniquindio.FitZone.dto.response.PaymentIntentResponse;
import co.edu.uniquindio.FitZone.dto.response.ReceiptResponse;
import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.service.interfaces.IReceiptService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipPaymentService.class);

    private final StripePaymentService stripePaymentService;
    private final IReceiptService receiptService;
    private final MembershipRepository membershipRepository;

    /**
     * Procesa el pago completo de una membresía incluyendo:
     * 1. Creación del PaymentIntent en Stripe
     * 2. Confirmación del pago
     * 3. Activación de la membresía
     * 4. Generación del recibo
     */
    public PaymentIntentResponse processPayment(PaymentIntentRequest request) throws StripeException {
        logger.info("Processing complete payment for membership: {}", request.getMembershipId());

        // Validar que la membresía existe y está en estado correcto
        Membership membership = membershipRepository.findById(request.getMembershipId())
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        if (membership.getStatus() == MembershipStatus.ACTIVE) {
            throw new RuntimeException("La membresía ya está activa");
        }

        // Crear PaymentIntent en Stripe
        PaymentIntentResponse paymentResponse = stripePaymentService.createPaymentIntent(request);

        logger.info("PaymentIntent created successfully: {}", paymentResponse.getPaymentIntentId());

        return paymentResponse;
    }

    /**
     * Completa el proceso después de que el pago fue exitoso
     */
    public void completeMembershipActivation(String paymentIntentId) throws StripeException {
        logger.info("Completing membership activation for PaymentIntent: {}", paymentIntentId);

        // Confirmar el estado del pago en Stripe
        PaymentIntentResponse paymentResponse = stripePaymentService.confirmPayment(paymentIntentId);

        if (!"succeeded".equals(paymentResponse.getStatus())) {
            throw new RuntimeException("El pago no fue exitoso: " + paymentResponse.getStatus());
        }

        // Activar la membresía
        activateMembership(paymentResponse.getMembershipId());

        // Generar recibo
        generateReceiptForPayment(paymentResponse.getMembershipId(), paymentIntentId);

        logger.info("Membership activation completed successfully");
    }

    private void activateMembership(Long membershipId) {
        logger.info("Activating membership: {}", membershipId);

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setStartDate(LocalDate.now());

        // Calcular fecha de finalización usando duración por defecto de 1 mes
        LocalDate endDate = calculateEndDate(membership.getStartDate(), 1);
        membership.setEndDate(endDate);

        membershipRepository.save(membership);
        logger.info("Membership {} activated from {} to {}", membershipId, membership.getStartDate(), membership.getEndDate());
    }

    private LocalDate calculateEndDate(LocalDate startDate, Integer durationMonths) {
        if (durationMonths == null || durationMonths <= 0) {
            durationMonths = 1; // Por defecto 1 mes
        }
        return startDate.plusMonths(durationMonths);
    }

    private void generateReceiptForPayment(Long membershipId, String paymentIntentId) {
        try {
            ReceiptResponse receipt = receiptService.generateReceipt(membershipId, paymentIntentId);
            logger.info("Receipt generated successfully: {}", receipt.receiptNumber());
        } catch (Exception e) {
            logger.error("Error generating receipt for membership {}: {}", membershipId, e.getMessage());
            // No lanzamos excepción aquí para no afectar el proceso principal
        }
    }

    /**
     * Cancela una membresía y procesa el reembolso si es necesario
     */
    public void cancelMembershipWithRefund(Long membershipId, String paymentIntentId, String reason) throws StripeException {
        logger.info("Canceling membership {} with refund for PaymentIntent: {}", membershipId, paymentIntentId);

        // Cancelar la membresía
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        membership.setStatus(MembershipStatus.CANCELLED);
        membershipRepository.save(membership);

        // Procesar reembolso en Stripe
        if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
            stripePaymentService.refundPayment(paymentIntentId, reason);
            logger.info("Refund processed successfully for membership: {}", membershipId);
        }
    }

    /**
     * Obtiene el estado actual del pago de una membresía
     */
    public PaymentIntentResponse getPaymentStatus(String paymentIntentId) throws StripeException {
        return stripePaymentService.getPaymentIntentStatus(paymentIntentId);
    }
}
