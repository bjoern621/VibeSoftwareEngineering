import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ConcertDiscoveryPage from './pages/ConcertDiscoveryPage';
import ConcertDetailPage from './pages/ConcertDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Default route redirects to concerts */}
          <Route path="/" element={<Navigate to="/concerts" replace />} />
          
          {/* Auth Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        
        {/* Concert Discovery Page */}
        <Route path="/concerts" element={<ConcertDiscoveryPage />} />
        
        {/* Concert Detail Page */}
        <Route path="/concerts/:id" element={<ConcertDetailPage />} />
        
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
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

