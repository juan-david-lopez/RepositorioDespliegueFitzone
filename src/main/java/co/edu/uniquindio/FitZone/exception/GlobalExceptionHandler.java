package co.edu.uniquindio.FitZone.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que maneja las excepciones globales de la aplicación
 * Esta clase intercepta las excepciones lanzadas por los controladores
 * y las convierte en respuestas HTTP adecuadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Object> handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception){
        return buildResponseEntity(HttpStatus.CONFLICT, "Conflicto", exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception){
        return buildResponseEntity(HttpStatus.NOT_FOUND, "No encontrado", exception.getMessage());
    }

    @ExceptionHandler(FranchiseNotFoundException.class)
    public ResponseEntity<Object> handleFranchiseNotFoundException(FranchiseNotFoundException exception){
        return buildResponseEntity(HttpStatus.NOT_FOUND, "No encontrado", exception.getMessage());
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<Object> handleLocationNotFoundException(LocationNotFoundException exception){
        return buildResponseEntity(HttpStatus.NOT_FOUND, "No encontrado", exception.getMessage());
    }

    @ExceptionHandler(MembershipTypeNotFoundException.class)
    public ResponseEntity<Object> handleMembershipTypeNotFoundException(MembershipTypeNotFoundException exception){
        return buildResponseEntity(HttpStatus.NOT_FOUND, "No encontrado", exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedRegistrationException.class)
    public ResponseEntity<Object> handleUnauthorizedRegistrationException(UnauthorizedRegistrationException exception){
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, "Unauthorized", exception.getMessage());
    }

    // Manejo de errores de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Validación fallida", mensaje);
    }

    //Manejo genérico para cualquier excepcion no controlada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception exception){
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", exception.getMessage());
    }


    /**
     * Método que me arma el cuerpo de la respuesta
     * @param status Estado del error
     * @param error Que tipo de error se presento
     * @param message Mensaje que explica el error
     * @return
     */
    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }



}
