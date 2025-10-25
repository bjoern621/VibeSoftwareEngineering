import React, { useState, useEffect } from 'react';
import './ScheduleRecommendation.css';
import api from '../services/api';

/**
 * Einsatzplanung-Dashboard
 * Zeigt empfohlene Personalbesetzung basierend auf erwarteter Besucherzahl und geplanten Gerichten
 */
function ScheduleRecommendation() {
  const [selectedDate, setSelectedDate] = useState(getTodayString());
  const [recommendation, setRecommendation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Lädt Einsatzplanung-Empfehlung vom Backend
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchRecommendation = React.useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.staff.getScheduleRecommendation(selectedDate);
      setRecommendation(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [selectedDate]);

  /**
   * Lädt Einsatzplanung-Empfehlung beim Start und bei Datum-Änderung
   */
  useEffect(() => {
    fetchRecommendation();
  }, [fetchRecommendation]);

  /**
   * Formatiert Datum für Anzeige (z.B. "25.10.2025")
   * 
   * @param {string} dateStr - Datum im Format "yyyy-MM-dd"
   * @returns {string} Formatiertes Datum
   */
  const formatDisplayDate = (dateStr) => {
    const date = new Date(dateStr + 'T00:00:00');
    return date.toLocaleDateString('de-DE', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  /**
   * Prüft, ob genügend Personal verfügbar ist
   * 
   * @returns {boolean} true wenn genügend Personal vorhanden
   */
  const hasEnoughStaff = () => {
    if (!recommendation) return false;
    const availableTotal = 
      recommendation.availableStaff.cooks.length +
      recommendation.availableStaff.service.length;
    return availableTotal >= recommendation.recommendedStaff.total;
  };

  /**
   * Berechnet Personallücke
   * 
   * @returns {number} Anzahl fehlender Mitarbeiter (negativ = Überbesetzung)
   */
  const getStaffGap = () => {
    if (!recommendation) return 0;
    const availableTotal = 
      recommendation.availableStaff.cooks.length +
      recommendation.availableStaff.service.length;
    return recommendation.recommendedStaff.total - availableTotal;
  };

  /**
   * Formatiert Mitarbeiter-Namen
   * 
   * @param {object} staff - Mitarbeiter-Objekt
   * @returns {string} Vollständiger Name
   */
  const formatStaffName = (staff) => {
    return `${staff.firstName} ${staff.lastName}`;
  };

  return (
    <div className="schedule-recommendation">
      <div className="section-header">
        <h2>📅 Einsatzplanung</h2>
        <button className="primary" onClick={fetchRecommendation}>
          🔄 Aktualisieren
        </button>
      </div>

      {/* Datumswahl */}
      <div className="date-selector">
        <label htmlFor="date-input">Datum auswählen:</label>
        <input
          id="date-input"
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          className="date-input"
        />
        <span className="date-display">{recommendation && formatDisplayDate(recommendation.date)}</span>
      </div>

      {error && <div className="error">{error}</div>}

      {loading ? (
        <div className="loading">Lade Einsatzplanung...</div>
      ) : recommendation && (
        <>
          {/* Warn-Banner bei Personalmangel */}
          {!hasEnoughStaff() && (
            <div className="alert-banner warning">
              <span className="icon">⚠️</span>
              <div className="alert-content">
                <strong>Personalmangel erkannt!</strong>
                <p>Es fehlen {getStaffGap()} Mitarbeiter für die optimale Besetzung.</p>
              </div>
            </div>
          )}

          {/* Success-Banner bei ausreichender Besetzung */}
          {hasEnoughStaff() && (
            <div className="alert-banner success">
              <span className="icon">✅</span>
              <div className="alert-content">
                <strong>Ausreichend Personal verfügbar!</strong>
                <p>Die empfohlene Besetzung kann erreicht werden.</p>
              </div>
            </div>
          )}

          {/* Besucherzahl-Prognose */}
          <div className="forecast-section">
            <h3>👥 Besucherzahl-Prognose</h3>
            <div className="forecast-cards">
              <div className="forecast-card">
                <div className="forecast-icon">🍽️</div>
                <div className="forecast-content">
                  <h4>Erwartete Besucher</h4>
                  <p className="forecast-value">{recommendation.expectedVisitors}</p>
                  <span className="forecast-hint">Basierend auf Speiseplan</span>
                </div>
              </div>

              <div className="forecast-card">
                <div className="forecast-icon">📋</div>
                <div className="forecast-content">
                  <h4>Geplante Gerichte</h4>
                  <p className="forecast-value">{recommendation.plannedMeals}</p>
                  <span className="forecast-hint">Im Speiseplan</span>
                </div>
              </div>
            </div>
          </div>

          {/* Empfohlene Personalbesetzung */}
          <div className="recommendation-section">
            <h3>💡 Empfohlene Personalbesetzung</h3>
            <div className="recommendation-cards">
              <div className="recommendation-card">
                <div className="card-header">
                  <span className="card-icon">👨‍🍳</span>
                  <h4>Köche</h4>
                </div>
                <div className="card-body">
                  <div className="staff-count">
                    <span className="recommended">{recommendation.recommendedStaff.cooks}</span>
                    <span className="label">empfohlen</span>
                  </div>
                  <div className="staff-count">
                    <span className={`available ${recommendation.availableStaff.cooks.length >= recommendation.recommendedStaff.cooks ? 'sufficient' : 'insufficient'}`}>
                      {recommendation.availableStaff.cooks.length}
                    </span>
                    <span className="label">verfügbar</span>
                  </div>
                </div>
              </div>

              <div className="recommendation-card">
                <div className="card-header">
                  <span className="card-icon">🍽️</span>
                  <h4>Service</h4>
                </div>
                <div className="card-body">
                  <div className="staff-count">
                    <span className="recommended">{recommendation.recommendedStaff.service}</span>
                    <span className="label">empfohlen</span>
                  </div>
                  <div className="staff-count">
                    <span className={`available ${recommendation.availableStaff.service.length >= recommendation.recommendedStaff.service ? 'sufficient' : 'insufficient'}`}>
                      {recommendation.availableStaff.service.length}
                    </span>
                    <span className="label">verfügbar</span>
                  </div>
                </div>
              </div>

              <div className="recommendation-card highlight">
                <div className="card-header">
                  <span className="card-icon">👥</span>
                  <h4>Gesamt</h4>
                </div>
                <div className="card-body">
                  <div className="staff-count">
                    <span className="recommended">{recommendation.recommendedStaff.total}</span>
                    <span className="label">empfohlen</span>
                  </div>
                  <div className="staff-count">
                    <span className={`available ${hasEnoughStaff() ? 'sufficient' : 'insufficient'}`}>
                      {recommendation.availableStaff.cooks.length + recommendation.availableStaff.service.length}
                    </span>
                    <span className="label">verfügbar</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Verfügbare Mitarbeiter */}
          <div className="available-staff-section">
            <h3>✅ Verfügbare Mitarbeiter</h3>
            
            <div className="staff-lists">
              {/* Köche */}
              <div className="staff-list">
                <h4>👨‍🍳 Köche ({recommendation.availableStaff.cooks.length})</h4>
                {recommendation.availableStaff.cooks.length > 0 ? (
                  <ul>
                    {recommendation.availableStaff.cooks.map(cook => (
                      <li key={cook.id} className="staff-item">
                        <div className="staff-info">
                          <span className="staff-name">{formatStaffName(cook)}</span>
                          <span className="staff-badge available">Verfügbar</span>
                        </div>
                        <button className="btn-assign">Einplanen</button>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="no-staff">⚠️ Keine Köche verfügbar</p>
                )}
              </div>

              {/* Service */}
              <div className="staff-list">
                <h4>🍽️ Service ({recommendation.availableStaff.service.length})</h4>
                {recommendation.availableStaff.service.length > 0 ? (
                  <ul>
                    {recommendation.availableStaff.service.map(staff => (
                      <li key={staff.id} className="staff-item">
                        <div className="staff-info">
                          <span className="staff-name">{formatStaffName(staff)}</span>
                          <span className="staff-badge available">Verfügbar</span>
                        </div>
                        <button className="btn-assign">Einplanen</button>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="no-staff">⚠️ Kein Service-Personal verfügbar</p>
                )}
              </div>
            </div>
          </div>

          {/* Info-Box mit Berechnungslogik */}
          <div className="info-box">
            <h4>ℹ️ Berechnungsgrundlage</h4>
            <ul>
              <li><strong>Köche:</strong> 1 Koch pro 50 erwartete Besucher (Minimum: 2)</li>
              <li><strong>Service:</strong> 1 Servicekraft pro 100 erwartete Besucher (Minimum: 2)</li>
              <li><strong>Datenquelle:</strong> Speiseplan-Portionen für {recommendation.date}</li>
              <li><strong>STAFFMAN-Sync:</strong> Verfügbarkeiten werden automatisch synchronisiert</li>
            </ul>
          </div>
        </>
      )}
    </div>
  );
}

/**
 * Hilfsfunktion: Heutiges Datum als String
 * 
 * @returns {string} Datum im Format "yyyy-MM-dd"
 */
function getTodayString() {
  const today = new Date();
  return today.toISOString().split('T')[0];
}

export default ScheduleRecommendation;
