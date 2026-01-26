import React, { useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * CheckoutSuccessPage - Bestätigungsseite nach erfolgreichem Kauf
 * Zeigt Order-Details, Ticket-Download-Links und Bestätigungsnachricht
 */
const CheckoutSuccessPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();

  // Bestelldaten aus Navigation State
  const { orders = [], totalAmount = 0, billingDetails = {}, paymentMethod = '' } = location.state || {};

  // Umleiten wenn keine Bestelldaten vorhanden
  useEffect(() => {
    if (!orders || orders.length === 0) {
      navigate('/concerts');
    }
  }, [orders, navigate]);

  // Zahlungsmethode formatieren
  const formatPaymentMethod = (method) => {
    const methods = {
      creditcard: 'Kreditkarte',
      paypal: 'PayPal',
      crypto: 'Kryptowährung',
    };
    return methods[method] || method;
  };

  // Datum formatieren
  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('de-DE', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Order ID kürzen für Anzeige
  const formatOrderId = (id) => {
    if (!id) return 'N/A';
    if (typeof id === 'number') return `#${id}`;
    return `#${id.toString().substring(0, 8).toUpperCase()}`;
  };

  if (!orders || orders.length === 0) {
    return null; // useEffect leitet um
  }

  return (
    <div className="min-h-screen bg-background-light dark:bg-background-dark flex flex-col">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-white dark:bg-[#1a2634] border-b border-gray-200 dark:border-gray-700 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <Link to="/concerts" className="flex items-center gap-2 text-primary">
              <span className="material-symbols-outlined text-3xl">confirmation_number</span>
              <h2 className="text-slate-900 dark:text-white text-xl font-bold tracking-tight">
                TicketMaster<span className="text-primary">.Clone</span>
              </h2>
            </Link>

            {/* User Menu */}
            {isAuthenticated && (
              <div className="flex items-center gap-3">
                <Link to="/profile" className="flex items-center gap-2 text-slate-600 dark:text-slate-300 hover:text-primary">
                  <span className="material-symbols-outlined">account_circle</span>
                  <span className="hidden md:inline">{user?.firstName}</span>
                </Link>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-grow w-full max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Success Icon & Message */}
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-green-100 dark:bg-green-900/30 rounded-full mb-6">
            <span className="material-symbols-outlined text-green-600 dark:text-green-400 text-5xl">
              check_circle
            </span>
          </div>
          <h1 className="text-3xl font-black tracking-tight text-slate-900 dark:text-white mb-2">
            Kauf erfolgreich!
          </h1>
          <p className="text-lg text-slate-500 dark:text-slate-400">
            Vielen Dank für Ihren Einkauf. Ihre Tickets wurden erfolgreich gebucht.
          </p>
        </div>

        {/* Order Summary Card */}
        <div className="bg-white dark:bg-[#1a2634] rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 overflow-hidden mb-8">
          {/* Header */}
          <div className="bg-primary/10 dark:bg-primary/20 px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white">
                Bestellbestätigung
              </h2>
              <span className="text-sm text-slate-500 dark:text-slate-400">
                {new Date().toLocaleDateString('de-DE')}
              </span>
            </div>
          </div>

          {/* Order Details */}
          <div className="p-6 space-y-6">
            {/* Orders List */}
            {orders.map((order, index) => (
              <div
                key={order?.id || index}
                className="border border-gray-200 dark:border-gray-700 rounded-lg p-4"
              >
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-white text-lg">
                      {order?.eventName || order?.concert?.name || 'Konzertticket'}
                    </h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400">
                      Order-ID: <span className="font-mono">{formatOrderId(order?.id)}</span>
                    </p>
                  </div>
                  <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-semibold bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400">
                    <span className="material-symbols-outlined text-sm">check</span>
                    {order?.status || 'Bestätigt'}
                  </span>
                </div>

                {/* Ticket Details */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                  {order?.eventDate && (
                    <div>
                      <p className="text-slate-500 dark:text-slate-400">Datum</p>
                      <p className="font-semibold text-slate-900 dark:text-white">
                        {formatDate(order.eventDate)}
                      </p>
                    </div>
                  )}
                  {order?.venue && (
                    <div>
                      <p className="text-slate-500 dark:text-slate-400">Veranstaltungsort</p>
                      <p className="font-semibold text-slate-900 dark:text-white">{order.venue}</p>
                    </div>
                  )}
                  {(order?.seatCategory || order?.seat?.category) && (
                    <div>
                      <p className="text-slate-500 dark:text-slate-400">Kategorie</p>
                      <p className="font-semibold text-slate-900 dark:text-white">
                        {order.seatCategory || order.seat?.category}
                      </p>
                    </div>
                  )}
                  {(order?.seatInfo || order?.seat) && (
                    <div>
                      <p className="text-slate-500 dark:text-slate-400">Platz</p>
                      <p className="font-semibold text-slate-900 dark:text-white">
                        {order.seatInfo || `Reihe ${order.seat?.row}, Nr. ${order.seat?.number}`}
                      </p>
                    </div>
                  )}
                </div>

                {/* Ticket Download Button */}
                <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                  <Link
                    to={`/profile?order=${order?.id}`}
                    className="inline-flex items-center gap-2 px-4 py-2 bg-primary/10 dark:bg-primary/20 text-primary rounded-lg hover:bg-primary/20 dark:hover:bg-primary/30 transition-colors"
                  >
                    <span className="material-symbols-outlined">download</span>
                    Ticket anzeigen / herunterladen
                  </Link>
                </div>
              </div>
            ))}

            {/* Payment Summary */}
            <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
              <h3 className="font-bold text-slate-900 dark:text-white mb-4">Zahlungsübersicht</h3>
              <div className="space-y-2">
                {billingDetails?.firstName && (
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500 dark:text-slate-400">Rechnungsadresse</span>
                    <span className="text-slate-900 dark:text-white">
                      {billingDetails.firstName} {billingDetails.lastName}
                    </span>
                  </div>
                )}
                {billingDetails?.email && (
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500 dark:text-slate-400">E-Mail</span>
                    <span className="text-slate-900 dark:text-white">{billingDetails.email}</span>
                  </div>
                )}
                {paymentMethod && (
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500 dark:text-slate-400">Zahlungsmethode</span>
                    <span className="text-slate-900 dark:text-white">{formatPaymentMethod(paymentMethod)}</span>
                  </div>
                )}
                <div className="flex justify-between text-sm">
                  <span className="text-slate-500 dark:text-slate-400">Anzahl Tickets</span>
                  <span className="text-slate-900 dark:text-white">{orders.length}</span>
                </div>
                <div className="flex justify-between text-lg font-bold pt-2 border-t border-gray-200 dark:border-gray-700 mt-2">
                  <span className="text-slate-900 dark:text-white">Gesamtbetrag</span>
                  <span className="text-primary">€{totalAmount?.toFixed?.(2) || '0.00'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Email Notification */}
        <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4 flex items-start gap-3 mb-8">
          <span className="material-symbols-outlined text-blue-600 dark:text-blue-400">mail</span>
          <div>
            <h3 className="font-semibold text-blue-800 dark:text-blue-300">Bestätigung per E-Mail</h3>
            <p className="text-sm text-blue-700 dark:text-blue-400 mt-1">
              Eine Bestellbestätigung wurde an <strong>{billingDetails?.email || user?.email || 'Ihre E-Mail-Adresse'}</strong> gesendet.
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            to="/profile"
            className="inline-flex items-center justify-center gap-2 px-6 py-3 bg-primary text-white rounded-xl font-semibold hover:bg-primary/90 transition-colors"
          >
            <span className="material-symbols-outlined">receipt_long</span>
            Meine Bestellungen
          </Link>
          <Link
            to="/concerts"
            className="inline-flex items-center justify-center gap-2 px-6 py-3 bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-white rounded-xl font-semibold hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors"
          >
            <span className="material-symbols-outlined">explore</span>
            Weitere Konzerte entdecken
          </Link>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white dark:bg-[#1a2634] border-t border-gray-200 dark:border-gray-700 py-6 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            © 2024 TicketMaster Clone. Alle Rechte vorbehalten.
          </p>
          <div className="flex gap-6 text-sm text-slate-500 dark:text-slate-400">
            <Link to="/privacy" className="hover:text-primary">Datenschutz</Link>
            <Link to="/terms" className="hover:text-primary">AGB</Link>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default CheckoutSuccessPage;
