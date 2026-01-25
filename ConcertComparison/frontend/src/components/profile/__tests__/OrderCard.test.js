import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import OrderCard from '../OrderCard';

// Mock child components
jest.mock('../OrderStatusBadge', () => {
  return function MockOrderStatusBadge({ status }) {
    return <div data-testid="status-badge">{status}</div>;
  };
});

jest.mock('../../../utils/dateFormatter', () => ({
  formatDateTime: (date) => '27. Juli 2026',
  formatTime: (date) => '19:30',
}));

jest.mock('../../../utils/priceFormatter', () => ({
  formatPrice: (price) => `${price.toFixed(2).replace('.', ',')} €`,
}));

describe('OrderCard', () => {
  const mockOrder = {
    orderId: 1,
    concertName: 'Taylor Swift | The Eras Tour',
    concertDate: '2026-07-27T19:30:00',
    venue: 'Olympiastadion, Munich',
    status: 'CONFIRMED',
    totalPrice: 245.0,
  };

  const mockOnViewDetails = jest.fn();
  const mockOnDownloadQR = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Rendering', () => {
    it('should render order information correctly', () => {
      render(
        <OrderCard
          order={mockOrder}
          onViewDetails={mockOnViewDetails}
          onDownloadQR={mockOnDownloadQR}
        />
      );

      expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      expect(screen.getByText('27. Juli 2026 • 19:30 Uhr')).toBeInTheDocument();
      expect(screen.getByText('Olympiastadion, Munich')).toBeInTheDocument();
      expect(screen.getByText('#1')).toBeInTheDocument();
      expect(screen.getByText('245,00 €')).toBeInTheDocument();
    });

    it('should render status badge', () => {
      render(
        <OrderCard
          order={mockOrder}
          onViewDetails={mockOnViewDetails}
          onDownloadQR={mockOnDownloadQR}
        />
      );

      expect(screen.getByTestId('status-badge')).toBeInTheDocument();
      expect(screen.getByTestId('status-badge')).toHaveTextContent('CONFIRMED');
    });
  });

  describe('User interactions', () => {
    it('should call onViewDetails when Details button is clicked', () => {
      render(
        <OrderCard
          order={mockOrder}
          onViewDetails={mockOnViewDetails}
          onDownloadQR={mockOnDownloadQR}
        />
      );

      const detailsButton = screen.getByRole('button', { name: /Details/i });
      fireEvent.click(detailsButton);

      expect(mockOnViewDetails).toHaveBeenCalledWith(mockOrder);
    });

    it('should call onDownloadQR when QR-Code button is clicked', async () => {
      mockOnDownloadQR.mockResolvedValue();

      render(
        <OrderCard
          order={mockOrder}
          onViewDetails={mockOnViewDetails}
          onDownloadQR={mockOnDownloadQR}
        />
      );

      const qrButton = screen.getByRole('button', { name: /QR-Code/i });
      fireEvent.click(qrButton);

      await waitFor(() => {
        expect(mockOnDownloadQR).toHaveBeenCalledWith(1, 'Taylor Swift | The Eras Tour');
      });
    });

    it('should disable QR button while downloading', async () => {
      mockOnDownloadQR.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      render(
        <OrderCard
          order={mockOrder}
          onViewDetails={mockOnViewDetails}
          onDownloadQR={mockOnDownloadQR}
        />
      );

      const qrButton = screen.getByRole('button', { name: /QR-Code/i });
      fireEvent.click(qrButton);

      expect(qrButton).toBeDisabled();
    });
  });

  describe('Past concerts', () => {
    it('should not show QR button for past concerts', () => {
      const pastOrder = {
        ...mockOrder,
        concertDate: '2024-01-01T20:00:00',
      };

      render(
        <OrderCard
          order={pastOrder}
          onViewDetails={mockOnViewDetails}
          onDownloadQR={mockOnDownloadQR}
        />
      );

      const qrButton = screen.queryByRole('button', { name: /QR-Code/i });
      expect(qrButton).not.toBeInTheDocument();
    });
  });
});
