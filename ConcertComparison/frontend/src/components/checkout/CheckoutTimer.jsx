import React, { useEffect, useState } from 'react';

const CheckoutTimer = ({ active, onExpire, initialSeconds = 600 }) => {
    const [secondsLeft, setSecondsLeft] = useState(initialSeconds);

    useEffect(() => {
        if (!active) return;

        if (secondsLeft <= 0) {
            onExpire();
            return;
        }

        const interval = setInterval(() => {
            setSecondsLeft((s) => s - 1);
        }, 1000);

        return () => clearInterval(interval);
    }, [active, secondsLeft, onExpire]);

    if (!active) {
        return (
            <div className="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-2 rounded mb-4">
                Hold abgelaufen. Bitte erneut reservieren.
            </div>
        );
    }

    const min = String(Math.floor(secondsLeft / 60)).padStart(2, '0');
    const sec = String(secondsLeft % 60).padStart(2, '0');


    return (
        <div className="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-2 rounded mb-4">
            <span className="font-bold">High Demand!</span>{' '}
            Ihre Tickets sind reserviert: <span>{min}:{sec}</span>
        </div>
    );
};

export default CheckoutTimer;
