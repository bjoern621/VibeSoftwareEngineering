import React, { useState, useEffect } from 'react';
import './InventoryManagement.css';
import api from '../services/api';

/**
 * Komponente für die Lagerverwaltung
 * Verwaltung von Lagerbeständen und automatische Nachbestellung
 */
function InventoryManagement() {
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  /**
   * Lädt Lagerbestände beim Start
   */
  useEffect(() => {
    fetchInventory();
  }, []);

  /**
   * Lädt alle Lagerbestände
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchInventory = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.inventory.getAll();
      setInventory(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Automatische Nachbestellung bei FOODSUPPLY auslösen
   * 
   * @async
   * @param {number} itemId - ID des Lagerartikels
   * @returns {Promise<void>}
   */
  const handleReorder = async (itemId) => {
    setError(null);
    setSuccess(null);
    try {
      await api.inventory.reorder(itemId);
      setSuccess('Nachbestellung erfolgreich ausgelöst! FOODSUPPLY wurde benachrichtigt.');
      fetchInventory();
    } catch (err) {
      setError(err.message);
    }
  };

  /**
   * Prüft ob Bestand unter Mindestmenge liegt
   * 
   * @param {number} quantity - Aktueller Bestand
   * @param {number} minStock - Mindestbestand
   * @returns {boolean} True wenn Nachbestellung nötig
   */
  const isLowStock = (quantity, minStock) => {
    return quantity < minStock;
  };

  return (
    <div className="inventory-management">
      <div className="section-header">
        <h2>📦 Lagerverwaltung</h2>
        <button className="primary" onClick={fetchInventory}>
          🔄 Aktualisieren
        </button>
      </div>

      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      {/* Info-Box */}
      <div className="info-box">
        <h3>ℹ️ Automatische Nachbestellung</h3>
        <p>
          Bei Unterschreitung von Mindestbeständen erfolgt automatisch eine Nachbestellung
          über das externe System <strong>FOODSUPPLY</strong>.
        </p>
      </div>

      {/* Lagerbestand-Übersicht */}
      {loading ? (
        <div className="loading">Lade Lagerbestand...</div>
      ) : (
        <div className="inventory-table-container">
          <table>
            <thead>
              <tr>
                <th>Zutat</th>
                <th>Bestand</th>
                <th>Einheit</th>
                <th>Mindestbestand</th>
                <th>Status</th>
                <th>Lieferant</th>
                <th>Letzte Bestellung</th>
                <th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {inventory.map(item => (
                <tr key={item.id} className={isLowStock(item.quantity, item.minStock) ? 'low-stock' : ''}>
                  <td><strong>{item.ingredientName}</strong></td>
                  <td className={isLowStock(item.quantity, item.minStock) ? 'quantity-low' : 'quantity-ok'}>
                    {item.quantity}
                  </td>
                  <td>{item.unit}</td>
                  <td>{item.minStock}</td>
                  <td>
                    {isLowStock(item.quantity, item.minStock) ? (
                      <span className="status-badge low">⚠️ Niedrig</span>
                    ) : (
                      <span className="status-badge ok">✅ Ausreichend</span>
                    )}
                  </td>
                  <td>{item.supplier}</td>
                  <td>{item.lastOrdered}</td>
                  <td className="actions">
                    <button 
                      className={isLowStock(item.quantity, item.minStock) ? 'warning' : 'secondary'}
                      onClick={() => handleReorder(item.id)}
                    >
                      🛒 Nachbestellen
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Statistiken */}
      <div className="inventory-stats">
        <div className="stat-card">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <h4>Gesamt-Artikel</h4>
            <p className="stat-value">{inventory.length}</p>
          </div>
        </div>

        <div className="stat-card warning">
          <div className="stat-icon">⚠️</div>
          <div className="stat-content">
            <h4>Niedrige Bestände</h4>
            <p className="stat-value">
              {inventory.filter(item => isLowStock(item.quantity, item.minStock)).length}
            </p>
          </div>
        </div>

        <div className="stat-card success">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <h4>Ausreichende Bestände</h4>
            <p className="stat-value">
              {inventory.filter(item => !isLowStock(item.quantity, item.minStock)).length}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default InventoryManagement;
