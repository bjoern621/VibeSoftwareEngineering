import React, { useState, useEffect } from 'react';
import './Dashboard.css';

function Dashboard() {
    const [financialData, setFinancialData] = useState({ totalRevenue: 0, totalExpense: 0, profit: 0 });
    const [mealStats, setMealStats] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    /**
     * Lädt Dashboard-Daten beim Komponenten-Start
     */
    useEffect(() => {
        loadDashboardData();
    }, []);

    /**
     * Lädt alle Dashboard-Daten vom Backend
     */
    const loadDashboardData = async () => {
        setLoading(true);
        setError(null);

        try {
            // API-Call zum Backend
            const response = await fetch('http://localhost:8080/api/dashboard');

            // Fehlerbehandlung
            if (!response.ok) {
                throw new Error('Fehler beim Laden der Dashboard-Daten');
            }

            // JSON parsen
            const data = await response.json();

            // State aktualisieren mit Backend-Daten
            setFinancialData({
                totalRevenue: data.totalRevenue,
                totalExpense: data.totalExpenses,
                profit: data.profit
            });
            setMealStats(data.mealStats);

        } catch (err) {
            console.error('Fehler beim Laden der Dashboard-Daten:', err);
            setError('Fehler beim Laden der Daten. Bitte versuche es später erneut.');
        } finally {
            setLoading(false);
        }
    };

    // Loading State
    if (loading) {
        return (
            <div className="dashboard-container">
                <div className="text-center">
                    <div className="loading-spinner"></div>
                    <p className="text-muted mt-small">Lade Dashboard...</p>
                </div>
            </div>
        );
    }

    // Error State
    if (error) {
        return (
            <div className="dashboard-container">
                <div className="error-message">
                    {error}
                </div>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            <h1 className="dashboard-title">Einnahmen & Kosten Dashboard</h1>

            {/* Übersicht */}
            <section className="section">
                <h2 className="section-title">Monatliche Übersicht</h2>
                <div className="cards-container">
                    <div className="card card-revenue">
                        <h3>Einnahmen</h3>
                        <p className="amount">
                            {financialData.totalRevenue.toLocaleString('de-DE', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            })} €
                        </p>
                    </div>
                    <div className="card card-expense">
                        <h3>Kosten</h3>
                        <p className="amount">
                            {financialData.totalExpense.toLocaleString('de-DE', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            })} €
                        </p>
                    </div>
                    <div className="card card-profit">
                        <h3>Gewinn</h3>
                        <p className="amount">
                            {financialData.profit.toLocaleString('de-DE', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            })} €
                        </p>
                    </div>
                </div>
            </section>

            {/* Kombinierte Gerichte-Übersicht */}
            <section className="section">
                <h2 className="section-title">Gerichte Übersicht</h2>
                <div className="table-container">
                    <table className="data-table">
                        <thead>
                        <tr>
                            <th>Gericht</th>
                            <th>Verkauft</th>
                            <th>Einnahmen</th>
                            <th>Ausgaben</th>
                            <th>Gewinn</th>
                        </tr>
                        </thead>
                        <tbody>
                        {mealStats.length === 0 ? (
                            <tr>
                                <td colSpan="5" className="text-center text-muted">
                                    Keine Daten verfügbar
                                </td>
                            </tr>
                        ) : (
                            mealStats.map((meal, index) => {
                                const profit = meal.totalRevenue - meal.totalExpenses;
                                return (
                                    <tr key={index}>
                                        <td>{meal.mealName}</td>
                                        <td>{meal.quantitySold}x</td>
                                        <td className="amount-cell">
                                            {meal.totalRevenue.toLocaleString('de-DE', {
                                                minimumFractionDigits: 2,
                                                maximumFractionDigits: 2
                                            })} €
                                        </td>
                                        <td className="amount-cell">
                                            {meal.totalExpenses.toLocaleString('de-DE', {
                                                minimumFractionDigits: 2,
                                                maximumFractionDigits: 2
                                            })} €
                                        </td>
                                        <td className="amount-cell">
                                            {profit.toLocaleString('de-DE', {
                                                minimumFractionDigits: 2,
                                                maximumFractionDigits: 2
                                            })} €
                                        </td>
                                    </tr>
                                );
                            })
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    );
}

export default Dashboard;
