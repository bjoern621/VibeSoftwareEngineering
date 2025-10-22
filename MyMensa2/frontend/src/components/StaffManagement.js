import React, { useState, useEffect } from 'react';
import './StaffManagement.css';
import api from '../services/api';

/**
 * Komponente für die Personalverwaltung
 * Verwaltung von Arbeitszeiten und Synchronisation mit STAFFMAN
 */
function StaffManagement() {
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  /**
   * Lädt Personal beim Start
   */
  useEffect(() => {
    fetchStaff();
  }, []);

  /**
   * Lädt alle Mitarbeiter
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchStaff = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.staff.getAll();
      setStaff(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Synchronisation mit STAFFMAN-System auslösen
   * 
   * @async
   * @returns {Promise<void>}
   */
  const handleSyncStaffman = async () => {
    setError(null);
    setSuccess(null);
    try {
      await api.staff.sync();
      setSuccess('Synchronisation mit STAFFMAN erfolgreich!');
      fetchStaff();
    } catch (err) {
      setError(err.message);
    }
  };

  /**
   * Berechnet Gesamtstunden aller Mitarbeiter
   * 
   * @returns {number} Summe aller Arbeitsstunden
   */
  const getTotalHours = () => {
    return staff.reduce((total, member) => total + member.hoursWorked, 0);
  };

  /**
   * Zählt verfügbare Mitarbeiter
   * 
   * @returns {number} Anzahl verfügbarer Mitarbeiter
   */
  const getAvailableStaff = () => {
    return staff.filter(member => member.availability === 'Verfügbar').length;
  };

  return (
    <div className="staff-management">
      <div className="section-header">
        <h2>👥 Personalverwaltung</h2>
        <button className="primary" onClick={handleSyncStaffman}>
          🔄 Mit STAFFMAN synchronisieren
        </button>
      </div>

      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      {/* Info-Box */}
      <div className="info-box">
        <h3>ℹ️ Integration mit STAFFMAN</h3>
        <p>
          Das System synchronisiert Arbeitszeiten und Verfügbarkeiten automatisch mit dem
          externen System <strong>STAFFMAN</strong>. Die Einsatzplanung erfolgt basierend
          auf erwarteter Besucherzahl und geplanten Gerichten.
        </p>
      </div>

      {/* Personal-Statistiken */}
      <div className="staff-stats">
        <div className="stat-card">
          <div className="stat-icon">👥</div>
          <div className="stat-content">
            <h4>Gesamt-Personal</h4>
            <p className="stat-value">{staff.length}</p>
          </div>
        </div>

        <div className="stat-card success">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <h4>Verfügbar</h4>
            <p className="stat-value">{getAvailableStaff()}</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">⏰</div>
          <div className="stat-content">
            <h4>Gesamt-Stunden (Woche)</h4>
            <p className="stat-value">{getTotalHours()}h</p>
          </div>
        </div>
      </div>

      {/* Personal-Liste */}
      {loading ? (
        <div className="loading">Lade Personal-Daten...</div>
      ) : (
        <div className="staff-table-container">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Rolle</th>
                <th>Arbeitsstunden (Woche)</th>
                <th>Verfügbarkeit</th>
                <th>Letzte Synchronisation</th>
                <th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {staff.map(member => (
                <tr key={member.id}>
                  <td><strong>{member.name}</strong></td>
                  <td>
                    <span className={`role-badge ${member.role.toLowerCase()}`}>
                      {member.role}
                    </span>
                  </td>
                  <td className="hours-worked">{member.hoursWorked}h</td>
                  <td>
                    <span className={`availability-badge ${member.availability.toLowerCase()}`}>
                      {member.availability}
                    </span>
                  </td>
                  <td>{member.lastSync}</td>
                  <td className="actions">
                    <button className="secondary">📅 Einsatzplan</button>
                    <button className="primary">✏️ Bearbeiten</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Einsatzplanung */}
      <div className="schedule-section">
        <h3>📅 Einsatzplanung</h3>
        <p>
          Die Einsatzplanung berücksichtigt die erwartete Besucherzahl und die geplanten
          Gerichte, um eine optimale Personalbesetzung sicherzustellen.
        </p>
        <button className="warning">Einsatzplan generieren (Coming Soon)</button>
      </div>
    </div>
  );
}

export default StaffManagement;
