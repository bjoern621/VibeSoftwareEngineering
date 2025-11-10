-- Testdaten für Reiseziele mit Visa- und Impfanforderungen

-- USA - Visum erforderlich (BUSINESS)
INSERT INTO travel_destinations (
    country_code, country_name, 
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'US', 'Vereinigte Staaten von Amerika',
    true, 'BUSINESS', 21, 'B-1 Visum für Geschäftsreisen erforderlich. ESTA nicht ausreichend für Geschäftsreisen.',
    false, null, 'COVID-19, Influenza', 'Empfohlene Impfungen aktualisiert gemäß CDC-Richtlinien',
    'ESTA reicht nur für touristische Zwecke. Für Dienstreisen B-1 Visum beantragen.', 
    CURRENT_TIMESTAMP
);

-- China - Visum und Impfungen erforderlich
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'CN', 'China',
    true, 'BUSINESS', 14, 'M-Visum für Geschäftsreisen. Einladungsschreiben erforderlich.',
    true, 'COVID-19, Hepatitis A, Hepatitis B', 'Typhus, Japanische Enzephalitis, Tollwut', 
    'Besonders bei Reisen in ländliche Gebiete zusätzliche Impfungen empfohlen',
    'Aktuell gelten strenge Einreisebestimmungen. Registrierung innerhalb 24h nach Ankunft.',
    CURRENT_TIMESTAMP
);

-- Indien - Visum erforderlich (E-Visa möglich)
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'IN', 'Indien',
    true, 'E_VISA', 7, 'Business E-Visum online beantragbar. Schneller als reguläres Visum.',
    true, 'Gelbfieber (bei Einreise aus Endemiegebiet)', 
    'Hepatitis A, Hepatitis B, Typhus, Tollwut, Japanische Enzephalitis',
    'Gelbfieberimpfung nur bei Einreise aus Risikoländern verpflichtend',
    'E-Visa einfach online zu beantragen. Malaria-Prophylaxe für bestimmte Regionen empfohlen.',
    CURRENT_TIMESTAMP
);

-- Brasilien - Visa on Arrival, Impfungen erforderlich
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'BR', 'Brasilien',
    false, null, null, 'EU-Bürger benötigen kein Visum für Aufenthalte bis 90 Tage',
    true, 'Gelbfieber', 'Hepatitis A, Hepatitis B, Typhus, Tollwut',
    'Gelbfieberimpfung mindestens 10 Tage vor Einreise. Impfpass mitführen!',
    'Gelbfieberimpfung zwingend erforderlich. Dengue-Fieber-Risiko in manchen Regionen.',
    CURRENT_TIMESTAMP
);

-- Südafrika - Kein Visum, Impfempfehlungen
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'ZA', 'Südafrika',
    false, null, null, 'Kein Visum für EU-Bürger bei Aufenthalten bis 90 Tage',
    false, null, 'Hepatitis A, Hepatitis B, Typhus, Tollwut',
    'Je nach Reisegebiet Malaria-Prophylaxe empfohlen (Kruger Nationalpark)',
    'Sicheres Reisen mit üblichen Vorsichtsmaßnahmen. Kriminalität in Großstädten beachten.',
    CURRENT_TIMESTAMP
);

-- Japan - Kein Visum, kaum Impfungen
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'JP', 'Japan',
    false, null, null, 'Kein Visum für EU-Bürger bei Aufenthalten bis 90 Tage',
    false, null, 'Hepatitis A, Hepatitis B, Japanische Enzephalitis',
    'Standardimpfungen ausreichend. Japanische Enzephalitis nur bei Langzeitaufenthalten',
    'Sehr sicheres Reiseland mit ausgezeichneter Infrastruktur. Englisch teilweise begrenzt.',
    CURRENT_TIMESTAMP
);

-- Russland - Visum erforderlich
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'RU', 'Russland',
    true, 'BUSINESS', 21, 'Business-Visum mit Einladungsschreiben. Registrierung innerhalb 7 Tage nach Ankunft.',
    false, null, 'Hepatitis A, Hepatitis B, FSME (Frühsommer-Meningoenzephalitis)',
    'FSME-Impfung bei Reisen in ländliche Gebiete im Sommer empfohlen',
    'Registrierungspflicht beachten! Visa-Beantragung kann komplex sein.',
    CURRENT_TIMESTAMP
);

