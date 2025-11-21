package com.rentacar;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-Sicherheitskonfiguration, die alle Endpoints ohne Authentifizierung zugänglich macht.
 * 
 * Diese Konfiguration wird in Controller-Tests verwendet, um die eigentliche Security-Konfiguration
 * zu überschreiben und alle Endpoints öffentlich zu machen.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .headers(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
