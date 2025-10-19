package co.edu.uniquindio.FitZone;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Prueba de integración simple para enviar un correo con SendGrid sin levantar el contexto de Spring.
 * La prueba se salta automáticamente si no encuentra la API key tras revisar env/system properties y application-prod.properties.
 */
class FitZoneApplicationTests {

    private static String resolveFromProperties(String fileName, String key) {
        try (InputStream is = FitZoneApplicationTests.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) return null;
            Properties p = new Properties();
            p.load(is);
            return p.getProperty(key);
        } catch (IOException e) {
            return null;
        }
    }

    private static String resolveApiKey() {
        return Optional.ofNullable(System.getenv("SENDGRID_API_KEY"))
                .orElseGet(() -> Optional.ofNullable(System.getProperty("sendgrid.api.key"))
                        .orElseGet(() -> Optional.ofNullable(resolveFromProperties("application-prod.properties", "sendgrid.api.key"))
                                .orElseGet(() -> resolveFromProperties("application.properties", "sendgrid.api.key"))));
    }

    private static String resolveFromEmail() {
        return Optional.ofNullable(System.getenv("SENDGRID_FROM_EMAIL"))
                .orElseGet(() -> Optional.ofNullable(System.getProperty("sendgrid.from.email"))
                        .orElseGet(() -> Optional.ofNullable(resolveFromProperties("application-prod.properties", "sendgrid.from.email"))
                                .orElseGet(() -> Optional.ofNullable(resolveFromProperties("application.properties", "sendgrid.from.email"))
                                        .orElse("murdersinc23@gmail.com"))));
    }

    @Test
    void sendgrid_can_send_email_example() throws IOException {
        String apiKey = resolveApiKey();

        // Si no hay API key, saltar la prueba en lugar de fallar
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(),
                "SENDGRID_API_KEY no configurada; se omite la prueba");

        // Configurar remitente y destinatario (puedes cambiarlos según tu cuenta SendGrid)
        String fromAddr = resolveFromEmail();
        String toAddr = Optional.ofNullable(System.getenv("SENDGRID_TO_EMAIL")).orElse(fromAddr);

        Email from = new Email(fromAddr);
        String subject = "Prueba SendGrid desde tests";
        Email to = new Email(toAddr);
        Content content = new Content("text/plain", "Hola! Esta es una prueba automática desde JUnit.");
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        System.out.println("Headers: " + response.getHeaders());

        // SendGrid retorna 202 Accepted en envíos correctos
        if (response.getStatusCode() >= 400) {
            throw new AssertionError("Fallo al enviar email. status=" + response.getStatusCode() +
                    ", body=" + response.getBody());
        }
    }
}
