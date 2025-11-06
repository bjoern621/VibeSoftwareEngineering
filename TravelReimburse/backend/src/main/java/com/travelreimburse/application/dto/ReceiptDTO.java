package com.travelreimburse.application.dto;

import com.travelreimburse.domain.model.ReceiptStatus;
import com.travelreimburse.domain.model.ReceiptType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO für Receipt-Response.
 * Wird verwendet um Receipt-Daten an die Presentation Layer zu übertragen.
 */
public record ReceiptDTO(
        Long id,
        Long travelRequestId,
        String fileName,
        String originalFileName,
        String contentType,
        Long fileSize,
        ReceiptType type,
        LocalDate issueDate,
        String description,
        BigDecimal amount,
        String currency,
        String vendor,
        ReceiptStatus status,
        LocalDateTime uploadedAt,
        LocalDateTime validatedAt,
        String rejectionReason
) {
}

