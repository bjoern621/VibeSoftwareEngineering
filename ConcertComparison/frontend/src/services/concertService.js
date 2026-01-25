import api from './api';

/**
 * Fetch concerts with optional filters, sorting, and pagination
 * @param {Object} params - Query parameters
 * @param {string} params.date - Filter by date (YYYY-MM-DD)
 * @param {string} params.venue - Filter by venue (partial match)
 * @param {number} params.minPrice - Minimum price filter
 * @param {number} params.maxPrice - Maximum price filter
 * @param {string} params.sortBy - Sort field (date|name|price)
 * @param {string} params.sortOrder - Sort order (asc|desc)
 * @param {number} params.page - Page number (0-indexed)
 * @param {number} params.size - Page size
 * @returns {Promise} - Response with concerts array and pagination info
 */
export const fetchConcerts = async (params = {}) => {
  try {
    const response = await api.get('/concerts', {
      params: {
        date: params.date || undefined,
        venue: params.venue || undefined,
        minPrice: params.minPrice || undefined,
        maxPrice: params.maxPrice || undefined,
        sortBy: params.sortBy || 'date',
        sortOrder: params.sortOrder || 'asc',
        page: params.page || 0,
        size: params.size || 20,
      },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching concerts:', error);
    throw error;
  }
};

/**
 * Fetch a single concert by ID
 * @param {string} id - Concert ID
 * @returns {Promise} - Concert details
 */
export const fetchConcertById = async (id) => {
  try {
    const response = await api.get(`/concerts/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching concert ${id}:`, error);
    throw error;
  }
};

/**
 * Search concerts by name
 * @param {string} query - Search query
 * @returns {Promise} - Array of matching concerts
 */
export const searchConcerts = async (query) => {
  try {
    const response = await api.get('/concerts/search', {
      params: { name: query },
    });
    return response.data;
  } catch (error) {
    console.error('Error searching concerts:', error);
    throw error;
  }
};
