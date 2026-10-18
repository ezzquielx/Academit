package com.academit.education.dto.usuario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioResponseDTO {

    private Integer idUsuario;
    private String username;
    private String nombre;
    private String email;
    private String rol;
    private String estado;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Integer idUsuario, String username, String nombre, String email, String rol, String estado) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.estado = estado;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer idUsuario;
        private String username;
        private String nombre;
        private String email;
        private String rol;
        private String estado;

        public Builder idUsuario(Integer idUsuario) {
            this.idUsuario = idUsuario;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder rol(String rol) {
            this.rol = rol;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public UsuarioResponseDTO build() {
            return new UsuarioResponseDTO(idUsuario, username, nombre, email, rol, estado);
        }
    }
}
