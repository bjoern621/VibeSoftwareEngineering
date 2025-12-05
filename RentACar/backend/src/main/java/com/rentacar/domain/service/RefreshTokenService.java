package com.rentacar.domain.service;

import com.rentacar.domain.model.Customer;
import com.rentacar.domain.model.RefreshToken;
import com.rentacar.domain.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Domain Service für RefreshToken-Verwaltung.
 * Handhabt Erstellung, Validierung und Widerruf von Refresh Tokens.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration}") Long refreshTokenExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Erstellt einen neuen Refresh Token für einen Kunden.
     *
     * @param customer Der Kunde, für den der Token erstellt wird
     * @return Neu erstellter RefreshToken
     */
    @Transactional
    public RefreshToken createRefreshToken(Customer customer) {
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);
        RefreshToken refreshToken = new RefreshToken(customer, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Sucht einen Refresh Token anhand seines Token-Strings.
     *
     * @param token Token-String
     * @return Optional mit RefreshToken, falls gefunden
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Validiert einen Refresh Token.
     * Prüft, ob Token existiert, nicht abgelaufen und nicht widerrufen ist.
     *
     * @param token Token-String
     * @return RefreshToken wenn gültig
     * @throws RefreshTokenException wenn Token ungültig ist
     */
    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException("Refresh Token nicht gefunden"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh Token ist abgelaufen");
        }

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenException("Refresh Token wurde widerrufen");
        }

        return refreshToken;
    }

    /**
     * Widerruft einen Refresh Token.
     *
     * @param token Token-String
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
        });
    }

    /**
     * Widerruft alle Refresh Tokens eines Kunden.
     * Nützlich beim Logout oder bei Sicherheitsvorfällen.
     *
     * @param customerId Kunden-ID
     */
    @Transactional
    public void revokeAllTokensByCustomerId(Long customerId) {
        refreshTokenRepository.revokeAllTokensByCustomerId(customerId);
    }

    /**
     * Löscht abgelaufene und widerrufene Tokens.
     * Kann als Scheduled-Task ausgeführt werden.
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevokedTokens();
    }

    /**
     * Exception für ungültige Refresh Tokens.
     */
    public static class RefreshTokenException extends RuntimeException {
        public RefreshTokenException(String message) {
            super(message);
        }
    }
}

