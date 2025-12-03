package com.rentacar.infrastructure.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-Tests für {@link PasswordValidator}.
 */
@DisplayName("PasswordValidator Tests")
class PasswordValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Passw0rd",        // Gültiges Standard-Passwort
            "MyP@ssw0rd123"    // Passwort mit allen Anforderungen
    })
    @DisplayName("Gültige Passwörter sollten akzeptiert werden")
    void validPasswords_ShouldPass(String password) {
        // Given
        TestDto dto = new TestDto(password);

        // When
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Null-Passwort sollte durchgelassen werden (wird von @NotBlank geprüft)")
    void nullPassword_ShouldPassValidator() {
        // Given
        TestDto dto = new TestDto(null);

        // When
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // Then
        // @ValidPassword lässt null durch, @NotBlank würde es abfangen
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Short1A",      // zu kurz (7 Zeichen)
            "short",        // zu kurz, keine Großbuchstaben, keine Zahlen
            "12345678"      // nur Zahlen
    })
    @DisplayName("Zu kurzes Passwort sollte abgelehnt werden")
    void tooShortPassword_ShouldFail(String password) {
        // Given
        TestDto dto = new TestDto(password);

        // When
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "passw0rd",      // ohne Großbuchstaben
            "PASSW0RD",      // ohne Kleinbuchstaben
            "Password",      // ohne Zahl
            "PASSWORD123",   // nur Großbuchstaben und Zahlen
            "password123"    // nur Kleinbuchstaben und Zahlen
    })
    @DisplayName("Passwörter mit fehlenden Anforderungen sollten abgelehnt werden")
    void passwordsWithMissingRequirements_ShouldFail(String password) {
        // Given
        TestDto dto = new TestDto(password);

        // When
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        String errorMessage = violations.iterator().next().getMessage();
        assertThat(errorMessage).matches(
                ".*(Großbuchstaben|Kleinbuchstaben|Zahl).*"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "MyP@ssw0rd!",
            "Secur3#Pass",
            "T3st$Pass!"
    })
    @DisplayName("Passwort mit Sonderzeichen sollte akzeptiert werden")
    void passwordWithSpecialChars_ShouldPass(String password) {
        // Given
        TestDto dto = new TestDto(password);

        // When
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        // Zusätzlich: Überprüfe, dass das Passwort tatsächlich ein Sonderzeichen enthält
        assertThat(password).matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    @Test
    @DisplayName("Passwort mit Sonderzeichen ist optional gültig")
    void passwordWithSpecialChar_OptionalValidation() {
        // Given
        TestDtoWithSpecialChar dto = new TestDtoWithSpecialChar("Passw0rd");

        // When
        Set<ConstraintViolation<TestDtoWithSpecialChar>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Sonderzeichen");
    }

    @Test
    @DisplayName("Passwort mit Sonderzeichen sollte bei requireSpecialChar=true akzeptiert werden")
    void passwordWithSpecialChar_RequiredValidation_ShouldPass() {
        // Given
        TestDtoWithSpecialChar dto = new TestDtoWithSpecialChar("Passw0rd!");

        // When
        Set<ConstraintViolation<TestDtoWithSpecialChar>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    // Test-DTO-Klassen
    private static class TestDto {
        @ValidPassword
        private final String password;

        public TestDto(String password) {
            this.password = password;
        }
    }

    private static class TestDtoWithSpecialChar {
        @ValidPassword(requireSpecialChar = true)
        private final String password;

        public TestDtoWithSpecialChar(String password) {
            this.password = password;
        }
    }
}

