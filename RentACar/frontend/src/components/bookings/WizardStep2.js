import React, { useState, useEffect } from 'react';
import { useBooking } from '../../context/BookingContext';

// Mock-Daten für Filialen (später durch API ersetzen)
const MOCK_BRANCHES = [
  { id: 1, name: 'Berlin Hauptbahnhof', address: 'Europaplatz 1, 10557 Berlin', openingHours: 'Mo-So: 06:00-22:00' },
  { id: 2, name: 'München Flughafen', address: 'Nordallee 25, 85356 München', openingHours: 'Mo-So: 05:00-23:00' },
  { id: 3, name: 'Hamburg City', address: 'Mönckebergstraße 7, 20095 Hamburg', openingHours: 'Mo-Fr: 08:00-20:00, Sa-So: 09:00-18:00' },
  { id: 4, name: 'Köln Hauptbahnhof', address: 'Breslauer Platz 1, 50668 Köln', openingHours: 'Mo-So: 07:00-21:00' },
  { id: 5, name: 'Frankfurt Flughafen', address: 'Terminal 1, 60549 Frankfurt', openingHours: 'Mo-So: 24/7' },
];

const WizardStep2 = () => {
  const { bookingData, setBranches } = useBooking();
  const [pickupBranchId, setPickupBranchId] = useState(
    bookingData.pickupBranchId || ''
  );
  const [returnBranchId, setReturnBranchId] = useState(
    bookingData.returnBranchId || ''
  );

  const selectedPickupBranch = MOCK_BRANCHES.find((b) => b.id === parseInt(pickupBranchId));
  const selectedReturnBranch = MOCK_BRANCHES.find((b) => b.id === parseInt(returnBranchId));

  // Sync mit Context
  useEffect(() => {
    if (pickupBranchId && returnBranchId) {
      setBranches({
        pickupBranchId: parseInt(pickupBranchId),
        pickupBranch: selectedPickupBranch,
        returnBranchId: parseInt(returnBranchId),
        returnBranch: selectedReturnBranch,
      });
    }
  }, [pickupBranchId, returnBranchId, selectedPickupBranch, selectedReturnBranch, setBranches]);

  const handleSameAsPickup = () => {
    setReturnBranchId(pickupBranchId);
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Filialauswahl
        </h2>
        <p className="text-gray-600">
          Wählen Sie Ihre Abhol- und Rückgabefiliale aus.
        </p>
      </div>

      {/* Abholfiliale */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <span className="material-symbols-outlined text-[#1976D2]">
            location_on
          </span>
          <h3 className="text-lg font-bold text-gray-900">Abholfiliale</h3>
        </div>

        <label className="block mb-2 text-sm font-medium text-gray-700">
          Filiale auswählen
        </label>
        <select
          value={pickupBranchId}
          onChange={(e) => setPickupBranchId(e.target.value)}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#1976D2] focus:border-transparent"
          required
        >
          <option value="">-- Bitte wählen --</option>
          {MOCK_BRANCHES.map((branch) => (
            <option key={branch.id} value={branch.id}>
              {branch.name}
            </option>
          ))}
        </select>

        {/* Filialdetails anzeigen */}
        {selectedPickupBranch && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div className="flex items-start gap-3">
              <span className="material-symbols-outlined text-gray-600">
                place
              </span>
              <div className="flex-grow">
                <p className="font-medium text-gray-900">
                  {selectedPickupBranch.name}
                </p>
                <p className="text-sm text-gray-600 mt-1">
                  {selectedPickupBranch.address}
                </p>
                <div className="flex items-center gap-2 mt-2">
                  <span className="material-symbols-outlined text-sm text-gray-500">
                    schedule
                  </span>
                  <p className="text-sm text-gray-600">
                    {selectedPickupBranch.openingHours}
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Rückgabefiliale */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-[#FF9800]">
              location_on
            </span>
            <h3 className="text-lg font-bold text-gray-900">Rückgabefiliale</h3>
          </div>

          {pickupBranchId && (
            <button
              type="button"
              onClick={handleSameAsPickup}
              className="text-sm font-medium text-[#1976D2] hover:text-[#1565C0] flex items-center gap-1"
            >
              <span className="material-symbols-outlined text-lg">
                content_copy
              </span>
              Wie Abholfiliale
            </button>
          )}
        </div>

        <label className="block mb-2 text-sm font-medium text-gray-700">
          Filiale auswählen
        </label>
        <select
          value={returnBranchId}
          onChange={(e) => setReturnBranchId(e.target.value)}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#1976D2] focus:border-transparent"
          required
        >
          <option value="">-- Bitte wählen --</option>
          {MOCK_BRANCHES.map((branch) => (
            <option key={branch.id} value={branch.id}>
              {branch.name}
            </option>
          ))}
        </select>

        {/* Filialdetails anzeigen */}
        {selectedReturnBranch && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <div className="flex items-start gap-3">
              <span className="material-symbols-outlined text-gray-600">
                place
              </span>
              <div className="flex-grow">
                <p className="font-medium text-gray-900">
                  {selectedReturnBranch.name}
                </p>
                <p className="text-sm text-gray-600 mt-1">
                  {selectedReturnBranch.address}
                </p>
                <div className="flex items-center gap-2 mt-2">
                  <span className="material-symbols-outlined text-sm text-gray-500">
                    schedule
                  </span>
                  <p className="text-sm text-gray-600">
                    {selectedReturnBranch.openingHours}
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Hinweis bei unterschiedlichen Filialen */}
        {pickupBranchId && returnBranchId && pickupBranchId !== returnBranchId && (
          <div className="mt-4 bg-amber-50 border border-amber-200 rounded-lg p-3">
            <div className="flex gap-2">
              <span className="material-symbols-outlined text-amber-600 text-sm">
                info
              </span>
              <p className="text-sm text-amber-800">
                Einwegmiete: Sie geben das Fahrzeug an einer anderen Filiale zurück.
                Es kann eine zusätzliche Gebühr anfallen.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default WizardStep2;
