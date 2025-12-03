package com.rentacar.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom Validator-Annotation für Passwort-Komplexität.
 *
 * Anforderungen:
 * - Mindestens 8 Zeichen
 * - Mindestens 1 Großbuchstabe
 * - Mindestens 1 Kleinbuchstabe
 * - Mindestens 1 Zahl
 * - Optional: Mindestens 1 Sonderzeichen
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Passwort erfüllt nicht die Komplexitätsanforderungen: " +
            "mindestens 8 Zeichen, 1 Großbuchstabe, 1 Kleinbuchstabe und 1 Zahl";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Ob Sonderzeichen erforderlich sind (Standard: false)
     */
    boolean requireSpecialChar() default false;
}

