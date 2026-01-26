import React from 'react';
import { SSE_CONNECTION_STATUS } from '../../services/seatService';

/**
 * Connection Status Badge Component
 * Zeigt den aktuellen SSE-Verbindungsstatus an
 * 
 * @param {string} status - Aktueller Verbindungsstatus
 * @param {Function} onReconnect - Callback für manuelle Neuverbindung
 * @param {boolean} compact - Kompakte Darstellung (nur Icon)
 */
const ConnectionStatusBadge = ({ status, onReconnect, compact = false }) => {
  const getStatusConfig = () => {
    switch (status) {
      case SSE_CONNECTION_STATUS.CONNECTED:
        return {
          icon: 'wifi',
          label: 'Live',
          bgColor: 'bg-green-100 dark:bg-green-900/30',
          textColor: 'text-green-700 dark:text-green-400',
          dotColor: 'bg-green-500',
          animate: false,
        };
      case SSE_CONNECTION_STATUS.CONNECTING:
        return {
          icon: 'sync',
          label: 'Verbinde...',
          bgColor: 'bg-blue-100 dark:bg-blue-900/30',
          textColor: 'text-blue-700 dark:text-blue-400',
          dotColor: 'bg-blue-500',
          animate: true,
        };
      case SSE_CONNECTION_STATUS.RECONNECTING:
        return {
          icon: 'sync_problem',
          label: 'Verbindung wird wiederhergestellt',
          bgColor: 'bg-yellow-100 dark:bg-yellow-900/30',
          textColor: 'text-yellow-700 dark:text-yellow-400',
          dotColor: 'bg-yellow-500',
          animate: true,
        };
      case SSE_CONNECTION_STATUS.ERROR:
        return {
          icon: 'wifi_off',
          label: 'Offline',
          bgColor: 'bg-red-100 dark:bg-red-900/30',
          textColor: 'text-red-700 dark:text-red-400',
          dotColor: 'bg-red-500',
          animate: false,
          showReconnect: true,
        };
      case SSE_CONNECTION_STATUS.CLOSED:
      default:
        return {
          icon: 'wifi_off',
          label: 'Nicht verbunden',
          bgColor: 'bg-gray-100 dark:bg-gray-800',
          textColor: 'text-gray-600 dark:text-gray-400',
          dotColor: 'bg-gray-400',
          animate: false,
        };
    }
  };

  const config = getStatusConfig();

  if (compact) {
    return (
      <div
        className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-full ${config.bgColor} ${config.textColor}`}
        title={config.label}
      >
        <span
          className={`w-2 h-2 rounded-full ${config.dotColor} ${config.animate ? 'animate-pulse' : ''}`}
        />
        <span className="material-symbols-outlined text-sm">{config.icon}</span>
      </div>
    );
  }

  return (
    <div
      className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-lg ${config.bgColor} ${config.textColor} text-sm font-medium`}
    >
      <span
        className={`w-2 h-2 rounded-full ${config.dotColor} ${config.animate ? 'animate-pulse' : ''}`}
      />
      <span className="material-symbols-outlined text-base">{config.icon}</span>
      <span>{config.label}</span>
      {config.showReconnect && onReconnect && (
        <button
          onClick={onReconnect}
          className="ml-1 p-0.5 rounded hover:bg-red-200 dark:hover:bg-red-800/50 transition-colors"
          title="Erneut verbinden"
        >
          <span className="material-symbols-outlined text-base">refresh</span>
        </button>
      )}
    </div>
  );
};

export default ConnectionStatusBadge;
