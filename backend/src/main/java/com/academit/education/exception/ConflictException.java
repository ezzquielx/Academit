package com.academit.education.exception;

/**
 * Conflicto con el estado actual o con una regla de negocio (HTTP 409).
 * Se usa para duplicados, operaciones no permitidas sobre un recurso en un
 * estado determinado y violaciones de restricciones unicas (RN05, RN08).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
