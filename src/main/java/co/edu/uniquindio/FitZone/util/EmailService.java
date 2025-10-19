package co.edu.uniquindio.FitZone.util;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Servicio para enviar correos electrónicos utilizando SendGrid.
 * Este servicio permite enviar correos electrónicos simples o correos electrónicos
 * basados en plantillas de Thymeleaf.
 */
@Service
public class EmailService {

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email:}")
    private String fromEmail;

    private final TemplateEngine templateEngine;
    private final org.springframework.core.env.Environment environment;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    public EmailService(TemplateEngine templateEngine, org.springframework.core.env.Environment environment) {
        this.templateEngine = templateEngine;
        this.environment = environment;
    }

    private String resolveFromClasspath(String fileName, String key) {
        try (InputStream is = EmailService.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) return null;
            Properties p = new Properties();
            p.load(is);
            String value = p.getProperty(key);
            return (value != null && !value.isBlank()) ? value : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Envia un correo electrónico utilizando SendGrid.
     * @param toEmail destinatario del correo electrónico
     * @param subject asunto del correo electrónico
     * @param body cuerpo del correo electrónico en formato HTML
     * @throws RuntimeException si ocurre un error al enviar el correo electrónico
     */
    public void sendEmail(String toEmail, String subject, String body) {

        // Resolver API key con retrocompatibilidad: @Value -> Environment -> System env -> classpath props
        String apiKey = Optional.ofNullable(sendGridApiKey)
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(environment.getProperty("sendgrid.api.key"))
                        .filter(s -> !s.isBlank())
                        .orElseGet(() -> Optional.ofNullable(System.getenv("SENDGRID_API_KEY"))
                                .filter(s -> !s.isBlank())
                                .orElseGet(() -> Optional.ofNullable(resolveFromClasspath("application-prod.properties", "sendgrid.api.key"))
                                        .orElseGet(() -> resolveFromClasspath("application.properties", "sendgrid.api.key")))));

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("SendGrid API key no configurada (propiedad 'sendgrid.api.key').");
        }

        // Resolver remitente: @Value -> Environment -> System env -> classpath props -> default
        String fromAddr = Optional.ofNullable(fromEmail)
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(environment.getProperty("sendgrid.from.email"))
                        .filter(s -> !s.isBlank())
                        .orElseGet(() -> Optional.ofNullable(System.getenv("SENDGRID_FROM_EMAIL"))
                                .filter(s -> !s.isBlank())
                                .orElseGet(() -> Optional.ofNullable(resolveFromClasspath("application-prod.properties", "sendgrid.from.email"))
                                        .orElseGet(() -> Optional.ofNullable(resolveFromClasspath("application.properties", "sendgrid.from.email"))
                                                .orElse("murdersinc23@gmail.com")))));

        Email from = new Email(fromAddr);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from,subject,to,content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try{
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            int status = response.getStatusCode();
            String respBody = response.getBody();
            log.info("SendGrid response status={}, body={} ", status, respBody);
            if (status >= 400) {
                throw new RuntimeException("Fallo al enviar email via SendGrid. status=" + status + ", body=" + respBody);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error enviando email via SendGrid", e);
        }

    }

    /**
     * Envia un correo electrónico utilizando una plantilla de Thymeleaf.
     * Este método procesa una plantilla de Thymeleaf con el contexto proporcionado
     * y envía el correo electrónico resultante.
     * @param toEmail destinatario del correo electrónico
     * @param subject asunto del correo electrónico
     * @param templateName nombre de la plantilla de Thymeleaf a utilizar
     * @param context contexto que se utilizará para procesar la plantilla
     * @throws IOException si ocurre un error al procesar la plantilla
     */
    public void sendTemplatedEmail(String toEmail, String subject, String templateName, Context context) throws IOException {
        // Procesa la plantilla con el contexto para generar el HTML
        String htmlBody = templateEngine.process(templateName, context);
        sendEmail(toEmail, subject, htmlBody);
    }

}
