package com.travelreimburse.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object für Währungskurse
 * Unveränderlich - repräsentiert einen Wechselkurs zwischen zwei Währungen zu einem bestimmten Datum
 */
public class ExchangeRate {

    private final Currency fromCurrency;
    private final Currency toCurrency;
    private final BigDecimal rate;
    private final LocalDate date;

    public ExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, LocalDate date) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Währungen dürfen nicht null sein");
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Wechselkurs muss positiv sein");
        }
        if (date == null) {
            throw new IllegalArgumentException("Datum darf nicht null sein");
        }
        if (fromCurrency == toCurrency) {
            throw new IllegalArgumentException("Quell- und Zielwährung müssen unterschiedlich sein");
        }

        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.date = date;
    }

    /**
     * Rechnet einen Geldbetrag von der Quellwährung in die Zielwährung um
     */
    public Money convert(Money money) {
        if (!money.getCurrency().equals(fromCurrency)) {
            throw new IllegalArgumentException(
                String.format("Geldbetrag muss in %s sein, ist aber in %s",
                    fromCurrency, money.getCurrency())
            );
        }

        BigDecimal convertedAmount = money.getAmount().multiply(rate);
        return new Money(convertedAmount, toCurrency);
    }

    /**
     * Gibt den inversen Wechselkurs zurück (z.B. EUR->USD wird zu USD->EUR)
     */
    public ExchangeRate inverse() {
        BigDecimal inverseRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
        return new ExchangeRate(toCurrency, fromCurrency, inverseRate, date);
    }

    public Currency getFromCurrency() {
        return fromCurrency;
    }

    public Currency getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return fromCurrency == that.fromCurrency &&
               toCurrency == that.toCurrency &&
               Objects.equals(rate, that.rate) &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency, rate, date);
    }

    @Override
    public String toString() {
        return String.format("ExchangeRate{%s -> %s: %s (am %s)}",
            fromCurrency, toCurrency, rate, date);
    }
}

