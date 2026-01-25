import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import CheckoutPage from '../../components/checkout/CheckoutPage';

const mockOrderSummary = {
    eventName: 'Test Concert',
    category: 'VIP',
    date: '2026-03-01',
    ticketCount: 2,
    ticketPrice: 50,
    serviceFee: 5,
    tax: 10,
    total: 115,
};

describe('Integration: Checkout Flow (Mock API via fetch)', () => {
    beforeEach(() => {
        global.fetch = jest.fn();
    });

    afterEach(() => {
        jest.resetAllMocks();
    });

    it('completes checkout with mocked HTTP API', async () => {
        // Mock HTTP response for POST /api/orders
        global.fetch.mockResolvedValue({
            ok: true,
            json: async () => ({
                id: 'order999',
                eventName: 'Integration Test Event',
                ticketCount: 2,
                total: 115,
                status: 'PAID',
            }),
        });

        render(
            <CheckoutPage
                holdId="hold-integration"
                orderSummary={mockOrderSummary}
            />
        );

        fireEvent.click(screen.getByTestId('checkout-btn'));

        // Confirmation screen should appear
        expect(
            await screen.findByText(/Kauf erfolgreich/i)
        ).toBeInTheDocument();

        expect(screen.getByText(/order999/)).toBeInTheDocument();

        // Verify API called correctly
        expect(global.fetch).toHaveBeenCalledTimes(1);
        expect(global.fetch).toHaveBeenCalledWith(
            '/api/orders',
            expect.objectContaining({
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
            })
        );

        // Optional: check payload contains holdId
        const body = JSON.parse(global.fetch.mock.calls[0][1].body);
        expect(body.holdId).toBe('hold-integration');
    });

    it('shows backend error message when API returns non-ok', async () => {
        global.fetch.mockResolvedValue({
            ok: false,
            json: async () => ({ message: 'Interner Serverfehler' }),
            status: 500,
        });

        render(
            <CheckoutPage
                holdId="hold-integration"
                orderSummary={mockOrderSummary}
            />
        );

        fireEvent.click(screen.getByTestId('checkout-btn'));

        expect(
            await screen.findByText('Interner Serverfehler')
        ).toBeInTheDocument();
    });
});
