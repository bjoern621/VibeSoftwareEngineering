import React from 'react';
import CreditCardForm, { validateCreditCard } from './CreditCardForm';
import PayPalForm, { validatePayPal } from './PayPalForm';
import BitcoinForm, { validateBitcoin } from './BitcoinForm';

/**
 * Payment method configuration
 */
const PAYMENT_METHODS = [
  {
    id: 'creditcard',
    label: 'Credit Card',
    icon: 'credit_card',
    backendValue: 'CREDIT_CARD',
  },
  {
    id: 'paypal',
    label: 'PayPal',
    icon: 'account_balance_wallet',
    backendValue: 'PAYPAL',
  },
  {
    id: 'bitcoin',
    label: 'Bitcoin',
    icon: 'currency_bitcoin',
    backendValue: 'BITCOIN',
  },
];

/**
 * PaymentMethodSelector - Allows user to select and enter payment details
 * Renders the appropriate payment form based on selection
 * 
 * @param {string} value - Currently selected payment method id
 * @param {function} onChange - Callback when method changes
 * @param {object} paymentDetails - Current payment form data
 * @param {function} onPaymentDetailsChange - Callback when payment details change
 * @param {object} errors - Validation errors for payment form
 * @param {boolean} disabled - Whether inputs are disabled
 * @param {number} totalAmount - Total amount for Bitcoin conversion display
 */
const PaymentMethodSelector = ({ 
  value, 
  onChange, 
  paymentDetails = {}, 
  onPaymentDetailsChange,
  errors = {},
  disabled = false,
  totalAmount = 0,
}) => {
  // Handle payment details change with stable reference
  const handleDetailsChange = (details) => {
    onPaymentDetailsChange?.(details);
  };

  return (
    <div className="space-y-6">
      {/* Payment Method Selection */}
      <div>
        <h2 className="text-lg font-semibold text-slate-900 dark:text-white mb-4">
          Zahlungsmethode
        </h2>

        <div className="grid grid-cols-3 gap-3">
          {PAYMENT_METHODS.map((method) => (
            <button
              key={method.id}
              type="button"
              disabled={disabled}
              onClick={() => onChange(method.id)}
              className={`flex flex-col items-center justify-center p-4 rounded-xl border-2 transition-all ${
                value === method.id
                  ? 'border-primary bg-primary/5 text-primary'
                  : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600 text-slate-600 dark:text-slate-400'
              } ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
            >
              <span className="material-symbols-outlined text-2xl mb-1">
                {method.icon}
              </span>
              <span className="text-sm font-medium">{method.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Payment Form based on selection */}
      {value && (
        <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
          {value === 'creditcard' && (
            <CreditCardForm
              value={paymentDetails}
              onChange={handleDetailsChange}
              disabled={disabled}
              errors={errors}
            />
          )}
          {value === 'paypal' && (
            <PayPalForm
              value={paymentDetails}
              onChange={handleDetailsChange}
              disabled={disabled}
              errors={errors}
            />
          )}
          {value === 'bitcoin' && (
            <BitcoinForm
              value={paymentDetails}
              onChange={handleDetailsChange}
              disabled={disabled}
              errors={errors}
              amount={totalAmount}
            />
          )}
        </div>
      )}
    </div>
  );
};

/**
 * Validate payment details based on method
 * @param {string} method - Payment method id
 * @param {object} details - Payment form details
 * @returns {object} - { isValid: boolean, errors: object }
 */
export const validatePaymentDetails = (method, details) => {
  switch (method) {
    case 'creditcard':
      return validateCreditCard(details);
    case 'paypal':
      return validatePayPal(details);
    case 'bitcoin':
      return validateBitcoin(details);
    default:
      return { isValid: false, errors: { method: 'Bitte Zahlungsmethode wählen' } };
  }
};

/**
 * Get backend payment method value
 * @param {string} methodId - Frontend method id
 * @returns {string} - Backend enum value
 */
export const getBackendPaymentMethod = (methodId) => {
  const method = PAYMENT_METHODS.find(m => m.id === methodId);
  return method?.backendValue || 'CREDIT_CARD';
};

export default PaymentMethodSelector;
