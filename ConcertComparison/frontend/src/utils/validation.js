/**
 * Validation utilities for forms
 */

/**
 * Validate email format
 * @param {string} email - Email to validate
 * @returns {Object} - { isValid: boolean, error: string }
 */
export const validateEmail = (email) => {
  if (!email) {
    return { isValid: false, error: 'Email darf nicht leer sein' };
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return { isValid: false, error: 'Ungültige Email-Adresse' };
  }
  
  return { isValid: true, error: '' };
};

/**
 * Validate password (minimum 8 characters)
 * @param {string} password - Password to validate
 * @returns {Object} - { isValid: boolean, error: string }
 */
export const validatePassword = (password) => {
  if (!password) {
    return { isValid: false, error: 'Passwort darf nicht leer sein' };
  }
  
  if (password.length < 8) {
    return { isValid: false, error: 'Passwort muss mindestens 8 Zeichen lang sein' };
  }
  
  return { isValid: true, error: '' };
};

/**
 * Validate password match
 * @param {string} password - Original password
 * @param {string} confirmPassword - Confirmation password
 * @returns {Object} - { isValid: boolean, error: string }
 */
export const validatePasswordMatch = (password, confirmPassword) => {
  if (!confirmPassword) {
    return { isValid: false, error: 'Passwort-Bestätigung darf nicht leer sein' };
  }
  
  if (password !== confirmPassword) {
    return { isValid: false, error: 'Passwörter stimmen nicht überein' };
  }
  
  return { isValid: true, error: '' };
};

/**
 * Calculate password strength
 * @param {string} password - Password to check
 * @returns {Object} - { strength: 'weak'|'medium'|'strong', percentage: number }
 */
export const calculatePasswordStrength = (password) => {
  if (!password) {
    return { strength: 'weak', percentage: 0 };
  }
  
  let strength = 0;
  
  // Length check
  if (password.length >= 8) strength += 25;
  if (password.length >= 12) strength += 25;
  
  // Character variety
  if (/[a-z]/.test(password)) strength += 12.5;
  if (/[A-Z]/.test(password)) strength += 12.5;
  if (/[0-9]/.test(password)) strength += 12.5;
  if (/[^a-zA-Z0-9]/.test(password)) strength += 12.5;
  
  let level = 'weak';
  if (strength >= 50 && strength < 75) {
    level = 'medium';
  } else if (strength >= 75) {
    level = 'strong';
  }
  
  return { strength: level, percentage: strength };
};

/**
 * Validate required field
 * @param {string} value - Value to validate
 * @param {string} fieldName - Name of the field for error message
 * @returns {Object} - { isValid: boolean, error: string }
 */
export const validateRequired = (value, fieldName = 'Feld') => {
  if (!value || value.trim() === '') {
    return { isValid: false, error: `${fieldName} darf nicht leer sein` };
  }
  
  return { isValid: true, error: '' };
};
