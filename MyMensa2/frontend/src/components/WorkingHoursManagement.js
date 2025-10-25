import React, { useState, useEffect } from 'react';
import './WorkingHoursManagement.css';
import api from '../services/api';

/**
 * Arbeitszeiten-Erfassung
 * Erfasst Arbeitszeiten und synchronisiert automatisch mit STAFFMAN
 */
function WorkingHoursManagement() {
  const [staff, setStaff] = useState([]);
  const [workingHours, setWorkingHours] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  // Formular-Daten
  const [formData, setFormData] = useState({
    staffId: '',
    date: getTodayString(),
    startTime: '08:00',
    endTime: '16:00'
  });

  /**
   * Lädt Mitarbeiter und Arbeitszeiten beim Start
   */
  useEffect(() => {
    fetchStaff();
    fetchWorkingHours();
  }, []);

  /**
   * Lädt alle Mitarbeiter
   */
  const fetchStaff = async () => {
    try {
      const data = await api.staff.getAll();
      setStaff(data);
      if (data.length > 0 && !formData.staffId) {
        setFormData(prev => ({ ...prev, staffId: data[0].id }));
      }
    } catch (err) {
      setError(err.message);
    }
  };

  /**
   * Lädt Arbeitszeiten der letzten 7 Tage
   */
  const fetchWorkingHours = async () => {
    try {
      const today = new Date();
      const weekAgo = new Date(today);
      weekAgo.setDate(today.getDate() - 7);
      
      const data = await api.staff.getWorkingHours(
        formatDate(weekAgo),
        formatDate(today)
      );
      setWorkingHours(data);
    } catch (err) {
      console.error('Fehler beim Laden der Arbeitszeiten:', err);
    }
  };

  /**
   * Arbeitszeiten erfassen
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const workingHoursData = {
        date: formData.date,
        startTime: formData.startTime + ':00',
        endTime: formData.endTime + ':00'
      };

      await api.staff.recordWorkingHours(formData.staffId, workingHoursData);
      setSuccess('✅ Arbeitszeiten erfasst und mit STAFFMAN synchronisiert!');
      
      // Formular zurücksetzen
      setFormData({
        staffId: formData.staffId,
        date: getTodayString(),
        startTime: '08:00',
        endTime: '16:00'
      });

      // Arbeitszeiten neu laden
      setTimeout(() => {
        fetchWorkingHours();
        setSuccess(null);
      }, 2000);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Formatiert Datum für Anzeige
   */
  const formatDisplayDate = (dateStr) => {
    const date = new Date(dateStr + 'T00:00:00');
    return date.toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  /**
   * Formatiert Zeit für Anzeige (HH:mm)
   */
  const formatDisplayTime = (timeStr) => {
    return timeStr.substring(0, 5); // "HH:mm:ss" -> "HH:mm"
  };

  /**
   * Findet Mitarbeiter-Namen nach ID
   */
  const getStaffName = (staffObj) => {
    if (staffObj && staffObj.firstName && staffObj.lastName) {
      return `${staffObj.firstName} ${staffObj.lastName}`;
    }
    return 'Unbekannt';
  };

  return (
    <div className="working-hours-management">
      <h3>⏰ Arbeitszeiten erfassen</h3>
      
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      {/* Erfassungs-Formular */}
      <div className="hours-form-container">
        <form onSubmit={handleSubmit} className="hours-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="staff-select">👤 Mitarbeiter</label>
              <select
                id="staff-select"
                value={formData.staffId}
                onChange={(e) => setFormData({ ...formData, staffId: e.target.value })}
                required
              >
                <option value="">Mitarbeiter auswählen...</option>
                {staff.map(member => (
                  <option key={member.id} value={member.id}>
                    {member.firstName} {member.lastName} ({member.role})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="date-input">📅 Datum</label>
              <input
                id="date-input"
                type="date"
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="start-time">🕐 Arbeitsbeginn</label>
              <input
                id="start-time"
                type="time"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="end-time">🕔 Arbeitsende</label>
              <input
                id="end-time"
                type="time"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                required
              />
            </div>
          </div>

          <button type="submit" className="primary" disabled={loading}>
            {loading ? '⏳ Wird erfasst...' : '🔄 Erfassen & mit STAFFMAN synchronisieren'}
          </button>
        </form>
      </div>

      {/* Info-Box */}
      <div className="info-box">
        <h4>ℹ️ STAFFMAN-Synchronisation</h4>
        <p>
          Erfasste Arbeitszeiten werden automatisch an das externe System <strong>STAFFMAN</strong> übertragen.
          Dies ermöglicht eine zentrale Verwaltung der Verfügbarkeiten und Einsatzplanung.
        </p>
      </div>

      {/* Letzte erfasste Arbeitszeiten */}
      <div className="recent-hours">
        <h4>📋 Erfasste Arbeitszeiten (letzte 7 Tage)</h4>
        
        {workingHours.length === 0 ? (
          <p className="no-data">Keine Arbeitszeiten erfasst</p>
        ) : (
          <div className="hours-table-container">
            <table>
              <thead>
                <tr>
                  <th>Mitarbeiter</th>
                  <th>Datum</th>
                  <th>Arbeitsbeginn</th>
                  <th>Arbeitsende</th>
                  <th>Stunden</th>
                  <th>STAFFMAN-Sync</th>
                </tr>
              </thead>
              <tbody>
                {workingHours.map(wh => (
                  <tr key={wh.id}>
                    <td><strong>{getStaffName(wh.staff)}</strong></td>
                    <td>{formatDisplayDate(wh.date)}</td>
                    <td>{formatDisplayTime(wh.startTime)}</td>
                    <td>{formatDisplayTime(wh.endTime)}</td>
                    <td className="hours-value">{wh.hoursWorked}h</td>
                    <td>
                      <span className={`sync-badge ${wh.syncedToStaffman ? 'synced' : 'pending'}`}>
                        {wh.syncedToStaffman ? '✅ Synchronisiert' : '⏳ Ausstehend'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

/**
 * Hilfsfunktion: Heutiges Datum als String
 */
function getTodayString() {
  const today = new Date();
  return today.toISOString().split('T')[0];
}

/**
 * Hilfsfunktion: Datum formatieren
 */
function formatDate(date) {
  return date.toISOString().split('T')[0];
}

export default WorkingHoursManagement;
