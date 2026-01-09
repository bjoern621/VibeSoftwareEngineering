import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useBooking } from '../context/BookingContext';
import { useAuth } from '../context/AuthContext';
import WizardProgressBar from '../components/bookings/WizardProgressBar';
import WizardStep1 from '../components/bookings/WizardStep1';
import WizardStep2 from '../components/bookings/WizardStep2';
import WizardStep3 from '../components/bookings/WizardStep3';
import WizardStep4 from '../components/bookings/WizardStep4';
import WizardStep5 from '../components/bookings/WizardStep5';

const BookingWizardPage = () => {
  const { currentStep, totalSteps, bookingData, nextStep, prevStep, setVehicle } = useBooking();
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Debug: Log beim Laden der Seite
  useEffect(() => {
    console.log('=== BookingWizardPage geladen ===');
    console.log('Current Step:', currentStep);
    console.log('Booking Data:', bookingData);
    console.log('BookingData.vehicleId:', bookingData.vehicleId);
    console.log('BookingData.vehicle:', bookingData.vehicle);
    console.log('BookingData.pickupBranchId:', bookingData.pickupBranchId);
    console.log('BookingData.returnBranchId:', bookingData.returnBranchId);
    console.log('BookingData.pickupDateTime:', bookingData.pickupDateTime);
    console.log('BookingData.returnDateTime:', bookingData.returnDateTime);
    console.log('Location State:', location.state);
  }, [currentStep, bookingData, location.state]);

  // Initialisiere Buchungsdaten aus Navigation State (wenn von VehicleDetailsPage kommend)
  useEffect(() => {
    if (location.state?.fromVehicleDetails) {
      console.log('=== Initialisierung von VehicleDetailsPage ===');
      // Fahrzeug, Datum und Filialen sollten bereits im Context sein
      // Aber zur Sicherheit nochmal validieren
      if (!bookingData.vehicleId || !bookingData.pickupDateTime || !bookingData.returnDateTime) {
        console.warn('WARNUNG: Buchungsdaten unvollständig nach Navigation von VehicleDetailsPage');
        console.log('VehicleId:', bookingData.vehicleId);
        console.log('PickupDateTime:', bookingData.pickupDateTime);
        console.log('ReturnDateTime:', bookingData.returnDateTime);
      }
    } else if (currentStep > 1 && !bookingData.vehicleId) {
      // Wenn User direkt zur Wizard-URL navigiert ohne Fahrzeug auszuwählen
      console.warn('WARNUNG: Direkter Zugriff auf Wizard ohne Fahrzeugauswahl - zurück zu Schritt 1');
      navigate('/vehicles', { 
        state: { message: 'Bitte wählen Sie zunächst ein Fahrzeug aus.' } 
      });
    }
  }, [location.state, bookingData, currentStep, navigate]);

  // Validierung für jeden Step
  const isStepValid = () => {
    switch (currentStep) {
      case 1:
        return bookingData.vehicleId && bookingData.vehicle;
      case 2:
        return (
          bookingData.pickupBranchId &&
          bookingData.returnBranchId &&
          bookingData.pickupBranch &&
          bookingData.returnBranch
        );
      case 3:
        return (
          bookingData.pickupDateTime &&
          bookingData.returnDateTime &&
          new Date(bookingData.returnDateTime) > new Date(bookingData.pickupDateTime)
        );
      case 4:
        // Step 4 (Extras): Prüfe ob Basisdaten vorhanden sind (für Direkteinstieg)
        return (
          bookingData.vehicleId &&
          bookingData.pickupDateTime &&
          bookingData.returnDateTime &&
          bookingData.pickupBranchId &&
          bookingData.returnBranchId
        );
      case 5:
        return true; // Letzter Step, Buchung wird in Step5 validiert
      default:
        return false;
    }
  };

  const handleNext = () => {
    if (isStepValid()) {
      nextStep();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      alert('Bitte füllen Sie alle Pflichtfelder aus.');
    }
  };

  const handlePrev = () => {
    prevStep();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Rendere aktuellen Step
  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return <WizardStep1 />;
      case 2:
        return <WizardStep2 />;
      case 3:
        return <WizardStep3 />;
      case 4:
        return <WizardStep4 />;
      case 5:
        return <WizardStep5 />;
      default:
        return <WizardStep1 />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Progress Bar */}
      <WizardProgressBar currentStep={currentStep} totalSteps={totalSteps} />

      {/* Main Content */}
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-5xl mx-auto">
          {/* Step Content */}
          <div className="mb-8">{renderStep()}</div>

          {/* Navigation Buttons */}
          {currentStep < 5 && (
            <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6">
              <div className="flex items-center justify-between gap-4">
                {/* Zurück Button */}
                <button
                  type="button"
                  onClick={handlePrev}
                  disabled={currentStep === 1}
                  className={`
                    flex items-center gap-2 px-6 py-3 rounded-lg font-medium transition-colors
                    ${
                      currentStep === 1
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    }
                  `}
                >
                  <span className="material-symbols-outlined">arrow_back</span>
                  Zurück
                </button>

                {/* Step Info (Mobile) */}
                <div className="text-center md:hidden">
                  <p className="text-sm text-gray-600">
                    Schritt {currentStep} von {totalSteps}
                  </p>
                </div>

                {/* Weiter Button */}
                <button
                  type="button"
                  onClick={handleNext}
                  disabled={!isStepValid()}
                  className={`
                    flex items-center gap-2 px-6 py-3 rounded-lg font-medium transition-colors
                    ${
                      isStepValid()
                        ? 'bg-[#1976D2] text-white hover:bg-[#1565C0] shadow-lg'
                        : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    }
                  `}
                >
                  Weiter
                  <span className="material-symbols-outlined">arrow_forward</span>
                </button>
              </div>

              {/* Validierungs-Hinweis */}
              {!isStepValid() && (
                <div className="mt-4 bg-amber-50 border border-amber-200 rounded-lg p-3">
                  <div className="flex gap-2">
                    <span className="material-symbols-outlined text-amber-600 text-sm">
                      warning
                    </span>
                    <p className="text-sm text-amber-800">
                      Bitte vervollständigen Sie alle erforderlichen Angaben, um fortzufahren.
                    </p>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Abbrechen Link */}
          <div className="text-center mt-6">
            <button
              type="button"
              onClick={() => {
                if (window.confirm('Möchten Sie den Buchungsvorgang wirklich abbrechen?')) {
                  navigate('/vehicles');
                }
              }}
              className="text-sm text-gray-600 hover:text-gray-900 underline"
            >
              Buchung abbrechen
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingWizardPage;
