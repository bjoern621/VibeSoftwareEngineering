import React, { useState, useEffect } from 'react';
import './AdminOrderManagement.css';
import api from '../services/api';
import { formatDate } from '../utils/dateUtils';
import { formatPriceSimple } from '../utils/priceUtils';

/**
 * Admin-Komponente für die Bestellverwaltung
 * Zeigt alle Bestellungen, QR-Code-Validierung und Abholstatus
 */
function AdminOrderManagement() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [filterDate, setFilterDate] = useState(formatDate(new Date()));
  const [filterStatus, setFilterStatus] = useState('all');

  /**
   * Lädt Bestellungen bei Änderung der Filter
   */
  useEffect(() => {
    fetchOrders();
    // eslint-disable-next-line
  }, [filterDate, filterStatus]);

  /**
   * Lädt alle Bestellungen mit Client-seitigem Filtern
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchOrders = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.orders.getAll();
      
      // Client-seitiges Filtern
      let filtered = data;
      
      if (filterDate) {
        filtered = filtered.filter(order => order.orderDate === filterDate);
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
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Bestellung als bezahlt markieren
   * 
   * @async
   * @param {number} orderId - ID der Bestellung
   * @returns {Promise<void>}
   */
  const handleMarkAsPaid = async (orderId) => {
    setError(null);
    try {
      await api.orders.markAsPaid(orderId);

      setSuccess('Zahlung erfolgreich markiert!');
      fetchOrders();
    } catch (err) {
      setError(err.message);
    }
  };

  /**
   * Bestellung als abgeholt markieren
   * 
   * @async
   * @param {number} orderId - ID der Bestellung
   * @returns {Promise<void>}
   */
  const handleMarkAsCollected = async (orderId) => {
    setError(null);
    try {
      await api.orders.markAsCollected(orderId);
      setSuccess('Bestellung als abgeholt markiert!');
      fetchOrders();
    } catch (err) {
      setError(err.message);
    }
  };

  /**
   * Bestellung löschen
   * 
   * @async
   * @param {number} orderId - ID der Bestellung
   * @returns {Promise<void>}
   */
  const handleDeleteOrder = async (orderId) => {
    if (!window.confirm('Möchten Sie diese Bestellung wirklich löschen?')) {
      return;
    }

    setError(null);
    try {
      await api.orders.delete(orderId);
      setSuccess('Bestellung erfolgreich gelöscht!');
      fetchOrders();
    } catch (err) {
      setError(err.message);
    }
  };

  const getOrderStats = () => {
    const total = orders.length;
    const paid = orders.filter(o => o.paid).length;
    const collected = orders.filter(o => o.collected).length;
    const pending = orders.filter(o => !o.paid).length;

    return { total, paid, collected, pending };
  };

  const stats = getOrderStats();

  return (
    <div className="admin-order-management">
      <div className="section-header">
        <h2>🛒 Bestellverwaltung</h2>
        <button className="primary" onClick={fetchOrders}>
          🔄 Aktualisieren
        </button>
      </div>

      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      {/* Filter */}
      <div className="filter-section">
        <div className="form-group">
          <label htmlFor="filter-date">Datum:</label>
          <input
            type="date"
            id="filter-date"
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
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
            <option value="pending">Ausstehend</option>
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

        <div className="stat-card warning">
          <div className="stat-icon">⏳</div>
          <div className="stat-content">
            <h4>Ausstehend</h4>
            <p className="stat-value">{stats.pending}</p>
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
                <th>Menge</th>
                <th>Gesamtpreis</th>
                <th>Bestelldatum</th>
                <th>QR-Code</th>
                <th>Bezahlt</th>
                <th>Abgeholt</th>
                <th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id} className={order.collected ? 'completed' : ''}>
                  <td>{order.id}</td>
                  <td><strong>{order.meal?.name || 'N/A'}</strong></td>
                  <td>{order.quantity}</td>
                  <td>
                    {order.meal ? formatPriceSimple(order.meal.price * order.quantity) : '0.00'} €
                  </td>
                  <td>{order.orderDate}</td>
                  <td className="qr-code">
                    <code>{order.qrCode}</code>
                  </td>
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
                  <td className="actions">
                    {!order.paid && (
                      <button 
                        className="success" 
                        onClick={() => handleMarkAsPaid(order.id)}
                      >
                        💰 Bezahlt
                      </button>
                    )}
                    {order.paid && !order.collected && (
                      <button 
                        className="primary" 
                        onClick={() => handleMarkAsCollected(order.id)}
                      >
                        ✅ Abgeholt
                      </button>
                    )}
                    <button 
                      className="danger" 
                      onClick={() => handleDeleteOrder(order.id)}
                    >
                      🗑️
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Info-Box */}
      <div className="info-box">
        <h3>ℹ️ QR-Code-Validierung</h3>
        <p>
          Nach erfolgreicher Bezahlung über <strong>EASYPAY</strong> erhalten Kunden einen
          QR-Code zur Abholung. Mensa-Mitarbeitende validieren den QR-Code über eine
          separate Mobile App bei der Essensausgabe.
        </p>
      </div>
    </div>
  );
}

export default AdminOrderManagement;
