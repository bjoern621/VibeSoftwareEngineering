import React, { useState, useEffect } from 'react';
import './WeekView.css';

/**
 * WeekView Komponente - Zeigt den Speiseplan für eine Woche an
 *
 * Diese Komponente ist dafür zuständig:
 * - Eine Wochenübersicht mit allen Wochentagen anzuzeigen
 * - Zwischen verschiedenen Wochen zu navigieren (vor/zurück)
 * - Das aktuelle Datum hervorzuheben
 * - Gerichte vom Backend zu laden und anzuzeigen
 */
function WeekView() {
  // State Hook: useState speichert den aktuellen Wochenoffset (0 = aktuelle Woche, 1 = nächste Woche, etc.)
  const [weekOffset, setWeekOffset] = useState(0);

  // State für die geladenen Gerichte
  const [meals, setMeals] = useState([]);

  // State für Lade-Status
  const [loading, setLoading] = useState(false);

  // State für Fehler
  const [error, setError] = useState(null);

  // Deutsche Wochentage-Namen - nur Montag bis Freitag
  const weekdays = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag'];

  /**
   * Lädt den Speiseplan vom Backend
   * Backend Endpoint: GET http://localhost:8080/api/meal-plans/week?startDate=2025-01-13
   * Erwartete Response: Array mit { date: "2025-01-13", meals: [{id, name, description, price, categories, allergens}] }
   */
  const getMealPlanForWeek = async (startDate) => {
    const dateString = startDate.toISOString().split('T')[0];
    const response = await fetch(`http://localhost:8080/api/meal-plans/week?startDate=${dateString}`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  };

  /**
   * useEffect Hook - wird ausgeführt, wenn sich weekOffset ändert
   * Lädt die Gerichte für die ausgewählte Woche vom Backend
   */
  useEffect(() => {
    const loadMealPlan = async () => {
      setLoading(true);
      setError(null);

      try {
        const weekStart = getWeekStart(weekOffset);
        const data = await getMealPlanForWeek(weekStart);
        setMeals(data);
      } catch (err) {
        setError('Fehler beim Laden des Speiseplans. Bitte versuche es später erneut.');
        console.error('Fehler beim Laden:', err);
      } finally {
        setLoading(false);
      }
    };

    loadMealPlan();
  }, [weekOffset]);

  /**
   * Berechnet das Startdatum (Montag) der aktuell angezeigten Woche
   */
  const getWeekStart = (offset) => {
    const today = new Date();
    const dayOfWeek = today.getDay();
    const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;

    const monday = new Date(today);
    monday.setDate(today.getDate() + diff + (offset * 7));
    monday.setHours(0, 0, 0, 0);

    return monday;
  };

  /**
   * Erstellt ein Array mit allen Daten der Woche (Montag bis Freitag)
   */
  const getWeekDates = () => {
    const weekStart = getWeekStart(weekOffset);
    const dates = [];

    for (let i = 0; i < 5; i++) {
      const date = new Date(weekStart);
      date.setDate(weekStart.getDate() + i);
      dates.push(date);
    }

    return dates;
  };

  /**
   * Prüft, ob ein Datum heute ist
   */
  const isToday = (date) => {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  };

  /**
   * Formatiert ein Datum schön (z.B. "13.01.2025")
   */
  const formatDate = (date) => {
    return date.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  /**
   * Formatiert Monat und Jahr für die Überschrift
   */
  const getMonthYearString = () => {
    const weekStart = getWeekStart(weekOffset);
    return weekStart.toLocaleDateString('de-DE', { month: 'long', year: 'numeric' });
  };

  /**
   * Findet die Gerichte für ein bestimmtes Datum
   */
  const getMealsForDate = (date) => {
    const dateString = date.toISOString().split('T')[0];
    const dayData = meals.find(day => day.date === dateString);
    return dayData ? dayData.meals : [];
  };

  const weekDates = getWeekDates();

  return (
    <div className="week-view-container page-container">
      <div className="week-view-header gradient-header">
        <button
          className="nav-button btn btn-light"
          onClick={() => setWeekOffset(weekOffset - 1)}
          aria-label="Vorherige Woche"
        >
          ← Vorherige Woche
        </button>

        <h2 className="week-title">
          Speiseplan - {getMonthYearString()}
        </h2>

        <button
          className="nav-button btn btn-light"
          onClick={() => setWeekOffset(weekOffset + 1)}
          aria-label="Nächste Woche"
        >
          Nächste Woche →
        </button>
      </div>

      {weekOffset !== 0 && (
        <div className="current-week-button-container text-center mb-medium">
          <button
            className="current-week-button btn btn-success"
            onClick={() => setWeekOffset(0)}
          >
            Zur aktuellen Woche
          </button>
        </div>
      )}

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {loading && (
        <div className="text-center mb-medium">
          <div className="loading-spinner"></div>
          <p className="text-muted mt-small">Lade Speiseplan...</p>
        </div>
      )}

      <div className="week-grid">
        {weekdays.map((day, index) => {
          const date = weekDates[index];
          const today = isToday(date);
          const dayMeals = getMealsForDate(date);

          return (
            <div
              key={index}
              className={`day-card card ${today ? 'today' : ''}`}
            >
              <div className="day-header card-header">
                <h3 className="day-name card-title">{day}</h3>
                <p className="day-date text-muted">{formatDate(date)}</p>
                {today && <span className="today-badge badge badge-primary">Heute</span>}
              </div>

              <div className="day-content card-content">
                {!loading && dayMeals.length === 0 && (
                  <p className="placeholder-text">
                    Noch keine Gerichte für diesen Tag
                  </p>
                )}

                {dayMeals.length > 0 && (
                  <div className="meals-list">
                    {dayMeals.map((meal) => (
                      <div key={meal.id} className="meal-item">
                        <h4 className="meal-name">{meal.name}</h4>
                        <p className="meal-description text-muted">{meal.description}</p>
                        <div className="meal-info">
                          <span className="meal-price">{meal.price.toFixed(2)} €</span>
                          <div className="meal-categories">
                            {meal.categories.map((cat, idx) => (
                              <span key={idx} className="badge badge-success">
                                {cat}
                              </span>
                            ))}
                          </div>
                        </div>
                        {meal.allergens && meal.allergens.length > 0 && (
                          <div className="meal-allergens">
                            <small className="text-danger">
                              Allergene: {meal.allergens.join(', ')}
                            </small>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default WeekView;

