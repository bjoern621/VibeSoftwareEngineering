import React from 'react';
import './App.css';
import WeekView from './components/WeekView';

function App() {
  return (
    <div className="App">
      {/* Header für die gesamte Anwendung */}
      <header className="app-header">
        <h1>🍽️ MyMensa</h1>
        <p className="app-subtitle">Dein digitaler Mensaplan</p>
      </header>

      {/* Hauptinhalt */}
      <main className="app-main">
        <WeekView />
      </main>

      {/* Footer */}
      <footer className="app-footer">
        <p>© 2025 MyMensa</p>
      </footer>
    </div>
  );
}

export default App;