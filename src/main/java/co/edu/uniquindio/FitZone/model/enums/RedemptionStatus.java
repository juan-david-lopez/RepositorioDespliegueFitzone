package co.edu.uniquindio.FitZone.model.enums;

/**
 * Enum que representa el estado de un canje de recompensa.
 */
public enum RedemptionStatus {
    PENDING,     // Pendiente de uso
    ACTIVE,      // Activo y disponible para usar
    USED,        // Ya utilizado
    EXPIRED,     // Expirado sin usar
    CANCELLED    // Cancelado
}
