import React from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * 404 Not Found Seite
 * Wird angezeigt, wenn eine Route nicht existiert
 */
const NotFoundPage = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate('/');
  };

  const handleGoBack = () => {
    navigate(-1);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center p-4">
      <div className="max-w-2xl w-full bg-white rounded-2xl shadow-xl p-8 md:p-12">
        {/* Icon */}
        <div className="flex justify-center mb-6">
          <div className="w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center">
            <span className="material-symbols-outlined text-6xl text-primary">
              search_off
            </span>
          </div>
        </div>

        {/* Fehlercode */}
        <div className="text-center mb-4">
          <p className="text-8xl font-bold text-primary opacity-20">404</p>
        </div>

        {/* Titel */}
        <h1 className="text-3xl md:text-4xl font-bold text-gray-900 text-center mb-4">
          Seite nicht gefunden
        </h1>

        {/* Beschreibung */}
        <p className="text-gray-600 text-center mb-8 text-lg">
          Die von Ihnen gesuchte Seite existiert leider nicht oder wurde verschoben.
          Bitte überprüfen Sie die URL oder kehren Sie zur Startseite zurück.
        </p>

        {/* Aktionen */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <button
            onClick={handleGoHome}
            className="bg-primary text-white px-8 py-3 rounded-lg hover:bg-primary-dark transition-colors duration-200 flex items-center justify-center gap-2 font-medium"
          >
            <span className="material-symbols-outlined">home</span>
            Zur Startseite
          </button>
          <button
            onClick={handleGoBack}
            className="bg-gray-200 text-gray-800 px-8 py-3 rounded-lg hover:bg-gray-300 transition-colors duration-200 flex items-center justify-center gap-2 font-medium"
          >
            <span className="material-symbols-outlined">arrow_back</span>
            Zurück
          </button>
        </div>

        {/* Hilfreiche Links */}
        <div className="mt-8 pt-6 border-t border-gray-200">
          <p className="text-sm text-gray-600 text-center mb-4">
            Vielleicht finden Sie hier, was Sie suchen:
          </p>
          <div className="flex flex-wrap justify-center gap-4">
            <button
              onClick={() => navigate('/vehicles')}
              className="text-primary hover:underline text-sm font-medium"
            >
              Fahrzeugsuche
            </button>
            <button
              onClick={() => navigate('/price-calculator')}
              className="text-primary hover:underline text-sm font-medium"
            >
              Preisrechner
            </button>
            <button
              onClick={() => navigate('/login')}
              className="text-primary hover:underline text-sm font-medium"
            >
              Anmelden
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
