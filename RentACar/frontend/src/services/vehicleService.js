/**
 * Vehicle Service - API Calls für Fahrzeugsuche und Verwaltung
 *
 * Stellt alle benötigten API-Funktionen für die Fahrzeugsuche bereit:
 * - Suche nach verfügbaren Fahrzeugen
 * - Abrufen von Fahrzeugtypen
 * - Abrufen von Filialen/Branches
 * - CRUD-Operationen für Fahrzeugverwaltung (Admin/Employee)
 */

import apiClient from '../config/axiosConfig';
import { extractErrorMessage } from '../utils/apiErrorHandler';

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
    throw extractErrorMessage(error, 'Fehler beim Laden der verfügbaren Fahrzeuge. Bitte versuchen Sie es später erneut.');
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
    throw extractErrorMessage(error, 'Fehler beim Laden der Fahrzeugtypen. Bitte versuchen Sie es später erneut.');
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
    throw extractErrorMessage(error, 'Fehler beim Laden der Filialen. Bitte versuchen Sie es später erneut.');
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
    throw extractErrorMessage(error, 'Fahrzeug nicht gefunden.');
  }
};

/**
 * Ruft alle Fahrzeuge ab (nur für EMPLOYEE/ADMIN).
 *
 * @returns {Promise<Array>} Liste aller Fahrzeuge
 * @throws {Error} Bei Netzwerkfehlern oder fehlender Berechtigung
 */
export const getAllVehicles = async () => {
  try {
    const response = await apiClient.get('/fahrzeuge');
    return response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Fahrzeuge:', error);
    throw extractErrorMessage(error, 'Fehler beim Laden der Fahrzeuge. Bitte versuchen Sie es später erneut.');
  }
};

/**
 * Erstellt ein neues Fahrzeug (nur für EMPLOYEE/ADMIN).
 *
 * @param {Object} vehicleData - Fahrzeugdaten
 * @param {string} vehicleData.licensePlate - Kennzeichen
 * @param {string} vehicleData.brand - Marke
 * @param {string} vehicleData.model - Modell
 * @param {number} vehicleData.year - Baujahr
 * @param {number} vehicleData.mileage - Kilometerstand
 * @param {string} vehicleData.vehicleType - Fahrzeugtyp (COMPACT_CAR, SEDAN, SUV, VAN)
 * @param {number} vehicleData.branchId - Filial-ID
 * @returns {Promise<Object>} Das erstellte Fahrzeug
 * @throws {Error} Bei Validierungsfehlern oder Netzwerkfehlern
 */
export const createVehicle = async (vehicleData) => {
  try {
    const response = await apiClient.post('/fahrzeuge', vehicleData);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Erstellen des Fahrzeugs:', error);
    throw extractErrorMessage(error, 'Fehler beim Erstellen des Fahrzeugs. Bitte überprüfen Sie die Eingaben.');
  }
};

/**
 * Aktualisiert ein bestehendes Fahrzeug (nur für EMPLOYEE/ADMIN).
 *
 * @param {number} vehicleId - Fahrzeug-ID
 * @param {Object} vehicleData - Aktualisierte Fahrzeugdaten
 * @param {string} vehicleData.brand - Marke
 * @param {string} vehicleData.model - Modell
 * @param {number} vehicleData.year - Baujahr
 * @param {number} vehicleData.mileage - Kilometerstand
 * @param {string} vehicleData.vehicleType - Fahrzeugtyp
 * @param {number} vehicleData.branchId - Filial-ID
 * @returns {Promise<Object>} Das aktualisierte Fahrzeug
 * @throws {Error} Bei Validierungsfehlern oder Netzwerkfehlern
 */
export const updateVehicle = async (vehicleId, vehicleData) => {
  try {
    const response = await apiClient.put(`/fahrzeuge/${vehicleId}`, vehicleData);
    return response.data;
  } catch (error) {
    console.error('Fehler beim Aktualisieren des Fahrzeugs:', error);
    throw extractErrorMessage(error, 'Fehler beim Aktualisieren des Fahrzeugs. Bitte überprüfen Sie die Eingaben.');
  }
};

/**
 * Markiert ein Fahrzeug als in Wartung (nur für EMPLOYEE/ADMIN).
 *
 * @param {number} vehicleId - Fahrzeug-ID
 * @throws {Error} Bei Netzwerkfehlern oder ungültigem Status-Übergang
 */
export const markAsInMaintenance = async (vehicleId) => {
  try {
    await apiClient.patch(`/fahrzeuge/${vehicleId}/wartung`);
  } catch (error) {
    console.error('Fehler beim Ändern des Status:', error);
    throw extractErrorMessage(error, 'Fehler beim Ändern des Status. Das Fahrzeug ist möglicherweise vermietet.');
  }
};

/**
 * Markiert ein Fahrzeug als außer Betrieb (nur für EMPLOYEE/ADMIN).
 *
 * @param {number} vehicleId - Fahrzeug-ID
 * @throws {Error} Bei Netzwerkfehlern oder ungültigem Status-Übergang
 */
export const markAsOutOfService = async (vehicleId) => {
  try {
    await apiClient.patch(`/fahrzeuge/${vehicleId}/ausser-betrieb`);
  } catch (error) {
    console.error('Fehler beim Ändern des Status:', error);
    throw extractErrorMessage(error, 'Fehler beim Ändern des Status. Das Fahrzeug ist möglicherweise vermietet.');
  }
};

/**
 * Reaktiviert ein Fahrzeug (von IN_MAINTENANCE oder OUT_OF_SERVICE auf AVAILABLE).
 * Nur für EMPLOYEE/ADMIN.
 *
 * @param {number} vehicleId - Fahrzeug-ID
 * @throws {Error} Bei Netzwerkfehlern oder ungültigem Status-Übergang
 */
export const markAsAvailable = async (vehicleId) => {
  try {
    await apiClient.patch(`/fahrzeuge/${vehicleId}/verfuegbar`);
  } catch (error) {
    console.error('Fehler beim Ändern des Status:', error);
    throw extractErrorMessage(error, 'Fehler beim Ändern des Status. Das Fahrzeug hat einen ungültigen Status.');
  }
};
