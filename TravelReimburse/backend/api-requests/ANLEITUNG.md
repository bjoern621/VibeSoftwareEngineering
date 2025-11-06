# Bruno VS Code Extension - Einfache Anleitung

## 🚀 Schnellstart (5 Schritte)

### Schritt 1: Server starten
Öffne Terminal in VS Code:
```bash
cd backend
./mvnw spring-boot:run
```
Warte bis "Started TravelReimburseApplication" erscheint.

---

### Schritt 2: Collection öffnen
- Öffne Datei: `backend/api-requests/collection.bru`

---

### Schritt 3: Ersten Request ausführen
1. Öffne: `TravelRequests/01-Create Travel Request.bru`
2. Klick auf **"Send"** Button (oben rechts)
3. Kopiere die `id` aus der Response (z.B. `1`)

**Response sieht so aus:**
```json
{
  "id": 1,
  "employeeId": 1,
  "destination": "Berlin",
  "status": "DRAFT",
  "travelLegs": []
}
```

---

### Schritt 4: ID für nächste Requests setzen
1. Öffne: `environments/dev.bru`
2. Ändere:
   ```bruno
   vars {
     baseUrl: http://localhost:8080/api
     travelRequestId: 1    👈 Hier die ID eintragen!
     legId: 
   }
   ```
3. Speichern (Cmd+S)

---

### Schritt 5: Reiseabschnitte hinzufügen
1. Öffne: `TravelRequests/08-Add Travel Leg.bru`
2. Klick auf **"Send"**
3. Wiederholen für weitere Legs

**Fertig!** 🎉

---

## 📋 Kompletter Test-Workflow

```
✅ Server läuft auf Port 8080

1️⃣ 01-Create Travel Request.bru
   → Send klicken
   → ID kopieren (z.B. 1)
   → In dev.bru eintragen: travelRequestId: 1

2️⃣ 08-Add Travel Leg.bru (Flug)
   → Send klicken
   → 201 Created

3️⃣ 08-Add Travel Leg.bru nochmal öffnen
   → Body ändern auf Mietwagen:
     "transportationType": "CAR_RENTAL"
     "departureLocation": "München Flughafen"
     "arrivalLocation": "Hotel München"
     "costAmount": 45.00
   → Send klicken

4️⃣ 09-Get Travel Legs.bru
   → Send klicken
   → Liste mit allen Legs sehen

5️⃣ 02-Get Travel Request By ID.bru
   → Send klicken
   → Kompletter Antrag mit travelLegs Array
```

---

## ⚙️ Troubleshooting

### ✅ HTTP Status Codes (Kein Fehler!)

**204 No Content** beim DELETE-Request?
- ✅ **Das ist KORREKT!** = Erfolgreich gelöscht
- Keine Response-Body = Normal bei DELETE
- Prüfe mit GET, ob Leg wirklich weg ist

**Übersicht:**
- **200 OK** = GET/PUT erfolgreich (mit Daten)
- **201 Created** = POST erfolgreich (neues Objekt)
- **204 No Content** = DELETE erfolgreich (keine Daten)
- **400 Bad Request** = Ungültige Eingabe
- **404 Not Found** = Objekt existiert nicht
- **500 Server Error** = Backend-Fehler

---

