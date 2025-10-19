package co.edu.uniquindio.FitZone.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Entidad que representa una franquicia en el sistema.
 * Basada en la tabla franchises_base de PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "franchises_base")
public class Franchise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_franchise")
    private Long idFranchise;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Para compatibilidad con el código existente
    @Transient
    private Set<Timeslot> timeslots;

}
