package com.travelreimburse.infrastructure.service;

import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.model.TravelRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mock E-Mail Notification Service
 * Simuliert den Versand von E-Mails durch Log-Ausgaben
 * Kann später durch einen echten E-Mail-Service ersetzt werden
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * Sendet eine Mock-E-Mail bei Statusänderung eines Reiseantrags
     *
     * @param travelRequest Der betroffene Reiseantrag
     * @param oldStatus Der alte Status
     * @param newStatus Der neue Status
     */
    public void sendStatusChangeNotification(TravelRequest travelRequest,
                                            TravelRequestStatus oldStatus,
                                            TravelRequestStatus newStatus) {
        String emailContent = buildEmailContent(travelRequest, oldStatus, newStatus);
        String employeeEmail = "employee" + travelRequest.getEmployeeId() + "@company.com";

        logger.info("""
                
                ╔═══════════════════════════════════════════════════════════════════╗
                ║                    MOCK E-MAIL VERSANDT                           ║
                ╠═══════════════════════════════════════════════════════════════════╣
                ║ An: {}
                ║ Betreff: Reiseantrag Status-Änderung: {}
                ║ Zeitpunkt: {}
                ╠═══════════════════════════════════════════════════════════════════╣
                {}
                ╚═══════════════════════════════════════════════════════════════════╝
                """,
                employeeEmail,
                travelRequest.getId(),
                LocalDateTime.now().format(FORMATTER),
                emailContent
        );
    }

    /**
     * Erstellt den E-Mail-Inhalt für eine Statusänderung
     */
    private String buildEmailContent(TravelRequest travelRequest,
                                     TravelRequestStatus oldStatus,
                                     TravelRequestStatus newStatus) {
        return String.format("""
            ║ Sehr geehrte/r Mitarbeiter/in,
            ║
            ║ Ihr Reiseantrag hat eine Statusänderung erfahren:
            ║
            ║ Reiseantrag-ID: %d
            ║ Alter Status: %s
            ║ Neuer Status: %s
            ║
            ║ Reiseziel: %s
            ║ Reisedatum: %s bis %s
            ║ Zweck: %s
            ║ Geschätzte Kosten: %s
            ║
            ║ Mit freundlichen Grüßen
            ║ Ihr TravelReimburse Team
            """,
            travelRequest.getId(),
            getStatusDisplayName(oldStatus),
            getStatusDisplayName(newStatus),
            travelRequest.getDestination(),
            travelRequest.getTravelPeriod().getStartDate(),
            travelRequest.getTravelPeriod().getEndDate(),
            travelRequest.getPurpose(),
            travelRequest.getEstimatedCost()
        );
    }

    /**
     * Gibt den deutschen Anzeigenamen für einen Status zurück
     */
    private String getStatusDisplayName(TravelRequestStatus status) {
        return switch (status) {
            case DRAFT -> "Entwurf";
            case SUBMITTED -> "Eingereicht";
            case APPROVED -> "Genehmigt";
            case REJECTED -> "Abgelehnt";
        };
    }
}

