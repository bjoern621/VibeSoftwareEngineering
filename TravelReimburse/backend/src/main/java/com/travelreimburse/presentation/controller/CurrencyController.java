package com.travelreimburse.presentation.controller;

import com.travelreimburse.domain.model.Currency;
import com.travelreimburse.domain.model.ExchangeRate;
import com.travelreimburse.domain.model.Money;
import com.travelreimburse.domain.service.CurrencyConversionService;
import com.travelreimburse.presentation.dto.CurrencyConversionRequestDTO;
import com.travelreimburse.presentation.dto.CurrencyConversionResponseDTO;
import com.travelreimburse.presentation.dto.ExchangeRateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * REST-Controller für Währungsverwaltung und -umrechnung
 */
@RestController
@RequestMapping("/api/currency")
@Tag(name = "Währungen", description = "Verwaltung von Währungen und Wechselkursen")
public class CurrencyController {

    private final CurrencyConversionService conversionService;

    public CurrencyController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Liste aller unterstützten Währungen
     */
    @GetMapping("/supported")
    @Operation(summary = "Liste aller unterstützten Währungen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen")
    })
    public ResponseEntity<List<Currency>> getSupportedCurrencies() {
        return ResponseEntity.ok(Arrays.asList(Currency.values()));
    }

    /**
     * Aktuellen Wechselkurs zwischen zwei Währungen abrufen
     */
    @GetMapping("/exchange-rate")
    @Operation(summary = "Wechselkurs abrufen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wechselkurs erfolgreich abgerufen"),
        @ApiResponse(responseCode = "400", description = "Ungültige Parameter"),
        @ApiResponse(responseCode = "503", description = "ExRat-Service nicht erreichbar")
    })
    public ResponseEntity<ExchangeRateDTO> getExchangeRate(
        @RequestParam Currency from,
        @RequestParam Currency to,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        ExchangeRate exchangeRate = conversionService.getExchangeRate(from, to, effectiveDate);

        ExchangeRateDTO dto = new ExchangeRateDTO(
            exchangeRate.getFromCurrency(),
            exchangeRate.getToCurrency(),
            exchangeRate.getRate(),
            exchangeRate.getDate()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Währungsumrechnung durchführen
     */
    @PostMapping("/convert")
    @Operation(summary = "Währung umrechnen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Umrechnung erfolgreich"),
        @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten"),
        @ApiResponse(responseCode = "503", description = "ExRat-Service nicht erreichbar")
    })
    public ResponseEntity<CurrencyConversionResponseDTO> convertCurrency(
        @Valid @RequestBody CurrencyConversionRequestDTO request
    ) {
        // Erstelle Money-Objekt aus Request
        Money originalMoney = new Money(request.amount(), request.fromCurrency());

        // Datum bestimmen
        LocalDate date = request.date() != null ? request.date() : LocalDate.now();

        // Umrechnung durchführen
        Money convertedMoney = conversionService.convert(originalMoney, request.toCurrency(), date);

        // Wechselkurs für Response
        ExchangeRate exchangeRate = conversionService.getExchangeRate(
            request.fromCurrency(),
            request.toCurrency(),
            date
        );

        // Response erstellen
        CurrencyConversionResponseDTO response = new CurrencyConversionResponseDTO(
            originalMoney.getAmount(),
            originalMoney.getCurrency(),
            convertedMoney.getAmount(),
            convertedMoney.getCurrency(),
            exchangeRate.getRate(),
            date
        );

        return ResponseEntity.ok(response);
    }
}

