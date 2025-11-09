package com.travelreimburse.infrastructure.external.easypay;

/**
 * Exception für EasyPay Integration Fehler.
 * Wird geworfen wenn die Kommunikation mit EasyPay fehlschlägt.
 */
public class EasyPayException extends RuntimeException {

    public EasyPayException(String message) {
        super(message);
    }

    public EasyPayException(String message, Throwable cause) {
        super(message, cause);
    }
}

