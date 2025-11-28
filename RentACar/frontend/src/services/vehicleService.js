/**
 * Vehicle Service - API Calls für Fahrzeugsuche und Verwaltung
 * 
 * Stellt alle benötigten API-Funktionen für die Fahrzeugsuche bereit:
 * - Suche nach verfügbaren Fahrzeugen
 * - Abrufen von Fahrzeugtypen
 * - Abrufen von Filialen/Branches
 */

import apiClient from '../config/axiosConfig';

/**
 * Sucht nach verfügbaren Fahrzeugen basierend auf Kriterien.
 * 
 * @param {Object} searchParams - Suchparameter
 * @param {string} searchParams.von - Startdatum (YYYY-MM-DD)
 * @param {string} searchParams.bis - Enddatum (YYYY-MM-DD)
 * @param {string} [searchParams.typ] - Fahrzeugtyp (COMPACT_CAR, SEDAN, SUV, VAN) - optional
 * @param {string} [searchParams.standort] - Standort/Filiale - optional
 * @returns {Promise<Array>} Liste der verfügbaren Fahrzeuge mit Preisen
 * @throws {Error} Bei Netzwerkfehlern oder ungültigen Parametern
 */
export const searchVehicles = async ({ von, bis, typ, standort }) => {
  try {
    // Query-Parameter zusammenstellen
    const params = new URLSearchParams();
    params.append('von', von);
    params.append('bis', bis);
    
    if (typ && typ !== 'ALL') {
      params.append('typ', typ);
    }
    
    if (standort) {
      params.append('standort', standort);
    }
    
    const response = await apiClient.get(`/fahrzeuge/suche?${params.toString()}`);
    return response.data;
  } catch (error) {
    console.error('Fehler bei Fahrzeugsuche:', error);
    throw new Error(
      error.response?.data?.message || 
      'Fehler beim Laden der verfügbaren Fahrzeuge. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Ruft alle verfügbaren Fahrzeugtypen ab.
 * 
 * @returns {Promise<Array>} Liste der Fahrzeugtypen mit Details
 * @throws {Error} Bei Netzwerkfehlern
 */
export const getVehicleTypes = async () => {
  try {
    const response = await apiClient.get('/vehicle-types');
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Fahrzeugtypen:', error);
    throw new Error(
      error.response?.data?.message || 
      'Fehler beim Laden der Fahrzeugtypen. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Ruft alle verfügbaren Filialen/Branches ab.
 * 
 * @returns {Promise<Array>} Liste der Filialen
 * @throws {Error} Bei Netzwerkfehlern
 */
export const getBranches = async () => {
  try {
    const response = await apiClient.get('/branches');
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Filialen:', error);
    throw new Error(
      error.response?.data?.message || 
      'Fehler beim Laden der Filialen. Bitte versuchen Sie es später erneut.'
    );
  }
};

/**
 * Ruft Details zu einem einzelnen Fahrzeug ab.
 * 
 * @param {number} vehicleId - ID des Fahrzeugs
 * @returns {Promise<Object>} Fahrzeugdetails
 * @throws {Error} Bei Netzwerkfehlern oder wenn Fahrzeug nicht gefunden
 */
export const getVehicleById = async (vehicleId) => {
  try {
    const response = await apiClient.get(`/fahrzeuge/${vehicleId}`);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden des Fahrzeugs:', error);
    throw new Error(
      error.response?.data?.message || 
      'Fahrzeug nicht gefunden.'
    );
  }
};
