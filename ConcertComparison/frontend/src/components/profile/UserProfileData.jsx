import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';

/**
 * UserProfileData Component
 * 
 * Displays user's personal information.
 * Name and Email are read-only, phone number can be edited.
 * 
 * @returns {React.ReactElement}
 */
const UserProfileData = () => {
  const { user } = useAuth();

  // Default values if user data is not available
  const userData = {
    firstName: user?.firstName || 'Max',
    lastName: user?.lastName || 'Mustermann',
    email: user?.email || 'user@example.com',
    phone: user?.phone || '',
    memberSince: user?.createdAt || '2023',
  };

  const [phone, setPhone] = useState(userData.phone);
  const [isEditingPhone, setIsEditingPhone] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const handleSavePhone = async () => {
    setIsSaving(true);
    try {
      // TODO: API Call zum Speichern der Telefonnummer
      // await api.put('/users/me', { phone });
      
      // Simuliere API-Call
      await new Promise(resolve => setTimeout(resolve, 500));
      
      setIsEditingPhone(false);
      // TODO: Update user context
    } catch (error) {
      console.error('Error saving phone:', error);
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancelPhone = () => {
    setPhone(userData.phone);
    setIsEditingPhone(false);
  };

  const handleDeletePhone = async () => {
    if (window.confirm('Möchten Sie Ihre Telefonnummer wirklich löschen?')) {
      setIsSaving(true);
      try {
        // TODO: API Call zum Löschen der Telefonnummer
        // await api.put('/users/me', { phone: null });
        
        // Simuliere API-Call
        await new Promise(resolve => setTimeout(resolve, 500));
        
        setPhone('');
        setIsEditingPhone(false);
        // TODO: Update user context
      } catch (error) {
        console.error('Error deleting phone:', error);
      } finally {
        setIsSaving(false);
      }
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h2 className="text-2xl font-bold text-text-primary dark:text-white">
          Persönliche Daten
        </h2>
        <p className="text-text-secondary dark:text-gray-400 text-sm mt-1">
          Verwalten Sie Ihre persönlichen Informationen und Kontoeinstellungen.
        </p>
      </div>

      {/* Data Form (Read Only) */}
      <div className="space-y-6">
        {/* Identity Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* First Name */}
          <div className="space-y-2">
            <label className="block text-sm font-medium text-text-secondary dark:text-gray-400">
              Vorname
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span className="material-symbols-outlined text-gray-400 text-[20px]">
                  person
                </span>
              </div>
              <input
                className="block w-full rounded-lg border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50 text-text-primary dark:text-white py-3 pl-10 pr-4 focus:ring-0 focus:border-gray-300 dark:focus:border-gray-600 cursor-default"
                readOnly
                type="text"
                value={userData.firstName}
              />
            </div>
          </div>

          {/* Last Name */}
          <div className="space-y-2">
            <label className="block text-sm font-medium text-text-secondary dark:text-gray-400">
              Nachname
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span className="material-symbols-outlined text-gray-400 text-[20px]">
                  person
                </span>
              </div>
              <input
                className="block w-full rounded-lg border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50 text-text-primary dark:text-white py-3 pl-10 pr-4 focus:ring-0 focus:border-gray-300 dark:focus:border-gray-600 cursor-default"
                readOnly
                type="text"
                value={userData.lastName}
              />
            </div>
          </div>

          {/* Email */}
          <div className="space-y-2">
            <label className="block text-sm font-medium text-text-secondary dark:text-gray-400">
              E-Mail-Adresse
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span className="material-symbols-outlined text-gray-400 text-[20px]">
                  mail
                </span>
              </div>
              <input
                className="block w-full rounded-lg border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50 text-text-primary dark:text-white py-3 pl-10 pr-4 focus:ring-0 focus:border-gray-300 dark:focus:border-gray-600 cursor-default"
                readOnly
                type="email"
                value={userData.email}
              />
            </div>
          </div>

          {/* Phone - Editable */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="block text-sm font-medium text-text-secondary dark:text-gray-400">
                Telefonnummer {!phone && <span className="text-xs">(Optional)</span>}
              </label>
              {!isEditingPhone && (
                <button
                  onClick={() => setIsEditingPhone(true)}
                  className="text-xs text-primary hover:text-primary-dark font-medium"
                >
                  {phone ? 'Bearbeiten' : 'Hinzufügen'}
                </button>
              )}
            </div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span className="material-symbols-outlined text-gray-400 text-[20px]">
                  call
                </span>
              </div>
              <input
                className={`block w-full rounded-lg border-gray-200 dark:border-gray-700 py-3 pl-10 pr-4 focus:ring-1 focus:ring-primary focus:border-primary text-text-primary dark:text-white ${
                  isEditingPhone
                    ? 'bg-white dark:bg-gray-800'
                    : 'bg-gray-50 dark:bg-gray-800/50 cursor-default'
                }`}
                readOnly={!isEditingPhone}
                type="tel"
                placeholder="+49 123 456789"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
            </div>
            {isEditingPhone && (
              <div className="flex gap-2 mt-2">
                <button
                  onClick={handleSavePhone}
                  disabled={isSaving}
                  className="px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-dark transition-colors disabled:opacity-50"
                >
                  {isSaving ? 'Speichern...' : 'Speichern'}
                </button>
                <button
                  onClick={handleCancelPhone}
                  disabled={isSaving}
                  className="px-4 py-2 bg-gray-200 dark:bg-gray-700 text-text-primary dark:text-white rounded-lg text-sm font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors disabled:opacity-50"
                >
                  Abbrechen
                </button>
                {phone && (
                  <button
                    onClick={handleDeletePhone}
                    disabled={isSaving}
                    className="px-4 py-2 bg-red-100 text-red-600 rounded-lg text-sm font-medium hover:bg-red-200 transition-colors disabled:opacity-50"
                  >
                    Löschen
                  </button>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Info Box */}
        <div className="mt-6 p-4 rounded-lg bg-blue-50 dark:bg-blue-900/10 border border-blue-100 dark:border-blue-900/30 flex gap-3">
          <span className="material-symbols-outlined text-primary text-[24px]">
            info
          </span>
          <p className="text-sm text-text-secondary dark:text-gray-300">
            Name und E-Mail-Adresse können aus Sicherheitsgründen nicht geändert werden. Bei Fragen kontaktieren Sie bitte den Support.
          </p>
        </div>
      </div>
    </div>
  );
};

export default UserProfileData;
