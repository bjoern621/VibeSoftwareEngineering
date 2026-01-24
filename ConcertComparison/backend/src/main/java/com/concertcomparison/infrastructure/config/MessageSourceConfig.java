package com.concertcomparison.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Konfiguration für Internationalisierung (i18n) und externalisierte Fehlermeldungen.
 * 
 * Ermöglicht Message Sourcing aus properties Dateien in mehreren Sprachen.
 * Übersetzungen:
 * - messages_de.properties (Deutsch)
 * - messages_en.properties (Englisch)
 */
@Configuration
public class MessageSourceConfig {

    /**
     * MessageSource Bean für Zugriff auf externalisierte Messages.
     * 
     * Konfiguration:
     * - Basepath: classpath:messages
     * - Default Encoding: UTF-8
     * - Fallback zur Default Locale wenn Übersetzung nicht vorhanden
     * - Cache Duration: 3600 Sekunden (1 Stunde)
     * 
     * @return Konfigurierter MessageSource für Error Messages
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        messageSource.setFallbackToSystemLocale(true);
        return messageSource;
    }
}
