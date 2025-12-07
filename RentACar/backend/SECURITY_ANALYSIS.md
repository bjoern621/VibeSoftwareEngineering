# Security-Audit mit OWASP ZAP (Stand: 07.12.2025)

## Kontext

Im Rahmen des Tickets #139 wurde ein OWASP ZAP Baseline-Scan gegen die laufende RENTACAR-Anwendung (Frontend & Backend) durchgeführt.

## Testsetup

-   **Tool:** OWASP ZAP (Baseline-Scan via Docker `zaproxy/zap-stable`)
-   **Ziel-URLs:**
    -   Frontend: http://localhost:3000
    -   Backend: http://localhost:8080
-   **Datum:** 07.12.2025
-   **Reports:**
    -   `backend/SECURITY/zap-report.html` (Frontend)
    -   `backend/SECURITY/zap-report-backend.html` (Backend)
    -   `backend/SECURITY/zap-report-ajax.html` (Frontend mit Ajax Spider)

## Zusammenfassung der Ergebnisse

Der Scan hat insgesamt **10 Warnungen** (Warnings) im Frontend und **1 Warnung** im Backend identifiziert. Es wurden keine kritischen Fehler (FAIL) gefunden.

### Frontend (React + Nginx)

Das Frontend zeigt typische fehlende Sicherheitsheader, die im Nginx nachgerüstet werden sollten. Der zusätzliche **Ajax Spider Scan** hat bestätigt, dass auch auf Unterseiten (wie `/datenschutz`, `/faq`, `/kontakt`) dieselben Header fehlen. Es wurden keine neuen, kritischen Schwachstellen durch den Ajax Spider gefunden.

### Backend (Spring Boot)

Das Backend antwortet auf der Root-URL mit `401 Unauthorized`, was korrekt ist (Security by Default). Der Scan konnte daher nur die HTTP-Header der Fehlerantwort prüfen. Hier wurde lediglich "Non-Storable Content" angemerkt, was bei Fehlerseiten akzeptabel ist.

## Top 5 Schwachstellen (priorisiert)

### 1. Content Security Policy (CSP) Header Not Set

-   **Beschreibung:** Der `Content-Security-Policy` Header fehlt. Dieser Header hilft, Cross-Site Scripting (XSS) und Data Injection Angriffe zu verhindern.
-   **Betroffene Endpunkte:** `/`, `/sitemap.xml` (Frontend)
-   **Empfehlung:** CSP-Header in `nginx.conf` konfigurieren, um erlaubte Quellen für Skripte, Styles und Bilder einzuschränken.

### 2. Missing Anti-clickjacking Header

-   **Beschreibung:** Der `X-Frame-Options` oder `Content-Security-Policy: frame-ancestors` Header fehlt. Dies ermöglicht es Angreifern, die Seite in einem iFrame einzubetten (Clickjacking).
-   **Betroffene Endpunkte:** `/`, `/sitemap.xml` (Frontend)
-   **Empfehlung:** `X-Frame-Options: DENY` oder `SAMEORIGIN` in `nginx.conf` setzen.

### 3. X-Content-Type-Options Header Missing

-   **Beschreibung:** Der Header `X-Content-Type-Options: nosniff` fehlt. Browser könnten versuchen, den Content-Type zu erraten (MIME-Sniffing), was zu Sicherheitsrisiken führen kann.
-   **Betroffene Endpunkte:** `/`, `/favicon.ico`, `/logo192.png`, etc. (Frontend)
-   **Empfehlung:** Header `X-Content-Type-Options: nosniff` global in `nginx.conf` setzen.

### 4. Server Leaks Version Information

-   **Beschreibung:** Der `Server` Header gibt Versionsinformationen preis (z.B. `nginx/1.x.x`). Dies erleichtert Angreifern die Suche nach bekannten Schwachstellen.
-   **Betroffene Endpunkte:** Alle (Frontend)
-   **Empfehlung:** `server_tokens off;` in der Nginx-Konfiguration setzen.

### 5. Permissions Policy Header Not Set

-   **Beschreibung:** Der `Permissions-Policy` Header fehlt. Dieser erlaubt die Steuerung von Browser-Features (z.B. Kamera, Mikrofon, Geolocation).
-   **Betroffene Endpunkte:** `/`, `/static/js/...` (Frontend)
-   **Empfehlung:** Header setzen und nicht benötigte Features deaktivieren.

## Weitere Beobachtungen

-   **Modern Web Application:** ZAP hat erkannt, dass die Anwendung stark auf JavaScript setzt. Für zukünftige, tiefere Scans sollte der **Ajax Spider** verwendet werden, um alle dynamischen Routen zu erfassen.
-   **Suspicious Comments:** In den JS-Dateien wurden Kommentare gefunden, die geprüft werden sollten (oft harmlos, aber keine Secrets committen).
-   **Timestamp Disclosure:** Unix-Timestamps in JS-Dateien (meist Build-Artefakte).

## Nächste Schritte

-   Erstellung von Tickets zur Implementierung der fehlenden Security-Header in der `frontend/nginx.conf`.
-   Für das Backend sind aktuell keine akuten Maßnahmen aus dem Baseline-Scan nötig, da der Zugriff korrekt blockiert wurde (401).
