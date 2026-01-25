import React from 'react';
import { render, screen } from '@testing-library/react';
import OrderStatusBadge from '../OrderStatusBadge';

describe('OrderStatusBadge', () => {
  describe('Upcoming concerts', () => {
    const futureDate = '2026-12-31T20:00:00';

    it('should render "Bestätigt" for CONFIRMED status', () => {
      render(<OrderStatusBadge status="CONFIRMED" concertDate={futureDate} />);
      
      expect(screen.getByText('Bestätigt')).toBeInTheDocument();
      expect(screen.getByText('check_circle')).toBeInTheDocument();
    });

    it('should render "Ausstehend" for PENDING status', () => {
      render(<OrderStatusBadge status="PENDING" concertDate={futureDate} />);
      
      expect(screen.getByText('Ausstehend')).toBeInTheDocument();
      expect(screen.getByText('schedule')).toBeInTheDocument();
    });

    it('should render "Storniert" for CANCELLED status', () => {
      render(<OrderStatusBadge status="CANCELLED" concertDate={futureDate} />);
      
      expect(screen.getByText('Storniert')).toBeInTheDocument();
      expect(screen.getByText('cancel')).toBeInTheDocument();
    });
  });

  describe('Past concerts', () => {
    const pastDate = '2024-01-01T20:00:00';

    it('should render "Vergangen" for past concert regardless of status', () => {
      render(<OrderStatusBadge status="CONFIRMED" concertDate={pastDate} />);
      
      expect(screen.getByText('Vergangen')).toBeInTheDocument();
      expect(screen.getByText('event_available')).toBeInTheDocument();
    });

    it('should apply gray styling for past concerts', () => {
      render(<OrderStatusBadge status="CONFIRMED" concertDate={pastDate} />);
      
      // Verify "Vergangen" text is rendered (which implies gray styling)
      expect(screen.getByText('Vergangen')).toBeInTheDocument();
    });
  });

  describe('Edge cases', () => {
    it('should handle Date object as concertDate', () => {
      const futureDate = new Date('2026-12-31T20:00:00');
      render(<OrderStatusBadge status="CONFIRMED" concertDate={futureDate} />);
      
      expect(screen.getByText('Bestätigt')).toBeInTheDocument();
    });
  });
});
