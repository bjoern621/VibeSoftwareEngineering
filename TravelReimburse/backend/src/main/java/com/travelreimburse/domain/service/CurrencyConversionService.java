package com.travelreimburse.domain.service;

import com.travelreimburse.domain.model.Currency;
import com.travelreimburse.domain.model.ExchangeRate;
import com.travelreimburse.domain.model.Money;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Domain Service für Währungsumrechnung
 * Orchestriert die Umrechnung von Geldbeträgen zwischen verschiedenen Währungen
 * 
 * ✅ DDD: Verwendet Domain-Interface statt direkter Infrastructure-Abhängigkeit
 */
@Service
public class CurrencyConversionService {

    private final ExchangeRateProvider exchangeRateProvider;

    public CurrencyConversionService(ExchangeRateProvider exchangeRateProvider) {
        this.exchangeRateProvider = exchangeRateProvider;
    }

    /**
     * Rechnet einen Geldbetrag in eine andere Währung um
     * Verwendet den Wechselkurs vom angegebenen Datum
     */
    public Money convert(Money amount, Currency targetCurrency, LocalDate date) {
        if (amount == null || targetCurrency == null || date == null) {
            throw new IllegalArgumentException("Betrag, Zielwährung und Datum dürfen nicht null sein");
        }

        // Keine Konvertierung nötig wenn gleiche Währung
        if (amount.getCurrency() == targetCurrency) {
            return amount;
        }

        // Hole Wechselkurs vom ExchangeRateProvider (Domain-Interface)
        ExchangeRate exchangeRate = exchangeRateProvider.getExchangeRate(
            amount.getCurrency(),
            targetCurrency,
            date
        );

        // Führe Umrechnung durch
        return exchangeRate.convert(amount);
    }

    /**
     * Rechnet einen Geldbetrag in eine andere Währung um
     * Verwendet den aktuellen Tageskurs
     */
    public Money convert(Money amount, Currency targetCurrency) {
        return convert(amount, targetCurrency, LocalDate.now());
    }

    /**
     * Holt den aktuellen Wechselkurs zwischen zwei Währungen
     * ✅ DDD: Verwendet Domain-Interface
     */
    public ExchangeRate getExchangeRate(Currency from, Currency to, LocalDate date) {
        if (from == null || to == null || date == null) {
            throw new IllegalArgumentException("Währungen und Datum dürfen nicht null sein");
        }

        return exchangeRateProvider.getExchangeRate(from, to, date);
    }

    /**
     * Holt den aktuellen Wechselkurs (für heutiges Datum)
     */
    public ExchangeRate getExchangeRate(Currency from, Currency to) {
        return getExchangeRate(from, to, LocalDate.now());
    }
}

