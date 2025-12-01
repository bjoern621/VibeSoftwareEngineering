/**
 * Tests für Rental Service
 */

import rentalService, {
  performCheckOut,
  performCheckIn,
  validateCheckoutData,
  validateCheckinData,
  FUEL_LEVELS,
  CLEANLINESS_OPTIONS,
  sliderValueToFuelLevel,
  fuelLevelToSliderValue,
} from './rentalService';
import apiClient from '../config/axiosConfig';

jest.mock('../config/axiosConfig', () => {
  const mockAxios = {
    post: jest.fn(),
    get: jest.fn(),
  };
  return {
    __esModule: true,
    default: mockAxios,
    TOKEN_STORAGE_KEY: 'rentacar_jwt_token',
  };
});

describe('rentalService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('performCheckOut', () => {
    it('sollte Check-out erfolgreich durchführen', async () => {
      apiClient.post.mockResolvedValue({ data: {} });

      const checkoutData = {
        mileage: 12500,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
        damagesDescription: null,
      };

      await performCheckOut(123, checkoutData);

      expect(apiClient.post).toHaveBeenCalledWith('/vermietung/123/checkout', {
        mileage: 12500,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
        damagesDescription: null,
      });
    });

    it('sollte Check-out mit Schadensbeschreibung durchführen', async () => {
      apiClient.post.mockResolvedValue({ data: {} });

      const checkoutData = {
        mileage: 12500,
        fuelLevel: '3/4',
        cleanliness: 'NORMAL',
        damagesDescription: 'Kleine Kratzer auf der linken Seite',
      };

      await performCheckOut(456, checkoutData);

      expect(apiClient.post).toHaveBeenCalledWith('/vermietung/456/checkout', {
        mileage: 12500,
        fuelLevel: '3/4',
        cleanliness: 'NORMAL',
        damagesDescription: 'Kleine Kratzer auf der linken Seite',
      });
    });

    it('sollte Fehler bei fehlgeschlagenem Check-out werfen', async () => {
      apiClient.post.mockRejectedValue({
        response: { data: { message: 'Buchung nicht gefunden' } },
      });

      await expect(
        performCheckOut(999, {
          mileage: 12500,
          fuelLevel: 'FULL',
          cleanliness: 'CLEAN',
        })
      ).rejects.toThrow('Buchung nicht gefunden');
    });
  });

  describe('performCheckIn', () => {
    it('sollte Check-in erfolgreich durchführen', async () => {
      apiClient.post.mockResolvedValue({ data: {} });

      const checkinData = {
        mileage: 13000,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
        damagesDescription: null,
      };

      await performCheckIn(123, checkinData);

      expect(apiClient.post).toHaveBeenCalledWith('/vermietung/123/checkin', {
        mileage: 13000,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
        damagesDescription: null,
      });
    });

    it('sollte Check-in mit Schäden durchführen', async () => {
      apiClient.post.mockResolvedValue({ data: {} });

      const checkinData = {
        mileage: 13500,
        fuelLevel: '1/2',
        cleanliness: 'DIRTY',
        damagesDescription: 'Beule an der Stoßstange',
      };

      await performCheckIn(789, checkinData);

      expect(apiClient.post).toHaveBeenCalledWith('/vermietung/789/checkin', {
        mileage: 13500,
        fuelLevel: '1/2',
        cleanliness: 'DIRTY',
        damagesDescription: 'Beule an der Stoßstange',
      });
    });

    it('sollte Fehler bei fehlgeschlagenem Check-in werfen', async () => {
      apiClient.post.mockRejectedValue({
        response: { data: { message: 'Kein Check-out gefunden' } },
      });

      await expect(
        performCheckIn(999, {
          mileage: 13000,
          fuelLevel: 'FULL',
          cleanliness: 'CLEAN',
        })
      ).rejects.toThrow('Kein Check-out gefunden');
    });
  });

  describe('validateCheckoutData', () => {
    it('sollte valide Daten akzeptieren', () => {
      const data = {
        mileage: 12500,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
      };

      const result = validateCheckoutData(data);

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('sollte fehlenden Kilometerstand erkennen', () => {
      const data = {
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
      };

      const result = validateCheckoutData(data);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Kilometerstand ist erforderlich.');
    });

    it('sollte negativen Kilometerstand erkennen', () => {
      const data = {
        mileage: -100,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
      };

      const result = validateCheckoutData(data);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Kilometerstand muss positiv sein.');
    });

    it('sollte fehlenden Tankfüllstand erkennen', () => {
      const data = {
        mileage: 12500,
        fuelLevel: '',
        cleanliness: 'CLEAN',
      };

      const result = validateCheckoutData(data);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Tankfüllstand ist erforderlich.');
    });

    it('sollte fehlende Sauberkeit erkennen', () => {
      const data = {
        mileage: 12500,
        fuelLevel: 'FULL',
        cleanliness: '',
      };

      const result = validateCheckoutData(data);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Sauberkeit ist erforderlich.');
    });

    it('sollte mehrere Fehler erkennen', () => {
      const data = {};

      const result = validateCheckoutData(data);

      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBeGreaterThanOrEqual(3);
    });
  });

  describe('validateCheckinData', () => {
    it('sollte valide Daten akzeptieren', () => {
      const data = {
        mileage: 13000,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
      };

      const result = validateCheckinData(data, 12500);

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('sollte Kilometerstand niedriger als bei Check-out erkennen', () => {
      const data = {
        mileage: 12000,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
      };

      const result = validateCheckinData(data, 12500);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        'Kilometerstand muss höher sein als bei Check-out (12500 km).'
      );
    });

    it('sollte gleichen Kilometerstand akzeptieren wenn checkoutMileage 0', () => {
      const data = {
        mileage: 12500,
        fuelLevel: 'FULL',
        cleanliness: 'CLEAN',
      };

      const result = validateCheckinData(data, 0);

      expect(result.isValid).toBe(true);
    });
  });

  describe('FUEL_LEVELS', () => {
    it('sollte 5 Tankfüllstände haben', () => {
      expect(FUEL_LEVELS).toHaveLength(5);
    });

    it('sollte EMPTY bis FULL enthalten', () => {
      const values = FUEL_LEVELS.map((l) => l.value);
      expect(values).toContain('EMPTY');
      expect(values).toContain('1/4');
      expect(values).toContain('1/2');
      expect(values).toContain('3/4');
      expect(values).toContain('FULL');
    });
  });

  describe('CLEANLINESS_OPTIONS', () => {
    it('sollte 3 Sauberkeitsoptionen haben', () => {
      expect(CLEANLINESS_OPTIONS).toHaveLength(3);
    });

    it('sollte DIRTY, NORMAL und CLEAN enthalten', () => {
      const values = CLEANLINESS_OPTIONS.map((o) => o.value);
      expect(values).toContain('DIRTY');
      expect(values).toContain('NORMAL');
      expect(values).toContain('CLEAN');
    });
  });

  describe('sliderValueToFuelLevel', () => {
    it('sollte 0 zu EMPTY konvertieren', () => {
      expect(sliderValueToFuelLevel(0)).toBe('EMPTY');
    });

    it('sollte 1 zu 1/4 konvertieren', () => {
      expect(sliderValueToFuelLevel(1)).toBe('1/4');
    });

    it('sollte 2 zu 1/2 konvertieren', () => {
      expect(sliderValueToFuelLevel(2)).toBe('1/2');
    });

    it('sollte 3 zu 3/4 konvertieren', () => {
      expect(sliderValueToFuelLevel(3)).toBe('3/4');
    });

    it('sollte 4 zu FULL konvertieren', () => {
      expect(sliderValueToFuelLevel(4)).toBe('FULL');
    });

    it('sollte ungültige Werte zu FULL konvertieren', () => {
      expect(sliderValueToFuelLevel(5)).toBe('FULL');
      expect(sliderValueToFuelLevel(-1)).toBe('FULL');
    });
  });

  describe('fuelLevelToSliderValue', () => {
    it('sollte EMPTY zu 0 konvertieren', () => {
      expect(fuelLevelToSliderValue('EMPTY')).toBe(0);
    });

    it('sollte 1/4 zu 1 konvertieren', () => {
      expect(fuelLevelToSliderValue('1/4')).toBe(1);
    });

    it('sollte 1/2 zu 2 konvertieren', () => {
      expect(fuelLevelToSliderValue('1/2')).toBe(2);
    });

    it('sollte 3/4 zu 3 konvertieren', () => {
      expect(fuelLevelToSliderValue('3/4')).toBe(3);
    });

    it('sollte FULL zu 4 konvertieren', () => {
      expect(fuelLevelToSliderValue('FULL')).toBe(4);
    });

    it('sollte ungültige Werte zu 4 konvertieren', () => {
      expect(fuelLevelToSliderValue('INVALID')).toBe(4);
    });
  });

  describe('default export', () => {
    it('sollte alle Funktionen exportieren', () => {
      expect(rentalService.performCheckOut).toBeDefined();
      expect(rentalService.performCheckIn).toBeDefined();
      expect(rentalService.validateCheckoutData).toBeDefined();
      expect(rentalService.validateCheckinData).toBeDefined();
      expect(rentalService.FUEL_LEVELS).toBeDefined();
      expect(rentalService.CLEANLINESS_OPTIONS).toBeDefined();
      expect(rentalService.sliderValueToFuelLevel).toBeDefined();
      expect(rentalService.fuelLevelToSliderValue).toBeDefined();
    });
  });
});
