import React, { useState } from 'react';

/**
 * CancellationModal - Modal für Buchungsstornierung
 * Ermöglicht Eingabe eines Stornierungsgrunds
 */
const CancellationModal = ({ onConfirm, onCancel, loading = false }) => {
  const [reason, setReason] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (reason.trim()) {
      onConfirm(reason.trim());
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl max-w-md w-full">
        {/* Header */}
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
              <span className="material-symbols-outlined text-red-500">warning</span>
              Buchung stornieren
            </h2>
            <button
              onClick={onCancel}
              disabled={loading}
              className="text-gray-400 hover:text-gray-600 transition-colors"
              aria-label="Schließen"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
          </div>
        </div>

        {/* Content */}
        <form onSubmit={handleSubmit} className="p-6">
          <div className="mb-6">
            <p className="text-gray-700 mb-4">
              Möchten Sie diese Buchung wirklich stornieren? Diese Aktion kann nicht rückgängig gemacht werden.
            </p>
            
            <label htmlFor="cancellation-reason" className="block text-sm font-medium text-gray-700 mb-2">
              Stornierungsgrund <span className="text-red-500">*</span>
            </label>
            <textarea
              id="cancellation-reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              disabled={loading}
              required
              rows={4}
              placeholder="Bitte geben Sie einen Grund für die Stornierung an..."
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent resize-none disabled:bg-gray-100 disabled:cursor-not-allowed"
              maxLength={500}
            />
            <p className="text-xs text-gray-500 mt-1">
              {reason.length}/500 Zeichen
            </p>
          </div>

          {/* Info Box */}
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
            <div className="flex gap-3">
              <span className="material-symbols-outlined text-yellow-600 flex-shrink-0">info</span>
              <div className="text-sm text-yellow-800">
                <p className="font-medium mb-1">Wichtige Hinweise:</p>
                <ul className="list-disc list-inside space-y-1">
                  <li>Stornierungen sind nur bis 24 Stunden vor Abholung möglich</li>
                  <li>Sie erhalten eine Bestätigungs-E-Mail</li>
                  <li>Eventuelle Rückerstattungen werden innerhalb von 5-7 Werktagen bearbeitet</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={onCancel}
              disabled={loading}
              className="px-6 py-3 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Abbrechen
            </button>
            <button
              type="submit"
              disabled={loading || !reason.trim()}
              className="px-6 py-3 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                  <span>Wird storniert...</span>
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined">check</span>
                  <span>Jetzt stornieren</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CancellationModal;
