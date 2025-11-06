package com.travelreimburse.domain.repository;

import com.travelreimburse.domain.model.Receipt;
import com.travelreimburse.domain.model.ReceiptStatus;
import com.travelreimburse.domain.model.ReceiptType;

import java.util.List;
import java.util.Optional;

/**
 * Repository-Interface für Receipt im Domain Layer.
 * Die Implementierung erfolgt im Infrastructure Layer.
 */
public interface ReceiptRepository {

    /**
     * Speichert einen Beleg
     */
    Receipt save(Receipt receipt);

    /**
     * Findet einen Beleg anhand seiner ID
     */
    Optional<Receipt> findById(Long id);

    /**
     * Findet alle Belege zu einem Reiseantrag
     */
    List<Receipt> findByTravelRequestId(Long travelRequestId);

    /**
     * Findet alle Belege mit einem bestimmten Status
     */
    List<Receipt> findByStatus(ReceiptStatus status);

    /**
     * Findet alle Belege eines bestimmten Typs
     */
    List<Receipt> findByType(ReceiptType type);

    /**
     * Findet alle Belege zu einem Reiseantrag mit einem bestimmten Status
     */
    List<Receipt> findByTravelRequestIdAndStatus(Long travelRequestId, ReceiptStatus status);

    /**
     * Löscht einen Beleg
     */
    void delete(Receipt receipt);

    /**
     * Löscht einen Beleg anhand seiner ID
     */
    void deleteById(Long id);

    /**
     * Prüft ob ein Beleg existiert
     */
    boolean existsById(Long id);

    /**
     * Zählt die Anzahl der Belege zu einem Reiseantrag
     */
    long countByTravelRequestId(Long travelRequestId);
}

