import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import bookingService from '../services/bookingService';

const BookingSuccessPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [bookingData, setBookingData] = useState(location.state?.bookingData || null);
  const [isLoading, setIsLoading] = useState(!bookingData);

  // Falls keine Daten im State, vom Backend laden
  useEffect(() => {
    if (!bookingData && id) {
      const fetchBooking = async () => {
        try {
          const data = await bookingService.getBookingById(id);
          setBookingData(data);
        } catch (error) {
          console.error('Fehler beim Laden der Buchung:', error);
        } finally {
          setIsLoading(false);
        }
      };
      fetchBooking();
    }
  }, [id, bookingData]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <span className="material-symbols-outlined text-6xl text-[#1976D2] animate-spin">
            progress_activity
          </span>
          <p className="mt-4 text-gray-600">Buchungsdaten werden geladen...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-12">
        <div className="max-w-3xl mx-auto">
          {/* Erfolgs-Header */}
          <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg shadow-2xl text-white p-8 mb-8 text-center">
            <div className="inline-block bg-white rounded-full p-4 mb-4">
              <span className="material-symbols-outlined text-green-600 text-6xl">
                check_circle
              </span>
            </div>
            <h1 className="text-4xl font-black mb-2">Buchung erfolgreich!</h1>
            <p className="text-green-100 text-lg">
              Ihre Buchung wurde erfolgreich erstellt und bestätigt.
            </p>
          </div>

          {/* Buchungsnummer */}
          <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6 mb-6">
            <div className="text-center">
              <p className="text-sm text-gray-600 mb-2">Ihre Buchungsnummer</p>
              <p className="text-4xl font-black text-[#1976D2] tracking-wider">
                {bookingData?.buchungsnummer || id}
              </p>
              <p className="text-sm text-gray-500 mt-2">
                Bitte notieren Sie sich diese Nummer für Ihre Unterlagen.
              </p>
            </div>
          </div>

          {/* Buchungsdetails */}
          <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden mb-6">
            <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] text-white px-6 py-3">
              <h2 className="font-bold flex items-center gap-2">
                <span className="material-symbols-outlined">description</span>
                Buchungsdetails
              </h2>
            </div>

            <div className="p-6 space-y-4">
              {/* Fahrzeug */}
              {bookingData?.fahrzeug && (
                <div className="pb-4 border-b border-gray-200">
                  <p className="text-sm text-gray-600 mb-1">Fahrzeug</p>
                  <p className="font-bold text-gray-900 text-lg">
                    {bookingData.fahrzeug.marke} {bookingData.fahrzeug.modell}
                  </p>
                  <p className="text-sm text-gray-600">
                    {bookingData.fahrzeug.fahrzeugtyp}
                  </p>
                </div>
              )}

              {/* Zeitraum */}
              {bookingData?.abholdatum && bookingData?.rueckgabedatum && (
                <div className="pb-4 border-b border-gray-200">
                  <p className="text-sm text-gray-600 mb-2">Mietzeitraum</p>
                  <div className="grid md:grid-cols-2 gap-4">
                    <div>
                      <p className="text-xs text-gray-500">Abholung</p>
                      <p className="font-medium text-gray-900">
                        {new Date(bookingData.abholdatum).toLocaleDateString('de-DE', {
                          weekday: 'short',
                          day: '2-digit',
                          month: '2-digit',
                          year: 'numeric',
                        })}
                      </p>
                      <p className="text-sm text-gray-600">
                        {new Date(bookingData.abholdatum).toLocaleTimeString('de-DE', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}{' '}
                        Uhr
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">Rückgabe</p>
                      <p className="font-medium text-gray-900">
                        {new Date(bookingData.rueckgabedatum).toLocaleDateString('de-DE', {
                          weekday: 'short',
                          day: '2-digit',
                          month: '2-digit',
                          year: 'numeric',
                        })}
                      </p>
                      <p className="text-sm text-gray-600">
                        {new Date(bookingData.rueckgabedatum).toLocaleTimeString('de-DE', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}{' '}
                        Uhr
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* Filialen */}
              {bookingData?.abholfiliale && bookingData?.rueckgabefiliale && (
                <div className="pb-4 border-b border-gray-200">
                  <p className="text-sm text-gray-600 mb-2">Filialen</p>
                  <div className="grid md:grid-cols-2 gap-4">
                    <div>
                      <p className="text-xs text-gray-500">Abholung</p>
                      <p className="font-medium text-gray-900">
                        {bookingData.abholfiliale.name}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">Rückgabe</p>
                      <p className="font-medium text-gray-900">
                        {bookingData.rueckgabefiliale.name}
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* Gesamtpreis */}
              <div className="pt-2">
                <p className="text-sm text-gray-600 mb-1">Gesamtpreis</p>
                <p className="text-3xl font-black text-[#1976D2]">
                  {bookingData?.gesamtpreis?.toFixed(2) || '0.00'}€
                </p>
              </div>
            </div>
          </div>

          {/* Bestätigungs-Email Hinweis */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <div className="flex gap-3">
              <span className="material-symbols-outlined text-[#1976D2] flex-shrink-0">
                email
              </span>
              <div className="text-sm text-gray-700">
                <p className="font-medium text-gray-900 mb-1">
                  Bestätigung per E-Mail
                </p>
                <p>
                  Wir haben Ihnen eine Buchungsbestätigung an Ihre E-Mail-Adresse
                  gesendet. Bitte überprüfen Sie auch Ihren Spam-Ordner.
                </p>
              </div>
            </div>
          </div>

          {/* Nächste Schritte */}
          <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden mb-8">
            <div className="bg-gradient-to-r from-[#1976D2] to-[#1565C0] text-white px-6 py-3">
              <h2 className="font-bold flex items-center gap-2">
                <span className="material-symbols-outlined">list_alt</span>
                Nächste Schritte
              </h2>
            </div>

            <div className="p-6">
              <ol className="space-y-4">
                <li className="flex gap-3">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-[#1976D2] text-white flex items-center justify-center font-bold">
                    1
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Führerschein mitbringen</p>
                    <p className="text-sm text-gray-600">
                      Bitte bringen Sie Ihren gültigen Führerschein zur Abholung mit.
                    </p>
                  </div>
                </li>
                <li className="flex gap-3">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-[#1976D2] text-white flex items-center justify-center font-bold">
                    2
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Pünktlich erscheinen</p>
                    <p className="text-sm text-gray-600">
                      Erscheinen Sie bitte pünktlich zur vereinbarten Abholzeit.
                    </p>
                  </div>
                </li>
                <li className="flex gap-3">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-[#1976D2] text-white flex items-center justify-center font-bold">
                    3
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Zahlungsmittel bereithalten</p>
                    <p className="text-sm text-gray-600">
                      Halten Sie eine Kreditkarte für die Kaution bereit.
                    </p>
                  </div>
                </li>
              </ol>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="grid md:grid-cols-2 gap-4">
            <button
              type="button"
              onClick={() => navigate('/bookings')}
              className="flex items-center justify-center gap-2 bg-[#1976D2] hover:bg-[#1565C0] text-white font-bold py-4 px-6 rounded-lg shadow-lg transition-colors"
            >
              <span className="material-symbols-outlined">event_note</span>
              Zu meinen Buchungen
            </button>

            <button
              type="button"
              onClick={() => navigate('/vehicles')}
              className="flex items-center justify-center gap-2 bg-white hover:bg-gray-50 text-[#1976D2] font-bold py-4 px-6 rounded-lg border-2 border-[#1976D2] transition-colors"
            >
              <span className="material-symbols-outlined">directions_car</span>
              Weitere Fahrzeuge
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingSuccessPage;
