// API-Service für Ticket-Kauf
export async function purchaseTicket(holdId, billingDetails, paymentMethod) {
  // POST /api/orders
  const res = await fetch('/api/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
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
