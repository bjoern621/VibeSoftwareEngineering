import { useState, useEffect, useCallback } from 'react';
import { fetchConcerts } from '../services/concertService';
import { isThisWeek, isWeekend } from '../utils/dateFormatter';
import { SORT_OPTIONS } from '../constants/concertFilters';

/**
 * Custom hook for managing concert data, filtering, sorting, and pagination
 */
export const useConcerts = () => {
  const [concerts, setConcerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filter state
  const [activeFilter, setActiveFilter] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [activeSort, setActiveSort] = useState('date');

  /**
   * Fetch concerts with current filters
   */
  const loadConcerts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // Get sort configuration
      const sortOption = SORT_OPTIONS.find((opt) => opt.id === activeSort) || SORT_OPTIONS[0];

      const params = {
        page,
        size: 20,
        sortBy: sortOption.field,
        sortOrder: sortOption.order,
      };

      // Add venue search if query exists
      if (searchQuery) {
        params.venue = searchQuery;
      }

      const response = await fetchConcerts(params);

      // Apply client-side filtering for special filters
      let filteredConcerts = response.concerts || [];

      if (activeFilter === 'thisWeek') {
        filteredConcerts = filteredConcerts.filter((concert) => isThisWeek(concert.date));
      } else if (activeFilter === 'weekend') {
        filteredConcerts = filteredConcerts.filter((concert) => isWeekend(concert.date));
      } else if (activeFilter === 'popular') {
        // Sort by popularity (low availability percentage = high demand)
        filteredConcerts = filteredConcerts.sort((a, b) => {
          const availabilityA = a.availableSeats / a.totalSeats;
          const availabilityB = b.availableSeats / b.totalSeats;
          return availabilityA - availabilityB;
        });
      }

      setConcerts(filteredConcerts);
      setTotalPages(response.page?.totalPages || 0);
      setTotalElements(response.page?.totalElements || 0);
    } catch (err) {
      console.error('Error loading concerts:', err);
      setError(err.response?.data?.message || 'Fehler beim Laden der Konzerte');
    } finally {
      setLoading(false);
    }
  }, [page, activeFilter, searchQuery, activeSort]);

  /**
   * Reload concerts when filters change
   */
  useEffect(() => {
    loadConcerts();
  }, [loadConcerts]);

  /**
   * Reset page when filters change
   */
  useEffect(() => {
    setPage(0);
  }, [activeFilter, searchQuery, activeSort]);

  /**
   * Handle filter change
   */
  const handleFilterChange = (filterId) => {
    setActiveFilter(filterId);
  };

  /**
   * Handle sort change
   */
  const handleSortChange = (sortId) => {
    setActiveSort(sortId);
  };

  /**
   * Handle search query change
   */
  const handleSearchChange = (query) => {
    setSearchQuery(query);
  };

  /**
   * Handle page change
   */
  const handlePageChange = (newPage) => {
    setPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  /**
   * Refresh concerts
   */
  const refresh = () => {
    loadConcerts();
  };

  return {
    concerts,
    loading,
    error,
    page,
    totalPages,
    totalElements,
    activeFilter,
    searchQuery,
    activeSort,
    handleFilterChange,
    handleSortChange,
    handleSearchChange,
    handlePageChange,
    refresh,
  };
};