-- Vereinigte Arabische Emirate - Visa on Arrival
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'AE', 'Vereinigte Arabische Emirate',
    true, 'VISA_ON_ARRIVAL', 0, 'Visa on Arrival für EU-Bürger. 30 Tage Aufenthalt, Verlängerung möglich.',
    false, null, 'Hepatitis A, Hepatitis B',
    'Standardimpfungen ausreichend',
    'Kleiderordnung und kulturelle Besonderheiten beachten. Sehr sicher.',
    CURRENT_TIMESTAMP
);

-- Schweiz - Kein Visum (außerhalb EU aber Schengen)
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'CH', 'Schweiz',
    false, null, null, 'Kein Visum erforderlich. Schengen-Mitglied.',
    false, null, null,
    'Keine speziellen Impfungen erforderlich',
    'Problemlose Einreise. Hohe Lebenshaltungskosten beachten.',
    CURRENT_TIMESTAMP
);

-- Großbritannien - Kein Visum (Post-Brexit)
INSERT INTO travel_destinations (
    country_code, country_name,
    visa_required, visa_type, visa_processing_days, visa_notes,
    vaccination_required, vaccination_types, vaccination_recommended, vaccination_notes,
    general_travel_advice, created_at
) VALUES (
    'GB', 'Großbritannien',
    false, null, null, 'Kein Visum für EU-Bürger bei Aufenthalten bis 6 Monate (seit Brexit)',
    false, null, null,
    'Keine speziellen Impfungen erforderlich',
    'Einreise mit Reisepass (Personalausweis nicht mehr ausreichend seit Brexit).',
    CURRENT_TIMESTAMP
);

-- ===================================================================
-- Testdaten für Employees (Mitarbeiter) mit verschiedenen Rollen
-- ===================================================================

-- EMPLOYEES (normale Mitarbeiter) - IDs 1, 2, 3
-- Diese werden in Bruno-Tests verwendet (employeeId: 1, 2, 3)

INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    1, 'Max', 'Mustermann', 'max.mustermann@company.com', 'EMPLOYEE', 100, 'IT-001', 'Munich', true, CURRENT_TIMESTAMP
);

INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    2, 'Anna', 'Schmidt', 'anna.schmidt@company.com', 'EMPLOYEE', 100, 'SALES-001', 'Berlin', true, CURRENT_TIMESTAMP
);

INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    3, 'Thomas', 'Weber', 'thomas.weber@company.com', 'EMPLOYEE', 100, 'IT-001', 'Munich', true, CURRENT_TIMESTAMP
);

-- MANAGER - ID 100
-- Wird in Bruno-Tests verwendet (approverId: 100)
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    100, 'Maria', 'Führung', 'maria.fuehrung@company.com', 'MANAGER', null, 'IT-001', 'Munich', true, CURRENT_TIMESTAMP
);

-- HR (Personalabteilung) - ID 200
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    200, 'Peter', 'Personal', 'peter.personal@company.com', 'HR', null, null, 'Berlin', true, CURRENT_TIMESTAMP
);

-- ASSISTANT (Assistent für Delegationen) - ID 300
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    300, 'Lisa', 'Assistent', 'lisa.assistent@company.com', 'ASSISTANT', 100, 'IT-001', 'Munich', true, CURRENT_TIMESTAMP
);

-- FINANCE (Finanzabteilung) - ID 400
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    400, 'Stefan', 'Finanzen', 'stefan.finanzen@company.com', 'FINANCE', null, null, 'Berlin', true, CURRENT_TIMESTAMP
);

-- Weiterer Manager für Tests - ID 101
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at
) VALUES (
    101, 'Julia', 'Abteilungsleiter', 'julia.abteilungsleiter@company.com', 'MANAGER', null, 'SALES-001', 'Berlin', true, CURRENT_TIMESTAMP
);

-- Deaktivierter Employee für Tests - ID 999
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, department_code, location, active, created_at, deactivated_at
) VALUES (
    999, 'Inactive', 'User', 'inactive.user@company.com', 'EMPLOYEE', 100, null, null, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Setze die Auto-Increment Sequenz auf den nächsten freien Wert (1000)
-- Dies ist notwendig, da wir manuelle IDs (1-999) verwendet haben
ALTER TABLE employees ALTER COLUMN id RESTART WITH 1000;

-- ========================================
-- Testdaten für Reiseanträge mit Kostenstellen
-- ========================================

-- Reise 1: IT-Abteilung, London, APPROVED
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name, 
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, approved_at
) VALUES (
    1, 'IT-001', 'IT Development',
    'London, UK', 'Teilnahme an Tech Conference',
    '2025-12-01', '2025-12-05',
    2500.00, 'EUR',
    'APPROVED', '2025-01-15 10:00:00', '2025-01-16 09:00:00', 100, '2025-01-17 14:30:00'
);

