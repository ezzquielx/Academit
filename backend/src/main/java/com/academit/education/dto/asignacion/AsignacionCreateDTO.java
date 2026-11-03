package com.academit.education.dto.asignacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la creación de una Asignación Docente a una materia y curso.
 */
@Getter
@Setter
@NoArgsConstructor
public class AsignacionCreateDTO {

    @NotNull(message = "El docente es obligatorio")
    private Integer idDocente;

    @NotNull(message = "La materia es obligatoria")
    private Integer idMateria;

    @NotNull(message = "El curso es obligatorio")
    private Integer idCurso;

    @Pattern(regexp = "^(?i)(TITULAR|ADJUNTO|JTP|AYUDANTE)$", message = "El rol docente debe ser TITULAR, ADJUNTO o JTP")
    private String rolDocente;
}

