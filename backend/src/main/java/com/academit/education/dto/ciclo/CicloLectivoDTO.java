package com.academit.education.dto.ciclo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Respuesta del contrato para ciclo lectivo (CicloLectivoDTO).
 */
@Getter
@Setter
public class CicloLectivoDTO {

    private Integer idCiclo;
    private Integer anio;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
}
