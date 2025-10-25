import React, { useState } from 'react';
import './StaffManagement.css';
import ScheduleRecommendation from './ScheduleRecommendation';
import WorkingHoursManagement from './WorkingHoursManagement';

/**
 * Personalverwaltung - Hauptkomponente
 * 
 * Kernaufgaben gemäß Anforderung:
 * 1. Arbeitszeiten erfassen und mit STAFFMAN synchronisieren
 * 2. Einsatzplanung basierend auf erwarteter Besucherzahl und geplanten Gerichten
 */
function StaffManagement() {
  const [activeTab, setActiveTab] = useState('schedule');

  return (
    <div className="staff-management">
      <div className="section-header">
        <h2>👥 Personalverwaltung</h2>
      </div>

      {/* Info-Box mit STAFFMAN-Integration */}
      <div className="info-box">
        <h3>ℹ️ Integration mit STAFFMAN</h3>
        <p>
          Das System synchronisiert Arbeitszeiten und Verfügbarkeiten automatisch mit dem
          externen System <strong>STAFFMAN</strong>. Die Einsatzplanung erfolgt basierend
          auf erwarteter Besucherzahl und geplanten Gerichten.
        </p>
      </div>

      {/* Tab-Navigation */}
      <div className="tabs">
        <button
          className={`tab ${activeTab === 'schedule' ? 'active' : ''}`}
          onClick={() => setActiveTab('schedule')}
        >
          📅 Einsatzplanung
        </button>
        <button
          className={`tab ${activeTab === 'hours' ? 'active' : ''}`}
          onClick={() => setActiveTab('hours')}
        >
          ⏰ Arbeitszeiten erfassen
        </button>
      </div>

      {/* Tab-Inhalte */}
      <div className="tab-content">
        {activeTab === 'schedule' && <ScheduleRecommendation />}
        {activeTab === 'hours' && <WorkingHoursManagement />}
      </div>
    </div>
  );
}

export default StaffManagement;
