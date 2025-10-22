/**
 * Preis- und Währungs-Utility-Funktionen
 * Zentrale Sammlung für alle Preis-Berechnungen und Formatierungen
 */

/**
 * Formatiert Preis als deutschen Währungsstring
 * 
 * @param {number} price - Preis in Euro
 * @param {number} decimals - Anzahl Dezimalstellen (Standard: 2)
 * @returns {string} Formatierter Preis (z.B. "12,50 €")
 * 
 * @example
 * formatPrice(12.5); // "12,50 €"
 * formatPrice(1234.56); // "1.234,56 €"
 */
export const formatPrice = (price, decimals = 2) => {
  return price.toLocaleString('de-DE', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }) + ' €';
};

/**
 * Formatiert Preis als String mit Punkt als Dezimaltrenner
 * 
 * @param {number} price - Preis in Euro
 * @returns {string} Formatierter Preis (z.B. "12.50")
 * 
 * @example
 * formatPriceSimple(12.5); // "12.50"
 */
export const formatPriceSimple = (price) => {
  return price.toFixed(2);
};

/**
 * Berechnet Gesamtpreis aus Warenkorb-Items
 * 
 * @param {Object} orderQuantities - Objekt mit Mengen {key: quantity}
 * @param {Object} weekMealPlans - Objekt mit Speiseplänen {date: mealPlans[]}
 * @returns {number} Gesamtpreis
 * 
 * @example
 * getTotalPrice({'2025-10-22_1': 2}, weekMealPlans); // 13.00
 */
export const calculateTotalPrice = (orderQuantities, weekMealPlans) => {
  let total = 0;
  
  Object.entries(orderQuantities).forEach(([key, quantity]) => {
    if (quantity > 0) {
      const [date, mealIdStr] = key.split('_');
      const mealId = parseInt(mealIdStr);
      
      const mealPlan = weekMealPlans[date]?.find(m => m.mealId === mealId);
      
      if (mealPlan && mealPlan.meal && mealPlan.meal.price) {
        const itemTotal = mealPlan.meal.price * quantity;
        total += itemTotal;
      }
    }
  });
  
  return total;
};

/**
 * Zählt Gesamtanzahl Items im Warenkorb
 * 
 * @param {Object} orderQuantities - Objekt mit Mengen {key: quantity}
 * @returns {number} Anzahl Items
 * 
 * @example
 * getCartItemCount({'2025-10-22_1': 2, '2025-10-23_3': 1}); // 3
 */
export const getCartItemCount = (orderQuantities) => {
  return Object.values(orderQuantities).reduce((sum, qty) => sum + qty, 0);
};

/**
 * Berechnet Bitcoin-Betrag basierend auf EUR-Betrag und BTC-Kurs
 * 
 * @param {number} eurAmount - Betrag in Euro
 * @param {number} btcPrice - Aktueller BTC-Kurs in EUR
 * @returns {string} BTC-Betrag mit 8 Dezimalstellen
 * 
 * @example
 * calculateBtcAmount(85, 85000); // "0.00100000"
 */
export const calculateBtcAmount = (eurAmount, btcPrice) => {
  if (!btcPrice || btcPrice === 0) return '0.00000000';
  const btc = eurAmount / btcPrice;
  return btc.toFixed(8);
};

/**
 * Formatiert Zahl mit deutschem Tausender-Trennzeichen
 * 
 * @param {number} number - Zu formatierende Zahl
 * @returns {string} Formatierte Zahl
 * 
 * @example
 * formatNumber(85342.50); // "85.342,50"
 */
export const formatNumber = (number) => {
  return number.toLocaleString('de-DE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};
