import { renderHook, act, waitFor } from '@testing-library/react';
import { useSeatSSE } from '../../hooks/useSeatSSE';
import { SSE_CONNECTION_STATUS } from '../../services/seatService';

// Mock EventSource
class MockEventSource {
  static instances = [];
  
  constructor(url) {
    this.url = url;
    this.readyState = 0;
    this.onopen = null;
    this.onmessage = null;
    this.onerror = null;
    this._listeners = {};
    MockEventSource.instances.push(this);
  }

  addEventListener(event, callback) {
    if (!this._listeners[event]) {
      this._listeners[event] = [];
    }
    this._listeners[event].push(callback);
  }

  removeEventListener(event, callback) {
    if (this._listeners[event]) {
      this._listeners[event] = this._listeners[event].filter(cb => cb !== callback);
    }
  }

  close() {
    this.readyState = 2;
  }

  // Helper für Tests: Simuliert Verbindungsaufbau
  simulateOpen() {
    this.readyState = 1;
    if (this.onopen) {
      this.onopen({ type: 'open' });
    }
  }

  // Helper für Tests: Simuliert Fehler
  simulateError() {
    if (this.onerror) {
      this.onerror({ type: 'error' });
    }
  }

  // Helper für Tests: Simuliert seat_update Event
  simulateSeatUpdate(data) {
    const event = { data: JSON.stringify(data) };
    if (this._listeners['seat_update']) {
      this._listeners['seat_update'].forEach(cb => cb(event));
    }
  }

  // Helper für Tests: Simuliert generische Message
  simulateMessage(data) {
    if (this.onmessage) {
      this.onmessage({ data: JSON.stringify(data) });
    }
  }

  static reset() {
    MockEventSource.instances = [];
  }

  static getLastInstance() {
    return MockEventSource.instances[MockEventSource.instances.length - 1];
  }
}

// EventSource global mocken
const originalEventSource = global.EventSource;

