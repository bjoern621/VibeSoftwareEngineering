import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import bookingService from '../services/bookingService';

const STATUS_MAPPING = {
  REQUESTED: { label: 'Angefragt', color: 'info' },
  CONFIRMED: { label: 'Bestätigt', color: 'success' },
  ACTIVE: { label: 'Aktiv', color: 'purple' },
  COMPLETED: { label: 'Abgeschlossen', color: 'gray' },
  CANCELLED: { label: 'Storniert', color: 'danger' },
  EXPIRED: { label: 'Abgelaufen', color: 'warning' },
};

const BookingManagementPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [actionLoading, setActionLoading] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await bookingService.getAllBookings(statusFilter || null);
      setBookings(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    if (!user || (user.role !== 'EMPLOYEE' && user.role !== 'ADMIN')) {
      navigate('/login');
      return;
    }
    loadData();
  }, [user, navigate, loadData]);

  const getStatusClasses = (color) => {
    const colors = {
      success: 'bg-green-100 text-green-800',
      warning: 'bg-yellow-100 text-yellow-800',
      info: 'bg-blue-100 text-blue-800',
      danger: 'bg-red-100 text-red-800',
      purple: 'bg-purple-100 text-purple-800',
      gray: 'bg-gray-100 text-gray-800',
    };
    return colors[color] || 'bg-gray-100 text-gray-800';
  };

  const getStatusDotClasses = (color) => {
    const colors = {
      success: 'bg-green-800',
      warning: 'bg-yellow-800',
      info: 'bg-blue-800',
      danger: 'bg-red-800',
      purple: 'bg-purple-800',
      gray: 'bg-gray-800',
    };
    return colors[color] || 'bg-gray-800';
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

  const handleConfirmBooking = async (bookingId) => {
    setActionLoading(bookingId);
    setError(null);
    setSuccessMessage(null);
    try {
      await bookingService.confirmBooking(bookingId);
      setSuccessMessage(`Buchung #${bookingId} wurde erfolgreich bestätigt.`);
      await loadData();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleCancelBooking = async (bookingId) => {
    setActionLoading(bookingId);
    setError(null);
    setSuccessMessage(null);
    try {
      await bookingService.cancelBooking(bookingId, 'Storniert durch Mitarbeiter');
      setSuccessMessage(`Buchung #${bookingId} wurde storniert.`);
      await loadData();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const filteredBookings = bookings.filter((booking) => {
    const searchLower = searchTerm.toLowerCase();
    const matchesSearch =
      booking.fahrzeug?.marke?.toLowerCase().includes(searchLower) ||
      booking.fahrzeug?.modell?.toLowerCase().includes(searchLower) ||
      booking.fahrzeug?.kennzeichen?.toLowerCase().includes(searchLower) ||
      booking.buchungsnummer?.toString().includes(searchLower);
    return matchesSearch;
  });

  const resetFilters = () => {
    setSearchTerm('');
    setStatusFilter('');
  };

  if (loading && bookings.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="relative flex min-h-screen w-full">
      <aside className="flex w-64 flex-col gap-y-6 border-r border-gray-200 p-4 sticky top-0 h-screen bg-gray-50">
        <div className="flex items-center gap-3">
          <div className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10 bg-primary flex items-center justify-center text-white font-bold">
            {user?.firstName?.charAt(0) || 'U'}
          </div>
          <div className="flex flex-col">
            <h1 className="text-base font-medium">
              {user?.firstName} {user?.lastName}
            </h1>
            <p className="text-gray-600 text-sm font-normal">
              {user?.role === 'ADMIN' ? 'Administrator' : 'Mitarbeiter'}
            </p>
          </div>
        </div>

        <nav className="flex flex-col gap-2 flex-1">
          <a href="/" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">home</span>
            <p className="text-sm font-medium">Startseite</p>
          </a>
          <a
            href="/employee/bookings"
            className="flex items-center gap-3 px-3 py-2 rounded-lg bg-primary/20 text-primary"
          >
            <span className="material-symbols-outlined">calendar_month</span>
            <p className="text-sm font-medium">Buchungsverwaltung</p>
          </a>
          <a
            href="/employee/vehicles"
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200"
          >
            <span className="material-symbols-outlined">directions_car</span>
            <p className="text-sm font-medium">Fahrzeugverwaltung</p>
          </a>
          <a
            href="/employee/check-in-out"
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200"
          >
            <span className="material-symbols-outlined">swap_horiz</span>
            <p className="text-sm font-medium">Check-in/out</p>
          </a>
        </nav>

        <div className="flex flex-col gap-1">
          <button
            onClick={logout}
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200 w-full text-left"
          >
            <span className="material-symbols-outlined">logout</span>
            <p className="text-sm font-medium">Abmelden</p>
          </button>
        </div>
      </aside>

      <main className="flex-1 p-8 bg-background-light">
        <div className="mx-auto max-w-7xl">
          {error && (
            <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-lg flex justify-between items-center">
              <span>{error}</span>
              <button onClick={() => setError(null)} className="text-red-700 hover:text-red-900">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
          )}

          {successMessage && (
            <div className="mb-4 p-4 bg-green-100 text-green-700 rounded-lg flex justify-between items-center">
              <span>{successMessage}</span>
              <button
                onClick={() => setSuccessMessage(null)}
                className="text-green-700 hover:text-green-900"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
          )}

          <header className="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div className="flex flex-col">
              <h1 className="text-4xl font-black tracking-tighter">Buchungsverwaltung</h1>
              <p className="text-gray-600 text-base">Verwalten und bestätigen Sie Buchungen.</p>
            </div>
          </header>

          <div className="mb-4 space-y-4">
            <div className="flex w-full h-12">
              <div className="flex bg-gray-100 items-center justify-center pl-4 rounded-l-lg">
                <span className="material-symbols-outlined text-gray-600">search</span>
              </div>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="form-input flex w-full min-w-0 flex-1 rounded-r-lg border-none bg-gray-100 h-full placeholder:text-gray-500 px-4 pl-2 text-base font-normal focus:ring-2 focus:ring-primary/50"
                placeholder="Suche nach Buchungsnummer, Fahrzeug..."
              />
            </div>

            <div className="flex flex-wrap gap-3">
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="h-8 rounded-lg bg-gray-100 px-3 text-sm font-medium focus:ring-2 focus:ring-primary/50"
              >
                <option value="">Alle Status</option>
                {Object.entries(STATUS_MAPPING).map(([key, { label }]) => (
                  <option key={key} value={key}>
                    {label}
                  </option>
                ))}
              </select>
              <button
                onClick={resetFilters}
                className="flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-lg text-gray-600 pl-3 pr-3 hover:bg-gray-100"
              >
                <p className="text-sm font-medium">Filter zurücksetzen</p>
              </button>
              <button
                onClick={loadData}
                disabled={loading}
                className="flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-primary/10 text-primary pl-3 pr-3 hover:bg-primary/20"
              >
                <span className="material-symbols-outlined text-sm">refresh</span>
                <p className="text-sm font-medium">Aktualisieren</p>
              </button>
            </div>
          </div>

          <div className="overflow-hidden rounded-lg border border-gray-200">
            <div className="overflow-x-auto">
              <table className="w-full min-w-max">
                <thead>
                  <tr className="bg-gray-50">
                    <th className="px-4 py-3 text-left text-sm font-medium">Buchungs-Nr.</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Fahrzeug</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Abholdatum</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Rückgabedatum</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Filiale</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Preis</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Status</th>
                    <th className="px-4 py-3 text-right text-sm font-medium">Aktionen</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredBookings.length === 0 ? (
                    <tr>
                      <td colSpan="8" className="px-4 py-8 text-center text-gray-500">
                        Keine Buchungen gefunden.
                      </td>
                    </tr>
                  ) : (
                    filteredBookings.map((booking) => {
                      const statusInfo = STATUS_MAPPING[booking.status] || {
                        label: booking.status,
                        color: 'gray',
                      };
                      return (
                        <tr key={booking.buchungsnummer}>
                          <td className="h-[72px] px-4 py-2 text-sm font-medium">
                            #{booking.buchungsnummer}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-sm">
                            <div>
                              <p className="font-medium">
                                {booking.fahrzeug?.marke} {booking.fahrzeug?.modell}
                              </p>
                              <p className="text-gray-500 text-xs">
                                {booking.fahrzeug?.kennzeichen}
                              </p>
                            </div>
                          </td>
                          <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                            {formatDateTime(booking.abholdatum)}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                            {formatDateTime(booking.rueckgabedatum)}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                            {booking.abholfiliale?.name}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-sm font-medium">
                            {booking.gesamtpreis?.toFixed(2)} {booking.waehrung}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-sm">
                            <div
                              className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium ${getStatusClasses(statusInfo.color)}`}
                            >
                              <span
                                className={`h-2 w-2 rounded-full ${getStatusDotClasses(statusInfo.color)}`}
                              />
                              {statusInfo.label}
                            </div>
                          </td>
                          <td className="h-[72px] px-4 py-2 text-sm">
                            <div className="flex justify-end gap-2">
                              <button
                                onClick={() => navigate(`/bookings/${booking.buchungsnummer}`)}
                                className="p-2 rounded-lg hover:bg-gray-100"
                                title="Details anzeigen"
                              >
                                <span className="material-symbols-outlined text-lg">
                                  visibility
                                </span>
                              </button>
                              {booking.status === 'REQUESTED' && (
                                <button
                                  onClick={() => handleConfirmBooking(booking.buchungsnummer)}
                                  disabled={actionLoading === booking.buchungsnummer}
                                  className="p-2 rounded-lg hover:bg-green-100 text-green-600 disabled:opacity-50"
                                  title="Buchung bestätigen"
                                >
                                  <span className="material-symbols-outlined text-lg">
                                    {actionLoading === booking.buchungsnummer
                                      ? 'hourglass_empty'
                                      : 'check_circle'}
                                  </span>
                                </button>
                              )}
                              {['REQUESTED', 'CONFIRMED'].includes(booking.status) && (
                                <button
                                  onClick={() => handleCancelBooking(booking.buchungsnummer)}
                                  disabled={actionLoading === booking.buchungsnummer}
                                  className="p-2 rounded-lg hover:bg-red-100 text-red-600 disabled:opacity-50"
                                  title="Buchung stornieren"
                                >
                                  <span className="material-symbols-outlined text-lg">cancel</span>
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>

          <div className="flex items-center justify-between mt-4">
            <p className="text-sm text-gray-600">
              Zeige {filteredBookings.length} von {bookings.length} Buchungen
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default BookingManagementPage;
