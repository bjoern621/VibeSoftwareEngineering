package com.rentacar.infrastructure.security;

/**
 * Konstanten für rollenbasierte Zugriffskontrolle (RBAC).
 * 
 * Diese Klasse definiert wiederverwendbare SpEL-Ausdrücke für @PreAuthorize-Annotationen.
 * 
 * Verwendung:
 * {@code @PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)}
 * 
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
public final class RoleConstants {

    private RoleConstants() {
        // Utility-Klasse: Instanziierung verhindern
        throw new UnsupportedOperationException("RoleConstants ist eine Utility-Klasse und kann nicht instanziiert werden");
    }

    /**
     * SpEL-Ausdruck: Nur Kunden.
     * Verwendung: Buchungen erstellen, eigene Daten anzeigen.
     */
    public static final String CUSTOMER = "hasRole('CUSTOMER')";

    /**
     * SpEL-Ausdruck: Nur Mitarbeiter.
     * Verwendung: Spezifische Mitarbeiter-Operationen (falls nötig).
     */
    public static final String EMPLOYEE = "hasRole('EMPLOYEE')";

    /**
     * SpEL-Ausdruck: Nur Administratoren.
     * Verwendung: System-Administration (falls implementiert).
     */
    public static final String ADMIN = "hasRole('ADMIN')";

    /**
     * SpEL-Ausdruck: Mitarbeiter ODER Administratoren.
     * Verwendung: Fahrzeugverwaltung, Check-in/out, Schadensberichte.
     * 
     * ADMIN = erweiterte Mitarbeiterrechte (keine separaten Endpoints).
     */
    public static final String EMPLOYEE_OR_ADMIN = "hasAnyRole('EMPLOYEE', 'ADMIN')";

    /**
     * SpEL-Ausdruck: Alle authentifizierten Benutzer.
     * Verwendung: Gemeinsame Endpoints (z.B. Buchungsdetails mit Ownership-Check).
     */
    public static final String ANY_AUTHENTICATED = "hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN')";
}
