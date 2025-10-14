import React, { useState, useEffect } from 'react';
import './OrderManagement.css';

/**
 * OrderManagement Komponente - Essen vorbestellen und bezahlen
 *
 * Funktionen:
 * - Anzeige verfügbarer Gerichte aus dem Speiseplan
 * - Gerichte in den Warenkorb legen
 * - Bestellung aufgeben und bezahlen
 * - QR-Code nach erfolgreicher Bezahlung anzeigen
 *
 * Hinweis: Backend-APIs für Orders müssen noch definiert werden
 * Aktuell wird der Speiseplan von GET /api/meal-plans geladen
 */
function OrderManagement() {
    const [selectedDate, setSelectedDate] = useState(new Date());
    const [availableMeals, setAvailableMeals] = useState([]);
    const [cart, setCart] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showCheckout, setShowCheckout] = useState(false);
    const [orderComplete, setOrderComplete] = useState(false);
    const [qrCode, setQrCode] = useState(null);

    // Verfügbare Daten für die nächsten 7 Tage
    const getAvailableDates = () => {
        const dates = [];
        const today = new Date();
        for (let i = 0; i < 7; i++) {
            const date = new Date(today);
            date.setDate(today.getDate() + i);
            // Nur Wochentage (Mo-Fr)
            if (date.getDay() !== 0 && date.getDay() !== 6) {
                dates.push(date);
            }
        }
        return dates;
    };

    const formatDate = (date) => {
        return date.toLocaleDateString('de-DE', {
            weekday: 'long',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };

    const formatDateForAPI = (date) => {
        return date.toISOString().split('T')[0];
    };

    // Speiseplan für ausgewähltes Datum laden
    const loadMealsForDate = async () => {
        setLoading(true);
        try {
            const dateString = formatDateForAPI(selectedDate);
            // Lade Speiseplan für den ausgewählten Tag
            const response = await fetch(`http://localhost:8080/api/meal-plans?startDate=${dateString}&endDate=${dateString}`);
            if (!response.ok) throw new Error('Fehler beim Laden');

            const data = await response.json();
            if (data.length > 0 && data[0].meals) {
                const meals = data[0].meals.map(item => ({
                    ...item.meal,
                    availableStock: item.stock
                }));
                setAvailableMeals(meals);
            } else {
                setAvailableMeals([]);
            }
        } catch (err) {
            console.error('Fehler beim Laden:', err);
            setAvailableMeals([]);
        } finally {
            setLoading(false);
        }
    };

    // Gericht zum Warenkorb hinzufügen
    const addToCart = (meal) => {
        const existingItem = cart.find(item => item.id === meal.id);
        if (existingItem) {
            // Menge erhöhen
            if (existingItem.quantity < meal.availableStock) {
                setCart(cart.map(item =>
                    item.id === meal.id
                        ? { ...item, quantity: item.quantity + 1 }
                        : item
                ));
            } else {
                alert('Maximale verfügbare Menge erreicht!');
            }
        } else {
            // Neues Item hinzufügen
            setCart([...cart, { ...meal, quantity: 1 }]);
        }
    };

    // Gericht aus Warenkorb entfernen
    const removeFromCart = (mealId) => {
        setCart(cart.filter(item => item.id !== mealId));
    };

    // Menge im Warenkorb ändern
    const updateQuantity = (mealId, newQuantity) => {
        const meal = cart.find(item => item.id === mealId);
        if (newQuantity > meal.availableStock) {
            alert('Maximale verfügbare Menge erreicht!');
            return;
        }
        if (newQuantity < 1) {
            removeFromCart(mealId);
            return;
        }
        setCart(cart.map(item =>
            item.id === mealId
                ? { ...item, quantity: newQuantity }
                : item
        ));
    };

    // Gesamtpreis berechnen
    const calculateTotal = () => {
        return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    };

    // Bestellung aufgeben (Bezahlung simuliert)
    const placeOrder = async () => {
        if (cart.length === 0) {
            alert('Warenkorb ist leer!');
            return;
        }

        setLoading(true);
        try {
            // TODO: Backend-API für Bestellungen implementieren
            // const response = await fetch('http://localhost:8080/api/orders', {
            //     method: 'POST',
            //     headers: { 'Content-Type': 'application/json' },
            //     body: JSON.stringify({
            //         date: formatDateForAPI(selectedDate),
            //         items: cart.map(item => ({ mealId: item.id, quantity: item.quantity }))
            //     })
            // });

            // Simuliere erfolgreiche Bestellung
            await new Promise(resolve => setTimeout(resolve, 1500));

            // Generiere Mock QR-Code (in Realität vom Backend)
            const mockQrCode = `ORDER-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
            setQrCode(mockQrCode);
            setOrderComplete(true);
            setCart([]);
            setShowCheckout(false);
        } catch (err) {
            alert('Fehler beim Aufgeben der Bestellung');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadMealsForDate();
    }, [selectedDate]);

    const availableDates = getAvailableDates();

    if (orderComplete) {
        return (
            <div className="order-complete-container">
                <div className="success-card">
                    <div className="success-icon">✅</div>
                    <h2>Bestellung erfolgreich!</h2>
                    <p>Deine Bestellung wurde erfolgreich aufgegeben und bezahlt.</p>

                    <div className="qr-code-section">
                        <h3>Dein QR-Code zur Abholung:</h3>
                        <div className="qr-code-placeholder">
                            <p className="qr-text">{qrCode}</p>
                            <small>Zeige diesen Code bei der Essensausgabe vor</small>
                        </div>
                    </div>

                    <div className="order-details">
                        <p><strong>Abholdatum:</strong> {formatDate(selectedDate)}</p>
                        <p><strong>Gesamtbetrag:</strong> {calculateTotal().toFixed(2)} €</p>
                    </div>

                    <button
                        className="btn btn-primary"
                        onClick={() => {
                            setOrderComplete(false);
                            setQrCode(null);
                        }}
                    >
                        Neue Bestellung aufgeben
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="order-management-container">
            {/* Header */}
            <div className="gradient-header">
                <h1 className="week-title">🍽️ Essen vorbestellen</h1>
                <p>Wähle ein Datum und bestelle dein Essen vor</p>
            </div>

            {/* Datumswahl */}
            <div className="date-selection">
                <label>Datum auswählen:</label>
                <div className="date-buttons">
                    {availableDates.map((date, index) => (
                        <button
                            key={index}
                            className={`date-btn ${formatDateForAPI(date) === formatDateForAPI(selectedDate) ? 'active' : ''}`}
                            onClick={() => setSelectedDate(date)}
                        >
                            {date.toLocaleDateString('de-DE', { weekday: 'short', day: '2-digit', month: '2-digit' })}
                        </button>
                    ))}
                </div>
            </div>

            <div className="order-content">
                {/* Verfügbare Gerichte */}
                <div className="meals-section">
                    <h2>Verfügbare Gerichte für {formatDate(selectedDate)}</h2>

                    {loading && <div className="loading-spinner"></div>}

                    {!loading && availableMeals.length === 0 && (
                        <div className="empty-message">
                            Für diesen Tag sind noch keine Gerichte verfügbar.
                        </div>
                    )}

                    <div className="meals-grid">
                        {availableMeals.map(meal => (
                            <div key={meal.id} className="meal-card">
                                <h3>{meal.name}</h3>
                                <p className="meal-description">{meal.description}</p>

                                {meal.categories && meal.categories.length > 0 && (
                                    <div className="meal-tags">
                                        {meal.categories.map((cat, idx) => (
                                            <span key={idx} className="badge badge-success">{cat}</span>
                                        ))}
                                    </div>
                                )}

                                {meal.allergens && meal.allergens.length > 0 && (
                                    <div className="meal-allergens">
                                        <small className="text-danger">⚠️ {meal.allergens.join(', ')}</small>
                                    </div>
                                )}

                                <div className="meal-footer">
                                    <span className="meal-price">{meal.price.toFixed(2)} €</span>
                                    <span className="meal-stock">📦 {meal.availableStock} verfügbar</span>
                                    <button
                                        className="btn btn-primary btn-sm"
                                        onClick={() => addToCart(meal)}
                                        disabled={meal.availableStock === 0}
                                    >
                                        ➕ In Warenkorb
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Warenkorb */}
                <div className="cart-section">
                    <h2>🛒 Warenkorb</h2>

                    {cart.length === 0 ? (
                        <div className="empty-cart">
                            <p>Dein Warenkorb ist leer</p>
                        </div>
                    ) : (
                        <>
                            <div className="cart-items">
                                {cart.map(item => (
                                    <div key={item.id} className="cart-item">
                                        <div className="cart-item-info">
                                            <h4>{item.name}</h4>
                                            <p className="cart-item-price">{item.price.toFixed(2)} € / Stück</p>
                                        </div>
                                        <div className="cart-item-controls">
                                            <button
                                                className="btn-quantity"
                                                onClick={() => updateQuantity(item.id, item.quantity - 1)}
                                            >
                                                −
                                            </button>
                                            <span className="quantity">{item.quantity}</span>
                                            <button
                                                className="btn-quantity"
                                                onClick={() => updateQuantity(item.id, item.quantity + 1)}
                                            >
                                                +
                                            </button>
                                            <button
                                                className="btn-remove"
                                                onClick={() => removeFromCart(item.id)}
                                            >
                                                🗑️
                                            </button>
                                        </div>
                                        <div className="cart-item-total">
                                            {(item.price * item.quantity).toFixed(2)} €
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <div className="cart-summary">
                                <div className="total">
                                    <span>Gesamtbetrag:</span>
                                    <span className="total-amount">{calculateTotal().toFixed(2)} €</span>
                                </div>
                                <button
                                    className="btn btn-success btn-large"
                                    onClick={placeOrder}
                                    disabled={loading}
                                >
                                    {loading ? '⏳ Wird bestellt...' : '💳 Jetzt bestellen & bezahlen'}
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}

export default OrderManagement;

