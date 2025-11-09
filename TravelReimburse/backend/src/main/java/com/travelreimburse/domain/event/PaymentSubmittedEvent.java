package com.travelreimburse.domain.event;

import java.time.LocalDateTime;

/**
 * Domain Event - wird publishet wenn Payment zu EasyPay übermittelt wird
 */
public record PaymentSubmittedEvent(
    Long paymentId,
    Long travelRequestId,
    String paymentReference,
    LocalDateTime timestamp
) {}

