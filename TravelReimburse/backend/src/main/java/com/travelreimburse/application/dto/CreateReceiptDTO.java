package com.travelreimburse.application.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.travelreimburse.domain.model.ReceiptType;
import com.travelreimburse.infrastructure.serialization.FlexibleBigDecimalDeserializer;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO für das Erstellen eines Receipts (ohne Datei).
 * Die Datei wird separat als MultipartFile hochgeladen.
 * Akzeptiert beide Dezimalformate: "20.00" und "20,00"
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
        @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
        BigDecimal amount,

        String currency,

        @Size(max = 100, message = "Anbieter darf maximal 100 Zeichen lang sein")
        String vendor
) {
}

