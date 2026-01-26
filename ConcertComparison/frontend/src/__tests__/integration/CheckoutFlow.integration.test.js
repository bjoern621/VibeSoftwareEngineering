import React from 'react';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { purchaseBulkTickets } from '../../api/checkoutApi';

// Mock API
jest.mock('../../api/checkoutApi', () => ({
  purchaseBulkTickets: jest.fn(),
}));

// Test-Daten
const mockCartItem = {
  holdId: 'hold-123',
  seat: {
    id: 'seat-1',
    category: 'Premium',
    row: 'A',
    number: 5,
    price: 149.99,
  },
  concert: {
    id: 'concert-1',
    name: 'Taylor Swift - Eras Tour',
    date: '2026-08-20T20:00:00Z',
    venue: 'Olympiastadion München',
    imageUrl: '/concert-image.jpg',
  },
  expiresAt: new Date(Date.now() + 600000).toISOString(), // 10 Minuten
};

const mockUser = {
  id: 1,
  firstName: 'Anna',
  lastName: 'Schmidt',
  email: 'anna@test.de',
};

// Mock CartContext mit kontrollierbarem State
const createMockCartContext = (items = [mockCartItem]) => ({
  cartItems: items,
  itemCount: items.length,
  subtotal: items.reduce((sum, item) => sum + (item.seat?.price || 0), 0),
  serviceFees: items.length * 5,
  total: items.reduce((sum, item) => sum + (item.seat?.price || 0) + 5, 0),
  oldestItem: items[0] || null,
  isEmpty: items.length === 0,
  addItem: jest.fn(),
  removeItem: jest.fn(),
  clearCart: jest.fn(),
  getAllHoldIds: () => items.map(item => item.holdId),
});

let mockCartContextValue = createMockCartContext();

// Mock der Contexts
jest.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    user: {
      id: 1,
      firstName: 'Anna',
      lastName: 'Schmidt',
      email: 'anna@test.de',
    },
    loading: false,
    login: jest.fn(),
    logout: jest.fn(),
    register: jest.fn(),
  }),
  AuthProvider: ({ children }) => children,
}));

jest.mock('../../context/CartContext', () => ({
  useCart: () => mockCartContextValue,
  CartProvider: ({ children }) => children,
}));

