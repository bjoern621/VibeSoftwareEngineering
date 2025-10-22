import React from 'react';
import './App.css';
import MealManagement from './components/MealManagement';

function App() {
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
