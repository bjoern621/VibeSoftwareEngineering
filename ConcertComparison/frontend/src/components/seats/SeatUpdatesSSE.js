import { useEffect, useRef, useState } from 'react';

/**
 * SSE hook for real-time seat status updates.
 * Maintains the latest status per seatId (state-of-the-world), not an event log.
 */
const useSeatUpdatesSSE = (concertId) => {
    const [seatStatusById, setSeatStatusById] = useState({});
    const [error, setError] = useState(null);

    const retryTimeoutRef = useRef(null);
    const eventSourceRef = useRef(null);
    const cancelledRef = useRef(false);

    useEffect(() => {
        cancelledRef.current = false;

        // Reset state when concert changes (optional but usually desired)
        setSeatStatusById({});
        setError(null);

        const cleanupCurrentConnection = () => {
            if (retryTimeoutRef.current) {
                clearTimeout(retryTimeoutRef.current);
                retryTimeoutRef.current = null;
            }
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
                eventSourceRef.current = null;
            }
        };

        const connect = () => {
            if (cancelledRef.current) return;

            // Ensure we never have multiple open connections/timers
            cleanupCurrentConnection();

            const es = new EventSource(`/api/concerts/${concertId}/seats/stream`);
            eventSourceRef.current = es;

            es.onmessage = (event) => {
                if (cancelledRef.current) return;

                try {
                    const data = JSON.parse(event.data);
                    // Expect: { seatId: number|string, status: string }
                    if (data && data.seatId != null) {
                        setSeatStatusById((prev) => ({
                            ...prev,
                            [data.seatId]: data.status,
                        }));
                    }
                    // Clear error on successful traffic (optional)
                    if (error) setError(null);
                } catch (e) {
                    // Keep running; do not crash hook due to one bad event
                    // eslint-disable-next-line no-console
                    console.error('Error parsing SSE data:', e);
                }
            };

            es.onerror = () => {
                if (cancelledRef.current) return;

                setError('Connection lost. Retrying...');

                // Close current connection and schedule reconnect
                if (eventSourceRef.current) {
                    eventSourceRef.current.close();
                    eventSourceRef.current = null;
                }

                // Prevent timer stacking on repeated errors
                if (retryTimeoutRef.current) {
                    clearTimeout(retryTimeoutRef.current);
                    retryTimeoutRef.current = null;
                }

                retryTimeoutRef.current = setTimeout(() => {
                    connect();
                }, 5000);
            };
        };

        connect();

        return () => {
            cancelledRef.current = true;
            cleanupCurrentConnection();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [concertId]);

    return { seatStatusById, error };
};

export default useSeatUpdatesSSE;
