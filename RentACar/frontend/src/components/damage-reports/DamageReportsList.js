import React, { useState, useEffect, useCallback } from 'react';
import { getDamageReportsByBooking } from '../../services/damageReportService';

/**
 * DamageReportsList - Liste der Schadensberichte pro Buchung
 *
 * Features:
 * - Liste aller Schadensberichte
 * - Detailansicht eines Schadens
 * - Galerie für Fotos
 */
const DamageReportsList = ({ bookingId, refreshTrigger }) => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [selectedPhoto, setSelectedPhoto] = useState(null);

  const fetchReports = useCallback(async () => {
    if (!bookingId) return;

    setLoading(true);
    setError(null);

    try {
      const data = await getDamageReportsByBooking(bookingId);
      setReports(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [bookingId]);

  useEffect(() => {
    fetchReports();
  }, [fetchReports, refreshTrigger]);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount || 0);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <p className="text-red-600 text-sm">{error}</p>
        <button
          onClick={fetchReports}
          className="mt-2 text-sm text-red-700 underline hover:no-underline"
        >
          Erneut versuchen
        </button>
      </div>
    );
  }

  if (reports.length === 0) {
    return (
      <div className="bg-gray-50 rounded-lg p-6 text-center">
        <span className="material-symbols-outlined text-4xl text-gray-400 mb-2">check_circle</span>
        <p className="text-gray-500">Keine Schadensberichte vorhanden</p>
      </div>
    );
  }

  return (
    <div>
      <h3 className="text-lg font-bold text-gray-900 mb-4">
        Vorhandene Schadensberichte ({reports.length})
      </h3>

      <div className="space-y-4">
        {reports.map((report) => (
          <div
            key={report.id}
            className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
            onClick={() => setSelectedReport(report)}
          >
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className="material-symbols-outlined text-red-500 text-lg">warning</span>
                  <span className="font-semibold text-gray-900">Schadensbericht #{report.id}</span>
                </div>
                <p className="text-sm text-gray-600 line-clamp-2">{report.description}</p>
              </div>
              <div className="text-right ml-4">
                <p className="text-lg font-bold text-red-600">
                  {formatCurrency(report.estimatedCost)}
                </p>
                {report.photos && report.photos.length > 0 && (
                  <p className="text-xs text-gray-500">{report.photos.length} Foto(s)</p>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {selectedReport && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          onClick={() => setSelectedReport(null)}
        >
          <div
            className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="sticky top-0 bg-white border-b border-gray-200 p-4 flex justify-between items-center">
              <h3 className="text-xl font-bold text-gray-900">
                Schadensbericht #{selectedReport.id}
              </h3>
              <button
                onClick={() => setSelectedReport(null)}
                className="p-2 hover:bg-gray-100 rounded-full"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div className="p-6">
              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-500 mb-1">Beschreibung</h4>
                <p className="text-gray-900">{selectedReport.description}</p>
              </div>

              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-500 mb-1">Geschätzte Kosten</h4>
                <p className="text-2xl font-bold text-red-600">
                  {formatCurrency(selectedReport.estimatedCost)}
                </p>
              </div>

              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-500 mb-1">Referenz-IDs</h4>
                <div className="grid grid-cols-2 gap-2 text-sm">
                  <div>
                    <span className="text-gray-500">Buchung:</span>{' '}
                    <span className="font-medium">#{selectedReport.bookingId}</span>
                  </div>
                  <div>
                    <span className="text-gray-500">Fahrzeug:</span>{' '}
                    <span className="font-medium">#{selectedReport.vehicleId}</span>
                  </div>
                  <div>
                    <span className="text-gray-500">Mietvertrag:</span>{' '}
                    <span className="font-medium">#{selectedReport.rentalAgreementId}</span>
                  </div>
                </div>
              </div>

              {selectedReport.photos && selectedReport.photos.length > 0 && (
                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-3">
                    Fotos ({selectedReport.photos.length})
                  </h4>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                    {selectedReport.photos.map((photo, index) => (
                      <div
                        key={index}
                        className="relative aspect-square rounded-lg overflow-hidden cursor-pointer hover:opacity-90 transition-opacity"
                        onClick={() => setSelectedPhoto(photo)}
                      >
                        <img
                          src={photo}
                          alt={`Schadensfoto ${index + 1}`}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {selectedPhoto && (
        <div
          className="fixed inset-0 bg-black bg-opacity-90 flex items-center justify-center z-[60]"
          onClick={() => setSelectedPhoto(null)}
        >
          <button
            onClick={() => setSelectedPhoto(null)}
            className="absolute top-4 right-4 p-2 text-white hover:bg-white hover:bg-opacity-20 rounded-full"
          >
            <span className="material-symbols-outlined text-3xl">close</span>
          </button>
          <img
            src={selectedPhoto}
            alt="Schadensfoto Vollbild"
            className="max-w-full max-h-full object-contain"
          />
        </div>
      )}
    </div>
  );
};

export default DamageReportsList;
