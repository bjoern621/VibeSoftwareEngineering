package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.CreateReceiptDTO;
import com.travelreimburse.application.dto.ReceiptDTO;
import com.travelreimburse.application.dto.UpdateReceiptDTO;
import com.travelreimburse.application.service.ReceiptService;
import com.travelreimburse.domain.model.ReceiptStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST-Controller für Receipt-Operationen.
 * Verwaltet die digitale Belegverwaltung.
 */
@RestController
@RequestMapping("/api/receipts")
@Tag(name = "Belege", description = "Verwaltung von digitalen Belegen")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Operation(summary = "Beleg hochladen",
            description = "Lädt einen neuen Beleg mit Datei hoch")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Beleg erfolgreich hochgeladen",
                    content = @Content(schema = @Schema(implementation = ReceiptDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ungültige Daten oder Datei"),
            @ApiResponse(responseCode = "404", description = "Reiseantrag nicht gefunden")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptDTO> uploadReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestParam("travelRequestId") Long travelRequestId,
            @RequestParam("type") String type,
            @RequestParam("issueDate") String issueDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "vendor", required = false) String vendor
    ) {
        // Erstelle DTO aus Request-Parametern
        CreateReceiptDTO dto = new CreateReceiptDTO(
                travelRequestId,
                com.travelreimburse.domain.model.ReceiptType.valueOf(type),
                java.time.LocalDate.parse(issueDate),
                description,
                amount != null ? new java.math.BigDecimal(amount) : null,
                currency,
                vendor
        );

        ReceiptDTO createdReceipt = receiptService.uploadReceipt(file, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReceipt);
    }

    @Operation(summary = "Beleg abrufen", description = "Ruft einen Beleg anhand seiner ID ab")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beleg gefunden",
                    content = @Content(schema = @Schema(implementation = ReceiptDTO.class))),
            @ApiResponse(responseCode = "404", description = "Beleg nicht gefunden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReceiptDTO> getReceipt(@PathVariable Long id) {
        ReceiptDTO receipt = receiptService.findById(id);
        return ResponseEntity.ok(receipt);
    }

    @Operation(summary = "Belege nach Reiseantrag",
            description = "Ruft alle Belege eines Reiseantrags ab")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste der Belege")
    })
    @GetMapping("/travel-request/{travelRequestId}")
    public ResponseEntity<List<ReceiptDTO>> getReceiptsByTravelRequest(
            @PathVariable Long travelRequestId
    ) {
        List<ReceiptDTO> receipts = receiptService.findByTravelRequestId(travelRequestId);
        return ResponseEntity.ok(receipts);
    }

    @Operation(summary = "Belege nach Status", description = "Ruft alle Belege mit einem Status ab")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste der Belege")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReceiptDTO>> getReceiptsByStatus(@PathVariable ReceiptStatus status) {
        List<ReceiptDTO> receipts = receiptService.findByStatus(status);
        return ResponseEntity.ok(receipts);
    }

    @Operation(summary = "Beleg aktualisieren",
            description = "Aktualisiert beschreibende Felder eines Belegs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beleg aktualisiert",
                    content = @Content(schema = @Schema(implementation = ReceiptDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ungültige Daten"),
            @ApiResponse(responseCode = "404", description = "Beleg nicht gefunden")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReceiptDTO> updateReceipt(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReceiptDTO dto
    ) {
        ReceiptDTO updatedReceipt = receiptService.updateReceipt(id, dto);
        return ResponseEntity.ok(updatedReceipt);
    }

    @Operation(summary = "Beleg validieren", description = "Markiert einen Beleg als validiert")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beleg validiert",
                    content = @Content(schema = @Schema(implementation = ReceiptDTO.class))),
            @ApiResponse(responseCode = "400", description = "Beleg kann nicht validiert werden"),
            @ApiResponse(responseCode = "404", description = "Beleg nicht gefunden")
    })
    @PostMapping("/{id}/validate")
    public ResponseEntity<ReceiptDTO> validateReceipt(@PathVariable Long id) {
        ReceiptDTO validatedReceipt = receiptService.validateReceipt(id);
        return ResponseEntity.ok(validatedReceipt);
    }

    @Operation(summary = "Beleg ablehnen", description = "Lehnt einen Beleg mit Grund ab")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beleg abgelehnt",
                    content = @Content(schema = @Schema(implementation = ReceiptDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ungültiger Ablehnungsgrund"),
            @ApiResponse(responseCode = "404", description = "Beleg nicht gefunden")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<ReceiptDTO> rejectReceipt(
            @PathVariable Long id,
            @RequestBody RejectReceiptRequest request
    ) {
        ReceiptDTO rejectedReceipt = receiptService.rejectReceipt(id, request.reason());
        return ResponseEntity.ok(rejectedReceipt);
    }

    @Operation(summary = "Beleg löschen", description = "Löscht einen Beleg und seine Datei")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Beleg gelöscht"),
            @ApiResponse(responseCode = "404", description = "Beleg nicht gefunden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceipt(@PathVariable Long id) {
        receiptService.deleteReceipt(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Beleg-Datei herunterladen",
            description = "Lädt die Datei eines Belegs herunter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datei gefunden"),
            @ApiResponse(responseCode = "404", description = "Beleg oder Datei nicht gefunden")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {
        ReceiptDTO receipt = receiptService.findById(id);
        byte[] fileContent = receiptService.downloadReceipt(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(receipt.contentType()));
        headers.setContentDispositionFormData("attachment", receipt.originalFileName());
        headers.setContentLength(fileContent.length);

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    /**
     * Request-Record für Beleg-Ablehnung
     */
    public record RejectReceiptRequest(
            @Schema(description = "Grund für die Ablehnung", example = "Beleg unleserlich")
            String reason
    ) {
    }
}

