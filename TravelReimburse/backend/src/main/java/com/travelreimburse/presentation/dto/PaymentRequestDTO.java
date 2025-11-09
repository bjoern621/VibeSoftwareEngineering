package com.travelreimburse.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DTO für PaymentRequest (Response)
 */
public record PaymentRequestDTO(
    Long id,
    Long travelRequestId,
    BigDecimal totalAmount,
    String currency,
    String paymentReference,
    String status,
    LocalDateTime createdAt,
    LocalDateTime submittedAt,
    LocalDateTime completedAt,
    Optional<String> easyPayTransactionId,
    Optional<String> failureReason
) {}

