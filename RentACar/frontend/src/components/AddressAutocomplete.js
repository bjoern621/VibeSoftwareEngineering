/**
 * AddressAutocomplete - Adress-Eingabe mit OpenStreetMap Nominatim API
 *
 * Features:
 * - Auto-Vervollständigung für Straße + Hausnummer
 * - Validiert echte Adressen in Deutschland
 * - Kostenlos (OpenStreetMap Nominatim)
 * - Automatisches Ausfüllen von PLZ und Stadt
 */

import React, { useState, useEffect, useRef } from 'react';

const AddressAutocomplete = ({ street, postalCode, city, onAddressChange, validationErrors }) => {
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const debounceTimer = useRef(null);
  const wrapperRef = useRef(null);

  /**
   * Schließt Vorschläge wenn außerhalb geklickt wird
   */
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  /**
   * Sucht Adressen über OpenStreetMap Nominatim API
   */
  const searchAddress = async (query) => {
    if (!query || query.length < 3) {
      setSuggestions([]);
      return;
    }

    try {
      setIsLoading(true);
      console.log('API Call gestartet für:', query); // Debug

      // Nominatim API Call (nur Deutschland)
      const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&countrycodes=de&addressdetails=1&limit=5`;
      console.log('URL:', url); // Debug

      const response = await fetch(url, {
        headers: {
          'Accept-Language': 'de',
          'User-Agent': 'RentACar-App', // Nominatim erfordert User-Agent
        },
      });

      if (!response.ok) {
        throw new Error('Adresssuche fehlgeschlagen');
      }

      const data = await response.json();
      console.log('API Antwort:', data); // Debug

      // Filtere nur Straßen-Adressen (nicht Städte/Bundesländer)
      const filteredResults = data.filter((item) => item.address && item.address.road);
      console.log('Gefilterte Ergebnisse:', filteredResults); // Debug

      setSuggestions(filteredResults);
      setShowSuggestions(filteredResults.length > 0);
    } catch (error) {
      console.error('Fehler bei Adresssuche:', error);
      setSuggestions([]);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Debounced Suche (wartet 500ms nach letzter Eingabe)
   */
  const handleStreetChange = (value) => {
    // Update sofort im Parent
    onAddressChange({ street: value });

    // Debounce: Warte 500ms nach letzter Eingabe
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    debounceTimer.current = setTimeout(() => {
      console.log('Suche Adresse:', value); // Debug
      searchAddress(value);
    }, 500);
  };

  /**
   * Wählt eine Adresse aus den Vorschlägen aus
   */
  const selectAddress = (suggestion) => {
    const address = suggestion.address;

    // Straße + Hausnummer
    const streetName = address.road || '';
    const houseNumber = address.house_number || '';
    const fullStreet = houseNumber ? `${streetName} ${houseNumber}` : streetName;

    // PLZ und Stadt
    const plz = address.postcode || '';
    const cityName = address.city || address.town || address.village || '';

    // Update alle Adressfelder
    onAddressChange({
      street: fullStreet,
      postalCode: plz,
      city: cityName,
    });

    // Schließe Vorschläge
    setShowSuggestions(false);
    setSuggestions([]);
  };

  /**
   * Formatiert Vorschlag für Anzeige
   */
  const formatSuggestion = (suggestion) => {
    const addr = suggestion.address;
    const street = addr.road || '';
    const houseNumber = addr.house_number || '';
    const plz = addr.postcode || '';
    const city = addr.city || addr.town || addr.village || '';

    return `${street} ${houseNumber}, ${plz} ${city}`.trim();
  };

  return (
    <div ref={wrapperRef} className="relative">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Straße mit Autocomplete */}
        <label className="flex flex-col md:col-span-2 relative">
          <p className="text-sm font-medium text-gray-700 pb-2">Straße *</p>
          <input
            className={`form-input rounded-lg border-gray-300 h-12 px-3 ${
              validationErrors?.street ? 'border-red-500' : ''
            }`}
            type="text"
            value={street}
            onChange={(e) => handleStreetChange(e.target.value)}
            onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
            placeholder="Straße und Hausnummer eingeben..."
            required
            autoComplete="off"
          />
          {validationErrors?.street && (
            <p className="text-red-500 text-xs mt-1">
              {validationErrors.street.message || validationErrors.street}
            </p>
          )}

          {/* Loading Indicator */}
          {isLoading && (
            <div className="absolute right-3 top-10">
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-primary"></div>
            </div>
          )}

          {/* Vorschläge Dropdown */}
          {showSuggestions && suggestions.length > 0 && (
            <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-300 rounded-lg shadow-lg z-50 max-h-60 overflow-y-auto">
              {suggestions.map((suggestion, index) => (
                <button
                  key={index}
                  type="button"
                  onClick={() => selectAddress(suggestion)}
                  className="w-full text-left px-4 py-3 hover:bg-gray-100 transition-colors border-b border-gray-100 last:border-b-0"
                >
                  <div className="flex items-start gap-2">
                    <span className="material-symbols-outlined text-primary text-lg mt-0.5">
                      location_on
                    </span>
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {formatSuggestion(suggestion)}
                      </p>
                      {suggestion.address.suburb && (
                        <p className="text-xs text-gray-500">{suggestion.address.suburb}</p>
                      )}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          )}

          {/* Kein Ergebnis */}
          {showSuggestions && !isLoading && suggestions.length === 0 && street.length >= 3 && (
            <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-300 rounded-lg shadow-lg z-50 px-4 py-3">
              <p className="text-sm text-gray-500">
                Keine Adressen gefunden. Bitte überprüfen Sie Ihre Eingabe.
              </p>
            </div>
          )}
        </label>

        {/* PLZ */}
        <label className="flex flex-col">
          <p className="text-sm font-medium text-gray-700 pb-2">Postleitzahl *</p>
          <input
            className={`form-input rounded-lg border-gray-300 h-12 px-3 ${
              validationErrors?.postalCode ? 'border-red-500' : ''
            }`}
            type="text"
            value={postalCode}
            onChange={(e) => onAddressChange({ postalCode: e.target.value })}
            required
            placeholder="PLZ"
          />
          {validationErrors?.postalCode && (
            <p className="text-red-500 text-xs mt-1">
              {validationErrors.postalCode.message || validationErrors.postalCode}
            </p>
          )}
        </label>

        {/* Stadt */}
        <label className="flex flex-col">
          <p className="text-sm font-medium text-gray-700 pb-2">Stadt *</p>
          <input
            className={`form-input rounded-lg border-gray-300 h-12 px-3 ${
              validationErrors?.city ? 'border-red-500' : ''
            }`}
            type="text"
            value={city}
            onChange={(e) => onAddressChange({ city: e.target.value })}
            required
            placeholder="Stadt"
          />
          {validationErrors?.city && (
            <p className="text-red-500 text-xs mt-1">
              {validationErrors.city.message || validationErrors.city}
            </p>
          )}
        </label>
      </div>

      {/* Hinweis */}
      <p className="text-xs text-gray-500 mt-2">
        💡 Tipp: Beginnen Sie mit der Eingabe der Straße für Vorschläge
      </p>
    </div>
  );
};

export default AddressAutocomplete;
