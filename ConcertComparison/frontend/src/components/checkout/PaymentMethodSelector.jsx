import React from 'react';

const PaymentMethodSelector = ({ value, onChange, disabled }) => {
    return (
        <div className="mb-6">
            <h2 className="text-lg font-semibold mb-2">Payment Method</h2>

            <div className="flex gap-4">
                {['creditcard', 'paypal', 'crypto'].map((method) => (
                    <button
                        key={method}
                        type="button"
                        disabled={disabled}
                        onClick={() => onChange(method)}
                        className={`payment-btn ${
                            value === method ? 'selected' : ''
                        }`}
                    >
                        {method === 'creditcard' && 'Credit Card'}
                        {method === 'paypal' && 'PayPal'}
                        {method === 'crypto' && 'Crypto'}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default PaymentMethodSelector;
