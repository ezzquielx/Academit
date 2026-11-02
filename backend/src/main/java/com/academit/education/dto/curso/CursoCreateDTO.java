package com.academit.education.dto.curso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de alta de curso según el contrato (CursoCreateDTO del openapi).
 * cicloLectivo es el AÑO del ciclo lectivo (ej. 2026), como lo usa el mock
 * del frontend; el backend lo resuelve a la entidad CicloLectivo.
 */
@Getter
@Setter
public class CursoCreateDTO {

    @NotBlank(message = "El código del curso es obligatorio")
    @Size(max = 40, message = "El código debe tener hasta 40 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Size(max = 30, message = "El nombre debe tener hasta 30 caracteres")
    private String nombre;

    @NotNull(message = "La carrera es obligatoria")
    private Integer idCarrera;

    @NotNull(message = "El plan de estudios es obligatorio")
    private Integer idPlan;

    /** MANANA, TARDE o NOCHE (TurnoEnum del contrato, sin tilde). */
    @NotBlank(message = "El turno es obligatorio")
    private String turno;

    @Positive(message = "La capacidad máxima debe ser mayor que 0")
    @NotNull(message = "La capacidad máxima es obligatoria")
    private Integer capacidadMaxima;

    @Size(max = 100, message = "El aula debe tener hasta 100 caracteres")
    private String aula;

    /** Año del ciclo lectivo al que pertenece el curso (RN23). */
    @NotNull(message = "El ciclo lectivo es obligatorio")
    private Integer cicloLectivo;
}
