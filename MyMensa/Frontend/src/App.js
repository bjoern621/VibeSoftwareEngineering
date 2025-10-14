import React, { useState } from 'react';
import './App.css';
import WeekView from './components/WeekView';
import OrderManagement from './components/OrderManagement';
import AdminPanel from './components/AdminPanel';

function App() {
  // State für die Navigation zwischen den Seiten
  const [currentPage, setCurrentPage] = useState('weekview');
  const [showAdminPanel, setShowAdminPanel] = useState(false);

  // Wenn AdminPanel aktiv ist, zeige nur dieses an
  if (showAdminPanel) {
    return <AdminPanel onExit={() => setShowAdminPanel(false)} />;
  }

  return (
    <div className="App">
      {/* Header für die gesamte Anwendung */}
      <header className="app-header">
        <h1>🍽️ MyMensa</h1>
        <p className="app-subtitle">Dein digitaler Mensaplan</p>

        {/* Navigation */}
        <nav className="app-nav">
          <button
            className={`nav-btn ${currentPage === 'weekview' ? 'active' : ''}`}
            onClick={() => setCurrentPage('weekview')}
          >
            📅 Wochenplan
          </button>
          <button
            className={`nav-btn ${currentPage === 'order' ? 'active' : ''}`}
            onClick={() => setCurrentPage('order')}
          >
            🛒 Bestellen
          </button>
          <button
            className="nav-btn admin-btn"
            onClick={() => setShowAdminPanel(true)}
          >
            🔧 Verwaltung
          </button>
        </nav>
      </header>

      {/* Hauptinhalt */}
      <main className="app-main">
        {currentPage === 'weekview' && <WeekView />}
        {currentPage === 'order' && <OrderManagement />}
      </main>

      {/* Footer */}
      <footer className="app-footer">
        <p>© 2025 MyMensa</p>
      </footer>
    </div>
  );
}

export default App;