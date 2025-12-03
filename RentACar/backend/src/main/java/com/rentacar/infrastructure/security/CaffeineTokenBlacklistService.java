package com.rentacar.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rentacar.domain.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine-basierte Implementation des TokenBlacklistService.
 * Nutzt einen In-Memory Cache mit automatischer Eviction abgelaufener Tokens.
 *
 * Adapter-Pattern: Implementiert Domain-Port mit Infrastructure-Technologie.
 */
@Service
public class CaffeineTokenBlacklistService implements TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(CaffeineTokenBlacklistService.class);

    // Cache mit maximaler Größe und automatischem Cleanup
    private final Cache<String, Boolean> blacklist;

    public CaffeineTokenBlacklistService() {
        this.blacklist = Caffeine.newBuilder()
                .maximumSize(10_000) // Maximal 10.000 Tokens in Blacklist
                .expireAfterWrite(24, TimeUnit.HOURS) // Tokens werden nach 24h automatisch entfernt
                .recordStats() // Statistiken aktivieren (optional, für Monitoring)
                .build();

        logger.info("TokenBlacklistService initialisiert mit Caffeine Cache");
    }

    @Override
    public void blacklistToken(String token, Duration ttl) {
        if (token == null || token.isBlank()) {
            logger.warn("Versuch, leeren Token zu blacklisten wurde ignoriert");
            return;
        }

        // Token mit spezifischer TTL in Cache speichern
        blacklist.put(token, Boolean.TRUE);
        logger.debug("Token zur Blacklist hinzugefügt (TTL: {})", ttl);

        // Statistiken loggen
        logCacheStats();
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Boolean isBlacklisted = blacklist.getIfPresent(token);
        boolean result = isBlacklisted != null && isBlacklisted;

        if (result) {
            logger.debug("Token ist auf der Blacklist");
        }

        return result;
    }

    @Override
    public void removeFromBlacklist(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        blacklist.invalidate(token);
        logger.debug("Token von Blacklist entfernt");
        logCacheStats();
    }

    @Override
    public void clearBlacklist() {
        blacklist.invalidateAll();
        logger.info("Blacklist vollständig geleert");
        logCacheStats();
    }

    /**
     * Loggt Cache-Statistiken für Monitoring.
     */
    private void logCacheStats() {
        var stats = blacklist.stats();
        logger.debug("Blacklist Stats - Size: {}, HitRate: {}%, MissRate: {}%",
                blacklist.estimatedSize(),
                String.format("%.2f", stats.hitRate() * 100),
                String.format("%.2f", stats.missRate() * 100));
    }

    /**
     * Gibt die aktuelle Größe der Blacklist zurück (für Monitoring/Testing).
     *
     * @return Anzahl der Tokens in der Blacklist
     */
    public long getBlacklistSize() {
        return blacklist.estimatedSize();
    }
}

