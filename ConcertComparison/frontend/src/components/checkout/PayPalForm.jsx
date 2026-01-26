import React, { useState, useEffect } from 'react';

/**
 * PayPalForm - Mock PayPal payment form
 * Simulates PayPal login/authorization flow
 * No real PayPal integration - for demo/UI purposes only
 */
const PayPalForm = ({ value = {}, onChange, disabled = false, errors = {} }) => {
  const [email, setEmail] = useState(value.email || '');
  const [isConnected, setIsConnected] = useState(value.isConnected || false);
  const [isConnecting, setIsConnecting] = useState(false);

  // Notify parent of changes
  useEffect(() => {
    onChange?.({
      email,
      isConnected,
      paymentMethod: 'paypal',
    });
  }, [email, isConnected, onChange]);

  // Simulate PayPal connection
  const handleConnectPayPal = async () => {
    if (!email || disabled) return;
    
    setIsConnecting(true);
    
    // Simulate PayPal redirect/popup delay
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    setIsConnected(true);
    setIsConnecting(false);
  };

  // Disconnect PayPal account
  const handleDisconnect = () => {
    setIsConnected(false);
    setEmail('');
  };

  if (isConnected) {
    return (
      <div className="space-y-4">
        {/* Connected State */}
        <div className="flex items-center justify-between p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-[#003087] rounded-full flex items-center justify-center">
              <svg className="w-6 h-6 text-white" viewBox="0 0 24 24" fill="currentColor">
                <path d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944.901C5.026.382 5.474 0 5.998 0h7.46c2.57 0 4.578.543 5.69 1.81 1.01 1.15 1.304 2.42 1.012 4.287-.023.143-.047.288-.077.437-.983 5.05-4.349 6.797-8.647 6.797h-2.19c-.524 0-.968.382-1.05.9l-1.12 7.106zm14.146-14.42a3.35 3.35 0 0 0-.607-.541c-.013.076-.026.175-.041.254-.93 4.778-4.005 7.201-9.138 7.201h-2.19a.563.563 0 0 0-.556.479l-1.187 7.527h-.506l-.24 1.516a.56.56 0 0 0 .554.647h3.882c.46 0 .85-.334.922-.788.06-.26.76-4.852.816-5.09a.932.932 0 0 1 .923-.788h.58c3.76 0 6.705-1.528 7.565-5.946.36-1.847.174-3.388-.777-4.471z"/>
              </svg>
            </div>
            <div>
              <p className="font-medium text-slate-900 dark:text-white">PayPal verbunden</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">{email}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleDisconnect}
            disabled={disabled}
            className="text-sm text-red-600 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300 disabled:opacity-50"
          >
            Trennen
          </button>
        </div>

        {/* Ready to Pay Notice */}
        <div className="flex items-center gap-2 p-3 bg-green-50 dark:bg-green-900/20 rounded-lg border border-green-200 dark:border-green-800">
          <span className="material-symbols-outlined text-green-600 dark:text-green-400">check_circle</span>
          <p className="text-sm text-green-700 dark:text-green-300">
            Bereit zur Zahlung über PayPal
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* PayPal Logo & Info */}
      <div className="flex items-center gap-3 p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg">
        <div className="w-12 h-12 bg-[#003087] rounded-lg flex items-center justify-center">
          <svg className="w-8 h-8 text-white" viewBox="0 0 24 24" fill="currentColor">
            <path d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944.901C5.026.382 5.474 0 5.998 0h7.46c2.57 0 4.578.543 5.69 1.81 1.01 1.15 1.304 2.42 1.012 4.287-.023.143-.047.288-.077.437-.983 5.05-4.349 6.797-8.647 6.797h-2.19c-.524 0-.968.382-1.05.9l-1.12 7.106zm14.146-14.42a3.35 3.35 0 0 0-.607-.541c-.013.076-.026.175-.041.254-.93 4.778-4.005 7.201-9.138 7.201h-2.19a.563.563 0 0 0-.556.479l-1.187 7.527h-.506l-.24 1.516a.56.56 0 0 0 .554.647h3.882c.46 0 .85-.334.922-.788.06-.26.76-4.852.816-5.09a.932.932 0 0 1 .923-.788h.58c3.76 0 6.705-1.528 7.565-5.946.36-1.847.174-3.388-.777-4.471z"/>
          </svg>
        </div>
        <div>
          <p className="font-medium text-slate-900 dark:text-white">Mit PayPal bezahlen</p>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Schnell, sicher und bequem
          </p>
        </div>
      </div>

      {/* Email Input */}
      <div>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
          PayPal E-Mail
        </label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="ihre-email@beispiel.de"
          disabled={disabled || isConnecting}
          className={`w-full px-4 py-3 rounded-lg border ${
            errors.email 
              ? 'border-red-500 focus:ring-red-500' 
              : 'border-gray-300 dark:border-gray-600 focus:ring-primary'
          } bg-white dark:bg-gray-800 text-slate-900 dark:text-white placeholder:text-gray-400 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all`}
        />
        {errors.email && (
          <p className="mt-1 text-sm text-red-500">{errors.email}</p>
        )}
      </div>

      {/* Connect Button */}
      <button
        type="button"
        onClick={handleConnectPayPal}
        disabled={disabled || isConnecting || !email}
        className="w-full py-3 px-4 bg-[#0070ba] hover:bg-[#003087] text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        {isConnecting ? (
          <>
            <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            Verbinde mit PayPal...
          </>
        ) : (
          <>
            <svg className="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944.901C5.026.382 5.474 0 5.998 0h7.46c2.57 0 4.578.543 5.69 1.81 1.01 1.15 1.304 2.42 1.012 4.287-.023.143-.047.288-.077.437-.983 5.05-4.349 6.797-8.647 6.797h-2.19c-.524 0-.968.382-1.05.9l-1.12 7.106z"/>
            </svg>
            Mit PayPal verbinden
          </>
        )}
      </button>

      {/* Info Text */}
      <p className="text-xs text-slate-500 dark:text-slate-400 text-center">
        Sie werden zu PayPal weitergeleitet, um die Zahlung zu autorisieren.
      </p>
    </div>
  );
};

/**
 * Validate PayPal form data
 * @param {Object} data - PayPal data { email, isConnected }
 * @returns {Object} - { isValid: boolean, errors: Object }
 */
export const validatePayPal = (data) => {
  const errors = {};

  if (!data.email) {
    errors.email = 'PayPal E-Mail ist erforderlich';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
    errors.email = 'Ungültige E-Mail-Adresse';
  }

  if (!data.isConnected) {
    errors.connection = 'Bitte verbinden Sie Ihr PayPal-Konto';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};

export default PayPalForm;
