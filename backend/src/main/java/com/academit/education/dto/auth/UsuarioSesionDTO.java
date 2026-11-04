package com.academit.education.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSesionDTO {

    private Integer idUsuario;
    private String username;
    private String nombre;
    private String rol;
    private Integer idAlumno;
    private Integer idDocente;
}
