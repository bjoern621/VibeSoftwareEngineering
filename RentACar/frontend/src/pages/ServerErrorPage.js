import React from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * 500 Server Error Seite
 * Wird angezeigt bei Server-Fehlern (5xx)
 */
const ServerErrorPage = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate('/');
  };

  const handleReload = () => {
    window.location.reload();
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 to-pink-50 flex items-center justify-center p-4">
      <div className="max-w-2xl w-full bg-white rounded-2xl shadow-xl p-8 md:p-12">
        {/* Icon */}
        <div className="flex justify-center mb-6">
          <div className="w-24 h-24 bg-purple-100 rounded-full flex items-center justify-center">
            <span className="material-symbols-outlined text-6xl text-purple-500">
              cloud_off
            </span>
          </div>
        </div>

        {/* Fehlercode */}
        <div className="text-center mb-4">
          <p className="text-8xl font-bold text-purple-500 opacity-20">500</p>
        </div>

        {/* Titel */}
        <h1 className="text-3xl md:text-4xl font-bold text-gray-900 text-center mb-4">
          Serverfehler
        </h1>

        {/* Beschreibung */}
        <p className="text-gray-600 text-center mb-8 text-lg">
          Entschuldigung! Unser Server hat gerade ein Problem. 
          Wir arbeiten bereits daran, das Problem zu beheben.
        </p>

        {/* Info-Box */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-8">
          <div className="flex items-start gap-3">
            <span className="material-symbols-outlined text-blue-600 mt-0.5">
              lightbulb
            </span>
            <div className="flex-1">
              <p className="text-sm text-blue-800 font-medium mb-1">
                Was Sie tun können:
              </p>
              <ul className="text-sm text-blue-700 space-y-1 list-disc list-inside">
                <li>Laden Sie die Seite in wenigen Sekunden neu</li>
                <li>Überprüfen Sie Ihre Internetverbindung</li>
                <li>Versuchen Sie es in ein paar Minuten erneut</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Aktionen */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <button
            onClick={handleReload}
            className="bg-primary text-white px-8 py-3 rounded-lg hover:bg-primary-dark transition-colors duration-200 flex items-center justify-center gap-2 font-medium"
          >
            <span className="material-symbols-outlined">refresh</span>
            Seite neu laden
          </button>
          <button
            onClick={handleGoHome}
            className="bg-gray-200 text-gray-800 px-8 py-3 rounded-lg hover:bg-gray-300 transition-colors duration-200 flex items-center justify-center gap-2 font-medium"
          >
            <span className="material-symbols-outlined">home</span>
            Zur Startseite
          </button>
        </div>

        {/* Support-Hinweis */}
        <div className="mt-8 pt-6 border-t border-gray-200 text-center">
          <p className="text-sm text-gray-500">
            Falls der Fehler weiterhin besteht, kontaktieren Sie bitte unseren Support.
          </p>
          <p className="text-xs text-gray-400 mt-2">
            Fehlercode: 500 - Interner Serverfehler
          </p>
        </div>
      </div>
    </div>
  );
};

export default ServerErrorPage;
