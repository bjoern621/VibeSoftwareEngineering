import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import CheckoutButton from '../../components/checkout/CheckoutButton';

describe('CheckoutButton', () => {
    it('calls onCheckout when clicked', () => {
        const onCheckout = jest.fn();

        render(
            <CheckoutButton
                onCheckout={onCheckout}
                loading={false}
                disabled={false}
            />
        );

        fireEvent.click(screen.getByText('Checkout'));
        expect(onCheckout).toHaveBeenCalledTimes(1);
    });

    it('shows loading state', () => {
        render(
            <CheckoutButton
                onCheckout={jest.fn()}
                loading={true}
                disabled={false}
            />
        );

        expect(screen.getByText('Processing...')).toBeInTheDocument();
        expect(screen.getByRole('button')).toBeDisabled();
    });

    it('shows error message', () => {
        render(
            <CheckoutButton
                onCheckout={jest.fn()}
                loading={false}
                disabled={false}
                error="Test Error"
            />
        );

        expect(screen.getByText('Test Error')).toBeInTheDocument();
    });
});
