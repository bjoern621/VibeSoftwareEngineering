package com.rentacar.infrastructure.external;

import com.rentacar.domain.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock-Implementierung des E-Mail-Service für Entwicklung und Tests.
 * Loggt E-Mails anstatt sie tatsächlich zu versenden.
 */
@Service
public class MockEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void sendVerificationEmail(String recipientEmail, String recipientName, String verificationToken) {
        log.info("=== E-MAIL VERSAND (MOCK) ===");
        log.info("An: {}", recipientEmail);
        log.info("Empfänger: {}", recipientName);
        log.info("Betreff: Bitte bestätigen Sie Ihre E-Mail-Adresse");
        log.info("Inhalt:");
        log.info("Hallo {},", recipientName);
        log.info("");
        log.info("Vielen Dank für Ihre Registrierung bei RentACar!");
        log.info("Bitte bestätigen Sie Ihre E-Mail-Adresse, indem Sie auf den folgenden Link klicken:");
        log.info("");
        log.info("http://localhost:8080/api/kunden/verify-email?token={}", verificationToken);
        log.info("");
        log.info("Der Link ist 24 Stunden gültig.");
        log.info("");
        log.info("Mit freundlichen Grüßen");
        log.info("Ihr RentACar Team");
        log.info("============================");
    }

    @Override
    public void sendWelcomeEmail(String recipientEmail, String recipientName) {
        log.info("=== E-MAIL VERSAND (MOCK) ===");
        log.info("An: {}", recipientEmail);
        log.info("Empfänger: {}", recipientName);
        log.info("Betreff: Willkommen bei RentACar!");
        log.info("Inhalt:");
        log.info("Hallo {},", recipientName);
        log.info("");
        log.info("Ihre E-Mail-Adresse wurde erfolgreich bestätigt.");
        log.info("Sie können sich jetzt mit Ihren Zugangsdaten anmelden.");
        log.info("");
        log.info("Mit freundlichen Grüßen");
        log.info("Ihr RentACar Team");
        log.info("============================");
    }

    @Override
    public void sendPasswordChangedEmail(String recipientEmail, String recipientName) {
        log.info("=== E-MAIL VERSAND (MOCK) ===");
        log.info("An: {}", recipientEmail);
        log.info("Empfänger: {}", recipientName);
        log.info("Betreff: Ihr Passwort wurde geändert");
        log.info("Inhalt:");
        log.info("Hallo {},", recipientName);
        log.info("");
        log.info("Ihr Passwort wurde erfolgreich geändert.");
        log.info("Falls Sie diese Änderung nicht vorgenommen haben, kontaktieren Sie bitte umgehend unseren Support.");
        log.info("");
        log.info("Mit freundlichen Grüßen");
        log.info("Ihr RentACar Team");
        log.info("============================");
    }

    @Override
    public void sendDamageReportNotification(String recipientEmail, String recipientName, String damageDescription) {
        log.info("=== E-MAIL VERSAND (MOCK) ===");
        log.info("An: {}", recipientEmail);
        log.info("Empfänger: {}", recipientName);
        log.info("Betreff: Neuer Schadensbericht");
        log.info("Inhalt:");
        log.info("Hallo {},", recipientName);
        log.info("");
        log.info("Es wurde ein neuer Schadensbericht für Ihre Miete erstellt:");
        log.info("{}", damageDescription);
        log.info("");
        log.info("Mit freundlichen Grüßen");
        log.info("Ihr RentACar Team");
        log.info("============================");
    }

    @Override
    public void sendInvoiceEmail(String recipientEmail, String recipientName, com.rentacar.domain.model.Booking booking, com.rentacar.domain.model.RentalAgreement rentalAgreement) {
        log.info("=== E-MAIL VERSAND (MOCK) ===");
        log.info("An: {}", recipientEmail);
        log.info("Empfänger: {}", recipientName);
        log.info("Betreff: Ihre Rechnung");
        log.info("Inhalt:");
        log.info("Hallo {},", recipientName);
        log.info("");
        log.info("Vielen Dank für Ihre Miete.");
        log.info("Hier ist Ihre Abrechnung:");
        // Note: booking.getId() might not be accessible if package private or protected, but usually it is public.
        // Assuming standard getters.

        com.rentacar.domain.model.AdditionalCosts costs = rentalAgreement.getAdditionalCosts();
        if (costs != null) {
            log.info("Zusatzkosten:");
            log.info("- Verspätung: {}", costs.getLateFee());
            log.info("- Mehrkilometer: {}", costs.getExcessMileageFee());
            log.info("- Schäden: {}", costs.getDamageCost());
            log.info("Gesamt Zusatzkosten: {}", costs.getTotalAdditionalCost());
        }

        log.info("");
        log.info("Mit freundlichen Grüßen");
        log.info("Ihr RentACar Team");
        log.info("============================");
    }

    @Override
    public void sendBookingCancellationEmail(
        String recipientEmail,
        String recipientName,
        Long bookingId,
        String cancellationReason
    ) {
        log.info("=== E-MAIL VERSAND (MOCK) ===");
        log.info("An: {}", recipientEmail);
        log.info("Empfänger: {}", recipientName);
        log.info("Betreff: Buchung #{} wurde storniert", bookingId);
        log.info("Inhalt:");
        log.info("Hallo {},", recipientName);
        log.info("");
        log.info("Ihre Buchung #{} wurde erfolgreich storniert.", bookingId);

        if (cancellationReason != null && !cancellationReason.isBlank()) {
            log.info("");
            log.info("Stornierungsgrund: {}", cancellationReason);
        }

        log.info("");
        log.info("Bei Fragen stehen wir Ihnen gerne zur Verfügung.");
        log.info("Kontakt: support@rentacar.com | Tel: +49 123 456789");
        log.info("");
        log.info("Mit freundlichen Grüßen");
        log.info("Ihr RentACar Team");
        log.info("============================");
    }
}
