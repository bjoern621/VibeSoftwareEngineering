import React, { useState } from 'react';
import BillingForm from './BillingForm';
import PaymentMethodSelector from './PaymentMethodSelector';
import OrderSummary from './OrderSummary';
import CheckoutSuccess from './CheckoutSuccess';
import CheckoutTimer from './CheckoutTimer';
import CheckoutButton from './CheckoutButton';
import { purchaseTicket } from '../../api/checkoutApi';

const CheckoutPage = ({ holdId, orderSummary }) => {
    const [billingDetails, setBillingDetails] = useState({});
    const [paymentMethod, setPaymentMethod] = useState('creditcard');
    const [timerActive, setTimerActive] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [orderDetails, setOrderDetails] = useState(null);

    const handleTimerExpire = () => {
        setTimerActive(false);
        setError('Hold ist abgelaufen.');
    };

    const handleCheckout = async () => {
        if (!timerActive) return;

        setLoading(true);
        setError(null);

        try {
            const order = await purchaseTicket(
                holdId,
                billingDetails,
                paymentMethod
            );

            if (!order?.id) {
                throw new Error('Ungültige Order-Antwort');
            }

            setOrderDetails(order);
            setSuccess(true);
        } catch (e) {
            setError(
                e.response?.data?.message ||
                e.message ||
                'Fehler beim Kauf.'
            );
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return <CheckoutSuccess order={orderDetails} />;
    }

    return (
        <div className="checkout-container max-w-4xl mx-auto p-6">
            <div className="flex flex-col md:flex-row gap-8">
                <div className="flex-1 bg-white rounded-lg shadow p-6">
                    <CheckoutTimer
                        active={timerActive}
                        onExpire={handleTimerExpire}
                    />

                    <BillingForm
                        value={billingDetails}
                        onChange={setBillingDetails}
                        disabled={!timerActive || loading}
                    />

                    <PaymentMethodSelector
                        value={paymentMethod}
                        onChange={setPaymentMethod}
                        disabled={!timerActive || loading}
                    />

                    <CheckoutButton
                        onCheckout={handleCheckout}
                        loading={loading}
                        disabled={!timerActive}
                        error={error}
                    />
                </div>

                <div className="w-full md:w-96">
                    <OrderSummary summary={orderSummary} />
                </div>
            </div>
        </div>
    );
};

export default CheckoutPage;
