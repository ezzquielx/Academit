package com.academit.education.dto.declaracion;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request de presentación de declaración jurada según el contrato
 * (DeclaracionJuradaCreateDTO). El estado inicial (PENDIENTE) y la fecha
 * los asigna el backend.
 */
@Getter
@Setter
public class DeclaracionJuradaCreateDTO {

    /** Alumno firmante; ALUMNO solo puede presentar la suya (403 si no). */
    @NotNull(message = "El alumno es obligatorio")
    private Integer idAlumno;

    @NotNull(message = "El ciclo lectivo es obligatorio")
    private Integer idCiclo;

    @NotEmpty(message = "Debe declarar al menos una materia a cursar")
    private List<Integer> materiasDeclaradasIds;

    private String observaciones;
}
