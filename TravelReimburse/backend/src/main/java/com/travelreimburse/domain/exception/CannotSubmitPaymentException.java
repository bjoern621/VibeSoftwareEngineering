package com.travelreimburse.domain.exception;

/**
 * Exception wenn Payment nicht übermittelt werden kann
 */
public class CannotSubmitPaymentException extends RuntimeException {

    private final String reason;

    public CannotSubmitPaymentException(Long paymentId, String reason) {
        super(String.format("Payment %d kann nicht übermittelt werden: %s", paymentId, reason));
        this.reason = reason;
    }

    public CannotSubmitPaymentException(String reason) {
        super(reason);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}

