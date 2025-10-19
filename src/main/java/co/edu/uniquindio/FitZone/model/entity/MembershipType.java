package co.edu.uniquindio.FitZone.model.entity;


import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad que representa un tipo de membresía en el sistema FitZone.
 * Basada en la tabla membership_types_base de PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "membership_types_base")
public class MembershipType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_membership_type")
    private Long idMembershipType;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private MembershipTypeName name;

    @Column(name = "description")
    private String description;

    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "access_to_all_locations", nullable = false)
    private Boolean accessToAllLocation;

    @Column(name = "group_classes_sessions_included", nullable = false)
    private Integer groupClassesSessionsIncluded;

    @Column(name = "personal_training_included", nullable = false)
    private Integer personalTrainingIncluded;

    @Column(name = "specialized_classes_included", nullable = false)
    private Boolean specializedClassesIncluded;

    // Métodos de compatibilidad para el código existente
    public void setName(MembershipTypeName name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public void setAccessToAllLocation(boolean accessToAllLocation) {
        this.accessToAllLocation = accessToAllLocation;
    }

    public void setGroupClassesSessionsIncluded(int groupClassesSessionsIncluded) {
        this.groupClassesSessionsIncluded = groupClassesSessionsIncluded;
    }

    public void setPersonalTrainingIncluded(int personalTrainingIncluded) {
        this.personalTrainingIncluded = personalTrainingIncluded;
    }

    public void setSpecializedClassesIncluded(boolean specializedClassesIncluded) {
        this.specializedClassesIncluded = specializedClassesIncluded;
    }
}
