package co.edu.uniquindio.FitZone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO genérico para respuestas simples.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse {
    private boolean success;
    private String message;
    private String error;
    private Map<String, Object> data;
}
