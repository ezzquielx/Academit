package com.academit.education.dto.solicitud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * SolicitudDTO del contrato (docs/frontend/openapi.yaml).
 * alumnoNombre es el unico campo calculado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudDTO {

    private Integer idSolicitud;
    private Integer idAlumno;
    private String alumnoNombre;
    private String tipo;
    private LocalDate fechaSolicitud;
    private String motivo;
    private String observaciones;
    private String estado;
    private LocalDate fechaResolucion;
    private String respuesta;
}
