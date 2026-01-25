import { renderHook, waitFor, act } from '@testing-library/react';
import { useConcertDetail } from '../../hooks/useConcertDetail';
import * as concertService from '../../services/concertService';
import * as seatService from '../../services/seatService';

// Mock services
jest.mock('../../services/concertService');
jest.mock('../../services/seatService');

describe('useConcertDetail Hook', () => {
  const mockConcert = {
    id: '1',
    name: 'Metallica Live 2025',
    date: '2025-06-15T20:00:00',
    venue: 'Mercedes-Benz Arena Berlin',
    minPrice: 89.5,
    maxPrice: 299.0,
    totalSeats: 100,
    availableSeats: 75,
  };

  const mockSeats = [
    { id: 's1', block: 'A', category: 'VIP', row: '1', number: '1', price: 299.0, status: 'AVAILABLE' },
    { id: 's2', block: 'A', category: 'VIP', row: '1', number: '2', price: 299.0, status: 'HELD' },
    { id: 's3', block: 'B', category: 'Standard', row: '1', number: '1', price: 89.5, status: 'SOLD' },
    { id: 's4', block: 'B', category: 'Standard', row: '1', number: '2', price: 89.5, status: 'AVAILABLE' },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    concertService.fetchConcertById.mockResolvedValue(mockConcert);
    seatService.fetchConcertSeats.mockResolvedValue(mockSeats);
  });

  describe('Initial State', () => {
    test('starts with loading state', () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      expect(result.current.loading).toBe(true);
      expect(result.current.concert).toBe(null);
      expect(result.current.seats).toEqual([]);
    });
  });

  describe('Data Fetching', () => {
    test('fetches concert and seats on mount', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(concertService.fetchConcertById).toHaveBeenCalledWith('1');
      expect(seatService.fetchConcertSeats).toHaveBeenCalledWith('1');
    });

    test('sets concert data after successful fetch', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.concert).toEqual(mockConcert);
      });
    });

    test('sets seats data after successful fetch', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.seats).toEqual(mockSeats);
      });
    });

    test('fetches in parallel for performance', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      // Both should be called immediately (parallel)
      expect(concertService.fetchConcertById).toHaveBeenCalledTimes(1);
      expect(seatService.fetchConcertSeats).toHaveBeenCalledTimes(1);
    });
  });

  describe('Error Handling', () => {
    test('sets error when concert fetch fails', async () => {
      concertService.fetchConcertById.mockRejectedValue({
        response: { status: 500, data: { message: 'Server Error' } },
      });

      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.error).toBe('Server Error');
        expect(result.current.loading).toBe(false);
      });
    });

    test('sets "Konzert nicht gefunden" for 404 error', async () => {
      concertService.fetchConcertById.mockRejectedValue({
        response: { status: 404 },
      });

      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.error).toBe('Konzert nicht gefunden');
      });
    });

    test('sets error when no concertId provided', async () => {
      const { result } = renderHook(() => useConcertDetail(null));

      await waitFor(() => {
        expect(result.current.error).toBe('Keine Konzert-ID angegeben');
        expect(result.current.loading).toBe(false);
      });
    });

    test('handles seats fetch failure gracefully', async () => {
      seatService.fetchConcertSeats.mockRejectedValue(new Error('Seats error'));

      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.error).toBeTruthy();
      });
    });
  });

  describe('Seats by Block', () => {
    test('groups seats by block', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.seatsByBlock).toHaveProperty('A');
        expect(result.current.seatsByBlock).toHaveProperty('B');
      });
    });

    test('correctly assigns seats to their blocks', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.seatsByBlock['A']).toHaveLength(2);
        expect(result.current.seatsByBlock['B']).toHaveLength(2);
      });
    });
  });

  describe('Availability Calculation', () => {
    test('calculates total seats', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.availability.total).toBe(4);
      });
    });

    test('calculates available seats', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.availability.available).toBe(2);
      });
    });

    test('calculates held seats', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.availability.held).toBe(1);
      });
    });

    test('calculates sold seats', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.availability.sold).toBe(1);
      });
    });
  });

  describe('Seat Selection', () => {
    test('handleSeatSelect sets selected seat for available seats', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.handleSeatSelect(mockSeats[0]); // AVAILABLE seat
      });

      expect(result.current.selectedSeat).toEqual(mockSeats[0]);
    });

    test('handleSeatSelect does not select non-available seats', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.handleSeatSelect(mockSeats[1]); // HELD seat
      });

      expect(result.current.selectedSeat).toBe(null);
    });

    test('clearSeatSelection clears the selected seat', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.handleSeatSelect(mockSeats[0]);
      });

      expect(result.current.selectedSeat).toEqual(mockSeats[0]);

      act(() => {
        result.current.clearSeatSelection();
      });

      expect(result.current.selectedSeat).toBe(null);
    });
  });

  describe('Update Seat Status', () => {
    test('updateSeatStatus updates a specific seat', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.updateSeatStatus('s1', 'HELD');
      });

      const updatedSeat = result.current.seats.find((s) => s.id === 's1');
      expect(updatedSeat.status).toBe('HELD');
    });

    test('updateSeatStatus does not affect other seats', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.updateSeatStatus('s1', 'HELD');
      });

      const otherSeat = result.current.seats.find((s) => s.id === 's4');
      expect(otherSeat.status).toBe('AVAILABLE');
    });

    test('updateSeatStatus updates availability calculations', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const initialAvailable = result.current.availability.available;

      act(() => {
        result.current.updateSeatStatus('s1', 'HELD');
      });

      expect(result.current.availability.available).toBe(initialAvailable - 1);
      expect(result.current.availability.held).toBe(2);
    });
  });

  describe('Refresh', () => {
    test('refresh reloads concert data', async () => {
      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.refresh();
      });

      await waitFor(() => {
        expect(concertService.fetchConcertById).toHaveBeenCalledTimes(2);
        expect(seatService.fetchConcertSeats).toHaveBeenCalledTimes(2);
      });
    });

    test('refresh clears previous error', async () => {
      concertService.fetchConcertById
        .mockRejectedValueOnce({ response: { status: 500 } })
        .mockResolvedValueOnce(mockConcert);

      const { result } = renderHook(() => useConcertDetail('1'));

      await waitFor(() => {
        expect(result.current.error).toBeTruthy();
      });

      act(() => {
        result.current.refresh();
      });

      await waitFor(() => {
        expect(result.current.error).toBe(null);
        expect(result.current.concert).toEqual(mockConcert);
      });
    });
  });

  describe('ConcertId Change', () => {
    test('refetches data when concertId changes', async () => {
      const { result, rerender } = renderHook(
        ({ id }) => useConcertDetail(id),
        { initialProps: { id: '1' } }
      );

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      rerender({ id: '2' });

      await waitFor(() => {
        expect(concertService.fetchConcertById).toHaveBeenCalledWith('2');
        expect(seatService.fetchConcertSeats).toHaveBeenCalledWith('2');
      });
    });
  });
});
