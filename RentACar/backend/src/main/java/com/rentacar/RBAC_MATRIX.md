# RBAC-Matrix - RENTACAR

## Übersicht

Diese Matrix dokumentiert die Zugriffsberechtigung für jeden API-Endpoint im RENTACAR-System basierend auf den drei Rollen:
- **CUSTOMER**: Registrierte Kunden
- **EMPLOYEE**: Mitarbeiter der Autovermietung
- **ADMIN**: System-Administratoren

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
| `/api/auth/logout` | POST | ✅ | ✅ | ✅ | ❌ **FEHLT** | Logout-Funktionalität implementieren |
| `/api/auth/refresh` | POST | ✅ | ✅ | ✅ | ❌ **FEHLT** | Refresh-Token-Mechanismus |

---

### 2. Buchungen (`/api/buchungen`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/buchungen/preis-berechnen` | POST | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich, sollte evtl. Rate-Limited sein |
| `/api/kunden/meine-buchungen` | GET | ✅ | ❌ | ❌ | ✅ Implementiert | `@PreAuthorize("hasRole('CUSTOMER')")` |
| `/api/kunden/{id}/buchungen` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/buchungen` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | Alle Buchungen, `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/buchungen` | POST | ✅ | ❌ | ❌ | ✅ Implementiert | `@PreAuthorize("hasRole('CUSTOMER')")` |
| `/api/buchungen/{id}` | GET | ⚠️ | ✅ | ✅ | ✅ Implementiert | Customer: nur eigene; Employee/Admin: alle |
| `/api/buchungen/{id}/zusatzkosten` | GET | ⚠️ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE', 'ADMIN')")` |
| `/api/buchungen/{id}/stornieren` | POST | ⚠️ | ✅ | ✅ | ✅ Implementiert | Customer: nur eigene; Employee/Admin: alle |

---

### 3. Fahrzeuge (`/api/fahrzeuge`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/fahrzeuge` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/fahrzeuge/{id}` | PUT | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/fahrzeuge` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich, evtl. einschränken |
| `/api/fahrzeuge/{id}` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentlich, evtl. einschränken |
| `/api/fahrzeuge/suche` | GET | 🔓 | 🔓 | 🔓 | ⚠️ | Öffentliche Suche |
| `/api/fahrzeuge/{id}/ausser-betrieb` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/fahrzeuge/{id}/vermieten` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/fahrzeuge/{id}/zurueckgeben` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/fahrzeuge/{id}/wartung` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/fahrzeuge/{id}/verfuegbar` | PATCH | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |

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
| `/api/vermietung/{buchungId}/checkout` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")` |
| `/api/vermietung/{buchungId}/checkin` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")` |

---

### 7. Schadensberichte (`/api/schadensberichte`, `/api/vermietung/.../schadensbericht`)

| Endpoint | HTTP Methode | CUSTOMER | EMPLOYEE | ADMIN | Status | Anmerkungen |
|----------|--------------|----------|----------|-------|--------|-------------|
| `/api/vermietung/{buchungId}/schadensbericht` | POST | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/schadensberichte/{id}` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |
| `/api/vermietung/{buchungId}/schadensberichte` | GET | ❌ | ✅ | ✅ | ✅ Implementiert | `@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")` |

---

## Zusammenfassung: Fehlende Implementierungen

### ❌ Kritische Lücken

1. **Logout-Funktionalität** (`POST /api/auth/logout`)
   - Fehlt komplett
   - Notwendig für Token-Invalidierung
   - **Issue #3**

2. **Refresh-Token-Mechanismus** (`POST /api/auth/refresh`)
   - Fehlt komplett
   - Verbessert Sicherheit durch kürzere Access-Token-Laufzeit
   - **Issue #6**

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

- Alle sensiblen Endpoints (Fahrzeugverwaltung, Vermietung, Schadensberichte) sind mit `@PreAuthorize` abgesichert
- Method Security ist aktiviert (`@EnableMethodSecurity`)
- JWT-basierte Authentifizierung funktioniert
- Ownership-Checks für kundenspezifische Daten vorhanden

### 2. ⚠️ Verbesserungspotenzial

- **Rate Limiting für Login** implementieren ➔ **Issue #2**
- **Logout & Token-Invalidierung** implementieren ➔ **Issue #3**
- **Refresh-Token** für kürzere Access-Token-Laufzeit ➔ **Issue #6**
- Evtl. Rate Limiting für öffentliche Endpoints (DoS-Schutz)



