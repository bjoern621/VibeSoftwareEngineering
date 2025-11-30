import React, { useState, useEffect } from 'react';
import { useBooking } from '../../context/BookingContext';

// Verfügbare Extras
const AVAILABLE_EXTRAS = [
  {
    id: 'gps',
    name: 'GPS Navigationssystem',
    description: 'Modernes GPS-Gerät mit aktuellen Karten',
    pricePerDay: 5.99,
    icon: 'map',
  },
  {
    id: 'child_seat',
    name: 'Kindersitz',
    description: 'Sicherheitskindersitz (0-18 kg)',
    pricePerDay: 8.99,
    icon: 'child_care',
  },
  {
    id: 'additional_driver',
    name: 'Zusatzfahrer',
    description: 'Ein weiterer Fahrer kann das Fahrzeug nutzen',
    pricePerDay: 7.99,
    icon: 'person_add',
  },
  {
    id: 'full_insurance',
    name: 'Vollkaskoversicherung',
    description: 'Vollkasko ohne Selbstbeteiligung',
    pricePerDay: 15.99,
    icon: 'shield',
  },
  {
    id: 'winter_tires',
    name: 'Winterreifen',
    description: 'Winterreifen für sichere Fahrt bei Schnee',
    pricePerDay: 4.99,
    icon: 'ac_unit',
  },
];

