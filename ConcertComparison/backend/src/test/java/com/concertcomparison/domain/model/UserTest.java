package com.concertcomparison.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für User Entity.
 * 
 * Testet Business Logic, Validierungen und State Transitions.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuvwxyz1234567890"; // BCrypt Hash
    private static final String FIRST_NAME = "Max";
    private static final String LAST_NAME = "Mustermann";

    @Nested
    @DisplayName("Factory Methods - createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Sollte User erfolgreich erstellen mit allen Parametern")
        void shouldCreateUserSuccessfully() {
            // When
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME, UserRole.USER);

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(user.getPassword()).isEqualTo(VALID_PASSWORD_HASH);
            assertThat(user.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(user.getLastName()).isEqualTo(LAST_NAME);
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.getEnabled()).isTrue();
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Sollte User mit Standard-Rolle USER erstellen")
        void shouldCreateUserWithDefaultRole() {
            // When
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);

            // Then
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("Sollte Admin erfolgreich erstellen")
        void shouldCreateAdminSuccessfully() {
            // When
            User admin = User.createAdmin(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);

            // Then
            assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(admin.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn Email null ist")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            assertThatThrownBy(() -> User.createUser(null, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Email darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn Email leer ist")
        void shouldThrowExceptionWhenEmailIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> User.createUser("  ", VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email darf nicht leer sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn Email ungültiges Format hat")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // When & Then
            assertThatThrownBy(() -> User.createUser("invalid-email", VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email muss gültiges Format haben");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn Password null ist")
        void shouldThrowExceptionWhenPasswordIsNull() {
            // When & Then
            assertThatThrownBy(() -> User.createUser(VALID_EMAIL, null, FIRST_NAME, LAST_NAME))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Password darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn Password zu kurz ist")
        void shouldThrowExceptionWhenPasswordIsTooShort() {
            // When & Then
            assertThatThrownBy(() -> User.createUser(VALID_EMAIL, "short", FIRST_NAME, LAST_NAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password (hashed) muss mindestens 8 Zeichen haben");
        }

        @Test
        @DisplayName("Sollte NullPointerException werfen wenn Vorname null ist")
        void shouldThrowExceptionWhenFirstNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, null, LAST_NAME))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Vorname darf nicht null sein");
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen wenn Nachname leer ist")
        void shouldThrowExceptionWhenLastNameIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nachname darf nicht leer sein");
        }
    }

    @Nested
    @DisplayName("Business Method - updateProfile()")
    class UpdateProfileTests {

        @Test
        @DisplayName("Sollte Profil erfolgreich aktualisieren")
        void shouldUpdateProfileSuccessfully() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);
            String newFirstName = "Erika";
            String newLastName = "Musterfrau";

            // When
            user.updateProfile(newFirstName, newLastName);

            // Then
            assertThat(user.getFirstName()).isEqualTo(newFirstName);
            assertThat(user.getLastName()).isEqualTo(newLastName);
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen bei ungültigem Namen")
        void shouldThrowExceptionWhenUpdatingWithInvalidName() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);

            // When & Then
            assertThatThrownBy(() -> user.updateProfile("", LAST_NAME))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Business Method - changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Sollte Password erfolgreich ändern")
        void shouldChangePasswordSuccessfully() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);
            String newPasswordHash = "$2a$10$newhashnewhashnewhashnewhashnewhas";

            // When
            user.changePassword(newPasswordHash);

            // Then
            assertThat(user.getPassword()).isEqualTo(newPasswordHash);
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen bei ungültigem Password")
        void shouldThrowExceptionWhenNewPasswordIsInvalid() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);

            // When & Then
            assertThatThrownBy(() -> user.changePassword("short"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Business Method - changeEmail()")
    class ChangeEmailTests {

        @Test
        @DisplayName("Sollte Email erfolgreich ändern")
        void shouldChangeEmailSuccessfully() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);
            String newEmail = "newemail@example.com";

            // When
            user.changeEmail(newEmail);

            // Then
            assertThat(user.getEmail()).isEqualTo(newEmail);
        }

        @Test
        @DisplayName("Sollte IllegalArgumentException werfen bei ungültiger Email")
        void shouldThrowExceptionWhenNewEmailIsInvalid() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);

            // When & Then
            assertThatThrownBy(() -> user.changeEmail("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Business Methods - enable() & disable()")
    class EnableDisableTests {

        @Test
        @DisplayName("Sollte User deaktivieren")
        void shouldDisableUser() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);
            assertThat(user.isEnabled()).isTrue();

            // When
            user.disable();

            // Then
            assertThat(user.isEnabled()).isFalse();
            assertThat(user.getEnabled()).isFalse();
        }

        @Test
        @DisplayName("Sollte User aktivieren")
        void shouldEnableUser() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);
            user.disable();
            assertThat(user.isEnabled()).isFalse();

            // When
            user.enable();

            // Then
            assertThat(user.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Helper Methods")
    class HelperMethodsTests {

        @Test
        @DisplayName("Sollte vollständigen Namen zurückgeben")
        void shouldReturnFullName() {
            // Given
            User user = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);

            // When
            String fullName = user.getFullName();

            // Then
            assertThat(fullName).isEqualTo("Max Mustermann");
        }

        @Test
        @DisplayName("Sollte isAdmin() korrekt zurückgeben")
        void shouldReturnCorrectAdminStatus() {
            // Given
            User normalUser = User.createUser(VALID_EMAIL, VALID_PASSWORD_HASH, FIRST_NAME, LAST_NAME);
            User admin = User.createAdmin("admin@example.com", VALID_PASSWORD_HASH, "Admin", "User");

            // Then
            assertThat(normalUser.isAdmin()).isFalse();
            assertThat(admin.isAdmin()).isTrue();
        }
    }
}
