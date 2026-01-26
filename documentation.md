# Project A: Concert Comparison / Ticketing

## Aufgabenstellung / Abgrenzung

Entwicklung einer skalierbaren Ticketing-Anwendung für Konzerte mit Fokus auf Transaktionssicherheit bei hohem Lastaufkommen.

Wesentliche Anwendungsfälle:

- Event-Management: Anlegen von Konzerten und Sitzplatz-Kategorien.
- Discovery: Vergleich von Veranstaltungen nach Preis, Datum und Verfügbarkeit.
- Monitoring: Echtzeit-Prüfung der aktuellen Platzverfügbarkeit.
- Booking: Zeitlich begrenzte Reservierung und finaler Ticketerwerb.

## User Stories

### US-01 — Seats für ein Konzert anzeigen (Verfügbarkeit)

**Als** Besucher:in
**möchte ich** die verbleibenden verfügbaren Plätze (z. B. pro Kategorie oder Block) sehen,
**damit** ich einschätzen kann, ob sich ein Kauf noch lohnt.

**Akzeptanzkriterien**

- Anzeige von:
    - Kategorien/Blöcken (z. B. Innenraum, Rang A) **oder** Sitzplan
    - Anzahl verfügbarer Plätze pro Kategorie
- Keine negativen oder inkonsistenten Werte
- Daten entsprechen dem aktuellen Systemzustand

**Priorität:** Must

### US-02 — Platz reservieren

**Als** kaufende Person
**möchte ich** beim Auswählen eines Platzes eine zeitlich begrenzte Reservierung erhalten,
**damit** mir der Platz während des Checkouts nicht weggekauft wird.

**Akzeptanzkriterien**

- Beim Auswählen eines Platzes wird ein Hold erzeugt mit:
    - Seat-ID(s)
    - Hold-ID
    - Ablaufzeitpunkt (TTL)
- Solange der Hold aktiv ist:
    - Platz zählt nicht mehr als verfügbar
- Nach Ablauf der TTL:
    - Hold wird automatisch freigegeben

**Priorität:** Must

### US-03 — Ticket kaufen

**Als** kaufende Person
**möchte ich** meinen reservierten Platz kaufen,
**damit** ich ein gültiges Ticket erhalte.

**Akzeptanzkriterien**

- Checkout akzeptiert nur aktive, nicht abgelaufene Holds
- Erfolgreicher Kauf setzt Seat-Status von HELD → SOLD
- Rückgabe einer Bestätigung (Order-ID/Ticket-ID)
- Ein verkaufter Platz kann nicht erneut gekauft werden

**Priorität:** Must

### US-04 — Kein Platz darf doppelt verkauft werden

**Als** Betreiber:in
**möchte ich** sicherstellen, dass ein Platz maximal einmal verkauft wird,
**damit** keine Überbuchungen entstehen.

**Akzeptanzkriterien**

- Bei parallelen Kaufversuchen auf denselben Platz:
    - Genau ein Request ist erfolgreich
    - Alle anderen erhalten einen klaren Fehler (z. B. 409 CONFLICT)
- Verhalten ist durch automatisierte Concurrency-Tests abgesichert

**Priorität:** Must

### US-05 — Verfügbarkeitsübersicht aktuell halten

**Als** Besucher:in
**möchte ich** sehen, wie sich die Verfügbarkeit während des Ticketverkaufs verändert,
**damit** ich schnell reagieren kann.

**Akzeptanzkriterien**

- Aktualisierung per Polling oder Push (z. B. SSE/WebSocket)
- Keine inkonsistenten oder negativen Seat-Zahlen
- Lastspitzen führen nicht zum Systemkollaps

**Priorität:** Should

### US-06 — Verständliche Fehlermeldungen

**Als** kaufende Person
**möchte ich** klare Rückmeldungen erhalten, wenn ein Platz nicht mehr verfügbar ist,
**damit** ich schnell neu wählen kann.

**Akzeptanzkriterien**

