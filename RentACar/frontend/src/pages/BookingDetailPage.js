import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import bookingService from '../services/bookingService';
import CancellationModal from '../components/CancellationModal';

/**
 * BookingDetailPage - Detailansicht einer Buchung
 * Zeigt alle Buchungsdetails, Zusatzkosten und ermöglicht Stornierung
 */
const BookingDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [additionalCosts, setAdditionalCosts] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    loadBookingDetails();
  }, [id]);

  const loadBookingDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const bookingData = await bookingService.getBookingById(id);
      setBooking(bookingData);
      
      // Lade Zusatzkosten nur wenn Buchung aktiv oder abgeschlossen ist
      if (['ACTIVE', 'COMPLETED'].includes(bookingData.status)) {
        try {
          const costs = await bookingService.getAdditionalCosts(id);
          setAdditionalCosts(costs);
        } catch (err) {
          console.warn('Zusatzkosten nicht verfügbar:', err);
          // Fehler ignorieren, Zusatzkosten sind optional
        }
      }
    } catch (err) {
      setError(err.message);
      console.error('Fehler beim Laden der Buchungsdetails:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelBooking = async (reason) => {
    try {
      setCancelling(true);
      await bookingService.cancelBooking(id, reason);
      
      // Erfolg: Buchung neu laden
      await loadBookingDetails();
      setShowCancelModal(false);
      
      // Optional: Erfolgsmeldung anzeigen
      alert('Buchung erfolgreich storniert.');
    } catch (err) {
      console.error('Fehler beim Stornieren:', err);
      alert(err.message || 'Stornierung fehlgeschlagen.');
    } finally {
      setCancelling(false);
    }
  };

  const canCancel = () => {
    if (!booking) return false;
    
    // Nur REQUESTED oder CONFIRMED können storniert werden
    if (!['REQUESTED', 'CONFIRMED'].includes(booking.status)) {
      return false;
    }
    
    // Prüfe 24h-Frist
    const now = new Date();
    const pickupDate = new Date(booking.abholdatum);
    const hoursUntilPickup = (pickupDate - now) / (1000 * 60 * 60);
    return hoursUntilPickup >= 24;
  };

  const getStatusColor = (status) => {
    const colors = {
      REQUESTED: 'bg-blue-100 text-blue-800',
      CONFIRMED: 'bg-green-100 text-green-800',
      ACTIVE: 'bg-purple-100 text-purple-800',
      COMPLETED: 'bg-gray-100 text-gray-800',
      CANCELLED: 'bg-red-100 text-red-800',
      EXPIRED: 'bg-orange-100 text-orange-800',
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  const getStatusLabel = (status) => {
    const labels = {
      REQUESTED: 'Angefragt',
      CONFIRMED: 'Bestätigt',
      ACTIVE: 'Aktiv',
      COMPLETED: 'Abgeschlossen',
      CANCELLED: 'Storniert',
      EXPIRED: 'Abgelaufen',
    };
    return labels[status] || status;
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('de-DE', {
      weekday: 'short',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatCurrency = (amount, currency = 'EUR') => {
    return `${amount.toFixed(2)} ${currency}`;
  };

  const getAdditionalServiceInfo = (service) => {
    const services = {
      CHILD_SEAT: { label: 'Kindersitz', price: 5.00 },
      GPS: { label: 'Navigationssystem', price: 8.00 },
      ADDITIONAL_DRIVER: { label: 'Zusätzlicher Fahrer', price: 12.00 },
      FULL_INSURANCE: { label: 'Vollkasko ohne SB', price: 15.00 },
      WINTER_TIRES: { label: 'Winterreifen', price: 6.00 },
      ROOF_RACK: { label: 'Dachgepäckträger', price: 7.00 },
    };
    return services[service] || { label: service, price: 0 };
  };

  const calculateRentalDays = () => {
    if (!booking?.abholdatum || !booking?.rueckgabedatum) return 0;
    const pickup = new Date(booking.abholdatum);
    const returnDate = new Date(booking.rueckgabedatum);
    const diffTime = Math.abs(returnDate - pickup);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays || 1; // Mindestens 1 Tag
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background-light flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
          <p className="mt-4 text-gray-600">Buchungsdetails werden geladen...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-background-light flex items-center justify-center">
        <div className="text-center max-w-md mx-auto px-4">
          <span className="material-symbols-outlined text-red-500 text-6xl mb-4">error</span>
          <h2 className="text-2xl font-bold mb-4">Fehler</h2>
          <p className="text-gray-600 mb-6">{error}</p>
          <div className="flex gap-4 justify-center">
            <button
              onClick={loadBookingDetails}
              className="px-6 py-3 bg-primary text-white rounded-lg hover:opacity-90"
            >
              Erneut versuchen
            </button>
            <button
              onClick={() => navigate('/bookings')}
              className="px-6 py-3 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300"
            >
              Zurück zur Übersicht
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!booking) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background-light">
      <main className="container mx-auto px-4 py-8 md:py-12">
        <div className="max-w-4xl mx-auto">
          {/* Header */}
          <div className="mb-6">
            <button
              onClick={() => navigate('/bookings')}
              className="flex items-center gap-2 text-primary hover:underline mb-4"
            >
              <span className="material-symbols-outlined">arrow_back</span>
              Zurück zur Übersicht
            </button>
            
            <div className="flex flex-wrap justify-between items-start gap-4">
              <div>
                <h1 className="text-3xl font-black tracking-tighter mb-2">
                  Buchungsdetails
                </h1>
                <p className="text-gray-600">Buchungs-Nr: {booking.buchungsnummer}</p>
              </div>
              <span
                className={`px-4 py-2 rounded-full text-sm font-semibold ${getStatusColor(booking.status)}`}
              >
                {getStatusLabel(booking.status)}
              </span>
            </div>
          </div>

          {/* Fahrzeugdaten */}
          <div className="bg-card-bg border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
              <span className="material-symbols-outlined text-primary">directions_car</span>
              Fahrzeug
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-500">Marke & Modell</p>
                <p className="font-medium text-lg">
                  {booking.fahrzeug.marke} {booking.fahrzeug.modell}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Kennzeichen</p>
                <p className="font-medium">{booking.fahrzeug.kennzeichen}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Fahrzeugtyp</p>
                <p className="font-medium">{booking.fahrzeug.fahrzeugtyp}</p>
              </div>
            </div>
          </div>

          {/* Abhol- und Rückgabedaten */}
          <div className="bg-card-bg border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
              <span className="material-symbols-outlined text-primary">event</span>
              Mietdauer
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <p className="text-sm text-gray-500 mb-2">Abholung</p>
                <div className="space-y-2">
                  <p className="font-medium">{formatDateTime(booking.abholdatum)}</p>
                  <div className="flex items-start gap-2">
                    <span className="material-symbols-outlined text-primary text-sm mt-0.5">location_on</span>
                    <div>
                      <p className="font-medium">{booking.abholfiliale.name}</p>
                      <p className="text-sm text-gray-600">{booking.abholfiliale.adresse}</p>
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <p className="text-sm text-gray-500 mb-2">Rückgabe</p>
                <div className="space-y-2">
                  <p className="font-medium">{formatDateTime(booking.rueckgabedatum)}</p>
                  <div className="flex items-start gap-2">
                    <span className="material-symbols-outlined text-primary text-sm mt-0.5">location_on</span>
                    <div>
                      <p className="font-medium">{booking.rueckgabefiliale.name}</p>
                      <p className="text-sm text-gray-600">{booking.rueckgabefiliale.adresse}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Preisübersicht */}
          <div className="bg-card-bg border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
              <span className="material-symbols-outlined text-primary">payments</span>
              Preisübersicht
            </h2>
            
            <div className="space-y-3">
              <div className="flex justify-between items-center pb-3 border-b">
                <span className="text-gray-600">Buchungspreis</span>
                <span className="font-medium">{formatCurrency(booking.gesamtpreis, booking.waehrung)}</span>
              </div>

              {/* Zusatzleistungen */}
              {booking.zusatzleistungen && booking.zusatzleistungen.length > 0 && (
                <div className="pt-3 border-t">
                  <p className="text-sm text-gray-500 mb-2">Zusatzleistungen</p>
                  <div className="space-y-1 text-sm">
                    {booking.zusatzleistungen.map((service, idx) => {
                      const serviceInfo = getAdditionalServiceInfo(service);
                      const rentalDays = calculateRentalDays();
                      const totalPrice = serviceInfo.price * rentalDays;
                      return (
                        <div key={idx} className="flex justify-between items-center">
                          <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-xs text-primary">check_circle</span>
                            <span className="text-gray-700">
                              {serviceInfo.label}
                              <span className="text-gray-500 text-xs ml-1">
                                ({serviceInfo.price.toFixed(2)} €/Tag × {rentalDays} {rentalDays === 1 ? 'Tag' : 'Tage'})
                              </span>
                            </span>
                          </div>
                          <span className="font-medium">{totalPrice.toFixed(2)} {booking.waehrung}</span>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Zusatzkosten */}
              {additionalCosts && additionalCosts.totalAdditionalCost > 0 && (
                <div className="pt-3 border-t">
                  <p className="text-sm text-gray-500 mb-2">Zusatzkosten</p>
                  <div className="space-y-1 text-sm">
                    {additionalCosts.lateFee > 0 && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Verspätungsgebühr</span>
                        <span>{formatCurrency(additionalCosts.lateFee, booking.waehrung)}</span>
                      </div>
                    )}
                    {additionalCosts.excessMileageFee > 0 && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Mehrkilometer</span>
                        <span>{formatCurrency(additionalCosts.excessMileageFee, booking.waehrung)}</span>
                      </div>
                    )}
                    {additionalCosts.damageCost > 0 && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Schadenskosten</span>
                        <span>{formatCurrency(additionalCosts.damageCost, booking.waehrung)}</span>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Gesamtpreis */}
              <div className="flex justify-between items-center pt-3 border-t-2 border-gray-300">
                <span className="text-lg font-bold">Gesamtpreis</span>
                <span className="text-xl font-bold text-secondary">
                  {formatCurrency(
                    booking.gesamtpreis + (additionalCosts?.totalAdditionalCost || 0),
                    booking.waehrung
                  )}
                </span>
              </div>
            </div>
          </div>

          {/* Stornierungsgrund (falls storniert) */}
          {booking.status === 'CANCELLED' && booking.stornierungsgrund && (
            <div className="bg-red-50 border border-red-200 rounded-xl p-6 shadow-sm mb-6">
              <h2 className="text-xl font-bold mb-2 flex items-center gap-2 text-red-800">
                <span className="material-symbols-outlined">cancel</span>
                Stornierungsgrund
              </h2>
              <p className="text-gray-700">{booking.stornierungsgrund}</p>
            </div>
          )}

          {/* Aktionen */}
          <div className="flex flex-wrap gap-4">
            <button
              onClick={() => navigate('/bookings')}
              className="px-6 py-3 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Zurück
            </button>
            
            {canCancel() && (
              <button
                onClick={() => setShowCancelModal(true)}
                className="px-6 py-3 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors flex items-center gap-2"
              >
                <span className="material-symbols-outlined">cancel</span>
                Buchung stornieren
              </button>
            )}
          </div>
        </div>
      </main>

      {/* Stornierungsmodal */}
      {showCancelModal && (
        <CancellationModal
          onConfirm={handleCancelBooking}
          onCancel={() => setShowCancelModal(false)}
          loading={cancelling}
        />
      )}
    </div>
  );
};

export default BookingDetailPage;
