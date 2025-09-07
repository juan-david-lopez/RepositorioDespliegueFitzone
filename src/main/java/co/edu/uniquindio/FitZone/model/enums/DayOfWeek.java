package co.edu.uniquindio.FitZone.model.enums;

import lombok.Getter;

/**
 * ENUM que representa los días de la semana.
 * Cada día tiene un nombre para mostrar en español.
 */
@Getter
public enum DayOfWeek {

    MONDAY("Lunes"),
    TUESDAY("Martes"),
    WEDNESDAY("Miércoles"),
    THURSDAY("Jueves"),
    FRIDAY("Viernes"),
    SATURDAY("Sábado"),
    SUNDAY("Domingo");

    private final String displayName;

    DayOfWeek(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
