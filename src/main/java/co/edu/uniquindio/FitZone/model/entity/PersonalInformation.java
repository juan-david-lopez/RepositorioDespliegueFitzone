package co.edu.uniquindio.FitZone.model.entity;


import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


/**
 * Clase que sera embebida en la entidad User.
 * Es la encargada de contener toda la información personal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class PersonalInformation {

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "medical_conditions", length = 255)
    private String medicalConditions;

    @Column(name = "emergency_contact_name", nullable = false, length = 20)
    private String emergencyContactName;

    // Método de compatibilidad para el código existente
    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactName = emergencyContactPhone;
    }

    public String getEmergencyContactPhone() {
        return this.emergencyContactName;
    }
}
