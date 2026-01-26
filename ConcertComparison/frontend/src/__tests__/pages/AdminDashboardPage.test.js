import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import AdminDashboardPage from '../../pages/AdminDashboardPage';
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

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('AdminDashboardPage', () => {
  const mockConcerts = {
    content: [
      {
        id: '1',
        name: 'Taylor Swift Konzert',
        date: '2026-06-15T20:00:00',
        venue: 'Olympiastadion Berlin',
        description: 'Ein großartiges Konzert',
        availableSeats: 500,
        totalSeats: 1000,
      },
      {
        id: '2',
        name: 'Ed Sheeran Live',
        date: '2026-07-20T19:00:00',
        venue: 'Allianz Arena München',
        description: 'Welttournee 2026',
        availableSeats: 0,
        totalSeats: 800,
      },
    ],
    totalPages: 1,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('userRole', 'ADMIN');
    concertService.fetchConcerts.mockResolvedValue(mockConcerts);
  });

  const renderPage = () => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          <CartProvider>
            <AdminDashboardPage />
          </CartProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  };

  test('zeigt Dashboard-Überschrift', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
    });
  });

  test('zeigt Konzerte in Tabelle an', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Taylor Swift Konzert')).toBeInTheDocument();
      expect(screen.getByText('Ed Sheeran Live')).toBeInTheDocument();
    });
  });

  test('zeigt Aktiv-Badge für verfügbare Konzerte', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Aktiv')).toBeInTheDocument();
    });
  });

  test('zeigt Ausverkauft-Badge für ausverkaufte Konzerte', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Ausverkauft')).toBeInTheDocument();
    });
  });

  test('zeigt Link zum Erstellen eines neuen Konzerts', async () => {
    renderPage();

    await waitFor(() => {
      // Es gibt zwei Links zum Erstellen (Sidebar und Hauptbereich)
      const links = screen.getAllByRole('link', { name: /Neues Konzert/i });
      expect(links.length).toBeGreaterThanOrEqual(1);
    });
  });

  test('zeigt Ladeanimation während des Ladens', () => {
    concertService.fetchConcerts.mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    renderPage();

    expect(screen.getByText('Lade Konzerte...')).toBeInTheDocument();
  });

  test('zeigt Fehlermeldung bei API-Fehler', async () => {
    concertService.fetchConcerts.mockRejectedValue(new Error('API Error'));

    renderPage();

    await waitFor(() => {
      expect(screen.getByText(/konnten nicht geladen werden/i)).toBeInTheDocument();
    });
  });

  test('zeigt leeren Zustand wenn keine Konzerte vorhanden', async () => {
    concertService.fetchConcerts.mockResolvedValue({
      content: [],
      totalPages: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Keine Konzerte vorhanden.')).toBeInTheDocument();
    });
  });
});
