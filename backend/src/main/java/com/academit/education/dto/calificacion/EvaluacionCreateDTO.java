package com.academit.education.dto.calificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de alta de evaluación según el contrato (EvaluacionCreateDTO).
 * La evaluación se vincula a la cátedra (curso + materia) donde el docente
 * crea la evaluación (RN11).
 */
@Getter
@Setter
public class EvaluacionCreateDTO {

    @NotNull(message = "La materia es obligatoria")
    private Integer idMateria;

    @NotNull(message = "El curso es obligatorio")
    private Integer idCurso;

    @NotNull(message = "La fecha es obligatoria")
    private String fecha;

    /** PARCIAL o TRABAJO_PRACTICO (catálogo del contrato). */
    @NotBlank(message = "El tipo de evaluación es obligatorio")
    private String tipo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción debe tener hasta 200 caracteres")
    private String descripcion;

    /** Texto libre del contrato (en la maqueta llega como «40%»). */
    @Size(max = 20, message = "La ponderación debe tener hasta 20 caracteres")
    private String ponderacion;
}
