import React from 'react';
import './Dashboard.css';

function Dashboard() {
    // TODO: Diese Daten später vom Backend holen (GET /api/reports/financial)
    const financialData = {
        totalRevenue: 12450,
        totalExpense: 8300,
    };

    const profit = financialData.totalRevenue - financialData.totalExpense;

    // TODO: Diese Daten später vom Backend holen (GET /api/reports/revenue-by-category)
    const revenueBreakdown = [
        { category: "Vegetarisch", amount: 4200 },
        { category: "Vegan", amount: 3800 },
        { category: "Halal", amount: 2650 },
        { category: "Glutenfrei", amount: 1800 }
    ];

    // TODO: Diese Daten später vom Backend holen (GET /api/reports/expenses-by-category)
    const expenseBreakdown = [
        { category: "Gemüse & Obst", amount: 2100 },
        { category: "Fleisch & Fisch", amount: 2400 },
        { category: "Milchprodukte", amount: 1500 },
        { category: "Getreide & Teigwaren", amount: 1200 },
        { category: "Gewürze & Sonstiges", amount: 1100 }
    ];

    return (
        <div className="dashboard-container">
            <h1 className="dashboard-title">Einnahmen & Kosten Dashboard</h1>

            {/* Übersicht */}
            <section className="section">
                <h2 className="section-title">Monatliche Übersicht</h2>
                <div className="cards-container">
                    <div className="card card-revenue">
                        <h3>Einnahmen</h3>
                        <p className="amount">{financialData.totalRevenue.toLocaleString('de-DE')} €</p>
                    </div>
                    <div className="card card-expense">
                        <h3>Kosten</h3>
                        <p className="amount">{financialData.totalExpense.toLocaleString('de-DE')} €</p>
                    </div>
                    <div className="card card-profit">
                        <h3>Gewinn</h3>
                        <p className="amount">{profit.toLocaleString('de-DE')} €</p>
                    </div>                </div>            </section>
            {/* Einnahmen nach Gericht-Kategorie */}
            <section className="section">
                <h2 className="section-title">Einnahmen nach Gericht-Kategorie</h2>
                <div className="table-container">
                    <table className="data-table">
                        <thead>                        <tr>                            <th>Kategorie</th>
                            <th>Betrag</th>
                            <th>Anteil</th>
                        </tr>                        </thead>                        <tbody>                        {revenueBreakdown.map((item, index) => (
                        <tr key={index}>
                            <td>{item.category}</td>
                            <td className="amount-cell">{item.amount.toLocaleString('de-DE')} €</td>
                            <td>{((item.amount / financialData.totalRevenue) * 100).toFixed(1)}%</td>
                        </tr>                        ))}
                    </tbody>
                    </table>                </div>            </section>
            {/* Kosten nach Zutaten-Kategorie */}
            <section className="section">
                <h2 className="section-title">Kosten nach Zutaten-Kategorie</h2>
                <div className="table-container">
                    <table className="data-table">
                        <thead>                        <tr>                            <th>Kategorie</th>
                            <th>Betrag</th>
                            <th>Anteil</th>
                        </tr>                        </thead>                        <tbody>                        {expenseBreakdown.map((item, index) => (
                        <tr key={index}>
                            <td>{item.category}</td>
                            <td className="amount-cell">{item.amount.toLocaleString('de-DE')} €</td>
                            <td>{((item.amount / financialData.totalExpense) * 100).toFixed(1)}%</td>
                        </tr>                        ))}
                    </tbody>
                    </table>                </div>            </section>
        </div>    );
}

export default Dashboard;