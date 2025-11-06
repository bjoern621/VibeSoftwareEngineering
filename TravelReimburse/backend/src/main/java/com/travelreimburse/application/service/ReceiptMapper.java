package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.ReceiptDTO;
import com.travelreimburse.domain.model.Receipt;
import org.springframework.stereotype.Component;

/**
 * Mapper für Receipt Entity ↔ DTO Konvertierung.
 */
@Component
public class ReceiptMapper {

    /**
     * Konvertiert Receipt Entity zu DTO
     */
    public ReceiptDTO toDTO(Receipt receipt) {
        if (receipt == null) {
            return null;
        }

        return new ReceiptDTO(
                receipt.getId(),
                receipt.getTravelRequest().getId(),
                receipt.getFileName(),
                receipt.getOriginalFileName(),
                receipt.getContentType(),
                receipt.getFileSize(),
                receipt.getType(),
                receipt.getIssueDate(),
                receipt.getDescription(),
                receipt.getAmount() != null ? receipt.getAmount().getAmount() : null,
                receipt.getAmount() != null ? receipt.getAmount().getCurrency().name() : null,
                receipt.getVendor(),
                receipt.getStatus(),
                receipt.getUploadedAt(),
                receipt.getValidatedAt(),
                receipt.getRejectionReason()
        );
    }
}

