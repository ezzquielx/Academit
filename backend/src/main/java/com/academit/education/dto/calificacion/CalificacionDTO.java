package com.academit.education.dto.calificacion;

import lombok.Getter;
import lombok.Setter;

/**
 * Response de calificación según el contrato (CalificacionDTO). La condición
 * (APROBADO con nota &gt;= 4, RN21) es un campo calculado y nunca persistido.
 */
@Getter
@Setter
public class CalificacionDTO {

    private Integer idCalificacion;

    private Integer idAlumno;

    private String alumnoNombre;

    private Integer idMateria;

    private String materiaNombre;

    private String evaluacionNombre;

    /** PARCIAL o TRABAJO_PRACTICO (de la evaluación). */
    private String tipo;

    private Integer nota;

    private String fecha;

    /** APROBADO si nota &gt;= 4, DESAPROBADO en otro caso (RN21). */
    private String condicion;

    private String observacion;
}
