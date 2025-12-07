/**
 * Axios Konfiguration mit JWT-Token-Interceptor und globalem Error Handling
 *
 * Diese Datei stellt eine vorkonfigurierte Axios-Instanz bereit, die:
 * - Die Backend Base-URL aus Environment-Variablen verwendet
 * - Automatisch JWT-Tokens zu geschützten Requests hinzufügt
 * - Bei 401-Fehlern (Unauthorized) automatisch Token-Refresh oder Logout durchführt
 * - Alle anderen HTTP-Fehler mit Toast-Benachrichtigungen behandelt
 */

import axios from 'axios';
import toast from 'react-hot-toast';

// Base URL aus Environment-Variablen
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';
const TOKEN_STORAGE_KEY = process.env.REACT_APP_TOKEN_STORAGE_KEY || 'rentacar_jwt_token';
const REFRESH_TOKEN_STORAGE_KEY = 'rentacar_refresh_token';

// Zähler für fehlgeschlagene Refresh-Versuche (verhindert Endlosschleifen)
let failedRefreshAttempts = 0;
const MAX_REFRESH_ATTEMPTS = 3;

/**
 * Vorkonfigurierte Axios-Instanz für API-Calls
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 Sekunden Timeout
});

/**
 * Request Interceptor: Fügt JWT-Token zu jedem Request hinzu
 */
