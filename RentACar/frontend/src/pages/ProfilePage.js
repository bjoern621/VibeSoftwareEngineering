/**
 * ProfilePage - Kundenprofil anzeigen und bearbeiten
 * Ticket #112: Frontend für Kundenprofil
 * 
 * Features:
 * - Profildaten anzeigen (Anzeigemodus)
 * - Profildaten bearbeiten (Bearbeitungsmodus)
 * - Validierung für Pflichtfelder
 * - Read-only Felder: driverLicenseNumber, emailVerified, createdAt
 * - Loading-States beim Laden und Speichern
 * - Erfolgs-/Fehlermeldungen
 */

import React, { useState, useEffect } from 'react';
import authService from '../services/authService';
import AddressAutocomplete from '../components/AddressAutocomplete';

const ProfilePage = () => {
  // State für Profildaten
  const [profileData, setProfileData] = useState({
    id: null,
    firstName: '',
    lastName: '',
    street: '',
    postalCode: '',
    city: '',
    driverLicenseNumber: '',
    email: '',
    phoneNumber: '',
    emailVerified: false,
    createdAt: null,
  });

  // State für Bearbeitungsmodus
  const [editMode, setEditMode] = useState(false);
  const [editData, setEditData] = useState({});

  // State für Loading, Fehler und Erfolg
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [validationErrors, setValidationErrors] = useState({});

  /**
   * Profildaten beim Laden der Seite abrufen
   */
  useEffect(() => {
    loadProfile();
  }, []);

  /**
   * Profildaten vom Backend laden
   */
  const loadProfile = async () => {
    try {
      setLoading(true);
      setError('');
      const profile = await authService.getProfile();
      setProfileData(profile);
    } catch (err) {
      setError(err.message || 'Profil konnte nicht geladen werden');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Bearbeitungsmodus aktivieren
   */
  const handleEditClick = () => {
    setEditData({
      firstName: profileData.firstName,
      lastName: profileData.lastName,
      street: profileData.street,
      postalCode: profileData.postalCode,
      city: profileData.city,
      email: profileData.email,
      phoneNumber: profileData.phoneNumber || '',
    });
    setEditMode(true);
    setError('');
    setSuccess('');
    setValidationErrors({});
  };

  /**
   * Bearbeitungsmodus abbrechen
   */
  const handleCancelClick = () => {
    setEditMode(false);
    setEditData({});
    setValidationErrors({});
    setError('');
    setSuccess('');
  };

  /**
   * Validierung der Profildaten (Frontend)
   */
  const validateProfileData = () => {
    const errors = {};

    // Straße validieren
    if (!editData.street?.trim()) {
      errors.street = 'Straße ist erforderlich';
    }

    // PLZ validieren
    if (!editData.postalCode?.trim()) {
      errors.postalCode = 'Postleitzahl ist erforderlich';
    } else if (!/^\d{5}$/.test(editData.postalCode.trim())) {
      errors.postalCode = 'Postleitzahl muss 5 Ziffern enthalten';
    }

    // Stadt validieren
    if (!editData.city?.trim()) {
      errors.city = 'Stadt ist erforderlich';
    }

    // E-Mail validieren
    if (!editData.email?.trim()) {
      errors.email = 'E-Mail ist erforderlich';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editData.email.trim())) {
      errors.email = 'E-Mail-Format ist ungültig';
    }

    return errors;
  };

  /**
   * Profildaten speichern
   */
  const handleSaveClick = async (e) => {
    e.preventDefault();

    // Frontend-Validierung
    const errors = validateProfileData();
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      setError('Bitte überprüfen Sie Ihre Eingaben');
      return;
    }

    try {
      setSaving(true);
      setError('');
      setSuccess('');
      setValidationErrors({});

      // Backend-Request
      const updatedProfile = await authService.updateProfile(editData);

      // Profildaten aktualisieren
      setProfileData(updatedProfile);
      setEditMode(false);
      
      // Erfolgsmelding mit Hinweis bei E-Mail-Änderung
      if (editData.email !== profileData.email) {
        setSuccess('Profil erfolgreich aktualisiert. Bitte verifizieren Sie Ihre neue E-Mail-Adresse über den Link, den wir Ihnen zugesendet haben.');
      } else {
        setSuccess('Profil erfolgreich aktualisiert');
      }
    } catch (err) {
      setError(err.message || 'Profil konnte nicht gespeichert werden');
    } finally {
      setSaving(false);
    }
  };

  /**
   * Datum formatieren (deutsch)
   */
  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  /**
   * Input-Handler für Formularfelder
   */
  const handleInputChange = (field, value) => {
    setEditData({
      ...editData,
      [field]: value,
    });
    // Validierungsfehler für das Feld entfernen
    if (validationErrors[field]) {
      setValidationErrors({
        ...validationErrors,
        [field]: undefined,
      });
    }
  };

  /**
   * Handler für Adress-Änderungen (für AddressAutocomplete)
   */
  const handleAddressChange = (addressFields) => {
    setEditData({
      ...editData,
      ...addressFields,
    });
    // Entferne Validierungsfehler für geänderte Adressfelder
    const newValidationErrors = { ...validationErrors };
    Object.keys(addressFields).forEach((key) => {
      if (newValidationErrors[key]) {
        delete newValidationErrors[key];
      }
    });
    setValidationErrors(newValidationErrors);
  };

  // Loading-Zustand
  if (loading) {
    return (
      <div className="min-h-screen bg-background-light flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-gray-600">Profil wird geladen...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background-light">
      <main className="flex-grow">
        <div className="container mx-auto px-4 py-8 md:py-12">
          <div className="max-w-3xl mx-auto">
            {/* Page Heading */}
            <div className="flex flex-wrap justify-between items-center gap-4 mb-8">
              <h1 className="text-4xl font-black tracking-tighter">Mein Profil</h1>
              {!editMode && (
                <button
                  onClick={handleEditClick}
                  className="flex items-center gap-2 min-w-[84px] cursor-pointer justify-center overflow-hidden rounded-lg h-10 px-4 bg-primary text-white text-sm font-bold shadow-sm hover:opacity-90 transition-colors"
                >
                  <span className="material-symbols-outlined text-lg">edit</span>
                  <span>Bearbeiten</span>
                </button>
              )}
            </div>

            {/* Erfolgs-/Fehlermeldungen */}
            {error && (
              <div className="p-4 rounded-lg bg-red-50 text-red-800 mb-6">
                {error}
              </div>
            )}
            {success && (
              <div className="p-4 rounded-lg bg-green-50 text-green-800 mb-6">
                {success}
              </div>
            )}

            {/* Profil-Card */}
            <div className="bg-white rounded-xl shadow-md p-6 md:p-8">
              {/* Anzeigemodus */}
              {!editMode && (
                <div className="space-y-6">
                  {/* Persönliche Daten */}
                  <div>
                    <h2 className="text-xl font-bold text-gray-900 mb-4">Persönliche Daten</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium text-gray-500">Vorname</p>
                        <p className="text-base text-gray-900 mt-1">{profileData.firstName}</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">Nachname</p>
                        <p className="text-base text-gray-900 mt-1">{profileData.lastName}</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">E-Mail</p>
                        <p className="text-base text-gray-900 mt-1">{profileData.email}</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">Telefon</p>
                        <p className="text-base text-gray-900 mt-1">
                          {profileData.phoneNumber || '-'}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Adresse */}
                  <div className="border-t pt-6">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">Adresse</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="md:col-span-2">
                        <p className="text-sm font-medium text-gray-500">Straße</p>
                        <p className="text-base text-gray-900 mt-1">{profileData.street}</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">Postleitzahl</p>
                        <p className="text-base text-gray-900 mt-1">{profileData.postalCode}</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">Stadt</p>
                        <p className="text-base text-gray-900 mt-1">{profileData.city}</p>
                      </div>
                    </div>
                  </div>

                  {/* Weitere Informationen (Read-only) */}
                  <div className="border-t pt-6">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">
                      Weitere Informationen
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium text-gray-500">Führerscheinnummer</p>
                        <div className="mt-1 p-3 bg-gray-100 rounded-lg">
                          <p className="text-base text-gray-700">
                            {profileData.driverLicenseNumber}
                          </p>
                          <p className="text-xs text-gray-500 mt-1">Nicht änderbar</p>
                        </div>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">E-Mail-Verifizierung</p>
                        <div className="mt-1 p-3 bg-gray-100 rounded-lg">
                          <span
                            className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-sm font-medium ${
                              profileData.emailVerified
                                ? 'bg-green-100 text-green-800'
                                : 'bg-yellow-100 text-yellow-800'
                            }`}
                          >
                            {profileData.emailVerified ? (
                              <>
                                <span className="material-symbols-outlined text-base">
                                  check_circle
                                </span>
                                Verifiziert
                              </>
                            ) : (
                              <>
                                <span className="material-symbols-outlined text-base">
                                  pending
                                </span>
                                Ausstehend
                              </>
                            )}
                          </span>
                        </div>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">Registriert seit</p>
                        <div className="mt-1 p-3 bg-gray-100 rounded-lg">
                          <p className="text-base text-gray-700">
                            {formatDate(profileData.createdAt)}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Bearbeitungsmodus */}
              {editMode && (
                <form onSubmit={handleSaveClick} className="space-y-6">
                  {/* Persönliche Daten */}
                  <div>
                    <h2 className="text-xl font-bold text-gray-900 mb-4">Persönliche Daten</h2>
                    <p className="text-sm text-gray-500 mb-4">
                      Vor- und Nachname können aus Sicherheitsgründen nicht geändert werden.
                    </p>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium text-gray-500 pb-2">Vorname</p>
                        <div className="p-3 bg-gray-100 rounded-lg">
                          <p className="text-base text-gray-700">{profileData.firstName}</p>
                        </div>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500 pb-2">Nachname</p>
                        <div className="p-3 bg-gray-100 rounded-lg">
                          <p className="text-base text-gray-700">{profileData.lastName}</p>
                        </div>
                      </div>
                      <label className="flex flex-col">
                        <p className="text-sm font-medium text-gray-700 pb-2">E-Mail *</p>
                        <input
                          className={`form-input rounded-lg border-gray-300 h-12 px-3 ${
                            validationErrors.email ? 'border-red-500' : ''
                          }`}
                          type="email"
                          value={editData.email || ''}
                          onChange={(e) => handleInputChange('email', e.target.value)}
                          required
                        />
                        {validationErrors.email && (
                          <p className="text-red-500 text-xs mt-1">{validationErrors.email}</p>
                        )}
                        {editData.email !== profileData.email && (
                          <p className="text-yellow-600 text-xs mt-1">
                            ⚠️ Bei E-Mail-Änderung ist eine erneute Verifizierung erforderlich
                          </p>
                        )}
                      </label>
                      <label className="flex flex-col">
                        <p className="text-sm font-medium text-gray-700 pb-2">Telefon</p>
                        <input
                          className="form-input rounded-lg border-gray-300 h-12 px-3"
                          type="tel"
                          value={editData.phoneNumber || ''}
                          onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                        />
                      </label>
                    </div>
                  </div>

                  {/* Adresse */}
                  <div className="border-t pt-6">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">Adresse</h2>
                    <AddressAutocomplete
                      street={editData.street || ''}
                      postalCode={editData.postalCode || ''}
                      city={editData.city || ''}
                      onAddressChange={handleAddressChange}
                      validationErrors={validationErrors}
                    />
                  </div>

                  {/* Read-only Felder (Info) */}
                  <div className="border-t pt-6">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">
                      Weitere Informationen
                    </h2>
                    <p className="text-sm text-gray-500 mb-4">
                      Die folgenden Felder können nicht bearbeitet werden:
                    </p>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium text-gray-500">Führerscheinnummer</p>
                        <div className="mt-1 p-3 bg-gray-100 rounded-lg">
                          <p className="text-base text-gray-700">
                            {profileData.driverLicenseNumber}
                          </p>
                        </div>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-500">E-Mail-Verifizierung</p>
                        <div className="mt-1 p-3 bg-gray-100 rounded-lg">
                          <span
                            className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-sm font-medium ${
                              profileData.emailVerified
                                ? 'bg-green-100 text-green-800'
                                : 'bg-yellow-100 text-yellow-800'
                            }`}
                          >
                            {profileData.emailVerified ? 'Verifiziert' : 'Ausstehend'}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="flex gap-4 border-t pt-6">
                    <button
                      type="submit"
                      disabled={saving}
                      className="flex-1 h-12 rounded-lg bg-primary text-white font-bold hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      {saving ? 'Wird gespeichert...' : 'Speichern'}
                    </button>
                    <button
                      type="button"
                      onClick={handleCancelClick}
                      disabled={saving}
                      className="flex-1 h-12 rounded-lg bg-gray-200 text-gray-700 font-bold hover:bg-gray-300 disabled:opacity-50 transition-colors"
                    >
                      Abbrechen
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default ProfilePage;