const WizardStep4 = () => {
  const { bookingData, setExtras, calculatePrice } = useBooking();
  const [selectedExtras, setSelectedExtras] = useState([]);

  // Lade vorhandene Extras aus Context
  useEffect(() => {
    if (bookingData.extras && bookingData.extras.length > 0) {
      setSelectedExtras(bookingData.extras);
    }
  }, [bookingData.extras]);
  
  // Berechne Preis beim ersten Laden (wichtig für Direkteinstieg bei Step 4)
  useEffect(() => {
    console.log('WizardStep4 geladen - berechne initialen Preis');
    calculatePrice();
  }, [calculatePrice]);

  // Toggle Extra
  const toggleExtra = (extra) => {
    const exists = selectedExtras.find((e) => e.id === extra.id);

    let updated;
    if (exists) {
      // Entfernen
      updated = selectedExtras.filter((e) => e.id !== extra.id);
    } else {
      // Hinzufügen
      updated = [
        ...selectedExtras,
        {
          id: extra.id,
          name: extra.name,
          pricePerDay: extra.pricePerDay,
          quantity: 1,
        },
      ];
    }

    setSelectedExtras(updated);
    setExtras(updated);
    // Preis neu berechnen
    setTimeout(() => calculatePrice(), 100);
  };

  // Ändere Menge
  const updateQuantity = (extraId, quantity) => {
    const updated = selectedExtras.map((e) =>
      e.id === extraId ? { ...e, quantity: Math.max(1, quantity) } : e
    );
    setSelectedExtras(updated);
    setExtras(updated);
    // Preis neu berechnen
    setTimeout(() => calculatePrice(), 100);
  };

  const isSelected = (extraId) => {
    return selectedExtras.some((e) => e.id === extraId);
  };

  const getQuantity = (extraId) => {
    const extra = selectedExtras.find((e) => e.id === extraId);
    return extra?.quantity || 1;
  };

  // Berechne Anzahl Tage
  const calculateDays = () => {
    if (!bookingData.pickupDateTime || !bookingData.returnDateTime) return 1;

    const pickup = new Date(bookingData.pickupDateTime);
    const returnD = new Date(bookingData.returnDateTime);

    const diffTime = Math.abs(returnD - pickup);
    return Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
  };

  const rentalDays = calculateDays();

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Zusatzleistungen
        </h2>
        <p className="text-gray-600">
          Wählen Sie optionale Extras für Ihre Buchung.
        </p>
      </div>

      {/* Buchungsübersicht (kompakt) */}
      {bookingData.vehicle && (
        <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] rounded-lg shadow-md text-white p-4">
          <div className="flex items-center justify-between flex-wrap gap-3">
            <div className="flex items-center gap-3">
              <span className="material-symbols-outlined text-2xl">directions_car</span>
              <div>
                <p className="font-bold">
                  {bookingData.vehicle.brand} {bookingData.vehicle.model}
                </p>
                <p className="text-sm text-blue-100">
                  {new Date(bookingData.pickupDateTime).toLocaleDateString('de-DE')} - {new Date(bookingData.returnDateTime).toLocaleDateString('de-DE')}
                </p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm text-blue-100">Grundpreis</p>
              <p className="text-xl font-bold">
                {(bookingData.basePrice * rentalDays).toFixed(2)}€
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Extras Grid */}
      <div className="grid md:grid-cols-2 gap-4">{AVAILABLE_EXTRAS.map((extra) => {
          const selected = isSelected(extra.id);
          const quantity = getQuantity(extra.id);

          return (
            <div
              key={extra.id}
              className={`
                bg-white rounded-lg shadow-md border-2 transition-all cursor-pointer
                ${
                  selected
                    ? 'border-[#1976D2] bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                }
              `}
              onClick={() => toggleExtra(extra)}
            >
              <div className="p-6">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-start gap-3">
                    <span
                      className={`material-symbols-outlined text-3xl ${
                        selected ? 'text-[#1976D2]' : 'text-gray-600'
                      }`}
                    >
                      {extra.icon}
                    </span>
                    <div>
                      <h3 className="font-bold text-gray-900">{extra.name}</h3>
                      <p className="text-sm text-gray-600 mt-1">
                        {extra.description}
                      </p>
                    </div>
                  </div>

                  {/* Checkbox */}
                  <div
                    className={`
                      w-6 h-6 rounded border-2 flex items-center justify-center flex-shrink-0
                      ${
                        selected
                          ? 'bg-[#1976D2] border-[#1976D2]'
                          : 'bg-white border-gray-300'
                      }
                    `}
                  >
                    {selected && (
                      <span className="material-symbols-outlined text-white text-lg">
                        check
                      </span>
                    )}
                  </div>
                </div>

                {/* Preis */}
                <div className="flex items-center justify-between mt-4">
                  <div>
                    <p className="text-xl font-bold text-[#1976D2]">
                      {extra.pricePerDay.toFixed(2)}€
                      <span className="text-sm text-gray-600 font-normal">
                        {' '}
                        / Tag
                      </span>
                    </p>
                    <p className="text-xs text-gray-500 mt-1">
                      {(extra.pricePerDay * rentalDays).toFixed(2)}€ für{' '}
                      {rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'}
                    </p>
                  </div>

                  {/* Quantity Selector (nur bei bestimmten Extras) */}
                  {selected && ['child_seat', 'additional_driver'].includes(extra.id) && (
                    <div
                      className="flex items-center gap-2"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <button
                        type="button"
                        onClick={() => updateQuantity(extra.id, quantity - 1)}
                        disabled={quantity <= 1}
                        className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
                      >
                        <span className="material-symbols-outlined text-lg">
                          remove
                        </span>
                      </button>
                      <span className="w-8 text-center font-medium">
                        {quantity}
                      </span>
                      <button
                        type="button"
                        onClick={() => updateQuantity(extra.id, quantity + 1)}
                        disabled={quantity >= 5}
                        className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
                      >
                        <span className="material-symbols-outlined text-lg">
                          add
                        </span>
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Zusammenfassung ausgewählter Extras */}
      {selectedExtras.length > 0 && (
        <div className="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg shadow-md border border-blue-200 p-6">
          <div className="flex items-center gap-2 mb-4">
            <span className="material-symbols-outlined text-[#1976D2]">
              checklist
            </span>
            <h3 className="text-lg font-bold text-gray-900">
              Ausgewählte Extras
            </h3>
          </div>

          <div className="space-y-2">
            {selectedExtras.map((extra) => (
              <div
                key={extra.id}
                className="flex items-center justify-between py-2 border-b border-blue-200 last:border-0"
              >
                <span className="text-sm text-gray-700">
                  {extra.name}
                  {extra.quantity > 1 && ` (${extra.quantity}x)`}
                </span>
                <span className="text-sm font-medium text-gray-900">
                  {(extra.pricePerDay * rentalDays * extra.quantity).toFixed(2)}€
                </span>
              </div>
            ))}

            <div className="pt-3 border-t-2 border-blue-300">
              <div className="flex items-center justify-between">
                <span className="font-bold text-gray-900">
                  Extras Gesamt
                </span>
                <span className="text-xl font-bold text-[#1976D2]">
                  {bookingData.extrasPrice?.toFixed(2) || '0.00'}€
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Hinweis wenn keine Extras */}
      {selectedExtras.length === 0 && (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
          <div className="flex gap-3">
            <span className="material-symbols-outlined text-gray-500 flex-shrink-0">
              info
            </span>
            <div className="text-sm text-gray-700">
              <p className="font-medium text-gray-900 mb-1">
                Keine Extras ausgewählt
              </p>
              <p>
                Sie können die Buchung ohne Extras fortsetzen oder optionale
                Zusatzleistungen auswählen.
              </p>
            </div>
          </div>
        </div>
      )}
      
      {/* Gesamtpreis Vorschau */}
      {bookingData.totalPrice > 0 && (
        <div className="bg-white rounded-lg shadow-lg border-2 border-[#1976D2] p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Gesamtpreis für {rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'}</p>
              <p className="text-3xl font-bold text-[#1976D2] mt-1">
                {bookingData.totalPrice.toFixed(2)}€
              </p>
              <p className="text-xs text-gray-500 mt-1">inkl. MwSt.</p>
            </div>
            <span className="material-symbols-outlined text-5xl text-[#1976D2]">
              receipt_long
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

export default WizardStep4;
