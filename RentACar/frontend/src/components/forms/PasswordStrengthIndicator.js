/**
 * PasswordStrengthIndicator - Komponente zur Anzeige der Passwort-Stärke
 * 
 * Zeigt visuelles Feedback zur Passwort-Qualität
 */

import React from 'react';
import {
  calculatePasswordStrength,
  getPasswordStrengthText,
  getPasswordStrengthColor,
} from '../../utils/validationRules';

const PasswordStrengthIndicator = ({ password }) => {
  if (!password) return null;

  const strength = calculatePasswordStrength(password);
  const strengthText = getPasswordStrengthText(strength);
  const strengthColor = getPasswordStrengthColor(strength);

  // Farben für die Balken
  const colorClasses = {
    red: 'bg-red-500',
    orange: 'bg-orange-500',
    yellow: 'bg-yellow-500',
    green: 'bg-green-500',
  };

  // Textfarben
  const textColorClasses = {
    red: 'text-red-600',
    orange: 'text-orange-600',
    yellow: 'text-yellow-600',
    green: 'text-green-600',
  };

  return (
    <div className="mt-2">
      {/* Balken */}
      <div className="flex gap-1 mb-1">
        {[0, 1, 2, 3].map((index) => (
          <div
            key={index}
            className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
              index <= strength ? colorClasses[strengthColor] : 'bg-gray-200'
            }`}
          />
        ))}
      </div>

      {/* Text */}
      <p className={`text-xs font-medium ${textColorClasses[strengthColor]}`}>
        Passwort-Stärke: {strengthText}
      </p>

      {/* Anforderungen */}
      {strength < 3 && (
        <ul className="mt-2 space-y-1 text-xs text-gray-600">
          {password.length < 8 && (
            <li className="flex items-center gap-1">
              <span className="material-symbols-outlined text-gray-400" style={{ fontSize: '14px' }}>
                circle
              </span>
              Mindestens 8 Zeichen
            </li>
          )}
          {!/[a-z]/.test(password) && (
            <li className="flex items-center gap-1">
              <span className="material-symbols-outlined text-gray-400" style={{ fontSize: '14px' }}>
                circle
              </span>
              Mindestens ein Kleinbuchstabe
            </li>
          )}
          {!/[A-Z]/.test(password) && (
            <li className="flex items-center gap-1">
              <span className="material-symbols-outlined text-gray-400" style={{ fontSize: '14px' }}>
                circle
              </span>
              Mindestens ein Großbuchstabe
            </li>
          )}
          {!/\d/.test(password) && (
            <li className="flex items-center gap-1">
              <span className="material-symbols-outlined text-gray-400" style={{ fontSize: '14px' }}>
                circle
              </span>
              Mindestens eine Ziffer
            </li>
          )}
          {!/[@$!%*?&]/.test(password) && (
            <li className="flex items-center gap-1">
              <span className="material-symbols-outlined text-gray-400" style={{ fontSize: '14px' }}>
                circle
              </span>
              Mindestens ein Sonderzeichen (@$!%*?&)
            </li>
          )}
        </ul>
      )}
    </div>
  );
};

export default PasswordStrengthIndicator;
