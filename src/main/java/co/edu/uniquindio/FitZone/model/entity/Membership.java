package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa una membresía en el sistema FitZone.
 * Basada en la tabla memberships_base de PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "memberships_base")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_membership")
    private Long idMembership;

    @Column(name = "user_ud", nullable = false)
    private Long userUd;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "membership_type_id", nullable = false)
    private Long membershipTypeId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MembershipStatus status;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "suspension_start")
    private LocalDate suspensionStart;

    @Column(name = "suspension_end")
    private LocalDate suspensionEnd;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    // Métodos de compatibilidad para el código existente
    public User getUser() {
        // Este método debería ser manejado por el servicio
        return null;
    }

    public void setUser(User user) {
        if (user != null) {
            this.userId = user.getIdUser();
            this.userUd = user.getIdUser(); // Parece que hay dos campos para user
        }
    }

    public MembershipType getType() {
        // Este método debería ser manejado por el servicio
        return null;
    }

    public void setType(MembershipType type) {
        if (type != null) {
            this.membershipTypeId = type.getIdMembershipType();
        }
    }

    public Location getLocation() {
        // Este método debería ser manejado por el servicio
        return null;
    }

    public void setLocation(Location location) {
        if (location != null) {
            this.locationId = location.getIdLocation();
        }
    }
}
