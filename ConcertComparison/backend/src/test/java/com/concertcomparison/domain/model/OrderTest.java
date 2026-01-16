package com.concertcomparison.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für Order Entity.
 * 
 * Testet Business Logic, Order-Lifecycle und Payment-Integration.
 */
@DisplayName("Order Entity Tests")
class OrderTest {

    private static final Long SEAT_ID = 1L;
    private static final String USER_ID = "user123";
    private static final Double TOTAL_PRICE = 99.99;

    @Nested
    @DisplayName("Factory Method - createOrder()")
    class CreateOrderTests {

        @Test
        @DisplayName("Sollte Order erfolgreich mit Payment erstellen")
        void shouldCreateOrderWithPaymentSuccessfully() {
            // When
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);

            // Then
            assertThat(order).isNotNull();
            assertThat(order.getSeatId()).isEqualTo(SEAT_ID);
            assertThat(order.getUserId()).isEqualTo(USER_ID);
            assertThat(order.getTotalPrice()).isEqualTo(TOTAL_PRICE);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getPurchaseDate()).isNotNull();
            assertThat(order.getPayment()).isNotNull();
            assertThat(order.getPayment().getAmount()).isEqualTo(TOTAL_PRICE);
            assertThat(order.getPayment().getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn SeatId null ist")
        void shouldThrowExceptionWhenSeatIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> Order.createOrder(null, USER_ID, TOTAL_PRICE, PaymentMethod.PAYPAL))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("SeatId darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn UserId null ist")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> Order.createOrder(SEAT_ID, null, TOTAL_PRICE, PaymentMethod.PAYPAL))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("UserId darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn TotalPrice negativ ist")
        void shouldThrowExceptionWhenTotalPriceIsNegative() {
            // When & Then
            assertThatThrownBy(() -> Order.createOrder(SEAT_ID, USER_ID, -10.0, PaymentMethod.PAYPAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TotalPrice muss positiv sein");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn PaymentMethod null ist")
        void shouldThrowExceptionWhenPaymentMethodIsNull() {
            // When & Then
            assertThatThrownBy(() -> Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("PaymentMethod darf nicht null sein");
        }
    }

    @Nested
    @DisplayName("Business Method - confirm()")
    class ConfirmOrderTests {

        @Test
        @DisplayName("Sollte Order bestätigen wenn Payment erfolgreich ist")
        void shouldConfirmOrderWhenPaymentIsSuccessful() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            order.completePayment("TXN-12345");

            // When
            // Payment wurde bereits durch completePayment() completed, Order sollte confirmed sein
            
            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(order.isConfirmed()).isTrue();
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Order nicht PENDING ist")
        void shouldThrowExceptionWhenOrderNotPending() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            order.completePayment("TXN-12345");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            // When & Then
            assertThatThrownBy(() -> order.confirm())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nur PENDING Orders können bestätigt werden");
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Payment nicht erfolgreich ist")
        void shouldThrowExceptionWhenPaymentNotSuccessful() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            // Payment ist noch PENDING

            // When & Then
            assertThatThrownBy(() -> order.confirm())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment erfolgreich ist");
        }
    }

    @Nested
    @DisplayName("Business Method - cancel()")
    class CancelOrderTests {

        @Test
        @DisplayName("Sollte PENDING Order stornieren")
        void shouldCancelPendingOrder() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            // When
            order.cancel();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.isCancelledOrRefunded()).isTrue();
        }

        @Test
        @DisplayName("Sollte CONFIRMED Order stornieren")
        void shouldCancelConfirmedOrder() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            order.completePayment("TXN-12345");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            // When
            order.cancel();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Order bereits storniert ist")
        void shouldThrowExceptionWhenOrderAlreadyCancelled() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            order.cancel();

            // When & Then
            assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bereits");
        }
    }

    @Nested
    @DisplayName("Business Method - refund()")
    class RefundOrderTests {

        @Test
        @DisplayName("Sollte CONFIRMED Order erstatten")
        void shouldRefundConfirmedOrder() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            order.completePayment("TXN-12345");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            // When
            order.refund();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(order.isCancelledOrRefunded()).isTrue();
        }

        @Test
        @DisplayName("Sollte IllegalStateException werfen wenn Order nicht CONFIRMED ist")
        void shouldThrowExceptionWhenOrderNotConfirmed() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            // When & Then
            assertThatThrownBy(() -> order.refund())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nur CONFIRMED Orders können erstattet werden");
        }
    }

    @Nested
    @DisplayName("Payment Integration")
    class PaymentIntegrationTests {

        @Test
        @DisplayName("Sollte Payment als erfolgreich markieren und Order bestätigen")
        void shouldCompletePaymentAndConfirmOrder() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.PAYPAL);
            String transactionId = "PAYPAL-TXN-789";

            // When
            order.completePayment(transactionId);

            // Then
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(order.getPayment().getTransactionId()).isEqualTo(transactionId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Sollte Payment als fehlgeschlagen markieren und Order stornieren")
        void shouldFailPaymentAndCancelOrder() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);

            // When
            order.failPayment();

            // Then
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Order Lifecycle")
    class OrderLifecycleTests {

        @Test
        @DisplayName("Sollte kompletten Order-Lifecycle durchlaufen: PENDING → CONFIRMED → REFUNDED")
        void shouldCompleteFullOrderLifecycle() {
            // Given - Order erstellen
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PENDING);

            // When - Payment erfolgreich
            order.completePayment("TXN-REFUND-123");
            
            // Then - Order bestätigt
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // When - Erstattung
            order.refund();

            // Then - Order erstattet
            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("Sollte Order-Lifecycle mit fehlgeschlagenem Payment durchlaufen")
        void shouldHandleFailedPaymentLifecycle() {
            // Given
            Order order = Order.createOrder(SEAT_ID, USER_ID, TOTAL_PRICE, PaymentMethod.CREDIT_CARD);

            // When - Payment schlägt fehl
            order.failPayment();

            // Then - Order storniert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(order.isCancelledOrRefunded()).isTrue();
        }
    }
}
