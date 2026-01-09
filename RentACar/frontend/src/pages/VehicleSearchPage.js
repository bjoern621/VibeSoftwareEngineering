import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import VehicleCard from '../components/vehicles/VehicleCard';
import { searchVehicles, getVehicleTypes, getBranches } from '../services/vehicleService';

/**
 * VehicleSearchPage - Fahrzeugsuche mit Filter-Sidebar und Ergebnis-Grid
 * Konvertiert von Stitch Design: homepage/fahrzeugsuche_1
 * 
 * Features:
 * - Backend-Integration für Fahrzeugsuche
 * - Datumsvalidierung
 * - Dynamische Dropdowns (Fahrzeugtypen, Filialen)
 * - Clientseitige Filter (Sidebar)
 * - Loading & Error States
 */
const VehicleSearchPage = () => {
  const [searchParams] = useSearchParams();
  
  // Heutiges Datum für Validierung
  const today = new Date().toISOString().split('T')[0];
  
  // Haupt-Suchformular State
  const [searchForm, setSearchForm] = useState({
    location: searchParams.get('location') || '',
    pickupDate: searchParams.get('pickupDate') || '',
    returnDate: searchParams.get('returnDate') || '',
    vehicleType: searchParams.get('vehicleType') || 'ALL',
  });
  
  // Clientseitige Filter (Sidebar)
  const [sidebarFilters, setSidebarFilters] = useState({
    priceRange: 250,
    selectedTypes: [],
  });
  
  // API-Daten State
  const [vehicles, setVehicles] = useState([]);
  const [vehicleTypes, setVehicleTypes] = useState([]);
  const [branches, setBranches] = useState([]);
  
  // UI State
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [hasSearched, setHasSearched] = useState(false);
  const [validationError, setValidationError] = useState('');

  
  // Laden von Fahrzeugtypen und Filialen beim Mount
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const [typesData, branchesData] = await Promise.all([
          getVehicleTypes(),
          getBranches(),
        ]);
        console.log('VehicleSearchPage - Types geladen:', typesData);
        console.log('VehicleSearchPage - Branches geladen:', branchesData);
        setVehicleTypes(typesData);
        setBranches(branchesData);
      } catch (err) {
        console.error('Fehler beim Laden der Initialdaten:', err);
        // Fehler beim Laden von Typen/Filialen ist nicht kritisch
      }
    };

    loadInitialData();
  }, []);
  
  // Automatische Suche wenn URL-Parameter vorhanden sind
  useEffect(() => {
    const pickup = searchParams.get('pickup');
    const returnDate = searchParams.get('return');
    
    // Nur suchen wenn mindestens Abhol- und Rückgabedatum vorhanden sind
    if (pickup && returnDate) {
      const location = searchParams.get('location') || '';
      const type = searchParams.get('type') || 'ALL';
      
      setSearchForm({
        location,
        pickupDate: pickup,
        returnDate,
        vehicleType: type,
      });
      
      // Sidebar-Filter entsprechend der Hauptsuche setzen
      // Wenn ein spezifischer Typ gesucht wird, diesen in der Sidebar vorauswählen
      if (type && type !== 'ALL') {
        setSidebarFilters(prev => ({
          ...prev,
          selectedTypes: [type],
        }));
      } else {
        // Bei "Alle Typen" alle Checkboxen leeren (zeigt alle Ergebnisse)
        setSidebarFilters(prev => ({
          ...prev,
          selectedTypes: [],
        }));
      }
      
      // Automatische Suche durchführen
      performSearch(location, pickup, returnDate, type);
    }
  }, [searchParams]);
  
  /**
   * Führt die Suche durch (wird von handleSearch und useEffect verwendet)
   */
  const performSearch = async (location, pickup, returnDate, type) => {
    setLoading(true);
    setError(null);
    setValidationError('');
    
    try {
      const results = await searchVehicles({
        von: pickup,
        bis: returnDate,
        typ: type === 'ALL' ? undefined : type,
        standort: location || undefined,
      });
      
      setVehicles(results);
      setHasSearched(true);
    } catch (err) {
      console.error('Fehler bei der Suche:', err);
      setError(err.response?.data?.message || 'Fehler bei der Fahrzeugsuche. Bitte versuchen Sie es später erneut.');
      setVehicles([]);
    } finally {
      setLoading(false);
    }
  };
  
  /**
   * Validiert das Suchformular
   * @returns {boolean} true wenn valide
   */
  const validateSearchForm = () => {
    setValidationError('');
    
    // Pflichtfelder prüfen
    if (!searchForm.pickupDate || !searchForm.returnDate) {
      setValidationError('Bitte wählen Sie Abhol- und Rückgabedatum aus.');
      return false;
    }
    
    // Abholdatum nicht in der Vergangenheit
    if (searchForm.pickupDate < today) {
      setValidationError('Das Abholdatum darf nicht in der Vergangenheit liegen.');
      return false;
    }
    
    // Rückgabedatum muss nach Abholdatum liegen
    if (searchForm.returnDate <= searchForm.pickupDate) {
      setValidationError('Das Rückgabedatum muss nach dem Abholdatum liegen.');
      return false;
    }
    
    return true;
  };
  
  /**
   * Führt die Fahrzeugsuche durch
   */
  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!validateSearchForm()) {
      return;
    }
    
    // Sidebar-Filter entsprechend der Hauptsuche aktualisieren
    // Wenn ein spezifischer Typ gesucht wird, diesen in der Sidebar vorauswählen
    if (searchForm.vehicleType && searchForm.vehicleType !== 'ALL') {
      setSidebarFilters(prev => ({
        ...prev,
        selectedTypes: [searchForm.vehicleType],
      }));
    } else {
      // Bei "Alle Typen" alle Checkboxen leeren (zeigt alle Ergebnisse)
      setSidebarFilters(prev => ({
        ...prev,
        selectedTypes: [],
      }));
    }
    
    // Verwende performSearch für konsistente Suche
    await performSearch(
      searchForm.location,
      searchForm.pickupDate,
      searchForm.returnDate,
      searchForm.vehicleType
    );
  };
  
  /**
   * Aktualisiert Suchformular-Felder
   */
  const handleSearchFormChange = (field, value) => {
    setSearchForm((prev) => ({
      ...prev,
      [field]: value,
    }));
    setValidationError(''); // Validierungsfehler zurücksetzen
  };
  
  /**
   * Aktualisiert Sidebar-Filter
   */
  const handleSidebarFilterChange = (field, value) => {
    setSidebarFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  };
  
  /**
   * Togglet einen Fahrzeugtyp in der Sidebar und führt neue Suche aus
   */
  const toggleVehicleTypeFilter = async (type) => {
    // Berechne die neuen ausgewählten Typen
    const newSelectedTypes = sidebarFilters.selectedTypes.includes(type)
      ? sidebarFilters.selectedTypes.filter((t) => t !== type)
      : [...sidebarFilters.selectedTypes, type];
    
    // Aktualisiere den State
    setSidebarFilters((prev) => ({
      ...prev,
      selectedTypes: newSelectedTypes,
    }));
    
    // Bestimme den Fahrzeugtyp für die Backend-Suche
    let vehicleTypeForSearch = 'ALL';
    if (newSelectedTypes.length === 1) {
      // Genau ein Typ ausgewählt → verwende diesen
      vehicleTypeForSearch = newSelectedTypes[0];
    } else if (newSelectedTypes.length > 1) {
      // Mehrere Typen ausgewählt → Backend-Suche mit 'ALL', dann clientseitig filtern
      vehicleTypeForSearch = 'ALL';
    }
    // Wenn 0 Typen ausgewählt sind, verwende 'ALL' (zeigt alle)
    
    // Aktualisiere auch das Hauptformular
    setSearchForm(prev => ({
      ...prev,
      vehicleType: vehicleTypeForSearch,
    }));
    
    // Führe neue Suche aus
    await performSearch(
      searchForm.location,
      searchForm.pickupDate,
      searchForm.returnDate,
      vehicleTypeForSearch
    );
  };
  
  /**
   * Setzt alle Filter zurück
   */
  const handleResetFilters = () => {
    setSidebarFilters({
      priceRange: 250,
      selectedTypes: [],
    });
  };
  
  /**
   * Wendet clientseitige Filter auf Ergebnisse an
   * Nur Preis-Filter wird clientseitig angewendet
   * Fahrzeugtyp-Filter löst eine neue Backend-Suche aus
   */
  const getFilteredVehicles = () => {
    let filtered = [...vehicles];
    
    // Preis-Filter (clientseitig)
    filtered = filtered.filter((v) => v.pricePerDay <= sidebarFilters.priceRange);
    
    // Fahrzeugtyp-Filter nur anwenden wenn mehrere Typen ausgewählt sind
    // (bei einzelnem Typ macht das Backend bereits die Filterung)
    if (sidebarFilters.selectedTypes.length > 1) {
      filtered = filtered.filter((v) =>
        sidebarFilters.selectedTypes.includes(v.vehicleType)
      );
    }
    
    return filtered;
  };
  
  const filteredVehicles = getFilteredVehicles();


  return (
    <div className="min-h-screen bg-background-light">
      {/* Hero-Section mit Suchformular */}
      <section className="relative flex min-h-[400px] w-full items-center justify-center bg-cover bg-center py-8" 
        style={{ backgroundImage: "linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.6)), url('https://lh3.googleusercontent.com/aida-public/AB6AXuBdG6sBcOPaNASQCivb3PyJ8ytLelWkNWq4WaEM6WdSL9tDCvHAb1CV6m37Vno8GmcjGAGsFaINEik-RUnjOHeh9oA9mMqIaR-__Un_agzGNyTyXNA3sZj1QUoi2ElEL36sG5TcB2ak2IuCS4D8ml0DElcLD2eoPD2emdYa8aWzHjOScQ25zXGI3vM_njPJvMSujP1Q68Lslw1_DVc0LLJTMhnImhqiTXCyZbaRxA1w14gyyWf9gxMbKRvbOs4qvFpJRkSWUj74WKqA')" }}>
        <div className="container mx-auto flex flex-col items-center gap-6 px-6 text-center">
          <div className="flex flex-col gap-2">
            <h1 className="text-3xl font-extrabold text-white sm:text-4xl">Finden Sie Ihr perfektes Mietauto</h1>
            <h2 className="text-sm font-normal text-white/90">Buchen Sie schnell und einfach aus unserer großen Auswahl an Fahrzeugen.</h2>
          </div>
          
          {/* Suchformular */}
          <div className="w-full max-w-4xl rounded-xl bg-card-bg/95 p-4 shadow-2xl backdrop-blur-sm">
            <form onSubmit={handleSearch} className="grid grid-cols-1 items-end gap-4 md:grid-cols-4 lg:grid-cols-10">
              {/* Standort Dropdown */}
              <label className="flex flex-col text-left md:col-span-2 lg:col-span-3">
                <p className="pb-2 text-sm font-medium text-text-main">Standort</p>
                <div className="relative flex w-full items-stretch">
                  <span className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">location_on</span>
                  <select
                    value={searchForm.location}
                    onChange={(e) => handleSearchFormChange('location', e.target.value)}
                    className="form-select h-14 w-full rounded-lg border-gray-300 bg-gray-50 pl-10 text-text-main focus:border-primary focus:ring-primary"
                  >
                    <option value="">Alle Standorte</option>
                    {branches.map((branch) => (
                      <option key={branch.id} value={branch.name}>
                        {branch.name}
                      </option>
                    ))}
                  </select>
                </div>
              </label>
              
              {/* Abholdatum */}
              <label className="flex flex-col text-left lg:col-span-2">
                <p className="pb-2 text-sm font-medium text-text-main">Abholdatum</p>
                <div className="relative">
                  <span className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">calendar_today</span>
                  <input
                    type="date"
                    min={today}
                    value={searchForm.pickupDate}
                    onChange={(e) => handleSearchFormChange('pickupDate', e.target.value)}
                    className="form-input h-14 w-full rounded-lg border-gray-300 bg-gray-50 pl-10 text-text-main placeholder:text-gray-400 focus:border-primary focus:ring-primary"
                    required
                  />
                </div>
              </label>
              
              {/* Rückgabedatum */}
              <label className="flex flex-col text-left lg:col-span-2">
                <p className="pb-2 text-sm font-medium text-text-main">Rückgabedatum</p>
                <div className="relative">
                  <span className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">event</span>
                  <input
                    type="date"
                    min={searchForm.pickupDate || today}
                    value={searchForm.returnDate}
                    onChange={(e) => handleSearchFormChange('returnDate', e.target.value)}
                    className="form-input h-14 w-full rounded-lg border-gray-300 bg-gray-50 pl-10 text-text-main placeholder:text-gray-400 focus:border-primary focus:ring-primary"
                    required
                  />
                </div>
              </label>
              
              {/* Fahrzeugtyp Dropdown */}
              <label className="flex flex-col text-left md:col-span-2 lg:col-span-2">
                <p className="pb-2 text-sm font-medium text-text-main">Fahrzeugtyp</p>
                <select
                  value={searchForm.vehicleType}
                  onChange={(e) => handleSearchFormChange('vehicleType', e.target.value)}
                  className="form-select h-14 w-full rounded-lg border-gray-300 bg-gray-50 text-text-main focus:border-primary focus:ring-primary"
                >
                  <option value="ALL">Alle Typen</option>
                  {vehicleTypes.map((type) => (
                    <option key={type.name} value={type.name}>
                      {type.displayName}
                    </option>
                  ))}
                </select>
              </label>
              
              {/* Such-Button */}
              <button
                type="submit"
                disabled={loading}
                className="h-14 w-full rounded-lg bg-primary text-base font-bold text-white transition-opacity hover:opacity-90 disabled:opacity-50 md:col-span-2 lg:col-span-1"
              >
                {loading ? '...' : 'Suchen'}
              </button>
            </form>
            
            {/* Validierungsfehler */}
            {validationError && (
              <div className="mt-3 rounded-lg bg-red-50 p-3 text-sm text-red-600">
                {validationError}
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Ergebnisse Section */}
      <section className="container mx-auto px-6 py-10">
        <div className="flex flex-col gap-8 lg:flex-row">
          {/* Filter Sidebar - nur anzeigen wenn Ergebnisse vorhanden */}
          {hasSearched && vehicles.length > 0 && (
            <aside className="w-full lg:w-1/4 xl:w-1/5">
              <div className="sticky top-24 rounded-xl border border-gray-200 bg-card-bg p-6 shadow-sm">
                <div className="flex items-center justify-between pb-4 border-b">
                  <h3 className="text-lg font-bold">Filter</h3>
                  <button
                    onClick={handleResetFilters}
                    className="text-sm font-medium text-primary hover:underline"
                  >
                    Zurücksetzen
                  </button>
                </div>
                <div className="space-y-6 pt-6">
                  {/* Fahrzeugtyp Filter (clientseitig) */}
                  <div>
                    <h4 className="mb-3 font-semibold">Fahrzeugtyp</h4>
                    <div className="space-y-2">
                      {vehicleTypes.map((type) => (
                        <label key={type.name} className="flex items-center">
                          <input
                            type="checkbox"
                            checked={sidebarFilters.selectedTypes.includes(type.name)}
                            onChange={() => toggleVehicleTypeFilter(type.name)}
                            className="form-checkbox h-5 w-5 rounded text-primary focus:ring-primary"
                          />
                          <span className="ml-3 text-sm">{type.displayName}</span>
                        </label>
                      ))}
                    </div>
                  </div>
                  
                  {/* Preis pro Tag Filter */}
                  <div>
                    <h4 className="mb-3 font-semibold">Preis pro Tag</h4>
                    <input
                      type="range"
                      min="20"
                      max="300"
                      value={sidebarFilters.priceRange}
                      onChange={(e) => handleSidebarFilterChange('priceRange', e.target.value)}
                      className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-primary"
                    />
                    <div className="flex justify-between text-sm text-gray-500 mt-2">
                      <span>20€</span>
                      <span className="font-semibold text-primary">{sidebarFilters.priceRange}€</span>
                      <span>300€</span>
                    </div>
                  </div>
                </div>
              </div>
            </aside>
          )}

          {/* Ergebnisse Grid */}
          <div className={`w-full ${hasSearched && vehicles.length > 0 ? 'lg:w-3/4 xl:w-4/5' : ''}`}>
            {/* Loading State */}
            {loading && (
              <div className="flex flex-col items-center justify-center py-20">
                <div className="h-16 w-16 animate-spin rounded-full border-4 border-gray-200 border-t-primary"></div>
                <p className="mt-4 text-lg text-gray-600">Suche nach verfügbaren Fahrzeugen...</p>
              </div>
            )}

            {/* Error State */}
            {error && !loading && (
              <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center">
                <span className="material-symbols-outlined text-5xl text-red-500">error</span>
                <h3 className="mt-4 text-xl font-bold text-red-800">Fehler beim Laden</h3>
                <p className="mt-2 text-red-600">{error}</p>
                <button
                  onClick={() => setError(null)}
                  className="mt-4 rounded-lg bg-red-600 px-6 py-2 text-white hover:bg-red-700"
                >
                  Schließen
                </button>
              </div>
            )}

            {/* Keine Ergebnisse */}
            {!loading && !error && hasSearched && filteredVehicles.length === 0 && (
              <div className="rounded-xl border border-gray-200 bg-card-bg p-12 text-center">
                <span className="material-symbols-outlined text-6xl text-gray-400">search_off</span>
                <h3 className="mt-4 text-2xl font-bold text-gray-800">Keine Fahrzeuge gefunden</h3>
                <p className="mt-2 text-gray-600">
                  Leider konnten wir keine verfügbaren Fahrzeuge für Ihre Suchkriterien finden.
                </p>
                <p className="mt-1 text-sm text-gray-500">
                  Versuchen Sie es mit anderen Daten oder einem anderen Standort.
                </p>
              </div>
            )}

            {/* Ergebnisse anzeigen */}
            {!loading && !error && filteredVehicles.length > 0 && (
              <>
                <div className="mb-6">
                  <h2 className="text-2xl font-bold">
                    {filteredVehicles.length} Fahrzeug{filteredVehicles.length !== 1 ? 'e' : ''} gefunden
                  </h2>
                  {sidebarFilters.selectedTypes.length > 0 && (
                    <p className="mt-1 text-sm text-gray-600">
                      {vehicles.length - filteredVehicles.length} Fahrzeuge durch Filter ausgeblendet
                    </p>
                  )}
                </div>
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
                  {filteredVehicles.map((vehicle) => (
                    <VehicleCard 
                      key={vehicle.id} 
                      vehicle={vehicle}
                      searchParams={{
                        pickupDate: searchForm.pickupDate,
                        returnDate: searchForm.returnDate,
                      }}
                    />
                  ))}
                </div>
              </>
            )}

            {/* Initial State - vor der ersten Suche */}
            {!hasSearched && !loading && (
              <div className="rounded-xl border border-gray-200 bg-card-bg p-12 text-center">
                <span className="material-symbols-outlined text-6xl text-primary">search</span>
                <h3 className="mt-4 text-2xl font-bold text-gray-800">Bereit für Ihre Suche</h3>
                <p className="mt-2 text-gray-600">
                  Wählen Sie Ihre Reisedaten und den gewünschten Fahrzeugtyp aus,<br />
                  um verfügbare Fahrzeuge zu finden.
                </p>
              </div>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};

export default VehicleSearchPage;