-- Reise 2: Sales-Abteilung, New York, APPROVED
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, approved_at
) VALUES (
    2, 'SALES-001', 'Sales Department',
    'New York, USA', 'Kundengespräch und Vertragsverhandlung',
    '2025-02-15', '2025-02-20',
    3500.00, 'EUR',
    'APPROVED', '2025-01-20 11:00:00', '2025-01-21 10:00:00', 100, '2025-01-22 16:00:00'
);

-- Reise 3: Marketing, Paris, APPROVED
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, approved_at
) VALUES (
    3, 'MKT-001', 'Marketing',
    'Paris, Frankreich', 'Marketingkampagne Launch Event',
    '2025-03-10', '2025-03-12',
    1500.00, 'EUR',
    'APPROVED', '2025-02-25 09:00:00', '2025-02-26 08:00:00', 100, '2025-02-27 10:00:00'
);

-- Reise 4: IT-Abteilung, Berlin, SUBMITTED (wartet auf Genehmigung)
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at
) VALUES (
    1, 'IT-001', 'IT Development',
    'Berlin, Deutschland', 'Cloud Infrastructure Workshop',
    '2025-12-15', '2025-12-17',
    1200.00, 'EUR',
    'SUBMITTED', '2025-10-05 14:00:00', '2025-10-06 09:00:00'
);

-- Reise 5: Executive, Tokyo, APPROVED (hoher Betrag)
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, approved_at
) VALUES (
    10, 'EXEC-001', 'Executive Management',
    'Tokyo, Japan', 'Strategische Partnerschaft Verhandlungen',
    '2025-06-10', '2025-06-18',
    8500.00, 'EUR',
    'APPROVED', '2025-04-15 16:00:00', '2025-04-16 08:00:00', 100, '2025-04-16 15:00:00'
);

-- Reise 6: HR, München, REJECTED
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, rejected_at, rejection_reason
) VALUES (
    5, 'HR-001', 'Human Resources',
    'München, Deutschland', 'HR Software Evaluation',
    '2025-07-25', '2025-07-26',
    800.00, 'EUR',
    'REJECTED', '2025-07-01 12:00:00', '2025-07-02 10:00:00', 100, '2025-07-03 11:00:00',
    'Budget für Q3 bereits ausgeschöpft. Bitte auf Q4 verschieben.'
);

-- Reise 7: Sales, Shanghai, APPROVED
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, approved_at
) VALUES (
    2, 'SALES-001', 'Sales Department',
    'Shanghai, China', 'Messebesuch und Kundentermine',
    '2025-09-05', '2025-09-12',
    4200.00, 'EUR',
    'APPROVED', '2025-07-28 13:00:00', '2025-07-29 09:00:00', 100, '2025-07-30 14:00:00'
);

-- Reise 8: IT, Zürich, DRAFT (noch nicht eingereicht)
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at
) VALUES (
    4, 'IT-001', 'IT Development',
    'Zürich, Schweiz', 'Kubernetes Training',
    '2026-01-20', '2026-01-22',
    1800.00, 'EUR',
    'DRAFT', '2025-10-07 15:00:00'
);

-- Reise 9: Marketing, Amsterdam, APPROVED
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, approved_at
) VALUES (
    3, 'MKT-001', 'Marketing',
    'Amsterdam, Niederlande', 'Digital Marketing Summit',
    '2025-08-28', '2025-08-30',
    1600.00, 'EUR',
    'APPROVED', '2025-08-10 10:00:00', '2025-08-11 09:00:00', 100, '2025-08-12 11:00:00'
);

-- Reise 10: Finance, Frankfurt, APPROVED
INSERT INTO travel_requests (
    employee_id, cost_center_code, cost_center_name,
    destination, purpose,
    start_date, end_date,
    estimated_amount, estimated_currency,
    status, created_at, submitted_at, approver_id, approved_at
) VALUES (
    6, 'FIN-001', 'Finance Department',
    'Frankfurt, Deutschland', 'Bankengespräch und Jahresabschluss',
    '2025-12-08', '2025-12-09',
    900.00, 'EUR',
    'APPROVED', '2025-09-18 11:00:00', '2025-09-19 08:00:00', 100, '2025-09-20 10:00:00'
);

-- ============================================================================
-- TRAVEL POLICIES (Reiserichtlinien)
-- ============================================================================

