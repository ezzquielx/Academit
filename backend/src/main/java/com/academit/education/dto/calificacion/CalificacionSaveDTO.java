package com.academit.education.dto.calificacion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de calificación individual según el contrato (CalificacionSaveDTO).
 * La evaluación se identifica por materia + nombre (descripción); si ya
 * existe calificación del alumno para esa evaluación se actualiza (upsert).
 */
@Getter
@Setter
public class CalificacionSaveDTO {

    @NotNull(message = "El alumno es obligatorio")
    private Integer idAlumno;

    @NotNull(message = "La materia es obligatoria")
    private Integer idMateria;

    @NotBlank(message = "El nombre de la evaluación es obligatorio")
    @Size(max = 200, message = "El nombre de la evaluación debe tener hasta 200 caracteres")
    private String evaluacionNombre;

    /** PARCIAL o TRABAJO_PRACTICO; default PARCIAL si no viene. */
    private String tipo;

    /** 1 a 10 (RN14). Fuera de rango, 400. */
    @NotNull(message = "La nota es obligatoria")
    @Min(value = 1, message = "La nota debe ser un entero entre 1 y 10")
    @Max(value = 10, message = "La nota debe ser un entero entre 1 y 10")
    private Integer nota;

    private String observacion;
}
