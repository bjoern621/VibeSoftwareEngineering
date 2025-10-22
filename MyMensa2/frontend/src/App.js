import React, { useState } from 'react';
import './App.css';
import MealManagement from './components/MealManagement';

/**
 * Haupt-App-Komponente für MyMensa2
 * Verwaltet die Navigation zwischen Kundenansicht und Admin-Portal
 */
function App() {
  const [view, setView] = useState('order'); // 'order' oder 'admin'

  return (
    <div className="App">
      {/* Header */}
      <header className="app-header">
        <div className="header-content">
          <h1>🍽️ MyMensa Verwaltungsportal</h1>
          <p className="header-subtitle">Gerichteverwaltung für Ihre Mensa</p>
        </div>
      </header>

      {/* Hauptinhalt */}
      <main className="app-main">
        <MealManagement />
      </main>

      {/* Footer */}
      <footer className="app-footer">
        <p>© 2025 MyMensa Verwaltungssystem</p>
      </footer>
    </div>
  );
}

export default App;
