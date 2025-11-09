package com.travelreimburse.presentation.dto;

import java.time.LocalDate;

/**
 * DTO für RetentionPeriod
 */
public record RetentionPeriodDTO(
    LocalDate archivedAt,
    LocalDate retentionEndDate,
    int retentionYears,
    boolean expired,
    long remainingDays
) {}

