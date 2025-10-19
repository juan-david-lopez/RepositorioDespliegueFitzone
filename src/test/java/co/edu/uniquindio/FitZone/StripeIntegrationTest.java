package co.edu.uniquindio.FitZone;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Prueba de integración ligera para Stripe que no levanta el contexto de Spring.
 *
 * Comportamiento:
 * - Resuelve la clave secreta de Stripe en este orden: variable de entorno STRIPE_SECRET_KEY →
 *   propiedad del sistema (stripe.api.key.secret) → application-prod.properties → application.properties.
 * - Si la clave no existe o NO es de prueba (no empieza por "sk_test"), la prueba se OMITE para evitar
 *   operaciones en modo live.
 * - Crea un PaymentIntent mínimo sin confirmarlo y valida que se haya creado correctamente.
 */
class StripeIntegrationTest {

    private static String resolveFromProperties(String fileName, String key) {
        try (InputStream is = StripeIntegrationTest.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) return null;
            Properties p = new Properties();
            p.load(is);
            String v = p.getProperty(key);
            return (v != null && !v.isBlank()) ? v : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static String resolveStripeSecretKey() {
        return Optional.ofNullable(System.getenv("STRIPE_SECRET_KEY"))
                .orElseGet(() -> Optional.ofNullable(System.getProperty("stripe.api.key.secret"))
                        .orElseGet(() -> Optional.ofNullable(resolveFromProperties("application-prod.properties", "stripe.api.key.secret"))
                                .orElseGet(() -> resolveFromProperties("application.properties", "stripe.api.key.secret"))));
    }

    @Test
    void stripe_can_create_payment_intent_without_confirming() throws StripeException {
        String secretKey = resolveStripeSecretKey();

        // Requerimos una clave de PRUEBAS para evitar efectos en producción
        Assumptions.assumeTrue(secretKey != null && !secretKey.isBlank(),
                "STRIPE_SECRET_KEY no configurada; se omite la prueba");
        Assumptions.assumeTrue(secretKey.startsWith("sk_test"),
                "La clave Stripe no es de prueba (sk_test_*); se omite la prueba para evitar operaciones live");

        Stripe.apiKey = secretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(100L) // $1.00 en centavos
                .setCurrency("usd")
                .setDescription("Stripe PI test desde JUnit (no confirmado)")
                // Usamos Automatic Payment Methods para minimizar configuración
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent pi = PaymentIntent.create(params);
        System.out.println("PI id: " + pi.getId());
        System.out.println("PI status: " + pi.getStatus());
        System.out.println("PI amount: " + pi.getAmount());

        if (pi == null || pi.getId() == null) {
            throw new AssertionError("PaymentIntent no fue creado correctamente (objeto nulo o sin id)");
        }
        if (!"requires_payment_method".equals(pi.getStatus())) {
            throw new AssertionError("Estado inesperado del PaymentIntent: " + pi.getStatus());
        }
        if (pi.getAmount() == null || pi.getAmount() != 100L) {
            throw new AssertionError("Monto inesperado del PaymentIntent: " + pi.getAmount());
        }
    }
}
