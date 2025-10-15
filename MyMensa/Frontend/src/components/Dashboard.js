import React, { useState, useEffect } from 'react';
import './Dashboard.css';

function Dashboard() {
    const [financialData, setFinancialData] = useState({ totalRevenue: 0, totalExpense: 0 });
    const [revenueBreakdown, setRevenueBreakdown] = useState([]);
    const [expenseBreakdown, setExpenseBreakdown] = useState([]);
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
            // API-Calls parallel ausführen
            const [financialResponse, revenueResponse, expensesResponse] = await Promise.all([
                fetch('http://localhost:8080/api/reports/financial'),
                fetch('http://localhost:8080/api/reports/revenue-by-category'),
                fetch('http://localhost:8080/api/reports/expenses-by-category')
            ]);

            // Fehlerbehandlung
            if (!financialResponse.ok || !revenueResponse.ok || !expensesResponse.ok) {
                throw new Error('Fehler beim Laden der Dashboard-Daten');
            }

            // JSON parsen
            const financial = await financialResponse.json();
            const revenue = await revenueResponse.json();
            const expenses = await expensesResponse.json();

            // State aktualisieren
            setFinancialData(financial);
            setRevenueBreakdown(revenue);
            setExpenseBreakdown(expenses);

        } catch (err) {
            console.error('Fehler beim Laden der Dashboard-Daten:', err);
            setError('Fehler beim Laden der Daten. Bitte versuche es später erneut.');
        } finally {
            setLoading(false);
        }
    };

    const profit = financialData.totalRevenue - financialData.totalExpense;

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
                            {profit.toLocaleString('de-DE', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            })} €
                        </p>
                    </div>
                </div>
            </section>

            {/* Einnahmen nach Gericht-Kategorie */}
            <section className="section">
                <h2 className="section-title">Einnahmen nach Gericht-Kategorie</h2>
                <div className="table-container">
                    <table className="data-table">
                        <thead>
                        <tr>
                            <th>Kategorie</th>
                            <th>Betrag</th>
                            <th>Anteil</th>
                        </tr>
                        </thead>
                        <tbody>
                        {revenueBreakdown.length === 0 ? (
                            <tr>
                                <td colSpan="3" className="text-center text-muted">
                                    Keine Daten verfügbar
                                </td>
                            </tr>
                        ) : (
                            revenueBreakdown.map((item, index) => (
                                <tr key={index}>
                                    <td>{item.category}</td>
                                    <td className="amount-cell">
                                        {item.amount.toLocaleString('de-DE', {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2
                                        })} €
                                    </td>
                                    <td>
                                        {financialData.totalRevenue > 0
                                            ? ((item.amount / financialData.totalRevenue) * 100).toFixed(1)
                                            : 0
                                        }%
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </section>

            {/* Kosten nach Zutaten-Kategorie */}
            <section className="section">
                <h2 className="section-title">Kosten nach Zutaten-Kategorie</h2>
                <div className="table-container">
                    <table className="data-table">
                        <thead>
                        <tr>
                            <th>Kategorie</th>
                            <th>Betrag</th>
                            <th>Anteil</th>
                        </tr>
                        </thead>
                        <tbody>
                        {expenseBreakdown.length === 0 ? (
                            <tr>
                                <td colSpan="3" className="text-center text-muted">
                                    Keine Daten verfügbar
                                </td>
                            </tr>
                        ) : (
                            expenseBreakdown.map((item, index) => (
                                <tr key={index}>
                                    <td>{item.category}</td>
                                    <td className="amount-cell">
                                        {item.amount.toLocaleString('de-DE', {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2
                                        })} €
                                    </td>
                                    <td>
                                        {financialData.totalExpense > 0
                                            ? ((item.amount / financialData.totalExpense) * 100).toFixed(1)
                                            : 0
                                        }%
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    );
}

export default Dashboard;
