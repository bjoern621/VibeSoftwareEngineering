import React, { useState, useEffect } from 'react';
import './OrderManagement.css';
import api from '../services/api';
import { getWeekDays } from '../utils/dateUtils';
import { calculateTotalPrice, getCartItemCount, calculateBtcAmount } from '../utils/priceUtils';
import { PAYMENT_METHODS, BITCOIN_COUNTDOWN_SECONDS } from '../utils/constants';

/**
 * Komponente für die Bestellverwaltung (Kundenansicht)
 * Wochenansicht Montag-Freitag mit Vorbestellmöglichkeit
 */
function OrderManagement() {
  const [weekMealPlans, setWeekMealPlans] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [orderQuantities, setOrderQuantities] = useState({});
  const [showCheckout, setShowCheckout] = useState(false);
  const [showPayment, setShowPayment] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(null);
  const [orderResult, setOrderResult] = useState(null);
  const [showQRCode, setShowQRCode] = useState(false);
  const [accountBalance, setAccountBalance] = useState(50.00);
  const [showBalanceModal, setShowBalanceModal] = useState(false);
  const [topUpAmount, setTopUpAmount] = useState('');
  const [showBitcoinPayment, setShowBitcoinPayment] = useState(false);
  const [bitcoinCountdown, setBitcoinCountdown] = useState(BITCOIN_COUNTDOWN_SECONDS);
  const [btcPrice, setBtcPrice] = useState(null);
  const [btcPriceLoading, setBtcPriceLoading] = useState(false);

  // Heutiges Datum und Wochentage berechnen (nur einmal beim Mount)
  const [weekDays] = useState(() => getWeekDays(new Date()));

  /**
   * BTC-Kurs von CoinGecko API abrufen (tagesaktuell)
   * 
   * @returns {Promise<number>} Aktueller BTC-Kurs in EUR
   */
  const fetchBitcoinPrice = async () => {
    setBtcPriceLoading(true);
    try {
      const response = await fetch('https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur');
      const data = await response.json();
      const price = data.bitcoin.eur;
      setBtcPrice(price);
      return price;
    } catch (err) {
      console.error('Fehler beim Abrufen des BTC-Kurses:', err);
      // Fallback auf geschätzten Wert
      setBtcPrice(50000);
      return 50000;
    } finally {
      setBtcPriceLoading(false);
    }
  };

  /**
   * Speisepläne für die ganze Woche laden
   */
  useEffect(() => {
    fetchWeekMealPlans();
    // eslint-disable-next-line
  }, []);

  /**
   * Lädt alle Speisepläne für die aktuelle Woche (Mo-Fr)
   * 
   * @async
   * @returns {Promise<void>}
   */
  const fetchWeekMealPlans = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const plans = {};
      
      // Speisepläne für alle Wochentage parallel laden
      await Promise.all(
        weekDays.map(async (day) => {
          const data = await api.mealPlans.getByDate(day.date);
          plans[day.date] = data;
        })
      );
      
      setWeekMealPlans(plans);
    } catch (err) {
      setError('Fehler beim Laden der Speisepläne: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Menge für ein Gericht im Warenkorb ändern
   * 
   * @param {string} date - Datum im Format yyyy-MM-dd
   * @param {number} mealId - ID des Gerichts
   * @param {string|number} newQuantity - Neue Anzahl
   */
  const handleQuantityChange = (date, mealId, newQuantity) => {
    const key = `${date}_${mealId}`;
    const qty = parseInt(newQuantity);
    
    if (isNaN(qty) || qty < 0) {
      setOrderQuantities(prev => ({
        ...prev,
        [key]: 0
      }));
      return;
    }
    
    setOrderQuantities(prev => ({
      ...prev,
      [key]: qty
    }));
  };

  /**
   * Bestellung absenden - öffnet Warenkorb-Modal
   * 
   * @async
   */
  const handleOrder = async () => {
    setError(null);
    setSuccess(null);
    
    // Warenkorb öffnen
    setShowCheckout(true);
  };

  /**
   * Von Warenkorb zur Zahlungsauswahl wechseln
   */
  const proceedToPayment = () => {
    setShowCheckout(false);
    setShowPayment(true);
  };

  /**
   * Zahlung abschließen
   * Verarbeitet verschiedene Zahlungsmethoden und öffnet entsprechende Modals
   */
  const completePayment = async () => {
    if (!selectedPaymentMethod) {
      setError('Bitte wählen Sie eine Zahlungsart');
      return;
    }

    // Bitcoin-Zahlung zeigt spezielles Modal
    if (selectedPaymentMethod === 'Bitcoin') {
      setShowPayment(false);
      setShowBitcoinPayment(true);
      setBitcoinCountdown(BITCOIN_COUNTDOWN_SECONDS);
      
      // BTC-Kurs abrufen beim Öffnen des Bitcoin-Modals
      await fetchBitcoinPrice();
      
      return;
    }

    // Guthabenkonto: Prüfe Saldo
    if (selectedPaymentMethod === 'Guthabenkonto') {
      const totalPrice = parseFloat(getTotalPrice());
      if (accountBalance < totalPrice) {
        setError(`Nicht genug Guthaben! Aktueller Stand: ${accountBalance.toFixed(2)} €. Bitte laden Sie Ihr Konto auf.`);
        return;
      }
    }

    setLoading(true);
    setError(null);
    
    try {
      // Alle Bestellungen sammeln
      const orders = [];
      Object.entries(orderQuantities).forEach(([key, quantity]) => {
        if (quantity > 0) {
          const [date, mealIdStr] = key.split('_');
          const mealId = parseInt(mealIdStr);
          const meal = weekMealPlans[date]?.find(m => m.mealId === mealId);
          
          if (meal) {
            orders.push({
              mealId: mealId,
              quantity,
              orderDate: date,
              mealName: meal.meal.name,
              price: meal.meal.price
            });
          }
        }
      });
      
      if (orders.length === 0) {
        setError('Bitte wählen Sie mindestens ein Gericht aus');
        return;
      }
      
      // Alle Bestellungen erstellen
      const createdOrders = await Promise.all(
        orders.map(order => api.orders.create({
          mealId: order.mealId,
          quantity: order.quantity,
          orderDate: order.orderDate
        }))
      );
      
      // Ergebnisse sammeln
      const result = {
        orders: orders,
        createdOrders: createdOrders,
        totalPrice: getTotalPrice(),
        paymentMethod: selectedPaymentMethod,
        qrCodes: createdOrders.map(o => o.qrCode),
        orderIds: createdOrders.map(o => o.id)
      };
      
      // Bei Guthabenkonto: Saldo abziehen
      if (selectedPaymentMethod === 'Guthabenkonto') {
        const totalPrice = parseFloat(getTotalPrice());
        setAccountBalance(prev => prev - totalPrice);
      }
      
      setOrderResult(result);
      setShowPayment(false);
      setShowQRCode(true);
      setOrderQuantities({});
      
      // Speisepläne neu laden
      fetchWeekMealPlans();
    } catch (err) {
      setError('Fehler beim Aufgeben der Bestellung: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Bitcoin-Countdown-Timer: Zählt Restzeit für Bitcoin-Zahlung herunter
   */
  useEffect(() => {
    let interval;
    if (showBitcoinPayment && bitcoinCountdown > 0) {
      interval = setInterval(() => {
        setBitcoinCountdown(prev => {
          if (prev <= 1) {
            setShowBitcoinPayment(false);
            setShowPayment(true);
            setError('Bitcoin-Zahlung abgelaufen. Bitte wählen Sie erneut.');
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [showBitcoinPayment, bitcoinCountdown]);

  /**
   * Bitcoin-Zahlung abschließen
   * Simuliert Bitcoin-Transaktion und erstellt Bestellungen
   * 
   * @async
   * @returns {Promise<void>}
   */
  const completeBitcoinPayment = async () => {
    setLoading(true);
    setShowBitcoinPayment(false);
    
    // Simuliere Bitcoin-Verarbeitung (würde in Realität auf Blockchain-Bestätigung warten)
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    try {
      const orders = [];
      Object.entries(orderQuantities).forEach(([key, quantity]) => {
        if (quantity > 0) {
          const [date, mealIdStr] = key.split('_');
          const mealId = parseInt(mealIdStr);
          const meal = weekMealPlans[date]?.find(m => m.mealId === mealId);
          
          if (meal) {
            orders.push({
              mealId: mealId,
              quantity,
              orderDate: date,
              mealName: meal.meal.name,
              price: meal.meal.price
            });
          }
        }
      });
      
      const createdOrders = await Promise.all(
        orders.map(order => api.orders.create({
          mealId: order.mealId,
          quantity: order.quantity,
          orderDate: order.orderDate
        }))
      );
      
      const result = {
        orders: orders,
        createdOrders: createdOrders,
        totalPrice: getTotalPrice(),
        paymentMethod: 'bitcoin',
        qrCodes: createdOrders.map(o => o.qrCode),
        orderIds: createdOrders.map(o => o.id)
      };
      
      setOrderResult(result);
      setShowQRCode(true);
      setOrderQuantities({});
      setSuccess('Bitcoin-Zahlung erfolgreich! Transaktion bestätigt.');
      fetchWeekMealPlans();
    } catch (err) {
      setError('Fehler bei Bitcoin-Zahlung: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Guthaben aufladen
   * Fügt Betrag zum Guthabenkonto hinzu
   */
  const handleTopUp = () => {
    const amount = parseFloat(topUpAmount);
    if (isNaN(amount) || amount <= 0) {
      setError('Bitte geben Sie einen gültigen Betrag ein');
      return;
    }
    
    setAccountBalance(prev => prev + amount);
    setSuccess(`Guthaben erfolgreich um ${amount.toFixed(2)} € aufgeladen!`);
    setTopUpAmount('');
    setShowBalanceModal(false);
  };

  /**
   * Warenkorb-Items mit Details holen
   * Transformiert orderQuantities zu Array mit Meal-Details
   * 
   * @returns {Array<Object>} Array mit Warenkorb-Items
   */
  const getCartItems = () => {
    const items = [];
    Object.entries(orderQuantities).forEach(([key, quantity]) => {
      if (quantity > 0) {
        const [date, mealIdStr] = key.split('_');
        const mealId = parseInt(mealIdStr);
        const mealPlan = weekMealPlans[date]?.find(m => m.mealId === mealId);
        
        if (mealPlan) {
          items.push({
            key,
            date,
            mealId,
            quantity,
            meal: mealPlan.meal,
            subtotal: mealPlan.meal.price * quantity
          });
        }
      }
    });
    return items;
  };

  /**
   * Gesamtpreis berechnen
   * 
   * @returns {string} Formatierter Gesamtpreis
   */
  const getTotalPrice = () => {
    return calculateTotalPrice(orderQuantities, weekMealPlans).toFixed(2);
  };

  /**
   * Gesamtpreis als Zahl (für Berechnungen)
   * 
   * @returns {number} Gesamtpreis als Zahl
   */
  const getTotalPriceNumber = () => {
    return calculateTotalPrice(orderQuantities, weekMealPlans);
  };

  /**
   * Anzahl Gerichte im Warenkorb
   * 
   * @returns {number} Anzahl Items
   */
  const getItemCount = () => {
    return getCartItemCount(orderQuantities);
  };

  return (
    <div className="order-management">
      {/* Top-Bar mit Guthaben und Warenkorb */}
      <div className="top-bar">
        <div className="balance-display" onClick={() => setShowBalanceModal(true)}>
          <span className="balance-icon">💰</span>
          <div className="balance-info">
            <span className="balance-label">Guthaben</span>
            <span className="balance-amount">{accountBalance.toFixed(2)} €</span>
          </div>
          <button className="balance-topup-btn">Aufladen +</button>
        </div>

        <div className="cart-icon-container" onClick={() => {
          if (getItemCount() > 0) {
            setShowCheckout(true);
          }
        }}>
          <div className="cart-icon">🛒</div>
          {getItemCount() > 0 && (
            <span className="cart-badge">{getItemCount()}</span>
          )}
        </div>
      </div>

      <div className="order-header">
        <h2>Wochenansicht & Vorbestellung</h2>
        <p className="subtitle">Bestellen Sie für Montag bis Freitag vor</p>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {loading ? (
        <div className="loading">Lade Speisepläne...</div>
      ) : (
        <>
          {/* Wochenübersicht */}
          <div className="week-view">
            {weekDays.map((day) => (
              <div key={day.date} className={`day-column ${day.isToday ? 'today' : ''}`}>
                {/* Tag-Header */}
                <div className="day-header">
                  <div className="day-name">{day.dayShort}</div>
                  <div className="day-date">{day.dayNum}. {day.monthShort}</div>
                  {day.isToday && <div className="today-badge">Heute</div>}
                </div>

                {/* Gerichte für diesen Tag */}
                <div className="day-meals">
                  {weekMealPlans[day.date] && weekMealPlans[day.date].length > 0 ? (
                    weekMealPlans[day.date].map((mealPlan) => {
                      const key = `${day.date}_${mealPlan.mealId}`; // WICHTIG: _ statt -
                      const quantity = orderQuantities[key] || 0;

                      return (
                        <div key={mealPlan.mealId} className="meal-card">
                          <div className="meal-info">
                            <h4 className="meal-name">{mealPlan.meal.name}</h4>
                            <p className="meal-description">{mealPlan.meal.description}</p>
                            
                            {/* Kategorien & Allergene */}
                            <div className="meal-tags">
                              <span className={`category-badge ${mealPlan.meal.category}`}>
                                {mealPlan.meal.category}
                              </span>
                              {mealPlan.meal.allergens && mealPlan.meal.allergens.length > 0 && (
                                <span className="allergens-badge">
                                  ⚠️ {mealPlan.meal.allergens.join(', ')}
                                </span>
                              )}
                            </div>

                            {/* Nährwerte */}
                            <div className="nutrition-info">
                              <span>{mealPlan.meal.calories} kcal</span>
                              <span>P: {mealPlan.meal.protein}g</span>
                              <span>K: {mealPlan.meal.carbohydrates}g</span>
                              <span>F: {mealPlan.meal.fat}g</span>
                            </div>

                            {/* Preis & Bestand */}
                            <div className="meal-bottom">
                              <span className="meal-price">{mealPlan.meal.price.toFixed(2)} €</span>
                              <span className={`meal-stock ${mealPlan.stock < 10 ? 'low' : ''}`}>
                                {mealPlan.stock > 0 ? `${mealPlan.stock} verfügbar` : 'Ausverkauft'}
                              </span>
                            </div>
                          </div>

                          {/* Mengenauswahl */}
                          <div className="quantity-selector">
                            <button
                              className="qty-btn"
                              onClick={() => {
                                const newQty = Math.max(0, quantity - 1);
                                handleQuantityChange(day.date, mealPlan.mealId, newQty);
                              }}
                              disabled={quantity === 0}
                            >
                              -
                            </button>
                            <input
                              type="number"
                              min="0"
                              max={mealPlan.stock}
                              value={quantity}
                              onChange={(e) => handleQuantityChange(day.date, mealPlan.mealId, e.target.value)}
                              className="qty-input"
                            />
                            <button
                              className="qty-btn"
                              onClick={() => {
                                const newQty = Math.min(mealPlan.stock, quantity + 1);
                                handleQuantityChange(day.date, mealPlan.mealId, newQty);
                              }}
                              disabled={quantity >= mealPlan.stock}
                            >
                              +
                            </button>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="no-meals">Kein Speiseplan für diesen Tag</div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Warenkorb-Zusammenfassung */}
          {getItemCount() > 0 && !showCheckout && !showPayment && !showQRCode && (
            <div className="cart-summary">
              <div className="cart-info">
                <h3>Bestellübersicht</h3>
                <div className="cart-details">
                  <span className="cart-items">{getItemCount()} Gericht(e)</span>
                  <span className="cart-total">Gesamt: {getTotalPrice()} €</span>
                </div>
              </div>
              <button className="order-button" onClick={handleOrder}>
                Zur Kasse
              </button>
            </div>
          )}
        </>
      )}

      {/* Warenkorb-Modal */}
      {showCheckout && (
        <div className="checkout-modal-overlay" onClick={() => setShowCheckout(false)}>
          <div className="checkout-modal" onClick={(e) => e.stopPropagation()}>
            <h2>🛒 Warenkorb</h2>
            
            <div className="cart-items-list">
              {getCartItems().map((item) => (
                <div key={item.key} className="cart-item">
                  <div className="cart-item-info">
                    <h4>{item.meal.name}</h4>
                    <p className="cart-item-date">📅 {item.date}</p>
                    <span className={`category-badge ${item.meal.category}`}>
                      {item.meal.category}
                    </span>
                  </div>
                  <div className="cart-item-quantity">
                    <span>{item.quantity}x {item.meal.price.toFixed(2)} €</span>
                  </div>
                  <div className="cart-item-total">
                    <strong>{item.subtotal.toFixed(2)} €</strong>
                  </div>
                </div>
              ))}
            </div>

            <div className="cart-total-section">
              <div className="cart-total-row">
                <span>Zwischensumme:</span>
                <span>{getTotalPrice()} €</span>
              </div>
              <div className="cart-total-row grand-total">
                <span><strong>Gesamt:</strong></span>
                <span><strong>{getTotalPrice()} €</strong></span>
              </div>
            </div>

            <div className="cart-actions">
              <button className="btn-secondary" onClick={() => setShowCheckout(false)}>
                Weiter einkaufen
              </button>
              <button className="btn-primary" onClick={proceedToPayment}>
                Zur Zahlung →
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Zahlungs-Modal */}
      {showPayment && (
        <div className="checkout-modal-overlay" onClick={() => setShowPayment(false)}>
          <div className="checkout-modal" onClick={(e) => e.stopPropagation()}>
            <h2>💳 Zahlung via EASYPAY</h2>
            
            <div className="payment-summary">
              <p>Zu zahlender Betrag: <strong>{getTotalPrice()} €</strong></p>
              <p className="payment-items">{getItemCount()} Gericht(e)</p>
            </div>

            <div className="payment-methods">
              <h3>Zahlungsart wählen:</h3>
              
              <div 
                className={`payment-method ${selectedPaymentMethod === PAYMENT_METHODS.KREDITKARTE ? 'selected' : ''}`}
                onClick={() => setSelectedPaymentMethod(PAYMENT_METHODS.KREDITKARTE)}
              >
                <div className="payment-icon">💳</div>
                <div className="payment-details">
                  <h4>{PAYMENT_METHODS.KREDITKARTE}</h4>
                  <p>Visa, Mastercard, American Express</p>
                </div>
                {selectedPaymentMethod === PAYMENT_METHODS.KREDITKARTE && <div className="check-mark">✓</div>}
              </div>

              <div 
                className={`payment-method ${selectedPaymentMethod === PAYMENT_METHODS.DEBITKARTE ? 'selected' : ''}`}
                onClick={() => setSelectedPaymentMethod(PAYMENT_METHODS.DEBITKARTE)}
              >
                <div className="payment-icon">💳</div>
                <div className="payment-details">
                  <h4>{PAYMENT_METHODS.DEBITKARTE}</h4>
                  <p>EC-Karte, Girocard</p>
                </div>
                {selectedPaymentMethod === PAYMENT_METHODS.DEBITKARTE && <div className="check-mark">✓</div>}
              </div>

              <div 
                className={`payment-method ${selectedPaymentMethod === PAYMENT_METHODS.GUTHABEN ? 'selected' : ''}`}
                onClick={() => setSelectedPaymentMethod(PAYMENT_METHODS.GUTHABEN)}
              >
                <div className="payment-icon">💰</div>
                <div className="payment-details">
                  <h4>{PAYMENT_METHODS.GUTHABEN}</h4>
                  <p>Mensa-Guthabenkonto</p>
                </div>
                {selectedPaymentMethod === PAYMENT_METHODS.GUTHABEN && <div className="check-mark">✓</div>}
              </div>

              <div 
                className={`payment-method ${selectedPaymentMethod === PAYMENT_METHODS.BITCOIN ? 'selected' : ''}`}
                onClick={() => setSelectedPaymentMethod(PAYMENT_METHODS.BITCOIN)}
              >
                <div className="payment-icon">₿</div>
                <div className="payment-details">
                  <h4>{PAYMENT_METHODS.BITCOIN}</h4>
                  <p>Kryptowährung</p>
                </div>
                {selectedPaymentMethod === PAYMENT_METHODS.BITCOIN && <div className="check-mark">✓</div>}
              </div>
            </div>

            <div className="payment-info">
              <p>ℹ️ Zahlung wird über EASYPAY abgewickelt</p>
              <p>🔒 Sichere Verschlüsselung</p>
            </div>

            <div className="cart-actions">
              <button className="btn-secondary" onClick={() => {
                setShowPayment(false);
                setShowCheckout(true);
              }}>
                ← Zurück
              </button>
              <button 
                className="btn-primary" 
                onClick={completePayment}
                disabled={!selectedPaymentMethod || loading}
              >
                {loading ? 'Verarbeite...' : 'Jetzt bezahlen'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* QR-Code-Modal nach erfolgreicher Zahlung */}
      {showQRCode && orderResult && (
        <div className="checkout-modal-overlay">
          <div className="checkout-modal qr-modal">
            <div className="success-header">
              <div className="success-icon">✅</div>
              <h2>Zahlung erfolgreich!</h2>
              <p>Ihre Bestellung wurde aufgegeben</p>
            </div>

            <div className="order-summary">
              <p>Bezahlt: <strong>{orderResult.totalPrice} €</strong></p>
              <p>via {orderResult.paymentMethod === 'creditcard' ? 'Kreditkarte' : 
                       orderResult.paymentMethod === 'debitcard' ? 'Debitkarte' :
                       orderResult.paymentMethod === 'account' ? 'Guthabenkonto' : 'Bitcoin'}</p>
            </div>

            <div className="qr-codes-section">
              <h3>🎫 Ihre QR-Codes zur Abholung:</h3>
              
              {orderResult.orders.map((order, index) => (
                <div key={index} className="qr-code-item">
                  <div className="qr-code-box">
                    <div className="qr-code-placeholder">
                      <div className="qr-pattern">
                        ▪▫▪▫▪▫<br/>
                        ▫▪▫▪▫▪<br/>
                        ▪▫▪▫▪▫<br/>
                        ▫▪▫▪▫▪<br/>
                        ▪▫▪▫▪▫
                      </div>
                      <div className="qr-code-text">
                        {orderResult.qrCodes[index]}
                      </div>
                    </div>
                  </div>
                  <div className="qr-code-info">
                    <h4>{order.mealName}</h4>
                    <p>Menge: {order.quantity}x</p>
                    <p>Datum: {order.orderDate}</p>
                    <p className="qr-label">QR-Code: <code>{orderResult.qrCodes[index]}</code></p>
                  </div>
                </div>
              ))}
            </div>

            <div className="pickup-info">
              <p>📱 QR-Code bei der Essensausgabe vorzeigen</p>
              <p>⏰ Abholung während der Öffnungszeiten</p>
              <p>📧 Bestätigung wurde per E-Mail versandt</p>
            </div>

            <button className="btn-primary" onClick={() => {
              setShowQRCode(false);
              setOrderResult(null);
              setSelectedPaymentMethod(null);
            }}>
              Schließen
            </button>
          </div>
        </div>
      )}

      {/* Guthaben-Auflade-Modal */}
      {showBalanceModal && (
        <div className="checkout-modal-overlay" onClick={() => setShowBalanceModal(false)}>
          <div className="checkout-modal" onClick={(e) => e.stopPropagation()}>
            <h2>💰 Guthaben aufladen</h2>
            
            <div className="balance-current">
              <p>Aktueller Kontostand:</p>
              <h3>{accountBalance.toFixed(2)} €</h3>
            </div>

            <div className="topup-form">
              <label htmlFor="topup-amount">Aufladebetrag:</label>
              <div className="topup-input-group">
                <input
                  id="topup-amount"
                  type="number"
                  min="1"
                  step="0.01"
                  value={topUpAmount}
                  onChange={(e) => setTopUpAmount(e.target.value)}
                  placeholder="z.B. 20.00"
                />
                <span className="currency">€</span>
              </div>

              <div className="quick-amounts">
                <button onClick={() => setTopUpAmount('10')}>10 €</button>
                <button onClick={() => setTopUpAmount('20')}>20 €</button>
                <button onClick={() => setTopUpAmount('50')}>50 €</button>
                <button onClick={() => setTopUpAmount('100')}>100 €</button>
              </div>
            </div>

            <div className="payment-info">
              <p>ℹ️ Zahlung erfolgt über EASYPAY</p>
              <p>💳 Kreditkarte, Debitkarte oder Bitcoin</p>
            </div>

            <div className="cart-actions">
              <button className="btn-secondary" onClick={() => {
                setShowBalanceModal(false);
                setTopUpAmount('');
              }}>
                Abbrechen
              </button>
              <button className="btn-primary" onClick={handleTopUp}>
                Aufladen
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Bitcoin-Zahlungs-Modal */}
      {showBitcoinPayment && (
        <div className="checkout-modal-overlay">
          <div className="checkout-modal bitcoin-modal">
            <h2>₿ Bitcoin-Zahlung</h2>
            
            <div className="bitcoin-amount">
              <p>Zu zahlender Betrag:</p>
              <h3>{getTotalPrice()} €</h3>
              {btcPriceLoading ? (
                <p className="btc-equivalent">Lade aktuellen BTC-Kurs...</p>
              ) : btcPrice ? (
                <>
                  <p className="btc-equivalent">≈ {calculateBtcAmount(getTotalPriceNumber(), btcPrice)} BTC</p>
                  <p className="btc-rate">1 BTC = {btcPrice.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €</p>
                </>
              ) : (
                <p className="btc-equivalent">BTC-Kurs wird geladen...</p>
              )}
            </div>

            <div className="bitcoin-countdown">
              <p>⏰ Verbleibende Zeit:</p>
              <div className="countdown-timer">
                <span className="time-large">{Math.floor(bitcoinCountdown / 60)}:{String(bitcoinCountdown % 60).padStart(2, '0')}</span>
                <span className="time-label">Minuten</span>
              </div>
            </div>

            <div className="bitcoin-qr-section">
              <h4>Bitcoin-Adresse scannen:</h4>
              <div className="bitcoin-qr-box">
                <div className="qr-code-placeholder bitcoin-qr">
                  <div className="qr-pattern">
                    ▪▫▪▫▪▫▪▫▪<br/>
                    ▫▪▫▪▫▪▫▪▫<br/>
                    ▪▫▪▫▪▫▪▫▪<br/>
                    ▫▪▫▪▫▪▫▪▫<br/>
                    ▪▫▪▫▪▫▪▫▪<br/>
                    ▫▪▫▪▫▪▫▪▫<br/>
                    ▪▫▪▫▪▫▪▫▪
                  </div>
                </div>
              </div>

              <div className="bitcoin-address">
                <label>Bitcoin-Adresse:</label>
                <div className="address-box">
                  <code>bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh</code>
                  <button className="copy-btn" onClick={() => {
                    navigator.clipboard.writeText('bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh');
                    setSuccess('Bitcoin-Adresse kopiert!');
                  }}>📋</button>
                </div>
              </div>
            </div>

            <div className="bitcoin-info">
              <p>📱 Scannen Sie den QR-Code mit Ihrer Bitcoin-Wallet</p>
              <p>⚡ Zahlung wird nach 1 Bestätigung akzeptiert</p>
              <p>🔒 Sichere Blockchain-Transaktion</p>
            </div>

            <div className="cart-actions">
              <button className="btn-secondary" onClick={() => {
                setShowBitcoinPayment(false);
                setShowPayment(true);
              }}>
                Abbrechen
              </button>
              <button className="btn-primary" onClick={completeBitcoinPayment} disabled={loading}>
                {loading ? 'Warte auf Bestätigung...' : 'Zahlung erhalten ✓'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default OrderManagement;
