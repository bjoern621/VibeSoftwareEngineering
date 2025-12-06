package com.rentacar.domain.repository;

import com.rentacar.domain.model.RefreshToken;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für RefreshToken Aggregate.
 * Port für die Infrastructure Layer Implementation.
 */
public interface RefreshTokenRepository {

    /**
     * Speichert einen Refresh Token.
     *
     * @param refreshToken zu speichernder Refresh Token
     * @return gespeicherter Refresh Token mit ID
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Sucht einen Refresh Token anhand seines Token-Strings.
     *
     * @param token Token-String
     * @return Optional mit RefreshToken, falls gefunden
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Sucht alle gültigen (nicht widerrufenen) Refresh Tokens eines Kunden.
     *
     * @param customerId Kunden-ID
     * @return Liste aller gültigen Refresh Tokens des Kunden
     */
    List<RefreshToken> findByCustomerIdAndRevokedFalse(Long customerId);

    /**
     * Löscht alle abgelaufenen und widerrufenen Tokens.
     * Kann für Aufräumarbeiten verwendet werden.
     */
    void deleteExpiredAndRevokedTokens();

    /**
     * Widerruft alle Refresh Tokens eines Kunden.
     * Nützlich beim Logout oder bei Sicherheitsvorfällen.
     *
     * @param customerId Kunden-ID
     */
    void revokeAllTokensByCustomerId(Long customerId);

    /**
     * Löscht einen Refresh Token.
     *
     * @param refreshToken zu löschender Refresh Token
     */
    void delete(RefreshToken refreshToken);
}

