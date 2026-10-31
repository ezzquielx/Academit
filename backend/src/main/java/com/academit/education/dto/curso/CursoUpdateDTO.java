package com.academit.education.dto.curso;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de actualización de curso según el contrato (CursoUpdateDTO).
 * Todos los campos opcionales; carrera, plan, código y ciclo no se cambian.
 */
@Getter
@Setter
public class CursoUpdateDTO {

    @Size(max = 30, message = "El nombre debe tener hasta 30 caracteres")
    private String nombre;

    /** MANANA, TARDE o NOCHE (TurnoEnum del contrato). */
    private String turno;

    @Size(max = 100, message = "El aula debe tener hasta 100 caracteres")
    private String aula;

    @Positive(message = "La capacidad máxima debe ser mayor que 0")
    private Integer capacidadMaxima;
}
