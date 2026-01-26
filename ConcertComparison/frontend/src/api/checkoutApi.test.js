import { processPayment } from './checkoutApi';

describe('processPayment', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe('Credit Card payments', () => {
    test('returns success for valid credit card', async () => {
      const paymentPromise = processPayment(
        {
          cardNumber: '4111111111111111',
          cardHolder: 'MAX MUSTERMANN',
          expiryDate: '12/30',
          cvv: '123',
        },
        'creditcard',
        100
      );

      // Fast-forward timer
      jest.advanceTimersByTime(5000);

      const result = await paymentPromise;

      expect(result.success).toBe(true);
      expect(result.transactionId).toMatch(/^TXN-/);
      expect(result.message).toContain('****1111');
    });

    test('returns failure for declined test card', async () => {
      const paymentPromise = processPayment(
        {
          cardNumber: '4000000000000002',
          cardHolder: 'MAX MUSTERMANN',
          expiryDate: '12/30',
          cvv: '123',
        },
        'creditcard',
        100
      );

      jest.advanceTimersByTime(5000);

      const result = await paymentPromise;

      expect(result.success).toBe(false);
      expect(result.message).toContain('abgelehnt');
    });
  });

  describe('PayPal payments', () => {
    test('returns success for connected PayPal account', async () => {
      const paymentPromise = processPayment(
        {
          email: 'test@example.com',
          isConnected: true,
        },
        'paypal',
        100
      );

      jest.advanceTimersByTime(5000);

      const result = await paymentPromise;

      expect(result.success).toBe(true);
      expect(result.transactionId).toMatch(/^TXN-/);
      expect(result.message).toContain('test@example.com');
    });

    test('returns failure for not connected PayPal', async () => {
      const paymentPromise = processPayment(
        {
          email: 'test@example.com',
          isConnected: false,
        },
        'paypal',
        100
      );

      jest.advanceTimersByTime(5000);

      const result = await paymentPromise;

      expect(result.success).toBe(false);
      expect(result.message).toContain('nicht verbunden');
    });
  });

  describe('Bitcoin payments', () => {
    test('returns success for confirmed Bitcoin payment', async () => {
      const paymentPromise = processPayment(
        {
          hasConfirmed: true,
          btcAmount: '0.00238095',
          transactionId: 'abc123',
        },
        'bitcoin',
        100
      );

      jest.advanceTimersByTime(5000);

      const result = await paymentPromise;

      expect(result.success).toBe(true);
      expect(result.transactionId).toMatch(/^TXN-/);
      expect(result.message).toContain('Bitcoin');
    });

    test('returns failure for unconfirmed Bitcoin payment', async () => {
      const paymentPromise = processPayment(
        {
          hasConfirmed: false,
          btcAmount: '0.00238095',
        },
        'bitcoin',
        100
      );

      jest.advanceTimersByTime(5000);

      const result = await paymentPromise;

      expect(result.success).toBe(false);
      expect(result.message).toContain('nicht bestätigt');
    });
  });

  describe('Invalid payment method', () => {
    test('returns failure for unknown payment method', async () => {
      const paymentPromise = processPayment({}, 'unknown', 100);

      jest.advanceTimersByTime(5000);

      const result = await paymentPromise;

      expect(result.success).toBe(false);
      expect(result.message).toContain('Ungültige Zahlungsmethode');
    });
  });

  test('generates unique transaction IDs', async () => {
    const promise1 = processPayment(
      { cardNumber: '4111111111111111', cardHolder: 'TEST', expiryDate: '12/30', cvv: '123' },
      'creditcard',
      100
    );
    const promise2 = processPayment(
      { cardNumber: '4111111111111111', cardHolder: 'TEST', expiryDate: '12/30', cvv: '123' },
      'creditcard',
      100
    );

    jest.advanceTimersByTime(5000);

    const [result1, result2] = await Promise.all([promise1, promise2]);

    expect(result1.transactionId).not.toBe(result2.transactionId);
  });
});
