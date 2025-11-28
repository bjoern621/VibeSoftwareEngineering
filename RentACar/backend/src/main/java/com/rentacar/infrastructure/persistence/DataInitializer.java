package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BranchRepository;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.domain.repository.CustomerRepository;
import com.rentacar.domain.repository.BookingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * DataInitializer - Lädt Testdaten beim Anwendungsstart.
 * 
 * Erstellt Branches und Fahrzeuge für Entwicklung und Testing.
 * Daten werden nur geladen, wenn die Datenbank leer ist.
 */
@Configuration
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Bean
    CommandLineRunner initDatabase(BranchRepository branchRepository, 
                                   VehicleRepository vehicleRepository,
                                   CustomerRepository customerRepository,
                                   BookingRepository bookingRepository) {
        return args -> {
            // Prüfen ob bereits Daten vorhanden sind
            if (branchRepository.findAll().isEmpty()) {
                logger.info("🚀 Initialisiere Testdaten...");
                
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                
                // ========== FILIALEN ==========
                Branch muenchen = branchRepository.save(new Branch(
                    "München Hauptbahnhof",
                    "Bayerstraße 10, 80335 München",
                    "Mo-Fr 08:00-20:00, Sa-So 09:00-18:00"
                ));
                logger.info("✅ Filiale erstellt: München Hauptbahnhof");
                
                Branch berlin = branchRepository.save(new Branch(
                    "Berlin Alexanderplatz",
                    "Alexanderplatz 1, 10178 Berlin",
                    "Mo-So 07:00-22:00"
                ));
                logger.info("✅ Filiale erstellt: Berlin Alexanderplatz");
                
                Branch hamburg = branchRepository.save(new Branch(
                    "Hamburg Flughafen",
                    "Flughafenstraße 1, 22335 Hamburg",
                    "24/7 geöffnet"
                ));
                logger.info("✅ Filiale erstellt: Hamburg Flughafen");
                
                Branch frankfurt = branchRepository.save(new Branch(
                    "Frankfurt Hauptbahnhof",
                    "Am Hauptbahnhof 1, 60329 Frankfurt",
                    "Mo-Fr 07:00-21:00, Sa-So 08:00-20:00"
                ));
                logger.info("✅ Filiale erstellt: Frankfurt Hauptbahnhof");
                
                // ========== FAHRZEUGE - MÜNCHEN ==========
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("M-GH 3434"),
                    "BMW",
                    "X5",
                    2023,
                    Mileage.of(15000),
                    VehicleType.SUV,
                    muenchen
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("M-CD 5678"),
                    "VW",
                    "Golf",
                    2022,
                    Mileage.of(25000),
                    VehicleType.COMPACT_CAR,
                    muenchen
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("M-MN 9090"),
                    "Mercedes",
                    "C-Klasse",
                    2024,
                    Mileage.of(8000),
                    VehicleType.SEDAN,
                    muenchen
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("M-MN 3344"),
                    "Audi",
                    "Q7",
                    2023,
                    Mileage.of(18000),
                    VehicleType.SUV,
                    muenchen
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("M-IJ 7890"),
                    "Fiat",
                    "500",
                    2021,
                    Mileage.of(35000),
                    VehicleType.COMPACT_CAR,
                    muenchen
                ));
                
                logger.info("✅ 5 Fahrzeuge in München erstellt");
                
                // ========== FAHRZEUGE - BERLIN ==========
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("B-ST 9900"),
                    "VW",
                    "Transporter",
                    2021,
                    Mileage.of(45000),
                    VehicleType.VAN,
                    berlin
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("B-QR 7788"),
                    "Mercedes",
                    "E-Klasse",
                    2024,
                    Mileage.of(5000),
                    VehicleType.SEDAN,
                    berlin
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("B-OP 5566"),
                    "BMW",
                    "3er",
                    2022,
                    Mileage.of(22000),
                    VehicleType.SEDAN,
                    berlin
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("B-EF 1212"),
                    "Renault",
                    "Clio",
                    2020,
                    Mileage.of(40000),
                    VehicleType.COMPACT_CAR,
                    berlin
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("B-UV 9900"),
                    "Ford",
                    "Transit",
                    2023,
                    Mileage.of(12000),
                    VehicleType.VAN,
                    berlin
                ));
                
                logger.info("✅ 5 Fahrzeuge in Berlin erstellt");
                
                // ========== FAHRZEUGE - HAMBURG ==========
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("HH-UV 1111"),
                    "Audi",
                    "A4",
                    2023,
                    Mileage.of(12000),
                    VehicleType.SEDAN,
                    hamburg
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("HH-YZ 6677"),
                    "Ford",
                    "Kuga",
                    2022,
                    Mileage.of(30000),
                    VehicleType.SUV,
                    hamburg
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("HH-UV 2233"),
                    "Opel",
                    "Corsa",
                    2021,
                    Mileage.of(28000),
                    VehicleType.COMPACT_CAR,
                    hamburg
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("HH-AB 4444"),
                    "Toyota",
                    "RAV4",
                    2024,
                    Mileage.of(7000),
                    VehicleType.SUV,
                    hamburg
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("HH-CD 5555"),
                    "Mercedes",
                    "Sprinter",
                    2022,
                    Mileage.of(35000),
                    VehicleType.VAN,
                    hamburg
                ));
                
                logger.info("✅ 5 Fahrzeuge in Hamburg erstellt");
                
                // ========== FAHRZEUGE - FRANKFURT ==========
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("F-EF 6666"),
                    "BMW",
                    "5er",
                    2023,
                    Mileage.of(16000),
                    VehicleType.SUV,
                    frankfurt
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("F-CD 5555"),
                    "VW",
                    "Polo",
                    2022,
                    Mileage.of(20000),
                    VehicleType.COMPACT_CAR,
                    frankfurt
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("F-IJ 5656"),
                    "Audi",
                    "A6",
                    2024,
                    Mileage.of(9000),
                    VehicleType.SEDAN,
                    frankfurt
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("F-KL 9999"),
                    "Peugeot",
                    "208",
                    2021,
                    Mileage.of(32000),
                    VehicleType.COMPACT_CAR,
                    frankfurt
                ));
                
                vehicleRepository.save(new Vehicle(
                    LicensePlate.of("F-MN 0000"),
                    "Volkswagen",
                    "Crafter",
                    2023,
                    Mileage.of(18000),
                    VehicleType.VAN,
                    frankfurt
                ));
                
                logger.info("✅ 5 Fahrzeuge in Frankfurt erstellt");
                
                // ========== TESTKUNDEN ==========
                logger.info("📝 Erstelle Testkunden...");
                
                Customer testCustomer1 = customerRepository.save(new Customer(
                    "Max",
                    "Mustermann",
                    new Address("Musterstraße 1", "80331", "München"),
                    new DriverLicenseNumber("B1234567890"),
                    "max.mustermann@example.com",
                    "+49 89 12345678",
                    passwordEncoder.encode("Test1234!")
                ));
                testCustomer1.verifyEmail(testCustomer1.generateVerificationToken());
                customerRepository.save(testCustomer1);
                
                Customer testCustomer2 = customerRepository.save(new Customer(
                    "Anna",
                    "Schmidt",
                    new Address("Berliner Str. 42", "10178", "Berlin"),
                    new DriverLicenseNumber("B9876543210"),
                    "anna.schmidt@example.com",
                    "+49 30 98765432",
                    passwordEncoder.encode("Test1234!")
                ));
                testCustomer2.verifyEmail(testCustomer2.generateVerificationToken());
                customerRepository.save(testCustomer2);
                
                Customer testCustomer3 = customerRepository.save(new Customer(
                    "Thomas",
                    "Weber",
                    new Address("Hauptstraße 123", "22335", "Hamburg"),
                    new DriverLicenseNumber("B1122334455"),
                    "thomas.weber@example.com",
                    "+49 40 11223344",
                    passwordEncoder.encode("Test1234!")
                ));
                testCustomer3.verifyEmail(testCustomer3.generateVerificationToken());
                customerRepository.save(testCustomer3);
                
                logger.info("✅ 3 Testkunden erstellt");
                
                // ========== TESTBUCHUNGEN - WEIHNACHTSZEIT & RANDOM ==========
                logger.info("📅 Erstelle Testbuchungen (Weihnachtszeit & zufällige Termine)...");
                
                // Hole alle Fahrzeuge
                var allVehicles = vehicleRepository.findAll();
                
                // Weihnachtszeit: 20. Dezember 2025 - 2. Januar 2026
                LocalDateTime christmasStart = LocalDateTime.of(2025, 12, 20, 10, 0);
                LocalDateTime christmasEnd = LocalDateTime.of(2026, 1, 2, 18, 0);
                
                // Buchung 1: BMW X5 (München) - Weihnachtszeit
                if (allVehicles.size() > 0) {
                    Vehicle bmwX5 = allVehicles.stream()
                        .filter(v -> v.getLicensePlate().getValue().equals("M-GH 3434"))
                        .findFirst().orElse(null);
                    if (bmwX5 != null) {
                        Booking booking1 = new Booking(
                            testCustomer1, bmwX5, muenchen, muenchen,
                            christmasStart, christmasEnd,
                            new BigDecimal("950.00"),
                            new HashSet<>()
                        );
                        booking1.confirm();
                        bookingRepository.save(booking1);
                        logger.info("   📌 BMW X5 blockiert: 20.12.2025 - 02.01.2026 (Weihnachten)");
                    }
                }
                
                // Buchung 2: Mercedes E-Klasse (Berlin) - Weihnachtszeit
                Vehicle mercedesE = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("B-QR 7788"))
                    .findFirst().orElse(null);
                if (mercedesE != null) {
                    Booking booking2 = new Booking(
                        testCustomer2, mercedesE, berlin, berlin,
                        LocalDateTime.of(2025, 12, 23, 14, 0),
                        LocalDateTime.of(2025, 12, 30, 10, 0),
                        new BigDecimal("620.00"),
                        new HashSet<>()
                    );
                    booking2.confirm();
                    bookingRepository.save(booking2);
                    logger.info("   📌 Mercedes E-Klasse blockiert: 23.12.2025 - 30.12.2025 (Weihnachten)");
                }
                
                // Buchung 3: VW Golf (München) - Silvester
                Vehicle vwGolf = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("M-CD 5678"))
                    .findFirst().orElse(null);
                if (vwGolf != null) {
                    Booking booking3 = new Booking(
                        testCustomer3, vwGolf, muenchen, frankfurt,
                        LocalDateTime.of(2025, 12, 29, 9, 0),
                        LocalDateTime.of(2026, 1, 3, 18, 0),
                        new BigDecimal("380.00"),
                        new HashSet<>()
                    );
                    booking3.confirm();
                    bookingRepository.save(booking3);
                    logger.info("   📌 VW Golf blockiert: 29.12.2025 - 03.01.2026 (Silvester)");
                }
                
                // Buchung 4: Audi Q7 (München) - Zufällig in naher Zukunft (2-5 Dez)
                Vehicle audiQ7 = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("M-MN 3344"))
                    .findFirst().orElse(null);
                if (audiQ7 != null) {
                    Booking booking4 = new Booking(
                        testCustomer1, audiQ7, muenchen, hamburg,
                        LocalDateTime.of(2025, 12, 2, 8, 0),
                        LocalDateTime.of(2025, 12, 5, 20, 0),
                        new BigDecimal("420.00"),
                        new HashSet<>()
                    );
                    booking4.confirm();
                    bookingRepository.save(booking4);
                    logger.info("   📌 Audi Q7 blockiert: 02.12.2025 - 05.12.2025");
                }
                
                // Buchung 5: VW Transporter (Berlin) - Zufällig (10-15 Dez)
                Vehicle vwTransporter = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("B-ST 9900"))
                    .findFirst().orElse(null);
                if (vwTransporter != null) {
                    Booking booking5 = new Booking(
                        testCustomer2, vwTransporter, berlin, berlin,
                        LocalDateTime.of(2025, 12, 10, 7, 0),
                        LocalDateTime.of(2025, 12, 15, 19, 0),
                        new BigDecimal("550.00"),
                        new HashSet<>()
                    );
                    booking5.confirm();
                    bookingRepository.save(booking5);
                    logger.info("   📌 VW Transporter blockiert: 10.12.2025 - 15.12.2025");
                }
                
                // Buchung 6: BMW 3er (Berlin) - Zufällig (5-8 Dez)
                Vehicle bmw3er = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("B-OP 5566"))
                    .findFirst().orElse(null);
                if (bmw3er != null) {
                    Booking booking6 = new Booking(
                        testCustomer3, bmw3er, berlin, hamburg,
                        LocalDateTime.of(2025, 12, 5, 12, 0),
                        LocalDateTime.of(2025, 12, 8, 16, 0),
                        new BigDecimal("290.00"),
                        new HashSet<>()
                    );
                    booking6.confirm();
                    bookingRepository.save(booking6);
                    logger.info("   📌 BMW 3er blockiert: 05.12.2025 - 08.12.2025");
                }
                
                // Buchung 7: Audi A4 (Hamburg) - Zufällig (7-12 Dez)
                Vehicle audiA4 = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("HH-UV 1111"))
                    .findFirst().orElse(null);
                if (audiA4 != null) {
                    Booking booking7 = new Booking(
                        testCustomer1, audiA4, hamburg, hamburg,
                        LocalDateTime.of(2025, 12, 7, 10, 0),
                        LocalDateTime.of(2025, 12, 12, 14, 0),
                        new BigDecimal("480.00"),
                        new HashSet<>()
                    );
                    booking7.confirm();
                    bookingRepository.save(booking7);
                    logger.info("   📌 Audi A4 blockiert: 07.12.2025 - 12.12.2025");
                }
                
                // Buchung 8: Toyota RAV4 (Hamburg) - Zufällig (15-20 Dez)
                Vehicle toyotaRAV4 = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("HH-AB 4444"))
                    .findFirst().orElse(null);
                if (toyotaRAV4 != null) {
                    Booking booking8 = new Booking(
                        testCustomer2, toyotaRAV4, hamburg, frankfurt,
                        LocalDateTime.of(2025, 12, 15, 9, 0),
                        LocalDateTime.of(2025, 12, 20, 11, 0),
                        new BigDecimal("520.00"),
                        new HashSet<>()
                    );
                    booking8.confirm();
                    bookingRepository.save(booking8);
                    logger.info("   📌 Toyota RAV4 blockiert: 15.12.2025 - 20.12.2025");
                }
                
                // Buchung 9: VW Polo (Frankfurt) - Kurz und random (3-4 Dez)
                Vehicle vwPolo = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("F-CD 5555"))
                    .findFirst().orElse(null);
                if (vwPolo != null) {
                    Booking booking9 = new Booking(
                        testCustomer3, vwPolo, frankfurt, frankfurt,
                        LocalDateTime.of(2025, 12, 3, 11, 0),
                        LocalDateTime.of(2025, 12, 4, 18, 0),
                        new BigDecimal("95.00"),
                        new HashSet<>()
                    );
                    booking9.confirm();
                    bookingRepository.save(booking9);
                    logger.info("   📌 VW Polo blockiert: 03.12.2025 - 04.12.2025");
                }
                
                // Buchung 10: Audi A6 (Frankfurt) - Weihnachten verlängert
                Vehicle audiA6 = allVehicles.stream()
                    .filter(v -> v.getLicensePlate().getValue().equals("F-IJ 5656"))
                    .findFirst().orElse(null);
                if (audiA6 != null) {
                    Booking booking10 = new Booking(
                        testCustomer1, audiA6, frankfurt, muenchen,
                        LocalDateTime.of(2025, 12, 18, 8, 0),
                        LocalDateTime.of(2026, 1, 5, 17, 0),
                        new BigDecimal("1350.00"),
                        new HashSet<>()
                    );
                    booking10.confirm();
                    bookingRepository.save(booking10);
                    logger.info("   📌 Audi A6 blockiert: 18.12.2025 - 05.01.2026 (lange Weihnachtsreise)");
                }
                
                logger.info("✅ 10 Testbuchungen erstellt");
                
                // ========== ZUSAMMENFASSUNG ==========
                long totalBranches = branchRepository.findAll().size();
                long totalVehicles = vehicleRepository.findAll().size();
                
                logger.info("================================================");
                logger.info("✅ Dateninitialisierung abgeschlossen!");
                logger.info("📍 {} Filialen erstellt", totalBranches);
                logger.info("🚗 {} Fahrzeuge erstellt", totalVehicles);
                logger.info("👤 3 Kunden erstellt");
                logger.info("📅 10 Buchungen erstellt (inkl. Weihnachtszeit!)");
                logger.info("================================================");
                
            } else {
                logger.info("ℹ️  Datenbank enthält bereits Daten - Initialisierung übersprungen");
            }
        };
    }
}
