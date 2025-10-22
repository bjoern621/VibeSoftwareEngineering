import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './MealManagement.css';

// Meal-Bilder Mapping (passend zu den Gerichten)
const mealImages = {
  1: 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400&h=300&fit=crop', // Spaghetti Carbonara
  2: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400&h=300&fit=crop', // Gemüse-Curry
  3: 'https://images.unsplash.com/photo-1562967914-608f82629710?w=400&h=300&fit=crop', // Hähnchen-Schnitzel
  4: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=400&h=300&fit=crop', // Linsen-Dal
  5: 'https://images.unsplash.com/photo-1505576399279-565b52d4ac71?w=400&h=300&fit=crop', // Quinoa-Salat
  6: 'https://images.unsplash.com/photo-1600891964092-4316c288032e?w=400&h=300&fit=crop', // Rindergulasch
  7: 'https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=400&h=300&fit=crop', // Falafel-Wrap
  8: 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400&h=300&fit=crop'  // Lachsfilet
};

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
      // Format nutrition info as string for API
      const nutritionString = formData.nutritionInfo.calories ||
                             formData.nutritionInfo.protein ||
                             formData.nutritionInfo.carbs ||
                             formData.nutritionInfo.fat
        ? `${formData.nutritionInfo.calories || '?'} kcal, ` +
          `${formData.nutritionInfo.protein || '?'}g Protein, ` +
          `${formData.nutritionInfo.carbs || '?'}g Kohlenhydrate, ` +
          `${formData.nutritionInfo.fat || '?'}g Fett`
        : '';

      const mealData = {
        name: formData.name,
        description: formData.description,
        price: parseFloat(formData.price),
        category: formData.category,
        allergens: formData.allergens,
        nutritionInfo: nutritionString,
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
        await loadMeals();
      } catch (err) {
        setError(err.message || 'Fehler beim Löschen');
      }
    }
  };

  // Modal handlers
  const handleOpenModal = (meal = null) => {
    if (meal) {
      setEditingMeal(meal);

      // Parse nutrition info from string
      const nutritionInfo = {
        calories: '',
        protein: '',
        carbs: '',
        fat: '',
      };

      if (meal.nutritionInfo) {
        const parts = meal.nutritionInfo.split(',').map(p => p.trim());
        parts.forEach(part => {
          if (part.includes('kcal')) {
            nutritionInfo.calories = part.replace(/\D/g, '');
          } else if (part.includes('Protein')) {
            nutritionInfo.protein = part.replace(/\D/g, '');
          } else if (part.includes('Kohlenhydrate')) {
            nutritionInfo.carbs = part.replace(/\D/g, '');
          } else if (part.includes('Fett')) {
            nutritionInfo.fat = part.replace(/\D/g, '');
          }
        });
      }

      setFormData({
        name: meal.name,
        description: meal.description,
        price: meal.price.toString(),
        category: meal.category,
        allergens: meal.allergens || [],
        nutritionInfo,
      });
    } else {
      setEditingMeal(null);
      setFormData({
        name: '',
        description: '',
        price: '',
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
            {meals.filter(meal => {
              const matchesSearch = meal.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                                   meal.description?.toLowerCase().includes(searchTerm.toLowerCase());
              const matchesCategory = categoryFilter === 'all' || meal.category === categoryFilter;
              return matchesSearch && matchesCategory;
            }).length === 0 ? (
              <div className="empty-state">
                <span className="empty-icon">🍽️</span>
                <h3>Keine Gerichte gefunden</h3>
                <p>Erstellen Sie Ihr erstes Gericht oder passen Sie die Filter an.</p>
              </div>
            ) : (
              meals.filter(meal => {
                const matchesSearch = meal.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                                     meal.description?.toLowerCase().includes(searchTerm.toLowerCase());
                const matchesCategory = categoryFilter === 'all' || meal.category === categoryFilter;
                return matchesSearch && matchesCategory;
              }).map(meal => (
                <div key={meal.id} className="meal-card">
                  <div className="meal-image-container" style={{backgroundImage: `url(${mealImages[meal.id] || mealImages[1]})`}}>
                    <span className="meal-category-badge">
                      {categories.find(c => c.value === meal.category)?.label || meal.category}
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

                    {meal.nutritionInfo && (
                      <div className="meal-nutrition">
                        <span className="nutrition-icon">📊</span>
                        <span>{meal.nutritionInfo}</span>
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
              ))
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

                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="price">Preis (€) *</label>
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