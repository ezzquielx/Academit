package com.academit.education.dto.carrera;

import lombok.Getter;
import lombok.Setter;

/**
 * Response de carrera según el contrato (CarreraDTO del openapi).
 */
@Getter
@Setter
public class CarreraDTO {

    private Integer idCarrera;
    private String codigo;
    private String nombre;
    private String tituloOtorgado;
    private Integer duracionAnios;
    private String resolucion;
    private String estado;
}