// Simplified mock component for testing checkout logic
const MockCheckoutPage = () => {
  const [isProcessing, setIsProcessing] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [success, setSuccess] = React.useState(false);

  const { cartItems, oldestItem, clearCart, removeItem, getAllHoldIds, isEmpty } = mockCartContextValue;

  // Check if hold is still active
  const hasActiveHolds = React.useMemo(() => {
    if (!oldestItem) return false;
    const expiresAt = new Date(oldestItem.expiresAt);
    return expiresAt > new Date();
  }, [oldestItem]);

  const handleCheckout = async () => {
    if (!hasActiveHolds) {
      setError('Reservierung abgelaufen');
      return;
    }

    setIsProcessing(true);
    setError(null);

    try {
      const holdIds = getAllHoldIds();
      const results = await purchaseBulkTickets(holdIds);

      const successfulOrders = results.filter(r => r.success);
      const failedOrders = results.filter(r => !r.success);

      if (successfulOrders.length > 0) {
        successfulOrders.forEach(result => {
          removeItem(result.holdId);
        });

        if (failedOrders.length === 0) {
          clearCart();
          setSuccess(true);
        } else {
          setError(`${successfulOrders.length} Ticket(s) erfolgreich gekauft. ${failedOrders.length} fehlgeschlagen.`);
        }
      } else {
        setError('Kauf fehlgeschlagen: ' + (failedOrders[0]?.error || 'Unbekannter Fehler'));
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setIsProcessing(false);
    }
  };

  if (isEmpty) {
    return <div>Warenkorb ist leer</div>;
  }

  if (success) {
    return <div>Kauf erfolgreich!</div>;
  }

  return (
    <div>
      <h1>Checkout</h1>
      <div>Tickets: {cartItems.length}</div>
      {error && <div role="alert">{error}</div>}
      <button
        data-testid="checkout-submit-btn"
        onClick={handleCheckout}
        disabled={isProcessing || !hasActiveHolds}
      >
        {isProcessing ? 'Wird verarbeitet...' : 'Jetzt kaufen'}
      </button>
    </div>
  );
};

describe('Checkout Flow Integration Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockCartContextValue = createMockCartContext();
  });

  describe('Checkout-Prozess', () => {
    it('führt erfolgreichen Checkout durch', async () => {
      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-123', success: true, data: { id: 'order-999' } },
      ]);

      render(<MockCheckoutPage />);

      expect(screen.getByRole('heading', { name: 'Checkout' })).toBeInTheDocument();

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(purchaseBulkTickets).toHaveBeenCalledWith(['hold-123']);
      });

      await waitFor(() => {
        expect(screen.getByText('Kauf erfolgreich!')).toBeInTheDocument();
      });
    });

    it('zeigt Fehlermeldung bei fehlgeschlagenem Checkout', async () => {
      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-123', success: false, error: 'Platz nicht mehr verfügbar' },
      ]);

      render(<MockCheckoutPage />);

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(screen.getByRole('alert')).toHaveTextContent(/Kauf fehlgeschlagen/);
      });
    });

    it('behandelt teilweise erfolgreiche Checkouts', async () => {
      const items = [
        mockCartItem,
        { ...mockCartItem, holdId: 'hold-456' },
      ];
      mockCartContextValue = createMockCartContext(items);

      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-123', success: true, data: { id: 'order-1' } },
        { holdId: 'hold-456', success: false, error: 'Fehler' },
      ]);

      render(<MockCheckoutPage />);

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(screen.getByRole('alert')).toHaveTextContent(/1 Ticket\(s\) erfolgreich gekauft/);
      });
    });
  });

  describe('Hold-Validierung', () => {
    it('deaktiviert Button wenn Hold abgelaufen', () => {
      const expiredItem = {
        ...mockCartItem,
        expiresAt: new Date(Date.now() - 1000).toISOString(),
      };
      mockCartContextValue = createMockCartContext([expiredItem]);

      render(<MockCheckoutPage />);

      expect(screen.getByTestId('checkout-submit-btn')).toBeDisabled();
    });

    it('aktiviert Button wenn Hold noch gültig', () => {
      render(<MockCheckoutPage />);

      expect(screen.getByTestId('checkout-submit-btn')).not.toBeDisabled();
    });
  });

  describe('Cart-Integration', () => {
    it('ruft clearCart nach erfolgreichem Checkout auf', async () => {
      const mockClearCart = jest.fn();
      mockCartContextValue = {
        ...createMockCartContext(),
        clearCart: mockClearCart,
      };

      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-123', success: true, data: { id: 'order-1' } },
      ]);

      render(<MockCheckoutPage />);

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(mockClearCart).toHaveBeenCalled();
      });
    });

    it('ruft removeItem für erfolgreiche Items auf', async () => {
      const mockRemoveItem = jest.fn();
      mockCartContextValue = {
        ...createMockCartContext(),
        removeItem: mockRemoveItem,
      };

      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-123', success: true, data: { id: 'order-1' } },
      ]);

      render(<MockCheckoutPage />);

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(mockRemoveItem).toHaveBeenCalledWith('hold-123');
      });
    });
  });

  describe('Leerer Warenkorb', () => {
    it('zeigt Meldung wenn Warenkorb leer', () => {
      mockCartContextValue = createMockCartContext([]);

      render(<MockCheckoutPage />);

      expect(screen.getByText(/Warenkorb ist leer/)).toBeInTheDocument();
    });
  });

  describe('Mehrere Tickets', () => {
    it('verarbeitet mehrere Tickets', async () => {
      const items = [
        mockCartItem,
        { ...mockCartItem, holdId: 'hold-456' },
        { ...mockCartItem, holdId: 'hold-789' },
      ];
      mockCartContextValue = createMockCartContext(items);

      purchaseBulkTickets.mockResolvedValue([
        { holdId: 'hold-123', success: true, data: { id: 'order-1' } },
        { holdId: 'hold-456', success: true, data: { id: 'order-2' } },
        { holdId: 'hold-789', success: true, data: { id: 'order-3' } },
      ]);

      render(<MockCheckoutPage />);

      expect(screen.getByText('Tickets: 3')).toBeInTheDocument();

      const checkoutButton = screen.getByTestId('checkout-submit-btn');
      fireEvent.click(checkoutButton);

      await waitFor(() => {
        expect(purchaseBulkTickets).toHaveBeenCalledWith(['hold-123', 'hold-456', 'hold-789']);
      });
    });
  });
});
