package com.academit.education.dto.materia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request de actualización de materia según el contrato
 * (MateriaUpdateDTO del openapi). Todos los campos opcionales; los
 * que no se envían no se cambian.
 */
@Getter
@Setter
public class MateriaUpdateDTO {

    private String nombre;

    @Min(value = 1, message = "El año de cursada debe ser 1, 2, 3, 4 o 5")
    @Max(value = 5, message = "El año de cursada debe ser 1, 2, 3, 4 o 5")
    private Integer anio;

    private String regimen;

    @Min(value = 1, message = "Las horas semanales deben ser mayores que 0")
    private Integer horasSemanales;

    /** Si se envía, reemplaza el set completo de correlativas. */
    private List<Integer> correlativas;
}
