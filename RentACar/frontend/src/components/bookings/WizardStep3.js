import React, { useState, useEffect } from 'react';
import { useBooking } from '../../context/BookingContext';

const WizardStep3 = () => {
  const { bookingData, setDates, calculatePrice } = useBooking();

  // Initialisiere mit vorhandenen Daten oder Standardwerten
  const [pickupDate, setPickupDate] = useState('');
  const [pickupTime, setPickupTime] = useState('09:00');
  const [returnDate, setReturnDate] = useState('');
  const [returnTime, setReturnTime] = useState('09:00');
  const [errors, setErrors] = useState({});

  // Lade vorhandene Daten aus Context
  useEffect(() => {
    if (bookingData.pickupDateTime) {
      const pickup = new Date(bookingData.pickupDateTime);
      setPickupDate(pickup.toISOString().split('T')[0]);
      setPickupTime(
        pickup.toTimeString().slice(0, 5)
      );
    }
    if (bookingData.returnDateTime) {
      const returnD = new Date(bookingData.returnDateTime);
      setReturnDate(returnD.toISOString().split('T')[0]);
      setReturnTime(
        returnD.toTimeString().slice(0, 5)
      );
    }
  }, [bookingData.pickupDateTime, bookingData.returnDateTime]);

  // Validierung
  const validateDates = () => {
    const newErrors = {};
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    if (!pickupDate) {
      newErrors.pickupDate = 'Abholdatum ist erforderlich';
    } else {
      const pickup = new Date(pickupDate);
      if (pickup < now) {
        newErrors.pickupDate = 'Abholdatum muss in der Zukunft liegen';
      }
    }

    if (!returnDate) {
      newErrors.returnDate = 'Rückgabedatum ist erforderlich';
    } else if (pickupDate && returnDate) {
      const pickup = new Date(`${pickupDate}T${pickupTime}`);
      const returnD = new Date(`${returnDate}T${returnTime}`);

      if (returnD <= pickup) {
        newErrors.returnDate = 'Rückgabedatum muss nach dem Abholdatum liegen';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Sync mit Context
  useEffect(() => {
    if (pickupDate && pickupTime && returnDate && returnTime) {
      if (validateDates()) {
        const pickupDateTime = new Date(`${pickupDate}T${pickupTime}`);
        const returnDateTime = new Date(`${returnDate}T${returnTime}`);

        setDates({
          pickupDateTime: pickupDateTime.toISOString(),
          returnDateTime: returnDateTime.toISOString(),
        });

        // Preis neu berechnen
        calculatePrice();
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pickupDate, pickupTime, returnDate, returnTime]);

  // Berechne Anzahl Tage
  const calculateDays = () => {
    if (!pickupDate || !returnDate || !pickupTime || !returnTime) return 0;

    const pickup = new Date(`${pickupDate}T${pickupTime}`);
    const returnD = new Date(`${returnDate}T${returnTime}`);

    if (returnD <= pickup) return 0;

    const diffTime = Math.abs(returnD - pickup);
    return Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
  };

  const rentalDays = calculateDays();

  // Minimales Datum: Heute
  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Zeitraum festlegen
        </h2>
        <p className="text-gray-600">
          Wählen Sie Abhol- und Rückgabezeitpunkt.
        </p>
      </div>

      {/* Abholdatum */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <span className="material-symbols-outlined text-[#1976D2]">
            event_available
          </span>
          <h3 className="text-lg font-bold text-gray-900">Abholung</h3>
        </div>

        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Datum
            </label>
            <input
              type="date"
              value={pickupDate}
              min={today}
              onChange={(e) => setPickupDate(e.target.value)}
              className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-[#1976D2] focus:border-transparent ${
                errors.pickupDate ? 'border-red-500' : 'border-gray-300'
              }`}
              required
            />
            {errors.pickupDate && (
              <p className="mt-1 text-sm text-red-600">{errors.pickupDate}</p>
            )}
          </div>

          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Uhrzeit
            </label>
            <input
              type="time"
              value={pickupTime}
              onChange={(e) => setPickupTime(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#1976D2] focus:border-transparent"
              required
            />
          </div>
        </div>

        {pickupDate && pickupTime && (
          <div className="mt-4 p-3 bg-blue-50 rounded-lg">
            <p className="text-sm text-gray-700">
              <span className="font-medium">Abholung am:</span>{' '}
              {new Date(`${pickupDate}T${pickupTime}`).toLocaleDateString('de-DE', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}{' '}
              um {pickupTime} Uhr
            </p>
          </div>
        )}
      </div>

      {/* Rückgabedatum */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <span className="material-symbols-outlined text-[#FF9800]">
            event_busy
          </span>
          <h3 className="text-lg font-bold text-gray-900">Rückgabe</h3>
        </div>

        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Datum
            </label>
            <input
              type="date"
              value={returnDate}
              min={pickupDate || today}
              onChange={(e) => setReturnDate(e.target.value)}
              className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-[#1976D2] focus:border-transparent ${
                errors.returnDate ? 'border-red-500' : 'border-gray-300'
              }`}
              required
            />
            {errors.returnDate && (
              <p className="mt-1 text-sm text-red-600">{errors.returnDate}</p>
            )}
          </div>

          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Uhrzeit
            </label>
            <input
              type="time"
              value={returnTime}
              onChange={(e) => setReturnTime(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#1976D2] focus:border-transparent"
              required
            />
          </div>
        </div>

        {returnDate && returnTime && (
          <div className="mt-4 p-3 bg-orange-50 rounded-lg">
            <p className="text-sm text-gray-700">
              <span className="font-medium">Rückgabe am:</span>{' '}
              {new Date(`${returnDate}T${returnTime}`).toLocaleDateString('de-DE', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}{' '}
              um {returnTime} Uhr
            </p>
          </div>
        )}
      </div>

      {/* Mietdauer */}
      {rentalDays > 0 && (
        <div className="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg shadow-md border border-blue-200 p-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="material-symbols-outlined text-[#1976D2] text-3xl">
                calendar_month
              </span>
              <div>
                <p className="text-sm text-gray-600">Mietdauer</p>
                <p className="text-3xl font-bold text-gray-900">
                  {rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'}
                </p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm text-gray-600">Geschätzter Preis</p>
              <p className="text-2xl font-bold text-[#1976D2]">
                {(bookingData.basePrice * rentalDays).toFixed(2)}€
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Hinweis */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex gap-3">
          <span className="material-symbols-outlined text-[#1976D2] flex-shrink-0">
            info
          </span>
          <div className="text-sm text-gray-700">
            <p className="font-medium text-gray-900 mb-1">Hinweis</p>
            <p>
              Die Mindestmietdauer beträgt 1 Tag. Bei verspäteter Rückgabe können
              zusätzliche Kosten entstehen.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WizardStep3;
