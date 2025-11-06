package com.travelreimburse.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit Tests für FlexibleBigDecimalDeserializer
 * Testet beide Dezimalformate: "20.00" und "20,00"
 */
class FlexibleBigDecimalDeserializerTest {

    private FlexibleBigDecimalDeserializer deserializer;
    private JsonParser parser;
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        deserializer = new FlexibleBigDecimalDeserializer();
        parser = Mockito.mock(JsonParser.class);
        context = Mockito.mock(DeserializationContext.class);
    }

    @Test
    void shouldDeserializeUSFormat() throws IOException {
        // Given - US-Format mit Punkt als Dezimaltrennzeichen
        when(parser.getText()).thenReturn("20.00");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("20.00").compareTo(result));
    }

    @Test
    void shouldDeserializeEuropeanFormat() throws IOException {
        // Given - Europäisches Format mit Komma als Dezimaltrennzeichen
        when(parser.getText()).thenReturn("20,00");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("20.00").compareTo(result));
    }

    @Test
    void shouldDeserializeGermanFormatWithThousandsSeparator() throws IOException {
        // Given - Deutsches Format: 1.234,56
        when(parser.getText()).thenReturn("1.234,56");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("1234.56").compareTo(result));
    }

    @Test
    void shouldDeserializeUSFormatWithThousandsSeparator() throws IOException {
        // Given - US-Format: 1,234.56
        when(parser.getText()).thenReturn("1,234.56");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("1234.56").compareTo(result));
    }

    @Test
    void shouldDeserializeIntegerWithoutDecimal() throws IOException {
        // Given - Ganzzahl ohne Dezimalstelle
        when(parser.getText()).thenReturn("100");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("100").compareTo(result));
    }

    @Test
    void shouldDeserializeSmallDecimalWithDot() throws IOException {
        // Given
        when(parser.getText()).thenReturn("0.99");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("0.99").compareTo(result));
    }

    @Test
    void shouldDeserializeSmallDecimalWithComma() throws IOException {
        // Given
        when(parser.getText()).thenReturn("0,99");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("0.99").compareTo(result));
    }

    @Test
    void shouldDeserializeLargeNumberWithComma() throws IOException {
        // Given - Große Zahl im europäischen Format
        when(parser.getText()).thenReturn("10.000,50");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("10000.50").compareTo(result));
    }

    @Test
    void shouldDeserializeLargeNumberWithDot() throws IOException {
        // Given - Große Zahl im US-Format
        when(parser.getText()).thenReturn("10,000.50");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("10000.50").compareTo(result));
    }

    @Test
    void shouldHandleWhitespace() throws IOException {
        // Given - Mit Leerzeichen
        when(parser.getText()).thenReturn("  20.00  ");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertEquals(0, new BigDecimal("20.00").compareTo(result));
    }

    @Test
    void shouldReturnNullForEmptyString() throws IOException {
        // Given
        when(parser.getText()).thenReturn("");

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertNull(result);
    }

    @Test
    void shouldReturnNullForNull() throws IOException {
        // Given
        when(parser.getText()).thenReturn(null);

        // When
        BigDecimal result = deserializer.deserialize(parser, context);

        // Then
        assertNull(result);
    }

    @Test
    void shouldThrowExceptionForInvalidFormat() throws IOException {
        // Given - Ungültiges Format
        when(parser.getText()).thenReturn("abc.def");

        // When & Then
        assertThrows(IOException.class, () ->
            deserializer.deserialize(parser, context)
        );
    }
}

