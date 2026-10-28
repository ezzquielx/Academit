package com.academit.education.exception;

import com.academit.education.dto.common.FieldErrorDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, List<FieldErrorDTO> errors) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("message", detail);
        problemDetail.setProperty("errors", errors != null ? errors : List.of());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldErrorDTO> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(new FieldErrorDTO(error.getField(), error.getDefaultMessage()));
        }
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                "Error de validación en los datos enviados",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException ex) {
        List<FieldErrorDTO> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(new FieldErrorDTO(violation.getPropertyPath().toString(), violation.getMessage()));
        }
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                "Error de validación de restricciones",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    /** Errores de parseo o de tipo en la solicitud: siempre 400 (RFC 7807). */
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            ServletRequestBindingException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                "La solicitud contiene datos inválidos o mal formateados",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflictException(ConflictException ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.CONFLICT,
                "Conflicto",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleException(BusinessRuleException ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Regla de negocio violada",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Credenciales inválidas",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorizedException(UnauthorizedException ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Acceso denegado: no posee los permisos requeridos",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String detalle = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        String msg = "Violación de restricción de integridad de datos";

        // Restriccion unica violada (RN05, RN08): es un conflicto, no un error de formato.
        if (detalle.contains("duplicate key") || detalle.contains("violates unique constraint")) {
            ProblemDetail pd = buildProblemDetail(
                    HttpStatus.CONFLICT,
                    "Conflicto",
                    "El registro ya existe o viola una restricción de unicidad",
                    List.of()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
        }

        ProblemDetail pd = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Violación de integridad",
                msg,
                List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor";
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                msg,
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
