package com.academit.education.dto.docente;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para Docente según openapi.yaml y entidades.json.
 */
@Getter
@Setter
@NoArgsConstructor
public class DocenteDTO {

    private Integer idDocente;
    private String legajo;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String titulo;
    private String estado;
}