- Klare, nicht-technische Fehlermeldungen
- Eindeutige Fehler-ID für Logging/Support
- Alternative Handlung wird angeboten (z. B. andere Plätze anzeigen)

**Priorität:** Should

### US-07 — Konzerte vergleichen

**Als** Nutzer:in
**möchte ich** Konzerte nach Kriterien vergleichen können,
**damit** ich das beste Angebot finde.

**Akzeptanzkriterien**

- Anzeige von:
    - Eventname
    - Datum
    - Ort
    - Einstiegspreis
    - Verfügbarkeitsindikator
- Filter- und Sortierfunktionen sind deterministisch

**Priorität:** Could

### US-08 — Rate Limiting & Basisschutz

**Als** Betreiber:in
**möchte ich** übermäßige oder automatisierte Zugriffe begrenzen,
**damit** das System bei hohem Traffic stabil bleibt.

**Akzeptanzkriterien**

- Konfigurierbare Limits pro IP/User
- Überschreitungen liefern HTTP 429
- Limits sind messbar und nachvollziehbar

**Priorität:** Should

### US-09 — Konzert und Seats anlegen

**Als** Admin
**möchte ich** ein Konzert mit Sitzplätzen oder Kategorien anlegen,
**damit** der Verkauf starten kann.

**Akzeptanzkriterien**

- Seats/Kategorien sind eindeutig definiert
- Initialzustand aller Seats: AVAILABLE
- Validierung: Gesamtanzahl Seats > 0

**Priorität:** Must

## Architektur

### High-Level System Übersicht

