package com.travelreimburse.presentation.dto;

import jakarta.validation.constraints.Min;

/**
 * Request DTO für benutzerdefinierte Archivierung
 */
public record ArchiveRequestDTO(
    @Min(value = 1, message = "Aufbewahrungsfrist muss mindestens 1 Jahr betragen")
    Integer retentionYears
) {}

