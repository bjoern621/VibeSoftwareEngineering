import React, { useState, useEffect } from 'react';
import { getVehicleTypes } from '../../services/vehicleService';
import { safeToFixed } from '../../utils/numberUtils';

/**
 * PriceCalculatorForm - Formular für die Preisberechnung
 *
 * Ermöglicht die Eingabe aller Parameter für die Preisberechnung:
 * - Fahrzeugtyp-Auswahl
 * - Abhol- und Rückgabedatum/-zeit
 * - Zusatzleistungen (Checkboxen)
 *
 * @param {Function} onCalculate - Callback-Funktion, die beim Submit aufgerufen wird
 * @param {boolean} isLoading - Ladezustand während der Berechnung
 */
const PriceCalculatorForm = ({ onCalculate, isLoading }) => {
  // Form state
  const [vehicleType, setVehicleType] = useState('');
  const [pickupDateTime, setPickupDateTime] = useState('');
  const [returnDateTime, setReturnDateTime] = useState('');
  const [additionalServices, setAdditionalServices] = useState([]);

  // Validation errors
  const [errors, setErrors] = useState({});

  // Vehicle types from backend
  const [vehicleTypes, setVehicleTypes] = useState([]);
  const [loadingTypes, setLoadingTypes] = useState(true);

  // Verfügbare Zusatzleistungen mit deutschen Namen und Preisen
  const availableServices = [
    { type: 'CHILD_SEAT', label: 'Kindersitz', icon: 'child_care' },
    { type: 'GPS', label: 'Navigationssystem', icon: 'navigation' },
    { type: 'ADDITIONAL_DRIVER', label: 'Zusatzfahrer', icon: 'person_add' },
    {
      type: 'FULL_INSURANCE',
      label: 'Vollkaskoversicherung',
      icon: 'verified_user',
    },
    { type: 'WINTER_TIRES', label: 'Winterreifen', icon: 'ac_unit' },
    { type: 'ROOF_RACK', label: 'Dachgepäckträger', icon: 'garage' },
  ];

  // Fahrzeugtypen beim Laden abrufen
  useEffect(() => {
    const fetchVehicleTypes = async () => {
      try {
        const types = await getVehicleTypes();
        // Normalisiere Backend-Antwort in das erwartete Format
        const normalized = (Array.isArray(types) ? types : []).map((t) => ({
          type: t.type || t.name || (t.name && t.name.toUpperCase && t.name.toUpperCase()) || t.name,
          basePrice: t.basePrice ?? t.dailyBaseRate ?? t.dailyRate ?? 0,
          displayName: t.displayName || t.label || t.name || t.type,
          passengerCapacity: t.passengerCapacity || t.seats || null,
        }));

        setVehicleTypes(normalized);
        // Falls zuvor ein Fehler angezeigt wurde, entfernen
        setErrors((prev) => {
          const copy = { ...prev };
          delete copy.general;
          return copy;
        });
      } catch (error) {
        console.error('Fehler beim Laden der Fahrzeugtypen:', error);
        // Fallback: Liste von Standard-Fahrzeugtypen anbieten, damit Tests lokal möglich sind
        const fallback = [
          { type: 'COMPACT_CAR', basePrice: 30, displayName: 'Kleinwagen' },
          { type: 'SEDAN', basePrice: 45, displayName: 'Limousine' },
          { type: 'SUV', basePrice: 70, displayName: 'SUV' },
          { type: 'VAN', basePrice: 90, displayName: 'Transporter' },
        ];
        setVehicleTypes(fallback);
        setErrors((prev) => ({
          ...prev,
          general:
            'Fahrzeugtypen konnten nicht geladen werden. Es werden Standardtypen verwendet. Bitte prüfen Sie die Backend-Verbindung.',
        }));
      } finally {
        setLoadingTypes(false);
      }
    };

    fetchVehicleTypes();
  }, []);

  /**
   * Übersetzt Fahrzeugtypen ins Deutsche
   */
  const translateVehicleType = (type) => {
    const translations = {
      COMPACT_CAR: 'Kleinwagen',
      SEDAN: 'Limousine',
      SUV: 'SUV',
      VAN: 'Van',
    };
    return translations[type] || type;
  };

  // Sichere Formatierung für Preisangaben (vermeidet .toFixed auf undefined)
  const formatPriceSafe = (value) => {
    // keep existing UI fallback '—'
    return safeToFixed(value, 2, '—');
  };

  /**
   * Validiert das Formular
   */
  const validateForm = () => {
    const newErrors = {};

    // Fahrzeugtyp erforderlich
    if (!vehicleType) {
      newErrors.vehicleType = 'Bitte wählen Sie einen Fahrzeugtyp aus.';
    }

    // Abholdatum erforderlich
    if (!pickupDateTime) {
      newErrors.pickupDateTime = 'Bitte geben Sie ein Abholdatum ein.';
    } else {
      const pickup = new Date(pickupDateTime);
      const now = new Date();

      // Abholdatum muss in der Zukunft liegen
      if (pickup < now) {
        newErrors.pickupDateTime = 'Das Abholdatum muss in der Zukunft liegen.';
      }
    }

    // Rückgabedatum erforderlich
    if (!returnDateTime) {
      newErrors.returnDateTime = 'Bitte geben Sie ein Rückgabedatum ein.';
    } else if (pickupDateTime) {
      const pickup = new Date(pickupDateTime);
      const returnDate = new Date(returnDateTime);

      // Rückgabedatum muss nach Abholdatum liegen
      if (returnDate <= pickup) {
        newErrors.returnDateTime =
          'Das Rückgabedatum muss nach dem Abholdatum liegen.';
      }

      // Mindestmietdauer: 1 Tag
      const diffHours = (returnDate - pickup) / (1000 * 60 * 60);
      if (diffHours < 24) {
        newErrors.returnDateTime =
          'Die Mindestmietdauer beträgt 1 Tag (24 Stunden).';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Behandelt Änderungen bei Zusatzleistungen
   */
  const handleServiceToggle = (serviceType) => {
    setAdditionalServices((prev) => {
      if (prev.includes(serviceType)) {
        return prev.filter((s) => s !== serviceType);
      } else {
        return [...prev, serviceType];
      }
    });
  };

  /**
   * Behandelt das Absenden des Formulars
   */
  const ensureSeconds = (dtString) => {
    if (!dtString) return dtString;
    // datetime-local usually returns "yyyy-MM-ddTHH:mm" (no seconds).
    // Backend erwartet pattern yyyy-MM-dd'T'HH:mm:ss -> append :00 when seconds are missing.
    // Also handle values already containing seconds or milliseconds.
    // Keep local time (no timezone / Z suffix).
    try {
      // If string already contains seconds (length >= 19 -> yyyy-MM-ddTHH:mm:ss)
      if (dtString.length >= 19 && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(dtString)) {
        return dtString.substring(0, 19);
      }
      // If it's like yyyy-MM-ddTHH:mm (length 16) append :00
      if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(dtString)) {
        return `${dtString}:00`;
      }
      // Fallback: try to create a Date and format local components
      const d = new Date(dtString);
      if (Number.isNaN(d.getTime())) return dtString;
      const pad = (n) => String(n).padStart(2, '0');
      const yyyy = d.getFullYear();
      const mm = pad(d.getMonth() + 1);
      const dd = pad(d.getDate());
      const hh = pad(d.getHours());
      const min = pad(d.getMinutes());
      const sec = pad(d.getSeconds());
      return `${yyyy}-${mm}-${dd}T${hh}:${min}:${sec}`;
    } catch (e) {
      return dtString;
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // Zusätzliche Guard: Fahrzeugtyp muss verfügbar und ausgewählt sein
    if (!vehicleTypes || vehicleTypes.length === 0) {
      setErrors((prev) => ({
        ...prev,
        general: 'Fahrzeugtypen konnten nicht geladen werden. Bitte Seite neu laden oder später erneut versuchen.'
      }));
      return;
    }

    if (!vehicleType) {
      setErrors((prev) => ({ ...prev, vehicleType: 'Bitte wählen Sie einen Fahrzeugtyp aus.' }));
      return;
    }

    if (validateForm()) {
      // Ensure datetimes include seconds to match backend LocalDateTime format (yyyy-MM-dd'T'HH:mm:ss)
      const payload = {
        vehicleType,
        pickupDateTime: ensureSeconds(pickupDateTime),
        returnDateTime: ensureSeconds(returnDateTime),
        additionalServices,
      };

      onCalculate(payload);
    }
  };

  /**
   * Berechnet Anzahl der Miettage zur Anzeige
   */
  const calculateDays = () => {
    if (!pickupDateTime || !returnDateTime) return null;

    const pickup = new Date(pickupDateTime);
    const returnDate = new Date(returnDateTime);
    const diffTime = returnDate - pickup;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    return diffDays > 0 ? diffDays : null;
  };

  const rentalDays = calculateDays();

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Fehleranzeige */}
      {errors.general && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex gap-2">
            <span className="material-symbols-outlined text-red-600">error</span>
            <p className="text-sm text-red-800">{errors.general}</p>
          </div>
        </div>
      )}

      {/* Fahrzeugtyp */}
      <div>
        <label htmlFor="vehicleType" className="block text-sm font-medium text-gray-700 mb-2">
          Fahrzeugtyp *
        </label>
        <select
          id="vehicleType"
          value={vehicleType}
          onChange={(e) => setVehicleType(e.target.value)}
          disabled={loadingTypes || isLoading}
          className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-colors ${
            errors.vehicleType ? 'border-red-500' : 'border-gray-300'
          }`}
        >
          <option value="">
            {loadingTypes
              ? 'Lade Fahrzeugtypen...'
              : 'Bitte wählen Sie einen Fahrzeugtyp'}
          </option>
          {vehicleTypes.length === 0 && !loadingTypes ? (
            <option value="" disabled>
              Keine Fahrzeugtypen verfügbar
            </option>
          ) : (
            vehicleTypes.map((type) => (
              <option key={type.type} value={type.type}>
                {translateVehicleType(type.type)} - ab {formatPriceSafe(type.basePrice)}{' '}
                €/Tag
              </option>
            ))
          )}
        </select>
        {errors.vehicleType && (
          <p className="mt-1 text-sm text-red-600">{errors.vehicleType}</p>
        )}
      </div>

      {/* Abholdatum und -zeit */}
      <div>
        <label htmlFor="pickupDateTime" className="block text-sm font-medium text-gray-700 mb-2">
          Abholdatum und -zeit *
        </label>
        <input
          type="datetime-local"
          id="pickupDateTime"
          value={pickupDateTime}
          onChange={(e) => setPickupDateTime(e.target.value)}
          disabled={isLoading}
          className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-colors ${
            errors.pickupDateTime ? 'border-red-500' : 'border-gray-300'
          }`}
        />
        {errors.pickupDateTime && (
          <p className="mt-1 text-sm text-red-600">{errors.pickupDateTime}</p>
        )}
      </div>

      {/* Rückgabedatum und -zeit */}
      <div>
        <label htmlFor="returnDateTime" className="block text-sm font-medium text-gray-700 mb-2">
          Rückgabedatum und -zeit *
        </label>
        <input
          type="datetime-local"
          id="returnDateTime"
          value={returnDateTime}
          onChange={(e) => setReturnDateTime(e.target.value)}
          disabled={isLoading}
          className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-colors ${
            errors.returnDateTime ? 'border-red-500' : 'border-gray-300'
          }`}
        />
        {errors.returnDateTime && (
          <p className="mt-1 text-sm text-red-600">{errors.returnDateTime}</p>
        )}

        {/* Anzeige der Miettage */}
        {rentalDays && (
          <p className="mt-2 text-sm text-gray-600">
            Mietdauer: {rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'}
          </p>
        )}
      </div>

      {/* Zusatzleistungen */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-3">
          Zusatzleistungen (optional)
        </label>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {availableServices.map((service) => (
            <div key={service.type} className="flex items-start">
              <input
                type="checkbox"
                id={service.type}
                checked={additionalServices.includes(service.type)}
                onChange={() => handleServiceToggle(service.type)}
                disabled={isLoading}
                className="mt-1 h-4 w-4 text-primary border-gray-300 rounded focus:ring-primary"
              />
              <label
                htmlFor={service.type}
                className="ml-3 flex items-center gap-2 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base text-gray-600">
                  {service.icon}
                </span>
                <span className="text-sm text-gray-700">{service.label}</span>
              </label>
            </div>
          ))}
        </div>
      </div>

      {/* Submit-Button */}
      <button
        type="submit"
        disabled={isLoading}
        className="w-full bg-primary text-white py-3 px-6 rounded-lg font-medium hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        {isLoading ? (
          <>
            <span className="material-symbols-outlined animate-spin">
              progress_activity
            </span>
            Berechne Preis...
          </>
        ) : (
          <>
            <span className="material-symbols-outlined">calculate</span>
            Preis berechnen
          </>
        )}
      </button>

      <p className="text-xs text-gray-500 text-center">* Pflichtfelder</p>
    </form>
  );
};

export default PriceCalculatorForm;
