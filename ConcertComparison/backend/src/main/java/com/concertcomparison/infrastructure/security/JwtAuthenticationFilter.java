package com.concertcomparison.infrastructure.security;

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 * 
 * Intercepted jeden HTTP Request und:
 * 1. Extrahiert JWT Token aus Authorization Header
 * 2. Validiert Token
 * 3. Setzt Spring Security Authentication Context
 * 
 * OncePerRequestFilter stellt sicher, dass Filter pro Request nur einmal ausgeführt wird.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                  CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // 1. JWT Token aus Request extrahieren
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // 2. Username aus Token extrahieren
                String username = jwtTokenProvider.extractUsername(jwt);
                
                // 3. UserDetails laden
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 4. Token validieren
                if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                    // 5. Authentication Object erstellen
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities());
                    
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 6. Security Context setzen
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("JWT Authentication fehlgeschlagen", ex);
        }
        
        // 7. Filter Chain fortsetzen
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extrahiert JWT Token aus Authorization Header.
     * 
     * Format: "Bearer <token>"
     * 
     * @param request HTTP Request
     * @return JWT Token oder null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " entfernen
        }
        
        return null;
    }
}
