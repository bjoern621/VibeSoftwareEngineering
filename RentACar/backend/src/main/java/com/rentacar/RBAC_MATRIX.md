# RBAC-Matrix - RENTACAR

## Übersicht

Diese Matrix dokumentiert die Zugriffsberechtigung für jeden API-Endpoint im RENTACAR-System basierend auf den drei Rollen:
- **CUSTOMER**: Registrierte Kunden
- **EMPLOYEE**: Mitarbeiter der Autovermietung  
- **ADMIN**: System-Administratoren (**erweiterte Mitarbeiterrechte**, keine separaten Admin-Endpoints)

**Wichtig:** `ADMIN` und `EMPLOYEE` haben **identische Berechtigungen**. Die ADMIN-Rolle ist für zukünftige Erweiterungen vorgesehen (z.B. Benutzerverwaltung, System-Konfiguration).

## Test-Accounts

| Email | Passwort | Rolle | Verwendung |
|-------|----------|-------|------------|
| `test.customer@example.com` | `Test1234!` | CUSTOMER | Kunde-Tests |
| `test.employee@example.com` | `Test1234!` | EMPLOYEE | Mitarbeiter-Tests |
| `test.admin@example.com` | `Test1234!` | ADMIN | Admin-Tests |

## Legende

| Symbol | Bedeutung |
|--------|-----------|
| ✅ | Zugriff erlaubt |
| ❌ | Zugriff verboten (HTTP 403 Forbidden) |
| 🔓 | Öffentlich (keine Authentifizierung erforderlich) |
| ⚠️ | Eingeschränkter Zugriff (z.B. nur eigene Daten) |

---

## Endpoints nach Modul

### 1. Authentifizierung & Kunden (`/api/kunden`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/kunden/registrierung` | POST | 🔓 | 🔓 | 🔓 | ✅ Implementiert | Öffentliche Registrierung |
| `/api/kunden/login` | POST | 🔓 | 🔓 | 🔓 | ✅ Implementiert | Öffentlicher Login |
| `/api/kunden/profil` | GET | ⚠️ | ❌ | ❌ | ✅ Implementiert | Nur eigenes Profil |
| `/api/kunden/profil` | PUT | ⚠️ | ❌ | ❌ | ✅ Implementiert | Nur eigenes Profil aktualisieren |
| `/api/kunden/verify-email` | POST | 🔓 | 🔓 | 🔓 | ✅ Implementiert | E-Mail-Verifikation per Token |
| `/api/kunden/logout` | POST | ✅ | ✅ | ✅ | ✅ Implementiert | Token-Blacklist, invalidiert Access + Refresh Tokens |
| `/api/auth/refresh` | POST | 🔓 | 🔓 | 🔓 | ✅ Implementiert | Refresh-Token-Rotation (Access 15min, Refresh 7d) |

---

### 2. Buchungen (`/api/buchungen`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/buchungen/preis-berechnen` | POST | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich, sollte evtl. Rate-Limited sein |
| `/api/kunden/meine-buchungen` | GET | ✅ | ❌ | ❌ | ✅ Implementiert | `@PreAuthorize(RoleConstants.CUSTOMER)` |
| `/api/kunden/{id}/buchungen` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/buchungen` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/buchungen` | POST | ✅ | ❌ | ❌ | ✅ Implementiert | `@PreAuthorize(RoleConstants.CUSTOMER)` |
| `/api/buchungen/{id}` | GET | ⚠️ | ✅ | ✅ | ✅ Implementiert | Customer: nur eigene; Employee/Admin: alle |
| `/api/buchungen/{id}/zusatzkosten` | GET | ⚠️ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.ANY_AUTHENTICATED)` |
| `/api/buchungen/{id}/stornieren` | POST | ⚠️ | ✅ | ✅ | ✅ Implementiert | Customer: nur eigene; Employee/Admin: alle |

---

### 3. Fahrzeuge (`/api/fahrzeuge`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/fahrzeuge` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/fahrzeuge/{id}` | PUT | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/fahrzeuge` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich, evtl. einschränken |
| `/api/fahrzeuge/{id}` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich, evtl. einschränken |
| `/api/fahrzeuge/suche` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentliche Suche |
| `/api/fahrzeuge/{id}/ausser-betrieb` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/fahrzeuge/{id}/vermieten` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/fahrzeuge/{id}/zurueckgeben` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/fahrzeuge/{id}/wartung` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/fahrzeuge/{id}/verfuegbar` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |

---

### 4. Fahrzeugtypen (`/api/vehicle-types`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/vehicle-types` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich |
| `/api/vehicle-types/{typeName}` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich |

---

### 5. Filialen (`/api/filialen`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/filialen` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich |
| `/api/filialen/{id}` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich |

---

### 6. Vermietung (Rental) (`/api/vermietung`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/vermietung/{buchungId}/checkout` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/vermietung/{buchungId}/checkin` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |

---

### 7. Schadensberichte (`/api/schadensberichte`, `/api/vermietung/.../schadensbericht`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/vermietung/{buchungId}/schadensbericht` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/schadensberichte/{id}` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |
| `/api/vermietung/{buchungId}/schadensberichte` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)` |

---

## Zusammenfassung: Implementierungsstatus

### ✅ Vollständig Implementiert

1. **Logout-Funktionalität** (`POST /api/kunden/logout`)
   - Token-Blacklist mit Caffeine Cache
   - Invalidiert Access + Refresh Tokens
   - **Issue #3 - abgeschlossen**

2. **Refresh-Token-Mechanismus** (`POST /api/auth/refresh`)
   - Refresh-Token-Rotation (One-time use)
   - Access Token: 15min, Refresh Token: 7 Tage
   - **Issue #6 - abgeschlossen**

3. **RBAC Guards** (`@PreAuthorize`)
   - RoleConstants utility class für wiederverwendbare SpEL-Ausdrücke
   - Konsistente Rechteprüfung über alle Controller
   - **Issue #137 - abgeschlossen**

---

### ⚠️ Sicherheitsbedenken

#### 1. Öffentliche Endpoints ohne Rate Limiting

Folgende Endpoints sind öffentlich zugänglich und sollten Rate-Limited werden:

- `POST /api/kunden/login` ➔ **Issue #2** (Rate Limiting für Login)
- `POST /api/kunden/registrierung` ➔ Evtl. Rate Limiting erwägen
- `POST /api/buchungen/preis-berechnen` ➔ Evtl. Rate Limiting erwägen
- `GET /api/fahrzeuge*` ➔ Öffentlich (OK für Browse-Funktionalität)
- `GET /api/vehicle-types*` ➔ Öffentlich (OK)
- `GET /api/filialen*` ➔ Öffentlich (OK)

**Empfehlung:**
- Login-Endpoint **muss** Rate-Limited werden (Max. 5 Versuche / 15 Min)
- Registrierung sollte Rate-Limited werden (Max. 3 Accounts / Stunde pro IP)
- Preisberechnung kann Rate-Limited werden (Max. 20 Anfragen / Minute)

---

#### 2. Customer-Endpoints mit eingeschränktem Zugriff

Diese Endpoints erlauben Kunden nur Zugriff auf **eigene Daten**:

| Endpoint | Prüfung | Status |
|----------|---------|--------|
| `GET /api/kunden/profil` | Username aus JWT | ✅ Implementiert |
| `PUT /api/kunden/profil` | Username aus JWT | ✅ Implementiert |
| `GET /api/kunden/meine-buchungen` | Customer-ID aus JWT | ✅ Implementiert |
| `POST /api/buchungen` | Customer-ID aus JWT | ✅ Implementiert |
| `GET /api/buchungen/{id}` | Ownership-Check | ✅ Implementiert (in Service-Layer) |
| `POST /api/buchungen/{id}/stornieren` | Ownership-Check | ✅ Implementiert (in Service-Layer) |

**Status:** Alle korrekt implementiert mit Ownership-Checks in Service-Layer.

---

## Best Practices & Empfehlungen

### 1. ✅ Korrekt implementiert

- **Alle sensiblen Endpoints** (Fahrzeugverwaltung, Vermietung, Schadensberichte) sind mit `@PreAuthorize` abgesichert
- **Method Security aktiviert** (`@EnableMethodSecurity` in `SecurityConfig`)
- **JWT-basierte Authentifizierung** funktioniert (Access Token 15min, Refresh Token 7 Tage)
- **Ownership-Checks** für kundenspezifische Daten vorhanden
- **Token-Blacklist** mit Caffeine Cache (Logout invalidiert Access + Refresh Tokens)
- **Refresh-Token-Rotation** (One-time use, automatische Revokation bei Logout)
- **Rate Limiting** für Login implementiert (Bucket4j)
- **RoleConstants** für wiederverwendbare `@PreAuthorize`-Ausdrücke
- **Global Exception Handlers** für 403/401 (AccessDeniedException, AuthenticationException)
- **Integration Tests** für RBAC (17 Test-Fälle in `RBACIntegrationTest.java`)
- **Frontend RBAC UI** (`EmployeeRoute`, conditional Navbar rendering)

### 2. ⚠️ Verbesserungspotenzial

- **Rate Limiting für öffentliche Endpoints** (z.B. Fahrzeugsuche) - DoS-Schutz
- **Audit-Logging** für sicherheitsrelevante Aktionen (NFR5)
- **Admin-spezifische Endpoints** (Benutzerverwaltung, System-Konfiguration) - aktuell nicht in Anforderungen

### 3. 🔧 Implementation Details

**RBAC Guards (`@PreAuthorize`) - RoleConstants Pattern:**
```java
// src/main/java/com/rentacar/util/RoleConstants.java
public final class RoleConstants {
    // Einzelne Rollen
    public static final String CUSTOMER = "hasRole('CUSTOMER')";
    public static final String EMPLOYEE = "hasRole('EMPLOYEE')";
    public static final String ADMIN = "hasRole('ADMIN')";
    
