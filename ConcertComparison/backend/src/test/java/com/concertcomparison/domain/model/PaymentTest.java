package com.concertcomparison.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für Payment Entity.
 * 
 * Testet Payment-Lifecycle und Status-Transitionen.
 * HINWEIS: Payment wird normalerweise nur über Order verwaltet,
 * aber wir testen die Business Logic isoliert.
 */
@DisplayName("Payment Entity Tests")
class PaymentTest {

    private static final Double AMOUNT = 149.99;

    @Nested
    @DisplayName("Factory Method - createPayment()")
    class CreatePaymentTests {

        @Test
        @DisplayName("Sollte Payment erfolgreich erstellen")
        void shouldCreatePaymentSuccessfully() {
            // When
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            // Then
            assertThat(payment).isNotNull();
            assertThat(payment.getAmount()).isEqualTo(AMOUNT);
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getTransactionId()).isNull();
            assertThat(payment.getCreatedAt()).isNotNull();
            assertThat(payment.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn Amount null ist")
        void shouldThrowExceptionWhenAmountIsNull() {
            // When & Then
            assertThatThrownBy(() -> Payment.createPayment(null, PaymentMethod.PAYPAL))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Amount darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn Amount negativ ist")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // When & Then
            assertThatThrownBy(() -> Payment.createPayment(-50.0, PaymentMethod.PAYPAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount muss positiv sein");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn PaymentMethod null ist")
        void shouldThrowExceptionWhenPaymentMethodIsNull() {
            // When & Then
            assertThatThrownBy(() -> Payment.createPayment(AMOUNT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("PaymentMethod darf nicht null sein");
        }
    }

    @Nested
    @DisplayName("Business Method - complete()")
    class CompletePaymentTests {

        @Test
        @DisplayName("Sollte Payment erfolgreich abschließen")
        void shouldCompletePaymentSuccessfully() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            String transactionId = "TXN-VISA-12345";

            // When
            payment.complete(transactionId);

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getTransactionId()).isEqualTo(transactionId);
            assertThat(payment.isSuccessful()).isTrue();
            assertThat(payment.isFailed()).isFalse();
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Payment nicht PENDING ist")
        void shouldThrowExceptionWhenPaymentNotPending() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.PAYPAL);
            payment.complete("TXN-1");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // When & Then
            assertThatThrownBy(() -> payment.complete("TXN-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nur PENDING Payments können abgeschlossen werden");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn TransactionId null ist")
        void shouldThrowExceptionWhenTransactionIdIsNull() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            // When & Then
            assertThatThrownBy(() -> payment.complete(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TransactionId darf nicht null sein");
        }
    }

    @Nested
    @DisplayName("Business Method - fail()")
    class FailPaymentTests {

        @Test
        @DisplayName("Sollte Payment als fehlgeschlagen markieren")
        void shouldFailPaymentSuccessfully() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            // When
            payment.fail();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.isFailed()).isTrue();
            assertThat(payment.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Payment nicht PENDING ist")
        void shouldThrowExceptionWhenPaymentNotPending() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.PAYPAL);
            payment.complete("TXN-123");

            // When & Then
            assertThatThrownBy(() -> payment.fail())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nur PENDING Payments können fehlschlagen");
        }
    }

    @Nested
    @DisplayName("Business Method - cancel()")
    class CancelPaymentTests {

        @Test
        @DisplayName("Sollte PENDING Payment stornieren")
        void shouldCancelPendingPayment() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.SOFORT);

            // When
            payment.cancel();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.isFailed()).isTrue();
        }

        @Test
        @DisplayName("Sollte COMPLETED Payment stornieren")
        void shouldCancelCompletedPayment() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            payment.complete("TXN-CANCEL-123");

            // When
            payment.cancel();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Payment bereits CANCELLED ist")
        void shouldThrowExceptionWhenPaymentAlreadyCancelled() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.PAYPAL);
            payment.cancel();

            // When & Then
            assertThatThrownBy(() -> payment.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bereits");
        }
    }

    @Nested
    @DisplayName("Business Method - refund()")
    class RefundPaymentTests {

        @Test
        @DisplayName("Sollte COMPLETED Payment erstatten")
        void shouldRefundCompletedPayment() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            payment.complete("TXN-REFUND-456");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // When
            payment.refund();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Payment nicht COMPLETED ist")
        void shouldThrowExceptionWhenPaymentNotCompleted() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.PAYPAL);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nur COMPLETED Payments können erstattet werden");
        }
    }

    @Nested
    @DisplayName("Payment Status Transitions")
    class PaymentStatusTransitionsTests {

        @Test
        @DisplayName("Sollte erfolgreichen Payment-Lifecycle durchlaufen: PENDING → COMPLETED")
        void shouldCompleteSuccessfulPaymentLifecycle() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // When
            payment.complete("TXN-SUCCESS-789");

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("Sollte fehlgeschlagenen Payment-Lifecycle durchlaufen: PENDING → FAILED")
        void shouldCompleteFailedPaymentLifecycle() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.PAYPAL);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            // When
            payment.fail();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.isFailed()).isTrue();
        }

        @Test
        @DisplayName("Sollte Refund-Lifecycle durchlaufen: PENDING → COMPLETED → REFUNDED")
        void shouldCompleteRefundLifecycle() {
            // Given
            Payment payment = Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            
            // When - Complete
            payment.complete("TXN-REFUND-999");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            
            // When - Refund
            payment.refund();

            // Then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("Payment Methods")
    class PaymentMethodsTests {

        @Test
        @DisplayName("Sollte alle Payment Methods unterstützen")
        void shouldSupportAllPaymentMethods() {
            // When & Then
            assertThatCode(() -> Payment.createPayment(AMOUNT, PaymentMethod.CREDIT_CARD)).doesNotThrowAnyException();
            assertThatCode(() -> Payment.createPayment(AMOUNT, PaymentMethod.PAYPAL)).doesNotThrowAnyException();
            assertThatCode(() -> Payment.createPayment(AMOUNT, PaymentMethod.SOFORT)).doesNotThrowAnyException();
            assertThatCode(() -> Payment.createPayment(AMOUNT, PaymentMethod.BANK_TRANSFER)).doesNotThrowAnyException();
            assertThatCode(() -> Payment.createPayment(AMOUNT, PaymentMethod.APPLE_PAY)).doesNotThrowAnyException();
            assertThatCode(() -> Payment.createPayment(AMOUNT, PaymentMethod.GOOGLE_PAY)).doesNotThrowAnyException();
        }
    }
}
