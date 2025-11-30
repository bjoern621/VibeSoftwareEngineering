import React, { useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { DamageReportForm, DamageReportsList } from '../components/damage-reports';

/**
 * DamageReportsPage - Seite für Schadensberichte einer Buchung
 *
 * Diese Seite ermöglicht Mitarbeitern:
 * - Neue Schadensberichte zu erstellen
 * - Bestehende Schadensberichte anzuzeigen
 * - Fotos in einer Galerie zu betrachten
 */
const DamageReportsPage = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [refreshKey, setRefreshKey] = useState(0);
  const [successMessage, setSuccessMessage] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

  const handleSuccess = useCallback((report) => {
    setSuccessMessage(`Schadensbericht #${report.id} wurde erfolgreich erstellt.`);
    setErrorMessage(null);
    setRefreshKey((prev) => prev + 1);
    setTimeout(() => setSuccessMessage(null), 5000);
  }, []);

  const handleError = useCallback((message) => {
    setErrorMessage(message);
    setSuccessMessage(null);
  }, []);

  if (!bookingId) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg text-center">
          <span className="material-symbols-outlined text-6xl text-gray-400 mb-4">
            error_outline
          </span>
          <h2 className="text-xl font-bold text-gray-900 mb-2">Keine Buchung ausgewählt</h2>
          <p className="text-gray-600 mb-4">
            Bitte wählen Sie eine Buchung aus, um Schadensberichte zu verwalten.
          </p>
          <button
            onClick={() => navigate('/employee/check-in-out')}
            className="px-6 py-2 bg-primary text-white rounded-lg hover:opacity-90"
          >
            Zurück zur Übersicht
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <button
              onClick={() => navigate(-1)}
              className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-2"
            >
              <span className="material-symbols-outlined">arrow_back</span>
              Zurück
            </button>
            <h1 className="text-3xl font-bold text-gray-900">Schadensberichte</h1>
            <p className="text-gray-500">Buchung #{bookingId}</p>
          </div>
        </div>

        {successMessage && (
          <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg flex items-center gap-3">
            <span className="material-symbols-outlined text-green-600">check_circle</span>
            <p className="text-green-700">{successMessage}</p>
            <button
              onClick={() => setSuccessMessage(null)}
              className="ml-auto text-green-600 hover:text-green-800"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
          </div>
        )}

        {errorMessage && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-center gap-3">
            <span className="material-symbols-outlined text-red-600">error</span>
            <p className="text-red-700">{errorMessage}</p>
            <button
              onClick={() => setErrorMessage(null)}
              className="ml-auto text-red-600 hover:text-red-800"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div>
            <DamageReportForm
              bookingId={parseInt(bookingId, 10)}
              onSuccess={handleSuccess}
              onError={handleError}
            />
          </div>

          <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
            <DamageReportsList bookingId={parseInt(bookingId, 10)} refreshTrigger={refreshKey} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default DamageReportsPage;
