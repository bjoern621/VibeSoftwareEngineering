package com.concertcomparison.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Order Aggregate Root.
 * 
 * Repräsentiert eine abgeschlossene Bestellung (Ticketkauf).
 * 
 * DDD Aggregate Root:
 * - Verwaltet den gesamten Bestellvorgang
 * - Enthält Payment als Teil des Aggregates
 * - Business Logic für Order-Lifecycle (Create, Confirm, Cancel, Refund)
 * 
 * Business Rules:
 * - Order entsteht aus einer Reservation (Hold → Order)
 * - Payment wird bei Order-Erstellung initiiert
 * - Order kann nur bestätigt werden wenn Payment erfolgreich
 * - Stornierung/Erstattung nur für bestätigte Orders
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_seat", columnList = "seat_id"),
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_purchase_date", columnList = "purchase_date")
})
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID des gekauften Seats.
     * Business Rule: Ein Seat kann nur einmal verkauft werden.
     */
    @Column(name = "seat_id", nullable = false, unique = true)
    private Long seatId;
    
    /**
     * ID des Käufers.
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;
    
    /**
     * Gesamtpreis der Bestellung.
     */
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;
    
    /**
     * Datum und Uhrzeit des Kaufs.
     */
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;
    
    /**
     * Status der Bestellung.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    /**
     * Payment (Teil des Order Aggregates).
     * Embedded als @OneToOne mit Cascade.
     */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Optimistic Locking für Concurrency Control.
     */
    @Version
    @Column(name = "version")
    private Long version;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default Constructor (JPA required).
     */
    protected Order() {
        // Für JPA/Hibernate
    }
    
    /**
     * Private Constructor für Factory Method.
     */
    private Order(Long seatId, String userId, Double totalPrice) {
        this.seatId = seatId;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.purchaseDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Factory Method: Erstellt eine neue Order mit Payment.
     * 
     * @param seatId ID des gekauften Seats
     * @param userId ID des Käufers
     * @param totalPrice Gesamtpreis
     * @param paymentMethod Zahlungsmethode
     * @return Neue Order im Status PENDING mit initiiertem Payment
     * @throws IllegalArgumentException bei ungültigen Parametern
     */
    public static Order createOrder(Long seatId, String userId, Double totalPrice, PaymentMethod paymentMethod) {
        Objects.requireNonNull(seatId, "SeatId darf nicht null sein");
        Objects.requireNonNull(userId, "UserId darf nicht null sein");
        Objects.requireNonNull(totalPrice, "TotalPrice darf nicht null sein");
        Objects.requireNonNull(paymentMethod, "PaymentMethod darf nicht null sein");
        
        if (totalPrice <= 0) {
            throw new IllegalArgumentException("TotalPrice muss positiv sein");
        }
        
        Order order = new Order(seatId, userId, totalPrice);
        
        // Payment als Teil des Aggregates erstellen
        Payment payment = Payment.createPayment(totalPrice, paymentMethod);
        order.setPayment(payment);
        payment.setOrder(order);
        
        return order;
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * Bestätigt die Order nach erfolgreicher Zahlung.
     * 
     * Business Rules:
     * - Order muss PENDING sein
     * - Payment muss erfolgreich sein (COMPLETED)
     * 
     * @throws IllegalStateException wenn Order nicht PENDING oder Payment nicht erfolgreich
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                String.format("Nur PENDING Orders können bestätigt werden (aktuell: %s)", this.status)
            );
        }
        
        if (this.payment == null || !this.payment.isSuccessful()) {
            throw new IllegalStateException(
                "Order kann nur bestätigt werden wenn Payment erfolgreich ist"
            );
        }
        
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Storniert die Order.
     * 
     * Business Rules:
     * - Order kann nur in PENDING oder CONFIRMED Status storniert werden
     * - Payment wird auf CANCELLED gesetzt
     * 
     * @throws IllegalStateException wenn Order bereits storniert/erstattet ist
     */
    public void cancel() {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.REFUNDED) {
            throw new IllegalStateException(
                String.format("Order ist bereits %s", this.status.getDisplayName())
            );
        }
        
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        
        if (this.payment != null && this.payment.getStatus() == PaymentStatus.COMPLETED) {
            this.payment.cancel();
        }
    }
    
    /**
     * Erstattet die Order.
     * 
     * Business Rules:
     * - Nur CONFIRMED Orders können erstattet werden
     * - Payment wird auf REFUNDED gesetzt
     * 
     * @throws IllegalStateException wenn Order nicht CONFIRMED ist
     */
    public void refund() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                "Nur CONFIRMED Orders können erstattet werden"
            );
        }
        
        if (this.payment == null || this.payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                "Payment muss COMPLETED sein für Erstattung"
            );
        }
        
        this.status = OrderStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
        this.payment.refund();
    }
    
    /**
     * Markiert das Payment als erfolgreich und bestätigt die Order.
     * 
     * @param transactionId Transaktions-ID des Zahlungsanbieters
     */
    public void completePayment(String transactionId) {
        if (this.payment == null) {
            throw new IllegalStateException("Order hat kein Payment");
        }
        
        this.payment.complete(transactionId);
        this.confirm();
    }
    
    /**
     * Markiert das Payment als fehlgeschlagen und storniert die Order.
     */
    public void failPayment() {
        if (this.payment != null) {
            this.payment.fail();
        }
        this.cancel();
    }
    
    /**
     * Prüft ob die Order bestätigt ist.
     */
    public boolean isConfirmed() {
        return this.status == OrderStatus.CONFIRMED;
    }
    
    /**
     * Prüft ob die Order storniert oder erstattet wurde.
     */
    public boolean isCancelledOrRefunded() {
        return this.status == OrderStatus.CANCELLED || this.status == OrderStatus.REFUNDED;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Setzt das Payment (bidirektionale Beziehung).
     * Package-private für Aggregate Consistency.
     */
    void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    // ==================== JPA LIFECYCLE CALLBACKS ====================
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (purchaseDate == null) {
            purchaseDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public Long getSeatId() {
        return seatId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    // ==================== EQUALS & HASHCODE ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", seatId=" + seatId +
                ", userId='" + userId + '\'' +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", purchaseDate=" + purchaseDate +
                '}';
    }
}
