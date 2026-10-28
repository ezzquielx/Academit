package com.academit.education.dto.ciclo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request de creación de ciclo lectivo (CicloLectivoCreateDTO).
 * Estado es opcional: si no se envía, el ciclo nace PLANIFICADO.
 */
@Getter
@Setter
public class CicloLectivoCreateDTO {

    @NotNull(message = "El año calendario es obligatorio")
    private Integer anio;

    @NotBlank(message = "El nombre del ciclo es obligatorio")
    private String nombre;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    private String estado;
}
