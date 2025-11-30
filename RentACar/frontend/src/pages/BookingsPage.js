import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import bookingService from '../services/bookingService';

/**
 * BookingsPage - Buchungsübersicht für Kunden
 * Konvertiert von Stitch Design: buchungsübersicht_(kunden)_1
 */
const BookingsPage = () => {
  const navigate = useNavigate();
  const [activeFilter, setActiveFilter] = useState('Alle');
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Lade Buchungen beim Mounten und wenn Filter sich ändert
  useEffect(() => {
    loadBookings();
  }, [activeFilter]);

  const loadBookings = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Filter-Mapping: Alle = kein Filter, Aktiv = REQUESTED|CONFIRMED|ACTIVE, Vergangen = COMPLETED|CANCELLED|EXPIRED
      let statusFilter = null;
      if (activeFilter === 'Aktiv') {
        // Backend unterstützt nur einen Status pro Query, daher laden wir alle und filtern client-seitig
        statusFilter = null; // Wir laden alle und filtern unten
      } else if (activeFilter === 'Vergangen') {
        statusFilter = null; // Gleiches hier
      }
      
      const data = await bookingService.getMyBookings(statusFilter);
      
      // Client-seitige Filterung für Aktiv/Vergangen
      let filteredData = data;
      if (activeFilter === 'Aktiv') {
        filteredData = data.filter(b => 
          ['REQUESTED', 'CONFIRMED', 'ACTIVE'].includes(b.status)
        );
      } else if (activeFilter === 'Vergangen') {
        filteredData = data.filter(b => 
          ['COMPLETED', 'CANCELLED', 'EXPIRED'].includes(b.status)
        );
      }
      
      // Sortierung nach Abholdatum (früheste zuerst)
      filteredData.sort((a, b) => {
        const dateA = new Date(a.pickupDateTime);
        const dateB = new Date(b.pickupDateTime);
        return dateA - dateB; // Aufsteigend sortieren - früheste Abholzeiten zuerst
      });
      
      setBookings(filteredData);
    } catch (err) {
      setError(err.message);
      console.error('Fehler beim Laden der Buchungen:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'REQUESTED':
        return 'bg-blue-100 text-blue-800';
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800';
      case 'ACTIVE':
        return 'bg-purple-100 text-purple-800';
      case 'COMPLETED':
        return 'bg-gray-100 text-gray-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      case 'EXPIRED':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
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

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const canCancel = (booking) => {
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

  return (
    <div className="min-h-screen bg-background-light">
      <main className="flex-grow">
        <div className="container mx-auto px-4 py-8 md:py-12">
          <div className="max-w-5xl mx-auto">
            {/* PageHeading */}
            <div className="flex flex-wrap justify-between items-center gap-4 mb-8">
              <h1 className="text-4xl font-black tracking-tighter">Meine Buchungen</h1>
              <button 
                onClick={() => navigate('/vehicles')}
                className="flex items-center gap-2 min-w-[84px] cursor-pointer justify-center overflow-hidden rounded-lg h-10 px-4 bg-secondary text-white text-sm font-bold shadow-sm hover:opacity-90 transition-colors"
              >
                <span className="material-symbols-outlined text-lg">add</span>
                <span>Neue Buchung</span>
              </button>
            </div>

            {/* SegmentedButtons */}
            <div className="mb-8">
              <div className="flex w-full md:w-auto h-12 items-center justify-center rounded-xl bg-gray-200 p-1.5">
                {['Alle', 'Aktiv', 'Vergangen'].map((filter) => (
                  <label
                    key={filter}
                    className={`flex cursor-pointer h-full grow items-center justify-center overflow-hidden rounded-lg px-4 transition-all ${activeFilter === filter ? 'bg-card-bg shadow-md text-primary' : 'text-gray-600'}`}
                  >
                    <span>{filter}</span>
                    <input
                      type="radio"
                      name="booking-filter"
                      value={filter}
                      checked={activeFilter === filter}
                      onChange={() => setActiveFilter(filter)}
                      className="invisible w-0"
                    />
                  </label>
                ))}
              </div>
            </div>

            {/* Bookings List */}
            <div className="space-y-4">
              {loading ? (
                <div className="text-center py-12">
                  <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                  <p className="mt-4 text-gray-600">Buchungen werden geladen...</p>
                </div>
              ) : error ? (
                <div className="text-center py-12">
                  <p className="text-red-600 mb-4">{error}</p>
                  <button
                    onClick={loadBookings}
                    className="px-4 py-2 bg-primary text-white rounded-lg hover:opacity-90"
                  >
                    Erneut versuchen
                  </button>
                </div>
              ) : bookings.length === 0 ? (
                <div className="text-center py-12">
                  <p className="text-gray-500 mb-4">Keine Buchungen gefunden.</p>
                  <button
                    onClick={() => navigate('/vehicles')}
                    className="px-6 py-3 bg-secondary text-white rounded-lg hover:opacity-90"
                  >
                    Jetzt Fahrzeug buchen
                  </button>
                </div>
              ) : (
                bookings.map((booking) => (
                  <div
                    key={booking.id}
                    className="bg-card-bg border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow"
                  >
                    <div className="flex flex-col md:flex-row gap-6">
                      {/* Vehicle Info */}
                      <div className="w-full md:w-48 flex-shrink-0">
                        <div className="aspect-video rounded-lg overflow-hidden bg-gray-100">
                          {/* Fallback für Fahrzeugbild */}
                          <div className="w-full h-full flex items-center justify-center text-gray-400">
                            <span className="material-symbols-outlined text-5xl">
                              directions_car
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* Booking Details */}
                      <div className="flex-grow">
                        <div className="flex flex-wrap justify-between items-start gap-4 mb-4">
                          <div>
                            <h3 className="text-xl font-bold mb-1">
                              {booking.fahrzeug.marke} {booking.fahrzeug.modell}
                            </h3>
                            <p className="text-sm text-gray-600">
                              Buchungs-Nr: {booking.buchungsnummer} | {booking.fahrzeug.kennzeichen}
                            </p>
                          </div>
                          <span
                            className={`px-3 py-1 rounded-full text-sm font-semibold ${getStatusColor(booking.status)}`}
                          >
                            {getStatusLabel(booking.status)}
                          </span>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 text-sm">
                          <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-lg">
                              location_on
                            </span>
                            <div>
                              <p className="text-gray-500">Abholfiliale</p>
                              <p className="font-medium">{booking.abholfiliale.name}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-lg">
                              calendar_today
                            </span>
                            <div>
                              <p className="text-gray-500">Abholdatum</p>
                              <p className="font-medium">{formatDateTime(booking.abholdatum)}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-lg">
                              event
                            </span>
                            <div>
                              <p className="text-gray-500">Rückgabedatum</p>
                              <p className="font-medium">{formatDateTime(booking.rueckgabedatum)}</p>
                            </div>
                          </div>
                        </div>

                        <div className="flex flex-wrap justify-between items-center mt-4 pt-4 border-t border-gray-200">
                          <div className="text-lg font-bold text-secondary">
                            {booking.gesamtpreis.toFixed(2)} {booking.waehrung}{' '}
                            <span className="text-sm font-normal text-gray-600">Gesamt</span>
                          </div>
                          <div className="flex gap-2">
                            <button
                              onClick={() => navigate(`/bookings/${booking.buchungsnummer}`)}
                              className="px-4 py-2 rounded-lg border border-primary text-primary hover:bg-primary/10 transition-colors text-sm font-medium"
                            >
                              Details
                            </button>
                            {canCancel(booking) && (
                              <button
                                onClick={() => navigate(`/bookings/${booking.buchungsnummer}`)}
                                className="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors text-sm font-medium"
                              >
                                Stornieren
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default BookingsPage;
