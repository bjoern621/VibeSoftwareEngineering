package com.rentacar.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator-Implementierung für {@link ValidPassword}.
 *
 * Überprüft Passwort-Komplexität:
 * - Mindestens 8 Zeichen
 * - Mindestens 1 Großbuchstabe
 * - Mindestens 1 Kleinbuchstabe
 * - Mindestens 1 Zahl
 * - Optional: Mindestens 1 Sonderzeichen
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;

    // Regex-Patterns für Validierung
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    private boolean requireSpecialChar;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Null-Werte werden von @NotBlank geprüft
        if (password == null) {
            return true;
        }

        // Mindestlänge prüfen
        if (password.length() < MIN_LENGTH) {
            buildCustomMessage(context, "Passwort muss mindestens " + MIN_LENGTH + " Zeichen lang sein");
            return false;
        }

        // Großbuchstabe prüfen
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            buildCustomMessage(context, "Passwort muss mindestens einen Großbuchstaben enthalten");
            return false;
        }

        // Kleinbuchstabe prüfen
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            buildCustomMessage(context, "Passwort muss mindestens einen Kleinbuchstaben enthalten");
            return false;
        }

        // Zahl prüfen
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            buildCustomMessage(context, "Passwort muss mindestens eine Zahl enthalten");
            return false;
        }

        // Optional: Sonderzeichen prüfen
        if (requireSpecialChar && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            buildCustomMessage(context, "Passwort muss mindestens ein Sonderzeichen enthalten (!@#$%^&*...)");
            return false;
        }

        return true;
    }

    /**
     * Erstellt eine benutzerdefinierte Fehlermeldung
     */
    private void buildCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

