import React from 'react';
import { safeToFixed } from '../../utils/numberUtils';

/**
 * PriceOverview - Komponente zur Anzeige der Preisberechnung
 *
 * Zeigt eine detaillierte Übersicht über die berechneten Kosten:
 * - Anzahl der Miettage
 * - Tagespreis
 * - Basispreis (Tage × Tagespreis)
 * - Aufschlüsselung der Zusatzleistungen
 * - Gesamtpreis der Zusatzleistungen
 * - Gesamtpreis
 *
 * @param {Object} priceData - Preisdaten vom Backend (PriceCalculationResponseDTO)
 * @param {number} priceData.numberOfDays - Anzahl der Miettage
 * @param {number} priceData.dailyRate - Tagespreis in Euro
 * @param {number} priceData.basePrice - Basispreis (Tage × Tagespreis)
 * @param {Array<Object>} priceData.additionalServices - Liste der Zusatzleistungen
 * @param {number} priceData.additionalServicesPrice - Gesamtpreis der Zusatzleistungen
 * @param {number} priceData.totalPrice - Gesamtpreis
 */
const PriceOverview = ({ priceData }) => {
  if (!priceData) {
    return null;
  }

  const {
    numberOfDays,
    dailyRate,
    basePrice,
    additionalServices = [],
    additionalServicesPrice,
    totalPrice,
  } = priceData;

  /**
   * Formatiert einen Betrag als Euro-Währung (defensive)
   */
  const formatCurrency = (amount) => {
    const n = (amount == null || Number.isNaN(Number(amount))) ? 0 : Number(amount);
    try {
      return new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR'
      }).format(n);
    } catch (e) {
      return safeToFixed(n, 2) + ' €';
    }
  };

  /**
   * Übersetzt Zusatzleistungs-Typen ins Deutsche
   */
  const translateService = (serviceType) => {
    const translations = {
      CHILD_SEAT: 'Kindersitz',
      GPS: 'Navigationssystem',
      ADDITIONAL_DRIVER: 'Zusatzfahrer',
      FULL_INSURANCE: 'Vollkaskoversicherung',
      WINTER_TIRES: 'Winterreifen',
      ROOF_RACK: 'Dachgepäckträger',
    };
    return translations[serviceType] || serviceType;
  };

  return (
    <div className="rounded-xl border border-gray-200 bg-card-bg shadow-sm p-6">
      <h2 className="text-2xl font-bold mb-6 text-gray-900">Preisübersicht</h2>

      <div className="space-y-4">
        {/* Mietdauer */}
        <div className="flex justify-between items-center pb-3 border-b border-gray-200">
          <div>
            <p className="font-medium text-gray-900">Mietdauer</p>
            <p className="text-sm text-gray-600">
              {numberOfDays} {numberOfDays === 1 ? 'Tag' : 'Tage'}
            </p>
          </div>
          <p className="text-lg font-semibold text-gray-900">
            {formatCurrency(dailyRate)}{' '}
            <span className="text-sm font-normal text-gray-600">/Tag</span>
          </p>
        </div>

        {/* Basispreis */}
        <div className="flex justify-between items-center pb-3 border-b border-gray-200">
          <div>
            <p className="font-medium text-gray-900">Basispreis</p>
            <p className="text-sm text-gray-600">
              {numberOfDays} {numberOfDays === 1 ? 'Tag' : 'Tage'} ×{' '}
              {formatCurrency(dailyRate)}
            </p>
          </div>
          <p className="text-lg font-semibold text-gray-900">
            {formatCurrency(basePrice)}
          </p>
        </div>

        {/* Zusatzleistungen */}
        {additionalServices.length > 0 && (
          <div className="pb-3 border-b border-gray-200">
            <p className="font-medium text-gray-900 mb-3">Zusatzleistungen</p>
            <div className="space-y-2 ml-4">
              {additionalServices.map((service, index) => (
                <div key={index} className="flex justify-between items-center text-sm">
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-base text-primary">
                      check_circle
                    </span>
                    <span className="text-gray-700">
                      {translateService(service.type)}
                    </span>
                  </div>
                  <span className="text-gray-900 font-medium">
                    {formatCurrency(service.totalPrice)}
                    <span className="text-xs text-gray-600 ml-1">
                      ({formatCurrency(service.dailyPrice)}/Tag)
                    </span>
                  </span>
                </div>
              ))}
            </div>
            <div className="flex justify-between items-center mt-3 pt-2 border-t border-gray-100">
              <p className="text-sm font-medium text-gray-700">
                Summe Zusatzleistungen
              </p>
              <p className="text-base font-semibold text-gray-900">
                {formatCurrency(additionalServicesPrice)}
              </p>
            </div>
          </div>
        )}

        {/* Gesamtpreis */}
        <div className="flex justify-between items-center pt-4 bg-primary bg-opacity-5 -mx-6 px-6 py-4 rounded-b-xl">
          <p className="text-xl font-bold text-gray-900">Gesamtpreis</p>
          <p className="text-2xl font-bold text-primary">
            {formatCurrency(totalPrice)}
          </p>
        </div>
      </div>

      {/* Hinweis */}
      <div className="mt-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <div className="flex gap-2">
          <span className="material-symbols-outlined text-yellow-600 text-base">
            info
          </span>
          <p className="text-xs text-yellow-800">
            Dies ist eine unverbindliche Preisberechnung. Der endgültige Preis
            kann je nach Verfügbarkeit und gewähltem Fahrzeug variieren.
          </p>
        </div>
      </div>
    </div>
  );
};

export default PriceOverview;
