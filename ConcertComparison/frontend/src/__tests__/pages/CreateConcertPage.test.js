import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import CreateConcertPage from '../../pages/CreateConcertPage';
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
  useParams: () => ({}), // Keine ID = Create-Modus
}));

describe('CreateConcertPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('userRole', 'ADMIN');
  });

  const renderPage = () => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          <CartProvider>
            <CreateConcertPage />
          </CartProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  };

  test('zeigt Formular zum Erstellen eines neuen Konzerts', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neues Konzert erstellen')).toBeInTheDocument();
    });

    // Prüfe Formularfelder
    expect(screen.getByLabelText(/Konzertname/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Datum/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Uhrzeit/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Veranstaltungsort/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Beschreibung/i)).toBeInTheDocument();
  });

  test('zeigt Validierungsfehler bei leerem Formular', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neues Konzert erstellen')).toBeInTheDocument();
    });

    // Klicke auf Erstellen ohne Felder auszufüllen
    fireEvent.click(screen.getByRole('button', { name: /Erstellen/i }));

    await waitFor(() => {
      expect(screen.getByText('Name ist erforderlich')).toBeInTheDocument();
    });
  });

  test('validiert Mindestlänge für Name', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neues Konzert erstellen')).toBeInTheDocument();
    });

    // Gib zu kurzen Namen ein
    fireEvent.change(screen.getByLabelText(/Konzertname/i), {
      target: { value: 'AB' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Erstellen/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Name muss mindestens 3 Zeichen lang sein')
      ).toBeInTheDocument();
    });
  });

  test('sendet Formular erfolgreich ab', async () => {
    adminService.createEvent.mockResolvedValue({ id: '123' });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neues Konzert erstellen')).toBeInTheDocument();
    });

    // Fülle Formular aus
    fireEvent.change(screen.getByLabelText(/Konzertname/i), {
      target: { value: 'Taylor Swift Konzert' },
    });

    // Setze Datum auf morgen
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateString = tomorrow.toISOString().split('T')[0];
    fireEvent.change(screen.getByLabelText(/Datum/i), {
      target: { value: dateString },
    });

    fireEvent.change(screen.getByLabelText(/Uhrzeit/i), {
      target: { value: '20:00' },
    });

    fireEvent.change(screen.getByLabelText(/Veranstaltungsort/i), {
      target: { value: 'Olympiastadion Berlin' },
    });

    fireEvent.change(screen.getByLabelText(/Beschreibung/i), {
      target: { value: 'Ein großartiges Konzert mit vielen Hits!' },
    });

    // Sende Formular ab
    fireEvent.click(screen.getByRole('button', { name: /Erstellen/i }));

    await waitFor(() => {
      expect(adminService.createEvent).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Konzert wurde erfolgreich erstellt/i)
      ).toBeInTheDocument();
    });
  });

  test('zeigt Fehlermeldung bei API-Fehler', async () => {
    adminService.createEvent.mockRejectedValue(new Error('API Error'));

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neues Konzert erstellen')).toBeInTheDocument();
    });

    // Fülle Formular aus
    fireEvent.change(screen.getByLabelText(/Konzertname/i), {
      target: { value: 'Taylor Swift Konzert' },
    });

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateString = tomorrow.toISOString().split('T')[0];
    fireEvent.change(screen.getByLabelText(/Datum/i), {
      target: { value: dateString },
    });

    fireEvent.change(screen.getByLabelText(/Uhrzeit/i), {
      target: { value: '20:00' },
    });

    fireEvent.change(screen.getByLabelText(/Veranstaltungsort/i), {
      target: { value: 'Olympiastadion Berlin' },
    });

    fireEvent.change(screen.getByLabelText(/Beschreibung/i), {
      target: { value: 'Ein großartiges Konzert mit vielen Hits!' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Erstellen/i }));

    await waitFor(() => {
      expect(
        screen.getByText(/konnte nicht gespeichert werden/i)
      ).toBeInTheDocument();
    });
  });

  test('navigiert zurück zum Dashboard bei Abbrechen', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Neues Konzert erstellen')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Abbrechen/i }));

    expect(mockNavigate).toHaveBeenCalledWith('/admin');
  });
});
