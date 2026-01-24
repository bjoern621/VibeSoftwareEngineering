package com.concertcomparison.infrastructure.ratelimit;

import com.concertcomparison.domain.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Filter für Rate Limiting pro Client (IP oder User).
 * Wird VOR JwtAuthenticationFilter ausgeführt, um Brute-Force-Angriffe früh zu blockieren.
 * 
 * Verhalten:
 * - Whitelisted IPs: Bypass Rate Limiting
 * - Whitelisted Roles (z.B. ADMIN): Bypass Rate Limiting
 * - Andere: Token Bucket pro IP/User pro Endpoint
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig.RateLimitService rateLimitService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public RateLimitFilter(RateLimitConfig.RateLimitService rateLimitService, HandlerExceptionResolver handlerExceptionResolver) {
        this.rateLimitService = rateLimitService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        // Wenn Rate Limiting deaktiviert ist, direkt fortfahren
        if (!rateLimitService.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String endpoint = rateLimitService.extractEndpoint(request.getRequestURI());

        // Prüfe, ob IP auf Whitelist steht
        if (rateLimitService.isIpWhitelisted(clientIp)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Prüfe, ob die Rolle des Users auf Whitelist steht
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> rateLimitService.isRoleWhitelisted(auth.getAuthority()))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verwende Username wenn authentifiziert, sonst IP
        String clientId = clientIp;
        if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
            clientId = authentication.getName();
        }

        // Überprüfe Rate Limit
        if (!rateLimitService.allowRequest(clientId, endpoint)) {
            long retryAfter = rateLimitService.getRetryAfterSeconds(clientId, endpoint);
            RateLimitExceededException ex = new RateLimitExceededException(
                "Rate limit exceeded. Please try again later.",
                clientId,
                retryAfter
            );
            handlerExceptionResolver.resolveException(request, response, null, ex);
            return;
        }

        // Request erlaubt, weiterleiten
        filterChain.doFilter(request, response);
    }

    /**
     * Extrahiert die Client-IP aus dem Request.
     * Berücksichtigt Proxy-Header wie X-Forwarded-For und CF-Connecting-IP.
     *
     * @param request Der HTTP-Request
     * @return Die Client-IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            return cfConnectingIp;
        }

        String remoteAddr = request.getHeader("X-Real-IP");
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }

        return request.getRemoteAddr();
    }
}
