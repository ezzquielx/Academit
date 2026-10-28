package com.academit.education.dto.ciclo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request de actualización de ciclo lectivo (CicloLectivoUpdateDTO).
 * Todos los campos son opcionales; solo se modifican los enviados.
 */
@Getter
@Setter
public class CicloLectivoUpdateDTO {

    private String nombre;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private String estado;
}
