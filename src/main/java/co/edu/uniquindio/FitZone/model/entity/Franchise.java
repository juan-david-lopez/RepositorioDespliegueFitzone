package co.edu.uniquindio.FitZone.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * ENTITY que representa una franquicia en el sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "franchises")
public class Franchise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFranchise;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "franchise", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Timeslot> timeslots;

    @OneToMany(mappedBy = "franchise")
    private List<Location> locations;

}
