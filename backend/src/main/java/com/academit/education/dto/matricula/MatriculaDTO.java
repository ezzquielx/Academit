package com.academit.education.dto.matricula;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * MatriculaDTO del contrato (docs/frontend/openapi.yaml).
 * estadoPago y los campos desnormalizados se calculan en el service;
 * nunca se persisten.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatriculaDTO {

    private Integer idMatricula;
    private Integer idAlumno;
    private String alumnoNombre;
    private Integer idCarrera;
    private String carreraNombre;
    private Integer idCiclo;
    private String cicloLectivoNombre;
    private Integer cicloLectivo;
    private LocalDate fecha;
    private String estado;
    private String estadoPago;
}
