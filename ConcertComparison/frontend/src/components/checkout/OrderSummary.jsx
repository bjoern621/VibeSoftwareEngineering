import React from 'react';

const OrderSummary = ({ summary }) => {
    if (!summary) return null;

    return (
        <div className="bg-gray-50 rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold mb-4">Order Summary</h2>

            <div className="flex items-center gap-4 mb-4">
                {summary.image && (
                    <img
                        src={summary.image}
                        alt="Event"
                        className="w-16 h-16 rounded"
                    />
                )}
                <div>
                    <div className="font-bold">{summary.eventName}</div>
                    <div className="text-sm text-gray-600">{summary.category}</div>
                    <div className="text-sm text-gray-600">{summary.date}</div>
                </div>
            </div>

            <div className="flex justify-between mb-2">
                <span>Tickets (x{summary.ticketCount})</span>
                <span>${summary.ticketPrice}</span>
            </div>

            <div className="flex justify-between mb-2">
                <span>Service Fee</span>
                <span>${summary.serviceFee}</span>
            </div>

            <div className="flex justify-between mb-2">
                <span>Tax</span>
                <span>${summary.tax}</span>
            </div>

            <div className="flex justify-between font-bold text-lg mt-4">
                <span>Total</span>
                <span>${summary.total}</span>
            </div>
        </div>
    );
};

export default OrderSummary;
