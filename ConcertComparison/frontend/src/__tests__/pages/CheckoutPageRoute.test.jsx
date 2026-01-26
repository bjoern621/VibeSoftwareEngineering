import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import CheckoutPage from '../../pages/CheckoutPage';
import { purchaseBulkTickets } from '../../api/checkoutApi';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';

// Mock der API
jest.mock('../../api/checkoutApi', () => ({
  purchaseBulkTickets: jest.fn(),
}));

// Mock der Contexts
jest.mock('../../context/AuthContext', () => ({
  useAuth: jest.fn(),
}));

jest.mock('../../context/CartContext', () => ({
  useCart: jest.fn(),
}));

// Mock für react-router-dom Navigation
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => ({ state: null }),
}));

// Test-Daten
const mockCartItems = [
  {
    holdId: 'hold-1',
    seat: {
      id: 'seat-1',
      category: 'VIP',
      row: 'A',
      number: 1,
      price: 99.99,
    },
    concert: {
      id: 'concert-1',
      name: 'Test Konzert',
      date: '2026-06-15T20:00:00Z',
      venue: 'Olympiahalle München',
      imageUrl: '/test-image.jpg',
    },
    expiresAt: new Date(Date.now() + 600000).toISOString(), // 10 Minuten
  },
  {
    holdId: 'hold-2',
    seat: {
      id: 'seat-2',
      category: 'VIP',
      row: 'A',
      number: 2,
      price: 99.99,
    },
    concert: {
      id: 'concert-1',
      name: 'Test Konzert',
      date: '2026-06-15T20:00:00Z',
      venue: 'Olympiahalle München',
      imageUrl: '/test-image.jpg',
    },
    expiresAt: new Date(Date.now() + 600000).toISOString(),
  },
];

const mockUser = {
  firstName: 'Max',
  lastName: 'Mustermann',
  email: 'max@test.de',
};

// Standard-Mocks für Auth und Cart
const setupMocks = (overrides = {}) => {
  useAuth.mockReturnValue({
    isAuthenticated: true,
    user: mockUser,
    ...overrides.auth,
  });

  useCart.mockReturnValue({
    cartItems: mockCartItems,
    itemCount: 2,
    subtotal: 199.98,
    serviceFees: 15.99,
    total: 215.97,
    oldestItem: mockCartItems[0],
    isEmpty: false,
    clearCart: jest.fn(),
    removeItem: jest.fn(),
    getAllHoldIds: () => ['hold-1', 'hold-2'],
    ...overrides.cart,
  });
};

const renderCheckoutPage = () => {
  return render(
    <MemoryRouter initialEntries={['/checkout']}>
      <CheckoutPage />
    </MemoryRouter>
  );
};

