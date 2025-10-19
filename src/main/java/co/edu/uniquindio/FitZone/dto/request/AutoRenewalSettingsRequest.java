package co.edu.uniquindio.FitZone.dto.request;

/**
 * DTO para configurar las opciones de auto-renovación de membresía.
 * @param enabled Indica si la auto-renovación está habilitada.
 * @param daysBeforeExpiration Días antes de la expiración para renovar.
 */
public record AutoRenewalSettingsRequest(
        boolean enabled,
        int daysBeforeExpiration
) {}

