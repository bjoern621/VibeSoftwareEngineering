// filepath: frontend/src/services/bookingService.js
/**
 * Booking Service - API Calls für Buchungen und Preisberechnung
 *
 * Enthält Funktionen für:
 * - Preisberechnung
 * - Buchungserstellung
 * - Buchungsabfrage
 */

import apiClient from '../config/axiosConfig';

/**
 * Berechnet den Preis für eine gegebene Konfiguration.
 *
 * Erwartetes payload-Shape (gemäß Backend PriceCalculationRequestDTO):
 * {
 *   vehicleType: string, // z.B. "COMPACT_CAR"
 *   pickupDateTime: string, // yyyy-MM-dd'T'HH:mm:ss
 *   returnDateTime: string, // yyyy-MM-dd'T'HH:mm:ss
 *   additionalServices: string[] // optional
 * }
 *
 * @param {Object} payload
 * @returns {Promise<Object>} PriceCalculationResponseDTO
 */
export const calculatePrice = async (payload) => {
  try {
    const response = await apiClient.post('/buchungen/preis-berechnen', payload);
    return response.data;
  } catch (error) {
    console.error('Fehler bei der Preisberechnung:', error);
    throw new Error(
      error.response?.data?.message ||
        'Fehler bei der Preisberechnung. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Erstellt eine neue Buchung.
 *
 * Erwartetes payload-Shape (gemäß Backend CreateBookingRequestDTO):
 * {
 *   vehicleId: number,
 *   pickupBranchId: number,
 *   returnBranchId: number,
 *   pickupDateTime: string, // ISO-8601 format
 *   returnDateTime: string, // ISO-8601 format
 *   extras: [{ id: string, name: string, pricePerDay: number, quantity: number }] // optional
 * }
 *
 * @param {Object} bookingData Buchungsdaten aus BookingContext
 * @returns {Promise<Object>} Erstellte Buchung (BookingHistoryDto)
 */
const createBooking = async (bookingData) => {
  try {
    // Transformiere Frontend-Daten in Backend-Format
    const payload = {
      vehicleId: bookingData.vehicleId,
      pickupBranchId: bookingData.pickupBranchId,
      returnBranchId: bookingData.returnBranchId,
      pickupDateTime: bookingData.pickupDateTime,
      returnDateTime: bookingData.returnDateTime,
      // Extras: Frontend nutzt {id, name, pricePerDay, quantity}
      // Backend erwartet additionalServices: string[]
      additionalServices: bookingData.extras?.map((extra) => extra.id) || [],
    };

    const response = await apiClient.post('/buchungen', payload);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Erstellen der Buchung:', error);
    throw new Error(
      error.response?.data?.message ||
        'Die Buchung konnte nicht erstellt werden. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Ruft eine Buchung anhand ihrer ID ab.
 *
 * @param {number|string} bookingId Buchungs-ID
 * @returns {Promise<Object>} Buchungsdetails
 */
const getBookingById = async (bookingId) => {
  try {
    const response = await apiClient.get(`/buchungen/${bookingId}`);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Buchung:', error);
    throw new Error(error.response?.data?.message || 'Die Buchung konnte nicht geladen werden.');
  }
};

/**
 * Ruft alle Buchungen des aktuellen Kunden ab.
 *
 * @param {string} status Optional: Filter nach Status (z.B. "CONFIRMED", "CANCELLED")
 * @returns {Promise<Array>} Liste der Buchungen
 */
const getMyBookings = async (status = null) => {
  try {
    const params = status ? { status } : {};
    const response = await apiClient.get('/kunden/meine-buchungen', { params });
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Buchungen:', error);
    throw new Error(error.response?.data?.message || 'Buchungen konnten nicht geladen werden.');
  }
};

/**
 * Storniert eine Buchung.
 *
 * @param {number} bookingId Buchungs-ID
 * @param {string} reason Stornierungsgrund (optional)
 * @returns {Promise<void>} 204 No Content bei Erfolg
 */
const cancelBooking = async (bookingId, reason = null) => {
  try {
    const payload = reason ? { reason } : null;
    await apiClient.delete(`/buchungen/${bookingId}/stornieren`, { data: payload });
  } catch (error) {
    console.error('Fehler beim Stornieren der Buchung:', error);
    throw new Error(error.response?.data?.message || 'Die Buchung konnte nicht storniert werden.');
  }
};

/**
 * Ruft die Zusatzkosten einer Buchung ab.
 *
 * @param {number} bookingId Buchungs-ID
 * @returns {Promise<Object>} AdditionalCostsDTO (lateFee, excessMileageFee, damageCost, total)
 */
const getAdditionalCosts = async (bookingId) => {
  try {
    const response = await apiClient.get(`/buchungen/${bookingId}/zusatzkosten`);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Zusatzkosten:', error);
    throw new Error(error.response?.data?.message || 'Zusatzkosten konnten nicht geladen werden.');
  }
};

/**
 * Ruft alle Buchungen ab (nur für Employee/Admin).
 *
 * @param {string} status Optional: Filter nach Status (z.B. "REQUESTED", "CONFIRMED")
 * @returns {Promise<Array>} Liste aller Buchungen
 */
const getAllBookings = async (status = null) => {
  try {
    const params = status ? { status } : {};
    const response = await apiClient.get('/buchungen', { params });
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden aller Buchungen:', error);
    throw new Error(error.response?.data?.message || 'Buchungen konnten nicht geladen werden.');
  }
};

/**
 * Bestätigt eine Buchung (nur für Employee/Admin).
 *
 * @param {number} bookingId Buchungs-ID
 * @returns {Promise<Object>} Die bestätigte Buchung
 */
const confirmBooking = async (bookingId) => {
  try {
    const response = await apiClient.post(`/buchungen/${bookingId}/bestaetigen`);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Bestätigen der Buchung:', error);
    throw new Error(error.response?.data?.message || 'Die Buchung konnte nicht bestätigt werden.');
  }
};

export default {
  calculatePrice,
  createBooking,
  getBookingById,
  getMyBookings,
  cancelBooking,
  getAdditionalCosts,
  getAllBookings,
  confirmBooking,
};
