package co.edu.uniquindio.FitZone.service.impl.scheduling;

import co.edu.uniquindio.FitZone.service.interfaces.ILoyaltyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Servicio programado para gestionar tareas automáticas del sistema de fidelización.
 * Ejecuta procesos periódicos como expiración de puntos, actualización de niveles, etc.
 */
@Service
public class LoyaltyScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyScheduledService.class);

    private final ILoyaltyService loyaltyService;

    public LoyaltyScheduledService(ILoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    /**
     * Actualiza los niveles de fidelización de todos los usuarios.
     * Se ejecuta diariamente a las 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateAllUserTiers() {
        logger.info("🔄 Iniciando actualización automática de niveles de fidelización");
        try {
            loyaltyService.updateTierForAllProfiles();
            logger.info("✅ Actualización de niveles completada exitosamente");
        } catch (Exception e) {
            logger.error("❌ Error al actualizar niveles de fidelización: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa puntos expirados (12 meses desde su obtención).
     * Se ejecuta diariamente a las 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void processExpiredPoints() {
        logger.info("🔄 Iniciando proceso de expiración de puntos");
        try {
            loyaltyService.processExpiredPoints();
            logger.info("✅ Proceso de expiración de puntos completado");
        } catch (Exception e) {
            logger.error("❌ Error al procesar puntos expirados: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa canjes de recompensas expirados.
     * Se ejecuta diariamente a las 4:00 AM.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void processExpiredRedemptions() {
        logger.info("🔄 Iniciando proceso de expiración de canjes");
        try {
            loyaltyService.processExpiredRedemptions();
            logger.info("✅ Proceso de expiración de canjes completado");
        } catch (Exception e) {
            logger.error("❌ Error al procesar canjes expirados: {}", e.getMessage(), e);
        }
    }
}

