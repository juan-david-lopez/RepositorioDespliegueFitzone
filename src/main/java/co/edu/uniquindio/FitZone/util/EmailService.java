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

/**
 * Servicio para enviar correos electrónicos utilizando SendGrid.
 * Este servicio permite enviar correos electrónicos simples o correos electrónicos
 * basados en plantillas de Thymeleaf.
 */
@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    private final TemplateEngine templateEngine;

    public EmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Envia un correo electrónico utilizando SendGrid.
     * @param toEmail destinatario del correo electrónico
     * @param subject asunto del correo electrónico
     * @param body cuerpo del correo electrónico en formato HTML
     * @throws RuntimeException si ocurre un error al enviar el correo electrónico
     */
    public void sendEmail(String toEmail, String subject, String body) {

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from,subject,to,content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try{
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
