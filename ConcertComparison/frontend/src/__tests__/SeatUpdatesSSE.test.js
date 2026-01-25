import { renderHook, act } from '@testing-library/react';
import useSeatUpdatesSSE from '../components/seats/SeatUpdatesSSE';

describe('useSeatUpdatesSSE', () => {
    let instances = [];

    beforeEach(() => {
        instances = [];

        global.EventSource = jest.fn(function (url) {
            this.url = url;
            this.onmessage = null;
            this.onerror = null;
            this.close = jest.fn();
            instances.push(this);
            return this;
        });

        jest.useFakeTimers();
    });

    afterEach(() => {
        jest.clearAllTimers();
        jest.useRealTimers();
        delete global.EventSource;
    });

    it('should update seatStatusById on incoming SSE events', () => {
        const { result } = renderHook(() => useSeatUpdatesSSE(1));

        expect(global.EventSource).toHaveBeenCalledWith('/api/concerts/1/seats/stream');
        const es = instances[0];

        act(() => {
            es.onmessage({ data: JSON.stringify({ seatId: 1, status: 'reserved' }) });
            es.onmessage({ data: JSON.stringify({ seatId: 2, status: 'available' }) });
        });

        expect(result.current.seatStatusById).toEqual({
            1: 'reserved',
            2: 'available',
        });
        expect(result.current.error).toBe(null);
    });

    it('should handle connection errors and retry without stacking timers', () => {
        renderHook(() => useSeatUpdatesSSE(1));
        const es = instances[0];

        act(() => {
            es.onerror();
            es.onerror(); // repeated error should not schedule multiple reconnects
        });

        // One reconnect scheduled; still only one additional connection after time
        act(() => {
            jest.advanceTimersByTime(5000);
        });

        expect(global.EventSource).toHaveBeenCalledTimes(2);
        expect(global.EventSource).toHaveBeenLastCalledWith('/api/concerts/1/seats/stream');
    });

    it('should cleanup on unmount: close EventSource and prevent reconnect', () => {
        const { unmount } = renderHook(() => useSeatUpdatesSSE(1));
        const es = instances[0];

        act(() => {
            es.onerror(); // schedules reconnect
        });

        unmount();

        expect(es.close).toHaveBeenCalled();

        // Even if timers advance, no new connection should be created
        act(() => {
            jest.advanceTimersByTime(5000);
        });

        expect(global.EventSource).toHaveBeenCalledTimes(1);
    });
});
