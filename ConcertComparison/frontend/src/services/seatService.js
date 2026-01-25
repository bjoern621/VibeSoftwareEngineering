import api from "./api";

/**
 * Fetch all seats for a specific concert
 * Backend endpoint: GET /api/events/{id}/seats
 * @param {string} concertId - Concert ID
 * @returns {Promise} - Array of seat objects with availability data
 */
export const fetchConcertSeats = async (concertId) => {
    try {
        // Backend endpoint is /api/events/{id}/seats (SeatController)
        const response = await api.get(`/events/${concertId}/seats`);
        // Return the seats array from the response
        return response.data.seats || response.data;
    } catch (error) {
        console.error(`Error fetching seats for concert ${concertId}:`, error);
        throw error;
    }
};

/**
 * Fetch aggregated seat availability for a concert
 * Backend endpoint: GET /api/events/{id}/seats (same as above, includes availability)
 * @param {string} concertId - Concert ID
 * @returns {Promise} - Availability summary by category/block
 */
export const fetchSeatAvailability = async (concertId) => {
    try {
        // Use the same endpoint - it returns availabilityByCategory
        const response = await api.get(`/events/${concertId}/seats`);
        return response.data.availabilityByCategory || response.data;
    } catch (error) {
        console.error(
            `Error fetching availability for concert ${concertId}:`,
            error,
        );
        throw error;
    }
};

/**
 * Create a hold (temporary reservation) for a seat
 * Backend endpoint: POST /api/seats/{id}/hold
 * @param {string} seatId - Seat ID to hold
 * @param {string} userId - User ID creating the hold
 * @returns {Promise} - Hold/Reservation details with expiration time
 */
export const createSeatHold = async (seatId, userId) => {
    try {
        const response = await api.post(`/seats/${seatId}/hold`, { userId });
        return response.data;
    } catch (error) {
        console.error(`Error creating hold for seat ${seatId}:`, error);
        throw error;
    }
};

/**
 * Cancel an existing seat hold
 * @param {string} reservationId - Reservation ID to cancel
 * @returns {Promise} - Cancellation confirmation
 */
export const cancelSeatHold = async (reservationId) => {
    try {
        const response = await api.delete(`/reservations/${reservationId}`);
        return response.data;
    } catch (error) {
        console.error(`Error canceling reservation ${reservationId}:`, error);
        throw error;
    }
};
