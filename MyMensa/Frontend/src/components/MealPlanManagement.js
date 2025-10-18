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
    const [editMode, setEditMode] = useState(false); // Neu: Bearbeitungsmodus

    const weekdays = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag'];

    // Helper: lokales ISO-Datum ohne timezone-shift (wie bei OrderManagement)
    const toLocalISODate = (d) => {
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    };

    // Wochenberechnungen
    const getWeekStart = (offset) => {
        const today = new Date();
        const localDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
        const dayOfWeek = localDay.getDay();

        // Ab Samstag (dayOfWeek === 6) die nächste Woche anzeigen
        let diff;
        if (dayOfWeek === 0) {
            // Sonntag: nächsten Montag
            diff = 1;
        } else if (dayOfWeek === 6) {
            // Samstag: nächsten Montag (in 2 Tagen)
            diff = 2;
        } else {
            // Montag-Freitag: aktueller/letzter Montag
            diff = 1 - dayOfWeek;
        }

        const monday = new Date(localDay);
        monday.setDate(localDay.getDate() + diff + (offset * 7));
        monday.setHours(0, 0, 0, 0);
        return monday;
    };

    const getWeekEnd = (offset) => {
        const weekStart = getWeekStart(offset);
        const friday = new Date(weekStart);
        friday.setDate(weekStart.getDate() + 4);
        friday.setHours(23, 59, 59, 999);
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
            const startDate = toLocalISODate(getWeekStart(weekOffset));
            const endDate = toLocalISODate(getWeekEnd(weekOffset));
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

    // Einheitliche Close-Funktion für das Modal: resettet Felder sauber
    const closeModal = () => {
        if (loading) return; // während eines Speichervorgangs nicht schließen
        setShowAddModal(false);
        setSelectedDate(null);
        setSelectedMealId('');
        setStock('');
        setEditMode(false);
    };

    const addMealToPlan = async () => {
        if (!selectedMealId || !selectedDate || !stock) {
            alert('Bitte alle Felder ausfüllen!');
            return;
        }

        setLoading(true);
        try {
            // selectedDate kann (vorsichtig) String oder Date sein -> sicherstellen
            const dateObj = (selectedDate instanceof Date) ? selectedDate : new Date(selectedDate);
            const response = await fetch('http://localhost:8080/api/meal-plans', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    mealId: parseInt(selectedMealId),
                    date: toLocalISODate(dateObj),
                    stock: parseInt(stock)
                })
            });

            if (!response.ok) throw new Error('Fehler beim Hinzufügen');

            // Neu laden
            await loadMealPlan();

            // Erfolgreich: loading beenden, dann Modal schließen (für saubere Transition)
            setLoading(false);

            // Kleiner Timeout für saubere Transition
            setTimeout(() => {
                closeModal();
            }, 100);

        } catch (err) {
            setLoading(false);
            alert(editMode ? 'Fehler beim Aktualisieren des Gerichts' : 'Fehler beim Hinzufügen des Gerichts');
            console.error(err);
        }
    };

    const removeMealFromPlan = async (mealId, date) => {
        if (!window.confirm('Gericht wirklich aus dem Speiseplan entfernen?')) return;

        setLoading(true);
        try {
            const dateString = toLocalISODate(date);
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
        const dateString = toLocalISODate(date);
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
        setEditMode(false);
        setSelectedMealId('');
        setStock('');
        setShowAddModal(true);
    };

    const openEditModal = (date, meal) => {
        setSelectedDate(date);
        setSelectedMealId(meal.id.toString());
        setStock(meal.availableStock.toString());
        setEditMode(true);
        setShowAddModal(true);

        // kleine UX-Verbesserung: scroll zur Modal-Position
        window.setTimeout(() => {
            const el = document.querySelector('.modal-content');
            if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }, 100);
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
                                    disabled={loading}
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
                                                <div className="meal-actions">
                                                    <button
                                                        className="btn-icon btn-edit"
                                                        onClick={() => openEditModal(date, meal)}
                                                        title="Bearbeiten"
                                                        disabled={loading}
                                                    >
                                                        ✏️
                                                    </button>
                                                    <button
                                                        className="btn-icon btn-delete"
                                                        onClick={() => removeMealFromPlan(meal.id, date)}
                                                        title="Entfernen"
                                                        disabled={loading}
                                                    >
                                                        🗑️
                                                    </button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Modal zum Hinzufügen/Bearbeiten */}
            {showAddModal && (
                <div className="modal-overlay" onClick={() => { if (!loading) closeModal(); }}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
                        <h2>{editMode ? 'Portionsanzahl bearbeiten' : 'Gericht zum Speiseplan hinzufügen'}</h2>
                        <p className="text-muted">Datum: {selectedDate && formatDate(selectedDate)}</p>

                        <div className="form-group">
                            <label>Gericht auswählen *</label>
                            <select
                                value={selectedMealId}
                                onChange={(e) => setSelectedMealId(e.target.value)}
                                required
                                disabled={editMode || loading}
                            >
                                <option value="">-- Bitte wählen --</option>
                                {meals.map(meal => (
                                    <option key={meal.id} value={meal.id}>
                                        {meal.name} ({meal.price?.toFixed(2)} €)
                                    </option>
                                ))}
                            </select>
                            {editMode && <small className="text-muted">Im Bearbeitungsmodus kann das Gericht nicht geändert werden</small>}
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
                                disabled={loading}
                            />
                        </div>

                        <div className="form-actions">
                            <button
                                className="btn btn-success"
                                onClick={addMealToPlan}
                                disabled={loading}
                            >
                                {loading ? '⏳ Speichern...' : (editMode ? '✅ Aktualisieren' : '✅ Hinzufügen')}
                            </button>
                            <button
                                className="btn btn-light"
                                onClick={() => closeModal()}
                                disabled={loading}
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
