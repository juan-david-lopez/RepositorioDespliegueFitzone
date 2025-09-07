package co.edu.uniquindio.FitZone.service.impl.scheduling;

import co.edu.uniquindio.FitZone.model.entity.Membership;
import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import co.edu.uniquindio.FitZone.repository.MembershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio para la reactivación automática de membresías suspendidas
 * y la expiración automática de membresías activas.
 * Esta clase utiliza una tarea programada que se ejecuta diariamente a medianoche.
 */
@Service
public class MembershipReactivationAndExpirationService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipReactivationAndExpirationService.class);

    private final MembershipRepository membershipRepository;
    
    public MembershipReactivationAndExpirationService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    /**
     * Tarea programada que se ejecuta a medianoche para manejar la reactivación
     * de membresías suspendidas y la expiración de membresías activas.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Se ejecuta a las 00:00:00 todos los días
    public void manageMembershipsStatus() {
        logger.info("Iniciando proceso de gestión automática de estados de membresías");
        LocalDate today = LocalDate.now();
        logger.debug("Fecha de ejecución: {}", today);

        // Lógica para reactivar automáticamente las membresías suspendidas
        logger.debug("Buscando membresías suspendidas que deben reactivarse");
        List<Membership> suspendedMemberships = membershipRepository.findByStatusAndSuspensionEndIsBefore(MembershipStatus.SUSPENDED, today);
        logger.info("Se encontraron {} membresías suspendidas que deben reactivarse", suspendedMemberships.size());
        
        int reactivatedMemberships = 0;
        int reactivationErrors = 0;
        
        for (Membership membership : suspendedMemberships) {
            try {
                logger.debug("Reactivando membresía suspendida - ID: {}, Usuario: {} (ID: {}), Fecha fin suspensión: {}", 
                    membership.getIdMembership(),
                    membership.getUser().getPersonalInformation().getFirstName(),
                    membership.getUser().getIdUser(),
                    membership.getSuspensionEnd());

                // Se calcula la duración de la suspensión con la fecha de fin estipulada
                long suspendedDays = ChronoUnit.DAYS.between(membership.getSuspensionStart(), membership.getSuspensionEnd());
                logger.debug("Días de suspensión calculados: {}, extendiendo fecha de finalización", suspendedDays);
                
                membership.setEndDate(membership.getEndDate().plusDays(suspendedDays));
                membership.setStatus(MembershipStatus.ACTIVE);
                membership.setSuspensionEnd(null);
                membership.setSuspensionReason(null);
                
                logger.debug("Guardando membresía reactivada en la base de datos");
                membershipRepository.save(membership);
                
                reactivatedMemberships++;
                logger.debug("Membresía reactivada exitosamente - ID: {}, Usuario: {}, Nueva fecha fin: {}", 
                    membership.getIdMembership(),
                    membership.getUser().getPersonalInformation().getFirstName(),
                    membership.getEndDate());
                    
            } catch (Exception e) {
                reactivationErrors++;
                logger.error("Error al reactivar membresía - ID: {}, Usuario: {}, Error: {}", 
                    membership.getIdMembership(),
                    membership.getUser().getPersonalInformation().getFirstName(),
                    e.getMessage(), e);
            }
        }

        logger.info("Resumen de reactivaciones - Total encontradas: {}, Exitosas: {}, Errores: {}", 
            suspendedMemberships.size(), reactivatedMemberships, reactivationErrors);

        // Lógica para cambiar el estado de las membresías que ya expiraron
        logger.debug("Buscando membresías activas que han expirado");
        List<Membership> activeAndExpiredMemberships = membershipRepository.findByStatusAndEndDateIsBefore(MembershipStatus.ACTIVE, today);
        logger.info("Se encontraron {} membresías activas que han expirado", activeAndExpiredMemberships.size());
        
        int expiredMemberships = 0;
        int expirationErrors = 0;
        
        for (Membership membership : activeAndExpiredMemberships) {
            try {
                logger.debug("Expirando membresía activa - ID: {}, Usuario: {} (ID: {}), Fecha fin: {}", 
                    membership.getIdMembership(),
                    membership.getUser().getPersonalInformation().getFirstName(),
                    membership.getUser().getIdUser(),
                    membership.getEndDate());

                membership.setStatus(MembershipStatus.EXPIRED);
                
                logger.debug("Guardando membresía expirada en la base de datos");
                membershipRepository.save(membership);
                
                expiredMemberships++;
                logger.debug("Membresía expirada exitosamente - ID: {}, Usuario: {}", 
                    membership.getIdMembership(),
                    membership.getUser().getPersonalInformation().getFirstName());
                    
            } catch (Exception e) {
                expirationErrors++;
                logger.error("Error al expirar membresía - ID: {}, Usuario: {}, Error: {}", 
                    membership.getIdMembership(),
                    membership.getUser().getPersonalInformation().getFirstName(),
                    e.getMessage(), e);
            }
        }

        logger.info("Resumen de expiraciones - Total encontradas: {}, Exitosas: {}, Errores: {}", 
            activeAndExpiredMemberships.size(), expiredMemberships, expirationErrors);

        logger.info("Proceso de gestión automática de estados de membresías completado - Reactivaciones: {}, Expiraciones: {}", 
            reactivatedMemberships, expiredMemberships);
    }
}
