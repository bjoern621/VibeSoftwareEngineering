/**
 * Purchase a single ticket using a hold
 * @param {string|number} holdId - Hold ID from backend
 * @param {string} userId - User email (required by backend)
 * @param {string} paymentMethod - Payment method (e.g., 'CREDIT_CARD', 'PAYPAL')
 * @returns {Promise<Object>} Order data
 */
export async function purchaseTicket(holdId, userId, paymentMethod = 'CREDIT_CARD') {
  const token = localStorage.getItem('token');
  
  const res = await fetch('/api/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
    },
    body: JSON.stringify({
      holdId: parseInt(holdId, 10),  // Backend expects Long
      userId,
      paymentMethod,
    }),
  });
  
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Fehler beim Kauf.');
  }
  
  return await res.json();
}

/**
 * Mock payment processing with simulated delay
 * In real implementation, this would call a payment gateway
 * @param {object} paymentDetails - Payment form data (card info, PayPal email, etc.)
 * @param {string} paymentMethod - Payment method ('creditcard', 'paypal', 'bitcoin')
 * @param {number} amount - Amount to charge
 * @returns {Promise<object>} - { success: boolean, transactionId: string, message?: string }
 */
export async function processPayment(paymentDetails, paymentMethod, amount) {
  // Simulate payment gateway delay (1.5-3 seconds)
  const delay = 1500 + Math.random() * 1500;
  await new Promise(resolve => setTimeout(resolve, delay));
  
  // Generate mock transaction ID
  const transactionId = `TXN-${Date.now()}-${Math.random().toString(36).substr(2, 9).toUpperCase()}`;
  
  // Mock validation based on payment method
  switch (paymentMethod) {
    case 'creditcard': {
      const cardNum = (paymentDetails.cardNumber || '').replace(/\s/g, '');
      // Simulate card decline for test card numbers starting with 4000000000000002
      if (cardNum === '4000000000000002') {
        return {
          success: false,
          message: 'Karte wurde abgelehnt. Bitte verwenden Sie eine andere Zahlungsmethode.',
        };
      }
      return {
        success: true,
        transactionId,
        message: `Zahlung erfolgreich mit Karte ****${cardNum.slice(-4)}`,
      };
    }
    
    case 'paypal': {
      if (!paymentDetails.isConnected) {
        return {
          success: false,
          message: 'PayPal-Konto nicht verbunden.',
        };
      }
      return {
        success: true,
        transactionId,
        message: `Zahlung erfolgreich über PayPal (${paymentDetails.email})`,
      };
    }
    
    case 'bitcoin': {
      if (!paymentDetails.hasConfirmed) {
        return {
          success: false,
          message: 'Bitcoin-Zahlung nicht bestätigt.',
        };
      }
      return {
        success: true,
        transactionId,
        message: `Bitcoin-Zahlung bestätigt: ${paymentDetails.btcAmount} BTC`,
      };
    }
    
    default:
      return {
        success: false,
        message: 'Ungültige Zahlungsmethode.',
      };
  }
}

/**
 * Purchase multiple tickets in bulk
 * Note: Backend doesn't support bulk purchase, so we make sequential calls
 * @param {string[]} holdIds - Array of hold IDs
 * @param {string} userId - User email (required by backend)
 * @param {string} paymentMethod - Payment method (optional, default: CREDIT_CARD)
 * @returns {Promise<Array>} Array of results with success/error status
 */
export async function purchaseBulkTickets(holdIds, userId, paymentMethod = 'CREDIT_CARD') {
  const token = localStorage.getItem('token');
  const results = [];

  for (const holdId of holdIds) {
    try {
      const res = await fetch('/api/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` }),
        },
        body: JSON.stringify({
          holdId: parseInt(holdId, 10),  // Backend expects Long
          userId,
          paymentMethod,
        }),
      });

      if (res.ok) {
        const data = await res.json();
        results.push({
          holdId,
          success: true,
          data,
        });
      } else {
        const error = await res.json().catch(() => ({ message: 'Unknown error' }));
        results.push({
          holdId,
          success: false,
          error: error.message || 'Purchase failed',
        });
      }
    } catch (error) {
      results.push({
        holdId,
        success: false,
        error: error.message || 'Network error',
      });
    }
  }

  return results;
}
