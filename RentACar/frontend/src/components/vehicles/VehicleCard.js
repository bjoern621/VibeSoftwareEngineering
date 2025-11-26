import React from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * VehicleCard - Wiederverwendbare Karte für die Fahrzeuganzeige
 * @param {Object} vehicle - Fahrzeugdaten mit id, name, image, seats, transmission, pricePerDay
 */
const VehicleCard = ({ vehicle }) => {
  const navigate = useNavigate();

  const handleBooking = () => {
    navigate(`/vehicles/${vehicle.id}`);
  };

  return (
    <div className="flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-card-bg shadow-sm transition-shadow hover:shadow-lg">
      <img
        className="h-48 w-full object-cover cursor-pointer"
        src={vehicle.image}
        alt={vehicle.name}
        onClick={handleBooking}
      />
      <div className="flex flex-grow flex-col p-5">
        <h3 className="text-lg font-bold">{vehicle.name}</h3>
        <div className="mt-2 flex items-center gap-4 text-sm text-gray-600">
          <div className="flex items-center gap-1.5">
            <span className="material-symbols-outlined text-base">group</span> {vehicle.seats} Sitze
          </div>
          <div className="flex items-center gap-1.5">
            <span className="material-symbols-outlined text-base">auto_transmission</span>{' '}
            {vehicle.transmission}
          </div>
        </div>
        <div className="mt-auto pt-4 text-right">
          <p className="text-sm text-gray-500">ab</p>
          <p className="text-2xl font-bold text-secondary">
            {vehicle.pricePerDay}€<span className="text-base font-normal">/Tag</span>
          </p>
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
