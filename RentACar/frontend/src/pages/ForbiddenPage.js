import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * 403 Forbidden Seite
 * Wird angezeigt, wenn ein Benutzer keine Berechtigung für eine Ressource hat
 */
const ForbiddenPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleGoHome = () => {
    navigate('/');
  };

  const handleGoBack = () => {
    navigate(-1);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-red-50 to-orange-50 flex items-center justify-center p-4">
      <div className="max-w-2xl w-full bg-white rounded-2xl shadow-xl p-8 md:p-12">
        {/* Icon */}
        <div className="flex justify-center mb-6">
          <div className="w-24 h-24 bg-red-100 rounded-full flex items-center justify-center">
            <span className="material-symbols-outlined text-6xl text-red-500">
              block
            </span>
          </div>
        </div>

        {/* Fehlercode */}
        <div className="text-center mb-4">
          <p className="text-8xl font-bold text-red-500 opacity-20">403</p>
        </div>

        {/* Titel */}
        <h1 className="text-3xl md:text-4xl font-bold text-gray-900 text-center mb-4">
          Zugriff verweigert
        </h1>

        {/* Beschreibung */}
        <p className="text-gray-600 text-center mb-8 text-lg">
          Sie haben keine Berechtigung, auf diese Seite zuzugreifen. 
          {user ? (
            <> Ihr aktuelles Konto (<strong>{user.email}</strong>) verfügt nicht über die erforderlichen Rechte.</>
          ) : (
            <> Bitte melden Sie sich mit einem autorisierten Konto an.</>
          )}
        </p>

        {/* Info-Box */}
        <div className="bg-orange-50 border border-orange-200 rounded-lg p-4 mb-8">
          <div className="flex items-start gap-3">
            <span className="material-symbols-outlined text-orange-600 mt-0.5">
              info
            </span>
            <div className="flex-1">
              <p className="text-sm text-orange-800 font-medium mb-1">
                Benötigen Sie Zugriff auf Mitarbeiter-Funktionen?
              </p>
              <p className="text-sm text-orange-700">
                Wenden Sie sich bitte an Ihren Administrator, um die entsprechenden Berechtigungen zu erhalten.
              </p>
            </div>
          </div>
        </div>

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

        {/* Hilfe */}
        <div className="mt-8 pt-6 border-t border-gray-200 text-center">
          <p className="text-sm text-gray-500">
            Falls Sie glauben, dass dies ein Fehler ist, kontaktieren Sie bitte unseren Support.
          </p>
        </div>
      </div>
    </div>
  );
};

export default ForbiddenPage;
