import React, { useState } from 'react';
import './App.css';
import WeekView from './components/WeekView';
import MealManagement from './components/MealManagement';
import Dashboard from './components/Dashboard';

function App() {
  // State für die Navigation zwischen den Seiten
  const [currentPage, setCurrentPage] = useState('weekview');

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
            className={`nav-btn ${currentPage === 'meals' ? 'active' : ''}`}
            onClick={() => setCurrentPage('meals')}
          >
            🍲 Gerichteverwaltung
          </button>
          <button
            className={`nav-btn ${currentPage === 'dashboard' ? 'active' : ''}`}
            onClick={() => setCurrentPage('dashboard')}
          >
            📊 Dashboard
          </button>
        </nav>
      </header>

      {/* Hauptinhalt */}
      <main className="app-main">
        {currentPage === 'weekview' && <WeekView />}
        {currentPage === 'meals' && <MealManagement />}
        {currentPage === 'dashboard' && <Dashboard />}
      </main>

      {/* Footer */}
      <footer className="app-footer">
        <p>© 2025 MyMensa</p>
      </footer>
    </div>
  );
}

export default App;