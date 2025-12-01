import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import bookingService from '../services/bookingService';
import rentalService, {
  FUEL_LEVELS,
  CLEANLINESS_OPTIONS,
  sliderValueToFuelLevel,
  validateCheckoutData,
  validateCheckinData,
} from '../services/rentalService';
import {
  createDamageReport,
  validateDamageReport,
  fileToBase64,
} from '../services/damageReportService';

const CheckInOutPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Process type: 'checkout' or 'checkin'
  const [processType, setProcessType] = useState('checkout');

  // Search state
  const [searchTerm, setSearchTerm] = useState('');
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState(null);

  // Selected booking
  const [selectedBooking, setSelectedBooking] = useState(null);

  // Form state
  const [mileage, setMileage] = useState('');
  const [fuelSliderValue, setFuelSliderValue] = useState(4);
  const [cleanliness, setCleanliness] = useState('CLEAN');
  const [damagesDescription, setDamagesDescription] = useState('');

  // Damage report state (for check-in)
  const [newDamageDescription, setNewDamageDescription] = useState('');
  const [newDamageEstimatedCost, setNewDamageEstimatedCost] = useState('');
  const [damagePhotos, setDamagePhotos] = useState([]);
  const [existingDamages, setExistingDamages] = useState([]);

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [validationErrors, setValidationErrors] = useState([]);

  // Check authorization
  useEffect(() => {
    if (!user || (user.role !== 'EMPLOYEE' && user.role !== 'ADMIN')) {
      navigate('/login');
    }
  }, [user, navigate]);

  // Search for booking by ID
  const handleSearch = useCallback(async () => {
    if (!searchTerm.trim()) {
      setSearchError('Bitte geben Sie eine Buchungsnummer ein.');
      return;
    }

    setSearchLoading(true);
    setSearchError(null);
    setSelectedBooking(null);

    try {
      const booking = await bookingService.getBookingById(searchTerm.trim());

      // Check if booking status is appropriate
      if (processType === 'checkout' && booking.status !== 'CONFIRMED') {
        setSearchError(
          `Buchung #${booking.buchungsnummer} ist nicht für Check-out bereit. Status: ${booking.status}`
        );
        return;
      }
      if (processType === 'checkin' && booking.status !== 'ACTIVE') {
        setSearchError(
          `Buchung #${booking.buchungsnummer} ist nicht für Check-in bereit. Status: ${booking.status}`
        );
        return;
      }

      setSelectedBooking(booking);
      // Set initial mileage from vehicle if available
      if (booking.fahrzeug?.kilometerstand) {
        setMileage(booking.fahrzeug.kilometerstand.toString());
      }
    } catch (err) {
      setSearchError(err.message || 'Buchung nicht gefunden.');
    } finally {
      setSearchLoading(false);
    }
  }, [searchTerm, processType]);

  // Handle photo upload
  const handlePhotoUpload = async (event) => {
    const files = Array.from(event.target.files);
    const newPhotos = [];

    for (const file of files) {
      try {
        const base64 = await fileToBase64(file);
        newPhotos.push({
          name: file.name,
          data: base64,
        });
      } catch (err) {
        console.error('Fehler beim Hochladen:', err);
      }
    }

    setDamagePhotos((prev) => [...prev, ...newPhotos]);
  };

  // Remove photo
  const removePhoto = (index) => {
    setDamagePhotos((prev) => prev.filter((_, i) => i !== index));
  };

  // Add damage to list
  const addDamage = () => {
    if (!newDamageDescription.trim()) {
      setValidationErrors(['Bitte geben Sie eine Schadensbeschreibung ein.']);
      return;
    }

    const damage = {
      description: newDamageDescription,
      estimatedCost: parseFloat(newDamageEstimatedCost) || 0,
      photos: damagePhotos.map((p) => p.data),
    };

    const validation = validateDamageReport(damage);
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      return;
    }

    setExistingDamages((prev) => [...prev, damage]);
    setNewDamageDescription('');
    setNewDamageEstimatedCost('');
    setDamagePhotos([]);
    setValidationErrors([]);
  };

  // Remove damage from list
  const removeDamage = (index) => {
    setExistingDamages((prev) => prev.filter((_, i) => i !== index));
  };

  // Submit check-out or check-in
  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    setSuccessMessage(null);
    setValidationErrors([]);

    const fuelLevel = sliderValueToFuelLevel(fuelSliderValue);
    const formData = {
      mileage: parseInt(mileage, 10),
      fuelLevel,
      cleanliness,
      damagesDescription: damagesDescription || null,
    };

    // Validate
    const validation =
      processType === 'checkout'
        ? validateCheckoutData(formData, selectedBooking?.fahrzeug?.kilometerstand || 0)
        : validateCheckinData(formData, selectedBooking?.checkoutMileage || 0);

    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setLoading(false);
      return;
    }

    try {
      if (processType === 'checkout') {
        await rentalService.performCheckOut(selectedBooking.buchungsnummer, formData);
        setSuccessMessage(
          `Check-out für Buchung #${selectedBooking.buchungsnummer} erfolgreich durchgeführt!`
        );
      } else {
        // First perform check-in
        await rentalService.performCheckIn(selectedBooking.buchungsnummer, formData);

        // Then create damage reports if any
        for (const damage of existingDamages) {
          try {
            await createDamageReport(selectedBooking.buchungsnummer, damage);
          } catch (dmgErr) {
            console.error('Fehler beim Erstellen des Schadensberichts:', dmgErr);
          }
        }

        setSuccessMessage(
          `Check-in für Buchung #${selectedBooking.buchungsnummer} erfolgreich durchgeführt!`
        );
      }

      // Reset form
      setSelectedBooking(null);
      setSearchTerm('');
      setMileage('');
      setFuelSliderValue(4);
      setCleanliness('CLEAN');
      setDamagesDescription('');
      setExistingDamages([]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Calculate driven kilometers (for check-in)
  const drivenKm =
    selectedBooking?.checkoutMileage && mileage
      ? parseInt(mileage, 10) - selectedBooking.checkoutMileage
      : null;

  // Format date
  const formatDateTime = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="relative flex min-h-screen w-full">
      {/* Sidebar */}
      <aside className="flex w-64 flex-col gap-y-6 border-r border-gray-200 p-4 sticky top-0 h-screen bg-gray-50">
        <div className="flex items-center gap-3">
          <div className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10 bg-primary flex items-center justify-center text-white font-bold">
            {user?.firstName?.charAt(0) || 'U'}
          </div>
          <div className="flex flex-col">
            <h1 className="text-base font-medium">
              {user?.firstName} {user?.lastName}
            </h1>
            <p className="text-gray-600 text-sm font-normal">
              {user?.role === 'ADMIN' ? 'Administrator' : 'Mitarbeiter'}
            </p>
          </div>
        </div>

        <nav className="flex flex-col gap-2 flex-1">
          <a href="/" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">home</span>
            <p className="text-sm font-medium">Startseite</p>
          </a>
          <a
            href="/employee/bookings"
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200"
          >
            <span className="material-symbols-outlined">calendar_month</span>
            <p className="text-sm font-medium">Buchungsverwaltung</p>
          </a>
          <a
            href="/employee/vehicles"
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200"
          >
            <span className="material-symbols-outlined">directions_car</span>
            <p className="text-sm font-medium">Fahrzeugverwaltung</p>
          </a>
          <a
            href="/employee/check-in-out"
            className="flex items-center gap-3 px-3 py-2 rounded-lg bg-primary/20 text-primary"
          >
            <span className="material-symbols-outlined">swap_horiz</span>
            <p className="text-sm font-medium">Check-in/out</p>
          </a>
        </nav>

        <div className="flex flex-col gap-1">
          <button
            onClick={logout}
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200 w-full text-left"
          >
            <span className="material-symbols-outlined">logout</span>
            <p className="text-sm font-medium">Abmelden</p>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8 bg-background-light">
        <div className="mx-auto max-w-7xl">
          {/* Error/Success Messages */}
          {error && (
            <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-lg flex justify-between items-center">
              <span>{error}</span>
              <button onClick={() => setError(null)} className="text-red-700 hover:text-red-900">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
          )}

          {successMessage && (
            <div className="mb-4 p-4 bg-green-100 text-green-700 rounded-lg flex justify-between items-center">
              <span>{successMessage}</span>
              <button
                onClick={() => setSuccessMessage(null)}
                className="text-green-700 hover:text-green-900"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
          )}

          {validationErrors.length > 0 && (
            <div className="mb-4 p-4 bg-yellow-100 text-yellow-700 rounded-lg">
              <ul className="list-disc list-inside">
                {validationErrors.map((err, i) => (
                  <li key={i}>{err}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Page Heading */}
          <header className="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div className="flex flex-col">
              <h1 className="text-4xl font-black tracking-tighter">
                Fahrzeug-Protokoll: Check-in / Check-out
              </h1>
              <p className="text-gray-600 text-base">
                Verwalten Sie den Fahrzeugzustand bei Übergabe und Rücknahme.
              </p>
            </div>
          </header>

          {/* Process Type Toggle */}
          <div className="flex mb-8">
            <div className="flex h-10 w-full max-w-sm items-center justify-center rounded-lg bg-gray-200 p-1 border border-gray-300">
              <label
                className={`flex cursor-pointer h-full grow items-center justify-center overflow-hidden rounded-lg px-2 transition-colors ${processType === 'checkout' ? 'bg-white shadow-sm text-gray-900' : 'text-primary'}`}
              >
                <span className="truncate">Check-out</span>
                <input
                  checked={processType === 'checkout'}
                  onChange={() => {
                    setProcessType('checkout');
                    setSelectedBooking(null);
                    setSearchTerm('');
                  }}
                  className="invisible w-0"
                  name="process_type"
                  type="radio"
                  value="checkout"
                />
              </label>
              <label
                className={`flex cursor-pointer h-full grow items-center justify-center overflow-hidden rounded-lg px-2 transition-colors ${processType === 'checkin' ? 'bg-white shadow-sm text-gray-900' : 'text-primary'}`}
              >
                <span className="truncate">Check-in</span>
                <input
                  checked={processType === 'checkin'}
                  onChange={() => {
                    setProcessType('checkin');
                    setSelectedBooking(null);
                    setSearchTerm('');
                  }}
                  className="invisible w-0"
                  name="process_type"
                  type="radio"
                  value="checkin"
                />
              </label>
            </div>
          </div>

          {/* Booking Search */}
          {!selectedBooking && (
            <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm mb-8">
              <h2 className="text-gray-900 text-xl font-bold mb-4">Buchung suchen</h2>
              <div className="flex gap-4">
                <div className="flex-1">
                  <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    placeholder="Buchungsnummer eingeben..."
                    className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                  />
                </div>
                <button
                  onClick={handleSearch}
                  disabled={searchLoading}
                  className="flex items-center justify-center px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 disabled:opacity-50"
                >
                  {searchLoading ? (
                    <span className="material-symbols-outlined animate-spin">refresh</span>
                  ) : (
                    <>
                      <span className="material-symbols-outlined mr-2">search</span>
                      Suchen
                    </>
                  )}
                </button>
              </div>
              {searchError && <p className="mt-2 text-red-600 text-sm">{searchError}</p>}
            </div>
          )}

          {/* Main Form - Only shown when booking is selected */}
          {selectedBooking && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* Left Column */}
              <div className="lg:col-span-2 flex flex-col gap-8">
                {/* Booking Information */}
                <div>
                  <h2 className="text-gray-900 text-[22px] font-bold mb-3">
                    Buchungsinformationen
                  </h2>
                  <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                    <div className="flex flex-col sm:flex-row items-start justify-between gap-6">
                      <div className="flex flex-col gap-4 flex-[2_2_0px]">
                        <div className="flex flex-col gap-1">
                          <p className="text-sm text-primary">
                            Buchung #{selectedBooking.buchungsnummer}
                          </p>
                          <p className="text-xl font-bold text-gray-900">
                            {selectedBooking.fahrzeug?.marke} {selectedBooking.fahrzeug?.modell}
                          </p>
                          <p className="text-gray-500 text-sm">
                            Kennzeichen: {selectedBooking.fahrzeug?.kennzeichen}
                          </p>
                        </div>
                        <div className="flex flex-col gap-1">
                          <p className="font-semibold text-gray-900">
                            {selectedBooking.kundenName || 'Kunde'}
                          </p>
                          <p className="text-gray-500 text-sm">
                            {formatDateTime(selectedBooking.abholdatum)} -{' '}
                            {formatDateTime(selectedBooking.rueckgabedatum)}
                          </p>
                        </div>
                        <div className="flex gap-2">
                          <span
                            className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium ${
                              selectedBooking.status === 'CONFIRMED'
                                ? 'bg-green-100 text-green-800'
                                : selectedBooking.status === 'ACTIVE'
                                  ? 'bg-purple-100 text-purple-800'
                                  : 'bg-gray-100 text-gray-800'
                            }`}
                          >
                            {selectedBooking.status}
                          </span>
                        </div>
                      </div>
                      {selectedBooking.fahrzeug?.bildUrl && (
                        <div
                          className="w-full sm:w-48 bg-center bg-no-repeat aspect-video bg-cover rounded-lg flex-1"
                          style={{
                            backgroundImage: `url("${selectedBooking.fahrzeug.bildUrl}")`,
                          }}
                        />
                      )}
                    </div>
                    <button
                      onClick={() => setSelectedBooking(null)}
                      className="mt-4 text-sm text-primary hover:underline"
                    >
                      Andere Buchung suchen
                    </button>
                  </div>
                </div>

                {/* Vehicle Status Form */}
                <div>
                  <h2 className="text-gray-900 text-[22px] font-bold mb-3">
                    Fahrzeugzustand ({processType === 'checkout' ? 'Check-out' : 'Check-in'})
                  </h2>
                  <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label
                        className="block text-sm font-medium text-gray-700 mb-1"
                        htmlFor="mileage"
                      >
                        Kilometerstand *
                      </label>
                      <input
                        className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                        id="mileage"
                        type="number"
                        min="0"
                        value={mileage}
                        onChange={(e) => setMileage(e.target.value)}
                        required
                      />
                      {selectedBooking.fahrzeug?.kilometerstand && processType === 'checkout' && (
                        <p className="text-xs text-gray-500 mt-1">
                          Aktueller Fahrzeugstand: {selectedBooking.fahrzeug.kilometerstand} km
                        </p>
                      )}
                      {selectedBooking.checkoutMileage && processType === 'checkin' && (
                        <p className="text-xs text-gray-500 mt-1">
                          Kilometerstand bei Check-out: {selectedBooking.checkoutMileage} km
                        </p>
                      )}
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Tankfüllstand *
                      </label>
                      <div className="flex items-center justify-between text-xs text-gray-500 px-1">
                        {FUEL_LEVELS.map((level) => (
                          <span key={level.value}>{level.label}</span>
                        ))}
                      </div>
                      <input
                        className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-primary"
                        max="4"
                        min="0"
                        step="1"
                        type="range"
                        value={fuelSliderValue}
                        onChange={(e) => setFuelSliderValue(parseInt(e.target.value, 10))}
                      />
                    </div>
                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Sauberkeit *
                      </label>
                      <div className="flex flex-wrap gap-4">
                        {CLEANLINESS_OPTIONS.map((option) => (
                          <label
                            key={option.value}
                            className="flex items-center gap-2 cursor-pointer"
                          >
                            <input
                              className="form-radio text-primary focus:ring-primary"
                              name="cleanliness"
                              type="radio"
                              value={option.value}
                              checked={cleanliness === option.value}
                              onChange={(e) => setCleanliness(e.target.value)}
                            />
                            <span className="text-sm">{option.label}</span>
                          </label>
                        ))}
                      </div>
                    </div>
                    <div className="md:col-span-2">
                      <label
                        className="block text-sm font-medium text-gray-700 mb-1"
                        htmlFor="damagesDescription"
                      >
                        Allgemeine Anmerkungen zu Schäden
                      </label>
                      <textarea
                        className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                        id="damagesDescription"
                        rows="2"
                        value={damagesDescription}
                        onChange={(e) => setDamagesDescription(e.target.value)}
                        placeholder="Kurze Beschreibung vorhandener Schäden..."
                      />
                    </div>
                  </div>
                </div>

                {/* Damage Report Section (for Check-in) */}
                {processType === 'checkin' && (
                  <div>
                    <h2 className="text-gray-900 text-[22px] font-bold mb-3">Schadensprotokoll</h2>
                    <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                      {/* Existing damages list */}
                      {existingDamages.length > 0 && (
                        <div className="mb-6">
                          <h3 className="font-semibold mb-3 text-gray-900">Erfasste Schäden</h3>
                          <div className="space-y-3">
                            {existingDamages.map((damage, index) => (
                              <div
                                key={index}
                                className="flex items-start justify-between p-3 bg-red-50 rounded-lg border border-red-200"
                              >
                                <div>
                                  <p className="font-medium text-gray-900">{damage.description}</p>
                                  {damage.estimatedCost > 0 && (
                                    <p className="text-sm text-red-600">
                                      Geschätzte Kosten: {damage.estimatedCost.toFixed(2)} EUR
                                    </p>
                                  )}
                                  {damage.photos?.length > 0 && (
                                    <p className="text-xs text-gray-500">
                                      {damage.photos.length} Foto(s)
                                    </p>
                                  )}
                                </div>
                                <button
                                  onClick={() => removeDamage(index)}
                                  className="text-red-600 hover:text-red-800"
                                >
                                  <span className="material-symbols-outlined">delete</span>
                                </button>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Add new damage */}
                      <div>
                        <h3 className="font-semibold mb-3 text-gray-900">
                          Neuen Schaden hinzufügen
                        </h3>
                        <div className="space-y-4">
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                              Schadensbeschreibung
                            </label>
                            <textarea
                              className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                              placeholder="Beschreibung des Schadens..."
                              rows="3"
                              value={newDamageDescription}
                              onChange={(e) => setNewDamageDescription(e.target.value)}
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                              Geschätzte Kosten (EUR)
                            </label>
                            <input
                              type="number"
                              min="0"
                              step="0.01"
                              className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                              placeholder="0.00"
                              value={newDamageEstimatedCost}
                              onChange={(e) => setNewDamageEstimatedCost(e.target.value)}
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                              Fotos
                            </label>
                            <div className="flex items-center justify-center w-full px-4 py-3 border-2 border-dashed rounded-lg border-gray-300 text-center cursor-pointer hover:bg-gray-50">
                              <label className="cursor-pointer">
                                <div className="text-sm text-gray-600">
                                  <span className="material-symbols-outlined text-3xl text-primary">
                                    upload_file
                                  </span>
                                  <p>
                                    <span className="font-semibold">Fotos hochladen</span> oder
                                    hierher ziehen
                                  </p>
                                </div>
                                <input
                                  type="file"
                                  className="hidden"
                                  accept="image/*"
                                  multiple
                                  onChange={handlePhotoUpload}
                                />
                              </label>
                            </div>
                            {damagePhotos.length > 0 && (
                              <div className="mt-2 flex flex-wrap gap-2">
                                {damagePhotos.map((photo, index) => (
                                  <div key={index} className="relative">
                                    <img
                                      src={photo.data}
                                      alt={photo.name}
                                      className="w-20 h-20 object-cover rounded-lg"
                                    />
                                    <button
                                      onClick={() => removePhoto(index)}
                                      className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs"
                                    >
                                      x
                                    </button>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                          <button
                            onClick={addDamage}
                            className="flex items-center gap-2 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                          >
                            <span className="material-symbols-outlined">add</span>
                            Schaden hinzufügen
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Right Column (Summary) */}
              <div className="lg:col-span-1">
                <div className="sticky top-8 bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                  <h2 className="text-gray-900 text-[22px] font-bold mb-4">Zusammenfassung</h2>
                  <div className="space-y-4">
                    <div className="flex justify-between items-center text-sm">
                      <p className="text-gray-600">Prozess</p>
                      <p className="font-medium text-gray-900">
                        {processType === 'checkout' ? 'Check-out' : 'Check-in'}
                      </p>
                    </div>
                    <div className="flex justify-between items-center text-sm">
                      <p className="text-gray-600">Kilometerstand</p>
                      <p className="font-medium text-gray-900">{mileage || '-'} km</p>
                    </div>
                    {processType === 'checkin' && drivenKm !== null && drivenKm >= 0 && (
                      <div className="flex justify-between items-center text-sm">
                        <p className="text-gray-600">Gefahrene Kilometer</p>
                        <p className="font-medium text-gray-900">{drivenKm} km</p>
                      </div>
                    )}
                    <div className="flex justify-between items-center text-sm">
                      <p className="text-gray-600">Tankfüllstand</p>
                      <p className="font-medium text-gray-900">
                        {FUEL_LEVELS.find((l) => l.numericValue === fuelSliderValue)?.label || '-'}
                      </p>
                    </div>
                    <div className="flex justify-between items-center text-sm">
                      <p className="text-gray-600">Sauberkeit</p>
                      <p className="font-medium text-gray-900">
                        {CLEANLINESS_OPTIONS.find((o) => o.value === cleanliness)?.label || '-'}
                      </p>
                    </div>

                    {processType === 'checkin' && existingDamages.length > 0 && (
                      <>
                        <hr className="border-gray-200" />
                        <div className="flex justify-between items-center text-sm">
                          <p className="text-gray-600">Erfasste Schäden</p>
                          <p className="font-medium text-red-600">{existingDamages.length}</p>
                        </div>
                        <div className="flex justify-between items-center text-sm">
                          <p className="text-gray-600">Geschätzte Schadenskosten</p>
                          <p className="font-medium text-red-600">
                            {existingDamages
                              .reduce((sum, d) => sum + (d.estimatedCost || 0), 0)
                              .toFixed(2)}{' '}
                            EUR
                          </p>
                        </div>
                      </>
                    )}
                  </div>

                  <div className="mt-8 flex flex-col gap-3">
                    <button
                      onClick={handleSubmit}
                      disabled={loading}
                      className="w-full flex items-center justify-center rounded-lg h-11 px-4 bg-primary text-white text-base font-bold hover:bg-primary/90 disabled:opacity-50"
                    >
                      {loading ? (
                        <span className="material-symbols-outlined animate-spin">refresh</span>
                      ) : (
                        <span>
                          {processType === 'checkout'
                            ? 'Check-out abschließen'
                            : 'Check-in abschließen'}
                        </span>
                      )}
                    </button>
                    <button
                      onClick={() => setSelectedBooking(null)}
                      disabled={loading}
                      className="w-full flex items-center justify-center rounded-lg h-11 px-4 bg-gray-200 text-gray-700 text-base font-bold hover:bg-gray-300 disabled:opacity-50"
                    >
                      <span>Abbrechen</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default CheckInOutPage;
