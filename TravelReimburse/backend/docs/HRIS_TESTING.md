# HRIS Integration - Test-Anleitung

## Implementierte Features

Die HRIS-Integration validiert Reiseanträge gegen bestehende Abwesenheiten (Urlaub, Krankheit, etc.).

### Komponenten

1. **Domain Model**

    - `AbsenceInfo` - Value Object für Abwesenheitsdaten
    - `AbsenceType` - Enum (VACATION, SICK_LEAVE, etc.)
    - `AbsenceStatus` - Enum (PENDING, APPROVED, etc.)

2. **Domain Service**

    - `AbsenceValidationService` - Prüft Überlappungen zwischen Reise und Abwesenheiten

3. **Infrastructure**

    - `HrisClient` - Mock mit Test-Daten

4. **Exception Handling**
    - `AbsenceConflictException` - Bei Konflikten
    - GlobalExceptionHandler liefert HTTP 409 Conflict

---

## Test-Daten (Mock)

Der HrisClient enthält vordefinierte Test-Daten:

### Employee 1

-   **Abwesenheit**: Urlaub vom 10.11. bis 15.11.2025
-   **Status**: APPROVED
-   **Typ**: VACATION

### Employee 2

-   **Abwesenheit**: Krankheit vom 08.11. bis 09.11.2025
-   **Status**: APPROVED
-   **Typ**: SICK_LEAVE

### Employee 3

-   **Keine Abwesenheiten**

---

## Test-Szenarien

### Szenario 1: Erfolgreicher Submit (kein Konflikt)

**Setup**:

1. Erstelle Reiseantrag für Employee 3
2. Reisezeitraum: 10.11. - 15.11.2025

**Erwartetes Ergebnis**:

-   Status wechselt zu SUBMITTED
-   HTTP 200 OK

### Szenario 2: Submit mit Konflikt

**Setup**:

1. Erstelle Reiseantrag für Employee 1
2. Reisezeitraum: 10.11. - 15.11.2025 (überlappt mit Urlaub!)

**Erwartetes Ergebnis**:

-   Status bleibt DRAFT
-   HTTP 409 Conflict
-   Response enthält Konflikt-Details:
    ```json
    {
        "status": 409,
        "message": "Reise kollidiert mit 1 bestehenden Abwesenheit(en)",
        "details": {
            "konflikt_1": "VACATION: 2025-11-10 bis 2025-11-15 (Jahresurlaub)"
        }
    }
    ```

### Szenario 3: Teilüberlappung

**Setup**:

1. Erstelle Reiseantrag für Employee 1
2. Reisezeitraum: 12.11. - 20.11.2025 (überlappt teilweise mit Urlaub)

**Erwartetes Ergebnis**:

-   HTTP 409 Conflict (Teilüberlappungen werden auch erkannt!)

---

## API-Requests zum Testen

### 1. Reiseantrag erstellen (Employee 3 - keine Konflikte)

```http
POST http://localhost:8080/api/travel-requests
Content-Type: application/json

{
  "employeeId": 3,
  "destination": "Berlin",
  "purpose": "Kundenmeeting",
  "startDate": "2025-11-10",
  "endDate": "2025-11-15",
  "estimatedAmount": "1500.00",
  "currency": "EUR"
}
```

Antwort: HTTP 201 Created, merke dir die `id` (z.B. 1)

### 2. Submit ohne Konflikt

```http
POST http://localhost:8080/api/travel-requests/1/submit
```

Antwort: HTTP 200 OK, Status = SUBMITTED

---

### 3. Reiseantrag erstellen (Employee 1 - mit Konflikt)

```http
POST http://localhost:8080/api/travel-requests
Content-Type: application/json

{
  "employeeId": 1,
  "destination": "München",
  "purpose": "Messe",
  "startDate": "2025-11-10",
  "endDate": "2025-11-15",
  "estimatedAmount": "2000.00",
  "currency": "EUR"
}
```

Antwort: HTTP 201 Created, merke dir die `id` (z.B. 2)

### 4. Submit mit Konflikt

```http
POST http://localhost:8080/api/travel-requests/2/submit
```

Antwort: HTTP 409 Conflict

```json
{
    "status": 409,
    "message": "Reise kollidiert mit 1 bestehenden Abwesenheit(en)",
    "timestamp": "2025-11-08T18:20:00",
    "details": {
        "konflikt_1": "VACATION: 2025-11-10 bis 2025-11-15 (Jahresurlaub)"
    }
}
```

---

## Business-Regeln

1. **Nur genehmigte Abwesenheiten zählen**: PENDING/REJECTED werden ignoriert
2. **BUSINESS_TRIP wird ignoriert**: Bereits gemeldete Dienstreisen blockieren nicht
3. **Überlappungserkennung**:
    - Vollständige Überlappung → Konflikt
    - Teilüberlappung → Konflikt
    - Reise umschließt Abwesenheit → Konflikt
    - Abwesenheit umschließt Reise → Konflikt
    - Angrenzende Zeiträume (Ende = Start) → KEIN Konflikt

---

## Architektur (DDD)

```
Presentation Layer
  └─ TravelRequestController
       └─ POST /api/travel-requests/{id}/submit

Application Layer
  └─ TravelRequestService.submitTravelRequest()
       ├─ Ruft AbsenceValidationService
       ├─ Bei Konflikt: throw AbsenceConflictException
       └─ Bei OK: request.submit()

Domain Layer
  ├─ AbsenceValidationService (Domain Service)
  │   └─ validateTravelAgainstAbsences()
  ├─ AbsenceInfo (Value Object)
  └─ AbsenceConflictException

Infrastructure Layer
  └─ HrisClient (Mock mit Test-Daten)
```

---

## Erweiterungsmöglichkeiten

Wenn echtes HRIS verfügbar ist:

1. HrisClient erweitern um REST-Call
2. DTO-Mapping implementieren
3. Retry-Mechanismus hinzufügen
4. Timeout-Handling
5. Konfiguration in application.properties

Aktuell: Einfacher Mock reicht für Entwicklung und Tests!
