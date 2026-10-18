package com.academit.education.dto.common;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApiErrorDTO {

    private int status;
    private String message;
    private List<String> errors;

    public ApiErrorDTO() {
        this.errors = new ArrayList<>();
    }

    public ApiErrorDTO(int status, String message) {
        this.status = status;
        this.message = message;
        this.errors = new ArrayList<>();
    }

    public ApiErrorDTO(int status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int status;
        private String message;
        private List<String> errors = new ArrayList<>();

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors != null ? errors : new ArrayList<>();
            return this;
        }

        public ApiErrorDTO build() {
            return new ApiErrorDTO(status, message, errors);
        }
    }
}
