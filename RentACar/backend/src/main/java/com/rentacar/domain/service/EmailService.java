package com.rentacar.domain.service;

/**
 * E-Mail-Service Interface (Port für Infrastructure Layer).
 * Ermöglicht das Versenden von E-Mails ohne direkte Abhängigkeit zur Implementierung.
 */
public interface EmailService {

    /**
     * Versendet eine E-Mail zur Verifikation der E-Mail-Adresse.
     *
     * @param recipientEmail E-Mail-Adresse des Empfängers
     * @param recipientName  Name des Empfängers
     * @param verificationToken Verifikations-Token
     */
    void sendVerificationEmail(String recipientEmail, String recipientName, String verificationToken);

    /**
     * Versendet eine Bestätigungs-E-Mail nach erfolgreicher Registrierung.
     *
     * @param recipientEmail E-Mail-Adresse des Empfängers
     * @param recipientName  Name des Empfängers
     */
    void sendWelcomeEmail(String recipientEmail, String recipientName);

    /**
     * Versendet eine E-Mail zur Benachrichtigung über Passwortänderung.
     *
     * @param recipientEmail E-Mail-Adresse des Empfängers
     * @param recipientName  Name des Empfängers
     */
    void sendPasswordChangedEmail(String recipientEmail, String recipientName);

    /**
     * Versendet eine Benachrichtigung über einen erstellten Schadensbericht.
     *
     * @param recipientEmail E-Mail-Adresse des Empfängers
     * @param recipientName  Name des Empfängers
     * @param damageDescription Beschreibung des Schadens
     */
    void sendDamageReportNotification(String recipientEmail, String recipientName, String damageDescription);

    /**
     * Versendet eine Rechnung per E-Mail.
     *
     * @param recipientEmail E-Mail-Adresse des Empfängers
     * @param recipientName Name des Empfängers
     * @param booking Die Buchung
     * @param rentalAgreement Der Mietvertrag (mit Zusatzkosten)
     */
    void sendInvoiceEmail(String recipientEmail, String recipientName, com.rentacar.domain.model.Booking booking, com.rentacar.domain.model.RentalAgreement rentalAgreement);

    /**
     * Versendet eine Bestätigungs-E-Mail nach Buchungsstornierung.
     *
     * @param recipientEmail E-Mail-Adresse des Empfängers
     * @param recipientName Name des Empfängers
     * @param bookingId ID der stornierten Buchung
     * @param cancellationReason Grund der Stornierung (optional, kann null sein)
     */
    void sendBookingCancellationEmail(
        String recipientEmail,
        String recipientName,
        Long bookingId,
        String cancellationReason
    );
}
