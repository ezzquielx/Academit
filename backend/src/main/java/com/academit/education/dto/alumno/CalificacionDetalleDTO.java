package com.academit.education.dto.alumno;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Detalle individual de calificación dentro de la situación académica.
 */
@Getter
@Setter
@NoArgsConstructor
public class CalificacionDetalleDTO {

    private Integer idCalificacion;
    private String materiaNombre;
    private String evaluacionNombre;
    private String tipo;
    private LocalDate fecha;
    private Integer nota;
    private String condicion;
    private String observacion;
}

