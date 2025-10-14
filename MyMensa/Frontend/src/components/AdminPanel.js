
import React, { useState } from 'react';
import './AdminPanel.css';
import MealManagement from './MealManagement';
import MealPlanManagement from './MealPlanManagement';
import Dashboard from './Dashboard';

/**
 * AdminPanel - Verwaltungsbereich für die Mensa
 *
 * Kapselt alle Verwaltungsfunktionen:
 * - Gerichteverwaltung
 * - Speiseplanverwaltung
 * - Dashboard (Einnahmen/Kosten)
 */
function AdminPanel({ onExit }) {
    const [currentView, setCurrentView] = useState('meals');

    return (
        <div className="admin-panel">
            {/* Admin Header */}
            <div className="admin-header">
                <div className="admin-title-section">
                    <h1>🔧 Verwaltungsbereich</h1>
                    <p className="admin-subtitle">Mensa-Management & Kontrolle</p>
                </div>
                <button className="btn-exit" onClick={onExit}>
                    ← Zurück zur Hauptansicht
                </button>
            </div>

            {/* Admin Navigation */}
            <nav className="admin-nav">
                <button
                    className={`admin-nav-btn ${currentView === 'meals' ? 'active' : ''}`}
                    onClick={() => setCurrentView('meals')}
                >
                    🍲 Gerichteverwaltung
                </button>
                <button
                    className={`admin-nav-btn ${currentView === 'mealplan' ? 'active' : ''}`}
                    onClick={() => setCurrentView('mealplan')}
                >
                    📝 Speiseplan verwalten
                </button>
                <button
                    className={`admin-nav-btn ${currentView === 'dashboard' ? 'active' : ''}`}
                    onClick={() => setCurrentView('dashboard')}
                >
                    📊 Dashboard
                </button>
            </nav>

            {/* Admin Content */}
            <div className="admin-content">
                {currentView === 'meals' && <MealManagement />}
                {currentView === 'mealplan' && <MealPlanManagement />}
                {currentView === 'dashboard' && <Dashboard />}
            </div>
        </div>
    );
}

export default AdminPanel;