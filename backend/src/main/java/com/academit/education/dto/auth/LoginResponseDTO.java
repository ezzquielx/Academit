package com.academit.education.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

    private Integer idUsuario;
    private String usuario;
    private String nombre;
    private String rol;
    private String email;
    private String token;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Integer idUsuario, String usuario, String nombre, String rol, String email, String token) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.nombre = nombre;
        this.rol = rol;
        this.email = email;
        this.token = token;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer idUsuario;
        private String usuario;
        private String nombre;
        private String rol;
        private String email;
        private String token;

        public Builder idUsuario(Integer idUsuario) {
            this.idUsuario = idUsuario;
            return this;
        }

        public Builder usuario(String usuario) {
            this.usuario = usuario;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder rol(String rol) {
            this.rol = rol;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseDTO build() {
            return new LoginResponseDTO(idUsuario, usuario, nombre, rol, email, token);
        }
    }
}
