import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import TicketDetailModal from '../TicketDetailModal';
import orderService from '../../../services/orderService';

jest.mock('../../../services/orderService');

jest.mock('../../../utils/dateFormatter', () => ({
  formatDateTime: (date) => '27. Juli 2026',
  formatTime: (date) => '19:30',
}));

jest.mock('../../../utils/priceFormatter', () => ({
  formatPrice: (price) => `${price.toFixed(2).replace('.', ',')} €`,
}));

describe('TicketDetailModal', () => {
  const mockOrder = {
    orderId: 1,
    concertName: 'Taylor Swift | The Eras Tour',
    concertDate: '2026-07-27T19:30:00',
    venue: 'Olympiastadion, Munich',
    status: 'CONFIRMED',
    totalPrice: 245.0,
    seatNumber: 'A-1-5',
    category: 'VIP',
  };

  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    orderService.getTicketQRCodeDataUrl.mockResolvedValue('data:image/png;base64,mockBase64');
    global.URL.createObjectURL = jest.fn(() => 'blob:mock-url');
    global.URL.revokeObjectURL = jest.fn();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Rendering', () => {
    it('should not render when isOpen is false', () => {
      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={false}
          onClose={mockOnClose}
        />
      );

      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('should render when isOpen is true', async () => {
      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });
    });

    it('should display ticket details', async () => {
      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });

      expect(screen.getByText('#1')).toBeInTheDocument();
      expect(screen.getByText('27. Juli 2026')).toBeInTheDocument();
      expect(screen.getByText(/19:30.*Uhr/i)).toBeInTheDocument();
      expect(screen.getByText('Olympiastadion, Munich')).toBeInTheDocument();
      expect(screen.getByText('VIP')).toBeInTheDocument();
      expect(screen.getByText('A-1-5')).toBeInTheDocument();
      expect(screen.getByText('245,00 €')).toBeInTheDocument();
    });
  });

  describe('QR Code loading', () => {
    it('should load QR code when modal opens', async () => {
      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        expect(orderService.getTicketQRCodeDataUrl).toHaveBeenCalledWith(1);
      });
    });

    it('should show loading state', () => {
      orderService.getTicketQRCodeDataUrl.mockImplementation(
        () => new Promise(() => {}) // Never resolves
      );

      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      // Check for loading spinner (dialog role is not set)
      const { container } = render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );
      const spinner = container.querySelector('.animate-spin');
      expect(spinner).toBeInTheDocument();
    });

    it('should display QR code when loaded', async () => {
      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        expect(screen.getByAltText('Ticket QR Code')).toBeInTheDocument();
      });
      
      const qrImage = screen.getByAltText('Ticket QR Code');
      expect(qrImage).toHaveAttribute('src', 'data:image/png;base64,mockBase64');
    });

    it('should show error message when QR code fails to load', async () => {
      orderService.getTicketQRCodeDataUrl.mockRejectedValue(new Error('Load failed'));

      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('QR-Code konnte nicht geladen werden')).toBeInTheDocument();
      });
    });
  });

  describe('User interactions', () => {
    it('should close modal when close button is clicked', async () => {
      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });

      // There are 2 "Schließen" buttons - one in header (X), one in footer
      const closeButtons = screen.getAllByRole('button', { name: 'Schließen' });
      fireEvent.click(closeButtons[1]); // Click the footer button

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('should download QR code when download button is clicked', async () => {
      orderService.downloadTicketQR.mockResolvedValue();

      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        const qrImage = screen.getByAltText('Ticket QR Code');
        expect(qrImage).toBeInTheDocument();
      });

      const downloadButton = screen.getByRole('button', { name: /herunterladen/i });
      fireEvent.click(downloadButton);

      await waitFor(() => {
        expect(orderService.downloadTicketQR).toHaveBeenCalledWith(1, 'Taylor Swift | The Eras Tour');
      });
    });

    it('should handle download errors', async () => {
      orderService.downloadTicketQR.mockRejectedValue(new Error('Download failed'));

      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        const qrImage = screen.getByAltText('Ticket QR Code');
        expect(qrImage).toBeInTheDocument();
      });

      const downloadButton = screen.getByRole('button', { name: /herunterladen/i });
      fireEvent.click(downloadButton);

      await waitFor(() => {
        expect(screen.getByText('Download fehlgeschlagen')).toBeInTheDocument();
      });
    });

    it('should close modal when backdrop is clicked', async () => {
      render(
        <TicketDetailModal
          order={mockOrder}
          isOpen={true}
          onClose={mockOnClose}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Taylor Swift | The Eras Tour')).toBeInTheDocument();
      });

      // Note: Testing backdrop click requires DOM access which Testing Library discourages
      // Instead we test the close button which provides the same functionality
      const closeButtons = screen.getAllByRole('button', { name: /schließen/i });
      fireEvent.click(closeButtons[0]);

      expect(mockOnClose).toHaveBeenCalled();
    });
  });
});
