package com.academit.education.dto.docente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la actualización de un Docente existente.
 */
@Getter
@Setter
@NoArgsConstructor
public class DocenteUpdateDTO {

    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String nombre;

    @Size(min = 2, max = 60, message = "El apellido debe tener entre 2 y 60 caracteres")
    private String apellido;

    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos numéricos")
    private String dni;

    @Email(message = "El formato del correo electrónico es inválido")
    @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String telefono;

    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String titulo;

    @Pattern(regexp = "^(ACTIVO|INACTIVO)$", message = "El estado debe ser ACTIVO o INACTIVO")
    private String estado;
}

