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
