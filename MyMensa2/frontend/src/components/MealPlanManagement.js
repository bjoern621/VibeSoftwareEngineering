import React, { useState, useEffect } from 'react';
import './MealPlanManagement.css';
import api from '../services/api';
import { getTwoWeeks } from '../utils/dateUtils';
import { formatPriceSimple } from '../utils/priceUtils';

/**
 * Komponente für die Speiseplan-Verwaltung
 * 2-Wochen-Ansicht Mo-Fr zum Hinzufügen, Bearbeiten und Löschen
 */
function MealPlanManagement() {
  const [weekMealPlans, setWeekMealPlans] = useState({});
  const [allMeals, setAllMeals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedDay, setSelectedDay] = useState(null);
  const [selectedWeek, setSelectedWeek] = useState(1); // Neue State für Wochenauswahl
  const [formData, setFormData] = useState({
    mealId: '',
    stock: ''
  });

  const today = new Date();
  const twoWeeks = getTwoWeeks(today);
  const currentWeek = twoWeeks.find(w => w.week === selectedWeek);
  const weekDays = currentWeek ? currentWeek.days : [];

  /**
   * Lädt Speisepläne und Gerichte beim Start
   */
  useEffect(() => {
    fetchData();
    // eslint-disable-next-line
  }, []);

  /**
   * Lädt alle Speisepläne der Woche und verfügbare Gerichte
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Alle Gerichte laden
      const mealsData = await api.meals.getAll();
      setAllMeals(mealsData);

      // Speisepläne für alle Wochentage parallel laden
      const plans = {};
      await Promise.all(
        weekDays.map(async (day) => {
          const data = await api.mealPlans.getByDate(day.date);
          plans[day.date] = data;
        })
      );
      
      setWeekMealPlans(plans);
    } catch (err) {
      setError('Fehler beim Laden der Daten: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Zufällige Speisepläne für alle Wochentage generieren
   * Löscht zuerst alle bestehenden Pläne, dann werden für jeden Tag 3 zufällige Gerichte mit 10-50 Portionen ausgewählt
   * 
   * @async
   * @returns {Promise<void>}
   */
  const generateRandomMealPlans = async () => {
    setError(null);
    setSuccess(null);
    setLoading(true);

    try {
      // Hilfsfunktion zum Mischen eines Arrays (Fisher-Yates)
      const shuffleArray = (array) => {
        const shuffled = [...array];
        for (let i = shuffled.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
        }
        return shuffled;
      };

      // SCHRITT 1: Alle bestehenden Speisepläne für die Woche löschen
      console.log('Lösche alte Speisepläne...');
      for (const day of weekDays) {
        const planForDay = weekMealPlans[day.date] || [];
        
        // Lösche alle Gerichte für diesen Tag
        for (const planItem of planForDay) {
          try {
            await api.mealPlans.delete(planItem.mealId, day.date);
            console.log(`Gelöscht: ${planItem.meal?.name} für ${day.date}`);
          } catch (err) {
            console.log(`Fehler beim Löschen von Meal ${planItem.mealId} für ${day.date}: ${err.message}`);
          }
        }
      }

      // SCHRITT 2: Für jeden Wochentag 3 zufällige Gerichte hinzufügen
      console.log('Generiere neue Speisepläne...');
      for (const day of weekDays) {
        // Überspringe, wenn keine Gerichte verfügbar sind
        if (allMeals.length < 3) {
          throw new Error('Mindestens 3 Gerichte müssen vorhanden sein.');
        }

        // 3 zufällige Gerichte für DIESEN Tag auswählen (neue Mischung für jeden Tag!)
        const shuffledMeals = shuffleArray(allMeals);
        const selectedMeals = shuffledMeals.slice(0, 3);

        console.log(`${day.dayName} (${day.date}): ${selectedMeals.map(m => m.name).join(', ')}`);

        // Für jedes ausgewählte Gericht einen Speiseplan-Eintrag erstellen
        for (const meal of selectedMeals) {
          const randomStock = Math.floor(Math.random() * 41) + 10; // 10-50 Portionen

          try {
            await api.mealPlans.create({
              mealId: meal.id,
              date: day.date,
              stock: randomStock
            });
          } catch (err) {
            console.log(`Fehler beim Hinzufügen: ${meal.name} für ${day.date}: ${err.message}`);
          }
        }
      }

      setSuccess('Zufällige Speisepläne für alle Wochentage erfolgreich generiert!');
      await fetchData(); // Aktualisiere die Anzeige
    } catch (err) {
      setError('Fehler beim Generieren der Speisepläne: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Modal zum Hinzufügen eines Gerichts öffnen
   * 
   * @param {Object} day - Gewählter Wochentag
   */
  const handleAddClick = (day) => {
    setSelectedDay(day);
    setFormData({ mealId: '', stock: '' });
    setShowAddForm(true);
  };

  /**
   * Gericht zum Speiseplan hinzufügen
   * 
   * @async
   * @param {Event} e - Submit-Event
   * @returns {Promise<void>}
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!selectedDay) return;

    try {
      await api.mealPlans.create({
        mealId: parseInt(formData.mealId),
        date: selectedDay.date,
        stock: parseInt(formData.stock)
      });

      setSuccess('Gericht erfolgreich zum Speiseplan hinzugefügt!');
      setShowAddForm(false);
      fetchData();
    } catch (err) {
      setError('Fehler beim Hinzufügen: ' + err.message);
    }
  };

  /**
   * Lagerbestand eines Gerichts aktualisieren
   * 
   * @async
   * @param {number} mealId - ID des Gerichts
   * @param {string} date - Datum
   * @param {number} newStock - Neuer Bestand
   * @returns {Promise<void>}
   */
  const handleStockUpdate = async (mealId, date, newStock) => {
    setError(null);
    
    try {
      await api.mealPlans.update(mealId, date, { stock: parseInt(newStock) });
      setSuccess('Bestand aktualisiert!');
      fetchData();
    } catch (err) {
      setError('Fehler beim Aktualisieren: ' + err.message);
    }
  };

  /**
   * Gericht vom Speiseplan entfernen
   * 
   * @async
   * @param {number} mealId - ID des Gerichts
   * @param {string} date - Datum
   * @returns {Promise<void>}
   */
  const handleDelete = async (mealId, date) => {
    if (!window.confirm('Möchten Sie dieses Gericht vom Speiseplan entfernen?')) {
      return;
    }

    setError(null);
    
    try {
      await api.mealPlans.delete(mealId, date);
      setSuccess('Gericht vom Speiseplan entfernt!');
      fetchData();
    } catch (err) {
      setError('Fehler beim Löschen: ' + err.message);
    }
  };

  // Verfügbare Gerichte für Dropdown (noch nicht im Speiseplan des gewählten Tages)
  const getAvailableMeals = () => {
    if (!selectedDay) return allMeals;
    
    const planForDay = weekMealPlans[selectedDay.date] || [];
    const usedMealIds = planForDay.map(p => p.mealId);
    
    return allMeals.filter(meal => !usedMealIds.includes(meal.id));
  };

  return (
    <div className="mealplan-management">
      <div className="mealplan-header">
        <h2>Speiseplanverwaltung</h2>
        <p className="subtitle">2-Wochen-Ansicht - Gerichte für Montag bis Freitag verwalten</p>
        <button 
          className="generate-random-btn"
          onClick={generateRandomMealPlans}
          disabled={loading}
        >
          🎲 Zufällige Speisepläne generieren
        </button>
      </div>

      {/* Wochenauswahl */}
      <div className="week-selector">
        {twoWeeks.map(week => (
          <button
            key={week.week}
            className={`week-button ${selectedWeek === week.week ? 'active' : ''}`}
            onClick={() => setSelectedWeek(week.week)}
          >
            {week.label}
          </button>
        ))}
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {loading ? (
        <div className="loading">Lade Speisepläne...</div>
      ) : (
        <>
          {/* Add-Form Modal */}
          {showAddForm && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3>Gericht hinzufügen: {selectedDay?.dayName}</h3>
                <form onSubmit={handleSubmit}>
                  <div className="form-group">
                    <label htmlFor="mealId">Gericht*</label>
                    <select
                      id="mealId"
                      value={formData.mealId}
                      onChange={(e) => setFormData({ ...formData, mealId: e.target.value })}
                      required
                    >
                      <option value="">Bitte wählen...</option>
                      {getAvailableMeals().map(meal => (
                        <option key={meal.id} value={meal.id}>
                          {meal.name} - {meal.price.toFixed(2)} € ({meal.category})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="stock">Bestand (Portionen)*</label>
                    <input
                      type="number"
                      id="stock"
                      min="1"
                      value={formData.stock}
                      onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-actions">
                    <button type="submit" className="primary">Hinzufügen</button>
                    <button type="button" className="secondary" onClick={() => setShowAddForm(false)}>
                      Abbrechen
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Wochenübersicht */}
          <div className="week-plan-view">
            {weekDays.map((day) => (
              <div key={day.date} className={`day-plan-column ${day.isToday ? 'today' : ''}`}>
                {/* Tag-Header */}
                <div className="day-plan-header">
                  <div className="day-info">
                    <div className="day-name">{day.dayShort}</div>
                    <div className="day-date">{day.dayNum}. {day.monthShort}</div>
                    {day.isToday && <div className="today-badge">Heute</div>}
                  </div>
                  <button 
                    className="add-meal-btn"
                    onClick={() => handleAddClick(day)}
                    title="Gericht hinzufügen"
                  >
                    + Gericht
                  </button>
                </div>

                {/* Gerichte für diesen Tag */}
                <div className="day-plan-meals">
                  {weekMealPlans[day.date] && weekMealPlans[day.date].length > 0 ? (
                    weekMealPlans[day.date].map((mealPlan) => (
                      <div key={mealPlan.mealId} className="plan-meal-card">
                        <div className="plan-meal-info">
                          <h4 className="plan-meal-name">{mealPlan.meal.name}</h4>
                          <span className={`category-badge ${mealPlan.meal.category}`}>
                            {mealPlan.meal.category}
                          </span>
                          <div className="plan-meal-price">{formatPriceSimple(mealPlan.meal.price)} €</div>
                        </div>

                        <div className="plan-meal-stock">
                          <label>Bestand:</label>
                          <input
                            type="number"
                            min="0"
                            value={mealPlan.stock}
                            onChange={(e) => handleStockUpdate(mealPlan.mealId, day.date, e.target.value)}
                            className="stock-input"
                          />
                          <span className="stock-unit">Portionen</span>
                        </div>

                        <button
                          className="delete-meal-btn"
                          onClick={() => handleDelete(mealPlan.mealId, day.date)}
                          title="Entfernen"
                        >
                          🗑️
                        </button>
                      </div>
                    ))
                  ) : (
                    <div className="no-meals-plan">
                      <p>Kein Speiseplan</p>
                      <button 
                        className="add-first-meal-btn"
                        onClick={() => handleAddClick(day)}
                      >
                        + Erstes Gericht hinzufügen
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

export default MealPlanManagement;
