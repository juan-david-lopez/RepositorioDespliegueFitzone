package co.edu.uniquindio.FitZone.model.entity;


import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity que representa un tipo de membresía en el sistema FitZone.
 * Contiene información sobre el nombre, descripción, precio mensual y beneficios asociados a la membresía
 * como acceso a todas las ubicaciones, sesiones de clases grupales, entrenamiento personal y clases especializadas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "membership_types")
public class MembershipType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMembershipType;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private MembershipTypeName name;

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



}
