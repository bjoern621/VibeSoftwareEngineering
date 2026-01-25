import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import UserProfilePage from '../../pages/UserProfilePage';
import { useAuth } from '../../context/AuthContext';
import useUserOrders from '../../hooks/useUserOrders';
import orderService from '../../services/orderService';

jest.mock('../../context/AuthContext');
jest.mock('../../hooks/useUserOrders');
jest.mock('../../services/orderService');

const mockUser = {
  id: 1,
  email: 'test@example.com',
  firstName: 'Max',
  lastName: 'Mustermann',
};

const mockOrders = [
  {
    orderId: 1,
    concertName: 'Taylor Swift | The Eras Tour',
    concertDate: '2026-07-27T19:30:00',
    venue: 'Olympiastadion, Munich',
    status: 'CONFIRMED',
    totalPrice: 245.0,
    seatNumber: 'A-1-5',
    category: 'VIP',
  },
  {
    orderId: 2,
    concertName: 'Coldplay: Music of the Spheres',
    concertDate: '2024-01-01T20:00:00',
    venue: 'Merkur Spiel-Arena, Dusseldorf',
    status: 'CONFIRMED',
    totalPrice: 180.5,
    seatNumber: 'B-12-7',
    category: 'Premium',
  },
];

const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      {component}
    </BrowserRouter>
  );
};

describe('UserProfile Integration Tests', () => {
  const mockLogout = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    useAuth.mockReturnValue({
      user: mockUser,
      logout: mockLogout,
    });

    useUserOrders.mockReturnValue({
      orders: mockOrders,
      loading: false,
      error: null,
      getFilteredOrders: jest.fn((filter) => {
        if (filter === 'upcoming') return [mockOrders[0]];
        if (filter === 'past') return [mockOrders[1]];
        return mockOrders;
      }),
      getOrderCounts: jest.fn(() => ({
        all: 2,
        upcoming: 1,
        past: 1,
      })),
      downloadTicket: jest.fn().mockResolvedValue(),
    });

    orderService.getTicketQRCodeDataUrl.mockResolvedValue('data:image/png;base64,mockBase64');
    orderService.downloadTicketQR.mockResolvedValue();

    delete window.location;
    window.location = { href: '' };
  });

  describe('Full user flow', () => {
    it('should render profile page with user data', () => {
      renderWithProviders(<UserProfilePage />);

      expect(screen.getByText('Concert Comparison')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Meine Bestellungen/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Mein Profil/i })).toBeInTheDocument();
    });

    it('should navigate between tabs and preserve state', async () => {
      renderWithProviders(<UserProfilePage />);

      // Start on orders tab
      expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();

      // Switch to profile tab
      const profileTab = screen.getByRole('button', { name: /Mein Profil/i });
      fireEvent.click(profileTab);

      await waitFor(() => {
        expect(screen.getByText(mockUser.email)).toBeInTheDocument();
      });

      // Switch back to orders tab
      const ordersTab = screen.getByRole('button', { name: /Meine Bestellungen/i });
      fireEvent.click(ordersTab);

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });
    });

    it('should display order cards in order history', async () => {
      renderWithProviders(<UserProfilePage />);

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });
      expect(screen.getByText('Coldplay: Music of the Spheres')).toBeInTheDocument();
    });

    it('should filter orders by upcoming/past', async () => {
      renderWithProviders(<UserProfilePage />);

      await waitFor(() => {
        expect(screen.getByText('Alle')).toBeInTheDocument();
      });

      // Click upcoming tab
      const upcomingTab = screen.getByRole('button', { name: /Bevorstehend/i });
      fireEvent.click(upcomingTab);

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });
      expect(screen.queryByText('Coldplay: Music of the Spheres')).not.toBeInTheDocument();

      // Click past tab
      const pastTab = screen.getByRole('button', { name: /Vergangen/i });
      fireEvent.click(pastTab);

      await waitFor(() => {
        expect(screen.getByText('Coldplay: Music of the Spheres')).toBeInTheDocument();
      });
      expect(screen.queryByText('Taylor Swift | The Eras Tour')).not.toBeInTheDocument();
    });

    it('should open ticket modal when clicking details', async () => {
      renderWithProviders(<UserProfilePage />);

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });

      const detailsButtons = screen.getAllByRole('button', { name: /Details/i });
      fireEvent.click(detailsButtons[0]);

      await waitFor(() => {
        expect(orderService.getTicketQRCodeDataUrl).toHaveBeenCalledWith(1);
      });
    });

    it('should handle logout', () => {
      renderWithProviders(<UserProfilePage />);

      const logoutButtons = screen.getAllByRole('button', { name: /Abmelden/i });
      fireEvent.click(logoutButtons[0]); // Click first logout button (header)

      expect(mockLogout).toHaveBeenCalled();
      expect(window.location.href).toBe('/login');
    });
  });

  describe('Error handling', () => {
    it('should display error when orders fail to load', async () => {
      useUserOrders.mockReturnValue({
        orders: [],
        loading: false,
        error: 'Fehler beim Laden der Bestellungen',
        getFilteredOrders: jest.fn(() => []),
        getOrderCounts: jest.fn(() => ({ all: 0, upcoming: 0, past: 0 })),
        downloadTicket: jest.fn(),
      });

      renderWithProviders(<UserProfilePage />);

      await waitFor(() => {
        const errorMessages = screen.getAllByText('Fehler beim Laden der Bestellungen');
        expect(errorMessages.length).toBeGreaterThan(0);
      });
    });

    it('should display loading state', () => {
      useUserOrders.mockReturnValue({
        orders: [],
        loading: true,
        error: null,
        getFilteredOrders: jest.fn(() => []),
        getOrderCounts: jest.fn(() => ({ all: 0, upcoming: 0, past: 0 })),
        downloadTicket: jest.fn(),
      });

      renderWithProviders(<UserProfilePage />);

      expect(screen.getByText('Bestellungen werden geladen...')).toBeInTheDocument();
    });

    it('should show empty state when no orders', async () => {
      useUserOrders.mockReturnValue({
        orders: [],
        loading: false,
        error: null,
        getFilteredOrders: jest.fn(() => []),
        getOrderCounts: jest.fn(() => ({ all: 0, upcoming: 0, past: 0 })),
        downloadTicket: jest.fn(),
      });

      renderWithProviders(<UserProfilePage />);

      await waitFor(() => {
        expect(screen.getByText(/keine bestellungen/i)).toBeInTheDocument();
      });
    });
  });
});
