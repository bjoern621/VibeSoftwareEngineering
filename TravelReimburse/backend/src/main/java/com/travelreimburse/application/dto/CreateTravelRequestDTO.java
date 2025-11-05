package com.travelreimburse.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO für das Erstellen eines neuen Reiseantrags
 * Enthält alle erforderlichen Informationen für einen Reiseantrag
 */
public record CreateTravelRequestDTO(
    @NotNull(message = "EmployeeId darf nicht null sein")
    Long employeeId,
    
    @NotBlank(message = "Reiseziel darf nicht leer sein")
    @Size(max = 500, message = "Reiseziel darf maximal 500 Zeichen lang sein")
    String destination,
    
    @NotBlank(message = "Reisezweck darf nicht leer sein")
    @Size(max = 1000, message = "Reisezweck darf maximal 1000 Zeichen lang sein")
    String purpose,
    
    @NotNull(message = "Startdatum darf nicht null sein")
    @FutureOrPresent(message = "Startdatum darf nicht in der Vergangenheit liegen")
    LocalDate startDate,
    
    @NotNull(message = "Enddatum darf nicht null sein")
    LocalDate endDate,
    
    @NotNull(message = "Geschätzte Kosten dürfen nicht null sein")
    @DecimalMin(value = "0.01", message = "Geschätzte Kosten müssen größer als 0 sein")
    BigDecimal estimatedAmount,
    
    @NotBlank(message = "Währung darf nicht leer sein")
    @Pattern(regexp = "EUR|USD|GBP|CHF", message = "Ungültige Währung. Erlaubt: EUR, USD, GBP, CHF")
    String currency
) {
}
