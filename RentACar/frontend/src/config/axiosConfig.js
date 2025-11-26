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
 * Response Interceptor: Behandelt Authentifizierungsfehler
 */
apiClient.interceptors.response.use(
  (response) => {
    // Erfolgreiche Responses durchlassen
    return response;
  },
  (error) => {
    // 401 Unauthorized → Token ist ungültig oder abgelaufen
    if (error.response && error.response.status === 401) {
      // Token aus localStorage entfernen
      localStorage.removeItem(TOKEN_STORAGE_KEY);

      // Zum Login umleiten (nur wenn nicht bereits auf Login-Seite)
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
export { API_BASE_URL, TOKEN_STORAGE_KEY };
