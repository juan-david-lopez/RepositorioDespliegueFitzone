package co.edu.uniquindio.FitZone.controller;

import co.edu.uniquindio.FitZone.model.entity.base.UserBase;
import co.edu.uniquindio.FitZone.model.enums.UserRole;
import co.edu.uniquindio.FitZone.repository.UserBaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar instructores (usuarios con rol INSTRUCTOR).
 */
@RestController
@RequestMapping("/instructors")
public class InstructorController {

    private static final Logger logger = LoggerFactory.getLogger(InstructorController.class);

    private final UserBaseRepository userRepository;

    public InstructorController(UserBaseRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtiene todos los instructores activos.
     */
    @GetMapping
    public ResponseEntity<?> getAllInstructors() {
        try {
            logger.info("📋 GET /api/instructors - Obteniendo todos los instructores activos");

            List<UserBase> instructors = userRepository.findByRoleAndIsActiveTrue(UserRole.INSTRUCTOR);

            // Mapear a un DTO simplificado para el frontend
            List<Map<String, Object>> instructorList = instructors.stream()
                    .map(this::mapToInstructorDTO)
                    .collect(Collectors.toList());

            logger.info("✅ Encontrados {} instructores activos", instructorList.size());

            return ResponseEntity.ok(instructorList);

        } catch (Exception e) {
            logger.error("❌ Error al obtener instructores: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener instructores");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene un instructor por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getInstructorById(@PathVariable Long id) {
        try {
            logger.info("📋 GET /api/instructors/{} - Obteniendo instructor", id);

            UserBase instructor = userRepository.findById(id)
                    .orElseThrow(() -> new Exception("Instructor no encontrado con ID: " + id));

            if (instructor.getRole() != UserRole.INSTRUCTOR) {
                throw new Exception("El usuario con ID " + id + " no es un instructor");
            }

            if (!instructor.isActive()) {
                throw new Exception("El instructor no está activo");
            }

            Map<String, Object> instructorDTO = mapToInstructorDTO(instructor);

            return ResponseEntity.ok(instructorDTO);

        } catch (Exception e) {
            logger.error("❌ Error al obtener instructor: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Instructor no encontrado");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Mapea un UserBase a un DTO simplificado para instructores.
     */
    private Map<String, Object> mapToInstructorDTO(UserBase user) {
        Map<String, Object> dto = new HashMap<>();

        dto.put("id", user.getIdUser());
        dto.put("email", user.getEmail());

        // Información personal
        if (user.getPersonalInformation() != null) {
            String fullName = user.getPersonalInformation().getFirstName() + " " +
                    (user.getPersonalInformation().getLastName() != null ?
                            user.getPersonalInformation().getLastName() : "");
            dto.put("name", fullName.trim());
            dto.put("phoneNumber", user.getPersonalInformation().getPhoneNumber());
            dto.put("photoUrl", null); // Por ahora no tenemos campo photoUrl
        } else {
            dto.put("name", "Instructor " + user.getIdUser());
            dto.put("phoneNumber", null);
            dto.put("photoUrl", null);
        }

        // Por ahora no tenemos campos específicos de instructor
        // En el futuro se pueden añadir: specialization, bio, etc.
        dto.put("specialization", "Entrenamiento General");
        dto.put("yearsOfExperience", null);
        dto.put("isActive", user.isActive());

        return dto;
    }
}
