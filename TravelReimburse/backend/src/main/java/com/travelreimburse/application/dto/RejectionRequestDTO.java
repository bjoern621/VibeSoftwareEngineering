package com.travelreimburse.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO für Ablehnung eines Reiseantrags
 */
public record RejectionRequestDTO(
    @NotNull(message = "ApproverId ist erforderlich")
    Long approverId,

    @NotBlank(message = "Ablehnungsgrund ist erforderlich")
    String reason
) {
}

