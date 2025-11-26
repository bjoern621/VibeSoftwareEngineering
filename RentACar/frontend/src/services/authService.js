/**
 * Authentication Service
 *
 * Stellt alle authentifizierungsbezogenen API-Calls bereit:
 * - Registrierung
 * - Login
 * - Logout
 * - E-Mail-Verifizierung
 * - Token-Management
 *
 * DDD-Prinzip: Dieser Service kapselt die gesamte Authentifizierungslogik
 * und ist die einzige Schnittstelle zwischen der Presentation Layer (React Components)
 * und der Backend-API für Auth-bezogene Operationen.
 */

import apiClient, { TOKEN_STORAGE_KEY } from '../config/axiosConfig';

/**
 * Kundenregistrierung
 *
 * @param {Object} registrationData - Registrierungsdaten
 * @param {string} registrationData.firstName - Vorname
 * @param {string} registrationData.lastName - Nachname
 * @param {string} registrationData.email - E-Mail-Adresse
 * @param {string} registrationData.password - Passwort
 * @param {string} registrationData.phoneNumber - Telefonnummer
 * @param {string} registrationData.driverLicenseNumber - Führerscheinnummer
 * @param {Object} registrationData.address - Adresse
 * @param {string} registrationData.address.street - Straße
 * @param {string} registrationData.address.postalCode - PLZ
 * @param {string} registrationData.address.city - Stadt
 * @param {string} registrationData.address.country - Land
 * @returns {Promise} Response mit Registrierungsbestätigung
 */
const register = async (registrationData) => {
  try {
    const response = await apiClient.post('/kunden/registrierung', registrationData);
    return response.data;
  } catch (error) {
    // Backend-Fehlermeldungen extrahieren und weiterleiten
    throw extractErrorMessage(error);
  }
};

/**
 * Kundenlogin
 *
 * @param {string} email - E-Mail-Adresse
 * @param {string} password - Passwort
 * @returns {Promise<Object>} Response mit JWT-Token und Kundendaten
 */
const login = async (email, password) => {
  try {
    const response = await apiClient.post('/kunden/login', {
      email,
      password,
    });

    // JWT-Token aus der Response extrahieren
    const { token } = response.data;

    if (token) {
      // Token in localStorage speichern
      localStorage.setItem(TOKEN_STORAGE_KEY, token);
    }

    return response.data;
  } catch (error) {
    throw extractErrorMessage(error);
  }
};

/**
 * Logout (lokaler Logout - Token aus localStorage entfernen)
 */
const logout = () => {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
};

/**
 * Prüft, ob ein JWT-Token vorhanden ist
 *
 * @returns {boolean} true, wenn Token existiert
 */
const isAuthenticated = () => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  return token !== null && token !== undefined && token !== '';
};

/**
 * Gibt das aktuelle JWT-Token zurück
 *
 * @returns {string|null} JWT-Token oder null
 */
const getToken = () => {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
};

/**
 * E-Mail-Verifizierung durchführen
 *
 * @param {string} token - Verification Token aus der E-Mail
 * @returns {Promise} Response mit Bestätigung
 */
const verifyEmail = async (token) => {
  try {
    // Token als Query-Parameter senden (nicht im Body)
    const response = await apiClient.post(`/kunden/verify-email?token=${encodeURIComponent(token)}`);
    return response.data;
  } catch (error) {
    throw extractErrorMessage(error);
  }
};

/**
 * Kundenprofil abrufen (benötigt Authentication)
 *
 * @returns {Promise<Object>} Kundenprofil-Daten
 */
const getProfile = async () => {
  try {
    const response = await apiClient.get('/kunden/profil');
    return response.data;
  } catch (error) {
    throw extractErrorMessage(error);
  }
};

/**
 * Kundenprofil aktualisieren (benötigt Authentication)
 *
 * @param {Object} profileData - Aktualisierte Profildaten
 * @returns {Promise<Object>} Aktualisiertes Profil
 */
const updateProfile = async (profileData) => {
  try {
    const response = await apiClient.put('/kunden/profil', profileData);
    return response.data;
  } catch (error) {
    throw extractErrorMessage(error);
  }
};

/**
 * Hilfsfunktion: Extrahiert Fehlermeldung aus Axios-Error-Response
 *
 * @param {Error} error - Axios-Fehler
 * @returns {Error} Neuer Error mit extrahierter Fehlermeldung
 */
const extractErrorMessage = (error) => {
  if (error.response && error.response.data) {
    // Backend-spezifische Fehlermeldung
    const errorData = error.response.data;

    // Fehlerformat vom Backend: { message: "...", errors: [...] }
    if (errorData.message) {
      return new Error(errorData.message);
    }

    // Validierungsfehler
    if (errorData.errors && Array.isArray(errorData.errors)) {
      return new Error(errorData.errors.join(', '));
    }

    // Generischer Fehler
    return new Error(JSON.stringify(errorData));
  }

  // Netzwerkfehler oder andere Fehler
  return new Error(error.message || 'Ein unerwarteter Fehler ist aufgetreten');
};

// Exported Functions
const authService = {
  register,
  login,
  logout,
  isAuthenticated,
  getToken,
  verifyEmail,
  getProfile,
  updateProfile,
};

export default authService;
