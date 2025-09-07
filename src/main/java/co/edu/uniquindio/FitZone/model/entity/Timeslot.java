package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Entity que representa un intervalo de tiempo (horario) en el sistema.
 * Cada intervalo de tiempo está asociado a un día de la semana y a una franquicia específica
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "timeslots")
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTimeslot;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek day;

    private LocalTime openTime;
    private LocalTime closeTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_id", nullable = false)
    private Franchise franchise;

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
