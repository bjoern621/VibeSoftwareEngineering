import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import CheckoutPage from '../../components/checkout/CheckoutPage';
import { purchaseTicket } from '../../api/checkoutApi';

jest.mock('../../api/checkoutApi', () => ({
    purchaseTicket: jest.fn(),
}));

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

const renderCheckout = (props = {}) =>
    render(
        <CheckoutPage
            holdId="test-hold-id"
            orderSummary={mockOrderSummary}
            {...props}
        />
    );

// Microtask flush ohne setTimeout (wichtig bei Fake Timers)
const flushMicrotasks = async () => {
    await act(async () => {
        await Promise.resolve();
    });
};

describe('CheckoutPage – Checkout Flow', () => {
    afterEach(() => {
        jest.clearAllMocks();
        jest.useRealTimers();
    });

    it('completes checkout and shows confirmation screen with order id', async () => {
        purchaseTicket.mockResolvedValue({
            id: 'order123',
            eventName: 'Test Concert',
            ticketCount: 2,
            total: 115,
            status: 'PAID',
        });

        renderCheckout();

        fireEvent.click(screen.getByTestId('checkout-btn'));

        expect(await screen.findByText(/Kauf erfolgreich/i)).toBeInTheDocument();
        expect(screen.getByText(/order123/)).toBeInTheDocument();

        expect(purchaseTicket).toHaveBeenCalledTimes(1);
        expect(purchaseTicket).toHaveBeenCalledWith(
            'test-hold-id',
            expect.any(Object),
            'creditcard'
        );
    });

    it('shows error message when purchase fails', async () => {
        purchaseTicket.mockRejectedValue(new Error('Payment failed'));

        renderCheckout();

        fireEvent.click(screen.getByTestId('checkout-btn'));

        expect(await screen.findByText('Payment failed')).toBeInTheDocument();
        expect(purchaseTicket).toHaveBeenCalledTimes(1);
    });

    it('disables checkout when hold expires', async () => {
        jest.useFakeTimers();

        renderCheckout();

        // Zeit vorspulen
        act(() => {
            jest.advanceTimersByTime(600_000);
        });

        // ggf. noch offene Interval/Effect-Jobs abarbeiten
        act(() => {
            jest.runOnlyPendingTimers();
        });

        await flushMicrotasks();

        expect(await screen.findByText(/Hold ist abgelaufen/i)).toBeInTheDocument();
        expect(screen.getByTestId('checkout-btn')).toBeDisabled();
    });

    it('does not trigger checkout when button is disabled (expired hold)', async () => {
        jest.useFakeTimers();
        purchaseTicket.mockResolvedValue({ id: 'order123' });

        renderCheckout();

        act(() => {
            jest.advanceTimersByTime(600_000);
        });

        act(() => {
            jest.runOnlyPendingTimers();
        });

        await flushMicrotasks();

        // Button disabled -> click sollte keinen Call auslösen
        fireEvent.click(screen.getByTestId('checkout-btn'));
        expect(purchaseTicket).not.toHaveBeenCalled();
    });
});
