/**
 * Purchase a single ticket using a hold
 * @param {string} holdId - Hold ID from backend
 * @param {Object} billingDetails - Billing information
 * @param {string} paymentMethod - Payment method
 * @returns {Promise<Object>} Order data
 */
export async function purchaseTicket(holdId, billingDetails, paymentMethod) {
  const token = localStorage.getItem('token');
  
  const res = await fetch('/api/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
    },
    body: JSON.stringify({
      holdId,
      billingDetails,
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
 * @returns {Promise<Array>} Array of results with success/error status
 */
export async function purchaseBulkTickets(holdIds) {
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
          holdId,
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
