package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Reservation para representar reservas de clases, entrenamientos o espacios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario principal que hace la reserva (users_base.id_user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserBase user;

    // Tipo de reserva: GROUP_CLASS, PERSONAL_TRAINING, SPECIALIZED_SPACE
    @Column(name = "reservation_type", nullable = false)
    private String reservationType;

    // Referencia opcional a la entidad objetivo (clase/id espacio/instructor)
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== NUEVOS CAMPOS PARA STRIPE ==========

    // Stripe payment intent id (para clases grupales no-ELITE)
    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    // Indica si esta reserva requirió pago
    @Column(name = "requires_payment", nullable = false)
    private Boolean requiresPayment;

    // Monto pagado (en caso de que aplique)
    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    // Indica si la reserva es grupal (múltiples participantes)
    @Column(name = "is_group", nullable = false)
    private Boolean isGroup;

    // Cupo máximo para clases grupales
    @Column(name = "max_capacity")
    private Integer maxCapacity;

    // Lista de participantes (para clases grupales)
    @ElementCollection
    @CollectionTable(
        name = "reservation_participants",
        joinColumns = @JoinColumn(name = "reservation_id")
    )
    @Column(name = "user_id")
    @Builder.Default  // ✅ FIX: Añadido para evitar warning de Lombok
    private List<Long> participantUserIds = new ArrayList<>();

    // Instructor asignado (para entrenamientos personales)
    @Column(name = "instructor_id")
    private Long instructorId;

    // Nombre/descripción de la clase o actividad
    @Column(name = "class_name")
    private String className;

    // Ubicación de la clase
    @Column(name = "location_id")
    private Long locationId;

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (requiresPayment == null) requiresPayment = false;
        if (isGroup == null) isGroup = false;
        if (participantUserIds == null) participantUserIds = new ArrayList<>();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Añade un participante a la reserva grupal
     */
    public void addParticipant(Long userId) {
        if (participantUserIds == null) {
            participantUserIds = new ArrayList<>();
        }
        if (!participantUserIds.contains(userId)) {
            participantUserIds.add(userId);
        }
    }

    /**
     * Verifica si hay cupo disponible
     */
    public boolean hasAvailableCapacity() {
        if (!isGroup || maxCapacity == null) {
            return true;
        }
        return participantUserIds.size() < maxCapacity;
    }
}
