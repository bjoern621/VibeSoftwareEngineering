package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.RefreshToken;
import com.rentacar.domain.repository.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository Implementation für RefreshToken.
 * Implementiert die Domain Repository Schnittstelle.
 */
@Repository
public interface JpaRefreshTokenRepository extends RefreshTokenRepository, JpaRepository<RefreshToken, Long> {

    @Override
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    @Override
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.customer.id = :customerId AND rt.revoked = false")
    List<RefreshToken> findByCustomerIdAndRevokedFalse(@Param("customerId") Long customerId);

    @Override
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiryDate < :now")
    default void deleteExpiredAndRevokedTokens() {
        deleteExpiredAndRevokedTokensNative(LocalDateTime.now());
    }

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiryDate < :now")
    void deleteExpiredAndRevokedTokensNative(@Param("now") LocalDateTime now);

    @Override
    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.customer.id = :customerId")
    void revokeAllTokensByCustomerId(@Param("customerId") Long customerId);
}

