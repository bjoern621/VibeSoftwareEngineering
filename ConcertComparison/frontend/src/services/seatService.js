import api from "./api";

// Base URL für SSE (ohne /api da EventSource nicht über axios läuft)
const SSE_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

/**
 * SSE Connection Status
 */
export const SSE_CONNECTION_STATUS = {
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  RECONNECTING: 'reconnecting',
  ERROR: 'error',
  CLOSED: 'closed',
};

/**
 * Create an SSE connection for seat updates
 * Backend endpoint: GET /api/events/{id}/seats/stream
 * 
 * @param {string} concertId - Concert ID to subscribe to
 * @param {Object} callbacks - Callback functions
 * @param {Function} callbacks.onSeatUpdate - Called when a seat status changes
 * @param {Function} callbacks.onConnectionChange - Called when connection status changes
 * @param {Function} callbacks.onError - Called on connection error
 * @returns {Function} - Cleanup function to close the connection
 */
export const createSeatStream = (concertId, callbacks = {}) => {
  const { onSeatUpdate, onConnectionChange, onError } = callbacks;
  
  let eventSource = null;
  let reconnectAttempts = 0;
  let reconnectTimeout = null;
  let isClosed = false;
  const maxReconnectAttempts = 10;
  const baseReconnectDelay = 1000; // 1 Sekunde
  const maxReconnectDelay = 30000; // 30 Sekunden max
  
  /**
   * Berechnet Verzögerung mit exponentiellem Backoff
   */
  const getReconnectDelay = () => {
    const delay = Math.min(
      baseReconnectDelay * Math.pow(2, reconnectAttempts),
      maxReconnectDelay
    );
    return delay;
  };
  
  /**
   * Erstellt die SSE-Verbindung
   */
  const connect = () => {
    if (isClosed) return;
    
    // JWT-Token für Authentifizierung (falls benötigt)
    const token = localStorage.getItem('token');
    const url = token 
      ? `${SSE_BASE_URL}/events/${concertId}/seats/stream?token=${encodeURIComponent(token)}`
      : `${SSE_BASE_URL}/events/${concertId}/seats/stream`;
    
    onConnectionChange?.(SSE_CONNECTION_STATUS.CONNECTING);
    
    eventSource = new EventSource(url);
    
    // Verbindung erfolgreich geöffnet
    eventSource.onopen = () => {
      console.log(`[SSE] Verbindung hergestellt für Konzert ${concertId}`);
      reconnectAttempts = 0;
      onConnectionChange?.(SSE_CONNECTION_STATUS.CONNECTED);
    };
    
    // seat_update Event Handler
    eventSource.addEventListener('seat_update', (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('[SSE] Seat Update erhalten:', data);
        onSeatUpdate?.(data);
      } catch (parseError) {
        console.error('[SSE] Fehler beim Parsen des Events:', parseError);
      }
    });
    
    // Generischer message Handler (Fallback)
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('[SSE] Message erhalten:', data);
        // Falls kein Event-Type, behandle als seat_update
        if (data.seatId && data.status) {
          onSeatUpdate?.(data);
        }
      } catch (parseError) {
        // Ignoriere nicht-JSON Messages (z.B. Heartbeats)
        console.log('[SSE] Non-JSON message:', event.data);
      }
    };
    
    // Fehlerbehandlung mit Auto-Reconnect
    eventSource.onerror = (error) => {
      console.error('[SSE] Verbindungsfehler:', error);
      eventSource.close();
      
      if (isClosed) return;
      
      if (reconnectAttempts < maxReconnectAttempts) {
        const delay = getReconnectDelay();
        reconnectAttempts++;
        console.log(`[SSE] Reconnect Versuch ${reconnectAttempts}/${maxReconnectAttempts} in ${delay}ms`);
        onConnectionChange?.(SSE_CONNECTION_STATUS.RECONNECTING);
        
        reconnectTimeout = setTimeout(connect, delay);
      } else {
        console.error('[SSE] Max Reconnect-Versuche erreicht');
        onConnectionChange?.(SSE_CONNECTION_STATUS.ERROR);
        onError?.(new Error('Verbindung konnte nicht hergestellt werden'));
      }
    };
  };
  
  // Initiale Verbindung starten
  connect();
  
  // Cleanup-Funktion zurückgeben
  return () => {
    isClosed = true;
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout);
      reconnectTimeout = null;
    }
    if (eventSource) {
      eventSource.close();
      eventSource = null;
    }
    onConnectionChange?.(SSE_CONNECTION_STATUS.CLOSED);
    console.log(`[SSE] Verbindung geschlossen für Konzert ${concertId}`);
  };
};

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
