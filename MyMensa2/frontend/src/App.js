import React, { useState } from 'react';
import './App.css';
import OrderManagement from './components/OrderManagement';
import AdminPanel from './components/AdminPanel';

/**
 * Haupt-App-Komponente für MyMensa2
 * Verwaltet die Navigation zwischen Kundenansicht und Admin-Portal
 */
function App() {
  const [view, setView] = useState('order'); // 'order' oder 'admin'

  return (
    <div className="App">
      {/* Header mit Navigation */}
      <header className="app-header">
        <div className="header-content">
          <h1>🍽️ MyMensa</h1>
          <p className="header-subtitle">Willkommen beim digitalen Bestellsystem</p>
        </div>
        
        <div className="header-nav">
          <button 
            className={`nav-button ${view === 'order' ? 'active' : ''}`}
            onClick={() => setView('order')}
          >
            🍽️ Bestellungen
          </button>
          <button 
            className={`nav-button ${view === 'admin' ? 'active' : ''}`}
            onClick={() => setView('admin')}
          >
            ⚙️ Verwaltung
          </button>
        </div>
      </header>

      {/* Hauptinhalt */}
      <main className="app-main">
        {view === 'order' ? <OrderManagement /> : <AdminPanel />}
      </main>
    </div>
  );
}

export default App;
