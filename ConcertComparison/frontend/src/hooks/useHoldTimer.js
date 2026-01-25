import { useState, useEffect, useCallback, useRef } from "react";

/**
 * Custom hook for managing hold countdown timer
 * Handles TTL countdown, expiration, and cleanup
 *
 * @param {number} ttlSeconds - Time-to-live in seconds
 * @param {Function} onExpire - Callback when hold expires
 * @returns {Object} - Timer state and control functions
 *   - timeLeft: Remaining time in seconds
 *   - isExpired: Whether hold has expired
 *   - isActive: Whether timer is active
 *   - start: Start the timer
 *   - stop: Stop the timer
 *   - reset: Reset the timer
 */
export const useHoldTimer = (ttlSeconds = 600, onExpire) => {
    const [timeLeft, setTimeLeft] = useState(ttlSeconds);
    const [isActive, setIsActive] = useState(false);
    const [isExpired, setIsExpired] = useState(false);

    const intervalRef = useRef(null);
    const onExpireRef = useRef(onExpire);

    // Update ref when callback changes
    useEffect(() => {
        onExpireRef.current = onExpire;
    }, [onExpire]);

    // Calculate formatted time (MM:SS)
    const formattedTime = (() => {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
    })();

    // Calculate progress percentage
    const progressPercentage =
        ttlSeconds > 0 ? ((ttlSeconds - timeLeft) / ttlSeconds) * 100 : 0;

    // Start the timer
    const start = useCallback(() => {
        setIsActive(true);
        setIsExpired(false);
        setTimeLeft(ttlSeconds);
    }, [ttlSeconds]);

    // Stop the timer
    const stop = useCallback(() => {
        setIsActive(false);
        if (intervalRef.current) {
            clearInterval(intervalRef.current);
            intervalRef.current = null;
        }
    }, []);

    // Reset the timer
    const reset = useCallback(() => {
        stop();
        setTimeLeft(ttlSeconds);
        setIsExpired(false);
    }, [stop, ttlSeconds]);

    // Countdown effect
    useEffect(() => {
        if (!isActive) {
            return;
        }

        intervalRef.current = setInterval(() => {
            setTimeLeft((prevTime) => {
                if (prevTime <= 1) {
                    // Timer expired
                    stop();
                    setIsExpired(true);
                    if (onExpireRef.current) {
                        onExpireRef.current();
                    }
                    return 0;
                }
                return prevTime - 1;
            });
        }, 1000);

        return () => {
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
                intervalRef.current = null;
            }
        };
    }, [isActive, stop]);

    // Cleanup on unmount
    useEffect(() => {
        return () => {
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
                intervalRef.current = null;
            }
        };
    }, []);

    return {
        timeLeft,
        formattedTime,
        progressPercentage,
        isActive,
        isExpired,
        start,
        stop,
        reset,
    };
};
