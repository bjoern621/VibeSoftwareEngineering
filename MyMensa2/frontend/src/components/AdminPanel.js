import React, { useState } from 'react';
import './AdminPanel.css';
import Dashboard from './Dashboard';
import MealManagement from './MealManagement';
import MealPlanManagement from './MealPlanManagement';
import AdminOrderManagement from './AdminOrderManagement';
import InventoryManagement from './InventoryManagement';
import StaffManagement from './StaffManagement';

/**
 * Admin-Panel mit Tabs für alle Verwaltungsfunktionen
 * Zentrale Komponente für die Mensaverwaltung
 */
function AdminPanel() {
  const [activeTab, setActiveTab] = useState('dashboard');

  const tabs = [
    { id: 'dashboard', label: '📊 Dashboard', icon: '📊' },
    { id: 'meals', label: '🍽️ Gerichte', icon: '🍽️' },
    { id: 'mealplans', label: '📅 Speisepläne', icon: '📅' },
    { id: 'orders', label: '🛒 Bestellungen', icon: '🛒' },
    { id: 'inventory', label: '📦 Lager', icon: '📦' },
    { id: 'staff', label: '👥 Personal', icon: '👥' }
  ];

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <Dashboard />;
      case 'meals':
        return <MealManagement />;
      case 'mealplans':
        return <MealPlanManagement />;
      case 'orders':
        return <AdminOrderManagement />;
      case 'inventory':
        return <InventoryManagement />;
      case 'staff':
        return <StaffManagement />;
      default:
        return <Dashboard />;
    }
  };

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h2>🏢 Verwaltungs-Portal</h2>
        <p>Zentrale Steuerung für MyMensa2</p>
      </div>

      <div className="admin-tabs">
        {tabs.map(tab => (
          <button
            key={tab.id}
            className={`tab-button ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <span className="tab-icon">{tab.icon}</span>
            <span className="tab-label">{tab.label}</span>
          </button>
        ))}
      </div>

      <div className="admin-content">
        {renderContent()}
      </div>
    </div>
  );
}

export default AdminPanel;
