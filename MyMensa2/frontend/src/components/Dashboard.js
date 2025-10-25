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
  const [forecastData, setForecastData] = useState(null);
  const [showForecast, setShowForecast] = useState(false);
  const [dateRange, setDateRange] = useState({
    startDate: formatDate(addDays(new Date(), -7)),
    endDate: formatDate(new Date())
  });
  const [loading, setLoading] = useState(false);
  const [forecastLoading, setForecastLoading] = useState(false);
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

  /**
   * Prognose generieren basierend auf historischen Daten
   * 
   * @async
   * @returns {Promise<void>}
   */
  const generateForecast = async () => {
    setForecastLoading(true);
    setError(null);
    try {
      // Berechne Prognosezeitraum: nächste 7 Tage
      const startDate = formatDate(addDays(new Date(), 1));
      const endDate = formatDate(addDays(new Date(), 7));
      
      console.log('🔍 Prognose-Anfrage:', { startDate, endDate });
      const forecast = await api.forecasts.getDemandForecast(startDate, endDate);
      console.log('📊 Prognose-Daten vom Backend:', forecast);
      console.log('📦 Anzahl Meal-Forecasts:', forecast.mealForecasts?.length);
      console.log('🥬 Anzahl Ingredient-Forecasts:', forecast.ingredientForecasts?.length);
      
      setForecastData(forecast);
      setShowForecast(true);
    } catch (err) {
      console.error('❌ Fehler bei Prognose-Generierung:', err);
      setError('Fehler beim Generieren der Prognose: ' + err.message);
    } finally {
      setForecastLoading(false);
    }
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
        <div className="date-range-inputs">
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
        </div>
        <button className="refresh-button" onClick={fetchDashboardData} title="Daten aktualisieren">
          <span className="refresh-icon">🔄</span>
          <span className="refresh-text">Aktualisieren</span>
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
                <p className="kpi-value">{formatPriceSimple(dashboardData.totalExpenses || 0)} €</p>
              </div>
            </div>

            <div className="kpi-card profit">
              <div className="kpi-icon">📈</div>
              <div className="kpi-content">
                <h3>Gewinn</h3>
                <p className="kpi-value">
                  {formatPriceSimple((dashboardData.totalRevenue || 0) - (dashboardData.totalExpenses || 0))} €
                </p>
              </div>
            </div>

            <div className="kpi-card orders">
              <div className="kpi-icon">🛒</div>
              <div className="kpi-content">
                <h3>Verkaufte Portionen</h3>
                <p className="kpi-value">
                  {dashboardData.mealStats?.reduce((sum, stat) => sum + stat.quantitySold, 0) || 0}
                </p>
              </div>
            </div>
          </div>

          {/* Statistik nach Gerichten */}
          {dashboardData.mealStats && dashboardData.mealStats.length > 0 && (
            <div className="meal-stats-section">
              <h3>📊 Gerichte-Statistik</h3>
              <div className="meal-stats-table">
                <table>
                  <thead>
                    <tr>
                      <th>Gericht</th>
                      <th>Verkauft</th>
                      <th>Einnahmen</th>
                      <th>Kosten</th>
                      <th>Gewinn</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dashboardData.mealStats
                      .sort((a, b) => b.quantitySold - a.quantitySold)
                      .map((stat, index) => (
                        <tr key={index}>
                          <td className="meal-name">{stat.mealName}</td>
                          <td className="quantity">{stat.quantitySold}</td>
                          <td className="revenue">{formatPriceSimple(stat.totalRevenue)} €</td>
                          <td className="expenses">{formatPriceSimple(stat.totalExpenses)} €</td>
                          <td className="profit">{formatPriceSimple(stat.totalRevenue - stat.totalExpenses)} €</td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Prognose-Sektion */}
          <div className="forecast-section">
            <h3>📉 Prognosen zur Reduktion von Lebensmittelverschwendung</h3>
            <p>
              Basierend auf historischen Daten können Wareneinsatz-Prognosen erstellt werden,
              um Lebensmittelverschwendung zu reduzieren und Nachhaltigkeit zu fördern.
            </p>
            <button 
              className="forecast-button" 
              onClick={generateForecast}
              disabled={forecastLoading}
            >
              {forecastLoading ? '⏳ Generiere Prognose...' : '🎯 Prognose generieren'}
            </button>

            {/* Prognose-Ergebnisse */}
            {showForecast && forecastData && (
              <div className="forecast-results">
                <h4>📊 Prognose für die nächsten 7 Tage</h4>
                <p className="forecast-period">
                  {forecastData.forecastPeriod.startDate} bis {forecastData.forecastPeriod.endDate}
                </p>

                {/* Meal Forecasts */}
                <div className="forecast-meals">
                  <h5>🍽️ Empfohlene Bestände pro Gericht</h5>
                  <div className="forecast-grid">
                    {forecastData.mealForecasts.map((mealForecast, index) => (
                      <div key={index} className="forecast-card">
                        <div className="forecast-meal-name">{mealForecast.mealName}</div>
                        <div className="forecast-metrics">
                          <div className="forecast-metric">
                            <span className="metric-label">Ø Tägliche Nachfrage:</span>
                            <span className="metric-value">{mealForecast.averageDailyDemand.toFixed(1)} Portionen</span>
                          </div>
                          <div className="forecast-metric">
                            <span className="metric-label">Empfohlener Bestand:</span>
                            <span className="metric-value highlight">{mealForecast.recommendedStock} Portionen</span>
                          </div>
                          <div className="forecast-metric">
                            <span className="metric-label">Konfidenz:</span>
                            <span className={`metric-value confidence ${mealForecast.confidenceLevel >= 0.8 ? 'high' : 'medium'}`}>
                              {(mealForecast.confidenceLevel * 100).toFixed(0)}%
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Ingredient Forecasts */}
                {forecastData.ingredientForecasts && forecastData.ingredientForecasts.length > 0 && (
                  <div className="forecast-ingredients">
                    <h5>🥬 Zutaten-Bedarf</h5>
                    <table className="ingredients-forecast-table">
                      <thead>
                        <tr>
                          <th>Zutat</th>
                          <th>Geschätzter Verbrauch</th>
                          <th>Aktueller Bestand</th>
                          <th>Nachbestellung</th>
                        </tr>
                      </thead>
                      <tbody>
                        {forecastData.ingredientForecasts.map((ingredient, index) => (
                          <tr key={index} className={ingredient.recommendedPurchase > 0 ? 'needs-reorder' : ''}>
                            <td className="ingredient-name">{ingredient.ingredientName}</td>
                            <td>{ingredient.estimatedConsumption.toFixed(1)}</td>
                            <td>{ingredient.currentStock.toFixed(1)}</td>
                            <td className={ingredient.recommendedPurchase > 0 ? 'reorder-amount' : ''}>
                              {ingredient.recommendedPurchase > 0 
                                ? `${ingredient.recommendedPurchase.toFixed(1)} ✅`
                                : '✓ Ausreichend'}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default Dashboard;
