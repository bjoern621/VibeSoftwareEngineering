// filepath: frontend/src/services/bookingService.js
/**
 * Booking Service - API Calls für Buchungen und Preisberechnung
 *
 * Enthält eine helper-Funktion für die Preisberechnung, die das
 * Backend-Endpoint POST /api/buchungen/preis-berechnen aufruft.
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

export default {
  calculatePrice,
};

