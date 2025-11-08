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
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    1, 'Max', 'Mustermann', 'max.mustermann@company.com', 'EMPLOYEE', 100, true, CURRENT_TIMESTAMP
);

INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    2, 'Anna', 'Schmidt', 'anna.schmidt@company.com', 'EMPLOYEE', 100, true, CURRENT_TIMESTAMP
);

INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    3, 'Thomas', 'Weber', 'thomas.weber@company.com', 'EMPLOYEE', 100, true, CURRENT_TIMESTAMP
);

-- MANAGER - ID 100
-- Wird in Bruno-Tests verwendet (approverId: 100)
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    100, 'Maria', 'Führung', 'maria.fuehrung@company.com', 'MANAGER', null, true, CURRENT_TIMESTAMP
);

-- HR (Personalabteilung) - ID 200
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    200, 'Peter', 'Personal', 'peter.personal@company.com', 'HR', null, true, CURRENT_TIMESTAMP
);

-- ASSISTANT (Assistent für Delegationen) - ID 300
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    300, 'Lisa', 'Assistent', 'lisa.assistent@company.com', 'ASSISTANT', 100, true, CURRENT_TIMESTAMP
);

-- FINANCE (Finanzabteilung) - ID 400
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    400, 'Stefan', 'Finanzen', 'stefan.finanzen@company.com', 'FINANCE', null, true, CURRENT_TIMESTAMP
);

-- Weiterer Manager für Tests - ID 101
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at
) VALUES (
    101, 'Julia', 'Abteilungsleiter', 'julia.abteilungsleiter@company.com', 'MANAGER', null, true, CURRENT_TIMESTAMP
);

-- Deaktivierter Employee für Tests - ID 999
INSERT INTO employees (
    id, first_name, last_name, email, role, manager_id, active, created_at, deactivated_at
) VALUES (
    999, 'Inactive', 'User', 'inactive.user@company.com', 'EMPLOYEE', 100, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Setze die Auto-Increment Sequenz auf den nächsten freien Wert (1000)
-- Dies ist notwendig, da wir manuelle IDs (1-999) verwendet haben
ALTER TABLE employees ALTER COLUMN id RESTART WITH 1000;
