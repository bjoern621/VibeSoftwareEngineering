import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import CheckoutSuccessPage from '../../pages/CheckoutSuccessPage';
import { useAuth } from '../../context/AuthContext';

// Mock der Contexts
jest.mock('../../context/AuthContext', () => ({
  useAuth: jest.fn(),
}));

// Mock für react-router-dom
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: jest.fn(),
}));

// Import useLocation für individuelle Mocks
import { useLocation } from 'react-router-dom';

// Test-Daten
const mockOrders = [
  {
    id: 'order-123',
    eventName: 'Coldplay - Music of the Spheres',
    eventDate: '2026-07-15T20:00:00Z',
    venue: 'Olympiastadion Berlin',
    seatCategory: 'Premium',
    seatInfo: 'Reihe 5, Platz 12',
    status: 'CONFIRMED',
  },
  {
    id: 'order-456',
    eventName: 'Coldplay - Music of the Spheres',
    eventDate: '2026-07-15T20:00:00Z',
    venue: 'Olympiastadion Berlin',
    seatCategory: 'Premium',
    seatInfo: 'Reihe 5, Platz 13',
    status: 'CONFIRMED',
  },
];

const mockBillingDetails = {
  firstName: 'Max',
  lastName: 'Mustermann',
  email: 'max@test.de',
  address: 'Teststraße 1',
  city: 'Berlin',
  zip: '10115',
};

const mockUser = {
  firstName: 'Max',
  lastName: 'Mustermann',
  email: 'max@test.de',
};

// Standard Location State
const setupLocationMock = (state = null) => {
  useLocation.mockReturnValue({
    state: state || {
      orders: mockOrders,
      totalAmount: 299.98,
      billingDetails: mockBillingDetails,
      paymentMethod: 'creditcard',
    },
  });
};

const renderSuccessPage = () => {
  return render(
    <MemoryRouter initialEntries={['/checkout/success']}>
      <CheckoutSuccessPage />
    </MemoryRouter>
  );
};

describe('CheckoutSuccessPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockNavigate.mockClear();

    useAuth.mockReturnValue({
      isAuthenticated: true,
      user: mockUser,
    });

    setupLocationMock();
  });

  describe('Rendering', () => {
    it('rendert die Erfolgsseite mit Bestätigungsnachricht', () => {
      renderSuccessPage();

      expect(screen.getByText('Kauf erfolgreich!')).toBeInTheDocument();
      expect(screen.getByText(/Vielen Dank für Ihren Einkauf/)).toBeInTheDocument();
    });

    it('zeigt das Erfolgs-Icon an', () => {
      renderSuccessPage();

      expect(screen.getByText('check_circle')).toBeInTheDocument();
    });

    it('zeigt alle Order-IDs an', () => {
      renderSuccessPage();

      expect(screen.getByText(/#ORDER-12/i)).toBeInTheDocument();
      expect(screen.getByText(/#ORDER-45/i)).toBeInTheDocument();
    });

    it('zeigt die Event-Details an', () => {
      renderSuccessPage();

      expect(screen.getAllByText(/Coldplay - Music of the Spheres/).length).toBeGreaterThan(0);
    });

    it('zeigt die Rechnungsdetails an', () => {
      renderSuccessPage();

      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      // Email kann mehrmals vorkommen (Rechnungsdetails und E-Mail-Hinweis)
      expect(screen.getAllByText(/max@test.de/).length).toBeGreaterThan(0);
    });

    it('zeigt die Zahlungsmethode an', () => {
      renderSuccessPage();

      expect(screen.getByText('Kreditkarte')).toBeInTheDocument();
    });

    it('zeigt den Gesamtbetrag an', () => {
      renderSuccessPage();

      expect(screen.getByText('€299.98')).toBeInTheDocument();
    });

    it('zeigt die Anzahl der Tickets an', () => {
      renderSuccessPage();

      expect(screen.getByText('2')).toBeInTheDocument();
    });
  });

  describe('Order Status', () => {
    it('zeigt den Bestätigt-Status für jede Order', () => {
      renderSuccessPage();

      const statusBadges = screen.getAllByText(/Bestätigt|CONFIRMED/);
      expect(statusBadges.length).toBeGreaterThan(0);
    });
  });

  describe('Navigation', () => {
    it('leitet zur Konzertseite um wenn keine Orders vorhanden', () => {
      setupLocationMock({ orders: [] });

      renderSuccessPage();

      expect(mockNavigate).toHaveBeenCalledWith('/concerts');
    });

    it('leitet um wenn state null ist', () => {
      setupLocationMock(null);
      // Muss explizit mit leeren Orders rendern
      useLocation.mockReturnValue({ state: null });

      renderSuccessPage();

      expect(mockNavigate).toHaveBeenCalledWith('/concerts');
    });

    it('enthält Link zu Meine Bestellungen', () => {
      renderSuccessPage();

      // Prüfe ob der Text "Meine Bestellungen" vorhanden ist
      expect(screen.getByText(/Meine Bestellungen/i)).toBeInTheDocument();
    });

    it('enthält Link zu Weitere Konzerte entdecken', () => {
      renderSuccessPage();

      // Prüfe ob der Text "Weitere Konzerte entdecken" vorhanden ist
      expect(screen.getByText(/Weitere Konzerte entdecken/i)).toBeInTheDocument();
    });
  });

  describe('Ticket Download', () => {
    it('zeigt Download-Links für jedes Ticket', () => {
      renderSuccessPage();

      const downloadLinks = screen.getAllByText(/Ticket anzeigen/);
      expect(downloadLinks.length).toBe(2);
    });
  });

  describe('Email Notification', () => {
    it('zeigt E-Mail-Bestätigungshinweis', () => {
      renderSuccessPage();

      expect(screen.getByText(/Bestätigung per E-Mail/)).toBeInTheDocument();
      // Email erscheint mehrmals auf der Seite - verwende getAllByText
      const emailElements = screen.getAllByText(/max@test.de/);
      expect(emailElements.length).toBeGreaterThan(0);
    });
  });

  describe('PayPal Payment Method', () => {
    it('zeigt PayPal als Zahlungsmethode korrekt an', () => {
      setupLocationMock({
        orders: mockOrders,
        totalAmount: 299.98,
        billingDetails: mockBillingDetails,
        paymentMethod: 'paypal',
      });

      renderSuccessPage();

      expect(screen.getByText('PayPal')).toBeInTheDocument();
    });
  });

  describe('Crypto Payment Method', () => {
    it('zeigt Kryptowährung als Zahlungsmethode korrekt an', () => {
      setupLocationMock({
        orders: mockOrders,
        totalAmount: 299.98,
        billingDetails: mockBillingDetails,
        paymentMethod: 'crypto',
      });

      renderSuccessPage();

      expect(screen.getByText('Kryptowährung')).toBeInTheDocument();
    });
  });
});
