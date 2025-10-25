import React, { useState } from 'react';
import './QRCodeValidation.css';
import api from '../services/api';

/**
 * QR-Code-Validierung für Mensa-Mitarbeiter
 * Validiert Bestellungen bei der Essensausgabe
 */
function QRCodeValidation() {
  const [qrCode, setQrCode] = useState('');
  const [validationResult, setValidationResult] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [scanHistory, setScanHistory] = useState([]);

  const handleValidate = async (e) => {
    e.preventDefault();
    
    if (!qrCode.trim()) {
      setError('⚠️ Bitte QR-Code eingeben!');
      return;
    }

    setIsLoading(true);
    setError(null);
    setValidationResult(null);

    try {
      const result = await api.orders.validateQRCode(qrCode.trim());
      console.log('✅ QR-Code validiert:', result);
      
      setValidationResult(result);
      
      // Füge zur Scan-Historie hinzu
      const historyEntry = {
        qrCode: qrCode.trim(),
        timestamp: new Date().toLocaleString('de-DE'),
        status: result.alreadyCollected ? 'bereits-abgeholt' : 'erfolgreich',
        mealName: result.meal?.name || 'Unbekannt',
        orderId: result.orderId
      };
      setScanHistory(prev => [historyEntry, ...prev.slice(0, 9)]); // Nur letzte 10 behalten
      
      // Leere Input nach erfolgreicher Validierung
      setQrCode('');
      
    } catch (err) {
      console.error('❌ QR-Code Validierung fehlgeschlagen:', err);
      
      if (err.message.includes('404')) {
        setError('❌ QR-Code ungültig oder Bestellung nicht bezahlt!');
      } else {
        setError(`❌ Fehler: ${err.message}`);
      }
      
      // Füge Fehler zur Historie hinzu
      const historyEntry = {
        qrCode: qrCode.trim(),
        timestamp: new Date().toLocaleString('de-DE'),
        status: 'fehler',
        mealName: '-',
        orderId: '-'
      };
      setScanHistory(prev => [historyEntry, ...prev.slice(0, 9)]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClear = () => {
    setQrCode('');
    setValidationResult(null);
    setError(null);
  };

  const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '-';
    try {
      const date = new Date(dateTimeString);
      return date.toLocaleString('de-DE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateTimeString;
    }
  };

  return (
    <div className="qr-validation-container">
      <div className="qr-validation-header">
        <h2>📱 QR-Code Validierung</h2>
        <p>Für Essensausgabe an Mensa-Mitarbeiter</p>
      </div>

      <div className="qr-validation-content">
        {/* Scan-Bereich */}
        <div className="qr-scan-section">
          <div className="scan-icon">📸</div>
          <h3>QR-Code scannen oder eingeben</h3>
          
          <form onSubmit={handleValidate}>
            <div className="input-group">
              <input
                type="text"
                value={qrCode}
                onChange={(e) => setQrCode(e.target.value)}
                placeholder="z.B. ORDER-123"
                className="qr-input"
                autoFocus
                disabled={isLoading}
              />
              <div className="button-group">
                <button 
                  type="submit" 
                  className="validate-button"
                  disabled={isLoading || !qrCode.trim()}
                >
                  {isLoading ? '⏳ Prüfe...' : '✓ Validieren'}
                </button>
                {qrCode && (
                  <button 
                    type="button" 
                    className="clear-button"
                    onClick={handleClear}
                    disabled={isLoading}
                  >
                    ✕ Löschen
                  </button>
                )}
              </div>
            </div>
          </form>

          {/* Fehler-Anzeige */}
          {error && (
            <div className="validation-error">
              <span className="error-icon">⚠️</span>
              <span>{error}</span>
            </div>
          )}

          {/* Erfolg-Anzeige */}
          {validationResult && (
            <div className={`validation-result ${validationResult.alreadyCollected ? 'warning' : 'success'}`}>
              {validationResult.alreadyCollected ? (
                <>
                  <div className="result-header warning-header">
                    <span className="result-icon">⚠️</span>
                    <h3>Bereits abgeholt!</h3>
                  </div>
                  <div className="result-details">
                    <p className="warning-message">
                      ⚠️ Diese Bestellung wurde bereits am <strong>{formatDateTime(validationResult.collectedAt)}</strong> ausgegeben!
                    </p>
                  </div>
                </>
              ) : (
                <>
                  <div className="result-header success-header">
                    <span className="result-icon">✅</span>
                    <h3>Bestellung gültig!</h3>
                  </div>
                  <div className="result-details">
                    <p className="success-message">
                      ✅ Bestellung wurde erfolgreich validiert und als abgeholt markiert!
                    </p>
                  </div>
                </>
              )}
              
              {/* Bestelldetails */}
              <div className="order-details">
                <div className="detail-row">
                  <span className="detail-label">📋 Bestell-Nr:</span>
                  <span className="detail-value">#{validationResult.orderId}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">🍽️ Gericht:</span>
                  <span className="detail-value meal-name">{validationResult.meal?.name || 'Unbekannt'}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">📅 Bestellt am:</span>
                  <span className="detail-value">{formatDateTime(validationResult.orderDate)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">📆 Abholdatum:</span>
                  <span className="detail-value">{validationResult.pickupDate}</span>
                </div>
                {validationResult.collectedAt && (
                  <div className="detail-row">
                    <span className="detail-label">✓ Abgeholt am:</span>
                    <span className="detail-value">{formatDateTime(validationResult.collectedAt)}</span>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Scan-Historie */}
        {scanHistory.length > 0 && (
          <div className="scan-history-section">
            <h3>📜 Scan-Historie (Letzte 10)</h3>
            <div className="history-list">
              {scanHistory.map((entry, index) => (
                <div 
                  key={index} 
                  className={`history-item ${entry.status === 'fehler' ? 'history-error' : entry.status === 'bereits-abgeholt' ? 'history-warning' : 'history-success'}`}
                >
                  <div className="history-time">{entry.timestamp}</div>
                  <div className="history-details">
                    <span className="history-qr">{entry.qrCode}</span>
                    <span className="history-meal">{entry.mealName}</span>
                  </div>
                  <div className={`history-status status-${entry.status}`}>
                    {entry.status === 'fehler' ? '❌ Fehler' : 
                     entry.status === 'bereits-abgeholt' ? '⚠️ Bereits abgeholt' : 
                     '✅ OK'}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Anleitung */}
        <div className="validation-info">
          <h4>ℹ️ Hinweise für Mitarbeiter</h4>
          <ul>
            <li>✓ Scannen Sie den QR-Code vom Smartphone des Kunden</li>
            <li>✓ Alternativ: QR-Code manuell eingeben (Format: ORDER-123)</li>
            <li>✓ Grüne Meldung = Bestellung gültig, Essen ausgeben</li>
            <li>⚠️ Gelbe Warnung = Bereits ausgegeben, keine erneute Ausgabe!</li>
            <li>❌ Rote Fehlermeldung = Ungültiger Code oder nicht bezahlt</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default QRCodeValidation;
