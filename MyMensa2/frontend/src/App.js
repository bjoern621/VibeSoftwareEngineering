import React, { useState } from 'react';
import './App.css';
import MealManagement from './components/MealManagement';
import Dashboard from './components/Dashboard';
import MealPlanManagement from './components/MealPlanManagement';
import OrderManagement from './components/OrderManagement';
import AdminOrderManagement from './components/AdminOrderManagement';
import InventoryManagement from './components/InventoryManagement';
import StaffManagement from './components/StaffManagement';

function App() {
  const [view, setView] = useState('customer'); // 'customer' oder 'admin'
  const [activeTab, setActiveTab] = useState('meals');

  const renderAdminContent = () => {
    switch (activeTab) {
      case 'meals':
        return <MealManagement />;
      case 'dashboard':
        return <Dashboard />;
      case 'mealplan':
        return <MealPlanManagement />;
      case 'admin-orders':
        return <AdminOrderManagement />;
      case 'inventory':
        return <InventoryManagement />;
      case 'staff':
        return <StaffManagement />;
      default:
        return <MealManagement />;
    }
  };

  return (
    <div className="App">
      {/* Header */}
      <header className="app-header">
        <div className="header-content">
          <h1>🍽️ MyMensa</h1>
          <p className="header-subtitle">
            {view === 'customer' ? 'Willkommen beim digitalen Bestellsystem' : 'Verwaltungsportal'}
          </p>
        </div>
        
        {/* View Switcher */}
        <div className="view-switcher">
          <button 
            className={`view-btn ${view === 'customer' ? 'active' : ''}`}
            onClick={() => setView('customer')}
          >
            🛒 Bestellungen
          </button>
          <button 
            className={`view-btn ${view === 'admin' ? 'active' : ''}`}
            onClick={() => setView('admin')}
          >
            ⚙️ Verwaltung
          </button>
        </div>
      </header>

      {view === 'customer' ? (
        /* Kundenbereich - Nur Bestellungen */
        <main className="app-main">
          <OrderManagement />
        </main>
      ) : (
        /* Verwaltungsbereich - Admin-Tabs */
        <>
          <nav className="app-nav">
            <button 
              className={`nav-tab ${activeTab === 'meals' ? 'active' : ''}`}
              onClick={() => setActiveTab('meals')}
            >
              🍕 Gerichte
            </button>
            <button 
              className={`nav-tab ${activeTab === 'mealplan' ? 'active' : ''}`}
              onClick={() => setActiveTab('mealplan')}
            >
              � Speiseplan
            </button>
            <button 
              className={`nav-tab ${activeTab === 'admin-orders' ? 'active' : ''}`}
              onClick={() => setActiveTab('admin-orders')}
            >
              ✅ Bestellverwaltung
            </button>
            <button 
              className={`nav-tab ${activeTab === 'dashboard' ? 'active' : ''}`}
              onClick={() => setActiveTab('dashboard')}
            >
              📊 Dashboard
            </button>
            <button 
              className={`nav-tab ${activeTab === 'inventory' ? 'active' : ''}`}
              onClick={() => setActiveTab('inventory')}
            >
              📦 Lager
            </button>
            <button 
              className={`nav-tab ${activeTab === 'staff' ? 'active' : ''}`}
              onClick={() => setActiveTab('staff')}
            >
              👥 Personal
            </button>
          </nav>

          <main className="app-main">
            {renderAdminContent()}
          </main>
        </>
      )}

      {/* Footer */}
      <footer className="app-footer">
        <p>© 2025 MyMensa Verwaltungssystem | Integriert mit EASYPAY, FOODSUPPLY & STAFFMAN</p>
      </footer>
    </div>
  );
} 

export default App;