package com.travelreimburse.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für ExchangeRate Value Object
 */
class ExchangeRateTest {

    @Test
    void shouldCreateValidExchangeRate() {
        // Given
        Currency from = Currency.EUR;
        Currency to = Currency.USD;
        BigDecimal rate = new BigDecimal("1.18");
        LocalDate date = LocalDate.now();

        // When
        ExchangeRate exchangeRate = new ExchangeRate(from, to, rate, date);

        // Then
        assertNotNull(exchangeRate);
        assertEquals(from, exchangeRate.getFromCurrency());
        assertEquals(to, exchangeRate.getToCurrency());
        assertEquals(rate, exchangeRate.getRate());
        assertEquals(date, exchangeRate.getDate());
    }

    @Test
    void shouldThrowExceptionWhenCurrenciesAreNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new ExchangeRate(null, Currency.USD, BigDecimal.ONE, LocalDate.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
            new ExchangeRate(Currency.EUR, null, BigDecimal.ONE, LocalDate.now())
        );
    }

    @Test
    void shouldThrowExceptionWhenRateIsNegative() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal("-1.0"), LocalDate.now())
        );
    }

    @Test
    void shouldThrowExceptionWhenRateIsZero() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new ExchangeRate(Currency.EUR, Currency.USD, BigDecimal.ZERO, LocalDate.now())
        );
    }

    @Test
    void shouldThrowExceptionWhenSameCurrency() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            new ExchangeRate(Currency.EUR, Currency.EUR, BigDecimal.ONE, LocalDate.now())
        );
    }

    @Test
    void shouldConvertMoney() {
        // Given
        ExchangeRate exchangeRate = new ExchangeRate(
            Currency.EUR,
            Currency.USD,
            new BigDecimal("1.18"),
            LocalDate.now()
        );
        Money euros = new Money(new BigDecimal("100.00"), Currency.EUR);

        // When
        Money dollars = exchangeRate.convert(euros);

        // Then
        assertEquals(Currency.USD, dollars.getCurrency());
        assertEquals(0, new BigDecimal("118.00").compareTo(dollars.getAmount()));
    }

    @Test
    void shouldThrowExceptionWhenConvertingWrongCurrency() {
        // Given
        ExchangeRate exchangeRate = new ExchangeRate(
            Currency.EUR,
            Currency.USD,
            new BigDecimal("1.18"),
            LocalDate.now()
        );
        Money pounds = new Money(new BigDecimal("100.00"), Currency.GBP);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            exchangeRate.convert(pounds)
        );
    }

    @Test
    void shouldCreateInverseExchangeRate() {
        // Given
        ExchangeRate eurToUsd = new ExchangeRate(
            Currency.EUR,
            Currency.USD,
            new BigDecimal("1.20"),
            LocalDate.now()
        );

        // When
        ExchangeRate usdToEur = eurToUsd.inverse();

        // Then
        assertEquals(Currency.USD, usdToEur.getFromCurrency());
        assertEquals(Currency.EUR, usdToEur.getToCurrency());
        assertTrue(usdToEur.getRate().compareTo(new BigDecimal("0.83")) > 0);
        assertTrue(usdToEur.getRate().compareTo(new BigDecimal("0.84")) < 0);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        // Given
        LocalDate date = LocalDate.now();
        ExchangeRate rate1 = new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal("1.18"), date);
        ExchangeRate rate2 = new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal("1.18"), date);

        // Then
        assertEquals(rate1, rate2);
        assertEquals(rate1.hashCode(), rate2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        // Given
        LocalDate date = LocalDate.now();
        ExchangeRate rate1 = new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal("1.18"), date);
        ExchangeRate rate2 = new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal("1.20"), date);

        // Then
        assertNotEquals(rate1, rate2);
    }
}

