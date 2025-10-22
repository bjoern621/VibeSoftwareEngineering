/**
 * Zentrale Konfiguration und Konstanten
 * Alle Magic Strings, Konfigurationswerte und Enums an einem Ort
 */

// ========== ZAHLUNGSMETHODEN ==========

/**
 * Verfügbare Zahlungsmethoden über EASYPAY
 */
export const PAYMENT_METHODS = {
  KREDITKARTE: 'Kreditkarte',
  DEBITKARTE: 'Debitkarte',
  GUTHABEN: 'Guthabenkonto',
  BITCOIN: 'Bitcoin'
};

/**
 * Array aller Zahlungsmethoden für Iteration
 */
export const PAYMENT_METHOD_LIST = [
  PAYMENT_METHODS.KREDITKARTE,
  PAYMENT_METHODS.DEBITKARTE,
  PAYMENT_METHODS.GUTHABEN,
  PAYMENT_METHODS.BITCOIN
];

// ========== KATEGORIEN ==========

/**
 * Verfügbare Gerichte-Kategorien
 */
export const CATEGORIES = {
  VEGAN: 'vegan',
  VEGETARISCH: 'vegetarisch',
  HALAL: 'halal',
  GLUTENFREI: 'glutenfrei',
  STANDARD: 'standard'
};

/**
 * Kategorien mit Display-Namen und Farben
 */
export const CATEGORY_CONFIG = {
  vegan: {
    label: 'Vegan',
    color: '#28a745'
  },
  vegetarisch: {
    label: 'Vegetarisch',
    color: '#ffc107'
  },
  halal: {
    label: 'Halal',
    color: '#17a2b8'
  },
  glutenfrei: {
    label: 'Glutenfrei',
    color: '#fd7e14'
  },
  standard: {
    label: 'Standard',
    color: '#6c757d'
  }
};

// ========== API KONFIGURATION ==========

/**
 * Mock Data Modus aktivieren/deaktivieren
 */
export const USE_MOCK_DATA = true;

/**
 * Verzögerung für Mock API Calls (Millisekunden)
 */
export const MOCK_DELAY = 300;

/**
 * Backend Base URL
 */
export const API_BASE_URL = 'http://localhost:8081/api';

/**
 * CoinGecko API Base URL für Bitcoin-Kurs
 */
export const COINGECKO_API_URL = 'https://api.coingecko.com/api/v3';

// ========== WOCHENTAGE ==========

/**
 * Deutsche Wochentags-Namen (Montag-Freitag)
 */
export const WEEKDAY_NAMES = {
  0: 'Mo',
  1: 'Di',
  2: 'Mi',
  3: 'Do',
  4: 'Fr'
};

/**
 * Vollständige Wochentags-Namen
 */
export const WEEKDAY_NAMES_FULL = {
  0: 'Montag',
  1: 'Dienstag',
  2: 'Mittwoch',
  3: 'Donnerstag',
  4: 'Freitag'
};

// ========== BESTELLSTATUS ==========

/**
 * Status einer Bestellung
 */
export const ORDER_STATUS = {
  PENDING: 'pending',
  PAID: 'paid',
  COLLECTED: 'collected',
  CANCELLED: 'cancelled'
};

// ========== UI KONSTANTEN ==========

/**
 * Bitcoin Payment Countdown Zeit (Sekunden)
 */
export const BITCOIN_COUNTDOWN_SECONDS = 180;

/**
 * Standard Guthaben-Auflade-Beträge (Euro)
 */
export const BALANCE_TOP_UP_AMOUNTS = [10, 20, 50, 100];

/**
 * Maximale Anzahl Items pro Warenkorb
 */
export const MAX_CART_ITEMS = 50;

/**
 * Standard Lagerbestand-Warngrenze
 */
export const LOW_STOCK_THRESHOLD = 10;

// ========== VALIDIERUNG ==========

/**
 * Minimaler Bestellbetrag (Euro)
 */
export const MIN_ORDER_AMOUNT = 0.01;

/**
 * Maximaler Bestellbetrag (Euro)
 */
export const MAX_ORDER_AMOUNT = 1000;

/**
 * Minimaler Guthaben-Auflade-Betrag (Euro)
 */
export const MIN_TOP_UP_AMOUNT = 5;

/**
 * Maximaler Guthaben-Auflade-Betrag (Euro)
 */
export const MAX_TOP_UP_AMOUNT = 500;

// ========== FEHLERMELDUNGEN ==========

/**
 * Standard Fehlermeldungen
 */
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Netzwerkfehler. Bitte versuchen Sie es später erneut.',
  INSUFFICIENT_BALANCE: 'Guthaben nicht ausreichend.',
  PAYMENT_FAILED: 'Zahlung fehlgeschlagen. Bitte versuchen Sie es erneut.',
  ORDER_FAILED: 'Bestellung konnte nicht erstellt werden.',
  BITCOIN_PRICE_FETCH_FAILED: 'Bitcoin-Kurs konnte nicht abgerufen werden.',
  INVALID_AMOUNT: 'Ungültiger Betrag.',
  CART_EMPTY: 'Warenkorb ist leer.',
  SESSION_EXPIRED: 'Sitzung abgelaufen. Bitte neu anmelden.'
};

// ========== ERFOLGSMELDUNGEN ==========

/**
 * Standard Erfolgsmeldungen
 */
export const SUCCESS_MESSAGES = {
  ORDER_CREATED: 'Bestellung erfolgreich erstellt!',
  PAYMENT_SUCCESS: 'Zahlung erfolgreich!',
  TOP_UP_SUCCESS: 'Guthaben erfolgreich aufgeladen!',
  MEAL_CREATED: 'Gericht erfolgreich erstellt!',
  MEAL_UPDATED: 'Gericht erfolgreich aktualisiert!',
  MEAL_DELETED: 'Gericht erfolgreich gelöscht!'
};
