package com.rentacar.infrastructure.security;

import com.rentacar.domain.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 * Interceptiert HTTP-Requests und validiert JWT-Token im Authorization-Header.
 * 
 * Unterscheidet zwischen:
 * - Token fehlt: Filter-Chain fortsetzen (könnte public endpoint sein)
 * - Token abgelaufen/ungültig: 401 zurückgeben und Filter-Chain STOPPEN
 * - Token gültig: Authentication setzen und Filter-Chain fortsetzen
 * 
 * Prüft zusätzlich die Token-Blacklist für ausgeloggte Tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomerUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomerUserDetailsService userDetailsService,
                                   TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        // 1. Extrahiere JWT-Token aus Authorization-Header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            // 2. Prüfe ob Token auf Blacklist steht (ausgeloggt)
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                logger.warn("Token ist auf der Blacklist (Benutzer wurde ausgeloggt)");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token wurde widerrufen");
                return; // Filter-Chain STOPPEN
            }

            try {
                // 3. Prüfe ob Token abgelaufen ist
                if (jwtUtil.isTokenExpired(jwt)) {
                    logger.warn("JWT-Token ist abgelaufen");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT-Token ist abgelaufen");
                    return; // Filter-Chain STOPPEN
                }

                // 4. Extrahiere Email aus Token
                email = jwtUtil.extractEmail(jwt);

            } catch (ExpiredJwtException e) {
                logger.warn("JWT-Token ist abgelaufen: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT-Token ist abgelaufen");
                return; // Filter-Chain STOPPEN
            } catch (JwtException e) {
                logger.error("JWT-Token ist ungültig: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT-Token ist ungültig");
                return; // Filter-Chain STOPPEN
            } catch (Exception e) {
                logger.error("JWT-Token konnte nicht verarbeitet werden: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT-Token konnte nicht verarbeitet werden");
                return; // Filter-Chain STOPPEN
            }
        }

        // 5. Validiere Token und setze Authentication (nur wenn Token vorhanden und gültig)
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (Boolean.TRUE.equals(jwtUtil.validateToken(jwt, userDetails))) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                jwt, // Token als credentials speichern für extractCustomerId
                                userDetails.getAuthorities()
                        );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                // Token validierung fehlgeschlagen
                logger.warn("JWT-Token Validierung fehlgeschlagen");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT-Token Validierung fehlgeschlagen");
                return; // Filter-Chain STOPPEN
            }
        }

        // 6. Filter-Chain fortsetzen (entweder Authentication gesetzt oder kein Token → public endpoint)
        filterChain.doFilter(request, response);
    }
}
