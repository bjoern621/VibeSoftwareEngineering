package com.travelreimburse.infrastructure.external.exrat;

import com.travelreimburse.domain.model.Currency;
import com.travelreimburse.domain.model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Client für ExRat-API (externer Währungskurs-Service)
 * Holt aktuelle und historische Wechselkurse
 */
@Service
public class ExRatClient {

    private static final Logger logger = LoggerFactory.getLogger(ExRatClient.class);

    private final RestClient restClient;
    private final String apiUrl;

    public ExRatClient(
        RestClient.Builder restClientBuilder,
        @Value("${exrat.api.url:https://api.exchangerate-api.com/v4/latest}") String apiUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.apiUrl = apiUrl;
    }

    /**
     * Holt den aktuellen Wechselkurs zwischen zwei Währungen
     * Ergebnis wird gecacht für bessere Performance
     */
    @Cacheable(value = "exchangeRates", key = "#from + '_' + #to + '_' + #date")
    public ExchangeRate getExchangeRate(Currency from, Currency to, LocalDate date) {
        if (from == to) {
            // Gleiche Währung = Kurs 1.0
            return new ExchangeRate(from, to, BigDecimal.ONE, date);
        }

        try {
            logger.info("Hole Wechselkurs {} -> {} für {}", from, to, date);

            // API-Call zu ExRat
            String url = String.format("%s/%s", apiUrl, from.name());
            ExRatResponse response = restClient.get()
                .uri(url)
                .retrieve()
                .body(ExRatResponse.class);

            if (response == null || response.rates() == null) {
                throw new ExRatClientException("Ungültige Antwort von ExRat-API");
            }

            // Rate für Zielwährung extrahieren
            BigDecimal rate = response.rates().get(to.name());
            if (rate == null) {
                throw new ExRatClientException(
                    String.format("Kein Wechselkurs für %s -> %s verfügbar", from, to)
                );
            }

            // Verwende aktuelles Datum wenn API-Datum nicht vorhanden
            LocalDate effectiveDate = response.date() != null ? response.date() : date;

            ExchangeRate exchangeRate = new ExchangeRate(from, to, rate, effectiveDate);
            logger.info("Wechselkurs erfolgreich geholt: {}", exchangeRate);

            return exchangeRate;

        } catch (RestClientException e) {
            logger.error("Fehler beim Abrufen des Wechselkurses von ExRat: {}", e.getMessage());
            throw new ExRatClientException("Fehler bei ExRat-API-Aufruf: " + e.getMessage(), e);
        }
    }

    /**
     * Holt den aktuellen Wechselkurs (für heutiges Datum)
     */
    public ExchangeRate getExchangeRate(Currency from, Currency to) {
        return getExchangeRate(from, to, LocalDate.now());
    }

    /**
     * Holt alle verfügbaren Wechselkurse für eine Basis-Währung
     */
    @Cacheable(value = "allExchangeRates", key = "#baseCurrency + '_' + #date")
    public Map<String, BigDecimal> getAllRates(Currency baseCurrency, LocalDate date) {
        try {
            logger.info("Hole alle Wechselkurse für {} am {}", baseCurrency, date);

            String url = String.format("%s/%s", apiUrl, baseCurrency.name());
            ExRatResponse response = restClient.get()
                .uri(url)
                .retrieve()
                .body(ExRatResponse.class);

            if (response == null || response.rates() == null) {
                throw new ExRatClientException("Ungültige Antwort von ExRat-API");
            }

            logger.info("Erfolgreich {} Wechselkurse für {} geholt",
                response.rates().size(), baseCurrency);

            return response.rates();

        } catch (RestClientException e) {
            logger.error("Fehler beim Abrufen aller Wechselkurse: {}", e.getMessage());
            throw new ExRatClientException("Fehler bei ExRat-API-Aufruf: " + e.getMessage(), e);
        }
    }
}

