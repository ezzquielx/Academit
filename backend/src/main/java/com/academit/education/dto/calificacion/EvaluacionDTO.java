package com.academit.education.dto.calificacion;

import lombok.Getter;
import lombok.Setter;

/**
 * Response de evaluación según el contrato (EvaluacionDTO). Los nombres de
 * materia y curso son campos desnormalizados calculados en el response.
 */
@Getter
@Setter
public class EvaluacionDTO {

    private Integer idEvaluacion;

    private Integer idMateria;

    private String materiaNombre;

    private Integer idCurso;

    private String cursoNombre;

    private String fecha;

    /** PARCIAL o TRABAJO_PRACTICO. */
    private String tipo;

    private String descripcion;

    private String ponderacion;
}
