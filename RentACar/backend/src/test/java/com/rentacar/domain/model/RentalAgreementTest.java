package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidMileageException;
import com.rentacar.domain.exception.InvalidRentalAgreementDataException;
import com.rentacar.domain.exception.RentalAgreementStatusTransitionException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RentalAgreementTest {

    @Test
    void checkIn_shouldUpdateStatusAndSetCheckinData() {
        // Arrange
        Booking booking = mock(Booking.class);
        Mileage checkoutMileage = Mileage.of(1000);
        LocalDateTime checkoutTime = LocalDateTime.now().minusDays(2);
        VehicleCondition checkoutCondition = new VehicleCondition("FULL", "CLEAN", null);
        
        RentalAgreement agreement = new RentalAgreement(booking, checkoutMileage, checkoutTime, checkoutCondition);

        Mileage checkinMileage = Mileage.of(1200);
        LocalDateTime checkinTime = LocalDateTime.now();
        VehicleCondition checkinCondition = new VehicleCondition("FULL", "DIRTY", "Scratch on bumper");

        // Act
        agreement.checkIn(checkinMileage, checkinTime, checkinCondition);

        // Assert
        assertThat(agreement.getStatus()).isEqualTo(RentalAgreementStatus.CLOSED);
        assertThat(agreement.getCheckinMileage()).isEqualTo(checkinMileage);
        assertThat(agreement.getCheckinTime()).isEqualTo(checkinTime);
        assertThat(agreement.getCheckinCondition()).isEqualTo(checkinCondition);
    }

    @Test
    void checkIn_shouldThrowException_whenMileageIsLessThanCheckout() {
        // Arrange
        Booking booking = mock(Booking.class);
        Mileage checkoutMileage = Mileage.of(1000);
        RentalAgreement agreement = new RentalAgreement(booking, checkoutMileage, LocalDateTime.now(), 
            new VehicleCondition("FULL", "CLEAN", null));

        Mileage checkinMileage = Mileage.of(900); // Less than checkout

        // Act & Assert
        assertThatThrownBy(() -> agreement.checkIn(checkinMileage, LocalDateTime.now(), 
            new VehicleCondition("FULL", "CLEAN", null)))
            .isInstanceOf(InvalidMileageException.class)
            .hasMessageContaining("Rückgabe-Kilometerstand darf nicht kleiner als Ausgabe-Kilometerstand sein");
    }

    @Test
    void checkIn_shouldThrowException_whenTimeIsBeforeCheckout() {
        // Arrange
        Booking booking = mock(Booking.class);
        LocalDateTime checkoutTime = LocalDateTime.now();
        RentalAgreement agreement = new RentalAgreement(booking, Mileage.of(1000), checkoutTime, 
            new VehicleCondition("FULL", "CLEAN", null));

        LocalDateTime checkinTime = checkoutTime.minusHours(1); // Before checkout

        // Act & Assert
        assertThatThrownBy(() -> agreement.checkIn(Mileage.of(1100), checkinTime, 
            new VehicleCondition("FULL", "CLEAN", null)))
            .isInstanceOf(InvalidRentalAgreementDataException.class)
            .hasMessageContaining("Rückgabezeitpunkt darf nicht vor Ausgabezeitpunkt liegen");
    }

    @Test
    void checkIn_shouldThrowException_whenAlreadyClosed() {
        // Arrange
        Booking booking = mock(Booking.class);
        RentalAgreement agreement = new RentalAgreement(booking, Mileage.of(1000), LocalDateTime.now(), 
            new VehicleCondition("FULL", "CLEAN", null));
        
        agreement.checkIn(Mileage.of(1100), LocalDateTime.now().plusHours(1), 
            new VehicleCondition("FULL", "CLEAN", null));

        // Act & Assert
        assertThatThrownBy(() -> agreement.checkIn(Mileage.of(1200), LocalDateTime.now().plusHours(2), 
            new VehicleCondition("FULL", "CLEAN", null)))
            .isInstanceOf(RentalAgreementStatusTransitionException.class)
            .hasMessageContaining("Ungültiger Statusübergang für Mietvertrag");
    }
}
