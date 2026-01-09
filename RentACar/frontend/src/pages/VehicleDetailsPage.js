import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useBooking } from '../context/BookingContext';
import { getVehicleById } from '../services/vehicleService';
import { calculatePrice } from '../services/bookingService';
import { getVehicleImage } from '../utils/vehicleImages';

/**
 * VehicleDetailsPage - Detailansicht eines Fahrzeugs mit Galerie und Buchungsformular
 */
const VehicleDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setVehicle, setDates, setBranches, setStep } = useBooking();
  
  const [activeImageIndex, setActiveImageIndex] = useState(0);
  const [activeTab, setActiveTab] = useState('equipment');
  const [vehicle, setVehicleData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [pricePerDay, setPricePerDay] = useState(null);
  const [isLoadingPrice, setIsLoadingPrice] = useState(false);
  
  // Datumsfelder aus URL oder heute + 7 Tage
  const today = new Date().toISOString().split('T')[0];
  const nextWeek = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  
  const [pickupDate, setPickupDate] = useState(searchParams.get('pickupDate') || today);
  const [returnDate, setReturnDate] = useState(searchParams.get('returnDate') || nextWeek);
  
  // Hilfsfunktion: Berechne intelligente Standard-Abholzeit
  const getDefaultPickupTime = (dateString) => {
    const selectedDate = new Date(dateString);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    selectedDate.setHours(0, 0, 0, 0);
    
    // Wenn das Abholdatum heute ist
    if (selectedDate.getTime() === today.getTime()) {
      const now = new Date();
      // Aktuelle Stunde + 2 Stunden als Mindestvorlaufzeit
      const futureHour = now.getHours() + 2;
      
      // Wenn es nach 22 Uhr ist, setze auf 23:59
      if (futureHour >= 23) {
        return '23:59:00';
      }
      
      // Sonst: nächste volle Stunde
      const roundedHour = Math.min(23, futureHour);
      return `${roundedHour.toString().padStart(2, '0')}:00:00`;
    }
    
    // Für zukünftige Tage: Standard 10:00 Uhr
    return '10:00:00';
  };
  
  // Fahrzeug vom Backend laden
  useEffect(() => {
    const loadVehicle = async () => {
      try {
        setIsLoading(true);
        const data = await getVehicleById(id);
        setVehicleData(data);
      } catch (error) {
        console.error('Fehler beim Laden des Fahrzeugs:', error);
        alert('Fahrzeug konnte nicht geladen werden.');
      } finally {
        setIsLoading(false);
      }
    };
    
    loadVehicle();
  }, [id]);
  
  // Berechne Mietdauer
  const calculateDays = () => {
    if (!pickupDate || !returnDate) return 0;
    const pickup = new Date(pickupDate);
    const returnD = new Date(returnDate);
    const diffTime = Math.abs(returnD - pickup);
    return Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
  };
  
  const rentalDays = calculateDays();
  
  // Preis berechnen wenn Fahrzeug und Datum verfügbar
  useEffect(() => {
    const loadPrice = async () => {
      if (!vehicle || !pickupDate || !returnDate || rentalDays === 0) return;
      
      // Debug-Log
      console.log('Lade Preis für:', {
        vehicleType: vehicle.vehicleType,
        pickupDate,
        returnDate,
        rentalDays
      });
      
      try {
        setIsLoadingPrice(true);
        
        // Berechne intelligente Zeiten basierend auf den Daten
        const pickupTime = getDefaultPickupTime(pickupDate);
        const returnTime = '10:00:00'; // Rückgabe kann Standard bleiben
        
        const priceData = await calculatePrice({
          vehicleType: vehicle.vehicleType, // Backend erwartet vehicleType, nicht vehicleId
          pickupDateTime: pickupDate + 'T' + pickupTime,
          returnDateTime: returnDate + 'T' + returnTime,
          additionalServices: []
        });
        
        console.log('Preis-Response:', priceData);
        setPricePerDay(priceData.basePrice / rentalDays);
      } catch (error) {
        console.error('Fehler beim Berechnen des Preises:', error);
        console.error('Error details:', error.response?.data);
        // Fallback: verwende Standardpreis wenn verfügbar
        setPricePerDay(null);
      } finally {
        setIsLoadingPrice(false);
      }
    };
    
    loadPrice();
  }, [vehicle, pickupDate, returnDate, rentalDays]);
  
  const totalPrice = pricePerDay ? pricePerDay * rentalDays : 0;
  
  // Handler für "Jetzt buchen"
  const handleBooking = () => {
    if (!pickupDate || !returnDate) {
      alert('Bitte wählen Sie Abhol- und Rückgabedatum aus.');
      return;
    }
    
    if (new Date(returnDate) <= new Date(pickupDate)) {
      alert('Rückgabedatum muss nach dem Abholdatum liegen.');
      return;
    }
    
    if (!pricePerDay) {
      alert('Preis konnte nicht berechnet werden. Bitte versuchen Sie es später erneut.');
      return;
    }
    
    console.log('=== Buchung starten ===');
    console.log('Fahrzeug:', vehicle);
    console.log('Vehicle ID:', vehicle.id);
    console.log('Datum:', pickupDate, 'bis', returnDate);
    console.log('Preis pro Tag:', pricePerDay);
    
    // WICHTIG: Alle Context-Updates synchron ausführen
    const vehicleData = {
      ...vehicle,
      pricePerDay: pricePerDay
    };
    
    console.log('Setze vehicleData:', vehicleData);
    console.log('vehicleData.id:', vehicleData.id);
    
    // 1. Fahrzeug mit Preis im Context setzen
    setVehicle(vehicleData);
    
    // 2. Filialen setzen (gleiche Filiale für Abholung und Rückgabe)
    setBranches({
      pickupBranchId: vehicle.branchId,
      pickupBranch: { id: vehicle.branchId, name: vehicle.branchName },
      returnBranchId: vehicle.branchId,
      returnBranch: { id: vehicle.branchId, name: vehicle.branchName }
    });
    
    // 3. Datum/Uhrzeit setzen mit intelligenter Zeitauswahl
    const pickupTime = getDefaultPickupTime(pickupDate);
    const returnTime = '10:00:00';
    
    setDates({
      pickupDateTime: pickupDate + 'T' + pickupTime,
      returnDateTime: returnDate + 'T' + returnTime
    });
    
    // 4. Zu Schritt 3 (Zeitraum) springen, damit Benutzer die Uhrzeit angeben können
    setStep(3);
    
    console.log('Alle Context updates ausgeführt, navigiere zum Wizard...');
    
    // Längere Verzögerung damit alle Context-Updates sicher durchlaufen
    setTimeout(() => {
      navigate('/booking/wizard', {
        state: { fromVehicleDetails: true }
      });
    }, 200);
  };
  
  if (isLoading) {
    return (
      <div className="min-h-screen bg-background-light flex items-center justify-center">
        <div className="text-center">
          <span className="material-symbols-outlined text-6xl text-primary animate-spin">
            progress_activity
          </span>
          <p className="mt-4 text-gray-600">Fahrzeugdaten werden geladen...</p>
        </div>
      </div>
    );
  }
  
  if (!vehicle) {
    return (
      <div className="min-h-screen bg-background-light flex items-center justify-center">
        <div className="text-center">
          <span className="material-symbols-outlined text-6xl text-red-600">error</span>
          <p className="mt-4 text-gray-900 font-bold">Fahrzeug nicht gefunden</p>
          <Link to="/vehicles" className="mt-4 inline-block text-primary hover:underline">
            Zurück zur Fahrzeugsuche
          </Link>
        </div>
      </div>
    );
  }
  
  // Bilder: entweder vom Backend oder Fallback
  const vehicleImages = [getVehicleImage(vehicle.brand, vehicle.model)];
  
  const specs = [
    { icon: 'event', label: vehicle.year?.toString() || 'N/A', sublabel: 'Baujahr' },
    { icon: 'speed', label: `${vehicle.mileage?.toLocaleString() || 0} km`, sublabel: 'Kilometerstand' },
    { icon: 'directions_car', label: vehicle.vehicleType || 'N/A', sublabel: 'Fahrzeugtyp' },
    { icon: 'confirmation_number', label: vehicle.licensePlate || 'N/A', sublabel: 'Kennzeichen' },
  ];

  return (
    <div className="min-h-screen bg-background-light">
      <main className="px-4 md:px-10 lg:px-20 xl:px-40 flex flex-1 justify-center py-5">
        <div className="flex flex-col w-full max-w-6xl flex-1">
          {/* Breadcrumb */}
          <div className="flex flex-wrap gap-2 p-4">
            <Link to="/" className="text-sm font-medium text-text-main/70 hover:text-primary">
              Home
            </Link>
            <span className="text-sm font-medium text-text-main/70">/</span>
            <Link
              to="/vehicles"
              className="text-sm font-medium text-text-main/70 hover:text-primary"
            >
              Fahrzeuge
            </Link>
            <span className="text-sm font-medium text-text-main/70">/</span>
            <span className="text-sm font-medium text-text-main">{vehicle.brand} {vehicle.model}</span>
          </div>

          {/* Image Gallery */}
          <div className="px-4 py-3">
            <div
              className="bg-cover bg-center flex flex-col justify-end overflow-hidden bg-gray-300 rounded-xl min-h-80 md:min-h-[450px]"
              style={{
                backgroundImage: `linear-gradient(0deg, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0) 25%), url("${vehicleImages[activeImageIndex]}")`,
              }}
            >
              {vehicleImages.length > 1 && (
                <div className="flex justify-center gap-2 p-5">
                  {vehicleImages.map((_, index) => (
                    <div
                      key={index}
                      className={`size-2 rounded-full ${index === activeImageIndex ? 'bg-white' : 'bg-white opacity-50'} cursor-pointer`}
                      onClick={() => setActiveImageIndex(index)}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Content */}
          <div className="flex flex-col lg:flex-row gap-8 mt-6">
            {/* Left Column: Details */}
            <div className="w-full lg:w-2/3">
              <div className="flex flex-wrap justify-between gap-3 p-4">
                <div className="flex min-w-72 flex-col gap-3">
                  <p className="text-4xl font-black leading-tight">
                    {vehicle.brand} {vehicle.model}
                  </p>
                  <p className="text-base font-normal text-gray-600">
                    {vehicle.vehicleType} • Baujahr {vehicle.year}
                  </p>
                </div>
              </div>

              {/* Specs Grid */}
              <div className="grid grid-cols-[repeat(auto-fit,minmax(158px,1fr))] gap-3 p-4">
                {specs.map((spec, index) => (
                  <div
                    key={index}
                    className="flex flex-1 gap-3 rounded-lg border border-gray-200 bg-gray-50 p-4 flex-col"
                  >
                    <span className="material-symbols-outlined text-primary text-2xl">
                      {spec.icon}
                    </span>
                    <div className="flex flex-col gap-1">
                      <h2 className="text-base font-bold">{spec.label}</h2>
                      <p className="text-sm font-normal text-gray-600">{spec.sublabel}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* Tabs */}
              <div className="p-4 mt-4">
                <div className="border-b border-gray-200">
                  <nav className="-mb-px flex space-x-8">
                    <button
                      onClick={() => setActiveTab('equipment')}
                      className={`whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'equipment' ? 'text-primary border-primary' : 'text-gray-500 hover:text-gray-700 hover:border-gray-300 border-transparent'}`}
                    >
                      Ausstattung
                    </button>
                    <button
                      onClick={() => setActiveTab('specs')}
                      className={`whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'specs' ? 'text-primary border-primary' : 'text-gray-500 hover:text-gray-700 hover:border-gray-300 border-transparent'}`}
                    >
                      Technische Daten
                    </button>
                    <button
                      onClick={() => setActiveTab('conditions')}
                      className={`whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'conditions' ? 'text-primary border-primary' : 'text-gray-500 hover:text-gray-700 hover:border-gray-300 border-transparent'}`}
                    >
                      Mietbedingungen
                    </button>
                  </nav>
                </div>
                <div className="pt-6">
                  {activeTab === 'equipment' && (
                    <div className="text-sm text-gray-700">
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div className="flex items-center gap-3">
                          <span className="material-symbols-outlined text-green-600">check_circle</span>
                          <span>Klimaanlage</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="material-symbols-outlined text-green-600">check_circle</span>
                          <span>Bluetooth</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="material-symbols-outlined text-green-600">check_circle</span>
                          <span>USB-Anschluss</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="material-symbols-outlined text-green-600">check_circle</span>
                          <span>Airbags</span>
                        </div>
                      </div>
                    </div>
                  )}
                  {activeTab === 'specs' && (
                    <div className="text-sm text-gray-700 space-y-2">
                      <div className="flex justify-between py-2 border-b">
                        <span className="font-medium">Marke:</span>
                        <span>{vehicle.brand}</span>
                      </div>
                      <div className="flex justify-between py-2 border-b">
                        <span className="font-medium">Modell:</span>
                        <span>{vehicle.model}</span>
                      </div>
                      <div className="flex justify-between py-2 border-b">
                        <span className="font-medium">Baujahr:</span>
                        <span>{vehicle.year}</span>
                      </div>
                      <div className="flex justify-between py-2 border-b">
                        <span className="font-medium">Kilometerstand:</span>
                        <span>{vehicle.mileage?.toLocaleString()} km</span>
                      </div>
                      <div className="flex justify-between py-2 border-b">
                        <span className="font-medium">Fahrzeugtyp:</span>
                        <span>{vehicle.vehicleType}</span>
                      </div>
                      <div className="flex justify-between py-2 border-b">
                        <span className="font-medium">Status:</span>
                        <span className="text-green-600 font-medium">{vehicle.status}</span>
                      </div>
                    </div>
                  )}
                  {activeTab === 'conditions' && (
                    <div className="text-sm text-gray-700 space-y-3">
                      <p><strong>Mindestalter:</strong> 21 Jahre</p>
                      <p><strong>Führerschein:</strong> Mindestens 1 Jahr im Besitz</p>
                      <p><strong>Kaution:</strong> Wird bei Abholung per Kreditkarte hinterlegt</p>
                      <p><strong>Tankregelung:</strong> Voll zu Voll</p>
                      <p><strong>Stornierung:</strong> Kostenfrei bis 24h vor Abholung</p>
                      <p><strong>Versicherung:</strong> Haftpflicht inklusive, Vollkasko optional</p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Right Column: Booking */}
            <div className="w-full lg:w-1/3 p-4">
              <div className="bg-gray-50 border border-gray-200 rounded-xl p-6 shadow-lg sticky top-24">
                <h3 className="text-xl font-bold mb-4">Fahrzeug mieten</h3>
                <div className="grid grid-cols-1 gap-4">
                  <div>
                    <label htmlFor="pickup-date" className="block text-sm font-medium mb-1">
                      Abholdatum
                    </label>
                    <input
                      type="date"
                      id="pickup-date"
                      value={pickupDate}
                      onChange={(e) => setPickupDate(e.target.value)}
                      min={today}
                      className="w-full rounded-lg border-gray-300 bg-white text-sm focus:border-primary focus:ring-primary p-2"
                    />
                  </div>
                  <div>
                    <label htmlFor="return-date" className="block text-sm font-medium mb-1">
                      Rückgabedatum
                    </label>
                    <input
                      type="date"
                      id="return-date"
                      value={returnDate}
                      onChange={(e) => setReturnDate(e.target.value)}
                      min={pickupDate || today}
                      className="w-full rounded-lg border-gray-300 bg-white text-sm focus:border-primary focus:ring-primary p-2"
                    />
                  </div>
                </div>
                <div className="mt-6 border-t border-gray-200 pt-6 space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Preis pro Tag</span>
                    {isLoadingPrice ? (
                      <span className="text-sm text-gray-500">Wird berechnet...</span>
                    ) : pricePerDay ? (
                      <span className="font-medium">{pricePerDay.toFixed(2)} €</span>
                    ) : (
                      <span className="text-sm text-gray-500">N/A</span>
                    )}
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Mietdauer</span>
                    <span className="font-medium">{rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'}</span>
                  </div>
                  <div className="flex justify-between font-bold text-lg border-t border-gray-200 pt-3 mt-3">
                    <span>Gesamtpreis</span>
                    {isLoadingPrice ? (
                      <span className="text-gray-500">Wird berechnet...</span>
                    ) : totalPrice > 0 ? (
                      <span className="text-primary">{totalPrice.toFixed(2)} €</span>
                    ) : (
                      <span className="text-gray-500">N/A</span>
                    )}
                  </div>
                  <p className="text-xs text-center text-gray-500">inkl. MwSt.</p>
                </div>
                <button 
                  onClick={handleBooking}
                  disabled={!pricePerDay || isLoadingPrice}
                  className="mt-6 w-full flex items-center justify-center overflow-hidden rounded-lg h-12 px-6 bg-secondary text-white text-base font-bold tracking-wide hover:opacity-90 shadow-md transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span className="material-symbols-outlined mr-2">shopping_cart</span>
                  <span>Jetzt buchen</span>
                </button>
                
                {/* Hinweis */}
                <div className="mt-4 bg-blue-50 border border-blue-200 rounded-lg p-3">
                  <div className="flex gap-2">
                    <span className="material-symbols-outlined text-blue-600 text-sm">info</span>
                    <p className="text-xs text-blue-800">
                      Der Preis ist eine Schätzung. Extras und Versicherungen können im nächsten Schritt hinzugefügt werden.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default VehicleDetailsPage;
