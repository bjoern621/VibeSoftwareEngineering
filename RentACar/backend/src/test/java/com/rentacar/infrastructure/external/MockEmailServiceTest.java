package com.rentacar.infrastructure.external;

import com.rentacar.domain.model.Booking;
import com.rentacar.domain.model.RentalAgreement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MockEmailServiceTest {

    @InjectMocks
    private MockEmailService mockEmailService;

    @Test
    void sendVerificationEmail() {
        assertDoesNotThrow(() -> mockEmailService.sendVerificationEmail("test@example.com", "Test User", "token123"));
    }

    @Test
    void sendWelcomeEmail() {
        assertDoesNotThrow(() -> mockEmailService.sendWelcomeEmail("test@example.com", "Test User"));
    }

    @Test
    void sendPasswordChangedEmail() {
        assertDoesNotThrow(() -> mockEmailService.sendPasswordChangedEmail("test@example.com", "Test User"));
    }

    @Test
    void sendDamageReportNotification() {
        assertDoesNotThrow(() -> mockEmailService.sendDamageReportNotification("test@example.com", "Test User", "Damage details"));
    }

    @Test
    void sendInvoiceEmail() {
        Booking booking = mock(Booking.class);
        RentalAgreement rentalAgreement = mock(RentalAgreement.class);
        assertDoesNotThrow(() -> mockEmailService.sendInvoiceEmail("test@example.com", "Test User", booking, rentalAgreement));
    }
}
