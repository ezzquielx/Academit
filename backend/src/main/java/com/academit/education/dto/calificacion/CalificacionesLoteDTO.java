package com.academit.education.dto.calificacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request de carga de calificaciones en lote según el contrato
 * (CalificacionesLoteDTO) para POST /api/evaluaciones/{idEvaluacion}/calificaciones.
 * El lote es atómico: si un item es invalido, 400 con detalle por posicion
 * y no se guarda nada.
 */
@Getter
@Setter
public class CalificacionesLoteDTO {

    @NotEmpty(message = "El lote de calificaciones es obligatorio")
    @Valid
    private List<Item> calificaciones;

    @Getter
    @Setter
    public static class Item {

        @NotNull(message = "El alumno es obligatorio")
        private Integer idAlumno;

        /** 1 a 10 (RN14). */
        @NotNull(message = "La nota es obligatoria")
        @Min(value = 1, message = "La nota debe ser un entero entre 1 y 10")
        @Max(value = 10, message = "La nota debe ser un entero entre 1 y 10")
        private Integer nota;

        @Size(max = 100, message = "La observacion debe tener hasta 100 caracteres")
        private String observacion;
    }
}