describe('CheckoutPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockNavigate.mockClear();
    setupMocks();
  });

  describe('Rendering', () => {
    it('rendert die Checkout-Seite mit allen Komponenten', () => {
      renderCheckoutPage();

      expect(screen.getByRole('heading', { name: 'Checkout' })).toBeInTheDocument();
      expect(screen.getByText('Rechnungsdetails')).toBeInTheDocument();
      expect(screen.getByText('Zahlungsmethode')).toBeInTheDocument();
      // "Ihre Tickets" erscheint mehrmals (Timer + Überschrift)
      expect(screen.getAllByText(/Ihre Tickets/).length).toBeGreaterThan(0);
    });

    it('zeigt die Warenkorb-Items an', () => {
      renderCheckoutPage();

      expect(screen.getAllByText('Test Konzert').length).toBeGreaterThan(0);
      expect(screen.getByText(/VIP – Reihe A, Platz 1/)).toBeInTheDocument();
    });

    it('zeigt den Gesamtbetrag im Button an', () => {
      renderCheckoutPage();

      expect(screen.getByTestId('checkout-submit-btn')).toHaveTextContent('€215.97');
    });

    it('zeigt den Countdown-Timer an', () => {
      renderCheckoutPage();

      // Timer sollte sichtbar sein (zeigt Minuten:Sekunden)
      expect(screen.getByText(/High Demand!/i)).toBeInTheDocument();
    });
  });

  describe('Hold Validation', () => {
    it('deaktiviert den Checkout-Button wenn Hold abgelaufen ist', async () => {
      jest.useFakeTimers();

      // Cart mit abgelaufenem Hold
      const expiredItem = {
        ...mockCartItems[0],
        expiresAt: new Date(Date.now() - 1000).toISOString(), // Abgelaufen
      };

      setupMocks({
        cart: {
          oldestItem: expiredItem,
          cartItems: [expiredItem],
        },
      });

      renderCheckoutPage();

      // Warten auf State-Update
      act(() => {
        jest.advanceTimersByTime(1000);
      });

      await waitFor(() => {
        const button = screen.getByTestId('checkout-submit-btn');
        expect(button).toBeDisabled();
      });

      jest.useRealTimers();
    });

    it('zeigt Fehlermeldung wenn Hold abgelaufen ist', async () => {
      jest.useFakeTimers();

      renderCheckoutPage();

      // Timer ablaufen lassen (10 Minuten + 1 Sekunde)
      act(() => {
        jest.advanceTimersByTime(601000);
      });

      await waitFor(() => {
        expect(screen.getByText(/Reservierung ist abgelaufen/i)).toBeInTheDocument();
      });

      jest.useRealTimers();
    });
  });

  describe('Authentication', () => {
    it('leitet nicht-authentifizierte Benutzer zum Login weiter', () => {
      setupMocks({
        auth: { isAuthenticated: false, user: null },
      });

      renderCheckoutPage();

      expect(mockNavigate).toHaveBeenCalledWith('/login', {
        state: { from: '/checkout' },
      });
    });
  });

  describe('Empty Cart', () => {
    it('leitet zum Warenkorb um wenn dieser leer ist', () => {
      setupMocks({
        cart: {
          isEmpty: true,
          cartItems: [],
          oldestItem: null,
        },
      });

      renderCheckoutPage();

      expect(mockNavigate).toHaveBeenCalledWith('/cart');
    });
  });

  describe('Checkout Process', () => {
    it('führt erfolgreichen Checkout durch und leitet zur Erfolgsseite weiter', async () => {
      const mockClearCart = jest.fn();
      const mockRemoveItem = jest.fn();

      setupMocks({
        cart: {
          clearCart: mockClearCart,
          removeItem: mockRemoveItem,
        },
      });

      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-1', success: true, data: { id: 'order-1' } },
        { holdId: 'hold-2', success: true, data: { id: 'order-2' } },
      ]);

      renderCheckoutPage();

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(purchaseBulkTickets).toHaveBeenCalledWith(
          ['hold-1', 'hold-2'],
          'max@test.de',
          'CREDIT_CARD'
        );
      });

      await waitFor(() => {
        expect(mockClearCart).toHaveBeenCalled();
        expect(mockNavigate).toHaveBeenCalledWith('/checkout/success', expect.any(Object));
      });
    });

    it('zeigt Fehlermeldung bei fehlgeschlagenem Checkout', async () => {
      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-1', success: false, error: 'Platz nicht mehr verfügbar' },
        { holdId: 'hold-2', success: false, error: 'Platz nicht mehr verfügbar' },
      ]);

      renderCheckoutPage();

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(screen.getByText(/Kauf fehlgeschlagen/)).toBeInTheDocument();
      });
    });

    it('behandelt teilweise erfolgreiche Checkouts', async () => {
      const mockRemoveItem = jest.fn();

      setupMocks({
        cart: {
          removeItem: mockRemoveItem,
        },
      });

      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-1', success: true, data: { id: 'order-1' } },
        { holdId: 'hold-2', success: false, error: 'Fehler' },
      ]);

      renderCheckoutPage();

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(screen.getByText(/1 Ticket\(s\) erfolgreich gekauft/)).toBeInTheDocument();
        expect(mockRemoveItem).toHaveBeenCalledWith('hold-1');
      });
    });

    it('zeigt Ladezustand während des Checkouts', async () => {
      purchaseBulkTickets.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 1000))
      );

      renderCheckoutPage();

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(screen.getByText(/Wird verarbeitet/)).toBeInTheDocument();
      });
    });
  });

  describe('Form Validation', () => {
    it('zeigt Fehler wenn Pflichtfelder fehlen', async () => {
      // Benutzer ohne vorausgefüllte Daten
      setupMocks({
        auth: { isAuthenticated: true, user: { firstName: '', lastName: '', email: '' } },
      });

      renderCheckoutPage();

      // Formularfelder leeren
      const firstNameInput = screen.getByPlaceholderText('First Name');
      fireEvent.change(firstNameInput, { target: { value: '' } });

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(screen.getByText(/erforderlichen Felder/)).toBeInTheDocument();
      });
    });
  });
});
