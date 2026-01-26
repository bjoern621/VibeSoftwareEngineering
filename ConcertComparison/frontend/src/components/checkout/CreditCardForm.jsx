import React, { useState, useEffect } from 'react';

/**
 * CreditCardForm - Mock credit card input form
 * Provides visual card number, expiry, CVV inputs with basic format validation
 * No real payment processing - for demo/UI purposes only
 */
const CreditCardForm = ({ value = {}, onChange, disabled = false, errors = {} }) => {
  const [cardNumber, setCardNumber] = useState(value.cardNumber || '');
  const [cardHolder, setCardHolder] = useState(value.cardHolder || '');
  const [expiryDate, setExpiryDate] = useState(value.expiryDate || '');
  const [cvv, setCvv] = useState(value.cvv || '');
  const [cardType, setCardType] = useState('');

  // Detect card type from number
  useEffect(() => {
    const num = cardNumber.replace(/\s/g, '');
    if (num.startsWith('4')) {
      setCardType('visa');
    } else if (/^5[1-5]/.test(num) || /^2[2-7]/.test(num)) {
      setCardType('mastercard');
    } else if (/^3[47]/.test(num)) {
      setCardType('amex');
    } else {
      setCardType('');
    }
  }, [cardNumber]);

  // Notify parent of changes
  useEffect(() => {
    onChange?.({
      cardNumber: cardNumber.replace(/\s/g, ''),
      cardHolder,
      expiryDate,
      cvv,
      cardType,
    });
  }, [cardNumber, cardHolder, expiryDate, cvv, cardType, onChange]);

  // Format card number with spaces every 4 digits
  const handleCardNumberChange = (e) => {
    let value = e.target.value.replace(/\D/g, '').slice(0, 16);
    value = value.replace(/(\d{4})(?=\d)/g, '$1 ');
    setCardNumber(value);
  };

  // Format expiry as MM/YY
  const handleExpiryChange = (e) => {
    let value = e.target.value.replace(/\D/g, '').slice(0, 4);
    if (value.length >= 2) {
      value = value.slice(0, 2) + '/' + value.slice(2);
    }
    setExpiryDate(value);
  };

  // CVV only digits, max 4 for Amex, 3 for others
  const handleCvvChange = (e) => {
    const maxLength = cardType === 'amex' ? 4 : 3;
    const value = e.target.value.replace(/\D/g, '').slice(0, maxLength);
    setCvv(value);
  };

  const getCardIcon = () => {
    switch (cardType) {
      case 'visa':
        return (
          <svg className="w-8 h-5" viewBox="0 0 50 16" fill="none">
            <rect width="50" height="16" rx="2" fill="#1A1F71"/>
            <path d="M19.5 12L21 4h2l-1.5 8h-2zm8.5-8l-2.5 5.5L25 4h-2l-3 8h2l.5-1.5h3l.3 1.5h2l-2-8h-1.8zm-1 5l1-3 .5 3h-1.5zM14 4l-1.5 5.5L12 4h-2l-1 8h1.5l.7-5.5L12 12h1.5l1.5-5.5.2 5.5h1.5l1-8H14zm22 0l-1.3 8h1.8l1.3-8H36z" fill="white"/>
          </svg>
        );
      case 'mastercard':
        return (
          <svg className="w-8 h-5" viewBox="0 0 50 16" fill="none">
            <rect width="50" height="16" rx="2" fill="#000"/>
            <circle cx="20" cy="8" r="5" fill="#EB001B"/>
            <circle cx="30" cy="8" r="5" fill="#F79E1B"/>
            <path d="M25 4.27a5 5 0 010 7.46 5 5 0 000-7.46z" fill="#FF5F00"/>
          </svg>
        );
      case 'amex':
        return (
          <svg className="w-8 h-5" viewBox="0 0 50 16" fill="none">
            <rect width="50" height="16" rx="2" fill="#006FCF"/>
            <path d="M10 10h3l.5-1h2l.5 1h4v-1l.4 1h2l.4-1v1h8V6h-8v.8L23 6h-2.2l-.4 1V6h-3l-.5 1-.5-1h-3v4h-3l1.5-4h2l1.5 4h-4z" fill="white"/>
          </svg>
        );
      default:
        return (
          <span className="material-symbols-outlined text-gray-400">credit_card</span>
        );
    }
  };

  return (
    <div className="space-y-4">
      {/* Card Number */}
      <div>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
          Kartennummer
        </label>
        <div className="relative">
          <input
            type="text"
            value={cardNumber}
            onChange={handleCardNumberChange}
            placeholder="1234 5678 9012 3456"
            disabled={disabled}
            className={`w-full px-4 py-3 pr-12 rounded-lg border ${
              errors.cardNumber 
                ? 'border-red-500 focus:ring-red-500' 
                : 'border-gray-300 dark:border-gray-600 focus:ring-primary'
            } bg-white dark:bg-gray-800 text-slate-900 dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all`}
          />
          <div className="absolute right-3 top-1/2 -translate-y-1/2">
            {getCardIcon()}
          </div>
        </div>
        {errors.cardNumber && (
          <p className="mt-1 text-sm text-red-500">{errors.cardNumber}</p>
        )}
      </div>

      {/* Cardholder Name */}
      <div>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
          Karteninhaber
        </label>
        <input
          type="text"
          value={cardHolder}
          onChange={(e) => setCardHolder(e.target.value.toUpperCase())}
          placeholder="MAX MUSTERMANN"
          disabled={disabled}
          className={`w-full px-4 py-3 rounded-lg border ${
            errors.cardHolder 
              ? 'border-red-500 focus:ring-red-500' 
              : 'border-gray-300 dark:border-gray-600 focus:ring-primary'
          } bg-white dark:bg-gray-800 text-slate-900 dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all uppercase`}
        />
        {errors.cardHolder && (
          <p className="mt-1 text-sm text-red-500">{errors.cardHolder}</p>
        )}
      </div>

      {/* Expiry & CVV Row */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            Gültig bis
          </label>
          <input
            type="text"
            value={expiryDate}
            onChange={handleExpiryChange}
            placeholder="MM/YY"
            disabled={disabled}
            className={`w-full px-4 py-3 rounded-lg border ${
              errors.expiryDate 
                ? 'border-red-500 focus:ring-red-500' 
                : 'border-gray-300 dark:border-gray-600 focus:ring-primary'
            } bg-white dark:bg-gray-800 text-slate-900 dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all`}
          />
          {errors.expiryDate && (
            <p className="mt-1 text-sm text-red-500">{errors.expiryDate}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
            CVV
            <span className="ml-1 text-gray-400 cursor-help" title="3-stelliger Sicherheitscode auf der Rückseite">
              ⓘ
            </span>
          </label>
          <input
            type="text"
            value={cvv}
            onChange={handleCvvChange}
            placeholder={cardType === 'amex' ? '1234' : '123'}
            disabled={disabled}
            className={`w-full px-4 py-3 rounded-lg border ${
              errors.cvv 
                ? 'border-red-500 focus:ring-red-500' 
                : 'border-gray-300 dark:border-gray-600 focus:ring-primary'
            } bg-white dark:bg-gray-800 text-slate-900 dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all`}
          />
          {errors.cvv && (
            <p className="mt-1 text-sm text-red-500">{errors.cvv}</p>
          )}
        </div>
      </div>

      {/* Security Notice */}
      <div className="flex items-center gap-2 p-3 bg-green-50 dark:bg-green-900/20 rounded-lg border border-green-200 dark:border-green-800">
        <span className="material-symbols-outlined text-green-600 dark:text-green-400">lock</span>
        <p className="text-sm text-green-700 dark:text-green-300">
          Ihre Zahlungsdaten werden sicher übertragen
        </p>
      </div>
    </div>
  );
};

/**
 * Validate credit card form data
 * @param {Object} data - Card data { cardNumber, cardHolder, expiryDate, cvv }
 * @returns {Object} - { isValid: boolean, errors: Object }
 */
export const validateCreditCard = (data) => {
  const errors = {};

  // Card number: 13-19 digits (without spaces)
  const cardNum = (data.cardNumber || '').replace(/\s/g, '');
  if (!cardNum) {
    errors.cardNumber = 'Kartennummer ist erforderlich';
  } else if (!/^\d{13,19}$/.test(cardNum)) {
    errors.cardNumber = 'Ungültige Kartennummer';
  }

  // Cardholder: required, at least 2 words
  if (!data.cardHolder?.trim()) {
    errors.cardHolder = 'Karteninhaber ist erforderlich';
  } else if (data.cardHolder.trim().split(/\s+/).length < 2) {
    errors.cardHolder = 'Bitte Vor- und Nachname eingeben';
  }

  // Expiry: MM/YY format, not expired
  if (!data.expiryDate) {
    errors.expiryDate = 'Gültigkeitsdatum ist erforderlich';
  } else if (!/^\d{2}\/\d{2}$/.test(data.expiryDate)) {
    errors.expiryDate = 'Format: MM/YY';
  } else {
    const [month, year] = data.expiryDate.split('/').map(Number);
    const now = new Date();
    const expiry = new Date(2000 + year, month, 0);
    if (month < 1 || month > 12) {
      errors.expiryDate = 'Ungültiger Monat';
    } else if (expiry < now) {
      errors.expiryDate = 'Karte ist abgelaufen';
    }
  }

  // CVV: 3-4 digits
  if (!data.cvv) {
    errors.cvv = 'CVV ist erforderlich';
  } else if (!/^\d{3,4}$/.test(data.cvv)) {
    errors.cvv = 'Ungültiger CVV';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};

export default CreditCardForm;
