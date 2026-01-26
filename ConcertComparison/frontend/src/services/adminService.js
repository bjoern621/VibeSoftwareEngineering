import api from './api';

/**
 * Admin Service
 * Handles all admin-related API calls for managing concerts and seats
 */
const adminService = {
  /**
   * Create a new concert
   * @param {Object} concertData - Concert data
   * @param {string} concertData.name - Concert name
   * @param {string} concertData.date - Concert date (ISO format: yyyy-MM-dd'T'HH:mm:ss)
   * @param {string} concertData.venue - Concert venue
   * @param {string} concertData.description - Concert description
   * @returns {Promise<Object>} Created concert
   */
  createEvent: async (concertData) => {
    try {
      const response = await api.post('/concerts', concertData);
      return response.data;
    } catch (error) {
      console.error('Fehler beim Erstellen des Konzerts:', error);
      throw error;
    }
  },

  /**
   * Update an existing concert
   * @param {string} concertId - Concert ID
   * @param {Object} concertData - Updated concert data
   * @returns {Promise<Object>} Updated concert
   */
  updateEvent: async (concertId, concertData) => {
    try {
      const response = await api.put(`/concerts/${concertId}`, concertData);
      return response.data;
    } catch (error) {
      console.error(`Fehler beim Aktualisieren des Konzerts ${concertId}:`, error);
      throw error;
    }
  },

  /**
   * Delete a concert
   * @param {string} concertId - Concert ID
   * @returns {Promise<void>}
   */
  deleteEvent: async (concertId) => {
    try {
      await api.delete(`/concerts/${concertId}`);
    } catch (error) {
      console.error(`Fehler beim Löschen des Konzerts ${concertId}:`, error);
      throw error;
    }
  },

  /**
   * Replace ALL seats for a concert (delete existing + create new)
   * @param {string} concertId - Concert ID
   * @param {Array<Object>} seats - Array of seat objects
   * @param {string} seats[].row - Row identifier (e.g., "A", "B")
   * @param {number} seats[].number - Seat number
   * @param {string} seats[].category - Seat category (e.g., "VIP", "STANDARD")
   * @param {number} seats[].price - Seat price
   * @returns {Promise<Object>} Response message
   */
  createSeats: async (concertId, seats) => {
    try {
      // Transformiere Frontend-Format in Backend-Format
      // Backend erwartet: seatNumber, category, block, row, number (String), price
      const transformedSeats = seats.map((seat) => ({
        seatNumber: `${seat.row}-${seat.number}`,
        category: seat.category,
        block: `Block ${seat.category}`, // Block basierend auf Kategorie
        row: seat.row,
        number: String(seat.number), // Backend erwartet String
        price: seat.price,
      }));
      
      // PUT statt POST - ersetzt ALLE vorhandenen Sitze
      const response = await api.put(`/concerts/${concertId}/seats`, { seats: transformedSeats });
      return response.data;
    } catch (error) {
      console.error(`Fehler beim Aktualisieren der Sitze für Konzert ${concertId}:`, error);
      throw error;
    }
  },

  /**
   * Get all seats for a concert (admin view with all statuses)
   * @param {string} concertId - Concert ID
   * @returns {Promise<Array>} Array of seats
   */
  getEventSeats: async (concertId) => {
    try {
      const response = await api.get(`/concerts/${concertId}/seats`);
      return response.data;
    } catch (error) {
      console.error(`Fehler beim Abrufen der Sitze für Konzert ${concertId}:`, error);
      throw error;
    }
  },

  /**
   * Delete a seat
   * @param {string} seatId - Seat ID
   * @returns {Promise<void>}
   */
  deleteSeat: async (seatId) => {
    try {
      await api.delete(`/seats/${seatId}`);
    } catch (error) {
      console.error(`Fehler beim Löschen des Sitzes ${seatId}:`, error);
      throw error;
    }
  },

  /**
   * Get admin dashboard statistics
   * @returns {Promise<Object>} Dashboard statistics
   */
  getDashboardStats: async () => {
    try {
      const response = await api.get('/admin/stats');
      return response.data;
    } catch (error) {
      console.error('Fehler beim Abrufen der Dashboard-Statistiken:', error);
      throw error;
    }
  },

  /**
   * Get all concerts for admin management
   * @param {Object} params - Query parameters
   * @returns {Promise<Object>} Concerts with pagination
   */
  getAllEvents: async (params = {}) => {
    try {
      const response = await api.get('/concerts', {
        params: {
          page: params.page || 0,
          size: params.size || 20,
          sortBy: params.sortBy || 'date',
          sortOrder: params.sortOrder || 'desc',
        },
      });
      return response.data;
    } catch (error) {
      console.error('Fehler beim Abrufen der Konzerte:', error);
      throw error;
    }
  }
};

export default adminService;
