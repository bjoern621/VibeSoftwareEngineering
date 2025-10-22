import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import api from '../services/api';
import { formatDate, addDays } from '../utils/dateUtils';
import { formatPriceSimple } from '../utils/priceUtils';

/**
 * Dashboard-Komponente für Finanzberichte und Übersichten
 * Zeigt Einnahmen, Ausgaben, beliebte Gerichte und Statistiken
 */
function Dashboard() {
  const [dashboardData, setDashboardData] = useState(null);
  const [dateRange, setDateRange] = useState({
    startDate: formatDate(addDays(new Date(), -7)),
    endDate: formatDate(new Date())
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Dashboard-Daten abrufen beim Start und bei Datumsänderung
   */
  useEffect(() => {
    fetchDashboardData();
    // eslint-disable-next-line
  }, [dateRange]);

  /**
   * Lädt Dashboard-Daten für den gewählten Zeitraum
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.dashboard.getData(dateRange.startDate, dateRange.endDate);
      setDashboardData(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Datumswerte im Formular ändern
   * 
   * @param {string} field - Feldname (startDate oder endDate)
   * @param {string} value - Neuer Datumswert
   */
  const handleDateChange = (field, value) => {
    setDateRange(prev => ({
      ...prev,
      [field]: value
    }));
  };

  if (loading) {
    return <div className="loading">Lade Dashboard-Daten...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="dashboard">
      <h2>📊 Finanz-Dashboard</h2>

      {/* Zeitraumauswahl */}
      <div className="date-range-selector">
        <div className="form-group">
          <label htmlFor="start-date">Von:</label>
          <input
            type="date"
            id="start-date"
            value={dateRange.startDate}
            onChange={(e) => handleDateChange('startDate', e.target.value)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="end-date">Bis:</label>
          <input
            type="date"
            id="end-date"
            value={dateRange.endDate}
            onChange={(e) => handleDateChange('endDate', e.target.value)}
          />
        </div>
        <button className="primary" onClick={fetchDashboardData}>
          Aktualisieren
        </button>
      </div>

      {/* Kennzahlen */}
      {dashboardData && (
        <>
          <div className="kpi-grid">
            <div className="kpi-card revenue">
              <div className="kpi-icon">💰</div>
              <div className="kpi-content">
                <h3>Gesamteinnahmen</h3>
                <p className="kpi-value">{formatPriceSimple(dashboardData.totalRevenue || 0)} €</p>
              </div>
            </div>

            <div className="kpi-card costs">
              <div className="kpi-icon">💸</div>
              <div className="kpi-content">
                <h3>Gesamtausgaben</h3>
                <p className="kpi-value">{formatPriceSimple(dashboardData.totalCost || 0)} €</p>
              </div>
            </div>

            <div className="kpi-card profit">
              <div className="kpi-icon">📈</div>
              <div className="kpi-content">
                <h3>Gewinn</h3>
                <p className="kpi-value">
                  {formatPriceSimple((dashboardData.totalRevenue || 0) - (dashboardData.totalCost || 0))} €
                </p>
              </div>
            </div>

            <div className="kpi-card orders">
              <div className="kpi-icon">🛒</div>
              <div className="kpi-content">
                <h3>Bestellungen</h3>
                <p className="kpi-value">{dashboardData.totalOrders || 0}</p>
              </div>
            </div>
          </div>

          {/* Beliebte Gerichte */}
          {dashboardData.popularMeals && dashboardData.popularMeals.length > 0 && (
            <div className="popular-meals-section">
              <h3>🏆 Beliebteste Gerichte</h3>
              <div className="popular-meals">
                {dashboardData.popularMeals.map((meal, index) => (
                  <div key={meal.mealId} className="popular-meal-item">
                    <span className="meal-rank">#{index + 1}</span>
                    <div className="meal-details">
                      <strong>{meal.mealName}</strong>
                      <span className="meal-count">{meal.orderCount} Bestellungen</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Prognose-Info */}
          <div className="forecast-section">
            <h3>📉 Prognosen zur Reduktion von Lebensmittelverschwendung</h3>
            <p>
              Basierend auf historischen Daten können Wareneinsatz-Prognosen erstellt werden,
              um Lebensmittelverschwendung zu reduzieren und Nachhaltigkeit zu fördern.
            </p>
            <button className="secondary">Prognose generieren (Coming Soon)</button>
          </div>
        </>
      )}
    </div>
  );
}

export default Dashboard;
