package com.concertcomparison.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für Validierungsfehler mit detaillierten Feldfehlern.
 * Wird verwendet, wenn mehrere Felder von Bean Validation fehlschlagen.
 */
@Schema(description = "Validierungsfehler Response mit Felddetails")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponseDTO {

    @Schema(description = "Error Code", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "Allgemeine Fehlermeldung", example = "Validierung fehlgeschlagen")
    private String message;

    @Schema(description = "HTTP Status Code", example = "400")
    private int status;

    @Schema(description = "Zeitstempel des Fehlers")
    private LocalDateTime timestamp;

    @Schema(description = "Request Path")
    private String path;

    @Schema(description = "Liste der Feldvalidierungsfehler")
    private List<FieldError> fieldErrors;

    // ==================== Konstruktoren ====================

    public ValidationErrorResponseDTO() {
    }

    public ValidationErrorResponseDTO(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public ValidationErrorResponseDTO(String code, String message, int status, LocalDateTime timestamp, String path) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.path = path;
    }

    // ==================== Getter & Setter ====================

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    // ==================== Builder ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String message;
        private int status;
        private LocalDateTime timestamp;
        private String path;
        private List<FieldError> fieldErrors;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder fieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public ValidationErrorResponseDTO build() {
            ValidationErrorResponseDTO dto = new ValidationErrorResponseDTO();
            dto.code = this.code;
            dto.message = this.message;
            dto.status = this.status;
            dto.timestamp = this.timestamp;
            dto.path = this.path;
            dto.fieldErrors = this.fieldErrors;
            return dto;
        }
    }

    /**
     * DTO für einzelne Feldvalidierungsfehler
     */
    @Schema(description = "Fehlerdetails für ein spezifisches Feld")
    public static class FieldError {

        @Schema(description = "Name des Feldes, das fehlgeschlagen hat", example = "email")
        private String field;

        @Schema(description = "Abgelehnter Wert", example = "invalid-email")
        private Object rejectedValue;

        @Schema(description = "Fehlermeldung für dieses Feld", example = "Muss eine gültige E-Mail-Adresse sein")
        private String message;

        @Schema(description = "Validierungscode", example = "Email")
        private String code;

        // Konstruktoren
        public FieldError() {
        }

        public FieldError(String field, Object rejectedValue, String message, String code) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
            this.code = code;
        }

        // Getter & Setter
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String field;
            private Object rejectedValue;
            private String message;
            private String code;

            public Builder field(String field) {
                this.field = field;
                return this;
            }

            public Builder rejectedValue(Object rejectedValue) {
                this.rejectedValue = rejectedValue;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder code(String code) {
                this.code = code;
                return this;
            }

            public FieldError build() {
                return new FieldError(field, rejectedValue, message, code);
            }
        }
    }
}
