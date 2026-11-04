package com.academit.education.dto.asignacion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para Asignación Docente según openapi.yaml y entidades.json.
 */
@Getter
@Setter
@NoArgsConstructor
public class AsignacionDTO {

    private Integer idAsignacion;
    private Integer idDocente;
    private String docenteNombre;
    private Integer idMateria;
    private String materiaNombre;
    private Integer idCurso;
    private String cursoNombre;
    private String rolDocente;
}

