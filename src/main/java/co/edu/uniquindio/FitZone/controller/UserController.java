package co.edu.uniquindio.FitZone.controller;


import co.edu.uniquindio.FitZone.dto.request.UserRequest;
import co.edu.uniquindio.FitZone.dto.request.UserUpdateRequest;
import co.edu.uniquindio.FitZone.dto.response.UserResponse;
import co.edu.uniquindio.FitZone.service.interfaces.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para manejar las operaciones relacionadas con los usuarios.
 * Este controlador define los endpoints para registrar, actualizar, eliminar y obtener usuarios.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST', 'INSTRUCTOR')")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest request) {
        logger.info("POST /users/register - Registro de usuario solicitado por usuario autorizado");
        logger.debug("Datos de usuario recibidos - Email: {}, Nombre: {}, Apellido: {}, Rol: {}", 
            request.email(), request.firstName(), request.lastName(), request.role());
        
        try {
            UserResponse response = userService.registerUser(request);
            logger.info("Usuario registrado exitosamente por usuario autorizado - ID: {}, Email: {}, Rol: {}", 
                response.idUser(), response.email(), response.role());
            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response));
        } catch (Exception e) {
            logger.error("Error al registrar usuario por usuario autorizado - Email: {}, Error: {}", 
                request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error registrando usuario", e.getMessage()));
        }
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> publicRegisterUser(@RequestBody UserRequest request) {
        logger.info("POST /users/public/register - Registro público de usuario solicitado");
        logger.debug("Datos de usuario recibidos - Email: {}, Nombre: {}, Apellido: {}, Documento: {}", 
            request.email(), request.firstName(), request.lastName(), request.documentNumber());
        
        try {
            UserResponse response = userService.publicRegisterUser(request);
            logger.info("Usuario registrado exitosamente de forma pública - ID: {}, Email: {}, Rol: {}", 
                response.idUser(), response.email(), response.role());
            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response));
        } catch (Exception e) {
            logger.error("Error al registrar usuario de forma pública - Email: {}, Error: {}", 
                request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error registrando usuario", e.getMessage()));
        }
    }

    @PutMapping("/{idUser}")
    public ResponseEntity<?> updateUser(@PathVariable Long idUser, @RequestBody UserUpdateRequest request) {
        logger.info("PUT /users/{} - Actualización de usuario solicitada", idUser);
        logger.debug("Datos de actualización recibidos - Nombre: {}, Apellido: {}, Email: {}, Documento: {}", 
            request.firstName(), request.lastName(), request.email(), request.documentNumber());
        
        try {
            UserResponse response = userService.updateUser(idUser, request);
            logger.info("Usuario actualizado exitosamente - ID: {}, Email: {}, Nombre: {}", 
                idUser, response.email(), response.name());
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            logger.error("Error al actualizar usuario - ID: {}, Error: {}", 
                idUser, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error actualizando usuario", e.getMessage()));
        }
    }

    @DeleteMapping("/{idUser}")
    public ResponseEntity<?> deleteUser(@PathVariable Long idUser) {
        logger.info("DELETE /users/{} - Eliminación lógica de usuario solicitada", idUser);
        
        try {
            userService.deleteUser(idUser);
            logger.info("Usuario eliminado lógicamente exitosamente - ID: {}", idUser);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario eliminado exitosamente");
            response.put("userId", idUser);
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            logger.error("Error al eliminar usuario - ID: {}, Error: {}", 
                idUser, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error eliminando usuario", e.getMessage()));
        }
    }

    @GetMapping("/{idUser}")
    public ResponseEntity<?> getUserById(@PathVariable Long idUser) {
        logger.debug("GET /users/{} - Consulta de usuario por ID", idUser);
        
        try {
            UserResponse response = userService.getUserById(idUser);
            logger.debug("Usuario encontrado por ID - ID: {}, Nombre: {}, Email: {}", 
                idUser, response.name(), response.email());

            // ✅ NUEVO: Log detallado para debugging del frontend
            logger.info("📤 [getUserById] Devolviendo usuario - ID: {}, idUser: {}, membershipType: {}, isActive: {}",
                    response.id(), response.idUser(), response.membershipType(), response.isActive());

            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            logger.error("Error al consultar usuario por ID - ID: {}, Error: {}", 
                idUser, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Usuario no encontrado", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        logger.debug("GET /users - Consulta de todos los usuarios activos");
        
        try {
            List<UserResponse> users = userService.getAllUsers();
            logger.debug("Se encontraron {} usuarios activos", users.size());
            return ResponseEntity.ok(createSuccessResponse(users));
        } catch (Exception e) {
            logger.error("Error al consultar todos los usuarios - Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error consultando usuarios", e.getMessage()));
        }
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        logger.debug("GET /users/by-email - Consulta de usuario por email: {}", email);
        
        try {
            UserResponse response = userService.getUserByEmail(email);
            logger.debug("Usuario encontrado por email - Email: {}, ID: {}, Nombre: {}", 
                email, response.idUser(), response.name());
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            logger.error("Error al consultar usuario por email - Email: {}, Error: {}", 
                email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Usuario no encontrado", e.getMessage()));
        }
    }

    @GetMapping("/by-document")
    public ResponseEntity<?> getUserByDocumentNumber(@RequestParam String documentNumber) {
        logger.debug("GET /users/by-document - Consulta de usuario por número de documento: {}", documentNumber);
        
        try {
            UserResponse response = userService.getUserByDocumentNumber(documentNumber);
            logger.debug("Usuario encontrado por documento - Documento: {}, ID: {}, Nombre: {}", 
                documentNumber, response.idUser(), response.name());
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            logger.error("Error al consultar usuario por documento - Documento: {}, Error: {}", 
                documentNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Usuario no encontrado", e.getMessage()));
        }
    }

    // ============== MÉTODOS AUXILIARES ==============

    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String error, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", System.currentTimeMillis());
        if (details != null && !details.trim().isEmpty()) {
            response.put("details", details);
        }
        return response;
    }
}
