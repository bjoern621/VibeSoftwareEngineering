import React from 'react';
import { useNavigate } from 'react-router-dom';
import getVehicleImage from '../../utils/vehicleImages';
import { safeToFixed } from '../../utils/numberUtils';

/**
 * VehicleCard - Wiederverwendbare Karte für die Fahrzeuganzeige
 *
 * Zeigt Fahrzeugdetails aus dem Backend an:
 * - Fahrzeugbild (modellspezifisch)
 * - Marke und Modell
 * - Sitze (aus VehicleType abgeleitet)
 * - Preis pro Tag
 * - Geschätzter Gesamtpreis (wenn verfügbar)
 *
 * @param {Object} vehicle - Fahrzeugdaten vom Backend (VehicleSearchResultDTO)
 * @param {Object} searchParams - Optionale Suchparameter für Buchung
 */
const VehicleCard = ({ vehicle, searchParams }) => {
  const navigate = useNavigate();

  const handleBooking = () => {
    // Navigation zur Fahrzeugdetailseite mit Query-Parametern
    const queryParams = new URLSearchParams();
    if (searchParams?.pickupDate) queryParams.append('pickupDate', searchParams.pickupDate);
    if (searchParams?.returnDate) queryParams.append('returnDate', searchParams.returnDate);

    navigate(`/vehicles/${vehicle.id}${queryParams.toString() ? '?' + queryParams.toString() : ''}`);
  };

  // Fahrzeugname aus Marke und Modell zusammensetzen
  const vehicleName = `${vehicle.brand} ${vehicle.model}`;

  // Modellspezifisches Bild ermitteln
  const vehicleImage = getVehicleImage(vehicle.brand, vehicle.model, vehicle.vehicleType);
  
  // Sitze aus VehicleType ableiten (falls nicht direkt vorhanden)
  const getSeatsFromType = (type) => {
    const seatsMap = {
      'COMPACT_CAR': 5,
      'SEDAN': 5,
      'SUV': 7,
      'VAN': 9,
    };
    return seatsMap[type] || 5;
  };
  
  const seats = vehicle.passengerCapacity || getSeatsFromType(vehicle.vehicleType);
  
  // Anzahl Tage berechnen wenn Daten vorhanden
  const calculateDays = () => {
    if (!searchParams?.pickupDate || !searchParams?.returnDate) return null;
    const start = new Date(searchParams.pickupDate);
    const end = new Date(searchParams.returnDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };
  
  const rentalDays = calculateDays();


  return (
    <div className="flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-card-bg shadow-sm transition-shadow hover:shadow-lg">
      <img
        className="h-48 w-full object-cover cursor-pointer"
        src={vehicleImage}
        alt={vehicleName}
        onClick={handleBooking}
      />
      <div className="flex flex-grow flex-col p-5">
        <h3 className="text-lg font-bold">{vehicleName}</h3>
        
        {/* Fahrzeugdetails */}
        <div className="mt-2 space-y-1 text-sm text-gray-600">
          <div className="flex items-center gap-1.5">
            <span className="material-symbols-outlined text-base">group</span> 
            {seats} Sitze
          </div>
          <div className="flex items-center gap-1.5">
            <span className="material-symbols-outlined text-base">category</span>
            {vehicle.vehicleType ? 
              (vehicle.vehicleType === 'COMPACT_CAR' ? 'Kleinwagen' :
               vehicle.vehicleType === 'SEDAN' ? 'Limousine' :
               vehicle.vehicleType === 'SUV' ? 'SUV' :
               vehicle.vehicleType === 'VAN' ? 'Transporter' : vehicle.vehicleType) 
              : 'Standard'}
          </div>
          {vehicle.year && (
            <div className="flex items-center gap-1.5">
              <span className="material-symbols-outlined text-base">calendar_month</span>
              Baujahr {vehicle.year}
            </div>
          )}
        </div>
        
        {/* Preisanzeige */}
        <div className="mt-auto pt-4 space-y-2">
          <div className="text-right">
            <p className="text-sm text-gray-500">ab</p>
            <p className="text-2xl font-bold text-secondary">
              {safeToFixed(vehicle?.pricePerDay, 2)}€
              <span className="text-base font-normal">/Tag</span>
            </p>
          </div>
          
          {/* Geschätzter Gesamtpreis */}
          {vehicle?.estimatedTotalPrice != null && rentalDays && (
             <div className="rounded-lg bg-primary/10 px-3 py-2 text-center">
               <p className="text-xs text-gray-600">Gesamtpreis für {rentalDays} Tag{rentalDays !== 1 ? 'e' : ''}</p>
               <p className="text-lg font-bold text-primary">
                {safeToFixed(vehicle?.estimatedTotalPrice, 2)}€
               </p>
             </div>
           )}
        </div>
        
        <button
          onClick={handleBooking}
          className="mt-4 w-full rounded-lg bg-primary py-2.5 font-bold text-white transition-opacity hover:opacity-90"
        >
          Jetzt buchen
        </button>
      </div>
    </div>
  );
};

export default VehicleCard;
