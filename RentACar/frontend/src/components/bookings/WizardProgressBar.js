import React from 'react';

const WizardProgressBar = ({ currentStep, totalSteps }) => {
  const steps = [
    { number: 1, name: 'Fahrzeug' },
    { number: 2, name: 'Filialen' },
    { number: 3, name: 'Zeitraum' },
    { number: 4, name: 'Extras' },
    { number: 5, name: 'Zusammenfassung' },
  ];

  return (
    <div className="sticky top-16 z-40 bg-white shadow-sm border-b border-gray-200">
      <div className="container mx-auto px-4 py-4">
        <div className="max-w-5xl mx-auto">
          {/* Mobile: Einfache Step-Anzeige */}
          <div className="md:hidden">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-600">
                Schritt {currentStep} von {totalSteps}
              </span>
              <span className="text-sm font-bold text-[#1976D2]">
                {steps[currentStep - 1]?.name}
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-[#1976D2] h-2 rounded-full transition-all duration-300"
                style={{ width: `${(currentStep / totalSteps) * 100}%` }}
              />
            </div>
          </div>

          {/* Desktop: Detaillierte Step-Anzeige */}
          <div className="hidden md:block">
            <div className="flex items-center justify-between">
              {steps.map((step, index) => (
                <React.Fragment key={step.number}>
                  {/* Step Circle */}
                  <div className="flex flex-col items-center flex-shrink-0">
                    <div
                      className={`
                        w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm
                        transition-all duration-300
                        ${
                          step.number < currentStep
                            ? 'bg-[#1976D2] text-white'
                            : step.number === currentStep
                            ? 'bg-[#1976D2] text-white ring-4 ring-blue-100'
                            : 'bg-gray-200 text-gray-500'
                        }
                      `}
                    >
                      {step.number < currentStep ? (
                        <span className="material-symbols-outlined text-lg">
                          check
                        </span>
                      ) : (
                        step.number
                      )}
                    </div>
                    <span
                      className={`
                        mt-2 text-xs font-medium transition-colors duration-300
                        ${
                          step.number === currentStep
                            ? 'text-[#1976D2]'
                            : step.number < currentStep
                            ? 'text-gray-700'
                            : 'text-gray-400'
                        }
                      `}
                    >
                      {step.name}
                    </span>
                  </div>

                  {/* Connector Line */}
                  {index < steps.length - 1 && (
                    <div className="flex-grow h-1 mx-4 relative top-[-10px]">
                      <div
                        className={`
                          h-full rounded transition-all duration-300
                          ${
                            step.number < currentStep
                              ? 'bg-[#1976D2]'
                              : 'bg-gray-200'
                          }
                        `}
                      />
                    </div>
                  )}
                </React.Fragment>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WizardProgressBar;
