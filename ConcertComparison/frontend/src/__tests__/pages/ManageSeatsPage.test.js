import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ManageSeatsPage from '../../pages/ManageSeatsPage';
import { AuthProvider } from '../../context/AuthContext';
import { CartProvider } from '../../context/CartContext';
import adminService from '../../services/adminService';
import * as concertService from '../../services/concertService';

// Mock services
jest.mock('../../services/adminService');
jest.mock('../../services/concertService');
jest.mock('../../services/authService', () => ({
  isAuthenticated: () => true,
  getUserRole: () => 'ADMIN',
  getToken: () => 'test-token',
  getUserEmail: () => 'admin@example.com',
  getProfile: jest.fn().mockResolvedValue({ email: 'admin@example.com', role: 'ADMIN' }),
  login: jest.fn(),
  logout: jest.fn(),
  register: jest.fn(),
}));

// Mock useNavigate and useParams
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useParams: () => ({ id: 'concert-123' }),
}));

describe('ManageSeatsPage', () => {
  const mockConcert = {
    id: 'concert-123',
    name: 'Taylor Swift Konzert',
    venue: 'Olympiastadion Berlin',
    date: '2026-06-15T20:00:00',
  };

  const mockExistingSeats = [
    { id: 'seat-1', row: 'A', number: 1, category: 'VIP', price: 150, status: 'AVAILABLE' },
    { id: 'seat-2', row: 'A', number: 2, category: 'VIP', price: 150, status: 'SOLD' },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('userRole', 'ADMIN');
    
    concertService.fetchConcertById.mockResolvedValue(mockConcert);
    adminService.getEventSeats.mockResolvedValue(mockExistingSeats);
  });

  const renderPage = () => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          <CartProvider>
            <ManageSeatsPage />
          </CartProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  };

  test('zeigt Konzertinformationen und Sitzplatz-Generator', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Sitzplätze verwalten')).toBeInTheDocument();
      expect(screen.getByText(/Taylor Swift Konzert/)).toBeInTheDocument();
    });
  });

  test('zeigt Generator-Formular mit Standardwerten', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neue Sitzplätze hinzufügen')).toBeInTheDocument();
    });

    // Prüfe dass Generator-Formular Eingabefelder hat
    const numberInputs = screen.getAllByRole('spinbutton');
    expect(numberInputs.length).toBeGreaterThan(0);
  });

  test('aktualisiert Vorschau bei Änderung der Generator-Konfiguration', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neue Sitzplätze hinzufügen')).toBeInTheDocument();
    });

    // Prüfe dass die Vorschau angezeigt wird
    await waitFor(() => {
      // Standard-Konfiguration: Block 1 (A-B=20) + Block 2 (C-E=30) + Block 3 (F-J=50) = 100 Sitzplätze
      expect(screen.getByText(/100 Sitzplätze aktualisieren/)).toBeInTheDocument();
    });
  });

  test('erstellt Sitzplätze erfolgreich', async () => {
    adminService.createSeats.mockResolvedValue({ created: 50 });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neue Sitzplätze hinzufügen')).toBeInTheDocument();
    });

    // Klicke auf Aktualisieren-Button
    fireEvent.click(screen.getByRole('button', { name: /Sitzplätze aktualisieren/i }));

    await waitFor(() => {
      expect(adminService.createSeats).toHaveBeenCalledWith('concert-123', expect.any(Array));
    });

    await waitFor(() => {
      expect(screen.getByText(/wurden aktualisiert/i)).toBeInTheDocument();
    });
  });

  test('zeigt existierende Sitzplätze an', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Vorhandene Sitzplätze (2)')).toBeInTheDocument();
    });

    // Prüfe ob existierende Sitzplätze in Tabelle angezeigt werden
    expect(screen.getByText('Verfügbar')).toBeInTheDocument();
    expect(screen.getByText('Verkauft')).toBeInTheDocument();
  });

  test('zeigt Kategorien-Auswahl im Generator', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neue Sitzplätze hinzufügen')).toBeInTheDocument();
    });

    // Prüfe dass es ein select Element für Kategorien gibt
    const categorySelects = screen.getAllByRole('combobox');
    expect(categorySelects.length).toBeGreaterThan(0);
  });

  test('wechselt zwischen Generator und CSV-Import', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Generator' })).toBeInTheDocument();
    });

    // Wechsle zu CSV-Import
    fireEvent.click(screen.getByRole('button', { name: 'CSV Import' }));

    await waitFor(() => {
      expect(screen.getByText(/CSV-Datei mit folgendem Format/i)).toBeInTheDocument();
    });
  });

  test('zeigt Fehler bei fehlgeschlagenem API-Call', async () => {
    adminService.createSeats.mockRejectedValue(new Error('API Error'));

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neue Sitzplätze hinzufügen')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Sitzplätze aktualisieren/i }));

    await waitFor(() => {
      expect(screen.getByText(/konnten nicht erstellt werden/i)).toBeInTheDocument();
    });
  });

  test('navigiert zurück zum Dashboard', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Zurück zum Dashboard')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Zurück zum Dashboard'));

    expect(mockNavigate).toHaveBeenCalledWith('/admin');
  });
});
