import { useState, useEffect, useCallback, useRef } from 'react';
import { createSeatStream, SSE_CONNECTION_STATUS } from '../services/seatService';

/**
 * Custom hook for managing SSE connection for seat updates
 * Provides real-time seat status updates with automatic reconnection
 * 
 * @param {string} concertId - Concert ID to subscribe to
 * @param {Object} options - Configuration options
 * @param {boolean} options.enabled - Whether SSE should be active (default: true)
 * @param {Function} options.onSeatUpdate - Callback when seat status changes
 * @returns {Object} - Connection status and control functions
 */
export const useSeatSSE = (concertId, options = {}) => {
  const { enabled = true, onSeatUpdate } = options;
  
  const [connectionStatus, setConnectionStatus] = useState(SSE_CONNECTION_STATUS.CLOSED);
  const [lastEvent, setLastEvent] = useState(null);
  const [error, setError] = useState(null);
  
  // Ref zum Speichern der Cleanup-Funktion
  const cleanupRef = useRef(null);
  // Ref für stabile Callback-Referenz
  const onSeatUpdateRef = useRef(onSeatUpdate);
  
  // Aktualisiere die Ref wenn sich der Callback ändert
  useEffect(() => {
    onSeatUpdateRef.current = onSeatUpdate;
  }, [onSeatUpdate]);
  
  /**
   * Handle incoming seat update events
   */
  const handleSeatUpdate = useCallback((data) => {
    setLastEvent({
      ...data,
      receivedAt: new Date().toISOString(),
    });
    setError(null);
    
    // Rufe den übergebenen Callback auf
    if (onSeatUpdateRef.current) {
      onSeatUpdateRef.current(data.seatId, data.status);
    }
  }, []);
  
  /**
   * Handle connection status changes
   */
  const handleConnectionChange = useCallback((status) => {
    setConnectionStatus(status);
    
    // Bei erfolgreicher Verbindung, Error zurücksetzen
    if (status === SSE_CONNECTION_STATUS.CONNECTED) {
      setError(null);
    }
  }, []);
  
  /**
   * Handle connection errors
   */
  const handleError = useCallback((err) => {
    setError(err.message || 'Verbindungsfehler');
  }, []);
  
  /**
   * Connect to SSE stream
   */
  const connect = useCallback(() => {
    if (!concertId || !enabled) return;
    
    // Vorherige Verbindung schließen
    if (cleanupRef.current) {
      cleanupRef.current();
    }
    
    // Neue Verbindung aufbauen
    cleanupRef.current = createSeatStream(concertId, {
      onSeatUpdate: handleSeatUpdate,
      onConnectionChange: handleConnectionChange,
      onError: handleError,
    });
  }, [concertId, enabled, handleSeatUpdate, handleConnectionChange, handleError]);
  
  /**
   * Disconnect from SSE stream
   */
  const disconnect = useCallback(() => {
    if (cleanupRef.current) {
      cleanupRef.current();
      cleanupRef.current = null;
    }
    setConnectionStatus(SSE_CONNECTION_STATUS.CLOSED);
  }, []);
  
  /**
   * Reconnect (manual reconnect trigger)
   */
  const reconnect = useCallback(() => {
    disconnect();
    // Kurze Verzögerung vor erneutem Verbinden
    setTimeout(connect, 100);
  }, [disconnect, connect]);
  
  // Verbindung aufbauen wenn concertId oder enabled sich ändert
  useEffect(() => {
    if (enabled && concertId) {
      connect();
    } else {
      disconnect();
    }
    
    // Cleanup bei Unmount oder Dependency-Änderung
    return () => {
      if (cleanupRef.current) {
        cleanupRef.current();
        cleanupRef.current = null;
      }
    };
  }, [concertId, enabled, connect, disconnect]);
  
  // Convenience Flags
  const isConnected = connectionStatus === SSE_CONNECTION_STATUS.CONNECTED;
  const isConnecting = connectionStatus === SSE_CONNECTION_STATUS.CONNECTING;
  const isReconnecting = connectionStatus === SSE_CONNECTION_STATUS.RECONNECTING;
  const hasError = connectionStatus === SSE_CONNECTION_STATUS.ERROR;
  
  return {
    // Status
    connectionStatus,
    isConnected,
    isConnecting,
    isReconnecting,
    hasError,
    error,
    lastEvent,
    
    // Controls
    connect,
    disconnect,
    reconnect,
  };
};

export default useSeatSSE;
