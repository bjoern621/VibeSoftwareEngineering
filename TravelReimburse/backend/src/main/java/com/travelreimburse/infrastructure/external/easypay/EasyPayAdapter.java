package com.travelreimburse.infrastructure.external.easypay;

import com.travelreimburse.domain.model.PaymentRequest;

/**
 * Adapter Interface für EasyPay externe Integration.
 * Abstrahiert die externe API - Implementierung kann gemockt oder real sein.
 */
public interface EasyPayAdapter {

    /**
     * Submits a payment to EasyPay
     *
     * @param paymentRequest the payment request to submit
     * @return EasyPayResponse mit Transaction-ID und Status
     * @throws EasyPayException bei Fehler
     */
    EasyPayResponse submitPayment(PaymentRequest paymentRequest) throws EasyPayException;

    /**
     * Checks the status of a payment in EasyPay
     *
     * @param easyPayTransactionId the transaction ID from EasyPay
     * @return the current payment status
     * @throws EasyPayException bei Fehler
     */
    EasyPayPaymentStatus checkPaymentStatus(String easyPayTransactionId) throws EasyPayException;

    /**
     * Record für EasyPay Response
     */
    record EasyPayResponse(
        String transactionId,
        String status,
        String message,
        long timestamp
    ) {}

    /**
     * Enum für EasyPay Payment Status
     */
    enum EasyPayPaymentStatus {
        PENDING, PROCESSING, SUCCESS, FAILED
    }
}

