package com.travelreimburse.infrastructure.external.easypay;

/**
 * Exception für EasyPay Integration Fehler
 */
public class EasyPayException extends Exception {

    private final String errorCode;

    public EasyPayException(String message) {
        super(message);
        this.errorCode = "EASYPAY_ERROR";
    }

    public EasyPayException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public EasyPayException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "EASYPAY_ERROR";
    }

    public EasyPayException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

