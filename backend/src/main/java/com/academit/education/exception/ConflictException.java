package com.academit.education.exception;

/**
 * Conflicto con el estado actual del recurso (409): por ejemplo, mesa
 * llena o inscripcion duplicada.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
