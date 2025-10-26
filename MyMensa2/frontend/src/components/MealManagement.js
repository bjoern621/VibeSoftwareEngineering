import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { getMealImage } from '../utils/imageUtils';
import './MealManagement.css';

const MealManagement = () => {
  const [meals, setMeals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingMeal, setEditingMeal] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    cost: '',
    ingredients: '',
    category: 'VEGETARIAN',
    allergens: [],
    nutritionInfo: {
      calories: '',
      protein: '',
      carbs: '',
      fat: '',
    },
  });

  const categories = [
    { value: 'VEGETARIAN', label: '🥗 Vegetarisch' },
    { value: 'VEGAN', label: '🌱 Vegan' },
    { value: 'MEAT', label: '🍖 Fleisch' },
    { value: 'FISH', label: '🐟 Fisch' },
    { value: 'HALAL', label: '☪️ Halal' },
    { value: 'GLUTEN_FREE', label: '🌾 Glutenfrei' },
  ];

  const commonAllergens = [
    'Gluten',
    'Laktose',
    'Eier',
    'Nüsse',
    'Erdnüsse',
    'Soja',
    'Fisch',
    'Schalentiere',
    'Sellerie',
    'Senf',
    'Sesam',
    'Lupinen',
  ];

  // Load meals
  useEffect(() => {
    loadMeals();
  }, []);

  const loadMeals = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.meals.getAll();
      setMeals(data);
    } catch (err) {
      setError(err.message || 'Fehler beim Laden der Gerichte');
    } finally {
      setLoading(false);
    }
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Format nutritionalInfo as object for API
      const nutritionalInfo = {
        calories: formData.nutritionInfo.calories ? parseInt(formData.nutritionInfo.calories) : null,
        protein: formData.nutritionInfo.protein ? parseFloat(formData.nutritionInfo.protein) : null,
        carbs: formData.nutritionInfo.carbs ? parseFloat(formData.nutritionInfo.carbs) : null,
        fat: formData.nutritionInfo.fat ? parseFloat(formData.nutritionInfo.fat) : null,
      };

      const mealData = {
        name: formData.name,
        description: formData.description,
        price: parseFloat(formData.price),
        cost: parseFloat(formData.cost) || 0,
        ingredients: formData.ingredients,
        nutritionalInfo: nutritionalInfo,
        categories: [formData.category], // Als Array
        allergens: formData.allergens,
      };

      if (editingMeal) {
        await api.meals.update(editingMeal.id, mealData);
      } else {
        await api.meals.create(mealData);
      }

      await loadMeals();
      handleCloseModal();
    } catch (err) {
      setError(err.message || 'Fehler beim Speichern');
    }
  };

  // Handle delete
  const handleDelete = async (id) => {
    if (window.confirm('Möchten Sie dieses Gericht wirklich löschen?')) {
      try {
        await api.meals.delete(id);

        // Nach dem Soft-Delete: Entferne das Gericht aus zukünftigen Speiseplänen
        try {
          await removeMealFromMealPlans(id);
        } catch (planErr) {
          // Logge den Fehler, aber fahre fort - das Backend hat das Gericht bereits gelöscht
          console.error('Fehler beim Entfernen aus dem Speiseplan:', planErr);
        }

        await loadMeals();
      } catch (err) {
        setError(err.message || 'Fehler beim Löschen');
      }
    }
  };

  // Entfernt ein Gericht aus Speiseplan-Einträgen in einem Zeitbereich
  const removeMealFromMealPlans = async (mealId) => {
    // Annahme: Entferne das Gericht aus den Speiseplänen der letzten 7 Tage bis in 30 Tage
    const today = new Date();
    const start = new Date(today);
    start.setDate(start.getDate() - 7);
    const end = new Date(today);
    end.setDate(end.getDate() + 30);

    const formatDate = (d) => d.toISOString().split('T')[0];
    const startStr = formatDate(start);
    const endStr = formatDate(end);

    // Hole alle Speiseplan-Einträge im Zeitraum
    const plans = await api.mealPlans.getByDateRange(startStr, endStr);
    if (!plans || plans.length === 0) return;

    const deletions = [];

    for (const day of plans) {
      const date = day.date;
      const mealsOnDate = Array.isArray(day.meals) ? day.meals : [];

      for (const item of mealsOnDate) {
        const candidateId = item.meal?.id ?? item.mealId ?? item.id;
        if (candidateId && parseInt(candidateId, 10) === parseInt(mealId, 10)) {
          // Lösche den Eintrag für dieses Datum
          deletions.push(api.mealPlans.delete(mealId, date).catch(e => {
            console.warn(`Konnte Speiseplan-Eintrag für Meal ${mealId} am ${date} nicht löschen:`, e);
          }));
        }
      }
    }

    // Warte auf alle Löschversuche
    await Promise.all(deletions);
  };

  // Modal handlers
  const handleOpenModal = (meal = null) => {
    if (meal) {
      setEditingMeal(meal);

      // Parse nutrition info from object (Backend sends NutritionalInfo object)
      const nutritionInfo = {
        calories: meal.nutritionalInfo?.calories?.toString() || '',
        protein: meal.nutritionalInfo?.protein?.toString() || '',
        carbs: meal.nutritionalInfo?.carbs?.toString() || '',
        fat: meal.nutritionalInfo?.fat?.toString() || '',
      };

      // Categories kommt als Array vom Backend, nehme das erste Element
      const category = meal.categories && meal.categories.length > 0
        ? meal.categories[0]
        : 'VEGETARIAN';

      setFormData({
        name: meal.name,
        description: meal.description,
        price: meal.price.toString(),
        cost: meal.cost ? meal.cost.toString() : '',
        ingredients: meal.ingredients || '',
        category: category,
        allergens: meal.allergens || [],
        nutritionInfo,
      });
    } else {
      setEditingMeal(null);
      setFormData({
        name: '',
        description: '',
        price: '',
        cost: '',
        ingredients: '',
        category: 'VEGETARIAN',
        allergens: [],
        nutritionInfo: {
          calories: '',
          protein: '',
          carbs: '',
          fat: '',
        },
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingMeal(null);
  };

  // Handle allergen toggle
  const toggleAllergen = (allergen) => {
    setFormData(prev => ({
      ...prev,
      allergens: prev.allergens.includes(allergen)
        ? prev.allergens.filter(a => a !== allergen)
        : [...prev.allergens, allergen]
    }));
  };

  // Handle nutrition info change
  const handleNutritionChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      nutritionInfo: {
        ...prev.nutritionInfo,
        [field]: value
      }
    }));
  };

  // Filtered Meals
  const filteredMeals = meals.filter(meal => {
    const matchesSearch = meal.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         meal.description?.toLowerCase().includes(searchTerm.toLowerCase());
    // categories ist jetzt ein Array
    const matchesCategory = categoryFilter === 'all' ||
                           (meal.categories && meal.categories.includes(categoryFilter));
    return matchesSearch && matchesCategory;
  });

  if (loading) {
    return (
      <div className="meal-management">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Gerichte werden geladen...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="meal-management">
      <div className="container">
        {/* Header Section */}
        <div className="page-header">
          <div className="header-left">
            <h2>Gerichteverwaltung</h2>
            <p className="subtitle">{meals.length} Gerichte verfügbar</p>
          </div>
          <button className="btn btn-primary" onClick={() => handleOpenModal()}>
            ➕ Neues Gericht
          </button>
        </div>

        {/* Error Message */}
        {error && (
          <div className="alert alert-error">
            <span className="alert-icon">⚠️</span>
            <span>{error}</span>
            <button className="alert-close" onClick={() => setError(null)}>✕</button>
          </div>
        )}

        {/* Filters */}
        <div className="filters-section">
          <div className="search-box">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              placeholder="Gerichte durchsuchen..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="category-filters">
            <button
              className={`filter-chip ${categoryFilter === 'all' ? 'active' : ''}`}
              onClick={() => setCategoryFilter('all')}
            >
              Alle
            </button>
            {categories.map(cat => (
              <button
                key={cat.value}
                className={`filter-chip ${categoryFilter === cat.value ? 'active' : ''}`}
                onClick={() => setCategoryFilter(cat.value)}
              >
                {cat.label}
              </button>
            ))}
          </div>
        </div>

        {/* Meals Grid */}
        {loading ? (
          <div className="loading-spinner">
            <div className="spinner"></div>
            <p>Gerichte werden geladen...</p>
          </div>
        ) : (
          <div className="meals-grid">
            {filteredMeals.length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🍽️</span>
                <h3>Keine Gerichte gefunden</h3>
                <p>Erstellen Sie Ihr erstes Gericht oder passen Sie die Filter an.</p>
              </div>
            ) : (
              filteredMeals.map(meal => {
                // Kategorie aus Array holen
                const mealCategory = meal.categories && meal.categories.length > 0
                  ? meal.categories[0]
                  : 'VEGETARIAN';

                // Nährwertinfo formatieren
                const nutritionText = meal.nutritionalInfo
                  ? `${meal.nutritionalInfo.calories || '?'} kcal, ${meal.nutritionalInfo.protein || '?'}g Protein, ${meal.nutritionalInfo.carbs || '?'}g Kohlenhydrate, ${meal.nutritionalInfo.fat || '?'}g Fett`
                  : null;

                return (
                  <div key={meal.id} className="meal-card">
                    <div className="meal-image-container" style={{backgroundImage: `url(${getMealImage(meal.name)})`}}>
                      <span className="meal-category-badge">
                        {categories.find(c => c.value === mealCategory)?.label || mealCategory}
                      </span>
                    </div>

                    <div className="meal-card-body">
                      <h3 className="meal-name">{meal.name}</h3>
                      <p className="meal-description">{meal.description}</p>

                      {meal.allergens && meal.allergens.length > 0 && (
                        <div className="meal-allergens">
                          <span className="allergen-label">⚠️ Allergene:</span>
                          <span className="allergen-list">{meal.allergens.join(', ')}</span>
                        </div>
                      )}

                      {nutritionText && (
                        <div className="meal-nutrition">
                          <span className="nutrition-icon">📊</span>
                          <span>{nutritionText}</span>
                        </div>
                      )}
                    </div>

                    <div className="meal-card-footer">
                      <div className="meal-price">
                        <span className="price-amount">€ {meal.price.toFixed(2)}</span>
                      </div>
                      <div className="meal-actions">
                        <button
                          className="btn-icon btn-edit"
                          onClick={() => handleOpenModal(meal)}
                          title="Bearbeiten"
                        >
                          ✏️
                        </button>
                        <button
                          className="btn-icon btn-delete"
                          onClick={() => handleDelete(meal.id)}
                          title="Löschen"
                        >
                          🗑️
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        )}

        {/* Modal */}
        {showModal && (
          <div className="modal-overlay" onClick={handleCloseModal}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h3>{editingMeal ? '✏️ Gericht bearbeiten' : '➕ Neues Gericht'}</h3>
                <button className="modal-close" onClick={handleCloseModal}>✕</button>
              </div>

              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  <div className="form-group">
                    <label htmlFor="name">Name *</label>
                    <input
                      type="text"
                      id="name"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      required
                      placeholder="z.B. Spaghetti Bolognese"
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="description">Beschreibung</label>
                    <textarea
                      id="description"
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      rows="3"
                      placeholder="Kurze Beschreibung des Gerichts..."
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="ingredients">Zutaten *</label>
                    <textarea
                      id="ingredients"
                      value={formData.ingredients}
                      onChange={(e) => setFormData({ ...formData, ingredients: e.target.value })}
                      rows="2"
                      required
                      placeholder="z.B. Nudeln, Hackfleisch, Tomatensauce, Zwiebeln"
                    />
                    <small className="form-hint">Komma-getrennte Liste der Zutaten</small>
                  </div>

                  <div className="form-row form-row-three">
                    <div className="form-group">
                      <label htmlFor="price">Verkaufspreis (€) *</label>
                      <input
                        type="number"
                        id="price"
                        value={formData.price}
                        onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                        required
                        step="0.01"
                        min="0"
                        placeholder="5.50"
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="cost">Kosten (€) *</label>
                      <input
                        type="number"
                        id="cost"
                        value={formData.cost}
                        onChange={(e) => setFormData({ ...formData, cost: e.target.value })}
                        required
                        step="0.01"
                        min="0"
                        placeholder="3.20"
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="category">Kategorie *</label>
                      <select
                        id="category"
                        value={formData.category}
                        onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                        required
                      >
                        {categories.map(cat => (
                          <option key={cat.value} value={cat.value}>
                            {cat.label}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Allergene</label>
                    <div className="allergens-grid">
                      {commonAllergens.map(allergen => (
                        <button
                          key={allergen}
                          type="button"
                          className={`allergen-chip ${formData.allergens.includes(allergen) ? 'selected' : ''}`}
                          onClick={() => toggleAllergen(allergen)}
                        >
                          {formData.allergens.includes(allergen) && '✓ '}
                          {allergen}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Nährwertinformationen (pro Portion)</label>
                    <div className="nutrition-grid">
                      <div className="nutrition-field">
                        <label htmlFor="calories">Kalorien</label>
                        <div className="input-with-unit">
                          <input
                            type="number"
                            id="calories"
                            value={formData.nutritionInfo.calories}
                            onChange={(e) => handleNutritionChange('calories', e.target.value)}
                            placeholder="450"
                            min="0"
                          />
                          <span className="unit">kcal</span>
                        </div>
                      </div>

                      <div className="nutrition-field">
                        <label htmlFor="protein">Protein</label>
                        <div className="input-with-unit">
                          <input
                            type="number"
                            id="protein"
                            value={formData.nutritionInfo.protein}
                            onChange={(e) => handleNutritionChange('protein', e.target.value)}
                            placeholder="20"
                            min="0"
                          />
                          <span className="unit">g</span>
                        </div>
                      </div>

                      <div className="nutrition-field">
                        <label htmlFor="carbs">Kohlenhydrate</label>
                        <div className="input-with-unit">
                          <input
                            type="number"
                            id="carbs"
                            value={formData.nutritionInfo.carbs}
                            onChange={(e) => handleNutritionChange('carbs', e.target.value)}
                            placeholder="50"
                            min="0"
                          />
                          <span className="unit">g</span>
                        </div>
                      </div>

                      <div className="nutrition-field">
                        <label htmlFor="fat">Fett</label>
                        <div className="input-with-unit">
                          <input
                            type="number"
                            id="fat"
                            value={formData.nutritionInfo.fat}
                            onChange={(e) => handleNutritionChange('fat', e.target.value)}
                            placeholder="15"
                            min="0"
                          />
                          <span className="unit">g</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={handleCloseModal}>
                    Abbrechen
                  </button>
                  <button type="submit" className="btn btn-primary">
                    {editingMeal ? 'Speichern' : 'Erstellen'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}; 

export default MealManagement;