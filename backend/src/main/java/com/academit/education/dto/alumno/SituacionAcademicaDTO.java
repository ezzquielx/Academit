package com.academit.education.dto.alumno;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Situación académica consolidada del alumno según el contrato OpenAPI.
 */
@Getter
@Setter
@NoArgsConstructor
public class SituacionAcademicaDTO {

    private Integer idAlumno;
    private Integer materiasAprobadas = 0;
    private Double promedio = 0.0;
    private List<String> regularidades = new ArrayList<>();
    private List<CalificacionDetalleDTO> calificaciones = new ArrayList<>();
}

