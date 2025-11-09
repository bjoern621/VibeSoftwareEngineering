package com.travelreimburse.domain.exception;

/**
 * Domain Exception für ungültige Beleg-Daten.
 * DDD: Spezifische Domain Exception statt generischer IllegalArgumentException.
 */
public class InvalidReceiptDataException extends RuntimeException {

    private final String fieldName;
    private final Object invalidValue;

    public InvalidReceiptDataException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
    }

    public InvalidReceiptDataException(String fieldName, Object invalidValue, String reason) {
        super(String.format("Ungültiger Wert für %s: %s - %s", fieldName, invalidValue, reason));
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}

