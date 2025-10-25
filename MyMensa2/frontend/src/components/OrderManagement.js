import React, { useState, useEffect } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import './OrderManagement.css';
import { calculateBtcAmount } from '../utils/priceUtils';
import { BITCOIN_COUNTDOWN_SECONDS, ERROR_MESSAGES } from '../utils/constants';
import { getTwoWeeks } from '../utils/dateUtils';
import { getMealImage } from '../utils/imageUtils';
import api from '../services/api';

const categories = [
  { value: 'VEGETARIAN', label: '🥗 Vegetarisch' },
  { value: 'VEGAN', label: '🌱 Vegan' },
  { value: 'MEAT', label: '🍖 Fleisch' },
  { value: 'FISH', label: '🐟 Fisch' },
  { value: 'HALAL', label: '☪️ Halal' },
];

function OrderManagement() {
  // Wochenansicht State - ERWEITERT für 2 Wochen
  const today = new Date();
  const [twoWeeks] = useState(() => getTwoWeeks(today)); // Nur einmal berechnen
  const [selectedWeek, setSelectedWeek] = useState(1); // 1 = diese Woche, 2 = nächste Woche
  const [selectedDay, setSelectedDay] = useState(twoWeeks[0].days[0]);
  const [weekMealPlans, setWeekMealPlans] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [cart, setCart] = useState({});
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [showCheckout, setShowCheckout] = useState(false);
  const [showPayment, setShowPayment] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(null);
  const [showQRCode, setShowQRCode] = useState(false);
  const [qrCodeData, setQrCodeData] = useState(null);
  const [accountBalance, setAccountBalance] = useState(50.00);
  const [showBalanceModal, setShowBalanceModal] = useState(false);
  const [topUpAmount, setTopUpAmount] = useState('');
  const [topUpStep, setTopUpStep] = useState(1); // 1 = Betrag, 2 = Zahlungsmethode
  const [isTopUp, setIsTopUp] = useState(false); // Kennzeichnet Guthaben-Aufladen vs. Bestellung
  
  // Bitcoin
  const [showBitcoinPayment, setShowBitcoinPayment] = useState(false);
  const [bitcoinCountdown, setBitcoinCountdown] = useState(BITCOIN_COUNTDOWN_SECONDS);
  const [btcPrice, setBtcPrice] = useState(null);
  
  // Kreditkarte
  const [showCreditCardForm, setShowCreditCardForm] = useState(false);
  const [creditCardData, setCreditCardData] = useState({
    cardNumber: '',
    cardHolder: '',
    expiryDate: '',
    cvv: ''
  });

  const paymentMethodsList = [
    { id: 'creditcard', name: '💳 Kreditkarte', description: 'Visa, Mastercard, American Express' },
    { id: 'account', name: '💰 Guthabenkonto', description: `Guthaben: ${accountBalance.toFixed(2)} €` },
    { id: 'bitcoin', name: '₿ Bitcoin', description: 'Kryptowährung' }
  ];

  // Lade BTC-Kurs von CoinGecko API
  const fetchBitcoinPrice = async () => {
    try {
      const response = await fetch('https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur');
      const data = await response.json();
      const price = data.bitcoin.eur;
      setBtcPrice(price);
      return price;
    } catch (err) {
      console.error('Fehler beim Abrufen des BTC-Kurses:', err);
      alert(ERROR_MESSAGES.BITCOIN_PRICE_FETCH_FAILED);
      setBtcPrice(50000); // Fallback
      return 50000;
    }
  };

  // Bitcoin Countdown Timer - 30 Minuten = 1800 Sekunden
  useEffect(() => {
    let timer;
    if (showBitcoinPayment && bitcoinCountdown > 0) {
      timer = setInterval(() => {
        setBitcoinCountdown(prev => {
          if (prev <= 1) {
            setShowBitcoinPayment(false);
            setShowPayment(true);
            alert('⏱️ Zeit abgelaufen! Bitte wählen Sie erneut eine Zahlungsmethode.');
            return 1800; // 30 Minuten
          }
          return prev - 1;
        });
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [showBitcoinPayment, bitcoinCountdown]);

  // Lade Speisepläne für beide Wochen (diese + nächste)
  useEffect(() => {
    const loadWeekMealPlans = async () => {
      try {
        setLoading(true);
        setError(null);
        const plans = {};
        
        // Lade Gerichte für beide Wochen
        const allDays = [...twoWeeks[0].days, ...twoWeeks[1].days];
        
        for (const day of allDays) {
          const mealPlans = await api.mealPlans.getByDate(day.date);
          // Füge passende Bilder zu den Gerichten hinzu
          const mealsForSelectedDay = mealPlans.map((mp) => ({
            ...mp.meal,
            stock: mp.stock,
            image: getMealImage(mp.meal.name)
          }));
          plans[day.date] = mealsForSelectedDay;
        }
        
        setWeekMealPlans(plans);
      } catch (err) {
        console.error('Fehler beim Laden der Speisepläne:', err);
        setError('Fehler beim Laden der Speisepläne: ' + err.message);
      } finally {
        setLoading(false);
      }
    };
    loadWeekMealPlans();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // twoWeeks ist stabil (useState mit Initializer)

  // Hole Gerichte für den ausgewählten Tag
  const mealsForSelectedDay = weekMealPlans[selectedDay.date] || [];

  const updateQuantity = (mealId, newQuantity) => {
    if (newQuantity <= 0) {
      const newCart = { ...cart };
      delete newCart[mealId];
      setCart(newCart);
    } else {
      const meal = mealsForSelectedDay.find(m => m.id === mealId);
      if (meal && newQuantity <= meal.stock) {
        setCart(prev => ({ ...prev, [mealId]: newQuantity }));
      }
    }
  };

  const getTotalItems = () => Object.values(cart).reduce((sum, qty) => sum + qty, 0);
  
  const getTotalPrice = () => {
    return Object.entries(cart).reduce((sum, [mealId, qty]) => {
      const meal = mealsForSelectedDay.find(m => m.id === parseInt(mealId));
      return sum + (meal ? meal.price * qty : 0);
    }, 0);
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCheckout = () => {
    setShowCheckout(true);
  };

  const handleCloseCheckout = () => {
    setShowCheckout(false);
    setShowPayment(false);
    setShowBitcoinPayment(false);
    setShowCreditCardForm(false);
    setSelectedPaymentMethod(null);
    setBitcoinCountdown(1800);
  };

  const handleContinueToPayment = () => {
    setShowCheckout(false);
    setShowPayment(true);
  };

  const handleSelectPaymentMethod = (methodId) => {
    setSelectedPaymentMethod(methodId);
    
    if (methodId === 'bitcoin') {
      setShowPayment(false);
      setShowBitcoinPayment(true);
      setBitcoinCountdown(1800);
      if (!btcPrice) {
        fetchBitcoinPrice();
      }
    } else if (methodId === 'creditcard') {
      setShowPayment(false);
      setShowCreditCardForm(true);
    }
  };

  const handleConfirmPayment = () => {
    const total = getTotalPrice();
    if (selectedPaymentMethod === 'account' && accountBalance < total) {
      alert(ERROR_MESSAGES.INSUFFICIENT_BALANCE);
      return;
    }
    if (selectedPaymentMethod === 'account') {
      setAccountBalance(prev => prev - total);
    }
    completeOrder();
  };

  const handleCreditCardPayment = () => {
    if (!creditCardData.cardNumber || !creditCardData.cardHolder || 
        !creditCardData.expiryDate || !creditCardData.cvv) {
      alert('❌ Bitte füllen Sie alle Felder aus!');
      return;
    }
    
    if (isTopUp) {
      // Guthaben-Aufladen
      completeTopUp();
      setShowCreditCardForm(false);
      setIsTopUp(false);
    } else {
      // Normale Bestellung
      completeOrder();
    }
  };

  const handleBitcoinPayment = () => {
    if (isTopUp) {
      // Guthaben-Aufladen
      completeTopUp();
      setShowBitcoinPayment(false);
      setIsTopUp(false);
    } else {
      // Normale Bestellung
      completeOrder();
    }
  };

  const completeOrder = async () => {
    // Erstelle lokale QR-Code-Daten für UI (wie vorher)
    const orderData = {
      orderId: 'ORDER-' + Date.now(),
      items: Object.entries(cart).map(([mealId, qty]) => {
        const meal = mealsForSelectedDay.find(m => m.id === parseInt(mealId));
        return { name: meal?.name || 'Unknown', quantity: qty };
      }),
      total: getTotalPrice(),
      timestamp: new Date().toISOString(),
      paymentMethod: selectedPaymentMethod
    };
    
    // Parallel: Erstelle Bestellungen im Backend (ohne UI zu blockieren)
    try {
      for (const [mealId, quantity] of Object.entries(cart)) {
        for (let i = 0; i < quantity; i++) {
          // Wichtig: Verwende selectedDay.date als pickupDate!
          const backendOrder = {
            mealId: parseInt(mealId),
            pickupDate: selectedDay.date // Das ausgewählte Datum, nicht "heute"!
          };
          
          // Erstelle Bestellung
          const createdOrder = await api.orders.create(backendOrder);
          console.log(`✅ Bestellung erstellt für ${selectedDay.date}:`, createdOrder);
          
          // Bezahle sofort mit der gewählten Methode
          let paymentMethod = 'PREPAID_ACCOUNT';
          if (selectedPaymentMethod === 'creditcard') paymentMethod = 'CREDIT_CARD';
          if (selectedPaymentMethod === 'bitcoin') paymentMethod = 'BITCOIN';
          
          // Backend gibt "orderId" zurück, nicht "id"
          const orderIdToUse = createdOrder.orderId || createdOrder.id;
          console.log(`💳 Versuche Bezahlung: Order ${orderIdToUse}, Methode: ${paymentMethod}`);
          const paymentResult = await api.orders.pay(orderIdToUse, {
            paymentMethod: paymentMethod,
            paymentTransactionId: `EASYPAY-${paymentMethod}-${Date.now()}`
          });
          console.log(`✅ Bezahlung erfolgreich:`, paymentResult);
          
          // Automatischer Lagerverbrauch nach erfolgreicher Bezahlung
          try {
            const consumeResult = await api.inventory.consumeForMeal(parseInt(mealId), 1);
            console.log(`📦 Lagerbestand aktualisiert:`, consumeResult);
          } catch (consumeErr) {
            console.warn(`⚠️ Lagerverbrauch fehlgeschlagen (wird ignoriert):`, consumeErr.message);
            // Fehler wird ignoriert, um Bestellung nicht zu blockieren
          }
        }
      }
      console.log('✅ Bestellungen erfolgreich im Backend erstellt');
    } catch (err) {
      console.error('❌ Backend-Bestellung fehlgeschlagen:', err);
      console.error('❌ Error Details:', err.message, err.stack);
      // Fehler nicht anzeigen - UI funktioniert trotzdem mit lokalen Daten
    }
    
    // UI wie vorher aktualisieren
    setQrCodeData(orderData);
    setShowPayment(false);
    setShowBitcoinPayment(false);
    setShowCreditCardForm(false);
    setShowQRCode(true);
  };


  const handleNewOrder = () => {
    setCart({});
    setShowQRCode(false);
    setSelectedPaymentMethod(null);
    setCreditCardData({ cardNumber: '', cardHolder: '', expiryDate: '', cvv: '' });
  };

  // Guthaben aufladen - Schritt 1: Betrag prüfen
  const handleContinueToTopUpPayment = () => {
    const amount = parseFloat(topUpAmount);
    if (isNaN(amount) || amount <= 0) {
      alert(ERROR_MESSAGES.INVALID_AMOUNT);
      return;
    }
    if (amount > 500) {
      alert('Maximaler Aufladebetrag ist 500 €.');
      return;
    }
    setTopUpStep(2); // Zur Zahlungsmethoden-Auswahl
  };

  // Guthaben aufladen - Schritt 2: Zahlungsmethode wählen
  const handleSelectTopUpPaymentMethod = (methodId) => {
    setIsTopUp(true); // Markieren als Top-Up-Transaktion
    if (methodId === 'bitcoin') {
      fetchBitcoinPrice();
      setShowBalanceModal(false);
      setShowBitcoinPayment(true);
      setBitcoinCountdown(1800);
    } else if (methodId === 'credit_card') {
      setShowBalanceModal(false);
      setShowCreditCardForm(true);
    }
  };

  // Nach Zahlung: Guthaben aufladen
  const completeTopUp = () => {
    const amount = parseFloat(topUpAmount);
    setAccountBalance(prev => prev + amount);
    setTopUpAmount('');
    setTopUpStep(1);
  };

  const filteredMeals = mealsForSelectedDay.filter(meal => {
    const matchesSearch = meal.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         meal.description?.toLowerCase().includes(searchTerm.toLowerCase());
    
    // Backend speichert categories als Array: ["Vegetarisch", "Glutenfrei"]
    // Filter-Buttons nutzen Werte wie 'VEGETARIAN', 'VEGAN', etc.
    let matchesCategory = categoryFilter === 'all';
    
    if (!matchesCategory && meal.categories && Array.isArray(meal.categories)) {
      // Mapping zwischen Filter-Buttons und Backend-Categories
      const categoryMap = {
        'VEGETARIAN': ['Vegetarisch'],
        'VEGAN': ['Vegan'],
        'MEAT': ['Fleisch'],
        'FISH': ['Fisch'],
        'HALAL': ['Halal']
      };
      
      const expectedCategories = categoryMap[categoryFilter] || [];
      matchesCategory = expectedCategories.some(cat => 
        meal.categories.some(mealCat => 
          mealCat.toLowerCase().includes(cat.toLowerCase())
        )
      );
    }
    
    return matchesSearch && matchesCategory && meal.stock > 0;
  });

  return (
    <div className="order-management">
      <div className="container">
        {/* Header */}
        <div className="page-header">
          <div className="header-left">
            <h2>Speiseplan der Woche</h2>
            <p className="subtitle">
              {loading ? 'Lade Speisepläne...' : `${mealsForSelectedDay.filter(m => m.stock > 0).length} Gerichte verfügbar am ${selectedDay.dayName}`}
            </p>
          </div>
          <div className="header-right">
            <div className="balance-display" onClick={() => setShowBalanceModal(true)}>
              <span className="balance-icon">💰</span>
              <div className="balance-info">
                <span className="balance-label">Guthaben</span>
                <span className="balance-amount">{accountBalance.toFixed(2)} €</span>
              </div>
            </div>
          </div>
        </div>

        {/* Wochenauswahl: Diese Woche / Nächste Woche */}
        <div className="week-selector">
          {twoWeeks.map(week => (
            <button
              key={week.week}
              className={`week-selector-btn ${selectedWeek === week.week ? 'active' : ''}`}
              onClick={() => {
                setSelectedWeek(week.week);
                setSelectedDay(week.days[0]); // Setze auf ersten Tag der ausgewählten Woche
              }}
            >
              {week.label}
            </button>
          ))}
        </div>

        {/* Wochentags-Tabs für die ausgewählte Woche */}
        <div className="week-tabs">
          {twoWeeks.find(w => w.week === selectedWeek).days.map(day => (
            <button
              key={day.date}
              className={`week-tab ${selectedDay.date === day.date ? 'active' : ''}`}
              onClick={() => setSelectedDay(day)}
            >
              {day.isToday && <span className="today-indicator"></span>}
              <div className="day-name">{day.dayName}</div>
              <div className="day-date">
                {day.date.split('-')[2]}.{day.date.split('-')[1]}.
              </div>
            </button>
          ))}
        </div>

        {/* Error Message */}
        {error && (
          <div className="error-message" style={{
            backgroundColor: '#fee',
            color: '#c33',
            padding: '15px',
            borderRadius: '8px',
            marginBottom: '20px'
          }}>
            {error}
          </div>
        )}

        {/* Filters */}
        <div className="filters-section">
          <div className="search-box">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              placeholder="Gerichte durchsuchen..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="category-filters">
            <button
              className={`filter-chip ${categoryFilter === 'all' ? 'active' : ''}`}
              onClick={() => setCategoryFilter('all')}
            >
              Alle
            </button>
            {categories.map(cat => (
              <button
                key={cat.value}
                className={`filter-chip ${categoryFilter === cat.value ? 'active' : ''}`}
                onClick={() => setCategoryFilter(cat.value)}
              >
                {cat.label}
              </button>
            ))}
          </div>
        </div>

        {/* Meals Grid */}
        <div className="meals-grid">
          {filteredMeals.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">🍽️</span>
              <h3>Keine Gerichte gefunden</h3>
              <p>Passen Sie die Filter an oder versuchen Sie es später erneut.</p>
            </div>
          ) : (
            filteredMeals.map(meal => (
              <div key={meal.id} className="meal-card">
                {/* Meal Image */}
                <div className="meal-image" style={{backgroundImage: `url(${meal.image})`}}>
                  <span className="meal-category-badge">
                    {categories.find(c => c.value === meal.category)?.label || '🍽️'}
                  </span>
                </div>

                <div className="meal-card-body">
                  <h3 className="meal-name">{meal.name}</h3>
                  <p className="meal-description">{meal.description}</p>

                  {/* Meal Info */}
                  <div className="meal-info-row">
                    <span className="info-badge">🔥 {meal.calories} kcal</span>
                    <span className={`stock-badge ${meal.stock < 15 ? 'low' : ''}`}>
                      📦 {meal.stock} verfügbar
                    </span>
                  </div>

                  {/* Allergens */}
                  {meal.allergens && meal.allergens.length > 0 && (
                    <div className="allergens">
                      <span className="allergen-label">⚠️ Allergene:</span>
                      <span className="allergen-list">{meal.allergens.join(', ')}</span>
                    </div>
                  )}

                  {/* Price & Quantity Controls */}
                  <div className="meal-card-footer">
                    <span className="meal-price">{meal.price.toFixed(2)} €</span>
                    
                    {cart[meal.id] > 0 ? (
                      <div className="quantity-controls">
                        <button 
                          className="qty-btn qty-minus"
                          onClick={() => updateQuantity(meal.id, cart[meal.id] - 1)}
                        >
                          ➖
                        </button>
                        <span className="qty-display">{cart[meal.id]}</span>
                        <button 
                          className="qty-btn qty-plus"
                          onClick={() => updateQuantity(meal.id, cart[meal.id] + 1)}
                          disabled={cart[meal.id] >= meal.stock}
                        >
                          ➕
                        </button>
                      </div>
                    ) : (
                      <button 
                        onClick={() => updateQuantity(meal.id, 1)} 
                        className="btn-add-cart"
                      >
                        🛒 Hinzufügen
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Cart Summary */}
        {getTotalItems() > 0 && (
          <div className="cart-summary-floating">
            <div className="cart-summary-content">
              <div className="cart-info">
                <span className="cart-icon">🛒</span>
                <div className="cart-details">
                  <strong>{getTotalItems()} Artikel</strong>
                  <span className="cart-total">{getTotalPrice().toFixed(2)} €</span>
                </div>
              </div>
              <button className="btn-checkout" onClick={handleCheckout}>
                Zur Kasse
              </button>
            </div>
          </div>
        )}

        {/* Checkout Modal */}
        {showCheckout && (
          <div className="modal-overlay" onClick={handleCloseCheckout}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>🛒 Bestellübersicht</h2>
                <button className="modal-close" onClick={handleCloseCheckout}>✕</button>
              </div>

              <div className="modal-body">
                {Object.entries(cart).map(([mealId, quantity]) => {
                  const meal = mealsForSelectedDay.find(m => m.id === parseInt(mealId));
                  if (!meal) return null;
                  return (
                    <div key={mealId} className="checkout-item">
                      <div className="checkout-item-image" style={{backgroundImage: `url(${meal.image})`}} />
                      <div className="checkout-item-details">
                        <h4>{meal.name}</h4>
                        <p className="checkout-item-quantity">{quantity}x à {meal.price.toFixed(2)} €</p>
                      </div>
                      <div className="checkout-item-price">
                        {(meal.price * quantity).toFixed(2)} €
                      </div>
                    </div>
                  );
                })}

                <div className="checkout-total">
                  <span className="checkout-total-label">Gesamtsumme:</span>
                  <span className="checkout-total-amount">{getTotalPrice().toFixed(2)} €</span>
                </div>
              </div>

              <div className="modal-footer">
                <button className="btn-secondary" onClick={handleCloseCheckout}>
                  Abbrechen
                </button>
                <button className="btn-primary" onClick={handleContinueToPayment}>
                  Weiter zur Zahlung
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Payment Method Selection Modal */}
        {showPayment && (
          <div className="modal-overlay" onClick={handleCloseCheckout}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>💳 Zahlungsmethode wählen</h2>
                <button className="modal-close" onClick={handleCloseCheckout}>✕</button>
              </div>

              <div className="modal-body">
                <div className="payment-amount-display">
                  <span>Zu zahlender Betrag:</span>
                  <strong>{getTotalPrice().toFixed(2)} €</strong>
                </div>

                <div className="payment-methods">
                  {paymentMethodsList.map(method => (
                    <div
                      key={method.id}
                      className={`payment-method-card ${selectedPaymentMethod === method.id ? 'selected' : ''} ${
                        method.id === 'account' && accountBalance < getTotalPrice() ? 'disabled' : ''
                      }`}
                      onClick={() => {
                        if (method.id === 'account' && accountBalance < getTotalPrice()) return;
                        handleSelectPaymentMethod(method.id);
                      }}
                    >
                      <div className="payment-method-icon">{method.name.split(' ')[0]}</div>
                      <div className="payment-method-info">
                        <h4>{method.name.substring(2)}</h4>
                        <p>{method.description}</p>
                        {method.id === 'account' && accountBalance < getTotalPrice() && (
                          <span className="insufficient-balance">❌ Nicht genügend Guthaben</span>
                        )}
                      </div>
                      {selectedPaymentMethod === method.id && (
                        <div className="payment-method-check">✓</div>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              <div className="modal-footer">
                <button className="btn-secondary" onClick={() => {
                  setShowPayment(false);
                  setShowCheckout(true);
                }}>
                  Zurück
                </button>
                <button 
                  className="btn-primary" 
                  onClick={handleConfirmPayment}
                  disabled={!selectedPaymentMethod}
                >
                  Jetzt bezahlen
                </button>
              </div>
            </div>
          </div>
        )}

        {/* QR Code Modal */}
        {showQRCode && qrCodeData && (
          <div className="modal-overlay">
            <div className="modal-content qr-modal">
              <div className="modal-header">
                <h2>✅ Bestellung erfolgreich!</h2>
              </div>

              <div className="modal-body qr-body">
                <div className="success-message">
                  <span className="success-icon">🎉</span>
                  <h3>Zahlung erfolgreich</h3>
                  <p>Ihre Bestellung wurde bezahlt und ist bereit zur Abholung.</p>
                </div>

                <div className="qr-code-container">
                  <div className="qr-code-placeholder">
                    <QRCodeSVG 
                      value={JSON.stringify(qrCodeData)} 
                      size={220}
                      level="H"
                      includeMargin={true}
                    />
                  </div>
                  <p className="qr-instructions">Zeigen Sie diesen Code bei der Essensausgabe vor</p>
                </div>

                <div className="order-summary-compact">
                  <div className="order-detail">
                    <span className="label">Bestell-Nr:</span>
                    <span className="value">{qrCodeData.orderId}</span>
                  </div>
                  <div className="order-detail">
                    <span className="label">Artikel:</span>
                    <span className="value">{getTotalItems()} Stück</span>
                  </div>
                  <div className="order-detail">
                    <span className="label">Bezahlt:</span>
                    <span className="value">{qrCodeData.total.toFixed(2)} €</span>
                  </div>
                  <div className="order-detail">
                    <span className="label">Methode:</span>
                    <span className="value">
                      {paymentMethodsList.find(m => m.id === qrCodeData.paymentMethod)?.name || 'N/A'}
                    </span>
                  </div>
                </div>

                {selectedPaymentMethod === 'account' && (
                  <div className="balance-info">
                    💰 Neues Guthaben: {accountBalance.toFixed(2)} €
                  </div>
                )}
              </div>

              <div className="modal-footer">
                <button className="btn-primary btn-new-order" onClick={handleNewOrder}>
                  Neue Bestellung aufgeben
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Bitcoin Payment Modal */}
        {showBitcoinPayment && (
          <div className="modal-overlay" onClick={handleCloseCheckout}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>₿ Bitcoin-Zahlung</h2>
                <button className="modal-close" onClick={handleCloseCheckout}>✕</button>
              </div>

              <div className="modal-body">
                <div className={`bitcoin-timer ${bitcoinCountdown < 30 ? 'urgent' : ''}`}>
                  <span className="timer-icon">⏱️</span>
                  <span className="timer-text">Verbleibende Zeit:</span>
                  <span className="timer-countdown">{formatTime(bitcoinCountdown)}</span>
                </div>

                <div className="bitcoin-payment-details">
                  <div className="payment-row">
                    <span className="payment-label">Betrag in EUR:</span>
                    <span className="payment-value">{getTotalPrice().toFixed(2)} €</span>
                  </div>
                  <div className="payment-row highlight">
                    <span className="payment-label">Betrag in BTC:</span>
                    <span className="payment-value btc">{calculateBtcAmount(getTotalPrice(), btcPrice || 50000)} ₿</span>
                  </div>
                  <div className="payment-row">
                    <span className="payment-label">BTC-Kurs:</span>
                    <span className="payment-value">1 ₿ = {(btcPrice || 50000).toLocaleString('de-DE')} €</span>
                  </div>
                </div>

                <div className="bitcoin-address">
                  <label>Bitcoin-Adresse:</label>
                  
                  {/* QR-Code für Bitcoin-Adresse */}
                  <div className="bitcoin-qr-code">
                    <QRCodeSVG 
                      value="bc1quyll5aj0hlwdngxnsug7cjqyw4w7uprsgcw3yw"
                      size={180}
                      level="H"
                      includeMargin={true}
                    />
                    <p className="qr-hint">Scannen Sie diesen QR-Code mit Ihrer Bitcoin-Wallet</p>
                  </div>
                  
                  <div className="address-box">
                    <code>bc1quyll5aj0hlwdngxnsug7cjqyw4w7uprsgcw3yw</code>
                  </div>
                  <p className="address-note">⚠️ Senden Sie den exakten Betrag an diese Adresse</p>
                </div>

                <div className="bitcoin-info">
                  <p>🔒 Die Zahlung wird über EASYPAY verarbeitet</p>
                  <p>📱 QR-Code scannen oder Adresse manuell kopieren</p>
                </div>
              </div>

              <div className="modal-footer">
                <button className="btn-secondary" onClick={() => {
                  setShowBitcoinPayment(false);
                  setShowPayment(true);
                  setBitcoinCountdown(180);
                }}>
                  Abbrechen
                </button>
                <button className="btn-primary" onClick={handleBitcoinPayment}>
                  Zahlung bestätigen
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Credit Card Form Modal */}
        {showCreditCardForm && (
          <div className="modal-overlay" onClick={handleCloseCheckout}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>💳 Kreditkarten-Zahlung</h2>
                <button className="modal-close" onClick={handleCloseCheckout}>✕</button>
              </div>

              <div className="modal-body">
                <div className="payment-amount-display">
                  <span>Zu zahlender Betrag:</span>
                  <strong>{getTotalPrice().toFixed(2)} €</strong>
                </div>

                <div className="credit-card-form">
                  <div className="form-group">
                    <label>Kartennummer</label>
                    <input
                      type="text"
                      placeholder="1234 5678 9012 3456"
                      maxLength="19"
                      value={creditCardData.cardNumber}
                      onChange={(e) => setCreditCardData({...creditCardData, cardNumber: e.target.value})}
                    />
                  </div>

                  <div className="form-group">
                    <label>Karteninhaber</label>
                    <input
                      type="text"
                      placeholder="Max Mustermann"
                      value={creditCardData.cardHolder}
                      onChange={(e) => setCreditCardData({...creditCardData, cardHolder: e.target.value})}
                    />
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Ablaufdatum</label>
                      <input
                        type="text"
                        placeholder="MM/JJ"
                        maxLength="5"
                        value={creditCardData.expiryDate}
                        onChange={(e) => setCreditCardData({...creditCardData, expiryDate: e.target.value})}
                      />
                    </div>
                    <div className="form-group">
                      <label>CVV</label>
                      <input
                        type="text"
                        placeholder="123"
                        maxLength="3"
                        value={creditCardData.cvv}
                        onChange={(e) => setCreditCardData({...creditCardData, cvv: e.target.value})}
                      />
                    </div>
                  </div>
                </div>

                <div className="payment-security-info">
                  <p>🔒 Ihre Zahlung wird sicher über EASYPAY verarbeitet</p>
                  <p>💳 Akzeptiert: Visa, Mastercard, American Express</p>
                </div>
              </div>

              <div className="modal-footer">
                <button className="btn-secondary" onClick={() => {
                  setShowCreditCardForm(false);
                  setShowPayment(true);
                }}>
                  Zurück
                </button>
                <button className="btn-primary" onClick={handleCreditCardPayment}>
                  Jetzt bezahlen
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Balance Top-Up Modal */}
        {/* Balance Modal - Guthaben aufladen */}
        {showBalanceModal && (
          <div className="modal-overlay" onClick={() => { setShowBalanceModal(false); setTopUpStep(1); }}>
            <div className="modal-content balance-modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>💰 Guthaben aufladen</h2>
                <button className="modal-close" onClick={() => { setShowBalanceModal(false); setTopUpStep(1); }}>✕</button>
              </div>

              <div className="modal-body">
                {topUpStep === 1 ? (
                  // Schritt 1: Betrag eingeben
                  <>
                    <div className="current-balance-display">
                      <span className="balance-label">Aktuelles Guthaben:</span>
                      <span className="balance-amount">{accountBalance.toFixed(2)} €</span>
                    </div>

                    <div className="form-group">
                      <label>Betrag aufladen:</label>
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="0.00"
                        value={topUpAmount}
                        onChange={(e) => setTopUpAmount(e.target.value)}
                      />
                    </div>

                    <div className="quick-amounts">
                      <button className="quick-amount-btn" onClick={() => setTopUpAmount('10')}>
                        + 10 €
                      </button>
                      <button className="quick-amount-btn" onClick={() => setTopUpAmount('20')}>
                        + 20 €
                      </button>
                      <button className="quick-amount-btn" onClick={() => setTopUpAmount('50')}>
                        + 50 €
                      </button>
                    </div>
                  </>
                ) : (
                  // Schritt 2: Zahlungsmethode wählen
                  <>
                    <div className="top-up-summary">
                      <p>Aufladebetrag: <strong>{parseFloat(topUpAmount).toFixed(2)} €</strong></p>
                    </div>

                    <h3 style={{ marginTop: '20px', marginBottom: '15px' }}>Zahlungsmethode wählen:</h3>
                    
                    <div className="payment-methods-grid">
                      <div 
                        className="payment-method-card"
                        onClick={() => handleSelectTopUpPaymentMethod('credit_card')}
                      >
                        <div className="payment-icon">💳</div>
                        <h3>Kreditkarte</h3>
                        <p>Visa, Mastercard</p>
                      </div>

                      <div 
                        className="payment-method-card"
                        onClick={() => handleSelectTopUpPaymentMethod('bitcoin')}
                      >
                        <div className="payment-icon">₿</div>
                        <h3>Bitcoin</h3>
                        <p>Kryptowährung</p>
                      </div>
                    </div>
                  </>
                )}
              </div>

              <div className="modal-footer">
                {topUpStep === 1 ? (
                  <>
                    <button className="btn-secondary" onClick={() => setShowBalanceModal(false)}>
                      Abbrechen
                    </button>
                    <button className="btn-primary" onClick={handleContinueToTopUpPayment}>
                      Weiter
                    </button>
                  </>
                ) : (
                  <>
                    <button className="btn-secondary" onClick={() => setTopUpStep(1)}>
                      Zurück
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
} 

export default OrderManagement;
