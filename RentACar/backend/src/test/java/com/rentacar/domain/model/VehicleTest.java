package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidMileageException;
import com.rentacar.domain.exception.InvalidVehicleDataException;
import com.rentacar.domain.exception.VehicleStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für Vehicle Entity.
 * 
 * Testet Business-Logik, Invarianten und Zustandsübergänge.
 */
class VehicleTest {
    
    private Branch testBranch;
    private LicensePlate testLicensePlate;
    private Mileage testMileage;
    
    @BeforeEach
    void setUp() {
        testBranch = new Branch("Hauptfiliale", "Hauptstraße 1, 10115 Berlin", "Mo-Fr 8-18 Uhr");
        testLicensePlate = LicensePlate.of("B-AB 1234");
        testMileage = Mileage.of(50000);
    }
    
    @Test
    void shouldCreateValidVehicle() {
        // When
        Vehicle vehicle = new Vehicle(
            testLicensePlate,
            "BMW",
            "3er",
            2020,
            testMileage,
            VehicleType.SEDAN,
            testBranch
        );
        
        // Then
        assertNotNull(vehicle);
        assertEquals(testLicensePlate, vehicle.getLicensePlate());
        assertEquals("BMW", vehicle.getBrand());
        assertEquals("3er", vehicle.getModel());
        assertEquals(2020, vehicle.getYear());
        assertEquals(testMileage, vehicle.getMileage());
        assertEquals(VehicleType.SEDAN, vehicle.getVehicleType());
        assertEquals(testBranch, vehicle.getBranch());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus()); // Neues Fahrzeug ist verfügbar
    }
    
    @Test
    void shouldRejectNullLicensePlate() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(null, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch)
        );
    }
    
    @Test
    void shouldRejectNullBrand() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, null, "3er", 2020, testMileage, VehicleType.SEDAN, testBranch)
        );
    }
    
    @Test
    void shouldRejectEmptyBrand() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, "  ", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch)
        );
    }
    
    @Test
    void shouldRejectNullModel() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, "BMW", null, 2020, testMileage, VehicleType.SEDAN, testBranch)
        );
    }
    
    @Test
    void shouldRejectInvalidYear() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, "BMW", "3er", 1899, testMileage, VehicleType.SEDAN, testBranch)
        );
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, "BMW", "3er", 2030, testMileage, VehicleType.SEDAN, testBranch)
        );
    }
    
    @Test
    void shouldRejectNullMileage() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, "BMW", "3er", 2020, null, VehicleType.SEDAN, testBranch)
        );
    }
    
    @Test
    void shouldRejectNullVehicleType() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, "BMW", "3er", 2020, testMileage, null, testBranch)
        );
    }
    
    @Test
    void shouldRejectNullBranch() {
        assertThrows(InvalidVehicleDataException.class, () -> 
            new Vehicle(testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, null)
        );
    }
    
    @Test
    void shouldMarkAsRentedWhenAvailable() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        
        // When
        vehicle.markAsRented();
        
        // Then
        assertEquals(VehicleStatus.RENTED, vehicle.getStatus());
    }
    
    @Test
    void shouldNotMarkAsRentedWhenNotAvailable() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        vehicle.markAsRented();
        
        // When & Then
        assertThrows(VehicleStatusTransitionException.class, vehicle::markAsRented);
    }
    
    @Test
    void shouldMarkAsAvailableAfterReturn() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        vehicle.markAsRented();
        Mileage returnMileage = Mileage.of(51000);
        
        // When
        vehicle.markAsAvailable(returnMileage);
        
        // Then
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertEquals(returnMileage, vehicle.getMileage());
    }
    
    @Test
    void shouldRejectReturnWithLowerMileage() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, Mileage.of(50000), VehicleType.SEDAN, testBranch
        );
        vehicle.markAsRented();
        Mileage lowerMileage = Mileage.of(49000);
        
        // When & Then
        assertThrows(InvalidMileageException.class, 
            () -> vehicle.markAsAvailable(lowerMileage)
        );
    }
    
    @Test
    void shouldMarkAsInMaintenance() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        
        // When
        vehicle.markAsInMaintenance();
        
        // Then
        assertEquals(VehicleStatus.IN_MAINTENANCE, vehicle.getStatus());
    }
    
    @Test
    void shouldNotMarkAsMaintenanceWhenRented() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        vehicle.markAsRented();
        
        // When & Then
        assertThrows(VehicleStatusTransitionException.class, vehicle::markAsInMaintenance);
    }
    
    @Test
    void shouldRetireVehicle() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        
        // When
        vehicle.retire();
        
        // Then
        assertEquals(VehicleStatus.OUT_OF_SERVICE, vehicle.getStatus());
    }
    
    @Test
    void shouldNotRetireRentedVehicle() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        vehicle.markAsRented();
        
        // When & Then
        assertThrows(VehicleStatusTransitionException.class, vehicle::retire);
    }
    
    @Test
    void shouldRelocateVehicle() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        Branch newBranch = new Branch("Filiale Nord", "Nordstraße 10, 20095 Hamburg", "Mo-Sa 9-19 Uhr");
        
        // When
        vehicle.relocateToBranch(newBranch);
        
        // Then
        assertEquals(newBranch, vehicle.getBranch());
    }
    
    @Test
    void shouldNotRelocateRentedVehicle() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, testMileage, VehicleType.SEDAN, testBranch
        );
        vehicle.markAsRented();
        Branch newBranch = new Branch("Filiale Nord", "Nordstraße 10, 20095 Hamburg", "Mo-Sa 9-19 Uhr");
        
        // When & Then
        assertThrows(VehicleStatusTransitionException.class, 
            () -> vehicle.relocateToBranch(newBranch)
        );
    }
    
    @Test
    void shouldUpdateMileage() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, Mileage.of(50000), VehicleType.SEDAN, testBranch
        );
        Mileage newMileage = Mileage.of(55000);
        
        // When
        vehicle.updateMileage(newMileage);
        
        // Then
        assertEquals(newMileage, vehicle.getMileage());
    }
    
    @Test
    void shouldRejectLowerMileageUpdate() {
        // Given
        Vehicle vehicle = new Vehicle(
            testLicensePlate, "BMW", "3er", 2020, Mileage.of(50000), VehicleType.SEDAN, testBranch
        );
        Mileage lowerMileage = Mileage.of(45000);
        
        // When & Then
        assertThrows(InvalidMileageException.class, 
            () -> vehicle.updateMileage(lowerMileage)
        );
    }
}
