package com.travelreimburse.domain.service;

import com.travelreimburse.domain.model.Currency;
import com.travelreimburse.domain.model.ExchangeRate;
import com.travelreimburse.domain.model.Money;
import com.travelreimburse.infrastructure.external.exrat.ExRatClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für CurrencyConversionService
 */
@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    @Mock
    private ExRatClient exRatClient;

    @InjectMocks
    private CurrencyConversionService conversionService;

    private ExchangeRate testExchangeRate;

    @BeforeEach
    void setUp() {
        testExchangeRate = new ExchangeRate(
            Currency.EUR,
            Currency.USD,
            new BigDecimal("1.18"),
            LocalDate.now()
        );
    }

    @Test
    void shouldConvertCurrency() {
        // Given
        Money euros = new Money(new BigDecimal("100.00"), Currency.EUR);
        LocalDate date = LocalDate.now();

        when(exRatClient.getExchangeRate(Currency.EUR, Currency.USD, date))
            .thenReturn(testExchangeRate);

        // When
        Money dollars = conversionService.convert(euros, Currency.USD, date);

        // Then
        assertNotNull(dollars);
        assertEquals(Currency.USD, dollars.getCurrency());
        assertEquals(0, new BigDecimal("118.00").compareTo(dollars.getAmount()));

        verify(exRatClient, times(1)).getExchangeRate(Currency.EUR, Currency.USD, date);
    }

    @Test
    void shouldReturnSameMoneyWhenSameCurrency() {
        // Given
        Money euros = new Money(new BigDecimal("100.00"), Currency.EUR);

        // When
        Money result = conversionService.convert(euros, Currency.EUR, LocalDate.now());

        // Then
        assertSame(euros, result);
        verify(exRatClient, never()).getExchangeRate(any(), any(), any());
    }

    @Test
    void shouldConvertWithCurrentDateWhenDateNotProvided() {
        // Given
        Money euros = new Money(new BigDecimal("100.00"), Currency.EUR);

        when(exRatClient.getExchangeRate(eq(Currency.EUR), eq(Currency.USD), any(LocalDate.class)))
            .thenReturn(testExchangeRate);

        // When
        Money dollars = conversionService.convert(euros, Currency.USD);

        // Then
        assertNotNull(dollars);
        assertEquals(Currency.USD, dollars.getCurrency());
        verify(exRatClient, times(1)).getExchangeRate(eq(Currency.EUR), eq(Currency.USD), any(LocalDate.class));
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            conversionService.convert(null, Currency.USD, LocalDate.now())
        );
    }

    @Test
    void shouldThrowExceptionWhenTargetCurrencyIsNull() {
        // Given
        Money euros = new Money(new BigDecimal("100.00"), Currency.EUR);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            conversionService.convert(euros, null, LocalDate.now())
        );
    }

    @Test
    void shouldThrowExceptionWhenDateIsNull() {
        // Given
        Money euros = new Money(new BigDecimal("100.00"), Currency.EUR);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            conversionService.convert(euros, Currency.USD, null)
        );
    }

    @Test
    void shouldGetExchangeRate() {
        // Given
        LocalDate date = LocalDate.now();
        when(exRatClient.getExchangeRate(Currency.EUR, Currency.USD, date))
            .thenReturn(testExchangeRate);

        // When
        ExchangeRate result = conversionService.getExchangeRate(Currency.EUR, Currency.USD, date);

        // Then
        assertNotNull(result);
        assertEquals(Currency.EUR, result.getFromCurrency());
        assertEquals(Currency.USD, result.getToCurrency());
        assertEquals(new BigDecimal("1.18"), result.getRate());

        verify(exRatClient, times(1)).getExchangeRate(Currency.EUR, Currency.USD, date);
    }

    @Test
    void shouldGetExchangeRateWithCurrentDate() {
        // Given
        when(exRatClient.getExchangeRate(eq(Currency.EUR), eq(Currency.USD), any(LocalDate.class)))
            .thenReturn(testExchangeRate);

        // When
        ExchangeRate result = conversionService.getExchangeRate(Currency.EUR, Currency.USD);

        // Then
        assertNotNull(result);
        verify(exRatClient, times(1)).getExchangeRate(eq(Currency.EUR), eq(Currency.USD), any(LocalDate.class));
    }
}

