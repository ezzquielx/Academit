package com.academit.education.dto.carrera;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request de actualización de carrera (parcial) según el contrato
 * (CarreraUpdateDTO del openapi). Todos los campos opcionales.
 */
@Getter
@Setter
public class CarreraUpdateDTO {

    @Size(max = 20, message = "El código debe tener hasta 20 caracteres")
    private String codigo;

    @Size(max = 100, message = "El nombre debe tener hasta 100 caracteres")
    private String nombre;

    @Size(max = 150, message = "El título otorgado debe tener hasta 150 caracteres")
    private String tituloOtorgado;

    @Positive(message = "La duración de la carrera debe ser mayor que 0 años")
    @Max(value = 10, message = "La duración no puede superar los 10 años")
    private Integer duracionAnios;

    @Size(max = 100, message = "La resolución debe tener hasta 100 caracteres")
    private String resolucion;

    /** ACTIVA o INACTIVA (EstadoCarreraEnum). */
    private String estado;
}
