package co.edu.uniquindio.FitZone.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity que representa una sede de una franquicia en el sistema FitZone.
 * Contiene información sobre la sede, como su nombre, dirección, número de teléfono,
 * la franquicia a la que pertenece, los miembros asociados y su estado de actividad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "locations_base")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLocation;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "address", unique = true)
    private String address;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_id", nullable = false)
    private Franchise franchise;

    @OneToMany(mappedBy = "mainLocation")
    @Builder.Default
    private List<User> members = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreated(){
        isActive= true;
    }
}
