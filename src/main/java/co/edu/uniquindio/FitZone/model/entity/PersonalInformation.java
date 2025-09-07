package co.edu.uniquindio.FitZone.model.entity;


import co.edu.uniquindio.FitZone.model.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


/**
 * Clase que sera embebida en la entidad User.
 * Es la encargada de contener toda la información personal
 */
@Getter
@Setter
@NoArgsConstructor
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
    private String emergencyContactPhone;

}
