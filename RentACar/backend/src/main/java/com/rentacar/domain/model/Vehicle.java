package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidMileageException;
import com.rentacar.domain.exception.InvalidVehicleDataException;
import com.rentacar.domain.exception.VehicleStatusTransitionException;
import jakarta.persistence.*;
import java.time.Year;
import java.util.Objects;

/**
 * Vehicle (Fahrzeug) Aggregate Root.
 * 
 * Repräsentiert ein Mietfahrzeug mit allen relevanten Eigenschaften.
 * Business-Logik wird durch expressive Methoden anstelle von Settern implementiert.
 * Alle Invarianten werden in der Entity validiert.
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "license_plate", nullable = false, unique = true))
    private LicensePlate licensePlate;
    
    @Column(nullable = false)
    private String brand;
    
    @Column(nullable = false)
    private String model;
    
    @Column(name = "production_year", nullable = false)
    private Integer year;
    
    @Embedded
    @AttributeOverride(name = "kilometers", column = @Column(name = "mileage", nullable = false))
    private Mileage mileage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    
    /**
     * Standardkonstruktor für JPA.
     */
    protected Vehicle() {
    }
    
    /**
     * Erstellt ein neues Fahrzeug mit Validierung aller Invarianten.
     * 
     * @param licensePlate das Kennzeichen
     * @param brand die Marke
     * @param model das Modell
     * @param year das Baujahr
     * @param mileage der Kilometerstand
     * @param vehicleType der Fahrzeugtyp
     * @param branch die zugeordnete Filiale
     * @throws InvalidVehicleDataException wenn Invarianten verletzt werden
     */
    public Vehicle(
        LicensePlate licensePlate,
        String brand,
        String model,
        Integer year,
        Mileage mileage,
        VehicleType vehicleType,
        Branch branch
    ) {
        validateLicensePlate(licensePlate);
        validateBrand(brand);
        validateModel(model);
        validateYear(year);
        validateMileage(mileage);
        validateVehicleType(vehicleType);
        validateBranch(branch);
        
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.vehicleType = vehicleType;
        this.branch = branch;
        this.status = VehicleStatus.AVAILABLE; // Neues Fahrzeug ist verfügbar
    }
    
    /**
     * Markiert das Fahrzeug als vermietet.
     * 
     * @throws VehicleStatusTransitionException wenn das Fahrzeug nicht verfügbar ist
     */
    public void markAsRented() {
        if (!status.isAvailableForRental()) {
            throw new VehicleStatusTransitionException(
                "Fahrzeug " + licensePlate + " kann nicht vermietet werden. Aktueller Status: " + status,
                status
            );
        }
        this.status = VehicleStatus.RENTED;
    }
    
    /**
     * Markiert das Fahrzeug als verfügbar nach Rückgabe.
     * 
     * @param returnMileage der Kilometerstand bei Rückgabe
     * @throws VehicleStatusTransitionException wenn das Fahrzeug nicht vermietet ist
     * @throws InvalidMileageException wenn der neue Kilometerstand kleiner ist
     */
    public void markAsAvailable(Mileage returnMileage) {
        if (!status.canBeReturned()) {
            throw new VehicleStatusTransitionException(
                "Fahrzeug " + licensePlate + " ist nicht vermietet. Aktueller Status: " + status,
                status
            );
        }
        validateMileage(returnMileage);
        if (returnMileage.isLessThan(this.mileage)) {
            throw new InvalidMileageException(
                "Rückgabe-Kilometerstand (" + returnMileage + 
                ") kann nicht kleiner sein als aktueller Stand (" + this.mileage + ")"
            );
        }
        this.mileage = returnMileage;
        this.status = VehicleStatus.AVAILABLE;
    }
    
    /**
     * Markiert das Fahrzeug als verfügbar (z.B. nach Buchungsstornierung).
     * Kilometerstand bleibt unverändert.
     *
     * Business Rule: Nur möglich, wenn Fahrzeug aktuell RENTED ist.
     *
     * @throws VehicleStatusTransitionException wenn das Fahrzeug nicht vermietet ist
     */
    public void makeAvailable() {
        if (status != VehicleStatus.RENTED) {
            throw new VehicleStatusTransitionException(
                "Fahrzeug " + licensePlate + " kann nur aus Status RENTED verfügbar gemacht werden (aktuell: " + status + ")",
                status
            );
        }
        this.status = VehicleStatus.AVAILABLE;
    }

    /**
     * Markiert das Fahrzeug als in Wartung.
     */
    public void markAsInMaintenance() {
        if (status == VehicleStatus.RENTED) {
            throw new VehicleStatusTransitionException(
                "Fahrzeug " + licensePlate + " kann nicht in Wartung geschickt werden, " +
                "da es aktuell vermietet ist",
                status
            );
        }
        this.status = VehicleStatus.IN_MAINTENANCE;
    }
    
    /**
     * Mustert das Fahrzeug aus (setzt Status auf außer Betrieb).
     */
    public void retire() {
        if (status == VehicleStatus.RENTED) {
            throw new VehicleStatusTransitionException(
                "Fahrzeug " + licensePlate + " kann nicht ausgemustert werden, " +
                "da es aktuell vermietet ist",
                status
            );
        }
        this.status = VehicleStatus.OUT_OF_SERVICE;
    }
    
    /**
     * Versetzt das Fahrzeug an eine andere Filiale.
     * 
     * @param newBranch die neue Filiale
     * @throws InvalidVehicleDataException wenn die Filiale ungültig ist
     * @throws VehicleStatusTransitionException wenn das Fahrzeug vermietet ist
     */
    public void relocateToBranch(Branch newBranch) {
        if (status == VehicleStatus.RENTED) {
            throw new VehicleStatusTransitionException(
                "Fahrzeug " + licensePlate + " kann nicht versetzt werden, " +
                "da es aktuell vermietet ist",
                status
            );
        }
        validateBranch(newBranch);
        this.branch = newBranch;
    }
    
    /**
     * Aktualisiert den Kilometerstand (z.B. nach Wartung).
     * 
     * @param newMileage der neue Kilometerstand
     * @throws InvalidMileageException wenn der neue Kilometerstand kleiner ist
     */
    public void updateMileage(Mileage newMileage) {
        validateMileage(newMileage);
        if (newMileage.isLessThan(this.mileage)) {
            throw new InvalidMileageException(
                "Neuer Kilometerstand (" + newMileage + 
                ") kann nicht kleiner sein als aktueller Stand (" + this.mileage + ")"
            );
        }
        this.mileage = newMileage;
    }
    
    /**
     * Aktualisiert Marke und Modell des Fahrzeugs.
     * 
     * @param newBrand die neue Marke
     * @param newModel das neue Modell
     * @throws InvalidVehicleDataException wenn Marke oder Modell ungültig sind
     */
    public void updateBrandAndModel(String newBrand, String newModel) {
        validateBrand(newBrand);
        validateModel(newModel);
        this.brand = newBrand;
        this.model = newModel;
    }
    
    // Validierungsmethoden
    
    private void validateLicensePlate(LicensePlate licensePlate) {
        if (licensePlate == null) {
            throw new InvalidVehicleDataException("Kennzeichen darf nicht null sein", "licensePlate", null);
        }
    }
    
    private void validateBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new InvalidVehicleDataException("Marke darf nicht null oder leer sein", "brand", brand);
        }
    }
    
    private void validateModel(String model) {
        if (model == null || model.isBlank()) {
            throw new InvalidVehicleDataException("Modell darf nicht null oder leer sein", "model", model);
        }
    }
    
    private void validateYear(Integer year) {
        if (year == null) {
            throw new InvalidVehicleDataException("Baujahr darf nicht null sein", "year", null);
        }
        int currentYear = Year.now().getValue();
        if (year < 1900 || year > currentYear + 1) {
            throw new InvalidVehicleDataException(
                "Baujahr muss zwischen 1900 und " + (currentYear + 1) + " liegen: " + year,
                "year",
                year
            );
        }
    }
    
    private void validateMileage(Mileage mileage) {
        if (mileage == null) {
            throw new InvalidVehicleDataException("Kilometerstand darf nicht null sein", "mileage", null);
        }
    }
    
    private void validateVehicleType(VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new InvalidVehicleDataException("Fahrzeugtyp darf nicht null sein", "vehicleType", null);
        }
    }
    
    private void validateBranch(Branch branch) {
        if (branch == null) {
            throw new InvalidVehicleDataException("Filiale darf nicht null sein", "branch", null);
        }
    }
    
    // Getter
    
    public Long getId() {
        return id;
    }
    
    public LicensePlate getLicensePlate() {
        return licensePlate;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public Mileage getMileage() {
        return mileage;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public VehicleStatus getStatus() {
        return status;
    }
    
    public Branch getBranch() {
        return branch;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", licensePlate=" + licensePlate +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", status=" + status +
                '}';
    }
}
