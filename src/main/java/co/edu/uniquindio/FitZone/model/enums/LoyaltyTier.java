package co.edu.uniquindio.FitZone.model.enums;

import lombok.Getter;

/**
 * Enum que define los niveles de fidelización del sistema de loyalty.
 */
@Getter
public enum LoyaltyTier {
    BRONZE(0, "Bronce", 0, 999),
    SILVER(1, "Plata", 1000, 2999),
    GOLD(2, "Oro", 3000, 4999),
    PLATINUM(3, "Platino", 5000, 9999),
    DIAMOND(4, "Diamante", 10000, Integer.MAX_VALUE);

    private final int level;
    private final String displayName;
    private final int minPoints;
    private final int maxPoints;

    LoyaltyTier(int level, String displayName, int minPoints, int maxPoints) {
        this.level = level;
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
    }

    /**
     * Determina el tier basado en los puntos totales del usuario.
     */
    @SuppressWarnings("unused")
    public static LoyaltyTier getTierByPoints(int totalPoints) {
        for (LoyaltyTier tier : values()) {
            if (totalPoints >= tier.minPoints && totalPoints <= tier.maxPoints) {
                return tier;
            }
        }
        return BRONZE; // Default fallback
    }

    /**
     * Calcula cuántos puntos faltan para el siguiente tier.
     */
    @SuppressWarnings("unused")
    public int pointsToNextTier(int currentPoints) {
        if (this == DIAMOND) {
            return 0; // Ya está en el tier más alto
        }

        LoyaltyTier nextTier = values()[this.ordinal() + 1];
        return Math.max(0, nextTier.minPoints - currentPoints);
    }

    /**
     * Obtiene el siguiente tier.
     */
    @SuppressWarnings("unused")
    public LoyaltyTier getNextTier() {
        if (this == DIAMOND) {
            return DIAMOND;
        }
        return values()[this.ordinal() + 1];
    }
}
