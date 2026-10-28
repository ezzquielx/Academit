package com.academit.education.dto.documentacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DocumentacionUpdateDTO del contrato: unica escritura del modulo.
 * No existe POST ni DELETE de documentacion en el contrato.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentacionUpdateDTO {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(ENTREGADO|PENDIENTE|RECHAZADO)$",
            message = "El estado debe ser ENTREGADO, PENDIENTE o RECHAZADO")
    private String estado;

    @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres")
    private String observaciones;

    private LocalDate fechaEntrega;
}
