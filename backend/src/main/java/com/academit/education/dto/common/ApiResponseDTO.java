package com.academit.education.dto.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDTO {

    private boolean success;
    private String message;

    public ApiResponseDTO() {
    }

    public ApiResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ApiResponseDTO ok() {
        return new ApiResponseDTO(true, null);
    }

    public static ApiResponseDTO ok(String message) {
        return new ApiResponseDTO(true, message);
    }

    public static ApiResponseDTO error(String message) {
        return new ApiResponseDTO(false, message);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String message;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ApiResponseDTO build() {
            return new ApiResponseDTO(success, message);
        }
    }
}
