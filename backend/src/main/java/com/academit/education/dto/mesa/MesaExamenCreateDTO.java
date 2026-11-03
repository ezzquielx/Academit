package com.academit.education.dto.mesa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de alta de mesa de examen según el contrato (MesaExamenCreateDTO).
 * La presidencia del tribunal no se asigna al crear (id_docente opcional en
 * la base; el tribunal completo se carga como texto).
 */
@Getter
@Setter
public class MesaExamenCreateDTO {

    @NotNull(message = "La materia es obligatoria")
    private Integer idMateria;

    @NotNull(message = "La carrera es obligatoria")
    private Integer idCarrera;

    @NotBlank(message = "La fecha es obligatoria")
    @Size(max = 10, message = "La fecha debe tener el formato YYYY-MM-DD")
    private String fecha;

    @Size(max = 8, message = "La hora debe tener hasta 8 caracteres")
    private String hora;

    /** MANANA, TARDE o NOCHE (TurnoEnum del contrato). */
    @NotBlank(message = "El turno es obligatorio")
    private String turno;

    @Size(max = 150, message = "El tribunal debe tener hasta 150 caracteres")
    private String tribunal;

    @Size(max = 50, message = "El aula debe tener hasta 50 caracteres")
    private String aula;

    @Positive(message = "El cupo máximo debe ser mayor que 0")
    @NotNull(message = "El cupo máximo es obligatorio")
    private Integer cupoMaximo;
}
