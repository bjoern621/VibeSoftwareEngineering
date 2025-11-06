package com.travelreimburse.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO für Genehmigung eines Reiseantrags
 */
public record ApprovalRequestDTO(
    @NotNull(message = "ApproverId ist erforderlich")
    Long approverId
) {
}

