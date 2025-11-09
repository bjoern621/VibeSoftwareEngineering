package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.CreateReceiptDTO;
import com.travelreimburse.application.dto.ReceiptDTO;
import com.travelreimburse.application.dto.UpdateReceiptDTO;
import com.travelreimburse.domain.exception.ReceiptNotFoundException;
import com.travelreimburse.domain.exception.TravelRequestNotFoundException;
import com.travelreimburse.domain.model.*;
import com.travelreimburse.domain.repository.ReceiptRepository;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import com.travelreimburse.infrastructure.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service für Receipt Use Cases.
 * Orchestriert die Business-Logik für die Belegverwaltung.
 */
@Service
@Transactional(readOnly = true)
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final FileStorageService fileStorageService;
    private final ReceiptMapper receiptMapper;

    public ReceiptService(ReceiptRepository receiptRepository,
                          TravelRequestRepository travelRequestRepository,
                          FileStorageService fileStorageService,
                          ReceiptMapper receiptMapper) {
        this.receiptRepository = receiptRepository;
        this.travelRequestRepository = travelRequestRepository;
        this.fileStorageService = fileStorageService;
        this.receiptMapper = receiptMapper;
    }

    /**
     * Lädt einen Beleg hoch und erstellt einen Receipt-Eintrag
     */
    @Transactional
    public ReceiptDTO uploadReceipt(MultipartFile file, CreateReceiptDTO dto) {
        // Validiere Datei
        fileStorageService.validateFile(file);

        // Finde TravelRequest
        TravelRequest travelRequest = travelRequestRepository.findById(dto.travelRequestId())
                .orElseThrow(() -> new TravelRequestNotFoundException(dto.travelRequestId()));

        // Speichere Datei
        String fileName = fileStorageService.storeFile(file);
        String filePath = fileStorageService.getFilePath(fileName);

        // Erstelle Money-Objekt falls Betrag angegeben
        Money amount = null;
        if (dto.amount() != null && dto.currency() != null) {
            amount = new Money(dto.amount(), Currency.valueOf(dto.currency()));
        }

        // Erstelle Receipt Entity
        Receipt receipt = new Receipt(
                travelRequest,
                fileName,
                file.getOriginalFilename(),
                filePath,
                file.getContentType(),
                file.getSize(),
                dto.type(),
                dto.issueDate(),
                dto.description(),
                amount,
                dto.vendor()
        );

        // Speichere Receipt
        Receipt savedReceipt = receiptRepository.save(receipt);

        return receiptMapper.toDTO(savedReceipt);
    }

    /**
     * Findet einen Beleg anhand seiner ID
     */
    public ReceiptDTO findById(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));
        return receiptMapper.toDTO(receipt);
    }

    /**
     * Findet alle Belege zu einem Reiseantrag
     */
    public List<ReceiptDTO> findByTravelRequestId(Long travelRequestId) {
        List<Receipt> receipts = receiptRepository.findByTravelRequestId(travelRequestId);
        return receipts.stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Findet alle Belege mit einem bestimmten Status
     */
    public List<ReceiptDTO> findByStatus(ReceiptStatus status) {
        List<Receipt> receipts = receiptRepository.findByStatus(status);
        return receipts.stream()
                .map(receiptMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Aktualisiert beschreibende Felder eines Belegs
     */
    @Transactional
    public ReceiptDTO updateReceipt(Long id, UpdateReceiptDTO dto) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));

        // ✅ DDD: Delegiere zu Entity Business-Methoden statt Service-Validierung
        if (dto.description() != null) {
            receipt.updateDescription(dto.description());
        }

        if (dto.amount() != null && dto.currency() != null) {
            Money amount = new Money(dto.amount(), Currency.valueOf(dto.currency()));
            receipt.updateAmount(amount);
        }

        if (dto.vendor() != null) {
            receipt.updateVendor(dto.vendor());
        }

        Receipt updatedReceipt = receiptRepository.save(receipt);
        return receiptMapper.toDTO(updatedReceipt);
    }

    /**
     * Validiert einen Beleg
     */
    @Transactional
    public ReceiptDTO validateReceipt(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));

        // Use domain model method (publishes event automatically)
        receipt.validate();

        // Persistieren (Spring Data publishes events automatically)
        Receipt validatedReceipt = receiptRepository.save(receipt);


        return receiptMapper.toDTO(validatedReceipt);
    }

    /**
     * Lehnt einen Beleg ab
     */
    @Transactional
    public ReceiptDTO rejectReceipt(Long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Ablehnungsgrund ist erforderlich");
        }

        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));

        // Use domain model method (publishes event automatically)
        receipt.reject(reason);

        // Persistieren (Spring Data publishes events automatically)
        Receipt rejectedReceipt = receiptRepository.save(receipt);

        return receiptMapper.toDTO(rejectedReceipt);
    }

    /**
     * Löscht einen Beleg
     */
    @Transactional
    public void deleteReceipt(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));

        // Lösche Datei
        fileStorageService.deleteFile(receipt.getFileName());

        // Lösche Receipt
        receiptRepository.delete(receipt);
    }

    /**
     * Lädt eine Beleg-Datei herunter
     */
    public byte[] downloadReceipt(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));

        try {
            return java.nio.file.Files.readAllBytes(
                    fileStorageService.loadFile(receipt.getFileName())
            );
        } catch (Exception ex) {
            throw new InvalidFileException("Konnte Datei nicht laden", ex);
        }
    }

    /**
     * Prüft ob alle Belege eines Reiseantrags validiert sind
     */
    public boolean areAllReceiptsValidated(Long travelRequestId) {
        List<Receipt> receipts = receiptRepository.findByTravelRequestId(travelRequestId);

        if (receipts.isEmpty()) {
            return false;
        }

        return receipts.stream()
                .allMatch(Receipt::isValid);
    }

    /**
     * Zählt die Anzahl der Belege zu einem Reiseantrag
     */
    public long countByTravelRequestId(Long travelRequestId) {
        return receiptRepository.countByTravelRequestId(travelRequestId);
    }
}

