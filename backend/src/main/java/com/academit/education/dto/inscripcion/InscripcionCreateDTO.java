package com.academit.education.dto.inscripcion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la creación de una inscripción de un alumno a un curso/comisión.
 */
@Getter
@Setter
@NoArgsConstructor
public class InscripcionCreateDTO {

    @NotNull(message = "El alumno es obligatorio")
    private Integer idAlumno;

    @NotNull(message = "El curso es obligatorio")
    private Integer idCurso;

    private Integer cicloLectivo;
    private String alumnoNombre;
    private String cursoNombre;
}

