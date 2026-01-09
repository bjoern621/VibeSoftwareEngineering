import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../../context/BookingContext';
import { useAuth } from '../../context/AuthContext';
import bookingService from '../../services/bookingService';
import { getVehicleImage } from '../../utils/vehicleImages';

const WizardStep5 = () => {
  const { bookingData, resetBooking } = useBooking();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Berechne Anzahl Tage
  const calculateDays = () => {
    if (!bookingData.pickupDateTime || !bookingData.returnDateTime) return 1;

    const pickup = new Date(bookingData.pickupDateTime);
    const returnD = new Date(bookingData.returnDateTime);

    const diffTime = Math.abs(returnD - pickup);
    return Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
  };

  const rentalDays = calculateDays();
  const baseTotal = bookingData.basePrice * rentalDays;

  // Buchung absenden
  const handleSubmit = async () => {
    // Prüfen ob User eingeloggt ist
    if (!isAuthenticated || !user) {
      // Zum Login weiterleiten, mit Rückkehr zur aktuellen Seite
      navigate('/login', { state: { from: '/booking/wizard', message: 'Bitte melden Sie sich an, um die Buchung abzuschließen.' } });
      return;
    }
    
    // Validierung der Buchungsdaten
    console.log('=== Buchung absenden - Validierung ===');
    console.log('BookingData:', bookingData);
    
    if (!bookingData.vehicleId) {
      setError('Fehler: Kein Fahrzeug ausgewählt. Bitte starten Sie den Buchungsvorgang erneut.');
      console.error('FEHLER: vehicleId fehlt');
      return;
    }
    
    if (!bookingData.pickupBranchId || !bookingData.returnBranchId) {
      setError('Fehler: Abholungs- oder Rückgabefiliale fehlt. Bitte starten Sie den Buchungsvorgang erneut.');
      console.error('FEHLER: Branch IDs fehlen', {
        pickupBranchId: bookingData.pickupBranchId,
        returnBranchId: bookingData.returnBranchId
      });
      return;
    }
    
    if (!bookingData.pickupDateTime || !bookingData.returnDateTime) {
      setError('Fehler: Abhol- oder Rückgabedatum fehlt. Bitte starten Sie den Buchungsvorgang erneut.');
      console.error('FEHLER: Datum fehlt', {
        pickupDateTime: bookingData.pickupDateTime,
        returnDateTime: bookingData.returnDateTime
      });
      return;
    }
    
    setIsSubmitting(true);
    setError(null);

    try {
      console.log('Sende Buchungsanfrage an Backend...');
      const response = await bookingService.createBooking(bookingData);
      console.log('Buchung erfolgreich erstellt:', response);

      // Erfolgreich - zur Success-Page navigieren
      navigate(`/booking/success/${response.id || response.bookingId}`, {
        state: { bookingData: response },
      });
    } catch (err) {
      console.error('Fehler beim Erstellen der Buchung:', err);
      console.error('Error Response:', err.response?.data);
      setError(
        err.message || 'Die Buchung konnte nicht erstellt werden. Bitte versuchen Sie es erneut.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Buchungszusammenfassung
        </h2>
        <p className="text-gray-600">
          Überprüfen Sie Ihre Buchungsdetails vor der Bestätigung.
        </p>
      </div>

      {/* Fehleranzeige */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex gap-3">
            <span className="material-symbols-outlined text-red-600 flex-shrink-0">
              error
            </span>
            <div>
              <p className="font-medium text-red-900">Fehler</p>
              <p className="text-sm text-red-700 mt-1">{error}</p>
            </div>
          </div>
        </div>
      )}

      {/* Fahrzeug */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
        <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] text-white px-6 py-3">
          <h3 className="font-bold flex items-center gap-2">
            <span className="material-symbols-outlined">directions_car</span>
            Fahrzeug
          </h3>
        </div>
        <div className="p-6">
          <div className="flex items-center gap-4">
            <img
              src={getVehicleImage(bookingData.vehicle?.brand, bookingData.vehicle?.model)}
              alt={`${bookingData.vehicle?.brand} ${bookingData.vehicle?.model}`}
              className="w-32 h-24 object-cover rounded-lg"
            />
            <div>
              <p className="text-xl font-bold text-gray-900">
                {bookingData.vehicle?.brand} {bookingData.vehicle?.model}
              </p>
              <p className="text-gray-600">{bookingData.vehicle?.vehicleType}</p>
              <p className="text-lg font-bold text-[#1976D2] mt-2">
                {bookingData.basePrice?.toFixed(2)}€ / Tag
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Filialen */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
        <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] text-white px-6 py-3">
          <h3 className="font-bold flex items-center gap-2">
            <span className="material-symbols-outlined">location_on</span>
            Filialen
          </h3>
        </div>
        <div className="p-6 grid md:grid-cols-2 gap-6">
          <div>
            <p className="text-sm text-gray-600 mb-1">Abholung</p>
            <p className="font-bold text-gray-900">
              {bookingData.pickupBranch?.name}
            </p>
            <p className="text-sm text-gray-600 mt-1">
              {bookingData.pickupBranch?.address}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-600 mb-1">Rückgabe</p>
            <p className="font-bold text-gray-900">
              {bookingData.returnBranch?.name}
            </p>
            <p className="text-sm text-gray-600 mt-1">
              {bookingData.returnBranch?.address}
            </p>
          </div>
        </div>
      </div>

      {/* Zeitraum */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
        <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] text-white px-6 py-3">
          <h3 className="font-bold flex items-center gap-2">
            <span className="material-symbols-outlined">event</span>
            Zeitraum
          </h3>
        </div>
        <div className="p-6 grid md:grid-cols-2 gap-6">
          <div>
            <p className="text-sm text-gray-600 mb-1">Abholung</p>
            <p className="font-bold text-gray-900">
              {new Date(bookingData.pickupDateTime).toLocaleDateString('de-DE', {
                weekday: 'short',
                year: 'numeric',
                month: 'short',
                day: 'numeric',
              })}
            </p>
            <p className="text-sm text-gray-600">
              {new Date(bookingData.pickupDateTime).toLocaleTimeString('de-DE', {
                hour: '2-digit',
                minute: '2-digit',
              })}{' '}
              Uhr
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-600 mb-1">Rückgabe</p>
            <p className="font-bold text-gray-900">
              {new Date(bookingData.returnDateTime).toLocaleDateString('de-DE', {
                weekday: 'short',
                year: 'numeric',
                month: 'short',
                day: 'numeric',
              })}
            </p>
            <p className="text-sm text-gray-600">
              {new Date(bookingData.returnDateTime).toLocaleTimeString('de-DE', {
                hour: '2-digit',
                minute: '2-digit',
              })}{' '}
              Uhr
            </p>
          </div>
        </div>
        <div className="px-6 pb-6">
          <div className="bg-blue-50 rounded-lg p-4">
            <p className="text-sm text-gray-700">
              <span className="font-bold text-gray-900">Mietdauer:</span>{' '}
              {rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'}
            </p>
          </div>
        </div>
      </div>

      {/* Extras */}
      {bookingData.extras && bookingData.extras.length > 0 && (
        <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
          <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] text-white px-6 py-3">
            <h3 className="font-bold flex items-center gap-2">
              <span className="material-symbols-outlined">add_circle</span>
              Zusatzleistungen
            </h3>
          </div>
          <div className="p-6">
            <div className="space-y-3">
              {bookingData.extras.map((extra) => (
                <div
                  key={extra.id}
                  className="flex items-center justify-between py-2 border-b border-gray-200 last:border-0"
                >
                  <span className="text-gray-700">
                    {extra.name}
                    {extra.quantity > 1 && ` (${extra.quantity}x)`}
                  </span>
                  <span className="font-medium text-gray-900">
                    {(extra.pricePerDay * rentalDays * extra.quantity).toFixed(2)}€
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Preisübersicht */}
      <div className="bg-gradient-to-br from-[#1976D2] to-[#1565C0] rounded-lg shadow-lg text-white p-6">
        <h3 className="font-bold text-lg mb-4 flex items-center gap-2">
          <span className="material-symbols-outlined">receipt_long</span>
          Preisübersicht
        </h3>

        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-blue-100">
              Fahrzeugmiete ({rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'})
            </span>
            <span className="font-medium">{baseTotal.toFixed(2)}€</span>
          </div>

          {bookingData.extrasPrice > 0 && (
            <div className="flex justify-between items-center">
              <span className="text-blue-100">Zusatzleistungen</span>
              <span className="font-medium">
                {bookingData.extrasPrice.toFixed(2)}€
              </span>
            </div>
          )}

          <div className="border-t-2 border-blue-300 pt-3 mt-3">
            <div className="flex justify-between items-center">
              <span className="text-xl font-bold">Gesamtpreis</span>
              <span className="text-3xl font-bold">
                {bookingData.totalPrice?.toFixed(2)}€
              </span>
            </div>
          </div>
        </div>

        <p className="text-xs text-blue-100 mt-4">
          Inkl. gesetzlicher MwSt. Alle Preise in Euro.
        </p>
      </div>

      {/* Kundendaten */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
        <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] text-white px-6 py-3">
          <h3 className="font-bold flex items-center gap-2">
            <span className="material-symbols-outlined">person</span>
            Kundendaten
          </h3>
        </div>
        <div className="p-6">
          <p className="text-gray-700">
            <span className="font-medium">Name:</span> {user?.firstName} {user?.lastName}
          </p>
          <p className="text-gray-700 mt-2">
            <span className="font-medium">E-Mail:</span> {user?.email}
          </p>
        </div>
      </div>

      {/* Bestätigen Button */}
      <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6">
        <div className="flex items-start gap-3 mb-4">
          <input
            type="checkbox"
            id="terms"
            className="mt-1"
            required
          />
          <label htmlFor="terms" className="text-sm text-gray-700">
            Ich habe die{' '}
            <a href="#" className="text-[#1976D2] hover:underline">
              Allgemeinen Geschäftsbedingungen
            </a>{' '}
            und{' '}
            <a href="#" className="text-[#1976D2] hover:underline">
              Datenschutzbestimmungen
            </a>{' '}
            gelesen und akzeptiere diese.
          </label>
        </div>

        <button
          type="button"
          onClick={handleSubmit}
          disabled={isSubmitting}
          className="w-full bg-[#1976D2] hover:bg-[#1565C0] disabled:bg-gray-400 text-white font-bold py-4 px-6 rounded-lg shadow-lg transition-colors flex items-center justify-center gap-2"
        >
          {isSubmitting ? (
            <>
              <span className="material-symbols-outlined animate-spin">
                progress_activity
              </span>
              Buchung wird erstellt...
            </>
          ) : (
            <>
              <span className="material-symbols-outlined">check_circle</span>
              Jetzt verbindlich buchen
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default WizardStep5;
