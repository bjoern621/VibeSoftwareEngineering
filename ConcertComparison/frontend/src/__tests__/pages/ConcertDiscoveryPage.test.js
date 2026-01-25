import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ConcertDiscoveryPage from '../../pages/ConcertDiscoveryPage';
import * as concertService from '../../services/concertService';
import { AuthProvider } from '../../context/AuthContext';
import { CartProvider } from '../../context/CartContext';

// Mock the concert service
jest.mock('../../services/concertService');
jest.mock('../../services/authService');

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('ConcertDiscoveryPage Integration', () => {
  const mockConcerts = {
    concerts: [
      {
        id: '1',
        name: 'Metallica Live',
        date: '2025-06-15T20:00:00',
        venue: 'Mercedes-Benz Arena Berlin',
        minPrice: 89.5,
        maxPrice: 299.0,
        totalSeats: 5000,
        availableSeats: 750,
      },
      {
        id: '2',
        name: 'Rock Festival',
        date: '2025-07-20T18:00:00',
        venue: 'Olympic Stadium Munich',
        minPrice: 120.0,
        maxPrice: 350.0,
        totalSeats: 10000,
        availableSeats: 5000,
      },
    ],
    page: {
      page: 0,
      size: 20,
      totalElements: 2,
      totalPages: 1,
    },
  };

  beforeEach(() => {
    jest.clearAllMocks();
    concertService.fetchConcerts.mockResolvedValue(mockConcerts);
  });

  const renderPage = () => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          <CartProvider>
            <ConcertDiscoveryPage />
          </CartProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  };

  test('displays concerts after loading', async () => {
    renderPage();

    // Should show loading initially
    expect(screen.getByText('Lade Konzerte...')).toBeInTheDocument();

    // Wait for concerts to load
    await waitFor(() => {
      expect(screen.getByText('Metallica Live')).toBeInTheDocument();
      expect(screen.getByText('Rock Festival')).toBeInTheDocument();
    });

    // Should show count
    expect(screen.getByText('2 Konzerte gefunden')).toBeInTheDocument();
  });

  test('handles API error', async () => {
    concertService.fetchConcerts.mockRejectedValue(new Error('API Error'));

    renderPage();

    await waitFor(() => {
      // Use getByRole for the heading to be more specific
      expect(screen.getByRole('heading', { name: 'Fehler beim Laden' })).toBeInTheDocument();
    });
  });

  test('shows empty state when no concerts found', async () => {
    concertService.fetchConcerts.mockResolvedValue({
      concerts: [],
      page: { page: 0, size: 20, totalElements: 0, totalPages: 0 },
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Keine Konzerte gefunden')).toBeInTheDocument();
    });
  });

  test('filter interaction updates concerts', async () => {
    renderPage();

    // Wait for initial load
    await waitFor(() => {
      expect(screen.getByText('Metallica Live')).toBeInTheDocument();
    });

    // Click "This Week" filter
    const thisWeekButton = screen.getByText('Diese Woche');
    fireEvent.click(thisWeekButton);

    // Should call fetchConcerts again
    await waitFor(() => {
      expect(concertService.fetchConcerts).toHaveBeenCalledTimes(2);
    });
  });

  test('sort interaction updates concerts', async () => {
    renderPage();

    // Wait for initial load
    await waitFor(() => {
      expect(screen.getByText('Metallica Live')).toBeInTheDocument();
    });

    // Open sort dropdown
    const sortButton = screen.getByText('Datum').closest('button');
    fireEvent.click(sortButton);

    // Click price sort
    const priceSort = screen.getByText('Preis (aufsteigend)');
    fireEvent.click(priceSort);

    // Should call fetchConcerts with new sort
    await waitFor(() => {
      expect(concertService.fetchConcerts).toHaveBeenCalledWith(
        expect.objectContaining({
          sortBy: 'price',
          sortOrder: 'asc',
        })
      );
    });
  });

  test('search input triggers API call', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Metallica Live')).toBeInTheDocument();
    });

    // Type in search box
    const searchInput = screen.getByPlaceholderText(/Suche nach Konzerten/);
    fireEvent.change(searchInput, { target: { value: 'Berlin' } });

    // Should debounce and call API
    await waitFor(
      () => {
        expect(concertService.fetchConcerts).toHaveBeenCalledWith(
          expect.objectContaining({
            venue: 'Berlin',
          })
        );
      },
      { timeout: 1000 }
    );
  });

  test('clicking concert card navigates to detail page', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Metallica Live')).toBeInTheDocument();
    });

    const concertCard = screen.getByText('Metallica Live').closest('div');
    fireEvent.click(concertCard);

    expect(mockNavigate).toHaveBeenCalledWith('/concerts/1');
  });
});
