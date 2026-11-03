package com.academit.education.dto.solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SolicitudResolverDTO del contrato: resolucion administrativa.
 * Solo requiere estado; la fecha de resolucion la pone el backend
 * y observacion se guarda como respuesta de la solicitud.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResolverDTO {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(APROBADA|RECHAZADA|EN_PROCESO)$",
            message = "El estado debe ser APROBADA, RECHAZADA o EN_PROCESO")
    private String estado;

    @Size(max = 255, message = "La observación no puede superar los 255 caracteres")
    private String observacion;
}
