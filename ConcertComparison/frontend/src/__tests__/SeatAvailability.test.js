import { render, screen, act } from '@testing-library/react';
import SeatAvailability from '../components/seats/SeatAvailability';
import useSeatUpdatesSSE from '../components/seats/SeatUpdatesSSE';

describe('SeatAvailability (integration with SSE mock)', () => {
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

    it('should render updates pushed via SSE', () => {
        render(<SeatAvailability concertId={1} />);

        expect(global.EventSource).toHaveBeenCalledWith('/api/concerts/1/seats/stream');
        const es = instances[0];

        act(() => {
            es.onmessage({ data: JSON.stringify({ seatId: 1, status: 'reserved' }) });
        });

        expect(screen.getByText('Seat 1: reserved')).toBeInTheDocument();

        act(() => {
            es.onmessage({ data: JSON.stringify({ seatId: 1, status: 'available' }) });
        });

        // Seat 1 should now show latest status
        expect(screen.getByText('Seat 1: available')).toBeInTheDocument();
    });

    it('should show error message and reconnect after 5s', () => {
        render(<SeatAvailability concertId={1} />);
        const es = instances[0];

        act(() => {
            es.onerror();
        });

        expect(screen.getByText('Connection lost. Retrying...')).toBeInTheDocument();

        act(() => {
            jest.advanceTimersByTime(5000);
        });

        expect(global.EventSource).toHaveBeenCalledTimes(2);
        expect(global.EventSource).toHaveBeenLastCalledWith('/api/concerts/1/seats/stream');
    });
});
