package com.academit.education.dto.inscripcion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO de respuesta para Inscripción según openapi.yaml y entidades.json.
 */
@Getter
@Setter
@NoArgsConstructor
public class InscripcionDTO {

    private Integer idInscripcion;
    private Integer idAlumno;
    private String alumnoNombre;
    private Integer idCurso;
    private String cursoNombre;
    private Integer cicloLectivo;
    private LocalDate fechaInscripcion;
    private String estado;
}

