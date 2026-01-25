import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';

/**
 * CartTimer Component
 * Displays countdown timer for the oldest hold in cart
 * 
 * @param {string} expiresAt - ISO timestamp when the hold expires
 */
const CartTimer = ({ expiresAt }) => {
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    if (!expiresAt) {
      setTimeLeft('--:--');
      return;
    }

    const updateTimer = () => {
      const now = new Date();
      const expiry = new Date(expiresAt);
      const diff = expiry - now;

      if (diff <= 0) {
        setTimeLeft('00:00');
        return;
      }

      const minutes = Math.floor(diff / 60000);
      const seconds = Math.floor((diff % 60000) / 1000);
      
      setTimeLeft(`${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`);
    };

    // Update immediately
    updateTimer();

    // Update every second
    const interval = setInterval(updateTimer, 1000);

    return () => clearInterval(interval);
  }, [expiresAt]);

  if (!expiresAt) {
    return null;
  }

  return (
    <div className="bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-900/50 rounded-lg px-4 py-3 flex items-center gap-3 shadow-sm">
      <span className="material-symbols-outlined text-ticket-orange animate-pulse">timer</span>
      <div className="flex flex-col">
        <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
          Tickets reserviert für
        </span>
        <span className="text-xl font-bold text-slate-900 dark:text-white tabular-nums">
          {timeLeft}
        </span>
      </div>
    </div>
  );
};

CartTimer.propTypes = {
  expiresAt: PropTypes.string,
};

export default CartTimer;
