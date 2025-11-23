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
}
