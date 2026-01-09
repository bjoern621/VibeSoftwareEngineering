/**
 * Damage Report Service - API Calls für Schadensberichte
 *
 * Enthält Funktionen für:
 * - Schadensbericht erstellen
 * - Schadensbericht abrufen
 * - Schadensberichte pro Buchung abrufen
 */

import apiClient from '../config/axiosConfig';

/**
 * Erstellt einen neuen Schadensbericht für eine Buchung.
 *
 * @param {number} bookingId - Buchungs-ID
 * @param {Object} damageData - Schadensdaten
 * @param {string} damageData.description - Beschreibung des Schadens
 * @param {number} damageData.estimatedCost - Geschätzte Kosten
 * @param {string[]} damageData.photos - Array von Foto-URLs (Base64 oder URL)
 * @returns {Promise<Object>} Erstellter Schadensbericht
 */
export const createDamageReport = async (bookingId, damageData) => {
  try {
    const response = await apiClient.post(`/vermietung/${bookingId}/schadensbericht`, {
      description: damageData.description,
      estimatedCost: damageData.estimatedCost,
      photos: damageData.photos || [],
    });
    return response.data;
  } catch (error) {
    console.error('Fehler beim Erstellen des Schadensberichts:', error);
    throw new Error(
      error.response?.data?.message ||
        'Der Schadensbericht konnte nicht erstellt werden. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Ruft einen Schadensbericht anhand seiner ID ab.
 *
 * @param {number} damageReportId - Schadensbericht-ID
 * @returns {Promise<Object>} Schadensbericht-Details
 */
export const getDamageReportById = async (damageReportId) => {
  try {
    const response = await apiClient.get(`/schadensberichte/${damageReportId}`);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden des Schadensberichts:', error);
    throw new Error(
      error.response?.data?.message || 'Der Schadensbericht konnte nicht geladen werden.'
    );
  }
};

/**
 * Ruft alle Schadensberichte für eine Buchung ab.
 *
 * @param {number} bookingId - Buchungs-ID
 * @returns {Promise<Array>} Liste der Schadensberichte
 */
export const getDamageReportsByBooking = async (bookingId) => {
  try {
    const response = await apiClient.get(`/buchungen/${bookingId}/schadensberichte`);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Schadensberichte:', error);
    throw new Error(
      error.response?.data?.message || 'Die Schadensberichte konnten nicht geladen werden.'
    );
  }
};

/**
 * Konvertiert eine Datei in Base64-Format für den Upload.
 *
 * @param {File} file - Die hochzuladende Datei
 * @returns {Promise<string>} Base64-codierter String
 */
export const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = (error) => reject(error);
  });
};

/**
 * Validiert die Schadensbericht-Daten.
 *
 * @param {Object} damageData - Zu validierende Daten
 * @returns {Object} Validierungsergebnis { isValid: boolean, errors: string[] }
 */
export const validateDamageReport = (damageData) => {
  const errors = [];

  if (!damageData.description || damageData.description.trim().length === 0) {
    errors.push('Beschreibung ist erforderlich.');
  }

  if (damageData.estimatedCost !== undefined && damageData.estimatedCost !== null) {
    const cost = parseFloat(damageData.estimatedCost);
    if (isNaN(cost) || cost < 0) {
      errors.push('Geschätzte Kosten müssen eine positive Zahl sein.');
    }
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
};

export default {
  createDamageReport,
  getDamageReportById,
  getDamageReportsByBooking,
  fileToBase64,
  validateDamageReport,
};
