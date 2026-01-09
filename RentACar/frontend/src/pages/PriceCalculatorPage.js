import React, { useState } from 'react';
import PriceCalculatorForm from '../components/calculator/PriceCalculatorForm';
import PriceOverview from '../components/calculator/PriceOverview';
import { calculatePrice } from '../services/bookingService';

/**
 * PriceCalculatorPage - Hauptseite für den Preiskalkulator
 * 
 * Ermöglicht Kunden die Berechnung der Mietkosten ohne Buchungsabschluss.
 * Zeigt ein Formular für die Eingabe der Parameter und eine detaillierte
 * Preisübersicht nach der Berechnung.
 */
const PriceCalculatorPage = () => {
  const [priceData, setPriceData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Behandelt die Preisberechnung
   */
  const handleCalculate = async (formData) => {
    setIsLoading(true);
    setError(null);
    setPriceData(null);

    try {
      const result = await calculatePrice(formData);
      setPriceData(result);
    } catch (err) {
      console.error('Fehler bei Preisberechnung:', err);
      setError(err.message || 'Ein unerwarteter Fehler ist aufgetreten.');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Setzt das Formular zurück
   */
  const handleReset = () => {
    setPriceData(null);
    setError(null);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-7xl">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">
            Preiskalkulator
          </h1>
          <p className="text-gray-600">
            Berechnen Sie unverbindlich die Kosten für Ihre geplante Fahrzeugmiete
          </p>
        </div>

        {/* Fehleranzeige */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <div className="flex gap-3">
              <span className="material-symbols-outlined text-red-600">error</span>
              <div className="flex-1">
                <h3 className="font-semibold text-red-900 mb-1">Fehler bei der Preisberechnung</h3>
                <p className="text-sm text-red-800">{error}</p>
              </div>
              <button
                onClick={() => setError(null)}
                className="text-red-600 hover:text-red-800"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
          </div>
        )}

        {/* Layout: Form links, Ergebnis rechts (Desktop) / untereinander (Mobile) */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Formular */}
          <div>
            <div className="rounded-xl border border-gray-200 bg-card-bg shadow-sm p-6">
              <h2 className="text-2xl font-bold mb-6 text-gray-900">
                Ihre Angaben
              </h2>
              <PriceCalculatorForm 
                onCalculate={handleCalculate} 
                isLoading={isLoading}
              />
            </div>

            {/* Info-Box */}
            <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex gap-3">
                <span className="material-symbols-outlined text-blue-600">lightbulb</span>
                <div className="flex-1">
                  <h3 className="font-semibold text-blue-900 mb-1">Tipp</h3>
                  <p className="text-sm text-blue-800">
                    Die Preisberechnung ist unverbindlich und zeigt Ihnen eine erste Kostenschätzung. 
                    Der endgültige Preis wird bei der tatsächlichen Buchung bestätigt.
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Preisübersicht */}
          <div>
            {isLoading ? (
              <div className="rounded-xl border border-gray-200 bg-card-bg shadow-sm p-6">
                <div className="flex flex-col items-center justify-center py-12">
                  <span className="material-symbols-outlined text-6xl text-primary animate-spin mb-4">
                    progress_activity
                  </span>
                  <p className="text-gray-600">Berechne Preis...</p>
                </div>
              </div>
            ) : priceData ? (
              <div>
                <PriceOverview priceData={priceData} />
                
                {/* Aktionen nach Berechnung */}
                <div className="mt-6 flex flex-col sm:flex-row gap-3">
                  <button
                    onClick={handleReset}
                    className="flex-1 bg-gray-100 text-gray-700 py-3 px-6 rounded-lg font-medium hover:bg-gray-200 transition-colors flex items-center justify-center gap-2"
                  >
                    <span className="material-symbols-outlined">refresh</span>
                    Neue Berechnung
                  </button>
                  <button
                    onClick={() => window.location.href = '/search'}
                    className="flex-1 bg-primary text-white py-3 px-6 rounded-lg font-medium hover:bg-primary-dark transition-colors flex items-center justify-center gap-2"
                  >
                    <span className="material-symbols-outlined">search</span>
                    Fahrzeuge suchen
                  </button>
                </div>
              </div>
            ) : (
              <div className="rounded-xl border border-gray-200 bg-card-bg shadow-sm p-6">
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <span className="material-symbols-outlined text-6xl text-gray-300 mb-4">
                    calculate
                  </span>
                  <h3 className="text-lg font-semibold text-gray-700 mb-2">
                    Bereit zur Berechnung
                  </h3>
                  <p className="text-gray-500 max-w-md">
                    Füllen Sie das Formular aus und klicken Sie auf "Preis berechnen", 
                    um eine detaillierte Kostenübersicht zu erhalten.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Zusätzliche Informationen */}
        <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg p-6 border border-gray-200">
            <div className="flex items-start gap-3">
              <span className="material-symbols-outlined text-primary text-2xl">verified</span>
              <div>
                <h3 className="font-semibold text-gray-900 mb-1">Transparente Preise</h3>
                <p className="text-sm text-gray-600">
                  Alle Kosten werden klar aufgeschlüsselt - keine versteckten Gebühren
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg p-6 border border-gray-200">
            <div className="flex items-start gap-3">
              <span className="material-symbols-outlined text-primary text-2xl">schedule</span>
              <div>
                <h3 className="font-semibold text-gray-900 mb-1">Flexible Mietdauer</h3>
                <p className="text-sm text-gray-600">
                  Mieten Sie ab 1 Tag - die Preise passen sich automatisch an
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg p-6 border border-gray-200">
            <div className="flex items-start gap-3">
              <span className="material-symbols-outlined text-primary text-2xl">add_circle</span>
              <div>
                <h3 className="font-semibold text-gray-900 mb-1">Zusatzleistungen</h3>
                <p className="text-sm text-gray-600">
                  Wählen Sie aus verschiedenen Extras für eine komfortable Fahrt
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PriceCalculatorPage;

