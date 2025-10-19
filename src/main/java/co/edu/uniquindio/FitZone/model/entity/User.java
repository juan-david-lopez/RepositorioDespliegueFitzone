package co.edu.uniquindio.FitZone.model.entity;

import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import co.edu.uniquindio.FitZone.model.enums.MembershipTypeName;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad User - Representa a un usuario del sistema.
 * Basada en la tabla users_base de PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users_base")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry_date")
    private LocalDateTime passwordResetTokenExpiryDate;

    // Campos de información personal directamente en la tabla
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "emergency_contact_name", nullable = false, length = 20)
    private String emergencyContactName;

    @Column(name = "medical_conditions")
    private String medicalConditions;

    // Referencias a otras entidades
    @Column(name = "main_location")
    private Long mainLocation;

    // ✅ AGREGADO: Campo membershipType para actualización directa
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type")
    private MembershipTypeName membershipType;

    // ✅ CORREGIDO: Relación con Membership usando JPA - Removidas las restricciones insertable/updatable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", referencedColumnName = "id_membership")
    private Membership membership;

    // ⚠️ YA NO ES NECESARIO: Este campo es redundante ahora que la relación JPA está corregida
    // Pero lo mantenemos por compatibilidad con código existente
    @Column(name = "membership_id", insertable = false, updatable = false)
    private Long membershipId;

    public User(Long idUser, String email, String password, Long mainLocation, UserRole role) {
        this.idUser = idUser;
        this.email = email;
        this.password = password;
        this.mainLocation = mainLocation;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos adicionales para compatibilidad
    public PersonalInformation getPersonalInformation() {
        return PersonalInformation.builder()
                .firstName(firstName)
                .lastName(lastName)
                .documentType(documentType)
                .documentNumber(documentNumber)
                .phoneNumber(phoneNumber)
                .birthDate(birthDate)
                .emergencyContactName(emergencyContactName)
                .medicalConditions(medicalConditions)
                .build();
    }

    public void setPersonalInformation(PersonalInformation personalInfo) {
        if (personalInfo != null) {
            this.firstName = personalInfo.getFirstName();
            this.lastName = personalInfo.getLastName();
            this.documentType = personalInfo.getDocumentType();
            this.documentNumber = personalInfo.getDocumentNumber();
            this.phoneNumber = personalInfo.getPhoneNumber();
            this.birthDate = personalInfo.getBirthDate();
            this.emergencyContactName = personalInfo.getEmergencyContactName();
            this.medicalConditions = personalInfo.getMedicalConditions();
        }
    }

    public Membership getMembership() {
        return membership;
    }

    public void setMembership(Membership membership) {
        this.membership = membership;
        // ✅ NOTA: No podemos obtener el tipo directamente de membership porque getType() retorna null
        // El membershipType debe ser asignado manualmente después de setMembership()
        // usando setMembershipType() con el tipo correcto
    }

    public Location getMainLocationEntity() {
        // Este método debería ser manejado por el servicio para cargar la location
        return null;
    }

    public void setMainLocation(Location location) {
        if (location != null) {
            this.mainLocation = location.getIdLocation();
        }
    }

    // Métodos adicionales que estaban faltando
    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return this.isActive != null ? this.isActive : false;
    }
}
