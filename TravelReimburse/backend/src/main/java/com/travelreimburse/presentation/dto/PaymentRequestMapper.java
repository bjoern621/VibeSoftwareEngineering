package com.travelreimburse.presentation.dto;

import com.travelreimburse.domain.model.PaymentRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Mapper für PaymentRequest <-> PaymentRequestDTO
 */
@Component
public class PaymentRequestMapper {

    /**
     * Konvertiert PaymentRequest Entity zu DTO
     */
    public PaymentRequestDTO toDTO(PaymentRequest entity) {
        if (entity == null) {
            return null;
        }

        return new PaymentRequestDTO(
            entity.getId(),
            entity.getTravelRequest().getId(),
            entity.getTotalAmount().getAmount(),
            entity.getTotalAmount().getCurrency().toString(),
            entity.getPaymentReference().getReference(),
            entity.getStatus().toString(),
            entity.getCreatedAt(),
            entity.getSubmittedAt(),
            entity.getCompletedAt(),
            Optional.ofNullable(entity.getEasyPayTransactionId()),
            Optional.ofNullable(entity.getFailureReason())
        );
    }
}

