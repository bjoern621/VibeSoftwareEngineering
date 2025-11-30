/**
 * authService Tests
 *
 * Unit-Tests für alle Authentication-Service-Funktionen
 */

import authService from './authService';
import apiClient, { TOKEN_STORAGE_KEY } from '../config/axiosConfig';

// Mock axios
jest.mock('../config/axiosConfig', () => {
  const mockAxios = {
    post: jest.fn(),
    get: jest.fn(),
    put: jest.fn(),
  };
  return {
    __esModule: true,
    default: mockAxios,
    TOKEN_STORAGE_KEY: 'rentacar_jwt_token',
  };
});

describe('authService', () => {
  // localStorage Mock
  let localStorageMock;

  beforeEach(() => {
    // localStorage Mock erstellen
    localStorageMock = (() => {
      let store = {};
      return {
        getItem: jest.fn((key) => store[key] || null),
        setItem: jest.fn((key, value) => {
          store[key] = value.toString();
        }),
        removeItem: jest.fn((key) => {
          delete store[key];
        }),
        clear: jest.fn(() => {
          store = {};
        }),
      };
    })();

    Object.defineProperty(window, 'localStorage', {
      value: localStorageMock,
      writable: true,
    });

    // Mocks zurücksetzen
    jest.clearAllMocks();
  });

  describe('register', () => {
    it('sollte erfolgreich einen Kunden registrieren', async () => {
      // Arrange
      const registrationData = {
        firstName: 'Max',
        lastName: 'Mustermann',
        email: 'max@example.com',
        password: 'SecurePass123!',
        phoneNumber: '+49123456789',
        driverLicenseNumber: 'DE123456789',
        address: {
          street: 'Hauptstraße 1',
          postalCode: '12345',
          city: 'Berlin',
          country: 'Deutschland',
        },
      };

      const mockResponse = {
        data: {
          message: 'Registrierung erfolgreich. Bitte bestätigen Sie Ihre E-Mail-Adresse.',
        },
      };

      apiClient.post.mockResolvedValue(mockResponse);

      // Act
      const result = await authService.register(registrationData);

      // Assert
      expect(apiClient.post).toHaveBeenCalledWith('/kunden/registrierung', registrationData);
      expect(result).toEqual(mockResponse.data);
    });

    it('sollte Fehler bei fehlerhaften Registrierungsdaten werfen', async () => {
      // Arrange
      const invalidData = {
        email: 'invalid-email',
      };

      const mockError = {
        response: {
          data: {
            message: 'Ungültige E-Mail-Adresse',
          },
        },
      };

      apiClient.post.mockRejectedValue(mockError);

      // Act & Assert
      await expect(authService.register(invalidData)).rejects.toThrow('Ungültige E-Mail-Adresse');
    });
  });

  describe('login', () => {
    it('sollte erfolgreich einloggen und Token speichern', async () => {
      // Arrange
      const email = 'max@example.com';
      const password = 'SecurePass123!';
      const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

      const mockResponse = {
        data: {
          token: mockToken,
          customerId: 1,
          email: email,
        },
      };

      apiClient.post.mockResolvedValue(mockResponse);

      // Act
      const result = await authService.login(email, password);

      // Assert
      expect(apiClient.post).toHaveBeenCalledWith('/kunden/login', { email, password });
      expect(localStorageMock.setItem).toHaveBeenCalledWith(TOKEN_STORAGE_KEY, mockToken);
      expect(result).toEqual(mockResponse.data);
    });

    it('sollte Fehler bei falschen Login-Daten werfen', async () => {
      // Arrange
      const email = 'wrong@example.com';
      const password = 'wrongpass';

      const mockError = {
        response: {
          data: {
            message: 'Ungültige E-Mail-Adresse oder Passwort',
          },
        },
      };

      apiClient.post.mockRejectedValue(mockError);

      // Act & Assert
      await expect(authService.login(email, password)).rejects.toThrow(
        'Ungültige E-Mail-Adresse oder Passwort'
      );
      expect(localStorageMock.setItem).not.toHaveBeenCalled();
    });
  });

  describe('logout', () => {
    it('sollte Token aus localStorage entfernen', () => {
      // Arrange
      localStorageMock.setItem(TOKEN_STORAGE_KEY, 'test-token');

      // Act
      authService.logout();

      // Assert
      expect(localStorageMock.removeItem).toHaveBeenCalledWith(TOKEN_STORAGE_KEY);
    });
  });

  describe('isAuthenticated', () => {
    it('sollte true zurückgeben, wenn Token vorhanden ist', () => {
      // Arrange
      localStorageMock.getItem.mockReturnValue('test-token');

      // Act
      const result = authService.isAuthenticated();

      // Assert
      expect(result).toBe(true);
      expect(localStorageMock.getItem).toHaveBeenCalledWith(TOKEN_STORAGE_KEY);
    });

    it('sollte false zurückgeben, wenn kein Token vorhanden ist', () => {
      // Arrange
      localStorageMock.getItem.mockReturnValue(null);

      // Act
      const result = authService.isAuthenticated();

      // Assert
      expect(result).toBe(false);
    });
  });

  describe('getToken', () => {
    it('sollte das Token aus localStorage zurückgeben', () => {
      // Arrange
      const mockToken = 'test-token';
      localStorageMock.getItem.mockReturnValue(mockToken);

      // Act
      const result = authService.getToken();

      // Assert
      expect(result).toBe(mockToken);
      expect(localStorageMock.getItem).toHaveBeenCalledWith(TOKEN_STORAGE_KEY);
    });
  });

  describe('verifyEmail', () => {
    it('sollte E-Mail erfolgreich verifizieren', async () => {
      // Arrange
      const token = 'verification-token-123';
      const mockResponse = {
        data: {
          message: 'E-Mail-Adresse erfolgreich verifiziert',
        },
      };

      apiClient.post.mockResolvedValue(mockResponse);

      // Act
      const result = await authService.verifyEmail(token);

      // Assert
      expect(apiClient.post).toHaveBeenCalledWith('/kunden/verify-email?token=verification-token-123');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('getProfile', () => {
    it('sollte Kundenprofil abrufen', async () => {
      // Arrange
      const mockProfile = {
        id: 1,
        firstName: 'Max',
        lastName: 'Mustermann',
        email: 'max@example.com',
        phoneNumber: '+49123456789',
      };

      const mockResponse = {
        data: mockProfile,
      };

      apiClient.get.mockResolvedValue(mockResponse);

      // Act
      const result = await authService.getProfile();

      // Assert
      expect(apiClient.get).toHaveBeenCalledWith('/kunden/profil');
      expect(result).toEqual(mockProfile);
    });
  });

  describe('updateProfile', () => {
    it('sollte Kundenprofil aktualisieren', async () => {
      // Arrange
      const profileData = {
        phoneNumber: '+49987654321',
        address: {
          street: 'Neue Straße 2',
          postalCode: '54321',
          city: 'München',
          country: 'Deutschland',
        },
      };

      const mockResponse = {
        data: {
          message: 'Profil erfolgreich aktualisiert',
        },
      };

      apiClient.put.mockResolvedValue(mockResponse);

      // Act
      const result = await authService.updateProfile(profileData);

      // Assert
      expect(apiClient.put).toHaveBeenCalledWith('/kunden/profil', profileData);
      expect(result).toEqual(mockResponse.data);
    });
  });
});
