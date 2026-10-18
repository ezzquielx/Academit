package com.academit.education.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPasswordDTO {

    @NotBlank(message = "La contraseña actual es requerida")
    private String actual;

    @NotBlank(message = "La nueva contraseña es requerida")
    private String nueva;

    public CambiarPasswordDTO() {
    }

    public CambiarPasswordDTO(String actual, String nueva) {
        this.actual = actual;
        this.nueva = nueva;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String actual;
        private String nueva;

        public Builder actual(String actual) {
            this.actual = actual;
            return this;
        }

        public Builder nueva(String nueva) {
            this.nueva = nueva;
            return this;
        }

        public CambiarPasswordDTO build() {
            return new CambiarPasswordDTO(actual, nueva);
        }
    }
}
