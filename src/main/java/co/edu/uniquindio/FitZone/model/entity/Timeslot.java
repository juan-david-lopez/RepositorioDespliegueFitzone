package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Entidad que representa un intervalo de tiempo (horario) en el sistema.
 * Basada en la tabla timeslots_base de PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "timeslots_base")
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_timeslot")
    private Long idTimeslot;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek day;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "franchise_id", nullable = false)
    private Long franchiseId;

    // Para compatibilidad con el código existente
    @Transient
    private Franchise franchise;

    // Métodos de compatibilidad para el código existente
    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public void setFranchise(Franchise franchise) {
        this.franchise = franchise;
        if (franchise != null) {
            this.franchiseId = franchise.getIdFranchise();
        }
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return day == timeslot.day;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(day);
    }
}
