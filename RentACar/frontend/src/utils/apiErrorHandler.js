/**
 * API Error Handler Utility
 * 
 * Zentralisierte Fehlerbehandlung für alle API-Services
 */

/**
 * Extrahiert eine benutzerfreundliche Fehlermeldung aus einem Axios-Error
 * 
 * Unterstützt:
 * - Backend-Fehlermeldungen (message field)
 * - Validierungsfehler (errors array)
 * - Netzwerkfehler
 * - Unbekannte Fehler
 * 
 * @param {Error} error - Axios-Fehler-Objekt
 * @param {string} fallbackMessage - Fallback-Nachricht falls keine spezifische Meldung vorhanden ist
 * @returns {Error} Neuer Error mit extrahierter Fehlermeldung
 */
export const extractErrorMessage = (error, fallbackMessage = 'Ein unerwarteter Fehler ist aufgetreten') => {
  // Backend-Response vorhanden?
  if (error.response && error.response.data) {
    const errorData = error.response.data;

    // Backend-spezifische Fehlermeldung (message field)
    if (errorData.message) {
      return new Error(errorData.message);
    }

    // Validierungsfehler (errors array)
    if (errorData.errors && Array.isArray(errorData.errors)) {
      return new Error(errorData.errors.join(', '));
    }

    // Fehler-Objekt als String
    if (typeof errorData === 'object') {
      return new Error(JSON.stringify(errorData));
    }

    // String-Fehler direkt verwenden
    if (typeof errorData === 'string') {
      return new Error(errorData);
    }
  }

  // Netzwerkfehler (keine Response vom Server)
  if (error.request) {
    return new Error('Keine Verbindung zum Server. Bitte überprüfen Sie Ihre Internetverbindung.');
  }

  // Anderer Fehler (z.B. Fehler beim Aufbau des Requests)
  return new Error(error.message || fallbackMessage);
};

/**
 * Prüft, ob ein Fehler ein 404-Fehler ist
 * 
 * @param {Error} error - Axios-Fehler-Objekt
 * @returns {boolean}
 */
export const isNotFoundError = (error) => {
  return error.response && error.response.status === 404;
};

/**
 * Prüft, ob ein Fehler ein 401-Fehler ist (Unauthorized)
 * 
 * @param {Error} error - Axios-Fehler-Objekt
 * @returns {boolean}
 */
export const isUnauthorizedError = (error) => {
  return error.response && error.response.status === 401;
};

/**
 * Prüft, ob ein Fehler ein 403-Fehler ist (Forbidden)
 * 
 * @param {Error} error - Axios-Fehler-Objekt
 * @returns {boolean}
 */
export const isForbiddenError = (error) => {
  return error.response && error.response.status === 403;
};

/**
 * Prüft, ob ein Fehler ein Validierungsfehler ist (400)
 * 
 * @param {Error} error - Axios-Fehler-Objekt
 * @returns {boolean}
 */
export const isValidationError = (error) => {
  return error.response && error.response.status === 400;
};

/**
 * Prüft, ob ein Fehler ein Server-Fehler ist (5xx)
 * 
 * @param {Error} error - Axios-Fehler-Objekt
 * @returns {boolean}
 */
export const isServerError = (error) => {
  return error.response && error.response.status >= 500;
};

/**
 * Prüft, ob ein Fehler ein Netzwerkfehler ist (kein Response)
 * 
 * @param {Error} error - Axios-Fehler-Objekt
 * @returns {boolean}
 */
export const isNetworkError = (error) => {
  return !error.response && error.request;
};

export default {
  extractErrorMessage,
  isNotFoundError,
  isUnauthorizedError,
  isForbiddenError,
  isValidationError,
  isServerError,
  isNetworkError,
};
