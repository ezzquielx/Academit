package com.academit.education.dto.solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SolicitudCreateDTO del contrato: el alumno crea sus propias solicitudes
 * (el idAlumno debe coincidir con el de la sesion). La solicitud nace
 * siempre en estado PENDIENTE.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCreateDTO {

    @NotNull(message = "El id del alumno es obligatorio")
    private Integer idAlumno;

    @NotBlank(message = "El tipo de solicitud es obligatorio")
    @Pattern(regexp = "^(CONSTANCIA|PASANTIA|TITULO_INTERMEDIO|ACADEMICA|CAMBIO_CARRERA|REINCORPORACION)$",
            message = "El tipo debe ser CONSTANCIA, PASANTIA, TITULO_INTERMEDIO, ACADEMICA, CAMBIO_CARRERA o REINCORPORACION")
    private String tipo;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres")
    private String observaciones;
}