apiClient.interceptors.request.use(
  (config) => {
    // Token aus localStorage holen
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);

    if (token) {
      // Token im Authorization-Header hinzufügen (Bearer-Schema)
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    // Request-Fehler behandeln
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor: Behandelt Authentifizierungsfehler mit automatischem Token-Refresh
 * und alle anderen HTTP-Fehler mit benutzerfreundlichen Toast-Benachrichtigungen
 */
apiClient.interceptors.response.use(
  (response) => {
    // Erfolgreiche Responses durchlassen
    // Bei erfolgreicher Response: Refresh-Attempt-Counter zurücksetzen
    failedRefreshAttempts = 0;
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Network-Fehler (Offline, Server nicht erreichbar)
    if (!error.response) {
      console.error('❌ Network error:', error.message);
      toast.error('Verbindungsfehler. Bitte überprüfen Sie Ihre Internetverbindung.');
      return Promise.reject(error);
    }

    const status = error.response.status;
    const requestUrl = originalRequest.url || '';

    // 401 Unauthorized → Token ist abgelaufen, versuche Refresh
    // ABER: Login/Register-Endpoints ausnehmen (dort gibt es noch KEINE Session)
    const isAuthEndpoint = requestUrl.includes('/login') || requestUrl.includes('/registrierung');
    
    if (status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      console.log('🔴 401 Unauthorized detected - attempting token refresh...');
      originalRequest._retry = true; // Verhindere Endlosschleifen

      try {
        // Prüfe, ob max. Anzahl an Refresh-Versuchen überschritten
        if (failedRefreshAttempts >= MAX_REFRESH_ATTEMPTS) {
          console.error(`❌ Max refresh attempts (${MAX_REFRESH_ATTEMPTS}) exceeded - forcing logout`);
          throw new Error('Maximale Anzahl an Token-Refresh-Versuchen überschritten');
        }

        // Refresh Token aus localStorage holen
        const refreshToken = localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY);
        console.log('🔑 Refresh Token from localStorage:', refreshToken ? 'EXISTS' : 'MISSING');

        if (!refreshToken) {
          // Kein Refresh Token vorhanden → Logout
          console.error('❌ No refresh token available - forcing logout');
          throw new Error('Kein Refresh Token vorhanden');
        }

        console.log('📤 Calling POST /api/auth/refresh...');
        // Token-Refresh durchführen (verwende raw axios statt apiClient um Interceptor-Loop zu vermeiden)
        const refreshResponse = await axios.post(
          `${API_BASE_URL}/auth/refresh`,
          { refreshToken }
        );

        console.log('✅ Token refresh successful!');
        // Neue Tokens aus Response extrahieren
        const { token, refreshToken: newRefreshToken } = refreshResponse.data;

        // Neue Tokens in localStorage speichern
        localStorage.setItem(TOKEN_STORAGE_KEY, token);
        localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, newRefreshToken);
        console.log('💾 New tokens saved to localStorage');

        // Refresh-Counter zurücksetzen
        failedRefreshAttempts = 0;

        // Original Request mit neuem Token wiederholen
        originalRequest.headers.Authorization = `Bearer ${token}`;
        console.log('🔄 Retrying original request with new token...');
        return apiClient(originalRequest);

      } catch (refreshError) {
        console.error('❌ Token refresh failed:', refreshError.message);
        failedRefreshAttempts++;

        // Refresh fehlgeschlagen → Logout und Redirect zu Login
        localStorage.removeItem(TOKEN_STORAGE_KEY);
        localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);

        toast.error('Ihre Sitzung ist abgelaufen. Bitte melden Sie sich erneut an.');

        // Zum Login umleiten (nur wenn nicht bereits auf Login-Seite)
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }

        return Promise.reject(refreshError);
      }
    }

    // 401 auf Login/Register → falsche Credentials, KEINE Toast (Component handled das)
    if (status === 401 && isAuthEndpoint) {
      console.log('❌ 401 on auth endpoint - invalid credentials (handled by component)');
      return Promise.reject(error);
    }

    // 403 Forbidden → Keine Berechtigung
    if (status === 403) {
      console.error('❌ 403 Forbidden - No permission for this resource');
      toast.error('Sie haben keine Berechtigung für diese Aktion.');
      
      // Redirect zu Forbidden-Seite (nur wenn nicht bereits dort)
      if (!window.location.pathname.includes('/forbidden')) {
        setTimeout(() => {
          window.location.href = '/forbidden';
        }, 1500);
      }
      
      return Promise.reject(error);
    }

    // 404 Not Found → Ressource nicht gefunden
    if (status === 404) {
      console.error('❌ 404 Not Found');
      // Nur Toast anzeigen, kein Redirect (könnte auch API-Ressource sein)
      toast.error('Die angeforderte Ressource wurde nicht gefunden.');
      return Promise.reject(error);
    }

    // 500/502/503/504 Server-Fehler
    if (status >= 500) {
      console.error(`❌ ${status} Server Error`);
      toast.error('Ein Serverfehler ist aufgetreten. Bitte versuchen Sie es später erneut.');
      
      // Bei kritischen Server-Fehlern → Redirect zu Server-Error-Seite
      if (!window.location.pathname.includes('/server-error')) {
        setTimeout(() => {
          window.location.href = '/server-error';
        }, 2000);
      }
      
      return Promise.reject(error);
    }

    // 400 Bad Request → Validierungsfehler (werden meist von Services behandelt)
    if (status === 400) {
      console.error('❌ 400 Bad Request');
      // Keine generische Toast-Nachricht, da spezifischer Error meist vom Service behandelt wird
      return Promise.reject(error);
    }

    // 429 Too Many Requests → Rate Limiting
    if (status === 429) {
      console.error('❌ 429 Too Many Requests - Rate limiting');
      const retryAfter = error.response.headers['retry-after'];
      const message = retryAfter
        ? `Zu viele Anfragen. Bitte warten Sie ${retryAfter} Sekunden.`
        : 'Zu viele Anfragen. Bitte versuchen Sie es später erneut.';
      toast.error(message, { duration: 8000 });
      return Promise.reject(error);
    }

    // Andere Fehler durchreichen (ohne Toast, außer es ist ein unbekannter Fehler)
    if (status >= 400 && status < 500) {
      console.error(`❌ ${status} Client Error`);
      // Keine generische Nachricht für andere Client-Fehler
    }

    return Promise.reject(error);
  }
);

export default apiClient;
export { API_BASE_URL, TOKEN_STORAGE_KEY };
