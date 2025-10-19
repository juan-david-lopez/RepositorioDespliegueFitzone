package co.edu.uniquindio.FitZone.dto.request;

import lombok.Data;

/**
 * DTO para información de facturación.
 */
@Data
public class BillingInfoRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
}

