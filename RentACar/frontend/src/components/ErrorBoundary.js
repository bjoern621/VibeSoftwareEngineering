import React from 'react';

/**
 * Error Boundary Komponente für das Abfangen von React-Laufzeitfehlern.
 * Zeigt eine benutzerfreundliche Fehlerseite anstelle des Absturzes der gesamten App.
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error) {
    // State aktualisieren, damit die nächste Render-Phase die Fallback-UI anzeigt
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // Fehler-Details für Debugging speichern
    this.setState({
      error,
      errorInfo,
    });

    // In Produktion: Fehler an Error-Tracking-Service senden (z.B. Sentry)
    console.error('Error Boundary hat einen Fehler abgefangen:', error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  handleGoHome = () => {
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center p-4">
          <div className="max-w-2xl w-full bg-white rounded-2xl shadow-xl p-8 md:p-12">
            {/* Icon */}
            <div className="flex justify-center mb-6">
              <div className="w-24 h-24 bg-red-100 rounded-full flex items-center justify-center">
                <span className="material-symbols-outlined text-6xl text-red-500">error</span>
              </div>
            </div>

            {/* Titel */}
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 text-center mb-4">
              Ups! Etwas ist schiefgelaufen
            </h1>

            {/* Beschreibung */}
            <p className="text-gray-600 text-center mb-8 text-lg">
              Ein unerwarteter Fehler ist aufgetreten. Bitte versuchen Sie, die Seite neu zu laden
              oder kehren Sie zur Startseite zurück.
            </p>

            {/* Fehlerdetails nur in DEV + Console (NICHT im Browser-UI) */}
            {process.env.NODE_ENV === 'development' && this.state.error && (
              <div className="hidden">
                {/* Error logged to console only - siehe componentDidCatch */}
              </div>
            )}

            {/* Aktionen */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={this.handleReload}
                className="bg-primary text-white px-8 py-3 rounded-lg hover:bg-primary-dark transition-colors duration-200 flex items-center justify-center gap-2 font-medium"
              >
                <span className="material-symbols-outlined">refresh</span>
                Seite neu laden
              </button>
              <button
                onClick={this.handleGoHome}
                className="bg-gray-200 text-gray-800 px-8 py-3 rounded-lg hover:bg-gray-300 transition-colors duration-200 flex items-center justify-center gap-2 font-medium"
              >
                <span className="material-symbols-outlined">home</span>
                Zur Startseite
              </button>
            </div>

            {/* Support-Hinweis */}
            <div className="mt-8 pt-6 border-t border-gray-200 text-center">
              <p className="text-sm text-gray-500">
                Falls das Problem weiterhin besteht, kontaktieren Sie bitte unseren Support.
              </p>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
