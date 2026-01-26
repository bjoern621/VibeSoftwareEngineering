import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import authService from '../../services/authService';

/**
 * AdminRoute - Wrapper component for admin-only routes
 * Redirects to login if user is not authenticated
 * Redirects to concerts page with 403 message if user is not an admin
 */
const AdminRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  const userRole = authService.getUserRole();

  if (loading) {
    // Zeige Ladeanimation während der Authentifizierungsprüfung
    return (
      <div className="min-h-screen bg-background-light flex items-center justify-center">
        <div className="text-center space-y-4">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="text-text-secondary">Wird geladen...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Weiterleitung zur Login-Seite mit Rückkehr-URL
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (userRole !== 'ADMIN') {
    // Weiterleitung zur Konzertseite mit Fehlermeldung für nicht-Admin-Benutzer
    return (
      <Navigate 
        to="/concerts" 
        state={{ 
          error: 'Zugriff verweigert. Sie benötigen Administrator-Rechte für diesen Bereich.',
          errorType: 'forbidden'
        }} 
        replace 
      />
    );
  }

  return children;
};

export default AdminRoute;
