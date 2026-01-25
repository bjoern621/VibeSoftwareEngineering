import React from 'react';
import { renderHook, act } from '@testing-library/react';
import { CartProvider, useCart } from '../CartContext';

// Wrapper component for hooks
const wrapper = ({ children }) => <CartProvider>{children}</CartProvider>;

// Mock localStorage
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => {
      store[key] = value.toString();
    },
    removeItem: (key) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('CartContext', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllTimers();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  describe('addItem', () => {
    it('should add item to cart', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const item = {
        holdId: 'hold-123',
        seat: { id: 1, category: 'VIP', row: 'A', number: '12', price: 99.99 },
        concert: { id: 1, name: 'Test Concert', date: '2024-12-31', venue: 'Test Venue' },
        ttlSeconds: 600,
      };

      act(() => {
        result.current.addItem(item);
      });

      expect(result.current.cartItems).toHaveLength(1);
      expect(result.current.cartItems[0].holdId).toBe('hold-123');
      expect(result.current.itemCount).toBe(1);
    });

    it('should not add duplicate items', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const item = {
        holdId: 'hold-123',
        seat: { id: 1, price: 99.99 },
        concert: { id: 1, name: 'Test Concert' },
        ttlSeconds: 600,
      };

      act(() => {
        result.current.addItem(item);
        result.current.addItem(item); // Try to add again
      });

      expect(result.current.cartItems).toHaveLength(1);
    });

    it('should not add item with missing required fields', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const invalidItem = {
        holdId: 'hold-123',
        // Missing seat and concert
      };

      act(() => {
        result.current.addItem(invalidItem);
      });

      expect(result.current.cartItems).toHaveLength(0);
    });

    it('should calculate expiration time correctly', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const now = new Date('2024-01-01T12:00:00Z');
      jest.setSystemTime(now);

      const item = {
        holdId: 'hold-123',
        seat: { id: 1, price: 99.99 },
        concert: { id: 1, name: 'Test Concert' },
        ttlSeconds: 300, // 5 minutes
      };

      act(() => {
        result.current.addItem(item);
      });

      const addedItem = result.current.cartItems[0];
      const expiresAt = new Date(addedItem.expiresAt);
      const expectedExpiry = new Date(now.getTime() + 300 * 1000);

      expect(expiresAt.getTime()).toBe(expectedExpiry.getTime());
    });
  });

  describe('removeItem', () => {
    it('should remove item from cart', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const item = {
        holdId: 'hold-123',
        seat: { id: 1, price: 99.99 },
        concert: { id: 1, name: 'Test Concert' },
        ttlSeconds: 600,
      };

      act(() => {
        result.current.addItem(item);
      });

      expect(result.current.cartItems).toHaveLength(1);

      act(() => {
        result.current.removeItem('hold-123');
      });

      expect(result.current.cartItems).toHaveLength(0);
      expect(result.current.isEmpty).toBe(true);
    });

    it('should not fail when removing non-existent item', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.removeItem('non-existent');
      });

      expect(result.current.cartItems).toHaveLength(0);
    });
  });

  describe('clearCart', () => {
    it('should clear all items from cart', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const items = [
        {
          holdId: 'hold-1',
          seat: { id: 1, price: 99.99 },
          concert: { id: 1, name: 'Concert 1' },
          ttlSeconds: 600,
        },
        {
          holdId: 'hold-2',
          seat: { id: 2, price: 79.99 },
          concert: { id: 2, name: 'Concert 2' },
          ttlSeconds: 600,
        },
      ];

      act(() => {
        items.forEach(item => result.current.addItem(item));
      });

      expect(result.current.cartItems).toHaveLength(2);

      act(() => {
        result.current.clearCart();
      });

      expect(result.current.cartItems).toHaveLength(0);
      expect(result.current.isEmpty).toBe(true);
    });
  });

  describe('price calculations', () => {
    it('should calculate subtotal correctly', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const items = [
        {
          holdId: 'hold-1',
          seat: { id: 1, price: 99.99 },
          concert: { id: 1, name: 'Concert 1' },
          ttlSeconds: 600,
        },
        {
          holdId: 'hold-2',
          seat: { id: 2, price: 79.99 },
          concert: { id: 2, name: 'Concert 2' },
          ttlSeconds: 600,
        },
      ];

      act(() => {
        items.forEach(item => result.current.addItem(item));
      });

      expect(result.current.subtotal).toBe(179.98);
    });

    it('should calculate service fees correctly', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      expect(result.current.serviceFees).toBe(0); // Empty cart

      act(() => {
        result.current.addItem({
          holdId: 'hold-1',
          seat: { id: 1, price: 99.99 },
          concert: { id: 1, name: 'Concert 1' },
          ttlSeconds: 600,
        });
      });

      expect(result.current.serviceFees).toBe(5.00); // 5 EUR flat fee
    });

    it('should calculate total correctly', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          holdId: 'hold-1',
          seat: { id: 1, price: 100.00 },
          concert: { id: 1, name: 'Concert 1' },
          ttlSeconds: 600,
        });
      });

      expect(result.current.total).toBe(105.00); // 100 + 5 service fee
    });
  });

  describe('hold expiry logic', () => {
    it('should remove expired holds automatically', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const now = new Date('2024-01-01T12:00:00Z');
      jest.setSystemTime(now);

      const item = {
        holdId: 'hold-123',
        seat: { id: 1, price: 99.99 },
        concert: { id: 1, name: 'Test Concert' },
        ttlSeconds: 10, // 10 seconds TTL
      };

      act(() => {
        result.current.addItem(item);
      });

      expect(result.current.cartItems).toHaveLength(1);

      // Fast-forward 15 seconds (past TTL)
      jest.setSystemTime(new Date('2024-01-01T12:00:15Z'));
      
      // Trigger expiry check (runs every 5 seconds)
      act(() => {
        jest.advanceTimersByTime(5000);
      });

      expect(result.current.cartItems).toHaveLength(0);
    });
  });

  describe('oldestItem', () => {
    it('should return oldest item in cart', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const now = new Date('2024-01-01T12:00:00Z');
      jest.setSystemTime(now);

      act(() => {
        result.current.addItem({
          holdId: 'hold-1',
          seat: { id: 1, price: 99.99 },
          concert: { id: 1, name: 'Concert 1' },
          ttlSeconds: 600,
        });
      });

      // Add second item 1 minute later
      jest.setSystemTime(new Date('2024-01-01T12:01:00Z'));

      act(() => {
        result.current.addItem({
          holdId: 'hold-2',
          seat: { id: 2, price: 79.99 },
          concert: { id: 2, name: 'Concert 2' },
          ttlSeconds: 600,
        });
      });

      expect(result.current.oldestItem.holdId).toBe('hold-1');
    });
  });

  describe('getAllHoldIds', () => {
    it('should return all hold IDs', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      const items = [
        {
          holdId: 'hold-1',
          seat: { id: 1, price: 99.99 },
          concert: { id: 1, name: 'Concert 1' },
          ttlSeconds: 600,
        },
        {
          holdId: 'hold-2',
          seat: { id: 2, price: 79.99 },
          concert: { id: 2, name: 'Concert 2' },
          ttlSeconds: 600,
        },
      ];

      act(() => {
        items.forEach(item => result.current.addItem(item));
      });

      const holdIds = result.current.getAllHoldIds();
      expect(holdIds).toEqual(['hold-1', 'hold-2']);
    });
  });

  describe('isEmpty', () => {
    it('should return true when cart is empty', () => {
      const { result } = renderHook(() => useCart(), { wrapper });
      expect(result.current.isEmpty).toBe(true);
    });

    it('should return false when cart has items', () => {
      const { result } = renderHook(() => useCart(), { wrapper });

      act(() => {
        result.current.addItem({
          holdId: 'hold-1',
          seat: { id: 1, price: 99.99 },
          concert: { id: 1, name: 'Concert 1' },
          ttlSeconds: 600,
        });
      });

      expect(result.current.isEmpty).toBe(false);
    });
  });
});
