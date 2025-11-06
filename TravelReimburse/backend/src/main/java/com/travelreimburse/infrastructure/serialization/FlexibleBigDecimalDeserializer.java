package com.travelreimburse.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Custom Deserializer für BigDecimal der beide Dezimaltrennzeichen akzeptiert
 * Akzeptiert: "20.00" (US-Format) und "20,00" (EU-Format)
 */
public class FlexibleBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim();

        try {
            // Versuche zunächst Standard-Parsing (für numerische Werte ohne Quotes)
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            // Falls Standard-Parsing fehlschlägt, versuche flexible Parsing
            return parseFlexible(value);
        }
    }

    /**
     * Flexibles Parsing das beide Formate akzeptiert:
     * - "20.00" (Punkt als Dezimaltrennzeichen)
     * - "20,00" (Komma als Dezimaltrennzeichen)
     * - "1.234,56" (Deutsches Format mit Tausendertrennzeichen)
     * - "1,234.56" (US-Format mit Tausendertrennzeichen)
     */
    private BigDecimal parseFlexible(String value) throws IOException {
        // Entferne Leerzeichen
        value = value.replace(" ", "");

        // Bestimme welches Zeichen als Dezimaltrennzeichen verwendet wird
        int lastComma = value.lastIndexOf(',');
        int lastDot = value.lastIndexOf('.');

        DecimalFormatSymbols symbols;

        if (lastComma > lastDot) {
            // Komma kommt nach Punkt -> Komma ist Dezimaltrennzeichen (z.B. "1.234,56")
            symbols = new DecimalFormatSymbols(Locale.GERMAN);
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');
        } else if (lastDot > lastComma) {
            // Punkt kommt nach Komma -> Punkt ist Dezimaltrennzeichen (z.B. "1,234.56")
            symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
        } else if (lastComma >= 0) {
            // Nur Komma vorhanden (z.B. "20,00")
            symbols = new DecimalFormatSymbols(Locale.GERMAN);
            symbols.setDecimalSeparator(',');
        } else if (lastDot >= 0) {
            // Nur Punkt vorhanden (z.B. "20.00")
            symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setDecimalSeparator('.');
        } else {
            // Keine Dezimalstelle (z.B. "20")
            symbols = new DecimalFormatSymbols(Locale.US);
        }

        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setParseBigDecimal(true);

        try {
            return (BigDecimal) format.parse(value);
        } catch (ParseException e) {
            throw new IOException("Ungültiges Zahlenformat: " + value + ". Verwende entweder '20.00' oder '20,00'", e);
        }
    }
}

