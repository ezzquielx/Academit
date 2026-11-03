package com.academit.education.dto.declaracion;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response de declaración jurada según el contrato (DeclaracionJuradaDTO).
 * Los nombres desnormalizados son calculados, no persistidos.
 */
@Getter
@Setter
public class DeclaracionJuradaDTO {

    private Integer idDeclaracion;

    private Integer idAlumno;

    private String alumnoNombre;

    private Integer idCiclo;

    private String cicloNombre;

    private String fecha;

    /** Ids de materias que el alumno se compromete a cursar. */
    private List<Integer> materiasDeclaradasIds;

    /** Nombres derivados de los ids (calculado). */
    private List<String> materiasNombres;

    /** VALIDADA, PENDIENTE o RECHAZADA. */
    private String estado;

    private String observaciones;
}
