package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity que representa una membresía en el sistema FitZone.
 * Contiene información sobre el usuario asociado, tipo de membresía,
 * ubicación, fechas de inicio y fin, estado, precio y detalles de suspensión.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMembership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_ud", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_type_id", nullable = false)
    private MembershipType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    private BigDecimal price;

    private LocalDate suspensionStart;

    private LocalDate suspensionEnd;

    private String suspensionReason;

    @PrePersist
    protected void onCreated(){
        status =  MembershipStatus.ACTIVE;
    }


}
