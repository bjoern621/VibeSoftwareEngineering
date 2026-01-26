import React, { useState, useEffect, useMemo } from 'react';

/**
 * BitcoinForm - Mock Bitcoin/Crypto payment form
 * Displays a mock BTC address and QR code for payment
 * No real crypto integration - for demo/UI purposes only
 */
const BitcoinForm = ({ value = {}, onChange, disabled = false, errors = {}, amount = 0 }) => {
  const [hasConfirmed, setHasConfirmed] = useState(value.hasConfirmed || false);
  const [transactionId, setTransactionId] = useState(value.transactionId || '');
  const [isVerifying, setIsVerifying] = useState(false);

  // Generate a mock Bitcoin address (deterministic based on session)
  const btcAddress = useMemo(() => {
    const chars = '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';
    let address = 'bc1q';
    for (let i = 0; i < 38; i++) {
      address += chars[Math.floor(Math.random() * chars.length)];
    }
    return address;
  }, []);

  // Mock BTC conversion rate (1 BTC = ~42,000 EUR for demo)
  const btcAmount = useMemo(() => {
    const rate = 42000;
    return (amount / rate).toFixed(8);
  }, [amount]);

  // Notify parent of changes
  useEffect(() => {
    onChange?.({
      btcAddress,
      btcAmount,
      hasConfirmed,
      transactionId,
      paymentMethod: 'bitcoin',
    });
  }, [btcAddress, btcAmount, hasConfirmed, transactionId, onChange]);

  // Copy address to clipboard
  const handleCopyAddress = async () => {
    try {
      await navigator.clipboard.writeText(btcAddress);
      // Could show a toast notification here
    } catch (err) {
      console.error('Failed to copy address:', err);
    }
  };

  // Simulate payment verification
  const handleVerifyPayment = async () => {
    if (!transactionId || disabled) return;
    
    setIsVerifying(true);
    
    // Simulate blockchain verification delay
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    setHasConfirmed(true);
    setIsVerifying(false);
  };

  return (
    <div className="space-y-4">
      {/* Bitcoin Header */}
      <div className="flex items-center gap-3 p-4 bg-orange-50 dark:bg-orange-900/20 rounded-lg">
        <div className="w-12 h-12 bg-[#f7931a] rounded-lg flex items-center justify-center">
          <svg className="w-8 h-8 text-white" viewBox="0 0 24 24" fill="currentColor">
            <path d="M23.638 14.904c-1.602 6.43-8.113 10.34-14.542 8.736C2.67 22.05-1.244 15.525.362 9.105 1.962 2.67 8.475-1.243 14.9.358c6.43 1.605 10.342 8.115 8.738 14.546zm-6.35-4.613c.24-1.59-.974-2.45-2.64-3.03l.54-2.153-1.315-.33-.525 2.107c-.345-.087-.7-.168-1.053-.252l.528-2.12-1.315-.33-.54 2.153c-.285-.066-.565-.13-.84-.2l.001-.007-1.813-.453-.35 1.404s.974.225.954.238c.533.133.63.485.612.764l-.614 2.465c.037.01.084.024.136.047l-.137-.035-.86 3.45c-.066.163-.232.408-.607.315.013.02-.955-.238-.955-.238l-.652 1.507 1.71.426c.319.08.631.163.94.242l-.545 2.19 1.313.327.54-2.158c.36.1.707.19 1.048.276l-.538 2.156 1.315.33.546-2.183c2.245.425 3.932.253 4.642-1.778.573-1.633-.028-2.576-1.21-3.19.86-.198 1.508-.766 1.68-1.938zm-3.01 4.22c-.406 1.633-3.16.75-4.053.528l.724-2.897c.894.223 3.754.664 3.33 2.37zm.408-4.248c-.37 1.49-2.663.732-3.405.547l.656-2.63c.743.185 3.135.53 2.75 2.083z"/>
          </svg>
        </div>
        <div>
          <p className="font-medium text-slate-900 dark:text-white">Mit Bitcoin bezahlen</p>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Dezentral und anonym
          </p>
        </div>
      </div>

      {hasConfirmed ? (
        /* Payment Confirmed State */
        <div className="flex items-center gap-2 p-4 bg-green-50 dark:bg-green-900/20 rounded-lg border border-green-200 dark:border-green-800">
          <span className="material-symbols-outlined text-green-600 dark:text-green-400 text-2xl">check_circle</span>
          <div>
            <p className="font-medium text-green-700 dark:text-green-300">Zahlung bestätigt</p>
            <p className="text-sm text-green-600 dark:text-green-400">Transaction: {transactionId.slice(0, 16)}...</p>
          </div>
        </div>
      ) : (
        <>
          {/* Amount to Pay */}
          <div className="p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg text-center">
            <p className="text-sm text-slate-500 dark:text-slate-400 mb-1">Zu zahlen</p>
            <p className="text-2xl font-bold text-orange-500">{btcAmount} BTC</p>
            <p className="text-sm text-slate-500 dark:text-slate-400">≈ €{amount.toFixed(2)}</p>
          </div>

          {/* QR Code Placeholder */}
          <div className="flex justify-center p-4">
            <div className="w-40 h-40 bg-white border-2 border-gray-200 rounded-lg flex items-center justify-center relative overflow-hidden">
              {/* Mock QR Code Pattern */}
              <div className="grid grid-cols-8 gap-0.5 p-2">
                {Array(64).fill(0).map((_, i) => (
                  <div 
                    key={i} 
                    className={`w-3 h-3 ${Math.random() > 0.5 ? 'bg-black' : 'bg-white'}`}
                  />
                ))}
              </div>
              <div className="absolute inset-0 flex items-center justify-center bg-white/80">
                <svg className="w-10 h-10 text-orange-500" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M23.638 14.904c-1.602 6.43-8.113 10.34-14.542 8.736C2.67 22.05-1.244 15.525.362 9.105 1.962 2.67 8.475-1.243 14.9.358c6.43 1.605 10.342 8.115 8.738 14.546z"/>
                </svg>
              </div>
            </div>
          </div>

          {/* Wallet Address */}
          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
              Bitcoin-Adresse
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={btcAddress}
                readOnly
                className="flex-1 px-4 py-3 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-800 text-slate-900 dark:text-white text-sm font-mono"
              />
              <button
                type="button"
                onClick={handleCopyAddress}
                className="px-4 py-3 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors"
                title="Adresse kopieren"
              >
                <span className="material-symbols-outlined text-slate-600 dark:text-slate-300">content_copy</span>
              </button>
            </div>
          </div>

          {/* Transaction ID Input */}
          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
              Transaktions-ID
            </label>
            <input
              type="text"
              value={transactionId}
              onChange={(e) => setTransactionId(e.target.value)}
              placeholder="z.B. 3a1b2c3d4e5f..."
              disabled={disabled || isVerifying}
              className={`w-full px-4 py-3 rounded-lg border ${
                errors.transactionId 
                  ? 'border-red-500 focus:ring-red-500' 
                  : 'border-gray-300 dark:border-gray-600 focus:ring-primary'
              } bg-white dark:bg-gray-800 text-slate-900 dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all font-mono text-sm`}
            />
            {errors.transactionId && (
              <p className="mt-1 text-sm text-red-500">{errors.transactionId}</p>
            )}
          </div>

          {/* Verify Button */}
          <button
            type="button"
            onClick={handleVerifyPayment}
            disabled={disabled || isVerifying || !transactionId}
            className="w-full py-3 px-4 bg-orange-500 hover:bg-orange-600 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isVerifying ? (
              <>
                <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                Verifiziere Blockchain...
              </>
            ) : (
              <>
                <span className="material-symbols-outlined">verified</span>
                Zahlung bestätigen
              </>
            )}
          </button>

          {/* Info Notice */}
          <div className="flex items-start gap-2 p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg border border-yellow-200 dark:border-yellow-800">
            <span className="material-symbols-outlined text-yellow-600 dark:text-yellow-400 mt-0.5">info</span>
            <p className="text-sm text-yellow-700 dark:text-yellow-300">
              Senden Sie den exakten Betrag an die oben angegebene Adresse. 
              Transaktionen benötigen mindestens 1 Bestätigung.
            </p>
          </div>
        </>
      )}
    </div>
  );
};

/**
 * Validate Bitcoin form data
 * @param {Object} data - Bitcoin data { hasConfirmed, transactionId }
 * @returns {Object} - { isValid: boolean, errors: Object }
 */
export const validateBitcoin = (data) => {
  const errors = {};

  if (!data.hasConfirmed) {
    errors.confirmation = 'Bitte bestätigen Sie die Zahlung';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};

export default BitcoinForm;
