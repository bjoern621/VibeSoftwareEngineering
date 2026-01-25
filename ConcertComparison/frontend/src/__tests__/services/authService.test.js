import authService from '../../services/authService';
import api from '../../services/api';

// Mock api service
jest.mock('../../services/api');

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  describe('register', () => {
    test('should register user successfully', async () => {
      const mockUserData = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'Test',
        lastName: 'User',
      };

      const mockResponse = {
        data: {
          id: 1,
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User',
        },
      };

      api.post.mockResolvedValue(mockResponse);

      const result = await authService.register(mockUserData);

      expect(api.post).toHaveBeenCalledWith('/auth/register', mockUserData);
      expect(result).toEqual(mockResponse.data);
    });

    test('should throw error on registration failure', async () => {
      const mockUserData = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'Test',
        lastName: 'User',
      };

      const mockError = new Error('Email already exists');
      api.post.mockRejectedValue(mockError);

      await expect(authService.register(mockUserData)).rejects.toThrow('Email already exists');
    });
  });

  describe('login', () => {
    test('should login user and save token', async () => {
      const mockCredentials = {
        email: 'test@example.com',
        password: 'password123',
      };

      const mockResponse = {
        data: {
          token: 'fake-jwt-token',
          email: 'test@example.com',
          role: 'USER',
        },
      };

      api.post.mockResolvedValue(mockResponse);

      const result = await authService.login(mockCredentials);

      expect(api.post).toHaveBeenCalledWith('/auth/login', mockCredentials);
      expect(result).toEqual(mockResponse.data);
      expect(localStorage.getItem('token')).toBe('fake-jwt-token');
      expect(localStorage.getItem('userEmail')).toBe('test@example.com');
      expect(localStorage.getItem('userRole')).toBe('USER');
    });

    test('should throw error on login failure', async () => {
      const mockCredentials = {
        email: 'test@example.com',
        password: 'wrongpassword',
      };

      const mockError = new Error('Invalid credentials');
      api.post.mockRejectedValue(mockError);

      await expect(authService.login(mockCredentials)).rejects.toThrow('Invalid credentials');
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  describe('logout', () => {
    test('should clear all auth data from localStorage', () => {
      localStorage.setItem('token', 'fake-token');
      localStorage.setItem('userEmail', 'test@example.com');
      localStorage.setItem('userRole', 'USER');

      authService.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('userEmail')).toBeNull();
      expect(localStorage.getItem('userRole')).toBeNull();
    });
  });

  describe('getProfile', () => {
    test('should get user profile successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User',
          role: 'USER',
        },
      };

      api.get.mockResolvedValue(mockResponse);

      const result = await authService.getProfile();

      expect(api.get).toHaveBeenCalledWith('/users/profile');
      expect(result).toEqual(mockResponse.data);
    });

    test('should throw error on profile fetch failure', async () => {
      const mockError = new Error('Unauthorized');
      api.get.mockRejectedValue(mockError);

      await expect(authService.getProfile()).rejects.toThrow('Unauthorized');
    });
  });

  describe('isAuthenticated', () => {
    test('should return true when token exists', () => {
      localStorage.setItem('token', 'fake-token');
      expect(authService.isAuthenticated()).toBe(true);
    });

    test('should return false when token does not exist', () => {
      expect(authService.isAuthenticated()).toBe(false);
    });
  });

  describe('getToken', () => {
    test('should return token from localStorage', () => {
      localStorage.setItem('token', 'fake-token');
      expect(authService.getToken()).toBe('fake-token');
    });

    test('should return null when no token exists', () => {
      expect(authService.getToken()).toBeNull();
    });
  });

  describe('getUserEmail', () => {
    test('should return user email from localStorage', () => {
      localStorage.setItem('userEmail', 'test@example.com');
      expect(authService.getUserEmail()).toBe('test@example.com');
    });

    test('should return null when no email exists', () => {
      expect(authService.getUserEmail()).toBeNull();
    });
  });

  describe('getUserRole', () => {
    test('should return user role from localStorage', () => {
      localStorage.setItem('userRole', 'USER');
      expect(authService.getUserRole()).toBe('USER');
    });

    test('should return null when no role exists', () => {
      expect(authService.getUserRole()).toBeNull();
    });
  });
});
