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
