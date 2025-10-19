package co.edu.uniquindio.FitZone.dto.response;

import java.util.List;

public record BenefitsResponse(
    List<Benefit> benefits,
    List<Facility> accessibleFacilities
) {
    public static BenefitsResponse createBasicBenefits() {
        // Retornar beneficios básicos y accesos básicos
        return new BenefitsResponse(List.of(
            new Benefit("BASIC1", "Acceso a gimnasio", true)
        ),
        List.of(
            new Facility("GYM", "Gimnasio principal")
        ));
    }

    public static BenefitsResponse createPremiumBenefits() {
        // Retornar beneficios premium y accesos premium
        return new BenefitsResponse(List.of(
            new Benefit("PREM1", "Acceso a gimnasio", true),
            new Benefit("PREM2", "Clases grupales", true)
        ),
        List.of(
            new Facility("GYM", "Gimnasio principal"),
            new Facility("POOL", "Piscina")
        ));
    }

    public static BenefitsResponse createEliteBenefits() {
        // Retornar beneficios elite y accesos elite
        return new BenefitsResponse(List.of(
            new Benefit("ELITE1", "Acceso a gimnasio", true),
            new Benefit("ELITE2", "Clases grupales", true),
            new Benefit("ELITE3", "Spa", true)
        ),
        List.of(
            new Facility("GYM", "Gimnasio principal"),
            new Facility("POOL", "Piscina"),
            new Facility("SPA", "Zona de spa")
        ));
    }

    public record Benefit(String code, String description, boolean isActive) {}
    public record Facility(String code, String description) {}
}

