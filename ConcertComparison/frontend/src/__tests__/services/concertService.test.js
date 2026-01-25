import axios from 'axios';
import { fetchConcerts, fetchConcertById, searchConcerts } from '../../services/concertService';

// Mock axios
jest.mock('../../services/api');
const api = require('../../services/api').default;

describe('Concert Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('fetchConcerts', () => {
    test('fetches concerts with default parameters', async () => {
      const mockResponse = {
        data: {
          concerts: [
            { id: '1', name: 'Concert 1' },
            { id: '2', name: 'Concert 2' },
          ],
          page: {
            page: 0,
            size: 20,
            totalElements: 2,
            totalPages: 1,
          },
        },
      };

      api.get.mockResolvedValue(mockResponse);

      const result = await fetchConcerts();

      expect(api.get).toHaveBeenCalledWith('/concerts', {
        params: {
          date: undefined,
          venue: undefined,
          minPrice: undefined,
          maxPrice: undefined,
          sortBy: 'date',
          sortOrder: 'asc',
          page: 0,
          size: 20,
        },
      });

      expect(result).toEqual(mockResponse.data);
    });

    test('fetches concerts with custom filters', async () => {
      const mockResponse = {
        data: {
          concerts: [],
          page: { page: 0, size: 20, totalElements: 0, totalPages: 0 },
        },
      };

      api.get.mockResolvedValue(mockResponse);

      await fetchConcerts({
        venue: 'Berlin',
        minPrice: 50,
        maxPrice: 150,
        sortBy: 'price',
        sortOrder: 'desc',
        page: 1,
        size: 10,
      });

      expect(api.get).toHaveBeenCalledWith('/concerts', {
        params: {
          date: undefined,
          venue: 'Berlin',
          minPrice: 50,
          maxPrice: 150,
          sortBy: 'price',
          sortOrder: 'desc',
          page: 1,
          size: 10,
        },
      });
    });

    test('handles fetch error', async () => {
      const mockError = new Error('Network error');
      api.get.mockRejectedValue(mockError);

      await expect(fetchConcerts()).rejects.toThrow('Network error');
    });
  });

  describe('fetchConcertById', () => {
    test('fetches single concert by ID', async () => {
      const mockConcert = {
        data: {
          id: '1',
          name: 'Metallica Live',
          date: '2025-06-15T20:00:00',
        },
      };

      api.get.mockResolvedValue(mockConcert);

      const result = await fetchConcertById('1');

      expect(api.get).toHaveBeenCalledWith('/concerts/1');
      expect(result).toEqual(mockConcert.data);
    });

    test('handles 404 error', async () => {
      const mockError = {
        response: {
          status: 404,
          data: { message: 'Concert not found' },
        },
      };

      api.get.mockRejectedValue(mockError);

      await expect(fetchConcertById('999')).rejects.toEqual(mockError);
    });
  });

  describe('searchConcerts', () => {
    test('searches concerts by name', async () => {
      const mockResults = {
        data: [
          { id: '1', name: 'Metallica' },
          { id: '2', name: 'Metalcore Festival' },
        ],
      };

      api.get.mockResolvedValue(mockResults);

      const result = await searchConcerts('Metal');

      expect(api.get).toHaveBeenCalledWith('/concerts/search', {
        params: { name: 'Metal' },
      });

      expect(result).toEqual(mockResults.data);
    });

    test('handles empty search results', async () => {
      api.get.mockResolvedValue({ data: [] });

      const result = await searchConcerts('Nonexistent');

      expect(result).toEqual([]);
    });
  });
});
