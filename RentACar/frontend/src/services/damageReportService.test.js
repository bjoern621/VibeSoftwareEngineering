/**
 * damageReportService Tests
 *
 * Unit-Tests für alle Damage Report Service Funktionen
 */

import damageReportService, {
  createDamageReport,
  getDamageReportById,
  getDamageReportsByBooking,
  fileToBase64,
  validateDamageReport,
} from './damageReportService';
import apiClient from '../config/axiosConfig';

jest.mock('../config/axiosConfig', () => {
  const mockAxios = {
    post: jest.fn(),
    get: jest.fn(),
  };
  return {
    __esModule: true,
    default: mockAxios,
  };
});

describe('damageReportService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createDamageReport', () => {
    it('sollte erfolgreich einen Schadensbericht erstellen', async () => {
      const bookingId = 1;
      const damageData = {
        description: 'Kratzer an der Stoßstange',
        estimatedCost: 150,
        photos: ['data:image/png;base64,test1'],
      };

      const mockResponse = {
        data: {
          id: 1,
          rentalAgreementId: 10,
          bookingId: 1,
          vehicleId: 5,
          description: 'Kratzer an der Stoßstange',
          estimatedCost: 150,
          photos: ['data:image/png;base64,test1'],
        },
      };

      apiClient.post.mockResolvedValue(mockResponse);

      const result = await createDamageReport(bookingId, damageData);

      expect(apiClient.post).toHaveBeenCalledWith('/vermietung/1/schadensbericht', {
        description: 'Kratzer an der Stoßstange',
        estimatedCost: 150,
        photos: ['data:image/png;base64,test1'],
      });
      expect(result).toEqual(mockResponse.data);
    });

    it('sollte leeres photos-Array verwenden wenn keine Fotos übergeben', async () => {
      const bookingId = 2;
      const damageData = {
        description: 'Delle',
        estimatedCost: 200,
      };

      const mockResponse = {
        data: { id: 2, description: 'Delle', estimatedCost: 200, photos: [] },
      };

      apiClient.post.mockResolvedValue(mockResponse);

      await createDamageReport(bookingId, damageData);

      expect(apiClient.post).toHaveBeenCalledWith('/vermietung/2/schadensbericht', {
        description: 'Delle',
        estimatedCost: 200,
        photos: [],
      });
    });

    it('sollte Fehler bei API-Fehler werfen', async () => {
      const bookingId = 1;
      const damageData = { description: 'Test' };

      const mockError = {
        response: {
          data: {
            message: 'Mietvertrag nicht gefunden',
          },
        },
      };

      apiClient.post.mockRejectedValue(mockError);

      await expect(createDamageReport(bookingId, damageData)).rejects.toThrow(
        'Mietvertrag nicht gefunden'
      );
    });

    it('sollte generische Fehlermeldung bei unbekanntem Fehler werfen', async () => {
      const bookingId = 1;
      const damageData = { description: 'Test' };

      apiClient.post.mockRejectedValue(new Error('Network Error'));

      await expect(createDamageReport(bookingId, damageData)).rejects.toThrow(
        'Der Schadensbericht konnte nicht erstellt werden. Bitte versuchen Sie es später erneut.'
      );
    });
  });

  describe('getDamageReportById', () => {
    it('sollte einen Schadensbericht anhand der ID abrufen', async () => {
      const reportId = 1;
      const mockResponse = {
        data: {
          id: 1,
          description: 'Kratzer',
          estimatedCost: 100,
          photos: [],
        },
      };

      apiClient.get.mockResolvedValue(mockResponse);

      const result = await getDamageReportById(reportId);

      expect(apiClient.get).toHaveBeenCalledWith('/schadensberichte/1');
      expect(result).toEqual(mockResponse.data);
    });

    it('sollte Fehler werfen wenn Bericht nicht gefunden', async () => {
      const reportId = 999;
      const mockError = {
        response: {
          data: {
            message: 'Schadensbericht nicht gefunden',
          },
        },
      };

      apiClient.get.mockRejectedValue(mockError);

      await expect(getDamageReportById(reportId)).rejects.toThrow('Schadensbericht nicht gefunden');
    });
  });

  describe('getDamageReportsByBooking', () => {
    it('sollte alle Schadensberichte für eine Buchung abrufen', async () => {
      const bookingId = 1;
      const mockResponse = {
        data: [
          { id: 1, description: 'Kratzer', estimatedCost: 100 },
          { id: 2, description: 'Delle', estimatedCost: 200 },
        ],
      };

      apiClient.get.mockResolvedValue(mockResponse);

      const result = await getDamageReportsByBooking(bookingId);

      expect(apiClient.get).toHaveBeenCalledWith('/buchungen/1/schadensberichte');
      expect(result).toHaveLength(2);
      expect(result[0].description).toBe('Kratzer');
    });

    it('sollte leeres Array zurückgeben wenn keine Berichte vorhanden', async () => {
      const bookingId = 99;
      const mockResponse = { data: [] };

      apiClient.get.mockResolvedValue(mockResponse);

      const result = await getDamageReportsByBooking(bookingId);

      expect(result).toEqual([]);
    });
  });

  describe('fileToBase64', () => {
    it('sollte eine Datei in Base64 konvertieren', async () => {
      const mockFile = new Blob(['test'], { type: 'image/png' });
      mockFile.name = 'test.png';

      const result = await fileToBase64(mockFile);

      expect(result).toContain('data:');
    });
  });

  describe('validateDamageReport', () => {
    it('sollte gültige Daten akzeptieren', () => {
      const damageData = {
        description: 'Kratzer an der Tür',
        estimatedCost: 150,
      };

      const result = validateDamageReport(damageData);

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('sollte fehlende Beschreibung ablehnen', () => {
      const damageData = {
        description: '',
        estimatedCost: 100,
      };

      const result = validateDamageReport(damageData);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Beschreibung ist erforderlich.');
    });

    it('sollte null/undefined Beschreibung ablehnen', () => {
      const damageData = {
        description: null,
        estimatedCost: 100,
      };

      const result = validateDamageReport(damageData);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Beschreibung ist erforderlich.');
    });

    it('sollte negative Kosten ablehnen', () => {
      const damageData = {
        description: 'Test Schaden',
        estimatedCost: -50,
      };

      const result = validateDamageReport(damageData);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Geschätzte Kosten müssen eine positive Zahl sein.');
    });

    it('sollte ungültige Kosten (NaN) ablehnen', () => {
      const damageData = {
        description: 'Test Schaden',
        estimatedCost: 'abc',
      };

      const result = validateDamageReport(damageData);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Geschätzte Kosten müssen eine positive Zahl sein.');
    });

    it('sollte null/undefined Kosten akzeptieren', () => {
      const damageData = {
        description: 'Test Schaden',
        estimatedCost: null,
      };

      const result = validateDamageReport(damageData);

      expect(result.isValid).toBe(true);
    });

    it('sollte Kosten von 0 akzeptieren', () => {
      const damageData = {
        description: 'Kleiner Kratzer ohne Kosten',
        estimatedCost: 0,
      };

      const result = validateDamageReport(damageData);

      expect(result.isValid).toBe(true);
    });
  });

  describe('default export', () => {
    it('sollte alle Funktionen exportieren', () => {
      expect(damageReportService.createDamageReport).toBeDefined();
      expect(damageReportService.getDamageReportById).toBeDefined();
      expect(damageReportService.getDamageReportsByBooking).toBeDefined();
      expect(damageReportService.fileToBase64).toBeDefined();
      expect(damageReportService.validateDamageReport).toBeDefined();
    });
  });
});
