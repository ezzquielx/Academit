package com.academit.education.exception;

import com.academit.education.dto.common.ApiErrorDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Error de validación en los datos enviados")
                .errors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .errors(List.of(ex.getMessage()))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorDTO> handleBusinessRuleException(BusinessRuleException ex) {
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .errors(List.of(ex.getMessage()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorDTO> handleBadCredentialsException(BadCredentialsException ex) {
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Credenciales inválidas")
                .errors(List.of("Nombre de usuario o contraseña incorrectos"))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorDTO> handleUnauthorizedException(UnauthorizedException ex) {
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .errors(List.of(ex.getMessage()))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleAccessDeniedException(AccessDeniedException ex) {
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("Acceso denegado: no posee los permisos requeridos")
                .errors(List.of("Permisos insuficientes para realizar esta operación"))
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String msg = "Violación de restricción de integridad de datos";
        if (ex.getMessage() != null && ex.getMessage().contains("duplicate key")) {
            msg = "El registro ya existe o viola una restricción de unicidad";
        }
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(msg)
                .errors(List.of(ex.getMostSpecificCause().getMessage()))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGlobalException(Exception ex) {
        ApiErrorDTO apiError = ApiErrorDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error interno del servidor")
                .errors(List.of(ex.getMessage() != null ? ex.getMessage() : "Error inesperado"))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}

