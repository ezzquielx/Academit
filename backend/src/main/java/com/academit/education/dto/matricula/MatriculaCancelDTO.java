package com.academit.education.dto.matricula;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MatriculaCancelDTO del contrato: motivo opcional de la cancelacion (RF27).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaCancelDTO {

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;
}
