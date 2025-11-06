package com.travelreimburse.application.dto;

import com.travelreimburse.domain.model.ReceiptType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO für das Erstellen eines Receipts (ohne Datei).
 * Die Datei wird separat als MultipartFile hochgeladen.
 */
public record CreateReceiptDTO(
        @NotNull(message = "Travel Request ID ist erforderlich")
        Long travelRequestId,

        @NotNull(message = "Belegtyp ist erforderlich")
        ReceiptType type,

        @NotNull(message = "Ausstellungsdatum ist erforderlich")
        @PastOrPresent(message = "Ausstellungsdatum darf nicht in der Zukunft liegen")
        LocalDate issueDate,

        @Size(max = 500, message = "Beschreibung darf maximal 500 Zeichen lang sein")
        String description,

        @DecimalMin(value = "0.01", message = "Betrag muss positiv sein")
        BigDecimal amount,

        String currency,

        @Size(max = 100, message = "Anbieter darf maximal 100 Zeichen lang sein")
        String vendor
) {
}

