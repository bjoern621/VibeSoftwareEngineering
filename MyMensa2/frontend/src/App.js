import React, { useState } from 'react';
import './App.css';
import OrderManagement from './components/OrderManagement';
import AdminPanel from './components/AdminPanel';
import AdminOrderManagement from './components/AdminOrderManagement';
import MobileRoleSelector from './components/MobileRoleSelector';

/**
 * Haupt-App-Komponente für MyMensa2
 * Verwaltet die Navigation zwischen:
 * - Desktop: Kundenansicht (OrderManagement) und Admin-Portal (AdminPanel)
 * - Mobile: iPhone-Frame mit Kunden-Bestellung oder Mitarbeiter-QR-Validierung
 */
function App() {
  const [view, setView] = useState('order'); // 'order' oder 'admin'
  const [mobileView, setMobileView] = useState(false); // Mobile Ansicht Toggle (iPhone-Frame)
  const [showMobileSelector, setShowMobileSelector] = useState(false); // Modal für Rollenauswahl
  const [mobileRole, setMobileRole] = useState(null); // 'customer' oder 'staff'

  // Mobile-Button wurde geklickt
  const handleMobileButtonClick = () => {
    if (mobileView) {
      // Zurück zur Desktop-Ansicht
      setMobileView(false);
      setMobileRole(null);
      setView('order'); // Reset zur Bestellseite
    } else {
      // Zeige Rollenauswahl-Modal
      setShowMobileSelector(true);
    }
  };

  // Rolle wurde ausgewählt
  const handleRoleSelect = (role) => {
    setMobileRole(role);
    setMobileView(true);
    setShowMobileSelector(false);
    
    // Setze die richtige Ansicht basierend auf der Rolle
    if (role === 'customer') {
      setView('order'); // Bestellungen für Kunden
    } else if (role === 'staff') {
      setView('admin'); // Admin-Panel für Mitarbeiter (enthält QR-Validierung)
    }
  };

  return (
    <div className={`App ${mobileView ? 'mobile-view' : ''}`}>
      {/* Scrollbarer Content Container - Header scrollt MIT */}
      <div className={mobileView ? 'mobile-screen-content' : ''}>
        {/* Header mit Navigation */}
        <header className="app-header">
          <div className="header-content">
            <h1>🍽️ MyMensa</h1>
            <p className="header-subtitle">
              {mobileView && mobileRole === 'customer' && '👨‍🎓 Kunde - Mobile Ansicht'}
              {mobileView && mobileRole === 'staff' && '👔 Mitarbeiter - Mobile Ansicht'}
              {!mobileView && 'Willkommen beim digitalen Bestellsystem'}
            </p>
          </div>
          
          <div className="header-nav">
            {/* Navigation nur im Desktop-Modus anzeigen */}
            {!mobileView && (
              <>
                <button 
                  className={`nav-button ${view === 'order' ? 'active' : ''}`}
                  onClick={() => setView('order')}
                >
                  🍽️ Bestellungen
                </button>
                <button 
                  className={`nav-button ${view === 'admin' ? 'active' : ''}`}
                  onClick={() => setView('admin')}
                >
                  ⚙️ Verwaltung
                </button>
              </>
            )}
            
            {/* Mobile View Toggle Button */}
            <button 
              className={`mobile-toggle-button ${mobileView ? 'active' : ''}`}
              onClick={handleMobileButtonClick}
              title={mobileView ? 'Desktop-Ansicht' : 'Mobile-Ansicht'}
            >
              {mobileView ? '🖥️ Desktop' : '📱 Mobile'}
            </button>
          </div>
        </header>

        {/* Hauptinhalt */}
        <main className="app-main">
          {/* Mobile Mitarbeiter: Nur QR-Validierung */}
          {mobileView && mobileRole === 'staff' ? (
            <AdminOrderManagement />
          ) : (
            /* Normal: Bestellungen oder Admin-Panel */
            view === 'order' ? <OrderManagement /> : <AdminPanel />
          )}
        </main>
      </div>

      {/* Mobile Rollenauswahl Modal */}
      {showMobileSelector && (
        <MobileRoleSelector
          onSelectRole={handleRoleSelect}
          onClose={() => setShowMobileSelector(false)}
        />
      )}
    </div>
  );
}

export default App;
