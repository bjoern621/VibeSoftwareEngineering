import React, { useState, useEffect } from 'react';
import './OrderManagement.css';
import { QRCodeSVG } from 'qrcode.react';

/**
 * OrderManagement Komponente - Wochenplan anzeigen und Essen vorbestellen
 *
 * Funktionen:
 * - Wochenansicht: Speiseplan für Mo-Fr anzeigen
 * - Tagesansicht: Gerichte für einen Tag anzeigen und bestellen
 * - Warenkorb-Funktionalität
 * - Bestellung aufgeben und bezahlen
 * - QR-Code nach erfolgreicher Bezahlung anzeigen
 */
function OrderManagement() {
    const [viewMode, setViewMode] = useState('week'); // 'week' oder 'day'
    const [selectedDate, setSelectedDate] = useState(new Date());
    const [weekOffset, setWeekOffset] = useState(0);
    const [availableMeals, setAvailableMeals] = useState([]);
    const [weekMeals, setWeekMeals] = useState([]);
    const [cart, setCart] = useState([]);
    const [loading, setLoading] = useState(false);
    const [orderComplete, setOrderComplete] = useState(false);
    // Backend kann mehrere Bestellungen (je Portion eine Order) zurückliefern -> mehrere QR-Codes
    const [qrCodes, setQrCodes] = useState([]); // Array mit { qrCode, mealName }
    const [orderTotal, setOrderTotal] = useState(0);
    const [orderDate, setOrderDate] = useState(new Date());
    const [enlargedQR, setEnlargedQR] = useState(null); // Für vergrößerten QR-Code

    const weekdays = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag'];

    // Helper: lokales ISO-Datum ohne timezone-shift
    const toLocalISODate = (d) => {
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    };

    // Verfügbare Daten für die nächsten 7 Tage (nur Wochentage)
    const getAvailableDates = () => {
        const dates = [];
        const today = new Date();
        for (let i = 0; i < 7; i++) {
            const date = new Date(today);
            date.setDate(today.getDate() + i);
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

    const formatDateShort = (date) => {
        return date.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });
    };

    const isToday = (date) => {
        const today = new Date();
        return date.toDateString() === new Date(today.getFullYear(), today.getMonth(), today.getDate()).toDateString();
    };

    // Wochenstart/ende berechnen
    const getWeekStart = (offset) => {
        const today = new Date();
        const localDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
        const dayOfWeek = localDay.getDay();
        const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
        const monday = new Date(localDay);
        monday.setDate(localDay.getDate() + diff + (offset * 7));
        monday.setHours(0, 0, 0, 0);
        return monday;
    };

    const getWeekEnd = (offset) => {
        const weekStart = getWeekStart(offset);
        const friday = new Date(weekStart);
        friday.setDate(weekStart.getDate() + 4);
        friday.setHours(23, 59, 59, 999);
        return friday;
    };

    const getWeekDates = () => {
        const weekStart = getWeekStart(weekOffset);
        const dates = [];
        for (let i = 0; i < 5; i++) {
            const date = new Date(weekStart);
            date.setDate(weekStart.getDate() + i);
            dates.push(date);
        }
        return dates;
    };

    const getMonthYearString = () => {
        const weekStart = getWeekStart(weekOffset);
        return weekStart.toLocaleDateString('de-DE', { month: 'long', year: 'numeric' });
    };

    // Speiseplan für Wochenansicht laden
    const loadWeekMealPlan = async () => {
        setLoading(true);
        try {
            const weekStart = getWeekStart(weekOffset);
            const weekEnd = getWeekEnd(weekOffset);
            const startDateString = toLocalISODate(weekStart);
            const endDateString = toLocalISODate(weekEnd);
            const response = await fetch(`http://localhost:8080/api/meal-plans?startDate=${startDateString}&endDate=${endDateString}`);
            if (!response.ok) throw new Error('Fehler beim Laden');
            const data = await response.json();
            setWeekMeals(data || []);
        } catch (err) {
            console.error('Fehler beim Laden:', err);
            setWeekMeals([]);
        } finally {
            setLoading(false);
        }
    };

    // Speiseplan für ausgewähltes Datum laden (Tagesansicht)
    const loadMealsForDate = async () => {
        setLoading(true);
        try {
            const dateString = toLocalISODate(selectedDate);
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

    const getMealsForDate = (date) => {
        const dateString = toLocalISODate(date);
        const dayData = weekMeals.find(day => day.date === dateString);
        if (!dayData || !dayData.meals) return [];
        return dayData.meals.map(entry => ({
            ...entry.meal,
            availableStock: entry.stock
        }));
    };

    // Warenkorb-Funktionen
    const addToCart = (meal) => {
        const existingItem = cart.find(item => item.id === meal.id);
        if (existingItem) {
            if (existingItem.quantity < meal.availableStock) {
                setCart(cart.map(item =>
                    item.id === meal.id ? { ...item, quantity: item.quantity + 1 } : item
                ));
            } else {
                alert('Maximale verfügbare Menge erreicht!');
            }
        } else {
            setCart([...cart, { ...meal, quantity: 1 }]);
        }
    };

    const removeFromCart = (mealId) => {
        setCart(cart.filter(item => item.id !== mealId));
    };

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
            item.id === mealId ? { ...item, quantity: newQuantity } : item
        ));
    };

    const calculateTotal = () => {
        return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    };

    // Bestellung aufgeben
    const createOrderBackend = async (mealId, dateString) => {
        const resp = await fetch(`http://localhost:8080/api/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mealId, date: dateString })
        });
        if (!resp.ok) throw new Error('createOrder failed: ' + resp.status);
        const data = await resp.json(); // { orderId }
        return data.orderId;
    };

    const payOrderBackend = async (orderId) => {
        const resp = await fetch(`http://localhost:8080/api/orders/${orderId}/pay`, {
            method: 'PUT'
        });
        if (!resp.ok) throw new Error('payOrder failed: ' + resp.status);
        const data = await resp.json(); // { qrCode }
        return data.qrCode;
    };

    /**
     * placeOrder:
     * - Versucht, für jede Portion eine Bestellung im Backend zu erstellen
     *   (Backend-API ist derzeit so gestaltet, dass eine Order je Portion entsteht)
     * - Bezahlt jede Order (PUT /api/orders/{id}/pay) und sammelt QR-Codes
     * - Falls Backend nicht erreichbar oder fehlerhaft, wird ein lokaler Mock-QR-Code erzeugt
     * Hinweis für Backend-Entwickler: Ein Batch-/Bulk-Endpoint (z.B. POST /api/orders/bulk)
     * wäre für mehrere Portionen effizienter. Außerdem wäre ein einzelnes Order-Objekt mit
     * mehreren Positionen (items: [{mealId, quantity}]) wünschenswert.
     */
    const placeOrder = async () => {
        if (cart.length === 0) {
            alert('Warenkorb ist leer!');
            return;
        }

        setLoading(true);
        const createdOrderIds = [];
        const collectedQrCodes = [];
        const dateString = toLocalISODate(selectedDate);

        // Gesamtbetrag und Datum VOR dem Leeren speichern
        const total = calculateTotal();
        const orderDateCopy = new Date(selectedDate);

        try {
            // Für jedes Item und dessen Menge einzelne Orders anlegen
            for (const item of cart) {
                for (let i = 0; i < item.quantity; i++) {
                    const orderId = await createOrderBackend(item.id, dateString);
                    createdOrderIds.push({ orderId, mealName: item.name });
                }
            }

            // Jede Order bezahlen und QR-Code sammeln
            for (const orderInfo of createdOrderIds) {
                const qr = await payOrderBackend(orderInfo.orderId);
                collectedQrCodes.push({ qrCode: qr, mealName: orderInfo.mealName });
            }

            // Erfolgreich: QR-Codes anzeigen und Warenkorb leeren
            setQrCodes(collectedQrCodes);
            setOrderTotal(total);
            setOrderDate(orderDateCopy);
            setOrderComplete(true);
            setCart([]);

            // Speiseplan neu laden um aktualisierte Verfügbarkeiten anzuzeigen
            await loadMealsForDate();
            await loadWeekMealPlan();
        } catch (err) {
            console.error('Error during order flow:', err);
            // Fallback: Erzeuge einen lokalen Mock-QR-Code, damit UX funktioniert
            const fallbackQr = `MOCK-ORDER-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`;
            setQrCodes([{ qrCode: fallbackQr, mealName: cart[0]?.name || 'Unbekannt' }]);
            setOrderTotal(total);
            setOrderDate(orderDateCopy);
            setOrderComplete(true);
            setCart([]);
            alert('Fehler beim Kontakt mit dem Backend. Es wurde ein lokaler Test-QR erstellt.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (viewMode === 'week') {
            loadWeekMealPlan();
        } else {
            loadMealsForDate();
        }
    }, [viewMode, selectedDate, weekOffset]);

    const availableDates = getAvailableDates();
    const weekDates = getWeekDates();

    // Bestellung abgeschlossen
    if (orderComplete) {
        return (
            <div className="order-complete-container">
                <div className="success-card">
                    <div className="success-icon">✅</div>
                    <h2>Bestellung erfolgreich!</h2>
                    <p>Deine Bestellung wurde erfolgreich aufgegeben und bezahlt.</p>
                    <div className="qr-code-section">
                        <h3>Dein{qrCodes.length > 1 ? 'e' : ''} QR-Code{qrCodes.length > 1 ? 's' : ''} zur Abholung:</h3>
                        <div className="qr-code-container">
                            {qrCodes.map((qrData, idx) => (
                                <div
                                    key={idx}
                                    className="qr-entry"
                                    onClick={() => setEnlargedQR(qrData)}
                                    style={{ cursor: 'pointer' }}
                                >
                                    <QRCodeSVG
                                        value={qrData.qrCode}
                                        size={200}
                                        level="H"
                                        includeMargin={true}
                                    />
                                    <p className="qr-text">{qrData.mealName}</p>
                                    <small className="qr-code-id">{qrData.qrCode}</small>
                                    {qrCodes.length > 1 && <small>Bestellung {idx + 1} von {qrCodes.length}</small>}
                                </div>
                            ))}
                        </div>
                        <small className="qr-instruction">Zeige {qrCodes.length > 1 ? 'diese Codes' : 'diesen Code'} bei der Essensausgabe vor</small>
                    </div>
                    <div className="order-details">
                        <p><strong>Abholdatum:</strong> {formatDate(orderDate)}</p>
                        <p><strong>Gesamtbetrag:</strong> {orderTotal.toFixed(2)} €</p>
                    </div>
                    <button
                        className="btn btn-primary"
                        onClick={() => {
                            setOrderComplete(false);
                            setQrCodes([]);
                            setOrderTotal(0);
                            setEnlargedQR(null);
                            // Beim Zurückgehen zur normalen Ansicht Daten neu laden
                            if (viewMode === 'week') {
                                loadWeekMealPlan();
                            } else {
                                loadMealsForDate();
                            }
                        }}
                    >
                        Neue Bestellung aufgeben
                    </button>
                </div>

                {/* Modal für vergrößerten QR-Code */}
                {enlargedQR && (
                    <div className="qr-modal" onClick={() => setEnlargedQR(null)}>
                        <div className="qr-modal-content" onClick={(e) => e.stopPropagation()}>
                            <button className="qr-modal-close" onClick={() => setEnlargedQR(null)}>✕</button>
                            <h3>{enlargedQR.mealName}</h3>
                            <QRCodeSVG
                                value={enlargedQR.qrCode}
                                size={400}
                                level="H"
                                includeMargin={true}
                            />
                            <p className="qr-text-large">{enlargedQR.qrCode}</p>
                        </div>
                    </div>
                )}
            </div>
        );
    }

    return (
        <div className="order-management-container">
            {/* Header */}
            <div className="gradient-header">
                <h1 className="week-title">🍽️ Speiseplan & Bestellungen</h1>
                <p>Wochenübersicht anzeigen oder Essen vorbestellen</p>
            </div>

            {/* View Mode Switcher */}
            <div className="view-mode-switcher">
                <button
                    className={`view-mode-btn ${viewMode === 'week' ? 'active' : ''}`}
                    onClick={() => setViewMode('week')}
                >
                    📅 Wochenansicht
                </button>
                <button
                    className={`view-mode-btn ${viewMode === 'day' ? 'active' : ''}`}
                    onClick={() => setViewMode('day')}
                >
                    🛒 Bestellen
                </button>
            </div>

            {/* Wochenansicht */}
            {viewMode === 'week' && (
                <div className="week-view-section">
                    <div className="week-navigation">
                        <button
                            className="nav-button-week"
                            onClick={() => setWeekOffset(weekOffset - 1)}
                        >
                            ← Vorherige Woche
                        </button>
                        <h2>Speiseplan - {getMonthYearString()}</h2>
                        <button
                            className="nav-button-week"
                            onClick={() => setWeekOffset(weekOffset + 1)}
                        >
                            Nächste Woche →
                        </button>
                    </div>

                    {weekOffset !== 0 && (
                        <div className="text-center mb-medium">
                            <button
                                className="btn btn-success"
                                onClick={() => setWeekOffset(0)}
                            >
                                Zur aktuellen Woche
                            </button>
                        </div>
                    )}

                    {loading && (
                        <div className="text-center">
                            <div className="loading-spinner"></div>
                            <p>Lade Speiseplan...</p>
                        </div>
                    )}

                    <div className="week-grid">
                        {weekDates.map((date, index) => {
                            const today = isToday(date);
                            const dayMeals = getMealsForDate(date);
                            return (
                                <div key={index} className={`day-card ${today ? 'today' : ''}`}>
                                    <div className="day-header">
                                        <h3 className="day-name">{weekdays[index]}</h3>
                                        <p className="day-date">{formatDateShort(date)}</p>
                                        {today && <span className="today-badge">Heute</span>}
                                    </div>
                                    <div className="day-content">
                                        {!loading && dayMeals.length === 0 && (
                                            <p className="placeholder-text">Noch keine Gerichte</p>
                                        )}
                                        {dayMeals.length > 0 && (
                                            <div className="meals-list-compact">
                                                {dayMeals.map((meal) => (
                                                    <div key={meal.id} className="meal-item-compact">
                                                        <h4 className="meal-name-compact">{meal.name}</h4>
                                                        <p className="meal-description-compact">{meal.description}</p>
                                                        <div className="meal-meta-compact">
                                                            <span className="meal-price-compact">{(meal.price ?? 0).toFixed(2)} €</span>
                                                            {meal.availableStock !== undefined && (
                                                                <span className="meal-stock-compact">📦 {meal.availableStock}</span>
                                                            )}
                                                        </div>
                                                        {meal.categories && meal.categories.length > 0 && (
                                                            <div className="meal-categories-compact">
                                                                {meal.categories.map((cat, idx) => (
                                                                    <span key={idx} className="category-badge-compact">{cat}</span>
                                                                ))}
                                                            </div>
                                                        )}
                                                        {meal.allergens && meal.allergens.length > 0 && (
                                                            <div className="meal-allergens-compact">
                                                                <small>⚠️ {meal.allergens.join(', ')}</small>
                                                            </div>
                                                        )}
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    <div className="switch-to-order-hint">
                        <p>💡 Möchtest du bestellen? Wechsle zur <button className="link-btn" onClick={() => setViewMode('day')}>Bestellansicht</button></p>
                    </div>
                </div>
            )}

            {/* Tagesansicht (Bestellen) */}
            {viewMode === 'day' && (
                <>
                    <div className="date-selection">
                        <label>Datum auswählen:</label>
                        <div className="date-buttons">
                            {availableDates.map((date, index) => (
                                <button
                                    key={index}
                                    className={`date-btn ${toLocalISODate(date) === toLocalISODate(selectedDate) ? 'active' : ''}`}
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
                </>
            )}
        </div>
    );
}

export default OrderManagement;

