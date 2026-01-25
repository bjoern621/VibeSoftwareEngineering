package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.ReservationStatus;
import com.concertcomparison.domain.repository.ReservationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Adapter für ReservationRepository.
 */
@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepository {

    @Override
    @Query("SELECT r FROM Reservation r WHERE r.seatId = :seatId AND r.status = 'ACTIVE'")
    Optional<Reservation> findActiveBySeatId(@Param("seatId") Long seatId);

    @Override
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = 'ACTIVE'")
    List<Reservation> findActiveByUserId(@Param("userId") String userId);

    @Override
    List<Reservation> findByStatus(ReservationStatus status);

    @Override
    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<Reservation> findExpired(@Param("now") LocalDateTime now);

    @Override
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status = 'ACTIVE'")
    long countActiveByUserId(@Param("userId") String userId);
}
