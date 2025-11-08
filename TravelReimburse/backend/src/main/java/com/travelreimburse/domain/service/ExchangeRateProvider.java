package com.travelreimburse.domain.service;

import com.travelreimburse.domain.model.Currency;
import com.travelreimburse.domain.model.ExchangeRate;

import java.time.LocalDate;

/**
 * Domain Service Interface für Wechselkurs-Provider
 * 
 * ✅ DDD: Interface im Domain Layer, Implementierung im Infrastructure Layer
 * ✅ Dependency Inversion Principle: Domain definiert Interface, Infrastructure implementiert
 */
public interface ExchangeRateProvider {
    
    /**
     * Holt den Wechselkurs zwischen zwei Währungen für ein bestimmtes Datum
     * 
     * @param fromCurrency Quellwährung
     * @param toCurrency Zielwährung
     * @param date Datum für den Wechselkurs
     * @return Wechselkurs-Objekt
     */
    ExchangeRate getExchangeRate(Currency fromCurrency, Currency toCurrency, LocalDate date);
}
