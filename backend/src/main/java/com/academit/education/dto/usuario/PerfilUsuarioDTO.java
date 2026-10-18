package com.academit.education.dto.usuario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilUsuarioDTO {

    private String username;
    private String nombre;
    private String email;
    private String telefono;
    private String domicilio;
    private String rol;

    public PerfilUsuarioDTO() {
    }

    public PerfilUsuarioDTO(String username, String nombre, String email, String telefono, String domicilio, String rol) {
        this.username = username;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.domicilio = domicilio;
        this.rol = rol;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String nombre;
        private String email;
        private String telefono;
        private String domicilio;
        private String rol;

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

        public Builder telefono(String telefono) {
            this.telefono = telefono;
            return this;
        }

        public Builder domicilio(String domicilio) {
            this.domicilio = domicilio;
            return this;
        }

        public Builder rol(String rol) {
            this.rol = rol;
            return this;
        }

        public PerfilUsuarioDTO build() {
            return new PerfilUsuarioDTO(username, nombre, email, telefono, domicilio, rol);
        }
    }
}
