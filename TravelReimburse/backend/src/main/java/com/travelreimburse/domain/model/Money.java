package com.travelreimburse.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object für Geldbeträge (unveränderlich)
 * Verhindert Rundungsfehler durch Verwendung von BigDecimal
 * 
 * ✅ DDD: Value Object - immutable (final fields)
 */
@Embeddable
public class Money {
    
    private final BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private final Currency currency;
    
    // JPA benötigt Default-Konstruktor (nur für Framework-Zugriff)
    // ✅ DDD: Final fields werden hier mit null initialisiert (JPA setzt echte Werte via Reflection)
    protected Money() {
        this.amount = null;
        this.currency = null;
    }
    
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Betrag und Währung dürfen nicht null sein");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Betrag darf nicht negativ sein");
        }
        this.amount = amount;
        this.currency = currency;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Addiert zwei Geldbeträge (müssen gleiche Währung haben)
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Währungen müssen übereinstimmen: %s != %s",
                    this.currency, other.currency)
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtrahiert zwei Geldbeträge (müssen gleiche Währung haben)
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Währungen müssen übereinstimmen: %s != %s",
                    this.currency, other.currency)
            );
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ergebnis darf nicht negativ sein");
        }
        return new Money(result, this.currency);
    }

    /**
     * Multipliziert den Betrag mit einem Faktor
     */
    public Money multiply(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Faktor darf nicht negativ sein");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }

    /**
     * Prüft ob dieser Betrag größer ist als der andere
     */
    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Kann nur Beträge gleicher Währung vergleichen");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Prüft ob dieser Betrag kleiner ist als der andere
     */
    public boolean isLessThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Kann nur Beträge gleicher Währung vergleichen");
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Prüft ob der Betrag Null ist
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Prüft ob der Betrag positiv ist (größer als Null)
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Prüft ob der Betrag größer oder gleich dem anderen ist
     */
    public boolean isGreaterThanOrEqual(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Kann nur Beträge gleicher Währung vergleichen");
        }
        return this.amount.compareTo(other.amount) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && currency == money.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency);
    }
}

