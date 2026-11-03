package com.academit.education.dto.asistencia;

import lombok.Getter;
import lombok.Setter;

/**
 * Response genérico SuccessResponseDTO del contrato (lo usa el registro de
 * asistencias en lote).
 */
@Getter
@Setter
public class SuccessResponseDTO {

    private boolean success;

    private String message;

    public static SuccessResponseDTO ok(String message) {
        SuccessResponseDTO r = new SuccessResponseDTO();
        r.setSuccess(true);
        r.setMessage(message);
        return r;
    }
}
