package com.concertcomparison.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardisierte Error Response DTO für alle API Fehler.
 * Bietet konsistente Fehlerstruktur mit eindeutigen Error Codes,
 * benutzerfreundlichen Nachrichten und optionalen Alternativen.
 */
@Schema(description = "Standardisierte Fehler Response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    @Schema(description = "Eindeutiger Error Code für maschinelle Verarbeitung", example = "SEAT_NOT_AVAILABLE")
    private String code;

    @Schema(description = "Benutzerfreundliche Fehlermeldung", example = "Der Sitzplatz ist leider nicht mehr verfügbar")
    private String message;

    @Schema(description = "HTTP Status Code", example = "409")
    private int status;

    @Schema(description = "Zeitstempel des Fehlers")
    private LocalDateTime timestamp;

    @Schema(description = "Request Path der den Fehler verursacht hat", example = "/api/reservations")
    private String path;

    @Schema(description = "Optionale Details zum Fehler")
    private String details;

    @Schema(description = "Optionale Alternativen oder Vorschläge für den Benutzer")
    private List<AlternativeDTO> alternatives;

    @Schema(description = "Retry-After Zeit in Sekunden (für Rate Limiting)")
    private Integer retryAfter;

    // ==================== Konstruktoren ====================

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public ErrorResponseDTO(String code, String message, int status, LocalDateTime timestamp, String path) {
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<AlternativeDTO> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<AlternativeDTO> alternatives) {
        this.alternatives = alternatives;
    }

    public Integer getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(Integer retryAfter) {
        this.retryAfter = retryAfter;
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
        private String details;
        private List<AlternativeDTO> alternatives;
        private Integer retryAfter;

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

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder alternatives(List<AlternativeDTO> alternatives) {
            this.alternatives = alternatives;
            return this;
        }

        public Builder retryAfter(Integer retryAfter) {
            this.retryAfter = retryAfter;
            return this;
        }

        public ErrorResponseDTO build() {
            ErrorResponseDTO dto = new ErrorResponseDTO();
            dto.code = this.code;
            dto.message = this.message;
            dto.status = this.status;
            dto.timestamp = this.timestamp;
            dto.path = this.path;
            dto.details = this.details;
            dto.alternatives = this.alternatives;
            dto.retryAfter = this.retryAfter;
            return dto;
        }
    }

    /**
     * DTO für Alternative Optionen (z.B. alternative Sitzplätze)
     */
    @Schema(description = "Alternative Sitzplatzangebote")
    public static class AlternativeDTO {

        @Schema(description = "Eindeutige ID der Alternative", example = "42")
        private String id;

        @Schema(description = "Beschreibung der Alternative", example = "Sitzplatz A-15, Kategorie VIP")
        private String description;

        @Schema(description = "Zusätzliche Metadaten (z.B. Preis, Kategorie)")
        private Object metadata;

        // Konstruktoren
        public AlternativeDTO() {
        }

        public AlternativeDTO(String id, String description, Object metadata) {
            this.id = id;
            this.description = description;
            this.metadata = metadata;
        }

        // Getter & Setter
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Object getMetadata() {
            return metadata;
        }

        public void setMetadata(Object metadata) {
            this.metadata = metadata;
        }

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String description;
            private Object metadata;

            public Builder id(String id) {
                this.id = id;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder metadata(Object metadata) {
                this.metadata = metadata;
                return this;
            }

            public AlternativeDTO build() {
                return new AlternativeDTO(id, description, metadata);
            }
        }
    }
}
