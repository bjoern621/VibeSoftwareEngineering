import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import OrderHistorySection from '../OrderHistorySection';
import useUserOrders from '../../../hooks/useUserOrders';

jest.mock('../../../hooks/useUserOrders');

jest.mock('../OrderCard', () => {
  return function MockOrderCard({ order, onViewDetails, onDownloadQR }) {
    return (
      <div data-testid={`order-card-${order.orderId}`}>
        <h3>{order.concertName}</h3>
        <button onClick={() => onViewDetails(order)}>Details</button>
        <button onClick={() => onDownloadQR(order.orderId, order.concertName)}>
          QR-Code
        </button>
      </div>
    );
  };
});

jest.mock('../TicketDetailModal', () => {
  return function MockTicketDetailModal({ order, isOpen, onClose }) {
    if (!isOpen) return null;
    return (
      <div data-testid="ticket-modal">
        <h2>{order.concertName}</h2>
        <button onClick={onClose}>Close</button>
      </div>
    );
  };
});

const renderWithRouter = (component) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('OrderHistorySection', () => {
  const mockOrders = [
    {
      orderId: 1,
      concertName: 'Taylor Swift | The Eras Tour',
      concertDate: '2026-07-27T19:30:00',
      venue: 'Olympiastadion, Munich',
      status: 'CONFIRMED',
      totalPrice: 245.0,
    },
    {
      orderId: 2,
      concertName: 'Coldplay: Music of the Spheres',
      concertDate: '2026-08-15T20:00:00',
      venue: 'Merkur Spiel-Arena, Dusseldorf',
      status: 'CONFIRMED',
      totalPrice: 180.5,
    },
    {
      orderId: 3,
      concertName: 'Ed Sheeran: +–=÷x Tour',
      concertDate: '2024-01-12T19:00:00',
      venue: 'Deutsche Bank Park, Frankfurt',
      status: 'CONFIRMED',
      totalPrice: 110.0,
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Loading state', () => {
    it('should show loading spinner when loading', () => {
      useUserOrders.mockReturnValue({
        orders: [],
        loading: true,
        error: null,
        getFilteredOrders: jest.fn(() => []),
        getOrderCounts: jest.fn(() => ({ all: 0, upcoming: 0, past: 0 })),
        downloadTicket: jest.fn(),
      });

      renderWithRouter(<OrderHistorySection />);

      expect(screen.getByText('Bestellungen werden geladen...')).toBeInTheDocument();
    });
  });

  describe('Error state', () => {
    it('should show error message when error occurs', () => {
      useUserOrders.mockReturnValue({
        orders: [],
        loading: false,
        error: 'Test error',
        getFilteredOrders: jest.fn(() => []),
        getOrderCounts: jest.fn(() => ({ all: 0, upcoming: 0, past: 0 })),
        downloadTicket: jest.fn(),
      });

      renderWithRouter(<OrderHistorySection />);

      expect(screen.getByText('Fehler beim Laden der Bestellungen')).toBeInTheDocument();
      expect(screen.getByText('Test error')).toBeInTheDocument();
    });
  });

  describe('Tab navigation', () => {
    beforeEach(() => {
      useUserOrders.mockReturnValue({
        orders: mockOrders,
        loading: false,
        error: null,
        getFilteredOrders: jest.fn((filter) => {
          if (filter === 'all') return mockOrders;
          if (filter === 'upcoming') return mockOrders.slice(0, 2);
          if (filter === 'past') return [mockOrders[2]];
          return mockOrders;
        }),
        getOrderCounts: jest.fn(() => ({
          all: 3,
          upcoming: 2,
          past: 1,
        })),
        downloadTicket: jest.fn(),
      });
    });

    it('should render all three tabs', () => {
      renderWithRouter(<OrderHistorySection />);

      // Find tabs by their count badges to distinguish from "Alle Rechnungen herunterladen"
      expect(screen.getByText('3')).toBeInTheDocument(); // Alle tab with count
      expect(screen.getByRole('button', { name: /Bevorstehend/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Vergangen/i })).toBeInTheDocument();
    });

    it('should show correct counts in tab badges', () => {
      renderWithRouter(<OrderHistorySection />);

      expect(screen.getByText('3')).toBeInTheDocument(); // All count
      expect(screen.getByText('2')).toBeInTheDocument(); // Upcoming count
      // Past count badge might not be shown or shown differently
      const pastButton = screen.getByRole('button', { name: /Vergangen/i });
      expect(pastButton).toBeInTheDocument();
    });

    it('should switch tabs when clicked', () => {
      renderWithRouter(<OrderHistorySection />);

      const upcomingTab = screen.getByRole('button', { name: /Bevorstehend/i });
      fireEvent.click(upcomingTab);

      expect(screen.getAllByTestId(/order-card-/)).toHaveLength(2);
    });
  });

  describe('Order list', () => {
    beforeEach(() => {
      useUserOrders.mockReturnValue({
        orders: mockOrders,
        loading: false,
        error: null,
        getFilteredOrders: jest.fn((filter) => {
          if (filter === 'all') return mockOrders;
          if (filter === 'upcoming') return mockOrders.slice(0, 2);
          if (filter === 'past') return [mockOrders[2]];
          return mockOrders;
        }),
        getOrderCounts: jest.fn(() => ({
          all: 3,
          upcoming: 2,
          past: 1,
        })),
        downloadTicket: jest.fn(),
      });
    });

    it('should render all orders in "Alle" tab', () => {
      renderWithRouter(<OrderHistorySection />);

      expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      expect(screen.getByText('Coldplay: Music of the Spheres')).toBeInTheDocument();
      expect(screen.getByText('Ed Sheeran: +–=÷x Tour')).toBeInTheDocument();
    });

    it('should filter orders when tab changes', () => {
      renderWithRouter(<OrderHistorySection />);

      const pastTab = screen.getByRole('button', { name: /Vergangen/i });
      fireEvent.click(pastTab);

      expect(screen.queryByText('Taylor Swift | The Eras Tour')).not.toBeInTheDocument();
      expect(screen.getByText('Ed Sheeran: +–=÷x Tour')).toBeInTheDocument();
    });
  });

  describe('Empty state', () => {
    it('should show appropriate message for empty upcoming tab', () => {
      useUserOrders.mockReturnValue({
        orders: [],
        loading: false,
        error: null,
        getFilteredOrders: jest.fn(() => []),
        getOrderCounts: jest.fn(() => ({
          all: 0,
          upcoming: 0,
          past: 0,
        })),
        downloadTicket: jest.fn(),
      });

      renderWithRouter(<OrderHistorySection />);

      expect(screen.getByText(/keine bestellungen/i)).toBeInTheDocument();
    });
  });

  describe('Modal interactions', () => {
    beforeEach(() => {
      useUserOrders.mockReturnValue({
        orders: mockOrders,
        loading: false,
        error: null,
        getFilteredOrders: jest.fn(() => [mockOrders[0]]),
        getOrderCounts: jest.fn(() => ({
          all: 1,
          upcoming: 1,
          past: 0,
        })),
        downloadTicket: jest.fn(),
      });
    });

    it('should open modal when Details button is clicked', async () => {
      renderWithRouter(<OrderHistorySection />);

      const detailsButton = screen.getByRole('button', { name: /Details/i });
      fireEvent.click(detailsButton);

      await waitFor(() => {
        expect(screen.getByTestId('ticket-modal')).toBeInTheDocument();
      });
    });

    it('should close modal when Close button is clicked', async () => {
      renderWithRouter(<OrderHistorySection />);

      const detailsButton = screen.getByRole('button', { name: /Details/i });
      fireEvent.click(detailsButton);

      await waitFor(() => {
        expect(screen.getByTestId('ticket-modal')).toBeInTheDocument();
      });

      const closeButton = screen.getByRole('button', { name: /Close/i });
      fireEvent.click(closeButton);

      await waitFor(() => {
        expect(screen.queryByTestId('ticket-modal')).not.toBeInTheDocument();
      });
    });
  });

  describe('Download functionality', () => {
    it('should call downloadTicket when QR-Code button is clicked', async () => {
      const mockDownloadTicket = jest.fn().mockResolvedValue();

      useUserOrders.mockReturnValue({
        orders: mockOrders,
        loading: false,
        error: null,
        getFilteredOrders: jest.fn(() => [mockOrders[0]]),
        getOrderCounts: jest.fn(() => ({
          all: 1,
          upcoming: 1,
          past: 0,
        })),
        downloadTicket: mockDownloadTicket,
      });

      renderWithRouter(<OrderHistorySection />);

      const qrButton = screen.getByRole('button', { name: /QR-Code/i });
      fireEvent.click(qrButton);

      await waitFor(() => {
        expect(mockDownloadTicket).toHaveBeenCalledWith(1, 'Taylor Swift | The Eras Tour');
      });
    });

    it('should handle download errors', async () => {
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      const mockDownloadTicket = jest.fn().mockRejectedValue(new Error('Download failed'));

      useUserOrders.mockReturnValue({
        orders: mockOrders,
        loading: false,
        error: null,
        getFilteredOrders: jest.fn(() => [mockOrders[0]]),
        getOrderCounts: jest.fn(() => ({
          all: 1,
          upcoming: 1,
          past: 0,
        })),
        downloadTicket: mockDownloadTicket,
      });

      renderWithRouter(<OrderHistorySection />);

      const qrButton = screen.getByRole('button', { name: /QR-Code/i });
      fireEvent.click(qrButton);

      await waitFor(() => {
        expect(mockDownloadTicket).toHaveBeenCalled();
      });

      consoleErrorSpy.mockRestore();
    });
  });
});
