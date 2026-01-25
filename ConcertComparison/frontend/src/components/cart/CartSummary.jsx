import React from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import { formatPrice } from '../../utils/priceFormatter';

/**
 * CartSummary Component
 * Displays order summary with pricing and checkout button
 * 
 * @param {number} subtotal - Subtotal amount
 * @param {number} serviceFees - Service fees
 * @param {number} total - Total amount
 * @param {number} itemCount - Number of items in cart
 * @param {Function} onCheckout - Callback when checkout button is clicked
 * @param {boolean} isLoading - Loading state during checkout
 */
const CartSummary = ({
  subtotal,
  serviceFees,
  total,
  itemCount,
  onCheckout,
  isLoading = false,
}) => {
  const navigate = useNavigate();

  return (
    <div className="lg:w-[40%] relative">
      <div className="sticky top-24 space-y-4">
        {/* Summary Card */}
        <div className="bg-white dark:bg-[#1a2634] rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-6">
            Bestellübersicht
          </h2>

          {/* Line Items */}
          <div className="space-y-3 pb-6 border-b border-gray-100 dark:border-gray-700">
            <div className="flex justify-between text-slate-600 dark:text-slate-400">
              <span>Zwischensumme ({itemCount} {itemCount === 1 ? 'Ticket' : 'Tickets'})</span>
              <span className="font-medium">{formatPrice(subtotal)}</span>
            </div>
            <div className="flex justify-between text-slate-600 dark:text-slate-400">
              <span className="flex items-center gap-1 cursor-help group/tooltip relative">
                Servicegebühren
                <span className="material-symbols-outlined text-base text-gray-400">info</span>
                <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-48 p-2 bg-gray-900 text-white text-xs rounded opacity-0 invisible group-hover/tooltip:opacity-100 group-hover/tooltip:visible transition-all">
                  Beinhaltet Bearbeitungs- und Buchungsgebühren.
                </div>
              </span>
              <span className="font-medium">{formatPrice(serviceFees)}</span>
            </div>
            <div className="flex justify-between text-slate-600 dark:text-slate-400">
              <span>MwSt. (19%)</span>
              <span className="font-medium">Enthalten</span>
            </div>
          </div>

          {/* Total */}
          <div className="py-6 flex justify-between items-center">
            <span className="text-lg font-bold text-slate-900 dark:text-white">Gesamt</span>
            <span className="text-3xl font-black text-ticket-orange">{formatPrice(total)}</span>
          </div>

          {/* Buttons */}
          <div className="space-y-3">
            <button
              onClick={onCheckout}
              disabled={isLoading}
              className="w-full bg-primary hover:bg-blue-700 text-white font-bold py-4 px-6 rounded-lg shadow-lg hover:shadow-xl hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2 group/btn disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:translate-y-0"
            >
              {isLoading ? (
                <>
                  <div className="inline-block animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent"></div>
                  <span>Wird verarbeitet...</span>
                </>
              ) : (
                <>
                  <span>Zur Zahlung</span>
                  <span className="material-symbols-outlined group-hover/btn:translate-x-1 transition-transform">
                    arrow_forward
                  </span>
                </>
              )}
            </button>
            <button
              onClick={() => navigate('/concerts')}
              className="w-full bg-white dark:bg-transparent border-2 border-gray-200 dark:border-gray-600 text-slate-700 dark:text-slate-300 font-bold py-3 px-6 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
            >
              Weiter einkaufen
            </button>
          </div>

          {/* Security Notice */}
          <div className="mt-6 pt-6 border-t border-gray-100 dark:border-gray-700">
            <div className="flex items-center justify-center gap-2 text-xs text-green-600 dark:text-green-400 font-medium mb-3">
              <span className="material-symbols-outlined text-sm">lock</span>
              SSL-verschlüsselte Transaktion
            </div>
            <div className="flex justify-center gap-6 opacity-60 grayscale">
              <span className="text-xl font-bold italic text-blue-800" title="Visa">
                VISA
              </span>
              <div className="flex items-center -space-x-1" title="Mastercard">
                <div className="w-5 h-5 bg-red-600 rounded-full opacity-90"></div>
                <div className="w-5 h-5 bg-yellow-500 rounded-full opacity-90"></div>
              </div>
              <span className="text-xl font-bold text-blue-600 italic" title="PayPal">
                PayPal
              </span>
              <span className="text-sm font-bold text-slate-600 flex items-center" title="Apple Pay">
                <span className="material-symbols-outlined text-lg">phone_iphone</span> Pay
              </span>
            </div>
          </div>
        </div>

        {/* Additional Info / Ticket Insurance (Optional) */}
        <div className="bg-blue-50 dark:bg-blue-900/10 rounded-lg p-4 border border-blue-100 dark:border-blue-900/30 flex gap-3 items-start">
          <span className="material-symbols-outlined text-primary mt-0.5">verified_user</span>
          <div>
            <h4 className="text-sm font-bold text-slate-900 dark:text-white">
              Ticket-Versicherung
            </h4>
            <p className="text-xs text-slate-600 dark:text-slate-400 mt-1 leading-relaxed">
              Schützen Sie Ihren Kauf für nur €4.99 pro Ticket. Volle Rückerstattung bei Krankheit oder Verkehrsproblemen.
            </p>
            <button className="text-xs font-bold text-primary mt-2 hover:underline">
              Versicherung hinzufügen
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

CartSummary.propTypes = {
  subtotal: PropTypes.number.isRequired,
  serviceFees: PropTypes.number.isRequired,
  total: PropTypes.number.isRequired,
  itemCount: PropTypes.number.isRequired,
  onCheckout: PropTypes.func.isRequired,
  isLoading: PropTypes.bool,
};

export default CartSummary;
