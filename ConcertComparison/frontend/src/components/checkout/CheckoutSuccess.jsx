import React from 'react';

const CheckoutSuccess = ({ order }) => {
    if (!order) return null;

    return (
        <div className="flex flex-col items-center justify-center min-h-[60vh]">
            <div className="bg-green-100 border border-green-400 text-green-700 px-6 py-4 rounded mb-6">
                <h2 className="text-2xl font-bold mb-2">
                    Kauf erfolgreich!
                </h2>
                <p>
                    Ihre Order-ID:{' '}
                    <span className="font-mono">{order.id}</span>
                </p>
            </div>

            <div className="bg-white rounded-lg shadow p-6 w-full max-w-md">
                <h3 className="font-semibold mb-2">Order Details</h3>
                <div>Event: {order.eventName}</div>
                <div>Tickets: {order.ticketCount}</div>
                <div>Total: ${order.total}</div>
                <div>Status: {order.status}</div>
            </div>
        </div>
    );
};

export default CheckoutSuccess;
