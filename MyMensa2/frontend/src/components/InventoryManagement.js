import React, { useState, useEffect } from 'react';
import './InventoryManagement.css';
import api from '../services/api';

function InventoryManagement() {
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    unit: 'kg',
    stockQuantity: '',
    minStockLevel: '',
    pricePerUnit: '',
    supplierId: ''
  });

  useEffect(() => {
    fetchInventory();
  }, []);

  const fetchInventory = async () => {
    setLoading(true);
    try {
      const data = await api.inventory.getAll();
      setIngredients(data);
    } catch (err) {
      console.error('Fehler beim Laden:', err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAddIngredient = async (e) => {
    e.preventDefault();
    try {
      await api.inventory.create({
        name: formData.name,
        unit: formData.unit,
        stockQuantity: parseFloat(formData.stockQuantity),
        minStockLevel: parseFloat(formData.minStockLevel),
        pricePerUnit: parseFloat(formData.pricePerUnit),
        supplierId: formData.supplierId
      });
      setFormData({
        name: '',
        unit: 'kg',
        stockQuantity: '',
        minStockLevel: '',
        pricePerUnit: '',
        supplierId: ''
      });
      setShowAddForm(false);
      fetchInventory();
    } catch (err) {
      alert('Fehler beim Hinzufügen: ' + err.message);
    }
  };

  const handleReorderAll = async () => {
    if (!window.confirm('Alle Zutaten mit niedrigem Bestand über FOODSUPPLY nachbestellen?')) {
      return;
    }
    try {
      const result = await api.inventory.reorderAll();
      alert(`✅ Nachbestellung erfolgreich!\n${result.reorderedItems.length} Artikel wurden über FOODSUPPLY nachbestellt.`);
      fetchInventory();
    } catch (err) {
      alert('❌ Fehler bei der Nachbestellung: ' + err.message);
    }
  };

  const isLowStock = (stock, minStock) => {
    return stock < minStock;
  };

  const stats = {
    total: ingredients.length,
    lowStock: ingredients.filter(i => isLowStock(i.stockQuantity, i.minStockLevel)).length,
    adequate: ingredients.filter(i => !isLowStock(i.stockQuantity, i.minStockLevel)).length
  };

  return (
    <div className="inventory-management">
      <div className="section-header">
        <h2>🏪 Lagerverwaltung</h2>
        <div className="header-actions">
          <p className="subtitle">
            Automatische Nachbestellung via FOODSUPPLY bei Unterschreitung der Mindestbestände
          </p>
          <button className="primary" onClick={() => setShowAddForm(true)}>
            ➕ Neue Zutat
          </button>
        </div>
      </div>

      <div className="inventory-stats">
        <div className="stat-card">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <h4>Gesamt-Artikel</h4>
            <p className="stat-value">{stats.total}</p>
          </div>
        </div>
        <div className="stat-card warning">
          <div className="stat-icon">⚠️</div>
          <div className="stat-content">
            <h4>Niedrige Bestände</h4>
            <p className="stat-value">{stats.lowStock}</p>
          </div>
        </div>
        <div className="stat-card success">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <h4>Ausreichende Bestände</h4>
            <p className="stat-value">{stats.adequate}</p>
          </div>
        </div>
        <div className="stat-card action-card">
          <div className="stat-icon">🔄</div>
          <div className="stat-content">
            <h4>FOODSUPPLY Nachbestellung</h4>
            <button 
              className="reorder-btn" 
              onClick={handleReorderAll} 
              disabled={stats.lowStock === 0}
            >
              {stats.lowStock > 0 
                ? `${stats.lowStock} Artikel nachbestellen` 
                : 'Keine Nachbestellung nötig'}
            </button>
          </div>
        </div>
      </div>

      {showAddForm && (
        <div className="modal-overlay" onClick={() => setShowAddForm(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>➕ Neue Zutat hinzufügen</h3>
            <form onSubmit={handleAddIngredient}>
              <div className="form-row">
                <div className="form-group">
                  <label>Zutat-Name*</label>
                  <input 
                    type="text" 
                    value={formData.name} 
                    onChange={(e) => setFormData({...formData, name: e.target.value})} 
                    required 
                  />
                </div>
                <div className="form-group">
                  <label>Einheit*</label>
                  <select 
                    value={formData.unit} 
                    onChange={(e) => setFormData({...formData, unit: e.target.value})} 
                    required
                  >
                    <option value="kg">kg</option>
                    <option value="Liter">Liter</option>
                    <option value="Stück">Stück</option>
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Anfangsbestand*</label>
                  <input 
                    type="number" 
                    step="0.01" 
                    min="0" 
                    value={formData.stockQuantity} 
                    onChange={(e) => setFormData({...formData, stockQuantity: e.target.value})} 
                    required 
                  />
                </div>
                <div className="form-group">
                  <label>Mindestbestand*</label>
                  <input 
                    type="number" 
                    step="0.01" 
                    min="0" 
                    value={formData.minStockLevel} 
                    onChange={(e) => setFormData({...formData, minStockLevel: e.target.value})} 
                    required 
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Preis pro Einheit (€)*</label>
                  <input 
                    type="number" 
                    step="0.01" 
                    min="0" 
                    value={formData.pricePerUnit} 
                    onChange={(e) => setFormData({...formData, pricePerUnit: e.target.value})} 
                    required 
                  />
                </div>
                <div className="form-group">
                  <label>Lieferanten-ID*</label>
                  <input 
                    type="text" 
                    placeholder="FOODSUPPLY-VENDOR-123" 
                    value={formData.supplierId} 
                    onChange={(e) => setFormData({...formData, supplierId: e.target.value})} 
                    required 
                  />
                </div>
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

      {loading ? (
        <div className="loading">Lade Lagerbestand...</div>
      ) : ingredients.length === 0 ? (
        <div className="no-data"><p>Keine Zutaten im Lager.</p></div>
      ) : (
        <div className="inventory-table-container">
          <table>
            <thead>
              <tr>
                <th>Zutat</th>
                <th>Aktueller Bestand</th>
                <th>Einheit</th>
                <th>Mindestbestand</th>
                <th>Status</th>
                <th>Preis/Einheit</th>
                <th>Lieferant (FOODSUPPLY)</th>
              </tr>
            </thead>
            <tbody>
              {ingredients.map(item => (
                <tr key={item.id} className={isLowStock(item.stockQuantity, item.minStockLevel) ? 'low-stock' : ''}>
                  <td><strong>{item.name}</strong></td>
                  <td className={isLowStock(item.stockQuantity, item.minStockLevel) ? 'quantity-low' : 'quantity-ok'}>
                    <strong>{item.stockQuantity}</strong>
                  </td>
                  <td>{item.unit}</td>
                  <td>{item.minStockLevel}</td>
                  <td>
                    {isLowStock(item.stockQuantity, item.minStockLevel) ? (
                      <span className="status-badge low">⚠️ Nachbestellen</span>
                    ) : (
                      <span className="status-badge ok">✅ Ausreichend</span>
                    )}
                  </td>
                  <td>{item.pricePerUnit.toFixed(2)} €</td>
                  <td className="supplier-cell">{item.supplierId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default InventoryManagement;
