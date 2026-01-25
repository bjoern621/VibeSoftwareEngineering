import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ConcertCard from '../../components/concerts/ConcertCard';

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('ConcertCard Component', () => {
  const mockConcert = {
    id: '1',
    name: 'Metallica Live 2025',
    date: '2025-06-15T20:00:00',
    venue: 'Mercedes-Benz Arena Berlin',
    minPrice: 89.5,
    maxPrice: 299.0,
    totalSeats: 5000,
    availableSeats: 750,
    imageUrl: 'https://example.com/concert.jpg',
  };

  const renderComponent = (concert = mockConcert) => {
    return render(
      <BrowserRouter>
        <ConcertCard concert={concert} />
      </BrowserRouter>
    );
  };

  beforeEach(() => {
    mockNavigate.mockClear();
  });

  test('renders concert card with correct information', () => {
    renderComponent();

    expect(screen.getByText('Metallica Live 2025')).toBeInTheDocument();
    expect(screen.getByText('Mercedes-Benz Arena Berlin')).toBeInTheDocument();
    expect(screen.getByText(/89,50/)).toBeInTheDocument();
  });

  test('navigates to concert detail page on card click', () => {
    renderComponent();

    const card = screen.getByText('Metallica Live 2025').closest('div');
    fireEvent.click(card);

    expect(mockNavigate).toHaveBeenCalledWith('/concerts/1');
  });

  test('toggles like button on click', () => {
    renderComponent();

    const likeButton = screen.getByLabelText('Like concert');
    
    // Initial state - not liked
    expect(likeButton).toHaveClass('bg-white/80');

    // Click to like
    fireEvent.click(likeButton);
    expect(likeButton).toHaveClass('bg-red-500');

    // Click again to unlike
    fireEvent.click(likeButton);
    expect(likeButton).toHaveClass('bg-white/80');
  });

  test('shows sold out overlay when no seats available', () => {
    const soldOutConcert = {
      ...mockConcert,
      availableSeats: 0,
    };

    renderComponent(soldOutConcert);

    expect(screen.getByText('AUSVERKAUFT')).toBeInTheDocument();
    // 'Ausverkauft' appears in badge, status text, and button - verify at least one
    const ausverkauftElements = screen.getAllByText('Ausverkauft');
    expect(ausverkauftElements.length).toBeGreaterThanOrEqual(1);
  });

  test('disables buy button when sold out', () => {
    const soldOutConcert = {
      ...mockConcert,
      availableSeats: 0,
    };

    renderComponent(soldOutConcert);

    const buyButton = screen.getByRole('button', { name: /Ausverkauft/i });
    expect(buyButton).toBeDisabled();
  });

  test('renders availability badge correctly for limited seats', () => {
    const limitedConcert = {
      ...mockConcert,
      availableSeats: 50,
      totalSeats: 1000,
    };

    renderComponent(limitedConcert);

    // Should show "Begrenzte Verfügbarkeit" or "Nur noch wenige Tickets"
    const badge = screen.getByText(/Begrenzte|wenige/);
    expect(badge).toBeInTheDocument();
  });

  test('handles image error gracefully', () => {
    renderComponent();

    const img = screen.getByAltText('Metallica Live 2025');
    
    // Simulate image load error
    fireEvent.error(img);

    // Should fall back to placeholder
    expect(img.src).toContain('placeholder');
  });

  test('prevents navigation when clicking like button', () => {
    renderComponent();

    const likeButton = screen.getByLabelText('Like concert');
    fireEvent.click(likeButton);

    // Should not navigate
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  test('shows correct price format', () => {
    renderComponent();

    // German price format
    expect(screen.getByText(/89,50\s*€/)).toBeInTheDocument();
  });

  test('displays availability progress bar', () => {
    renderComponent();

    // Should show percentage sold
    expect(screen.getByText(/85% verkauft/)).toBeInTheDocument();
  });
});
