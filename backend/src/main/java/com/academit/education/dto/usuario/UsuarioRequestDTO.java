package com.academit.education.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    private String password;

    private String nombre;

    private String email;

    private String rol;

    private String estado;

    public UsuarioRequestDTO() {
    }

    public UsuarioRequestDTO(String username, String password, String nombre, String email, String rol, String estado) {
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.estado = estado;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password;
        private String nombre;
        private String email;
        private String rol;
        private String estado;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
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

        public UsuarioRequestDTO build() {
            return new UsuarioRequestDTO(username, password, nombre, email, rol, estado);
        }
    }
}
