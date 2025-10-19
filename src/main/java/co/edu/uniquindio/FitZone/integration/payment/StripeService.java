package co.edu.uniquindio.FitZone.integration.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para manejar la integración con Stripe.
 */
@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.api.key.secret}")
    private String secretKey;

    /**
     * Crea un PaymentIntent en Stripe.
     *
     * @param amount      Monto en centavos.
     * @param currency    Moneda (por ejemplo, "usd").
     * @param description Descripción del pago.
     * @return El PaymentIntent creado.
     * @throws StripeException Si ocurre un error al interactuar con la API de Stripe.
     */
    public PaymentIntent createPaymentIntent(Long amount, String currency, String description) throws StripeException {

        Stripe.apiKey =  secretKey;

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency);
        params.put("description", description);

        PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setDescription(description)
                .build();

        return  PaymentIntent.create(createParams);
    }

    /**
     * Normaliza distintos identificadores provenientes del front para obtener el ID de PaymentIntent.
     * Acepta:
     * - ID directo de PaymentIntent: "pi_..."
     * - client_secret completo: "pi_..._secret_..." (extrae la parte pi_...)
     * Rechaza explícitamente IDs de PaymentMethod ("pm_...") con una explicación clara.
     */
    private String extractPaymentIntentId(String incoming) {
        if (incoming == null || incoming.isBlank()) {
            throw new IllegalArgumentException("El identificador de pago está vacío");
        }
        String trimmed = incoming.trim();
        if (trimmed.startsWith("pm_")) {
            throw new IllegalArgumentException("Se recibió un ID de PaymentMethod (pm_...). Debe enviarse el ID del PaymentIntent (pi_...) o el client_secret del PaymentIntent");
        }
        if (trimmed.startsWith("pi_")) {
            int idx = trimmed.indexOf("_secret_");
            if (idx > 0) {
                return trimmed.substring(0, idx);
            }
            return trimmed;
        }
        // Si llega un client_secret sin iniciar por pi_ por alguna razón
        int idx = trimmed.indexOf("_secret_");
        if (idx > 2) {
            return trimmed.substring(0, idx);
        }
        throw new IllegalArgumentException("Identificador de pago inválido. Se esperaba un PaymentIntent (pi_...) o un client_secret válido");
    }

    public PaymentIntent getPaymentIntent(String paymentIntentIdOrSecret) throws StripeException {
        Stripe.apiKey =  secretKey;
        String normalizedId = extractPaymentIntentId(paymentIntentIdOrSecret);
        return PaymentIntent.retrieve(normalizedId);
    }

    /**
     * Crea y confirma un PaymentIntent usando un PaymentMethod.
     * Este método debe ser usado desde el backend con la Secret Key.
     *
     * @param amount Monto en centavos
     * @param currency Moneda (ej: "cop", "usd")
     * @param paymentMethodId ID del PaymentMethod creado en el frontend (pm_xxx)
     * @param description Descripción del pago
     * @return PaymentIntent confirmado
     * @throws StripeException Si ocurre un error con Stripe
     */
    public PaymentIntent createAndConfirmPaymentIntent(
            Long amount,
            String currency,
            String paymentMethodId,
            String description) throws StripeException {

        Stripe.apiKey = secretKey;

        logger.info("💳 Creando y confirmando PaymentIntent - Monto: {} {}, PaymentMethod: {}",
                amount, currency, paymentMethodId);

        // Validar que sea un PaymentMethod ID
        if (paymentMethodId == null || !paymentMethodId.startsWith("pm_")) {
            throw new IllegalArgumentException(
                    "paymentMethodId debe tener formato pm_xxx, recibido: " + paymentMethodId
            );
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)  // Confirma inmediatamente
                    .setDescription(description)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            logger.info("✅ PaymentIntent creado - ID: {}, Status: {}",
                    paymentIntent.getId(), paymentIntent.getStatus());

            return paymentIntent;

        } catch (com.stripe.exception.InvalidRequestException e) {
            // Error específico cuando el PaymentMethod no existe o es de otra cuenta
            if (e.getMessage().contains("No such PaymentMethod")) {
                logger.error("❌ PaymentMethod no encontrado: {}. " +
                                "Verifica que las Stripe Keys (Publishable y Secret) sean de la misma cuenta y modo (test/live). " +
                                "PaymentMethod recibido: {}, Error: {}",
                        paymentMethodId, paymentMethodId, e.getMessage());
                throw new IllegalArgumentException(
                        "El método de pago no es válido o fue creado con una clave diferente. " +
                                "Asegúrate de que el frontend y backend usen las mismas Stripe Keys (test o live).",
                        e
                );
            }
            throw e; // Re-lanzar otros errores de InvalidRequest
        }
    }

}
