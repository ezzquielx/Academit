package com.academit.education.dto.asistencia;

import lombok.Getter;
import lombok.Setter;

/**
 * Componente de situación académica por materia (calculado, nunca
 * persistido) que expondrá GET /api/alumnos/{id}/situacion-academica.
 */
@Getter
@Setter
public class SituacionMateriaDTO {

    private Integer idMateria;

    private String materiaNombre;

    /** Clases registradas con asistencia para alumno + materia. */
    private Integer clasesRegistradas;

    /** Clases con asistencia a favor (PRESENTE, TARDANZA o JUSTIFICADO). */
    private Integer clasesConAsistencia;

    /** Porcentaje de asistencia: 0 a 100, redondeado. */
    private Integer porcentajeAsistencia;

    /** True si las notas de la materia no tienen desaprobado sin recuperar. */
    private Boolean notasSinDesaprobados;

    /** REGULAR si porcentaje &gt;= 75 y notasSinDesaprobados (regla del grupo). */
    private Boolean regular;
}
