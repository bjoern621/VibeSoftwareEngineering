package com.rentacar.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * RefreshToken Entity für JWT Token-Refresh-Mechanismus.
 * Ermöglicht das sichere Erneuern von Access Tokens ohne erneute Anmeldung.
 * Tokens können widerrufen werden (z.B. bei Logout).
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // JPA benötigt einen Default-Konstruktor
    protected RefreshToken() {
    }

    /**
     * Erstellt einen neuen Refresh Token für einen Kunden.
     *
     * @param customer Der Kunde, für den der Token erstellt wird
     * @param expiryDate Das Ablaufdatum des Tokens
     * @throws IllegalArgumentException wenn customer oder expiryDate null sind
     */
    public RefreshToken(Customer customer, LocalDateTime expiryDate) {
        this.customer = Objects.requireNonNull(customer, "Customer darf nicht null sein");
        this.expiryDate = Objects.requireNonNull(expiryDate, "ExpiryDate darf nicht null sein");
        this.token = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.revoked = false;
    }

    /**
     * Widerruft diesen Refresh Token.
     * Ein widerrufener Token kann nicht mehr verwendet werden.
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Prüft, ob der Token abgelaufen ist.
     *
     * @return true wenn der Token abgelaufen ist
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Prüft, ob der Token gültig ist (nicht abgelaufen und nicht widerrufen).
     *
     * @return true wenn der Token gültig ist
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}

