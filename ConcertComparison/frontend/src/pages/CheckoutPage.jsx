import React, { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import BillingForm from '../components/checkout/BillingForm';
import PaymentMethodSelector from '../components/checkout/PaymentMethodSelector';
import OrderSummary from '../components/checkout/OrderSummary';
import CheckoutTimer from '../components/checkout/CheckoutTimer';
import { purchaseBulkTickets } from '../api/checkoutApi';

/**
 * CheckoutPage - Hauptseite für den Checkout-Prozess
 * Zeigt Rechnungsformular, Zahlungsoptionen und Bestellübersicht
 * Validiert Hold-Status vor dem Kauf
 */
const CheckoutPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, user } = useAuth();
  const {
    cartItems,
    itemCount,
    subtotal,
    serviceFees,
    total,
    oldestItem,
    isEmpty,
    clearCart,
    removeItem,
    getAllHoldIds,
  } = useCart();

  // Zustandsverwaltung
  const [billingDetails, setBillingDetails] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    address: '',
    city: '',
    state: '',
    zip: '',
  });
  const [paymentMethod, setPaymentMethod] = useState('creditcard');
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState(null);
  const [holdExpired, setHoldExpired] = useState(false);
  const [purchaseComplete, setPurchaseComplete] = useState(false);  // Flag um Redirect zu blockieren

  // Benutzer auf Login umleiten, wenn nicht authentifiziert
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/checkout' } });
    }
  }, [isAuthenticated, navigate]);

  // Auf Warenkorb umleiten, wenn leer (aber nicht nach erfolgreichem Kauf!)
  useEffect(() => {
    if (isEmpty && !purchaseComplete) {
      navigate('/cart');
    }
  }, [isEmpty, navigate, purchaseComplete]);

  // Benutzerdetails beim Laden vorausfüllen
  useEffect(() => {
    if (user) {
      setBillingDetails(prev => ({
        ...prev,
        firstName: user.firstName || prev.firstName,
        lastName: user.lastName || prev.lastName,
        email: user.email || prev.email,
      }));
    }
  }, [user]);

  /**
   * Prüft, ob noch aktive Holds vorhanden sind
   */
  const hasActiveHolds = useMemo(() => {
    if (!oldestItem) return false;
    const expiresAt = new Date(oldestItem.expiresAt);
    return expiresAt > new Date();
  }, [oldestItem]);

  /**
   * Timer für den ältesten Hold
   */
  const holdSecondsRemaining = useMemo(() => {
    if (!oldestItem) return 0;
    const expiresAt = new Date(oldestItem.expiresAt);
    const now = new Date();
    const diff = Math.floor((expiresAt - now) / 1000);
    return Math.max(0, diff);
  }, [oldestItem]);

  /**
   * Handler für Hold-Ablauf
   */
  const handleHoldExpired = () => {
    setHoldExpired(true);
    setError('Ihre Reservierung ist abgelaufen. Bitte kehren Sie zum Warenkorb zurück und reservieren Sie erneut.');
  };

  /**
   * Bestellübersicht für die OrderSummary-Komponente
   */
  const orderSummaryData = useMemo(() => {
    if (isEmpty || !cartItems.length) return null;

    const firstItem = cartItems[0];
    return {
      eventName: firstItem.concert?.name || 'Unbekanntes Event',
      category: firstItem.seat?.category || 'Standard',
      date: firstItem.concert?.date
        ? new Date(firstItem.concert.date).toLocaleDateString('de-DE', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
          })
        : '',
      image: firstItem.concert?.imageUrl,
      ticketCount: itemCount,
      ticketPrice: subtotal.toFixed(2),
      serviceFee: serviceFees.toFixed(2),
      tax: (total * 0.07).toFixed(2), // 7% MwSt
      total: total.toFixed(2),
    };
  }, [cartItems, itemCount, subtotal, serviceFees, total, isEmpty]);

  /**
   * Checkout-Handler
   * Führt den Kaufprozess durch
   */
  const handleCheckout = async () => {
    // Validierung: Hold noch aktiv?
    if (holdExpired || !hasActiveHolds) {
      setError('Ihre Reservierung ist abgelaufen. Bitte kehren Sie zum Warenkorb zurück.');
      return;
    }

    // Validierung: Rechnungsdetails ausgefüllt?
    if (!billingDetails.firstName || !billingDetails.lastName || !billingDetails.email) {
      setError('Bitte füllen Sie alle erforderlichen Felder aus.');
      return;
    }
    
    // UserId für Backend (entweder aus User-Objekt oder aus Formular)
    const userId = user?.email || billingDetails.email;
    if (!userId) {
      setError('Benutzer nicht erkannt. Bitte melden Sie sich erneut an.');
      return;
    }

    setIsProcessing(true);
    setError(null);

    try {
      const holdIds = getAllHoldIds();
      // Map frontend payment method to backend enum
      const backendPaymentMethod = paymentMethod === 'creditcard' ? 'CREDIT_CARD' 
        : paymentMethod === 'paypal' ? 'PAYPAL' 
        : paymentMethod === 'sofort' ? 'BANK_TRANSFER'
        : 'CREDIT_CARD';
      
      const results = await purchaseBulkTickets(holdIds, userId, backendPaymentMethod);

      // Ergebnisse auswerten
      const successfulOrders = results.filter(r => r.success);
      const failedOrders = results.filter(r => !r.success);

      if (successfulOrders.length > 0) {
        // Erfolgreich gekaufte Items aus dem Warenkorb entfernen
        successfulOrders.forEach(result => {
          removeItem(result.holdId);
        });

        if (failedOrders.length === 0) {
          // Alle Käufe erfolgreich - Flag setzen BEVOR clearCart!
          setPurchaseComplete(true);
          clearCart();
          navigate('/checkout/success', {
            state: {
              orders: successfulOrders.map(r => r.data),
              totalAmount: total,
              billingDetails,
              paymentMethod,
            },
          });
        } else {
          // Teilweiser Erfolg
          setError(
            `${successfulOrders.length} Ticket(s) erfolgreich gekauft. ${failedOrders.length} fehlgeschlagen. Die fehlgeschlagenen Tickets befinden sich noch in Ihrem Warenkorb.`
          );
        }
      } else {
        // Alle fehlgeschlagen
        const errorMessage = failedOrders[0]?.error || 'Unbekannter Fehler';
        setError(`Kauf fehlgeschlagen: ${errorMessage}`);
      }
    } catch (err) {
      console.error('Checkout error:', err);
      setError(err.message || 'Ein unerwarteter Fehler ist aufgetreten.');
    } finally {
      setIsProcessing(false);
    }
  };

  // Checkout-Button deaktiviert?
  const isCheckoutDisabled = holdExpired || !hasActiveHolds || isProcessing || isEmpty;

  if (!isAuthenticated || isEmpty) {
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

            {/* Breadcrumb */}
            <nav className="hidden md:flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
              <Link to="/cart" className="hover:text-primary">Warenkorb</Link>
              <span className="material-symbols-outlined text-xs">chevron_right</span>
              <span className="text-primary font-semibold">Checkout</span>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-grow w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Title */}
        <div className="mb-8">
          <h1 className="text-3xl font-black tracking-tight text-slate-900 dark:text-white">
            Checkout
          </h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">
            Schließen Sie Ihren Kauf ab
          </p>
        </div>

        {/* Timer Warning */}
        <div className="mb-6">
          <CheckoutTimer
            active={!holdExpired && hasActiveHolds}
            onExpire={handleHoldExpired}
            initialSeconds={holdSecondsRemaining}
          />
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 flex items-start gap-3">
            <span className="material-symbols-outlined text-red-600 dark:text-red-400">error</span>
            <div className="flex-1">
              <h3 className="font-semibold text-red-800 dark:text-red-300">Fehler</h3>
              <p className="text-sm text-red-700 dark:text-red-400 mt-1">{error}</p>
            </div>
            <button
              onClick={() => setError(null)}
              className="text-red-600 dark:text-red-400 hover:text-red-800"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
          </div>
        )}

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Left Column: Forms */}
          <div className="lg:w-[60%] space-y-6">
            {/* Billing Details */}
            <div className="bg-white dark:bg-[#1a2634] rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary">person</span>
                Rechnungsdetails
              </h2>
              <BillingForm
                value={billingDetails}
                onChange={setBillingDetails}
                disabled={holdExpired || isProcessing}
              />
            </div>

            {/* Payment Method */}
            <div className="bg-white dark:bg-[#1a2634] rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary">credit_card</span>
                Zahlungsmethode
              </h2>
              <PaymentMethodSelector
                value={paymentMethod}
                onChange={setPaymentMethod}
                disabled={holdExpired || isProcessing}
              />
            </div>

            {/* Cart Items Preview */}
            <div className="bg-white dark:bg-[#1a2634] rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary">confirmation_number</span>
                Ihre Tickets ({itemCount})
              </h2>
              <div className="space-y-3">
                {cartItems.map((item, index) => (
                  <div
                    key={item.holdId || index}
                    className="flex items-center gap-4 p-3 bg-gray-50 dark:bg-gray-800/50 rounded-lg"
                  >
                    {item.concert?.imageUrl && (
                      <img
                        src={item.concert.imageUrl}
                        alt={item.concert.name}
                        className="w-12 h-12 rounded object-cover"
                      />
                    )}
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-slate-900 dark:text-white truncate">
                        {item.concert?.name || 'Unbekanntes Event'}
                      </p>
                      <p className="text-sm text-slate-500 dark:text-slate-400">
                        {item.seat?.category} – Reihe {item.seat?.row}, Platz {item.seat?.number}
                      </p>
                    </div>
                    <p className="font-semibold text-slate-900 dark:text-white">
                      €{item.seat?.price?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Right Column: Summary */}
          <div className="lg:w-[40%]">
            <div className="sticky top-24 space-y-6">
              {/* Order Summary */}
              <div className="bg-white dark:bg-[#1a2634] rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
                <OrderSummary summary={orderSummaryData} />
              </div>

              {/* Checkout Button */}
              <button
                onClick={handleCheckout}
                disabled={isCheckoutDisabled}
                className={`w-full py-4 rounded-xl font-bold text-lg transition-all flex items-center justify-center gap-2 ${
                  isCheckoutDisabled
                    ? 'bg-gray-300 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed'
                    : 'bg-primary hover:bg-primary/90 text-white shadow-lg hover:shadow-xl'
                }`}
                data-testid="checkout-submit-btn"
              >
                {isProcessing ? (
                  <>
                    <svg
                      className="animate-spin h-5 w-5"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                    <span>Wird verarbeitet...</span>
                  </>
                ) : holdExpired ? (
                  <>
                    <span className="material-symbols-outlined">timer_off</span>
                    <span>Reservierung abgelaufen</span>
                  </>
                ) : (
                  <>
                    <span className="material-symbols-outlined">lock</span>
                    <span>Jetzt kaufen – €{total.toFixed(2)}</span>
                  </>
                )}
              </button>

              {/* Security Note */}
              <div className="flex items-center justify-center gap-2 text-sm text-slate-500 dark:text-slate-400">
                <span className="material-symbols-outlined text-green-500">verified_user</span>
                <span>Sichere SSL-Verschlüsselung</span>
              </div>

              {/* Back to Cart */}
              <Link
                to="/cart"
                className="block text-center text-primary hover:underline text-sm"
              >
                ← Zurück zum Warenkorb
              </Link>
            </div>
          </div>
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

export default CheckoutPage;
