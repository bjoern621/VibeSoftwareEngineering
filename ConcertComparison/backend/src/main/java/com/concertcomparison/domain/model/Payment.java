package com.concertcomparison.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Payment Entity (Teil des Order Aggregates).
 * 
 * Repräsentiert eine Zahlung für eine Bestellung.
 * 
 * DDD Design:
 * - Payment ist Teil des Order Aggregates (keine eigenständige Aggregate Root)
 * - Kann nur über Order modifiziert werden
 * - Lifecycle ist an Order gebunden (Cascade, OrphanRemoval)
 * 
 * Business Rules:
 * - Payment wird bei Order-Erstellung mit Status PENDING initiiert
 * - Status kann auf COMPLETED, FAILED, CANCELLED, REFUNDED wechseln
 * - Transaction-ID wird bei Erfolg gesetzt
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_order", columnList = "order_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_transaction", columnList = "transaction_id")
})
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Bidirektionale Beziehung zu Order.
     */
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    /**
     * Zahlungsbetrag (identisch mit Order.totalPrice).
     */
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    /**
     * Zahlungsmethode.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method;
    
    /**
     * Payment Status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;
    
    /**
     * Transaktions-ID vom Zahlungsanbieter.
     * Wird bei erfolgreichem Payment gesetzt.
     */
    @Column(name = "transaction_id", length = 200)
    private String transactionId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default Constructor (JPA required).
     */
    protected Payment() {
        // Für JPA/Hibernate
    }
    
    /**
     * Private Constructor für Factory Method.
     */
    private Payment(Double amount, PaymentMethod method) {
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Factory Method: Erstellt ein neues Payment im Status PENDING.
     * 
     * @param amount Zahlungsbetrag
     * @param method Zahlungsmethode
     * @return Neues Payment im Status PENDING
     * @throws IllegalArgumentException bei ungültigen Parametern
     */
    static Payment createPayment(Double amount, PaymentMethod method) {
        Objects.requireNonNull(amount, "Amount darf nicht null sein");
        Objects.requireNonNull(method, "PaymentMethod darf nicht null sein");
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount muss positiv sein");
        }
        
        return new Payment(amount, method);
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * Markiert das Payment als erfolgreich abgeschlossen.
     * 
     * Business Rule: Nur PENDING Payments können completed werden.
     * 
     * @param transactionId Transaktions-ID vom Zahlungsanbieter
     * @throws IllegalStateException wenn Payment nicht PENDING ist
     */
    void complete(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                String.format("Nur PENDING Payments können abgeschlossen werden (aktuell: %s)", this.status)
            );
        }
        
        Objects.requireNonNull(transactionId, "TransactionId darf nicht null sein");
        
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Markiert das Payment als fehlgeschlagen.
     * 
     * Business Rule: Nur PENDING Payments können failed werden.
     * 
     * @throws IllegalStateException wenn Payment nicht PENDING ist
     */
    void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                String.format("Nur PENDING Payments können fehlschlagen (aktuell: %s)", this.status)
            );
        }
        
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Storniert das Payment.
     * 
     * Business Rule: Nur PENDING oder COMPLETED Payments können storniert werden.
     * 
     * @throws IllegalStateException wenn Payment bereits CANCELLED oder REFUNDED ist
     */
    void cancel() {
        if (this.status == PaymentStatus.CANCELLED || this.status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException(
                String.format("Payment ist bereits %s", this.status.getDisplayName())
            );
        }
        
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Erstattet das Payment.
     * 
     * Business Rule: Nur COMPLETED Payments können erstattet werden.
     * 
     * @throws IllegalStateException wenn Payment nicht COMPLETED ist
     */
    void refund() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                "Nur COMPLETED Payments können erstattet werden"
            );
        }
        
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Prüft ob das Payment erfolgreich war.
     */
    public boolean isSuccessful() {
        return this.status == PaymentStatus.COMPLETED;
    }
    
    /**
     * Prüft ob das Payment fehlgeschlagen ist.
     */
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED || this.status == PaymentStatus.CANCELLED;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Setzt die Order (bidirektionale Beziehung).
     * Package-private für Aggregate Consistency.
     */
    void setOrder(Order order) {
        this.order = order;
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public PaymentMethod getMethod() {
        return method;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // ==================== EQUALS & HASHCODE ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", amount=" + amount +
                ", method=" + method +
                ", status=" + status +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
