package com.concertcomparison.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Order Aggregate Root - Repräsentiert einen abgeschlossenen Ticket-Kauf.
 * 
 * <p>Eine Order entsteht durch den erfolgreichen Abschluss einer Reservation (Hold)
 * und markiert den finalen Verkauf eines Seats.</p>
 * 
 * <p><b>Invarianten:</b></p>
 * <ul>
 *   <li>seatId, userId, totalPrice sind required (not null)</li>
 *   <li>totalPrice muss positiv sein</li>
 *   <li>createdAt wird automatisch gesetzt</li>
 *   <li>status ist immer CONFIRMED (in dieser Version)</li>
 * </ul>
 * 
 * <p><b>Concurrency:</b></p>
 * Optimistic Locking via @Version verhindert Lost Updates.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID des gekauften Seats.
     */
    @Column(nullable = false)
    private Long seatId;

    /**
     * ID des Käufers.
     */
    @Column(nullable = false)
    private String userId;

    /**
     * Gesamtpreis des Tickets (in EUR).
     */
    @Column(nullable = false)
    private Double totalPrice;

    /**
     * Status der Order (initial immer CONFIRMED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /**
     * Zeitstempel der Order-Erstellung.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Version für Optimistic Locking.
     */
    @Version
    private Long version;

    /**
     * JPA Constructor (nicht für Business-Logik verwenden).
     */
    protected Order() {
    }

    /**
     * Private Constructor - erzwingt Nutzung der Factory-Method.
     * 
     * @param seatId     ID des gekauften Seats
     * @param userId     ID des Käufers
     * @param totalPrice Gesamtpreis (muss positiv sein)
     * @throws IllegalArgumentException wenn Validierung fehlschlägt
     */
    private Order(Long seatId, String userId, Double totalPrice) {
        validateSeatId(seatId);
        validateUserId(userId);
        validateTotalPrice(totalPrice);

        this.seatId = seatId;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory-Method: Erstellt eine Order aus einer bestätigten Reservation.
     * 
     * <p>Diese Methode ist der empfohlene Weg, um eine Order zu erstellen,
     * da sie sicherstellt, dass alle Business-Invarianten erfüllt sind.</p>
     * 
     * @param reservation Die Reservation, die in eine Order konvertiert wird
     * @param seat        Der Seat, der gekauft wird (für Preis-Informationen)
     * @return Neue Order-Instanz im Status CONFIRMED
     * @throws IllegalArgumentException wenn Reservation oder Seat null sind
     */
    public static Order createFromReservation(Reservation reservation, Seat seat) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation darf nicht null sein");
        }
        if (seat == null) {
            throw new IllegalArgumentException("Seat darf nicht null sein");
        }

        return new Order(
                reservation.getSeatId(),
                reservation.getUserId(),
                seat.getPrice()
        );
    }

    // ========== Validierungsmethoden ==========

    private void validateSeatId(Long seatId) {
        if (seatId == null) {
            throw new IllegalArgumentException("SeatId darf nicht null sein");
        }
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId darf nicht null oder leer sein");
        }
    }

    private void validateTotalPrice(Double totalPrice) {
        if (totalPrice == null) {
            throw new IllegalArgumentException("TotalPrice darf nicht null sein");
        }
        if (totalPrice <= 0) {
            throw new IllegalArgumentException("TotalPrice muss positiv sein");
        }
    }

    // ========== Getters ==========

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

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }

    // ========== Equals & HashCode ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        // Transiente Entities (ohne ID) sind niemals gleich
        if (id == null || order.id == null) return false;
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
                ", createdAt=" + createdAt +
                ", version=" + version +
                '}';
    }
}
