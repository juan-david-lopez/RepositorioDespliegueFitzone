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

import java.util.List;

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
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest request) {
        logger.info("POST /users/register - Registro de usuario solicitado por usuario autorizado");
        logger.debug("Datos de usuario recibidos - Email: {}, Nombre: {}, Apellido: {}, Rol: {}", 
            request.email(), request.firstName(), request.lastName(), request.role());
        
        try {
            UserResponse response = userService.registerUser(request);
            logger.info("Usuario registrado exitosamente por usuario autorizado - ID: {}, Email: {}, Rol: {}", 
                response.idUser(), response.email(), response.userRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error al registrar usuario por usuario autorizado - Email: {}, Error: {}", 
                request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/public/register")
    public ResponseEntity<UserResponse> publicRegisterUser(@RequestBody UserRequest request) {
        logger.info("POST /users/public/register - Registro público de usuario solicitado");
        logger.debug("Datos de usuario recibidos - Email: {}, Nombre: {}, Apellido: {}, Documento: {}", 
            request.email(), request.firstName(), request.lastName(), request.documentNumber());
        
        try {
            UserResponse response = userService.publicRegisterUser(request);
            logger.info("Usuario registrado exitosamente de forma pública - ID: {}, Email: {}, Rol: {}", 
                response.idUser(), response.email(), response.userRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error al registrar usuario de forma pública - Email: {}, Error: {}", 
                request.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{idUser}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long idUser, @RequestBody UserUpdateRequest request) {
        logger.info("PUT /users/{} - Actualización de usuario solicitada", idUser);
        logger.debug("Datos de actualización recibidos - Nombre: {}, Apellido: {}, Email: {}, Documento: {}", 
            request.firstName(), request.lastName(), request.email(), request.documentNumber());
        
        try {
            UserResponse response = userService.updateUser(idUser, request);
            logger.info("Usuario actualizado exitosamente - ID: {}, Email: {}, Nombre: {}", 
                idUser, response.email(), response.firstName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al actualizar usuario - ID: {}, Error: {}", 
                idUser, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{idUser}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long idUser) {
        logger.info("DELETE /users/{} - Eliminación lógica de usuario solicitada", idUser);
        
        try {
            userService.deleteUser(idUser);
            logger.info("Usuario eliminado lógicamente exitosamente - ID: {}", idUser);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al eliminar usuario - ID: {}, Error: {}", 
                idUser, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{idUser}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long idUser) {
        logger.debug("GET /users/{} - Consulta de usuario por ID", idUser);
        
        try {
            UserResponse response = userService.getUserById(idUser);
            logger.debug("Usuario encontrado por ID - ID: {}, Nombre: {}, Email: {}", 
                idUser, response.firstName(), response.email());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar usuario por ID - ID: {}, Error: {}", 
                idUser, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.debug("GET /users - Consulta de todos los usuarios activos");
        
        try {
            List<UserResponse> users = userService.getAllUsers();
            logger.debug("Se encontraron {} usuarios activos", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error al consultar todos los usuarios - Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        logger.debug("GET /users/by-email - Consulta de usuario por email: {}", email);
        
        try {
            UserResponse response = userService.getUserByEmail(email);
            logger.debug("Usuario encontrado por email - Email: {}, ID: {}, Nombre: {}", 
                email, response.idUser(), response.firstName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar usuario por email - Email: {}, Error: {}", 
                email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/by-document")
    public ResponseEntity<UserResponse> getUserByDocumentNumber(@RequestParam String documentNumber) {
        logger.debug("GET /users/by-document - Consulta de usuario por número de documento: {}", documentNumber);
        
        try {
            UserResponse response = userService.getUserByDocumentNumber(documentNumber);
            logger.debug("Usuario encontrado por documento - Documento: {}, ID: {}, Nombre: {}", 
                documentNumber, response.idUser(), response.firstName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar usuario por documento - Documento: {}, Error: {}", 
                documentNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
