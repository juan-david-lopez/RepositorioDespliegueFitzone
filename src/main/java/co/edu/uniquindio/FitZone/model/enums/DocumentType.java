package co.edu.uniquindio.FitZone.model.enums;

/**
 * ENUM - Representa los tipos de documentos de identificación utilizados en Colombia.|
 */
public enum DocumentType {

    CC("Cédula de Ciudadanía"),
    CE("Cédula de Extranjería"),
    TI("Tarjeta de Identidad"),
    PP("Pasaporte");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
