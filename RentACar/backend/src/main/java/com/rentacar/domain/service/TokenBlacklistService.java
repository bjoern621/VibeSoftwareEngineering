package com.rentacar.domain.service;

import java.time.Duration;

/**
 * Domain Service Interface für JWT Token Blacklist.
 * Ermöglicht das Invalidieren von JWT-Tokens beim Logout.
 *
 * Dies ist ein Port im Hexagonalen Architektur-Pattern.
 * Die Implementation befindet sich in der Infrastructure-Schicht.
 */
public interface TokenBlacklistService {

    /**
     * Fügt einen Token zur Blacklist hinzu.
     * Der Token ist ab diesem Zeitpunkt ungültig, auch wenn er noch nicht abgelaufen ist.
     *
     * @param token JWT-Token der invalidiert werden soll
     * @param ttl Time-To-Live (wie lange der Token in der Blacklist bleiben soll)
     */
    void blacklistToken(String token, Duration ttl);

    /**
     * Prüft, ob ein Token auf der Blacklist steht.
     *
     * @param token JWT-Token der geprüft werden soll
     * @return true wenn Token auf Blacklist, false sonst
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Entfernt einen Token von der Blacklist (nur für Testing/Admin-Zwecke).
     *
     * @param token JWT-Token der von der Blacklist entfernt werden soll
     */
    void removeFromBlacklist(String token);

    /**
     * Löscht alle Tokens von der Blacklist (nur für Testing-Zwecke).
     */
    void clearBlacklist();
}

