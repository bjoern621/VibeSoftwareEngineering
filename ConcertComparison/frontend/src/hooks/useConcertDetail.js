import { useState, useEffect, useCallback } from 'react';
import { fetchConcertById } from '../services/concertService';
import { fetchConcertSeats } from '../services/seatService';

/**
 * Custom hook for fetching and managing concert detail data
 * @param {string} concertId - The concert ID to fetch
 * @returns {Object} - Concert data, seats, loading state, error, and refresh function
 */
export const useConcertDetail = (concertId) => {
  const [concert, setConcert] = useState(null);
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedSeat, setSelectedSeat] = useState(null);

  /**
   * Load concert details and seats
   */
  const loadConcertDetail = useCallback(async () => {
    if (!concertId) {
      setError('Keine Konzert-ID angegeben');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // Fetch concert details and seats in parallel
      const [concertData, seatsData] = await Promise.all([
        fetchConcertById(concertId),
        fetchConcertSeats(concertId),
      ]);

      setConcert(concertData);
      setSeats(seatsData || []);
    } catch (err) {
      console.error('Error loading concert detail:', err);
      
      // Handle specific error cases
      if (err.response?.status === 404) {
        setError('Konzert nicht gefunden');
      } else {
        setError(err.response?.data?.message || 'Fehler beim Laden der Konzertdetails');
      }
    } finally {
      setLoading(false);
    }
  }, [concertId]);

  /**
   * Load data on mount and when concertId changes
   */
  useEffect(() => {
    loadConcertDetail();
  }, [loadConcertDetail]);

  /**
   * Handle seat selection
   * @param {Object} seat - The selected seat object
   */
  const handleSeatSelect = (seat) => {
    if (seat.status === 'AVAILABLE') {
      setSelectedSeat(seat);
    }
  };

  /**
   * Clear seat selection
   */
  const clearSeatSelection = () => {
    setSelectedSeat(null);
  };

  /**
   * Update a single seat's status (useful after hold/purchase)
   * @param {string} seatId - Seat ID to update
   * @param {string} newStatus - New status (AVAILABLE, HELD, SOLD)
   */
  const updateSeatStatus = (seatId, newStatus) => {
    setSeats((prevSeats) =>
      prevSeats.map((seat) =>
        seat.id === seatId ? { ...seat, status: newStatus } : seat
      )
    );
  };

  /**
   * Refresh concert data
   */
  const refresh = () => {
    loadConcertDetail();
  };

  /**
   * Group seats by block for organized display
   */
  const seatsByBlock = seats.reduce((acc, seat) => {
    const block = seat.block || 'Allgemein';
    if (!acc[block]) {
      acc[block] = [];
    }
    acc[block].push(seat);
    return acc;
  }, {});

  /**
   * Get availability statistics
   */
  const availability = {
    total: seats.length,
    available: seats.filter((s) => s.status === 'AVAILABLE').length,
    held: seats.filter((s) => s.status === 'HELD').length,
    sold: seats.filter((s) => s.status === 'SOLD').length,
  };

  return {
    concert,
    seats,
    seatsByBlock,
    availability,
    loading,
    error,
    selectedSeat,
    handleSeatSelect,
    clearSeatSelection,
    updateSeatStatus,
    refresh,
  };
};
