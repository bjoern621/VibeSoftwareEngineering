import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ConcertDiscoveryPage from './pages/ConcertDiscoveryPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Default route redirects to concerts */}
        <Route path="/" element={<Navigate to="/concerts" replace />} />
        
        {/* Concert Discovery Page */}
        <Route path="/concerts" element={<ConcertDiscoveryPage />} />
        
        {/* Concert Detail Page (placeholder for now) */}
        <Route
          path="/concerts/:id"
          element={
            <div className="min-h-screen bg-background-light dark:bg-background-dark flex items-center justify-center">
              <div className="text-center space-y-4">
                <span className="material-symbols-outlined text-primary text-6xl">
                  construction
                </span>
                <h2 className="text-2xl font-bold text-text-primary dark:text-white">
                  Konzertdetails
                </h2>
                <p className="text-text-secondary dark:text-gray-400">
                  Diese Seite wird von deinen Teamkollegen implementiert
                </p>
              </div>
            </div>
          }
        />
        
        {/* 404 Not Found */}
        <Route
          path="*"
          element={
            <div className="min-h-screen bg-background-light dark:bg-background-dark flex items-center justify-center">
              <div className="text-center space-y-4">
                <span className="material-symbols-outlined text-red-500 text-6xl">
                  error
                </span>
                <h2 className="text-2xl font-bold text-text-primary dark:text-white">
                  404 - Seite nicht gefunden
                </h2>
                <a
                  href="/concerts"
                  className="inline-block px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                >
                  Zurück zur Konzertübersicht
                </a>
              </div>
            </div>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

