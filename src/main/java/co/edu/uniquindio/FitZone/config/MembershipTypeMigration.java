package co.edu.uniquindio.FitZone.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migración para convertir registros elite existentes a ELITE
 * Se ejecuta antes del inicializador principal para limpiar datos inconsistentes
 */
@Component
@Order(1) // Se ejecuta antes que MembershipTypeInitializer
public class MembershipTypeMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MembershipTypeMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public MembershipTypeMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("🔄 Iniciando migración de tipos de membresía...");

        try {
            // Verificar si existen registros con elite en la base de datos
            String checkEliteQuery = "SELECT COUNT(*) FROM membership_types_base WHERE name = 'elite'";
            Integer eliteCount = jdbcTemplate.queryForObject(checkEliteQuery, Integer.class);

            if (eliteCount != null && eliteCount > 0) {
                logger.info("📋 Encontrados {} registros con tipo elite. Migrando a ELITE...", eliteCount);

                // Migrar elite a ELITE
                String updateQuery = "UPDATE membership_types_base SET name = 'ELITE' WHERE name = 'elite'";
                int updatedRows = jdbcTemplate.update(updateQuery);

                logger.info("✅ Migración completada: {} registros elite convertidos a ELITE", updatedRows);

                // También actualizar las membresías que referencian estos tipos
                String updateMembershipsQuery = """
                    UPDATE memberships_base m
                    SET membership_type_id = (
                        SELECT mt.id_membership_type
                        FROM membership_types_base mt
                        WHERE mt.name = 'ELITE'
                    )
                    WHERE m.membership_type_id IN (
                        SELECT mt2.id_membership_type
                        FROM membership_types_base mt2
                        WHERE mt2.name = 'elite'
                    )
                """;

                try {
                    int updatedMemberships = jdbcTemplate.update(updateMembershipsQuery);
                    logger.info("✅ Membresías actualizadas: {} registros ahora apuntan a ELITE", updatedMemberships);
                } catch (Exception e) {
                    logger.warn("⚠️ No se pudieron actualizar las membresías (posiblemente no existen): {}", e.getMessage());
                }

            } else {
                logger.info("✅ No se encontraron registros elite para migrar");
            }

            logger.info("🎉 Migración de tipos de membresía completada exitosamente");

        } catch (Exception e) {
            logger.error("❌ Error durante la migración de tipos de membresía: {}", e.getMessage(), e);
            // No lanzamos la excepción para permitir que la aplicación continúe
        }
    }
}
