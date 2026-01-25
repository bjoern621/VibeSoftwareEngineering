import { renderHook, act, waitFor } from '@testing-library/react';
import useUserOrders from '../useUserOrders';
import orderService from '../../services/orderService';

jest.mock('../../services/orderService');

describe('useUserOrders', () => {
  const mockOrders = [
    {
      orderId: 1,
      concertName: 'Future Concert',
      concertDate: '2026-12-31T20:00:00',
      status: 'CONFIRMED',
    },
    {
      orderId: 2,
      concertName: 'Past Concert',
      concertDate: '2024-01-01T20:00:00',
      status: 'CONFIRMED',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should load orders on mount', async () => {
    orderService.fetchUserOrders.mockResolvedValue(mockOrders);

    const { result } = renderHook(() => useUserOrders());

    expect(result.current.loading).toBe(true);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.orders).toEqual(mockOrders);
    expect(result.current.error).toBeNull();
  });

  it('should handle loading error', async () => {
    const errorMessage = 'Fehler beim Laden der Bestellungen';
    orderService.fetchUserOrders.mockRejectedValue({
      response: { data: { message: errorMessage } },
    });

    const { result } = renderHook(() => useUserOrders());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe(errorMessage);
    expect(result.current.orders).toEqual([]);
  });

  it('should filter upcoming orders', async () => {
    orderService.fetchUserOrders.mockResolvedValue(mockOrders);

    const { result } = renderHook(() => useUserOrders());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    const upcoming = result.current.getFilteredOrders('upcoming');
    expect(upcoming).toHaveLength(1);
    expect(upcoming[0].concertName).toBe('Future Concert');
  });

  it('should filter past orders', async () => {
    orderService.fetchUserOrders.mockResolvedValue(mockOrders);

    const { result } = renderHook(() => useUserOrders());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    const past = result.current.getFilteredOrders('past');
    expect(past).toHaveLength(1);
    expect(past[0].concertName).toBe('Past Concert');
  });

  it('should return all orders with "all" filter', async () => {
    orderService.fetchUserOrders.mockResolvedValue(mockOrders);

    const { result } = renderHook(() => useUserOrders());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    const all = result.current.getFilteredOrders('all');
    expect(all).toHaveLength(2);
  });

  it('should calculate order counts correctly', async () => {
    orderService.fetchUserOrders.mockResolvedValue(mockOrders);

    const { result } = renderHook(() => useUserOrders());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    const counts = result.current.getOrderCounts();
    expect(counts.all).toBe(2);
    expect(counts.upcoming).toBe(1);
    expect(counts.past).toBe(1);
  });

  it('should download ticket', async () => {
    orderService.fetchUserOrders.mockResolvedValue(mockOrders);
    orderService.downloadTicketQR.mockResolvedValue();

    const { result } = renderHook(() => useUserOrders());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    await act(async () => {
      await result.current.downloadTicket(1, 'Test Concert');
    });

    expect(orderService.downloadTicketQR).toHaveBeenCalledWith(1, 'Test Concert');
  });

  it('should refresh orders', async () => {
    orderService.fetchUserOrders.mockResolvedValue(mockOrders);

    const { result } = renderHook(() => useUserOrders());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    orderService.fetchUserOrders.mockClear();
    orderService.fetchUserOrders.mockResolvedValue([mockOrders[0]]);

    await act(async () => {
      result.current.refresh();
    });

    await waitFor(() => {
      expect(orderService.fetchUserOrders).toHaveBeenCalled();
    });
  });
});
