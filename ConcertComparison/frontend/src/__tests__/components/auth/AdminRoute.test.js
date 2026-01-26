import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import AdminRoute from '../../../components/auth/AdminRoute';
import { AuthProvider } from '../../../context/AuthContext';
import authService from '../../../services/authService';

// Mock auth service
jest.mock('../../../services/authService');

describe('AdminRoute', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  const renderWithRouter = (ui, { initialEntries = ['/admin'] } = {}) => {
    return render(
      <MemoryRouter initialEntries={initialEntries}>
        <AuthProvider>
          {ui}
        </AuthProvider>
      </MemoryRouter>
    );
  };

  test('zeigt Ladeanimation oder verarbeitet Loading-State', async () => {
    // Dieser Test prüft, dass die Komponente korrekt rendert
    // Der Loading-State wird durch AuthProvider gesteuert
    authService.isAuthenticated.mockReturnValue(true);
    authService.getUserRole.mockReturnValue('ADMIN');
    authService.getProfile.mockResolvedValue({ email: 'admin@test.com', role: 'ADMIN' });
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('userRole', 'ADMIN');
    
    renderWithRouter(
      <AdminRoute>
        <div>Admin Content</div>
      </AdminRoute>
    );

    // Warte auf das Laden - entweder Ladeanimation oder Inhalt sollte erscheinen
    await waitFor(() => {
      const hasContent = screen.queryByText('Admin Content');
      const hasLoading = screen.queryByText('Wird geladen...');
      expect(hasContent || hasLoading).toBeTruthy();
    });
  });

  test('leitet nicht-authentifizierte Benutzer zur Login-Seite weiter', async () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.getUserRole.mockReturnValue(null);
    localStorage.removeItem('token');

    renderWithRouter(
      <AdminRoute>
        <div>Admin Content</div>
      </AdminRoute>
    );

    await waitFor(() => {
      // Sollte Admin-Inhalt nicht anzeigen
      expect(screen.queryByText('Admin Content')).not.toBeInTheDocument();
    });
  });

  test('leitet normale Benutzer zur Konzertseite weiter', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getUserRole.mockReturnValue('USER');
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('userRole', 'USER');

    renderWithRouter(
      <AdminRoute>
        <div>Admin Content</div>
      </AdminRoute>
    );

    await waitFor(() => {
      // Sollte Admin-Inhalt nicht anzeigen
      expect(screen.queryByText('Admin Content')).not.toBeInTheDocument();
    });
  });

  test('zeigt Admin-Inhalt für Admin-Benutzer', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getUserRole.mockReturnValue('ADMIN');
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('userRole', 'ADMIN');

    renderWithRouter(
      <AdminRoute>
        <div>Admin Content</div>
      </AdminRoute>
    );

    await waitFor(() => {
      expect(screen.getByText('Admin Content')).toBeInTheDocument();
    });
  });
});
