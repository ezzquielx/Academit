package com.academit.education.dto.documentacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DocumentacionDTO del contrato (docs/frontend/openapi.yaml).
 * alumnoNombre es el unico campo calculado; el resto vive en la tabla
 * documentacion (tipo con catalogo restringido en BD).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentacionDTO {

    private Integer idDocumento;
    private Integer idAlumno;
    private String alumnoNombre;
    private String tipo;
    private Boolean obligatorio;
    private String estado;
    private LocalDate fechaEntrega;
    private String observaciones;
}
