package com.academit.education.exception;

import com.academit.education.dto.common.FieldErrorDTO;

import java.util.List;

/**
 * Regla de negocio violada (400). Opcionalmente lleva errores por campo o
 * por posicion (por ejemplo, items invalidos de un lote atomico).
 */
public class BusinessRuleException extends RuntimeException {

    private final List<FieldErrorDTO> errors;

    public BusinessRuleException(String message) {
        super(message);
        this.errors = List.of();
    }

    public BusinessRuleException(String message, List<FieldErrorDTO> errors) {
        super(message);
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }

    public List<FieldErrorDTO> getErrors() {
        return errors;
    }
}
