package com.academit.education.dto.carrera;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de alta de carrera según el contrato (CarreraCreateDTO del openapi).
 */
@Getter
@Setter
public class CarreraCreateDTO {

    @NotBlank(message = "El código de la carrera es obligatorio")
    @Size(max = 20, message = "El código debe tener hasta 20 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre de la carrera es obligatorio")
    @Size(max = 100, message = "El nombre debe tener hasta 100 caracteres")
    private String nombre;

    @NotBlank(message = "El título otorgado es obligatorio")
    @Size(max = 150, message = "El título otorgado debe tener hasta 150 caracteres")
    private String tituloOtorgado;

    @NotNull(message = "La duración en años es obligatoria")
    @Positive(message = "La duración de la carrera debe ser mayor que 0 años")
    @Max(value = 10, message = "La duración no puede superar los 10 años")
    private Integer duracionAnios;

    @Size(max = 100, message = "La resolución debe tener hasta 100 caracteres")
    private String resolucion;
}