-- Policy 1: Standard-Policy mit Auto-Approval (für alle Abteilungen und Standorte)
INSERT INTO travel_policies (
    name, description, 
    department_code, location,
    auto_approval_enabled, active,
    created_at
) VALUES (
    'Standard Reiserichtlinie',
    'Allgemeine Reiserichtlinie für alle Mitarbeiter mit automatischer Genehmigung bei Einhaltung der Limits',
    null, null,
    true, true,
    CURRENT_TIMESTAMP
);

-- Limits für Standard-Policy (ID = 1)
INSERT INTO policy_category_limits (policy_id, expense_category, max_amount) VALUES
    (1, 'ACCOMMODATION', '150.00'),  -- Max 150 EUR pro Nacht für Hotel
    (1, 'MEALS', '50.00'),           -- Max 50 EUR pro Tag für Verpflegung
    (1, 'TRANSPORTATION', '500.00'), -- Max 500 EUR für Flug/Bahn
    (1, 'FUEL', '0.30'),             -- Max 0.30 EUR pro Kilometer
    (1, 'PARKING_TOLLS', '30.00'),   -- Max 30 EUR pro Tag
    (1, 'OTHER', '100.00');          -- Max 100 EUR sonstige Kosten

-- Policy 2: IT Department - Höhere Limits, keine Auto-Approval (wegen Konferenzen)
INSERT INTO travel_policies (
    name, description,
    department_code, location,
    auto_approval_enabled, active,
    created_at
) VALUES (
    'IT Department Policy',
    'Erhöhte Limits für IT-Abteilung (Konferenzen, Schulungen), manuelle Genehmigung erforderlich',
    'IT-001', null,
    false, true,
    CURRENT_TIMESTAMP
);

-- Limits für IT-Policy (ID = 2)
INSERT INTO policy_category_limits (policy_id, expense_category, max_amount) VALUES
    (2, 'ACCOMMODATION', '200.00'),  -- Höheres Hotel-Limit
    (2, 'MEALS', '75.00'),           -- Höheres Verpflegungs-Limit
    (2, 'TRANSPORTATION', '800.00'), -- Höheres Transport-Limit
    (2, 'FUEL', '0.35'),
    (2, 'PARKING_TOLLS', '50.00'),
    (2, 'OTHER', '200.00');          -- Höher für Konferenz-Tickets etc.

-- Policy 3: Sales Department - Auto-Approval für Kundenbesuche
INSERT INTO travel_policies (
    name, description,
    department_code, location,
    auto_approval_enabled, active,
    created_at
) VALUES (
    'Sales Department Policy',
    'Sales-Mitarbeiter dürfen Kundenbesuche automatisch genehmigen lassen',
    'SALES-001', null,
    true, true,
    CURRENT_TIMESTAMP
);

-- Limits für Sales-Policy (ID = 3)
INSERT INTO policy_category_limits (policy_id, expense_category, max_amount) VALUES
    (3, 'ACCOMMODATION', '180.00'),
    (3, 'MEALS', '60.00'),
    (3, 'TRANSPORTATION', '600.00'),
    (3, 'FUEL', '0.32'),
    (3, 'PARKING_TOLLS', '40.00'),
    (3, 'OTHER', '150.00');

-- Policy 4: Munich Standort - Spezielle Regelung (Beispiel für Location-Filter)
INSERT INTO travel_policies (
    name, description,
    department_code, location,
    auto_approval_enabled, active,
    created_at
) VALUES (
    'Munich Location Policy',
    'Spezielle Richtlinie für Mitarbeiter am Standort München (niedrigere Limits)',
    null, 'Munich',
    true, true,
    CURRENT_TIMESTAMP
);

-- Limits für Munich-Policy (ID = 4)
INSERT INTO policy_category_limits (policy_id, expense_category, max_amount) VALUES
    (4, 'ACCOMMODATION', '120.00'),  -- Niedrigere Limits
    (4, 'MEALS', '40.00'),
    (4, 'TRANSPORTATION', '400.00'),
    (4, 'FUEL', '0.28'),
    (4, 'PARKING_TOLLS', '25.00'),
    (4, 'OTHER', '80.00');

-- Policy 5: Executive Policy - Keine Limits, manuelle Genehmigung
INSERT INTO travel_policies (
    name, description,
    department_code, location,
    auto_approval_enabled, active,
    created_at
) VALUES (
    'Executive Policy',
    'Für Geschäftsführung - keine Limits, aber manuelle Genehmigung durch Board',
    'EXEC-001', null,
    false, true,
    CURRENT_TIMESTAMP
);

-- Keine Limits für Executive Policy (würde unbegrenzt sein)

