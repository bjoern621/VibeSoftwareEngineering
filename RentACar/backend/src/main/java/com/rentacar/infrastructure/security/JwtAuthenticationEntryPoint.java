package com.rentacar.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point.
 * 
 * Wird aufgerufen wenn ein protected Endpoint ohne gültige Authentication aufgerufen wird.
 * Gibt 401 Unauthorized zurück, damit der Frontend-Interceptor den Token-Refresh triggern kann.
 * 
 * Unterscheidung:
 * - 401 Unauthorized: Keine/ungültige Authentication (triggert Frontend-Refresh)
 * - 403 Forbidden: Gültige Authentication, aber fehlende Berechtigung (kein Refresh!)
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Kein gültiger Token bei protected endpoint → 401 Unauthorized
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
            "Authentifizierung erforderlich. Bitte melden Sie sich an oder aktualisieren Sie Ihren Token.");
    }
}
