import React, { useState, useEffect } from 'react';
import './AdminOrderManagement.css';
import api from '../services/api';
import { formatPriceSimple } from '../utils/priceUtils';

/**
 * Hilfsfunktion: Berechnet Datum in X Tagen
 */
const getDateInDays = (daysFromNow) => {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
};

/**
 * Admin-Komponente für die Bestellverwaltung
 * Zeigt alle Bestellungen, QR-Code-Validierung und Abholstatus
 */
function AdminOrderManagement() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  // Standardwerte: Heute bis +14 Tage (2 Wochen)
  const [filterStartDate, setFilterStartDate] = useState(getDateInDays(0));
  const [filterEndDate, setFilterEndDate] = useState(getDateInDays(14));
  const [filterStatus, setFilterStatus] = useState('all');
  const [selectedOrder, setSelectedOrder] = useState(null); // Für QR-Modal

  /**
   * Lädt Bestellungen bei Änderung der Filter
   */
  useEffect(() => {
    fetchOrders();
    // eslint-disable-next-line
  }, [filterStartDate, filterEndDate, filterStatus]);

  /**
   * Lädt alle Bestellungen mit Client-seitigem Filtern
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchOrders = async () => {
    setLoading(true);
    try {
      const data = await api.orders.getAll();
      
      // Client-seitiges Filtern für Tabelle UND Statistiken
      let filtered = data;
      
      // Filtere nach Abholdatum (pickupDate), nicht Bestelldatum!
      if (filterStartDate && filterEndDate) {
        filtered = filtered.filter(order => {
          const pickupDate = order.pickupDate; // Format: "YYYY-MM-DD"
          return pickupDate >= filterStartDate && pickupDate <= filterEndDate;
        });
      } else if (filterStartDate) {
        filtered = filtered.filter(order => order.pickupDate >= filterStartDate);
      } else if (filterEndDate) {
        filtered = filtered.filter(order => order.pickupDate <= filterEndDate);
      }
      
      if (filterStatus === 'paid') {
        filtered = filtered.filter(order => order.paid && !order.collected);
      } else if (filterStatus === 'collected') {
        filtered = filtered.filter(order => order.collected);
      } else if (filterStatus === 'unpaid') {
        filtered = filtered.filter(order => !order.paid);
      }
      
      setOrders(filtered);
    } catch (err) {
      console.error('Fehler beim Laden der Bestellungen:', err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * QR-Code validieren (für einzelne Bestellung aus der Tabelle)
   * 
   * @async
   * @param {object} order - Die zu validierende Bestellung
   * @returns {Promise<void>}
   */
  const handleValidateOrder = async (order) => {
    if (!order.paid) {
      alert('Bestellung muss erst bezahlt werden!');
      return;
    }

    // Generiere QR-Code (simuliert - im echten System käme der vom Backend)
    const qrCode = `ORDER-${order.id}`;

    try {
      const result = await api.orders.validateQRCode(qrCode);
      
      if (result.alreadyCollected) {
        const collectedTime = new Date(result.collectedAt).toLocaleString('de-DE');
        alert(`⚠️ Diese Bestellung wurde bereits am ${collectedTime} abgeholt!`);
      } else {
        const mealName = result.meal?.name || 'Unbekannt';
        alert(`✅ QR-Code erfolgreich validiert!\nBestellung #${result.orderId} - ${mealName} wurde ausgegeben.`);
      }
      
      fetchOrders(); // Tabelle aktualisieren
    } catch (err) {
      alert('❌ Validierung fehlgeschlagen: ' + err.message);
    }
  };

  const getOrderStats = () => {
    // Verwende die GEFILTERTEN orders, nicht allOrders!
    const total = orders.length;
    const paid = orders.filter(o => o.paid).length;
    const collected = orders.filter(o => o.collected).length;

    return { total, paid, collected };
  };

  const stats = getOrderStats();

  return (
    <div className="admin-order-management">
      <div className="section-header">
        <h2>📋 Bestellverwaltung & QR-Validierung</h2>
      </div>

      {/* Filter */}
      <div className="filter-section">
        <div className="form-group">
          <label htmlFor="filter-start-date">Abholdatum von:</label>
          <input
            type="date"
            id="filter-start-date"
            value={filterStartDate}
            onChange={(e) => setFilterStartDate(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="filter-end-date">Abholdatum bis:</label>
          <input
            type="date"
            id="filter-end-date"
            value={filterEndDate}
            onChange={(e) => setFilterEndDate(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="filter-status">Status:</label>
          <select
            id="filter-status"
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <option value="all">Alle</option>
            <option value="unpaid">Ausstehend</option>
            <option value="paid">Bezahlt</option>
            <option value="collected">Abgeholt</option>
          </select>
        </div>
      </div>

      {/* Statistiken */}
      <div className="order-stats">
        <div className="stat-card">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <h4>Gesamt-Bestellungen</h4>
            <p className="stat-value">{stats.total}</p>
          </div>
        </div>

        <div className="stat-card success">
          <div className="stat-icon">💰</div>
          <div className="stat-content">
            <h4>Bezahlt</h4>
            <p className="stat-value">{stats.paid}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <h4>Abgeholt</h4>
            <p className="stat-value">{stats.collected}</p>
          </div>
        </div>
      </div>

      {/* Bestellliste */}
      {loading ? (
        <div className="loading">Lade Bestellungen...</div>
      ) : orders.length === 0 ? (
        <div className="no-data">
          <p>Keine Bestellungen für die gewählten Filter vorhanden.</p>
        </div>
      ) : (
        <div className="orders-table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Gericht</th>
                <th>Preis</th>
                <th>Bestelldatum</th>
                <th>Abholdatum</th>
                <th>Bezahlt</th>
                <th>Abgeholt</th>
                <th>QR-Code</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id} className={order.collected ? 'completed' : ''}>
                  <td>{order.id}</td>
                  <td><strong>{order.meal?.name || 'N/A'}</strong></td>
                  <td>
                    {order.meal ? formatPriceSimple(order.meal.price) : '0.00'} €
                  </td>
                  <td>{new Date(order.orderDate).toLocaleString('de-DE')}</td>
                  <td>{order.pickupDate}</td>
                  <td>
                    {order.paid ? (
                      <span className="status-badge paid">✅ Ja</span>
                    ) : (
                      <span className="status-badge unpaid">❌ Nein</span>
                    )}
                  </td>
                  <td>
                    {order.collected ? (
                      <span className="status-badge collected">✅ Ja</span>
                    ) : (
                      <span className="status-badge not-collected">❌ Nein</span>
                    )}
                  </td>
                  <td>
                    {order.paid ? (
                      <button 
                        className="qr-button"
                        onClick={() => setSelectedOrder(order)}
                        title="QR-Code anzeigen und validieren"
                      >
                        📱 QR-Code
                      </button>
                    ) : (
                      <span className="qr-disabled">Nicht bezahlt</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* QR-Code-Modal */}
      {selectedOrder && (
        <div className="modal-overlay" onClick={() => setSelectedOrder(null)}>
          <div className="qr-modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setSelectedOrder(null)}>
              ✕
            </button>
            
            <h3>📱 QR-Code Validierung</h3>
            
            <div className="qr-modal-body">
              <div className="order-details">
                <h4>Bestellung #{selectedOrder.id}</h4>
                <p><strong>Gericht:</strong> {selectedOrder.meal?.name}</p>
                <p><strong>Preis:</strong> {formatPriceSimple(selectedOrder.meal?.price)} €</p>
                <p><strong>Abholdatum:</strong> {selectedOrder.pickupDate}</p>
              </div>

              <div className="qr-code-display">
                <div className="qr-code-box">
                  <p className="qr-label">QR-Code:</p>
                  <code className="qr-code-value">ORDER-{selectedOrder.id}</code>
                </div>
              </div>

              <div className="validation-status">
                {selectedOrder.collected ? (
                  <div className="status-alert already-collected">
                    <span className="status-icon">⚠️</span>
                    <div>
                      <strong>Bereits abgeholt</strong>
                      <p>Diese Bestellung wurde am {new Date(selectedOrder.collectedAt).toLocaleString('de-DE')} ausgegeben.</p>
                    </div>
                  </div>
                ) : (
                  <button 
                    className="validate-order-btn"
                    onClick={() => {
                      handleValidateOrder(selectedOrder);
                      setSelectedOrder(null);
                    }}
                  >
                    ✅ Essen ausgeben & validieren
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default AdminOrderManagement;