[![](https://mermaid.ink/img/pako:eNptUl1v2kAQ_CvWPjUSX8YYjFVFCjiotJA4IU_BeTjwAqfiO2vvXJUC_71ru0kTkZNs3c3s7s2M7ghrnSKEsCWR75zZY6IcXqZY1UAC471EZZ2ZOCAlUNPlmtwuE5iQVhZV-nVF7esvP3SWa4Ul0lwJI5HsVQIvdQ9XJepi-gLpF9Ll9Jt4uuTPaTtTHkcbsca66OV9Tby8yfO9XAsrtbrgo_s5S4x0JuQ_spY51oTOqDBSoTHOTG_l-r_Mck3vJsup2pAwloq1Lejj3Z87iZGMNGz9Vel7O9GIlSweZk4krOBosFYSa2O3hEy05wf-f5rW5NZpNq9P356e4vb3xf3dqQynpsqEmCuTeAXiCmDrHwG2VAO8qcaVYh4KpMOJxdVUNKqYRzTF3p7KSmjwu5AphBuxN9iADInD5DMcy5YE7A4zTCDkbSroZ2n5zE25UM9aZxByftxGutju3oYUeSosRlJwctkbSuwYaawLZSF0O_6wmgLhEX5D2PSD1qATDIO-73X7bs_1G3CA0Ot5rcAb9rt-p9cN_G7PPzfgT3VxpzUYdDwv6Lv9oe92B67XAEyl1TSv33v17M9_AZZl5h0?type=png)](https://mermaid.live/edit#pako:eNptUl1v2kAQ_CvWPjUSX8YYjFVFCjiotJA4IU_BeTjwAqfiO2vvXJUC_71ru0kTkZNs3c3s7s2M7ghrnSKEsCWR75zZY6IcXqZY1UAC471EZZ2ZOCAlUNPlmtwuE5iQVhZV-nVF7esvP3SWa4Ul0lwJI5HsVQIvdQ9XJepi-gLpF9Ll9Jt4uuTPaTtTHkcbsca66OV9Tby8yfO9XAsrtbrgo_s5S4x0JuQ_spY51oTOqDBSoTHOTG_l-r_Mck3vJsup2pAwloq1Lejj3Z87iZGMNGz9Vel7O9GIlSweZk4krOBosFYSa2O3hEy05wf-f5rW5NZpNq9P356e4vb3xf3dqQynpsqEmCuTeAXiCmDrHwG2VAO8qcaVYh4KpMOJxdVUNKqYRzTF3p7KSmjwu5AphBuxN9iADInD5DMcy5YE7A4zTCDkbSroZ2n5zE25UM9aZxByftxGutju3oYUeSosRlJwctkbSuwYaawLZSF0O_6wmgLhEX5D2PSD1qATDIO-73X7bs_1G3CA0Ot5rcAb9rt-p9cN_G7PPzfgT3VxpzUYdDwv6Lv9oe92B67XAEyl1TSv33v17M9_AZZl5h0)

**Frontend** (Komponenten-basiert):

- Hier werden die UI-Elemente (z. B. SeatMap, CheckoutForm, ConcertList) isoliert entwickelt.
- Die Komponenten kommunizieren über REST-Aufrufe mit dem Backend.

**Backend** (Domain-Driven Design):

- Domain Layer: Enthält die gesamte Geschäftslogik (z. B. "Ist ein Platz frei?", "Reservierung abgelaufen?"). Dieser Kern ist unabhängig von Frameworks oder Datenbanken.
- Application Layer: Steuert den Ablauf (Use Cases), z. B. "Platz reservieren und Hold in DB speichern".
- Infrastructure Layer: Kümmert sich um technische Details wie SQL-Zugriffe (JPA/Hibernate) oder E-Mails.

**Persistenz**:

- Eine relationale Datenbank (z. B. PostgreSQL) eignet sich am besten für transaktionelle Sicherheit und ACID-Eigenschaften, die wichtig sind, um Doppelverkäufe zu verhindern.

### Backend DDD

[![](https://mermaid.ink/img/pako:eNp9VG1vmzAQ_iuWpUmtRF6A5g1NldJQaZ1aNWu6fRjpBwMX4pXYyDbRsrb_fQcmCVW28gF8d_Zz9zx3-IUmMgUa0EyxYk0ew6Ug-Ogyto4lnSvQIAwzXApyy3agltRuqp6ZFEbJPAcVPVwvHlv251j1Lq9FWkgujA5ID7YIox3SW8s8xU9c7p4sEIh0KU4ST4si58l_8i5AbXkCUXtT49N15nuVrEEbxUGVIiMpKPJdA5kxDfrDtKHcMN5kJGczqeC8nXiaZQoyZkBHx2WdsVORT0CZxloA2y_vFeZ_OoL8YHkJ9_EvSIyOaoM0VnPgTgrYOTXEAqUvdeuwLXBPNmrqfUe-Q-aKJxx538qMJ62CyHTLeM5innOzI7M1JM8t6I90uRErxVDQMjGlgtOOPEAhNTcSFddR26izV7kPTmRWK3J0tOldoYwrnkXhVSWoQFWq5vbI1_kU3194DEqg6B82cQ5Kc20A-_Fuar7dhlfRGX5IyAyLcRjOT3E-fSIhFOjA41i_dR4Hm3Q6l3u5bawx6sBxKE5j7zt3Gm-rZqNtj4Vo1LHhvWVLqshZ_ymPHbkR20oUlPLsRhhQK4Y14H-x4gJSghNkq3MI3xQ5bPBfte667-f_KqfbuXw9bNavLerUwfuEpzRYsVyDQzegEBtt-lIBLalZ46ElDXCZMvVc9egNDxVM_JRyQwMcMzymZJmtDyBlkSJ2yBk2eXPwqoqfmslSGBp4_RqDBi_0Nw3c8aQ7HvXHk4E7ufDdoec7dIdud9B1--7A90ee5_ueN3hz6J86bb87Grj-xBuN3ZF_cTEcDh0KaUX4zt6S9WX59hdvq7Aj?type=png)](https://mermaid.live/edit#pako:eNp9VG1vmzAQ_iuWpUmtRF6A5g1NldJQaZ1aNWu6fRjpBwMX4pXYyDbRsrb_fQcmCVW28gF8d_Zz9zx3-IUmMgUa0EyxYk0ew6Ug-Ogyto4lnSvQIAwzXApyy3agltRuqp6ZFEbJPAcVPVwvHlv251j1Lq9FWkgujA5ID7YIox3SW8s8xU9c7p4sEIh0KU4ST4si58l_8i5AbXkCUXtT49N15nuVrEEbxUGVIiMpKPJdA5kxDfrDtKHcMN5kJGczqeC8nXiaZQoyZkBHx2WdsVORT0CZxloA2y_vFeZ_OoL8YHkJ9_EvSIyOaoM0VnPgTgrYOTXEAqUvdeuwLXBPNmrqfUe-Q-aKJxx538qMJ62CyHTLeM5innOzI7M1JM8t6I90uRErxVDQMjGlgtOOPEAhNTcSFddR26izV7kPTmRWK3J0tOldoYwrnkXhVSWoQFWq5vbI1_kU3194DEqg6B82cQ5Kc20A-_Fuar7dhlfRGX5IyAyLcRjOT3E-fSIhFOjA41i_dR4Hm3Q6l3u5bawx6sBxKE5j7zt3Gm-rZqNtj4Vo1LHhvWVLqshZ_ymPHbkR20oUlPLsRhhQK4Y14H-x4gJSghNkq3MI3xQ5bPBfte667-f_KqfbuXw9bNavLerUwfuEpzRYsVyDQzegEBtt-lIBLalZ46ElDXCZMvVc9egNDxVM_JRyQwMcMzymZJmtDyBlkSJ2yBk2eXPwqoqfmslSGBp4_RqDBi_0Nw3c8aQ7HvXHk4E7ufDdoec7dIdud9B1--7A90ee5_ueN3hz6J86bb87Grj-xBuN3ZF_cTEcDh0KaUX4zt6S9WX59hdvq7Aj)

### Datenbank Modell

[![](https://mermaid.ink/img/pako:eNqNVG1vmzAQ_iuWP1MUIOSFbzRha7W0mSiqtCoScuFGrAY7MvbWNM1_n3FoQhO61p_su-funoc7boszngMOMIgpJYUg5YIhfSbz20kUJ-j19eKCb9FdFCYoQBlnklBWdWKi--g2SSdhEn2fx780ekkaoIluZUqv5rOpBiwwrdASVjl63CzwOXYeT6M4vU6iGwOuuEZS9oZ8T2K7f9ZHKapxOfr542irpKCsQIyUcDTmRIKkJZjLGfYPMNVYd-2KJzq_XlhLeLEvbRQTbQjfZBgikNGSrNBa0KyrpPkonxQyNt2gDIRM9fXbmU-LLLjYnDobhpUkUlWaY3gfXs_Cy1lkoatoNrXQne5Wm20TIPjfo40yiZgqH0F8QH_f869oqIB0C1AViFTyJ2AdLcyEDoM8JbLDCc9rKqA6ON-xa03ZKT0ucl2yi8spyfOMn2n9P_NmICSXZJW2xwJbuBA0x8FvsqrAwiWIktRvbOotsFyCnnFc_zA5EU914-qgNWEPnJc4kELpMMFVsTwkUeuaTbMADhBgWv2EKyZx4LgmBQ62-Fm_RmN7NOyNxr4z7nvOwPUsvNFmx7ednuN73tB1Pc91_Z2FX0zVnj30HW_sDkfO0Ov3B4OBhSGnkoub_f4xa2j3D7ziUEY?type=png)](https://mermaid.live/edit#pako:eNqNVG1vmzAQ_iuWP1MUIOSFbzRha7W0mSiqtCoScuFGrAY7MvbWNM1_n3FoQhO61p_su-funoc7boszngMOMIgpJYUg5YIhfSbz20kUJ-j19eKCb9FdFCYoQBlnklBWdWKi--g2SSdhEn2fx780ekkaoIluZUqv5rOpBiwwrdASVjl63CzwOXYeT6M4vU6iGwOuuEZS9oZ8T2K7f9ZHKapxOfr542irpKCsQIyUcDTmRIKkJZjLGfYPMNVYd-2KJzq_XlhLeLEvbRQTbQjfZBgikNGSrNBa0KyrpPkonxQyNt2gDIRM9fXbmU-LLLjYnDobhpUkUlWaY3gfXs_Cy1lkoatoNrXQne5Wm20TIPjfo40yiZgqH0F8QH_f869oqIB0C1AViFTyJ2AdLcyEDoM8JbLDCc9rKqA6ON-xa03ZKT0ucl2yi8spyfOMn2n9P_NmICSXZJW2xwJbuBA0x8FvsqrAwiWIktRvbOotsFyCnnFc_zA5EU914-qgNWEPnJc4k80Wv2EKyZx4LgmBQ62-Fm_RmN7NOyNxr4z7nvOwPUsvNFmx7ednuN73tB1Pc91_Z2FX0zVnj30HW_sDkfO0Ov3B4OBhSGnkoub_f4xa2j3D7ziUEY)

---

## IST-ZUSTAND: System Status & Testergebnisse

### Implementierte Features

- Event-Management mit Sitzplatz-Kategorien und Blöcken
- Event Discovery mit Filterung und Sortierung (Datum, Ort, Preis, Verfügbarkeit)
- Echtzeit-Prüfung der Platzverfügbarkeit
- Zeitlich begrenzte Reservierung (Hold) mit TTL
- Finaler Ticketerwerb mit Order-Generierung
- Optimistic Locking via JPA @Version für alle kritischen Entities
- Race Condition Prevention bei parallelen Zugriffen auf denselben Platz
- Keine Doppelverkäufe unter extremer Last garantiert
- Caching (Caffeine) für Performance-Optimierung
- Scheduled Jobs für automatische Hold-Freigabe nach TTL-Ablauf
- Spring Security mit JWT Authentication
- Rollenbasierte Zugriffsrechte (USER, ADMIN)
- Bucket4j für Rate Limiting und Schutz vor übermäßigen Zugriffen

### Load Testing (Gatling)

Das System wurde mit Gatling unter verschiedenen Lastszenarien getestet:

**BasicSmokeTest:**

- Erfolgsquote: 100%
- Durchschnittliche Antwortzeit: 34ms
- Szenarien: Health Check, Verfügbarkeit, einzelne Holds, gleichzeitige Holds

**ConcurrencyStressTest:**

- Erfolgsquote: 98.49% (13,414/13,620 Requests)
- Szenario: 1000+ gleichzeitige User, 10,000 Requests über 60 Sekunden
- Hot Seat Test: Nur 1 Reservierung erfolgreich, Rest HTTP 409 CONFLICT
- Performance: System bleibt stabil unter extremer Last

**SeatHoldLoadTest:**

- Erfolgsquote: 100%
- Durchschnittliche Antwortzeit: 9ms
- Szenario: 100 parallele Holds bei Verkaufsstart-Simulation
- Ergebnis: Perfektes Concurrency Control, keine Race Conditions

### Aktuelle Kapazität

- Unterstützt 1.000-2.000 req/s mit 1-2 Instanzen
- Perfektes Concurrency Control unter extremer Last
- Keine Doppelverkäufe in Concurrency Tests nachgewiesen

## Testergebnisse

### Load Testing (Gatling)

Das System wurde mit Gatling unter verschiedenen Lastszenarien getestet:

**BasicSmokeTest:**

- Erfolgsquote: 100%
- Durchschnittliche Antwortzeit: 34ms
- Szenarien: Health Check, Verfügbarkeit, einzelne Holds, gleichzeitige Holds

**ConcurrencyStressTest:**

- Erfolgsquote: 98.49% (13,414/13,620 Requests)
- Szenario: 1000+ gleichzeitige User, 10,000 Requests über 60 Sekunden
- Hot Seat Test: Nur 1 Reservierung erfolgreich, Rest HTTP 409 CONFLICT
- Performance: System bleibt stabil unter extremer Last

**SeatHoldLoadTest:**

- Erfolgsquote: 100%
- Durchschnittliche Antwortzeit: 9ms
- Szenario: 100 parallele Holds bei Verkaufsstart-Simulation
- Ergebnis: Perfektes Concurrency Control, keine Race Conditions

### Concurrency Tests

- Hot Seat Test: 100 parallele Threads auf denselben Platz → genau 1 erfolgreich
- Optimistic Locking Version Increment: Verhindert Lost Updates
- High Load auf verschiedene Seats: Alle 50 Reservierungen erfolgreich

### Testabdeckung

- Unit Tests: Domänen-Logik und Business Rules
- Integration Tests: REST API, Datenbank, Caching
- Concurrency Tests: Race Condition Prevention
- Load Tests: Performance und Skalierbarkeit
- Bruno API Tests: End-to-End Integration

## Skalierbarkeit & Performance

### Aktuelle Performance

Das System wurde mit Gatling Load Tests unter verschiedenen Lastszenarien validiert. Die aktuelle Implementierung mit Optimistic Locking ist skalierbar bis ca. 1.000-2.000 Requests/Sekunde mit 1-2 Instanzen.

## Sicherheit & Rate Limiting

### Authentication & Authorization

- JWT-basierte Authentifizierung (stateless)
- Rollen: USER, ADMIN
- Geschützte Endpoints für Hold und Order Funktionen

### Rate Limiting

- Bucket4j für Token Bucket Algorithmus
- Konfigurierbare Limits pro IP/User
- HTTP 429 Too Many Requests bei Limit-Überschreitung

## Deployment

### Docker Compose

Das System wird über Docker Compose orchestriert mit separaten Containern für Backend, Frontend.

## API Endpoints

### Events

- GET /api/events - Liste aller Events mit Filtern
- GET /api/events/{id} - Event-Details
- POST /api/events - Event erstellen (ADMIN)
- PUT /api/events/{id} - Event aktualisieren (ADMIN)
- DELETE /api/events/{id} - Event löschen (ADMIN)

### Seats

- GET /api/events/{eventId}/seats - Alle Seats für ein Event
- GET /api/events/{eventId}/availability - Verfügbarkeit aggregiert
- POST /api/seats - Seats bulk-erstellen (ADMIN)

### Reservations

- POST /api/reservations - Seat reservieren (Hold)
- GET /api/reservations/{id} - Reservation-Details
- DELETE /api/reservations/{id} - Reservation stornieren

### Orders

- POST /api/orders - Reservation kaufen (Checkout)
- GET /api/orders/{id} - Order-Details
- GET /api/users/me/orders - Meine Orders

## Fehlerbehandlung

Das System verwendet einen Global Exception Handler für konsistente Fehlerbehandlung:

- `SeatNotAvailableException`: HTTP 409 CONFLICT
- `ReservationExpiredException`: HTTP 400 BAD REQUEST
- `PaymentFailedException`: HTTP 400 BAD REQUEST
- `ObjectOptimisticLockingFailureException`: HTTP 409 CONFLICT
- `ValidationException`: HTTP 400 BAD REQUEST

Alle Fehler werden als strukturierte JSON Responses mit Fehlercode, Nachricht und Zeitstempel zurückgegeben.

## Monitoring & Logging

### Health Checks

Spring Actuator stellt folgende Endpoints bereit:

- /actuator/health - System Status
- /actuator/metrics - JVM, DB, Cache Metriken
- /actuator/prometheus - Prometheus Export

## Fazit & Ausblick

Das System hat sich als hoch skalierbar und zuverlässig erwiesen. Die implementierte Lösung mit Optimistic Locking garantiert unter allen Testbedingungen, dass kein Platz doppelt verkauft wird.

### Aktuelle Kapazität

- Unterstützt 1.000-2.000 req/s mit 1-2 Instanzen
- Perfektes Concurrency Control unter extremer Last
- Keine Doppelverkäufe in Concurrency Tests nachgewiesen

### Weiterentwicklung

Für die Zielskalierung von 10.000 req/s werden empfohlen:

1. Redis Distributed Lock oder Message Queue Serialisierung
2. Horizontal Skalierung mit Load Balancer
3. Erweitertes Caching mit Redis Cluster
4. Performance Monitoring mit Prometheus/Grafana
