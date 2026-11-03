package com.academit.education.dto.matricula;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MatriculaUpdateDTO del contrato: actualiza el estado de la matricula.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaUpdateDTO {

    @NotBlank(message = "El estado de la matrícula es obligatorio")
    @Pattern(regexp = "^(ACTIVA|PENDIENTE|CANCELADA)$",
            message = "El estado debe ser ACTIVA, PENDIENTE o CANCELADA")
    private String estado;
}
