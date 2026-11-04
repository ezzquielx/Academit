package com.academit.education.dto.asistencia;

import lombok.Getter;
import lombok.Setter;

/**
 * Response de asistencia según el contrato (AsistenciaDTO). Nombres de
 * alumno y materia desnormalizados calculados en el response.
 */
@Getter
@Setter
public class AsistenciaDTO {

    private Integer idAsistencia;

    private Integer idAlumno;

    private String alumnoNombre;

    private Integer idMateria;

    private String materiaNombre;

    private Integer idCurso;

    private String fecha;

    /** PRESENTE, AUSENTE, TARDANZA o JUSTIFICADO. */
    private String estado;
}
