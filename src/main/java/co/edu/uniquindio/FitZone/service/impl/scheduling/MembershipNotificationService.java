package co.edu.uniquindio.FitZone.service.impl.scheduling;

import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import co.edu.uniquindio.FitZone.util.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para el envío de notificaciones de renovación de membresías.
 * Este servicio se ejecuta diariamente a las 6:00 AM y envía recordatorios
 * a los usuarios cuyas membresías están por expirar en 7 días y en 1 día.
 */
@Service
public class MembershipNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipNotificationService.class);

    private final MembershipRepository membershipRepository;
    private final EmailService emailService;

    public MembershipNotificationService(MembershipRepository membershipRepository, EmailService emailService) {
        this.membershipRepository = membershipRepository;
        this.emailService = emailService;
    }

    /**
     * Método programado para enviar recordatorios de renovación de membresías.
     * Se ejecuta todos los días a las 6:00 AM.
     * @throws IOException Si ocurre un error al enviar el correo electrónico.
     */
    @Scheduled(cron = "0 0 6 * * *") // Se ejecuta todos los días a las 6:00 AM
    public void sendRenewalReminders() throws IOException {
        logger.info("Iniciando proceso de envío de recordatorios de renovación de membresías");
        LocalDate today = LocalDate.now();
        logger.debug("Fecha de ejecución: {}", today);

        // Membresías que expiran en 7 días
        LocalDate sevenDaysFromNow = today.plusDays(7);
        logger.debug("Buscando membresías que expiran en 7 días - Fecha: {}", sevenDaysFromNow);
        List<Membership> expiringInSevenDays = membershipRepository.findByStatusAndEndDate(MembershipStatus.ACTIVE, sevenDaysFromNow);
        logger.info("Se encontraron {} membresías que expiran en 7 días", expiringInSevenDays.size());
        sendNotification(expiringInSevenDays, 7);

        // Membresías que expiran en 1 día
        LocalDate oneDayFromNow = today.plusDays(1);
        logger.debug("Buscando membresías que expiran en 1 día - Fecha: {}", oneDayFromNow);
        List<Membership> expiringInOneDay = membershipRepository.findByStatusAndEndDate(MembershipStatus.ACTIVE, oneDayFromNow);
        logger.info("Se encontraron {} membresías que expiran en 1 día", expiringInOneDay.size());
        sendNotification(expiringInOneDay, 1);

        logger.info("Proceso de envío de recordatorios de renovación completado");
    }

    /**
     * Envía notificaciones por correo electrónico a los usuarios cuyas membresías están por expirar.
     * @param memberships Lista de membresías que están por expirar.
     * @param daysRemaining Días restantes para la expiración de la membresía.
     * @throws IOException Si ocurre un error al enviar el correo electrónico.
     */
    private void sendNotification(List<Membership> memberships, long daysRemaining) throws IOException {
        if (memberships.isEmpty()) {
            logger.debug("No hay membresías para enviar notificaciones con {} días restantes", daysRemaining);
            return;
        }

        logger.debug("Iniciando envío de {} notificaciones para membresías con {} días restantes", 
            memberships.size(), daysRemaining);

        int notificationsSent = 0;
        int notificationsFailed = 0;

        for (Membership membership : memberships) {
            try {
                logger.debug("Enviando notificación de renovación para usuario: {} (ID: {}), Email: {}, Días restantes: {}", 
                    membership.getUser().getPersonalInformation().getFirstName(), 
                    membership.getUser().getIdUser(),
                    membership.getUser().getEmail(),
                    daysRemaining);

                Context context = new Context();
                context.setVariable("userName", membership.getUser().getPersonalInformation().getFirstName());
                context.setVariable("membershipType", membership.getType().getName());
                context.setVariable("expiryDate", membership.getEndDate());
                context.setVariable("daysRemaining", daysRemaining);
                context.setVariable("renewalLink", ""); // Falta remplazar con el enlace para renovar la membresía
                context.setVariable("gymPhone", "***");
                context.setVariable("gymEmail", "fitzoneuq@gmail.com");

                String subject = "Recordatorio de Renovación - FitZone";
                emailService.sendTemplatedEmail(membership.getUser().getEmail(), subject, "membership-renewal-reminder", context);
                
                notificationsSent++;
                logger.debug("Notificación enviada exitosamente para usuario: {} (Email: {})", 
                    membership.getUser().getPersonalInformation().getFirstName(), 
                    membership.getUser().getEmail());

            } catch (Exception e) {
                notificationsFailed++;
                logger.error("Error al enviar notificación para usuario: {} (Email: {}), Error: {}", 
                    membership.getUser().getPersonalInformation().getFirstName(), 
                    membership.getUser().getEmail(), 
                    e.getMessage(), e);
            }
        }

        logger.info("Resumen de notificaciones enviadas - Total: {}, Exitosas: {}, Fallidas: {}, Días restantes: {}", 
            memberships.size(), notificationsSent, notificationsFailed, daysRemaining);
    }
}
