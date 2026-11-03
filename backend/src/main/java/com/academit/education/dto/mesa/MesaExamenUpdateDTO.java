package com.academit.education.dto.mesa;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de edición de mesa de examen según el contrato
 * (MesaExamenUpdateDTO). Materia y carrera no se cambian.
 */
@Getter
@Setter
public class MesaExamenUpdateDTO {

    private String fecha;

    @Size(max = 8, message = "La hora debe tener hasta 8 caracteres")
    private String hora;

    /** MANANA, TARDE o NOCHE. */
    private String turno;

    @Size(max = 150, message = "El tribunal debe tener hasta 150 caracteres")
    private String tribunal;

    @Size(max = 50, message = "El aula debe tener hasta 50 caracteres")
    private String aula;

    @Positive(message = "El cupo máximo debe ser mayor que 0")
    private Integer cupoMaximo;

    /** ABIERTA, CERRADA o FINALIZADA. */
    private String estado;
}
