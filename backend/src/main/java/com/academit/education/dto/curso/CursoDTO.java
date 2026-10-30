package com.academit.education.dto.curso;

import lombok.Getter;
import lombok.Setter;

/**
 * Response de curso según el contrato (CursoDTO del openapi).
 * inscriptos, cuposDisponibles, carreraNombre y planNombre son campos
 * calculados (no persistidos). Lo comparten los módulos carreras y cursos.
 */
@Getter
@Setter
public class CursoDTO {

    private Integer idCurso;
    private String codigo;
    private String nombre;
    private Integer idCarrera;
    private String carreraNombre;
    private Integer idPlan;
    private String planNombre;
    private String turno;
    private Integer capacidadMaxima;
    private Integer inscriptos;
    private Integer cuposDisponibles;
    /** Año del ciclo lectivo del curso (el mock del frontend usa el año). */
    private Integer cicloLectivo;
    private String aula;
}
