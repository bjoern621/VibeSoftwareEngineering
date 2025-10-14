import React, { useState, useEffect } from 'react';
import './MealManagement.css';

/**
 * MealManagement Komponente - Verwaltung aller Gerichte
 *
 * Backend-Anbindung:
 * - GET /api/meals - Alle Gerichte laden
 * - POST /api/meals - Neues Gericht erstellen
 * - PUT /api/meals/{id} - Gericht aktualisieren
 * - DELETE /api/meals/{id} - Gericht löschen
 *
 * Erwartetes Gericht-Objekt:
 * {
 *   id: number,
 *   name: string,
 *   description: string,
 *   price: number,
 *   categories: string[] (z.B. ["vegetarisch", "vegan"]),
 *   allergens: string[] (z.B. ["Gluten", "Nüsse"])
 * }
 */
function MealManagement() {
    const [meals, setMeals] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [editingMeal, setEditingMeal] = useState(null);
    const [selectedCategory, setSelectedCategory] = useState('Alle');

    // Formular-State
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        price: '',
        cost: '',
        stock: '',
        ingredients: '',
        nutritionalInfo: {
            calories: '',
            protein: '',
            carbs: '',
            fat: ''
        },
        categories: [],
        allergens: []
    });

    // Verfügbare Kategorien
    const categories = ['Alle', 'Vegetarisch', 'Vegan', 'Halal', 'Glutenfrei'];
    const availableAllergens = ['Gluten', 'Milch/Laktose', 'Nüsse', 'Eier', 'Fisch', 'Soja'];

    /**
     * Lädt alle Gerichte vom Backend
     * Backend Endpoint: GET /api/meals
     */
    const loadMeals = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('http://localhost:8080/api/meals');
            if (!response.ok) throw new Error('Fehler beim Laden der Gerichte');
            const data = await response.json();
            setMeals(data);
        } catch (err) {
            setError(err.message);
            setMeals([]); // Leere Liste bei Fehler
        } finally {
            setLoading(false);
        }
    };

    /**
     * Speichert ein neues oder bearbeitetes Gericht
     * Backend Endpoint: POST /api/meals oder PUT /api/meals/{id}
     */
    const saveMeal = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const url = editingMeal
                ? `http://localhost:8080/api/meals/${editingMeal.id}`
                : 'http://localhost:8080/api/meals';

            const method = editingMeal ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ...formData,
                    price: parseFloat(formData.price) || 0,
                    cost: parseFloat(formData.cost) || 0,
                    stock: parseInt(formData.stock) || 0,
                    nutritionalInfo: {
                        calories: parseInt(formData.nutritionalInfo.calories) || 0,
                        protein: parseFloat(formData.nutritionalInfo.protein) || 0,
                        carbs: parseFloat(formData.nutritionalInfo.carbs) || 0,
                        fat: parseFloat(formData.nutritionalInfo.fat) || 0
                    }
                })
            });

            if (!response.ok) throw new Error('Fehler beim Speichern');

            await loadMeals();
            resetForm();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    /**
     * Löscht ein Gericht
     * Backend Endpoint: DELETE /api/meals/{id}
     */
    const deleteMeal = async (id) => {
        if (!window.confirm('Gericht wirklich löschen?')) return;

        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`http://localhost:8080/api/meals/${id}`, {
                method: 'DELETE'
            });
            if (!response.ok) throw new Error('Fehler beim Löschen');
            await loadMeals();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    // Formular zurücksetzen
    const resetForm = () => {
        setFormData({
            name: '',
            description: '',
            price: '',
            categories: [],
            allergens: []
        });
        setEditingMeal(null);
        setShowForm(false);
    };

    // Gericht bearbeiten
    const editMeal = (meal) => {
        setFormData({
            name: meal.name,
            description: meal.description,
            price: meal.price?.toString() || '',
            cost: meal.cost?.toString() || '',
            stock: meal.stock?.toString() || '',
            ingredients: meal.ingredients || '',
            nutritionalInfo: {
                calories: meal.nutritionalInfo?.calories?.toString() || '',
                protein: meal.nutritionalInfo?.protein?.toString() || '',
                carbs: meal.nutritionalInfo?.carbs?.toString() || '',
                fat: meal.nutritionalInfo?.fat?.toString() || ''
            },
            categories: meal.categories || [],
            allergens: meal.allergens || []
        });
        setEditingMeal(meal);
        setShowForm(true);
    };

    // Checkbox Handler
    const toggleCategory = (category) => {
        setFormData(prev => ({
            ...prev,
            categories: prev.categories.includes(category)
                ? prev.categories.filter(c => c !== category)
                : [...prev.categories, category]
        }));
    };

    const toggleAllergen = (allergen) => {
        setFormData(prev => ({
            ...prev,
            allergens: prev.allergens.includes(allergen)
                ? prev.allergens.filter(a => a !== allergen)
                : [...prev.allergens, allergen]
        }));
    };

    // Gerichte filtern
    const filteredMeals = selectedCategory === 'Alle'
        ? meals
        : meals.filter(meal => meal.categories?.includes(selectedCategory));

    // Beim ersten Laden
    useEffect(() => {
        loadMeals();
    }, []);

    return (
        <div className="meal-management-container">
            {/* Header */}
            <div className="gradient-header">
                <h1 className="week-title">Gerichteverwaltung</h1>
                <button className="btn btn-light" onClick={() => setShowForm(!showForm)}>
                    {showForm ? '❌ Abbrechen' : '➕ Neues Gericht'}
                </button>
            </div>

            {/* Fehler-Anzeige */}
            {error && (
                <div className="error-message">
                    ⚠️ {error}
                </div>
            )}

            {/* Formular */}
            {showForm && (
                <div className="meal-form-card">
                    <h2>{editingMeal ? 'Gericht bearbeiten' : 'Neues Gericht erstellen'}</h2>
                    <form onSubmit={saveMeal}>
                        <div className="form-group">
                            <label>Gerichtname *</label>
                            <input
                                type="text"
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                                required
                                placeholder="z.B. Spaghetti Bolognese"
                            />
                        </div>

                        <div className="form-group">
                            <label>Beschreibung</label>
                            <textarea
                                value={formData.description}
                                onChange={(e) => setFormData({...formData, description: e.target.value})}
                                placeholder="Kurze Beschreibung des Gerichts"
                                rows="3"
                            />
                        </div>

                        {/* Preis & Kosten in einer Reihe */}
                        <div className="form-row">
                            <div className="form-group">
                                <label>Verkaufspreis (€) *</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    value={formData.price}
                                    onChange={(e) => setFormData({...formData, price: e.target.value})}
                                    required
                                    placeholder="z.B. 6.50"
                                />
                            </div>

                            <div className="form-group">
                                <label>Kosten (€)</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    value={formData.cost}
                                    onChange={(e) => setFormData({...formData, cost: e.target.value})}
                                    placeholder="z.B. 3.20"
                                />
                            </div>

                            <div className="form-group">
                                <label>Bestand</label>
                                <input
                                    type="number"
                                    min="0"
                                    value={formData.stock}
                                    onChange={(e) => setFormData({...formData, stock: e.target.value})}
                                    placeholder="z.B. 50"
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Zutaten</label>
                            <textarea
                                value={formData.ingredients}
                                onChange={(e) => setFormData({...formData, ingredients: e.target.value})}
                                placeholder="z.B. Nudeln, Hackfleisch, Tomatensauce, Zwiebeln"
                                rows="2"
                            />
                        </div>

                        {/* Nährwertinformationen */}
                        <div className="form-section">
                            <h3 className="section-heading">Nährwertinformationen (pro Portion)</h3>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Kalorien (kcal)</label>
                                    <input
                                        type="number"
                                        min="0"
                                        value={formData.nutritionalInfo.calories}
                                        onChange={(e) => setFormData({
                                            ...formData,
                                            nutritionalInfo: {...formData.nutritionalInfo, calories: e.target.value}
                                        })}
                                        placeholder="z.B. 650"
                                    />
                                </div>

                                <div className="form-group">
                                    <label>Protein (g)</label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        min="0"
                                        value={formData.nutritionalInfo.protein}
                                        onChange={(e) => setFormData({
                                            ...formData,
                                            nutritionalInfo: {...formData.nutritionalInfo, protein: e.target.value}
                                        })}
                                        placeholder="z.B. 28.5"
                                    />
                                </div>

                                <div className="form-group">
                                    <label>Kohlenhydrate (g)</label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        min="0"
                                        value={formData.nutritionalInfo.carbs}
                                        onChange={(e) => setFormData({
                                            ...formData,
                                            nutritionalInfo: {...formData.nutritionalInfo, carbs: e.target.value}
                                        })}
                                        placeholder="z.B. 75.0"
                                    />
                                </div>

                                <div className="form-group">
                                    <label>Fett (g)</label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        min="0"
                                        value={formData.nutritionalInfo.fat}
                                        onChange={(e) => setFormData({
                                            ...formData,
                                            nutritionalInfo: {...formData.nutritionalInfo, fat: e.target.value}
                                        })}
                                        placeholder="z.B. 18.3"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Kategorien</label>
                            <div className="checkbox-group">
                                {categories.filter(c => c !== 'Alle').map(category => (
                                    <label key={category} className="checkbox-label">
                                        <input
                                            type="checkbox"
                                            checked={formData.categories.includes(category)}
                                            onChange={() => toggleCategory(category)}
                                        />
                                        <span>{category}</span>
                                    </label>
                                ))}
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Allergene</label>
                            <div className="checkbox-group">
                                {availableAllergens.map(allergen => (
                                    <label key={allergen} className="checkbox-label">
                                        <input
                                            type="checkbox"
                                            checked={formData.allergens.includes(allergen)}
                                            onChange={() => toggleAllergen(allergen)}
                                        />
                                        <span>{allergen}</span>
                                    </label>
                                ))}
                            </div>
                        </div>

                        <div className="form-actions">
                            <button type="submit" className="btn btn-success" disabled={loading}>
                                {loading ? '⏳ Speichern...' : '💾 Speichern'}
                            </button>
                            <button type="button" className="btn btn-light" onClick={resetForm}>
                                Abbrechen
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Filter */}
            <div className="filter-section">
                <label>Filter nach Kategorie:</label>
                <select
                    value={selectedCategory}
                    onChange={(e) => setSelectedCategory(e.target.value)}
                    className="filter-select"
                >
                    {categories.map(cat => (
                        <option key={cat} value={cat}>{cat}</option>
                    ))}
                </select>
            </div>

            {/* Gerichte-Liste */}
            <div className="meals-grid">
                {loading && meals.length === 0 ? (
                    <div className="loading-message">⏳ Lade Gerichte...</div>
                ) : filteredMeals.length === 0 ? (
                    <div className="empty-message">
                        Keine Gerichte gefunden. Erstelle das erste Gericht!
                    </div>
                ) : (
                    filteredMeals.map(meal => (
                        <div key={meal.id} className="meal-card">
                            <div className="meal-header">
                                <h3>{meal.name}</h3>
                                <span className="meal-price">{meal.price?.toFixed(2) || '0.00'} €</span>
                            </div>

                            {meal.description && (
                                <p className="meal-description">{meal.description}</p>
                            )}

                            {/* Kosten & Bestand Info */}
                            {(meal.cost || meal.stock) && (
                                <div className="meal-meta">
                                    {meal.cost && (
                                        <div className="meta-item">
                                            <small>💰 Kosten: {meal.cost.toFixed(2)} €</small>
                                        </div>
                                    )}
                                    {meal.stock !== undefined && (
                                        <div className="meta-item">
                                            <small>📦 Bestand: {meal.stock}</small>
                                        </div>
                                    )}
                                    {meal.cost && meal.price && (
                                        <div className="meta-item profit">
                                            <small>💹 Gewinn: {(meal.price - meal.cost).toFixed(2)} €</small>
                                        </div>
                                    )}
                                </div>
                            )}

                            {/* Nährwertinformationen */}
                            {meal.nutritionalInfo && (meal.nutritionalInfo.calories || meal.nutritionalInfo.protein) && (
                                <div className="meal-nutrition">
                                    {meal.nutritionalInfo.calories > 0 && (
                                        <span className="nutrition-item">🔥 {meal.nutritionalInfo.calories} kcal</span>
                                    )}
                                    {meal.nutritionalInfo.protein > 0 && (
                                        <span className="nutrition-item">💪 {meal.nutritionalInfo.protein}g Protein</span>
                                    )}
                                    {meal.nutritionalInfo.carbs > 0 && (
                                        <span className="nutrition-item">🍞 {meal.nutritionalInfo.carbs}g Carbs</span>
                                    )}
                                    {meal.nutritionalInfo.fat > 0 && (
                                        <span className="nutrition-item">🥑 {meal.nutritionalInfo.fat}g Fett</span>
                                    )}
                                </div>
                            )}

                            {/* Zutaten */}
                            {meal.ingredients && (
                                <div className="meal-ingredients">
                                    <small><strong>Zutaten:</strong> {meal.ingredients}</small>
                                </div>
                            )}

                            {meal.categories && meal.categories.length > 0 && (
                                <div className="meal-tags">
                                    {meal.categories.map(cat => (
                                        <span key={cat} className="tag tag-category">{cat}</span>
                                    ))}
                                </div>
                            )}

                            {meal.allergens && meal.allergens.length > 0 && (
                                <div className="meal-allergens">
                                    <small>⚠️ Allergene: {meal.allergens.join(', ')}</small>
                                </div>
                            )}

                            <div className="meal-actions">
                                <button
                                    className="btn-icon btn-edit"
                                    onClick={() => editMeal(meal)}
                                    title="Bearbeiten"
                                >
                                    ✏️
                                </button>
                                <button
                                    className="btn-icon btn-delete"
                                    onClick={() => deleteMeal(meal.id)}
                                    title="Löschen"
                                >
                                    🗑️
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default MealManagement;
