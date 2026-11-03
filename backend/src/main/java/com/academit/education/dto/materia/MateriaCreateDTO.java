package com.academit.education.dto.materia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request de alta de materia según el contrato (MateriaCreateDTO del openapi).
 * La materia se registra sobre un plan de estudios existente.
 */
@Getter
@Setter
public class MateriaCreateDTO {

    @NotBlank(message = "El código de la materia es obligatorio")
    @Size(max = 40, message = "El código debe tener hasta 40 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre de la materia es obligatorio")
    @Size(max = 80, message = "El nombre debe tener hasta 80 caracteres")
    private String nombre;

    @NotNull(message = "La carrera es obligatoria")
    private Integer idCarrera;

    @NotNull(message = "El plan de estudios es obligatorio")
    private Integer idPlan;

    @NotNull(message = "El año de cursada es obligatorio")
    @Min(value = 1, message = "El año de cursada debe ser 1, 2, 3, 4 o 5")
    @Max(value = 5, message = "El año de cursada debe ser 1, 2, 3, 4 o 5")
    private Integer anio;

    /** ANUAL o CUATRIMESTRAL (RegimenMateriaEnum). */
    @NotBlank(message = "El régimen es obligatorio")
    private String regimen;

    @NotNull(message = "La carga horaria semanal es obligatoria")
    @Min(value = 1, message = "Las horas semanales deben ser mayores que 0")
    private Integer horasSemanales;

    /** Ids de materias requeridas previas (del mismo plan). */
    private List<Integer> correlativas;
}
