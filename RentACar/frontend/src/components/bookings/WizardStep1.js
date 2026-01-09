import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../../context/BookingContext';
import { getVehicleImage } from '../../utils/vehicleImages';

const WizardStep1 = () => {
  const { bookingData } = useBooking();
  const navigate = useNavigate();
  const { vehicle } = bookingData;

  // Validierung: Falls kein Fahrzeug vorhanden, zurück zur Suche.
  useEffect(() => {
    if (!vehicle) {
      alert('Bitte wählen Sie zuerst ein Fahrzeug aus.');
      navigate('/vehicles');
    }
  }, [vehicle, navigate]);

  if (!vehicle) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Fahrzeugauswahl bestätigen
        </h2>
        <p className="text-gray-600">
          Überprüfen Sie das ausgewählte Fahrzeug.
        </p>
      </div>

      {/* Fahrzeugkarte */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200">
        <div className="md:flex">
          {/* Fahrzeugbild */}
          <div className="md:w-2/5 bg-gray-100">
            <img
              src={getVehicleImage(vehicle.brand, vehicle.model)}
              alt={`${vehicle.brand} ${vehicle.model}`}
              className="w-full h-64 md:h-full object-cover"
            />
          </div>

          {/* Fahrzeugdetails */}
          <div className="md:w-3/5 p-6">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h3 className="text-2xl font-bold text-gray-900">
                  {vehicle.brand} {vehicle.model}
                </h3>
                <p className="text-gray-600 mt-1">{vehicle.vehicleType}</p>
              </div>
              <div className="text-right">
                <div className="text-3xl font-bold text-[#1976D2]">
                  {vehicle.pricePerDay || 0}€
                </div>
                <div className="text-sm text-gray-600">pro Tag</div>
              </div>
            </div>

            {/* Fahrzeugmerkmale */}
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-gray-600">
                  event
                </span>
                <span className="text-sm text-gray-700">
                  Baujahr: {vehicle.year}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-gray-600">
                  speed
                </span>
                <span className="text-sm text-gray-700">
                  {vehicle.mileage?.toLocaleString()} km
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-gray-600">
                  airline_seat_recline_normal
                </span>
                <span className="text-sm text-gray-700">
                  {vehicle.seats || 5} Sitze
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-gray-600">
                  luggage
                </span>
                <span className="text-sm text-gray-700">
                  {vehicle.doors || 4} Türen
                </span>
              </div>
            </div>

            {/* Status */}
            <div className="flex items-center gap-2 bg-green-50 border border-green-200 rounded-lg p-3">
              <span className="material-symbols-outlined text-green-600">
                check_circle
              </span>
              <span className="text-sm font-medium text-green-700">
                Fahrzeug verfügbar
              </span>
            </div>

            {/* Kennzeichen */}
            {vehicle.licensePlate && (
              <div className="mt-4 text-sm text-gray-500">
                Kennzeichen: {vehicle.licensePlate}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Hinweis */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex gap-3">
          <span className="material-symbols-outlined text-[#1976D2] flex-shrink-0">
            info
          </span>
          <div className="text-sm text-gray-700">
            <p className="font-medium text-gray-900 mb-1">Hinweis</p>
            <p>
              Im nächsten Schritt wählen Sie die Abhol- und Rückgabefiliale aus.
              Sie können das Fahrzeug an einer anderen Filiale zurückgeben.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WizardStep1;
