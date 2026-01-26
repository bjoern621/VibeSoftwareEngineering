import { purchaseBulkTickets, purchaseTicket } from '../../api/checkoutApi';

/**
 * Integration tests for checkout API functions.
 * Tests the actual fetch calls with mocked responses.
 */
describe('Integration: Checkout Flow (Mock API via fetch)', () => {
    const originalFetch = global.fetch;

    beforeEach(() => {
        global.fetch = jest.fn();
        localStorage.setItem('token', 'test-jwt-token');
    });

    afterEach(() => {
        jest.resetAllMocks();
        global.fetch = originalFetch;
        localStorage.clear();
    });

    describe('purchaseBulkTickets', () => {
        it('sends correct payload with holdId, userId, and paymentMethod', async () => {
            global.fetch.mockResolvedValue({
                ok: true,
                json: async () => ({
                    orderId: 123,
                    status: 'CONFIRMED',
                }),
            });

            const results = await purchaseBulkTickets(
                [101, 102],  // Use numeric IDs that can be parsed
                'user@example.com',
                'CREDIT_CARD'
            );

            // Should have called fetch twice (once per hold)
            expect(global.fetch).toHaveBeenCalledTimes(2);

            // Check first call payload
            const firstCallBody = JSON.parse(global.fetch.mock.calls[0][1].body);
            expect(firstCallBody.holdId).toBe(101);
            expect(firstCallBody.userId).toBe('user@example.com');
            expect(firstCallBody.paymentMethod).toBe('CREDIT_CARD');

            // Check results
            expect(results).toHaveLength(2);
            expect(results[0].success).toBe(true);
        });

        it('handles API errors gracefully', async () => {
            global.fetch.mockResolvedValue({
                ok: false,
                json: async () => ({ message: 'Reservation abgelaufen' }),
            });

            const results = await purchaseBulkTickets(
                ['123'],
                'user@example.com',
                'PAYPAL'
            );

            expect(results[0].success).toBe(false);
            expect(results[0].error).toBe('Reservation abgelaufen');
        });
    });

    describe('purchaseTicket', () => {
        it('sends correct payload for single ticket purchase', async () => {
            global.fetch.mockResolvedValue({
                ok: true,
                json: async () => ({
                    orderId: 456,
                    status: 'CONFIRMED',
                }),
            });

            const result = await purchaseTicket(123, 'user@example.com', 'PAYPAL');

            expect(global.fetch).toHaveBeenCalledTimes(1);
            expect(global.fetch).toHaveBeenCalledWith(
                '/api/orders',
                expect.objectContaining({
                    method: 'POST',
                    headers: expect.objectContaining({
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer test-jwt-token',
                    }),
                })
            );

            const body = JSON.parse(global.fetch.mock.calls[0][1].body);
            expect(body.holdId).toBe(123);
            expect(body.userId).toBe('user@example.com');
            expect(body.paymentMethod).toBe('PAYPAL');

            expect(result.orderId).toBe(456);
        });

        it('throws error when API returns non-ok', async () => {
            global.fetch.mockResolvedValue({
                ok: false,
                json: async () => ({ message: 'Interner Serverfehler' }),
            });

            await expect(
                purchaseTicket(999, 'user@example.com')
            ).rejects.toThrow('Interner Serverfehler');
        });
    });
});
