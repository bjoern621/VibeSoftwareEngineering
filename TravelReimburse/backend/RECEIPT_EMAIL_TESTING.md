# 📧 E-Mail-Benachrichtigungen für Receipt Status-Änderungen - Test-Anleitung

## ✅ Implementierte Features

### Neue/Geänderte Dateien:
1. **ReceiptStatusChangedEvent.java** - Domain Event für Status-Änderungen
2. **ReceiptService.java** - Erweitert mit Event Publisher
3. **EmailNotificationService.java** - Erweitert mit Receipt-Benachrichtigungen
4. **ReceiptEventListener.java** - NEU - Listener für Receipt Events

---

## 🧪 Wie du testen kannst

### Schritt 1: Backend starten
```bash
cd /home/uncleruckus/Documents/uni/2025WS/projekt/VibeSoftwareEngineering/TravelReimburse/backend
mvn spring-boot:run
```

### Schritt 2: Einen Receipt hochladen (falls noch nicht vorhanden)

Mit Bruno:
- Öffne `01-Upload-Receipt.bru`
- Führe die Request aus
- Merke dir die `receiptId` aus der Response

### Schritt 3: Receipt Status ändern - Validieren

Mit Bruno:
- Öffne `05-Validate-Receipt.bru`
- Ändere die Receipt-ID in der URL falls nötig
- Führe die Request aus

**Erwartetes Ergebnis:**
```
╔═══════════════════════════════════════════════════════════════════╗
║                    MOCK E-MAIL VERSANDT                           ║
╠═══════════════════════════════════════════════════════════════════╣
║ An: employeeXXX@company.com
║ Betreff: Beleg Status-Änderung: 1
║ Zeitpunkt: 07.11.2025 13:45:00
╠═══════════════════════════════════════════════════════════════════╣
║ Sehr geehrte/r Mitarbeiter/in,
║
║ Ihr Beleg hat eine Statusänderung erfahren:
║
║ Beleg-ID: 1
║ Alter Status: Hochgeladen
║ Neuer Status: Validiert
║
║ Reiseantrag-ID: 1
║ Belegtyp: Hotelrechnung
║ Ausstellungsdatum: 2025-11-01
║ Betrag: 150.00 EUR
║ Anbieter: Hotel Hilton
║ 
║ Mit freundlichen Grüßen
║ Ihr TravelReimburse Team
╚═══════════════════════════════════════════════════════════════════╝
```

### Schritt 4: Receipt Status ändern - Ablehnen

Mit Bruno:
- Öffne `06-Reject-Receipt.bru`
- Ändere die Receipt-ID in der URL falls nötig
- Führe die Request aus

**Erwartetes Ergebnis:**
```
╔═══════════════════════════════════════════════════════════════════╗
║                    MOCK E-MAIL VERSANDT                           ║
╠═══════════════════════════════════════════════════════════════════╣
║ An: employeeXXX@company.com
║ Betreff: Beleg Status-Änderung: 1
║ Zeitpunkt: 07.11.2025 13:46:00
╠═══════════════════════════════════════════════════════════════════╣
║ Sehr geehrte/r Mitarbeiter/in,
║
║ Ihr Beleg hat eine Statusänderung erfahren:
║
║ Beleg-ID: 1
║ Alter Status: Hochgeladen
║ Neuer Status: Abgelehnt
║
║ Reiseantrag-ID: 1
║ Belegtyp: Hotelrechnung
║ Ausstellungsdatum: 2025-11-01
║ Betrag: 150.00 EUR
║ Anbieter: Hotel Hilton
║ Ablehnungsgrund: Beleg ist unleserlich, bitte erneut hochladen
║ 
║ Mit freundlichen Grüßen
║ Ihr TravelReimburse Team
╚═══════════════════════════════════════════════════════════════════╝
```

---

## 📋 Commit-Übersicht

### Commit 1: Add ReceiptStatusChangedEvent
```
feat: Add ReceiptStatusChangedEvent for email notifications

- Create domain event to track receipt status changes
- Follows same pattern as TravelRequestStatusChangedEvent
- Required for email notification feature
```

**Datei:** `ReceiptStatusChangedEvent.java`

---

### Commit 2: Extend ReceiptService with event publishing
```
feat: Publish events on receipt status changes

- Add ApplicationEventPublisher to ReceiptService
- Publish ReceiptStatusChangedEvent in validateReceipt()
- Publish ReceiptStatusChangedEvent in rejectReceipt()
- Enables email notifications for receipt status changes
```

**Datei:** `ReceiptService.java`

---

### Commit 3: Extend EmailNotificationService for receipts
```
feat: Add email notifications for receipt status changes

- Add sendReceiptStatusChangeNotification() method
- Add buildReceiptEmailContent() method
- Add getReceiptStatusDisplayName() helper
- Add getReceiptTypeDisplayName() helper
- Support for rejection reason display
```

**Datei:** `EmailNotificationService.java`

---

### Commit 4: Add ReceiptEventListener
```
feat: Add event listener for receipt status changes

- Create ReceiptEventListener component
- Listen to ReceiptStatusChangedEvent
- Trigger email notifications via EmailNotificationService
- Add logging for event handling
```

**Datei:** `ReceiptEventListener.java`

---

### Commit 5 (Optional): Add test documentation
```
docs: Add testing instructions for receipt email notifications

- Document test steps for receipt status changes
- Add expected email output examples
```

**Datei:** `RECEIPT_EMAIL_TESTING.md`

---

## 🎯 Zusammenfassung

**Implementierte Funktionalität:**
- ✅ E-Mail-Benachrichtigungen bei Receipt-Validierung
- ✅ E-Mail-Benachrichtigungen bei Receipt-Ablehnung
- ✅ Event-driven Architecture (Domain Events)
- ✅ Mock E-Mail Service (Console-Ausgabe)
- ✅ Deutsche E-Mail-Texte
- ✅ Detaillierte Beleg-Informationen in E-Mails
- ✅ Ablehnungsgrund wird angezeigt

**Nächste Schritte (falls gewünscht):**
- Echten SMTP E-Mail-Versand implementieren
- E-Mail-Templates (HTML) hinzufügen
- Async E-Mail-Versand mit Queue