    // Kombinierte Rollen
    public static final String EMPLOYEE_OR_ADMIN = "hasAnyRole('EMPLOYEE', 'ADMIN')";
    public static final String ANY_AUTHENTICATED = "hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN')";
    
    private RoleConstants() {
        throw new AssertionError("RoleConstants darf nicht instanziiert werden");
    }
}

// Controller Example - Konsistente Verwendung
@PostMapping("/api/fahrzeuge")
@PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)
public ResponseEntity<VehicleResponseDTO> createVehicle(@Valid @RequestBody CreateVehicleRequestDTO request) {
    // ...
}

@PostMapping("/api/buchungen")
@PreAuthorize(RoleConstants.CUSTOMER)
public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody CreateBookingRequestDTO request) {
    // ...
}
```

**Vorteile des RoleConstants-Patterns:**
- ✅ Single Source of Truth für alle SpEL-Ausdrücke
- ✅ IDE-Autocomplete und Refactoring-Unterstützung
- ✅ Verhindert Tippfehler in `@PreAuthorize`-Annotations
- ✅ Einfache Anpassung bei zukünftigen Änderungen

**Security Configuration:**
- `SecurityConfig.java`: HTTP-Methoden-basierte Zugriffskontrolle (GET public, POST/PUT/DELETE authenticated)
- `JwtAuthenticationFilter.java`: Token-Validation + Blacklist-Check
- `JwtAuthenticationEntryPoint.java`: Unified 401 response für unauthenticated requests
- `GlobalExceptionHandler.java`: Custom error responses für 403/401

**Frontend Defense-in-Depth:**
- `EmployeeRoute`: Guards employee pages (redirect to `/` if not EMPLOYEE/ADMIN)
- `Navbar.js`: Conditional rendering (employee links only for EMPLOYEE/ADMIN)
- Pages: Additional role checks (redundant security layer)



