import React from 'react';

const CheckoutButton = ({ onCheckout, loading, disabled, error }) => {
    return (
        <div>
            <button
                className="btn btn-primary w-full"
                onClick={onCheckout}
                disabled={disabled || loading}
                data-testid="checkout-btn"
            >
                {loading ? 'Processing...' : 'Checkout'}
            </button>

            {error && (
                <p className="text-red-500 mt-2">{error}</p>
            )}
        </div>
    );
};

export default CheckoutButton;
