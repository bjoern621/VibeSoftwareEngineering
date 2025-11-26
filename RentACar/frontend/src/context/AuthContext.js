/**
 * Auth Context - Globales State Management für Authentifizierung
 *
 * Verwaltet:
 * - Login-Status
 * - Aktuelle Benutzerdaten
 * - Login/Logout-Funktionen
 *
 * DDD-Prinzip: Dieser Context kapselt den gesamten Auth-State
 * und stellt ihn der gesamten App zur Verfügung (Context API Pattern).
 */

import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from '../services/authService';

// Context erstellen
const AuthContext = createContext(null);

/**
 * AuthProvider - Wrapper-Komponente für die gesamte App
 */
export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  /**
   * Initialisierung: Prüft ob User eingeloggt ist beim App-Start
   */
  useEffect(() => {
    const initAuth = async () => {
      try {
        // Prüfen ob Token vorhanden ist
        if (authService.isAuthenticated()) {
          // Versuche Profil zu laden
          const profile = await authService.getProfile();
          setUser(profile);
          setIsAuthenticated(true);
        }
      } catch (error) {
        // Token ungültig/abgelaufen → Logout
        authService.logout();
        setIsAuthenticated(false);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  /**
   * Login-Funktion
   */
  const login = async (email, password) => {
    try {
      const response = await authService.login(email, password);

      // Profil laden nach erfolgreichem Login
      const profile = await authService.getProfile();
      setUser(profile);
      setIsAuthenticated(true);

      return response;
    } catch (error) {
      throw error;
    }
  };

  /**
   * Logout-Funktion
   */
  const logout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUser(null);
  };

  /**
   * Registrierungs-Funktion
   */
  const register = async (registrationData) => {
    try {
      const response = await authService.register(registrationData);
      return response;
    } catch (error) {
      throw error;
    }
  };

  /**
   * Context-Value: Alle Funktionen und States
   */
  const value = {
    isAuthenticated,
    user,
    loading,
    login,
    logout,
    register,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Custom Hook: useAuth()
 * Ermöglicht einfachen Zugriff auf Auth-Context in Komponenten
 *
 * Verwendung:
 * const { isAuthenticated, user, login, logout } = useAuth();
 */
export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth muss innerhalb von AuthProvider verwendet werden');
  }

  return context;
};

export default AuthContext;
