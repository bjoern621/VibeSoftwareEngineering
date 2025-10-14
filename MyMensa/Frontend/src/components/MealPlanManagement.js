import React, { useState, useEffect } from 'react';
import './MealPlanManagement.css';

/**
 * MealPlanManagement Komponente - Speiseplan erstellen und verwalten
 *
 * Backend-Anbindung:
 * - GET /api/meals - Alle verfügbaren Gerichte laden
 * - GET /api/meal-plans?startDate=X&endDate=Y - Speiseplan für Zeitraum laden
 * - PUT /api/meal-plans - Gericht zum Speiseplan hinzufügen/aktualisieren
 * - DELETE /api/meal-plans?mealId=X&date=Y - Gericht aus Speiseplan entfernen
 */
function MealPlanManagement() {
    const [weekOffset, setWeekOffset] = useState(0);
    const [meals, setMeals] = useState([]);
    const [mealPlan, setMealPlan] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [selectedDate, setSelectedDate] = useState(null);
    const [selectedMealId, setSelectedMealId] = useState('');
    const [stock, setStock] = useState('');

    const weekdays = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag'];

    // Wochenberechnungen
    const getWeekStart = (offset) => {
        const today = new Date();
        const dayOfWeek = today.getDay();
        const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
        const monday = new Date(today);
        monday.setDate(today.getDate() + diff + (offset * 7));
        monday.setHours(0, 0, 0, 0);
        return monday;
    };

    const getWeekEnd = (offset) => {
        const weekStart = getWeekStart(offset);
        const friday = new Date(weekStart);
        friday.setDate(weekStart.getDate() + 4);
        return friday;
    };

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

    const formatDate = (date) => {
        return date.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });
    };

    const getMonthYearString = () => {
        const weekStart = getWeekStart(weekOffset);
        return weekStart.toLocaleDateString('de-DE', { month: 'long', year: 'numeric' });
    };

    // API Calls
    const loadMeals = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/meals');
            if (!response.ok) throw new Error('Fehler beim Laden der Gerichte');
            const data = await response.json();
            setMeals(data);
        } catch (err) {
            console.error('Fehler beim Laden der Gerichte:', err);
        }
    };

    const loadMealPlan = async () => {
        setLoading(true);
        setError(null);
        try {
            const startDate = getWeekStart(weekOffset).toISOString().split('T')[0];
            const endDate = getWeekEnd(weekOffset).toISOString().split('T')[0];
            const response = await fetch(`http://localhost:8080/api/meal-plans?startDate=${startDate}&endDate=${endDate}`);
            if (!response.ok) throw new Error('Fehler beim Laden des Speiseplans');
            const data = await response.json();
            setMealPlan(data);
        } catch (err) {
            setError('Fehler beim Laden des Speiseplans');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const addMealToPlan = async () => {
        if (!selectedMealId || !selectedDate || !stock) {
            alert('Bitte alle Felder ausfüllen!');
            return;
        }

        setLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/meal-plans', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    mealId: parseInt(selectedMealId),
                    date: selectedDate.toISOString().split('T')[0],
                    stock: parseInt(stock)
                })
            });

            if (!response.ok) throw new Error('Fehler beim Hinzufügen');

            await loadMealPlan();
            setShowAddModal(false);
            setSelectedMealId('');
            setStock('');
            setSelectedDate(null);
        } catch (err) {
            alert('Fehler beim Hinzufügen des Gerichts');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const removeMealFromPlan = async (mealId, date) => {
        if (!window.confirm('Gericht wirklich aus dem Speiseplan entfernen?')) return;

        setLoading(true);
        try {
            const dateString = date.toISOString().split('T')[0];
            const response = await fetch(`http://localhost:8080/api/meal-plans?mealId=${mealId}&date=${dateString}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error('Fehler beim Entfernen');
            await loadMealPlan();
        } catch (err) {
            alert('Fehler beim Entfernen des Gerichts');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const getMealsForDate = (date) => {
        const dateString = date.toISOString().split('T')[0];
        const dayData = mealPlan.find(day => day.date === dateString);
        if (dayData && dayData.meals) {
            return dayData.meals.map(item => ({
                ...item.meal,
                availableStock: item.stock
            }));
        }
        return [];
    };

    const openAddModal = (date) => {
        setSelectedDate(date);
        setShowAddModal(true);
    };

    useEffect(() => {
        loadMeals();
    }, []);

    useEffect(() => {
        loadMealPlan();
    }, [weekOffset]);

    const weekDates = getWeekDates();

    return (
        <div className="meal-plan-management-container">
            {/* Header */}
            <div className="gradient-header">
                <button
                    className="nav-button btn btn-light"
                    onClick={() => setWeekOffset(weekOffset - 1)}
                >
                    ← Vorherige Woche
                </button>
                <h1 className="week-title">Speiseplan verwalten - {getMonthYearString()}</h1>
                <button
                    className="nav-button btn btn-light"
                    onClick={() => setWeekOffset(weekOffset + 1)}
                >
                    Nächste Woche →
                </button>
            </div>

            {weekOffset !== 0 && (
                <div className="text-center mb-medium">
                    <button className="btn btn-success" onClick={() => setWeekOffset(0)}>
                        Zur aktuellen Woche
                    </button>
                </div>
            )}

            {error && <div className="error-message">⚠️ {error}</div>}

            {loading && (
                <div className="text-center mb-medium">
                    <div className="loading-spinner"></div>
                    <p>Lade Speiseplan...</p>
                </div>
            )}

            {/* Wochenübersicht */}
            <div className="week-grid">
                {weekdays.map((day, index) => {
                    const date = weekDates[index];
                    const dayMeals = getMealsForDate(date);

                    return (
                        <div key={index} className="day-card">
                            <div className="day-header">
                                <h3>{day}</h3>
                                <p className="text-muted">{formatDate(date)}</p>
                                <button
                                    className="btn btn-primary btn-sm"
                                    onClick={() => openAddModal(date)}
                                >
                                    ➕ Gericht hinzufügen
                                </button>
                            </div>

                            <div className="day-content">
                                {dayMeals.length === 0 ? (
                                    <p className="text-muted">Keine Gerichte</p>
                                ) : (
                                    <div className="meals-list">
                                        {dayMeals.map((meal) => (
                                            <div key={meal.id} className="meal-item">
                                                <div className="meal-info">
                                                    <h4>{meal.name}</h4>
                                                    <p className="text-muted">{meal.description}</p>
                                                    <span className="badge badge-info">
                                                        📦 {meal.availableStock} Portionen
                                                    </span>
                                                    <span className="badge badge-success">
                                                        {meal.price?.toFixed(2)} €
                                                    </span>
                                                </div>
                                                <button
                                                    className="btn-icon btn-delete"
                                                    onClick={() => removeMealFromPlan(meal.id, date)}
                                                    title="Entfernen"
                                                >
                                                    🗑️
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Modal zum Hinzufügen */}
            {showAddModal && (
                <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h2>Gericht zum Speiseplan hinzufügen</h2>
                        <p className="text-muted">Datum: {selectedDate && formatDate(selectedDate)}</p>

                        <div className="form-group">
                            <label>Gericht auswählen *</label>
                            <select
                                value={selectedMealId}
                                onChange={(e) => setSelectedMealId(e.target.value)}
                                required
                            >
                                <option value="">-- Bitte wählen --</option>
                                {meals.map(meal => (
                                    <option key={meal.id} value={meal.id}>
                                        {meal.name} ({meal.price?.toFixed(2)} €)
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Anzahl Portionen *</label>
                            <input
                                type="number"
                                min="1"
                                value={stock}
                                onChange={(e) => setStock(e.target.value)}
                                placeholder="z.B. 50"
                                required
                            />
                        </div>

                        <div className="form-actions">
                            <button
                                className="btn btn-success"
                                onClick={addMealToPlan}
                                disabled={loading}
                            >
                                {loading ? '⏳ Hinzufügen...' : '✅ Hinzufügen'}
                            </button>
                            <button
                                className="btn btn-light"
                                onClick={() => setShowAddModal(false)}
                            >
                                Abbrechen
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default MealPlanManagement;