describe('useSeatSSE Hook', () => {
  beforeAll(() => {
    global.EventSource = MockEventSource;
  });

  afterAll(() => {
    global.EventSource = originalEventSource;
  });

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
    MockEventSource.reset();
    localStorage.clear();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe('Connection Establishment', () => {
    test('creates EventSource connection when enabled', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      const eventSource = MockEventSource.getLastInstance();
      expect(eventSource.url).toContain('/events/concert-123/seats/stream');
    });

    test('does not connect when disabled', () => {
      renderHook(() => useSeatSSE('concert-123', { enabled: false }));

      expect(MockEventSource.instances.length).toBe(0);
    });

    test('does not connect without concertId', () => {
      renderHook(() => useSeatSSE(null));

      expect(MockEventSource.instances.length).toBe(0);
    });

    test('sets connectionStatus to CONNECTING initially', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(result.current.connectionStatus).toBe(SSE_CONNECTION_STATUS.CONNECTING);
      });
    });

    test('sets connectionStatus to CONNECTED on successful open', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      expect(result.current.connectionStatus).toBe(SSE_CONNECTION_STATUS.CONNECTED);
      expect(result.current.isConnected).toBe(true);
    });

    test('includes JWT token in URL when available', async () => {
      localStorage.setItem('token', 'test-jwt-token');

      renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      const eventSource = MockEventSource.getLastInstance();
      expect(eventSource.url).toContain('token=test-jwt-token');
    });
  });

  describe('Event Handling', () => {
    test('calls onSeatUpdate callback when seat_update event received', async () => {
      const onSeatUpdate = jest.fn();
      
      renderHook(() => useSeatSSE('concert-123', { onSeatUpdate }));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      act(() => {
        MockEventSource.getLastInstance().simulateSeatUpdate({
          seatId: 'seat-1',
          status: 'SOLD',
          timestamp: '2026-01-26T10:00:00Z',
        });
      });

      expect(onSeatUpdate).toHaveBeenCalledWith('seat-1', 'SOLD');
    });

    test('updates lastEvent state when event received', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      act(() => {
        MockEventSource.getLastInstance().simulateSeatUpdate({
          seatId: 'seat-1',
          status: 'HELD',
          timestamp: '2026-01-26T10:00:00Z',
        });
      });

      expect(result.current.lastEvent).toMatchObject({
        seatId: 'seat-1',
        status: 'HELD',
      });
      expect(result.current.lastEvent.receivedAt).toBeDefined();
    });

    test('handles generic message as seat_update if it has seatId and status', async () => {
      const onSeatUpdate = jest.fn();
      
      renderHook(() => useSeatSSE('concert-123', { onSeatUpdate }));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      act(() => {
        MockEventSource.getLastInstance().simulateMessage({
          seatId: 'seat-2',
          status: 'AVAILABLE',
        });
      });

      expect(onSeatUpdate).toHaveBeenCalledWith('seat-2', 'AVAILABLE');
    });
  });

  describe('Auto-Reconnect', () => {
    test('sets connectionStatus to RECONNECTING on error', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      act(() => {
        MockEventSource.getLastInstance().simulateError();
      });

      expect(result.current.connectionStatus).toBe(SSE_CONNECTION_STATUS.RECONNECTING);
      expect(result.current.isReconnecting).toBe(true);
    });

    test('attempts to reconnect after error with exponential backoff', async () => {
      renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      // Erster Fehler
      act(() => {
        MockEventSource.getLastInstance().simulateError();
      });

      expect(MockEventSource.instances.length).toBe(1);

      // Nach 1 Sekunde (erster Reconnect-Versuch)
      act(() => {
        jest.advanceTimersByTime(1000);
      });

      expect(MockEventSource.instances.length).toBe(2);
    });

    test('increases delay exponentially on subsequent errors', async () => {
      renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      // Erster Fehler, Reconnect nach 1s
      act(() => {
        MockEventSource.getLastInstance().simulateError();
        jest.advanceTimersByTime(1000);
      });

      expect(MockEventSource.instances.length).toBe(2);

      // Zweiter Fehler, Reconnect nach 2s
      act(() => {
        MockEventSource.getLastInstance().simulateError();
        jest.advanceTimersByTime(1000); // Noch nicht
      });

      expect(MockEventSource.instances.length).toBe(2);

      act(() => {
        jest.advanceTimersByTime(1000); // Jetzt (gesamt 2s)
      });

      expect(MockEventSource.instances.length).toBe(3);
    });

    test('resets reconnect counter after successful connection', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      // Fehler und Reconnect
      act(() => {
        MockEventSource.getLastInstance().simulateError();
        jest.advanceTimersByTime(1000);
      });

      // Erfolgreiche Verbindung
      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      expect(result.current.isConnected).toBe(true);

      // Neuer Fehler sollte wieder bei 1s Delay starten
      act(() => {
        MockEventSource.getLastInstance().simulateError();
        jest.advanceTimersByTime(1000);
      });

      expect(MockEventSource.instances.length).toBe(3);
    });
  });

  describe('Cleanup', () => {
    test('closes EventSource on unmount', async () => {
      const { unmount } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      const eventSource = MockEventSource.getLastInstance();
      const closeSpy = jest.spyOn(eventSource, 'close');

      unmount();

      expect(closeSpy).toHaveBeenCalled();
    });

    test('closes EventSource when concertId changes', async () => {
      const { rerender } = renderHook(
        ({ concertId }) => useSeatSSE(concertId),
        { initialProps: { concertId: 'concert-123' } }
      );

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      const firstInstance = MockEventSource.getLastInstance();
      const closeSpy = jest.spyOn(firstInstance, 'close');

      rerender({ concertId: 'concert-456' });

      await waitFor(() => {
        expect(closeSpy).toHaveBeenCalled();
        expect(MockEventSource.instances.length).toBe(2);
      });
    });

    test('clears reconnect timeout on unmount', async () => {
      const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');

      const { unmount } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      // Trigger Reconnect
      act(() => {
        MockEventSource.getLastInstance().simulateError();
      });

      unmount();

      expect(clearTimeoutSpy).toHaveBeenCalled();
      clearTimeoutSpy.mockRestore();
    });

    test('sets status to CLOSED on disconnect', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      expect(result.current.isConnected).toBe(true);

      act(() => {
        result.current.disconnect();
      });

      expect(result.current.connectionStatus).toBe(SSE_CONNECTION_STATUS.CLOSED);
    });
  });

  describe('Manual Controls', () => {
    test('reconnect() closes existing and creates new connection', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        MockEventSource.getLastInstance().simulateOpen();
      });

      const firstInstance = MockEventSource.getLastInstance();
      const closeSpy = jest.spyOn(firstInstance, 'close');

      act(() => {
        result.current.reconnect();
        jest.advanceTimersByTime(100); // Kurze Verzögerung im reconnect
      });

      expect(closeSpy).toHaveBeenCalled();
      expect(MockEventSource.instances.length).toBe(2);
    });

    test('disconnect() prevents further reconnect attempts', async () => {
      const { result } = renderHook(() => useSeatSSE('concert-123'));

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      act(() => {
        result.current.disconnect();
      });

      // Versuche Error zu triggern - sollte keine neue Verbindung erstellen
      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(MockEventSource.instances.length).toBe(1);
    });
  });

  describe('Enabled Flag', () => {
    test('connects when enabled changes from false to true', async () => {
      const { rerender } = renderHook(
        ({ enabled }) => useSeatSSE('concert-123', { enabled }),
        { initialProps: { enabled: false } }
      );

      expect(MockEventSource.instances.length).toBe(0);

      rerender({ enabled: true });

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });
    });

    test('disconnects when enabled changes from true to false', async () => {
      const { result, rerender } = renderHook(
        ({ enabled }) => useSeatSSE('concert-123', { enabled }),
        { initialProps: { enabled: true } }
      );

      await waitFor(() => {
        expect(MockEventSource.instances.length).toBe(1);
      });

      const eventSource = MockEventSource.getLastInstance();
      const closeSpy = jest.spyOn(eventSource, 'close');

      rerender({ enabled: false });

      expect(closeSpy).toHaveBeenCalled();
      expect(result.current.connectionStatus).toBe(SSE_CONNECTION_STATUS.CLOSED);
    });
  });
});
