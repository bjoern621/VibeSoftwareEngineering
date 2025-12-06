/**
 * Axios Konfiguration mit JWT-Token-Interceptor
 *
 * Diese Datei stellt eine vorkonfigurierte Axios-Instanz bereit, die:
 * - Die Backend Base-URL aus Environment-Variablen verwendet
 * - Automatisch JWT-Tokens zu geschützten Requests hinzufügt
 * - Bei 401-Fehlern (Unauthorized) automatisch zum Login umleitet
 */

import axios from 'axios';

// Base URL aus Environment-Variablen
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';
const TOKEN_STORAGE_KEY = process.env.REACT_APP_TOKEN_STORAGE_KEY || 'rentacar_jwt_token';
const REFRESH_TOKEN_STORAGE_KEY = 'rentacar_refresh_token';

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
 */
apiClient.interceptors.response.use(
  (response) => {
    // Erfolgreiche Responses durchlassen
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // 401 Unauthorized → Token ist abgelaufen, versuche Refresh
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      console.log('🔴 401 Unauthorized detected - attempting token refresh...');
      originalRequest._retry = true; // Verhindere Endlosschleifen

      try {
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

        // Original Request mit neuem Token wiederholen
        originalRequest.headers.Authorization = `Bearer ${token}`;
        console.log('🔄 Retrying original request with new token...');
        return apiClient(originalRequest);

      } catch (refreshError) {
        console.error('❌ Token refresh failed:', refreshError.message);
        // Refresh fehlgeschlagen → Logout und Redirect zu Login
        localStorage.removeItem(TOKEN_STORAGE_KEY);
        localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);

        // Zum Login umleiten (nur wenn nicht bereits auf Login-Seite)
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }

        return Promise.reject(refreshError);
      }
    }

    // Andere Fehler durchreichen
    return Promise.reject(error);
  }
);

export default apiClient;
export { API_BASE_URL, TOKEN_STORAGE_KEY };
