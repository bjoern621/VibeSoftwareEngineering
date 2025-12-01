/**
 * Rental Service - API Calls für Vermietungsprozesse (Check-in/Check-out)
 *
 * Enthält Funktionen für:
 * - Check-out (Fahrzeugübergabe)
 * - Check-in (Fahrzeugrückgabe)
 */

import apiClient from '../config/axiosConfig';

/**
 * Führt den Check-out (Fahrzeugübergabe) durch.
 * Nur für EMPLOYEE/ADMIN.
 *
 * @param {number} bookingId - Buchungs-ID
 * @param {Object} checkoutData - Check-out Daten
 * @param {number} checkoutData.mileage - Kilometerstand
 * @param {string} checkoutData.fuelLevel - Tankfüllstand (z.B. "FULL", "3/4", "1/2", "1/4", "EMPTY")
 * @param {string} checkoutData.cleanliness - Sauberkeit (z.B. "CLEAN", "NORMAL", "DIRTY")
 * @param {string} [checkoutData.damagesDescription] - Schadensbeschreibung (optional)
 * @returns {Promise<void>}
 */
export const performCheckOut = async (bookingId, checkoutData) => {
  try {
    await apiClient.post(`/vermietung/${bookingId}/checkout`, {
      mileage: checkoutData.mileage,
      fuelLevel: checkoutData.fuelLevel,
      cleanliness: checkoutData.cleanliness,
      damagesDescription: checkoutData.damagesDescription || null,
    });
  } catch (error) {
    console.error('Fehler beim Check-out:', error);
    throw new Error(
      error.response?.data?.message ||
        'Der Check-out konnte nicht durchgeführt werden. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Führt den Check-in (Fahrzeugrückgabe) durch.
 * Nur für EMPLOYEE/ADMIN.
 *
 * @param {number} bookingId - Buchungs-ID
 * @param {Object} checkinData - Check-in Daten
 * @param {number} checkinData.mileage - Kilometerstand
 * @param {string} checkinData.fuelLevel - Tankfüllstand (z.B. "FULL", "3/4", "1/2", "1/4", "EMPTY")
 * @param {string} checkinData.cleanliness - Sauberkeit (z.B. "CLEAN", "NORMAL", "DIRTY")
 * @param {string} [checkinData.damagesDescription] - Schadensbeschreibung (optional)
 * @returns {Promise<void>}
 */
export const performCheckIn = async (bookingId, checkinData) => {
  try {
    await apiClient.post(`/vermietung/${bookingId}/checkin`, {
      mileage: checkinData.mileage,
      fuelLevel: checkinData.fuelLevel,
      cleanliness: checkinData.cleanliness,
      damagesDescription: checkinData.damagesDescription || null,
    });
  } catch (error) {
    console.error('Fehler beim Check-in:', error);
    throw new Error(
      error.response?.data?.message ||
        'Der Check-in konnte nicht durchgeführt werden. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Validiert die Check-out Daten.
 *
 * @param {Object} checkoutData - Zu validierende Daten
 * @returns {Object} Validierungsergebnis { isValid: boolean, errors: string[] }
 */
export const validateCheckoutData = (checkoutData) => {
  const errors = [];

  if (checkoutData.mileage === undefined || checkoutData.mileage === null) {
    errors.push('Kilometerstand ist erforderlich.');
  } else if (checkoutData.mileage < 0) {
    errors.push('Kilometerstand muss positiv sein.');
  }

  if (!checkoutData.fuelLevel || checkoutData.fuelLevel.trim() === '') {
    errors.push('Tankfüllstand ist erforderlich.');
  }

  if (!checkoutData.cleanliness || checkoutData.cleanliness.trim() === '') {
    errors.push('Sauberkeit ist erforderlich.');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
};

/**
 * Validiert die Check-in Daten.
 *
 * @param {Object} checkinData - Zu validierende Daten
 * @param {number} checkoutMileage - Kilometerstand bei Check-out (für Vergleich)
 * @returns {Object} Validierungsergebnis { isValid: boolean, errors: string[] }
 */
export const validateCheckinData = (checkinData, checkoutMileage = 0) => {
  const errors = [];

  if (checkinData.mileage === undefined || checkinData.mileage === null) {
    errors.push('Kilometerstand ist erforderlich.');
  } else if (checkinData.mileage < 0) {
    errors.push('Kilometerstand muss positiv sein.');
  } else if (checkoutMileage > 0 && checkinData.mileage < checkoutMileage) {
    errors.push(`Kilometerstand muss höher sein als bei Check-out (${checkoutMileage} km).`);
  }

  if (!checkinData.fuelLevel || checkinData.fuelLevel.trim() === '') {
    errors.push('Tankfüllstand ist erforderlich.');
  }

  if (!checkinData.cleanliness || checkinData.cleanliness.trim() === '') {
    errors.push('Sauberkeit ist erforderlich.');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
};

/**
 * Tankfüllstand-Optionen für Dropdowns/Slider
 */
export const FUEL_LEVELS = [
  { value: 'EMPTY', label: 'E', numericValue: 0 },
  { value: '1/4', label: '1/4', numericValue: 1 },
  { value: '1/2', label: '1/2', numericValue: 2 },
  { value: '3/4', label: '3/4', numericValue: 3 },
  { value: 'FULL', label: 'F', numericValue: 4 },
];

/**
 * Sauberkeits-Optionen
 */
export const CLEANLINESS_OPTIONS = [
  { value: 'DIRTY', label: 'Verschmutzt' },
  { value: 'NORMAL', label: 'Normal' },
  { value: 'CLEAN', label: 'Sauber' },
];

/**
 * Konvertiert numerischen Slider-Wert zu Tankfüllstand-String
 */
export const sliderValueToFuelLevel = (value) => {
  const level = FUEL_LEVELS.find((l) => l.numericValue === value);
  return level ? level.value : 'FULL';
};

/**
 * Konvertiert Tankfüllstand-String zu numerischem Slider-Wert
 */
export const fuelLevelToSliderValue = (fuelLevel) => {
  const level = FUEL_LEVELS.find((l) => l.value === fuelLevel);
  return level ? level.numericValue : 4;
};

const rentalService = {
  performCheckOut,
  performCheckIn,
  validateCheckoutData,
  validateCheckinData,
  FUEL_LEVELS,
  CLEANLINESS_OPTIONS,
  sliderValueToFuelLevel,
  fuelLevelToSliderValue,
};

export default rentalService;
