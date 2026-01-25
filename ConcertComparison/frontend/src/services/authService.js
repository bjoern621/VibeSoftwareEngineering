import api from './api';

/**
 * Auth Service
 * Handles all authentication-related API calls and token management
 */
const authService = {
  /**
   * Register a new user
   * @param {Object} userData - User registration data
   * @param {string} userData.email - User email
   * @param {string} userData.password - User password
   * @param {string} userData.firstName - User first name
   * @param {string} userData.lastName - User last name
   * @returns {Promise<Object>} User profile response
   */
  register: async (userData) => {
    try {
      const response = await api.post('/auth/register', userData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Login user and save JWT token
   * @param {Object} credentials - Login credentials
   * @param {string} credentials.email - User email
   * @param {string} credentials.password - User password
   * @returns {Promise<Object>} Login response with token
   */
  login: async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials);
      
      // Save token to localStorage
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('userEmail', response.data.email);
        localStorage.setItem('userRole', response.data.role);
      }
      
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Logout user and clear token
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
  },

  /**
   * Get current user profile
   * @returns {Promise<Object>} User profile
   */
  getProfile: async () => {
    try {
      const response = await api.get('/users/profile');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Check if user is authenticated
   * @returns {boolean} True if token exists
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  /**
   * Get stored token
   * @returns {string|null} JWT token
   */
  getToken: () => {
    return localStorage.getItem('token');
  },

  /**
   * Get stored user email
   * @returns {string|null} User email
   */
  getUserEmail: () => {
    return localStorage.getItem('userEmail');
  },

  /**
   * Get stored user role
   * @returns {string|null} User role
   */
  getUserRole: () => {
    return localStorage.getItem('userRole');
  }
};

export default authService;
