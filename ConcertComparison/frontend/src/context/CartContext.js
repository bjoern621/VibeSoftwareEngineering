import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';

const CartContext = createContext(null);

/**
 * CartProvider - Provides shopping cart state to the entire app
 * Manages hold items with automatic expiry handling
 */
export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState([]);

  /**
   * Load cart from localStorage on mount
   */
  useEffect(() => {
    const loadCart = () => {
      try {
        const savedCart = localStorage.getItem('cart');
        if (savedCart) {
          const items = JSON.parse(savedCart);
          // Filter out expired items
          const validItems = items.filter(item => {
            const expiresAt = new Date(item.expiresAt);
            return expiresAt > new Date();
          });
          setCartItems(validItems);
          
          // Save back if items were filtered
          if (validItems.length !== items.length) {
            localStorage.setItem('cart', JSON.stringify(validItems));
          }
        }
      } catch (error) {
        console.error('Failed to load cart from localStorage:', error);
        localStorage.removeItem('cart');
      }
    };

    loadCart();
  }, []);

  /**
   * Save cart to localStorage whenever it changes
   */
  useEffect(() => {
    try {
      localStorage.setItem('cart', JSON.stringify(cartItems));
    } catch (error) {
      console.error('Failed to save cart to localStorage:', error);
    }
  }, [cartItems]);

  /**
   * Check for expired holds periodically
   */
  useEffect(() => {
    const checkExpiry = () => {
      setCartItems(prevItems => {
        const now = new Date();
        const validItems = prevItems.filter(item => {
          const expiresAt = new Date(item.expiresAt);
          const isValid = expiresAt > now;
          
          // Log expired items
          if (!isValid) {
            console.log(`Hold expired for seat ${item.seat?.id} in concert ${item.concert?.name}`);
          }
          
          return isValid;
        });

        // Return previous if no changes to avoid unnecessary re-renders
        return validItems.length === prevItems.length ? prevItems : validItems;
      });
    };

    // Check every 5 seconds
    const interval = setInterval(checkExpiry, 5000);

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, []);

  /**
   * Add item to cart
   * @param {Object} item - Hold item to add
   * @param {string} item.holdId - Hold ID from backend
   * @param {Object} item.seat - Seat data (id, category, row, number, price)
   * @param {Object} item.concert - Concert data (id, name, date, venue, imageUrl)
   * @param {number} item.ttlSeconds - Time-to-live in seconds
   */
  const addItem = useCallback((item) => {
    if (!item.holdId || !item.seat || !item.concert) {
      console.error('Invalid cart item:', item);
      return;
    }

    // Calculate expiration time
    const expiresAt = new Date();
    expiresAt.setSeconds(expiresAt.getSeconds() + (item.ttlSeconds || 600));

    const cartItem = {
      holdId: item.holdId,
      seat: item.seat,
      concert: item.concert,
      ttlSeconds: item.ttlSeconds || 600,
      expiresAt: expiresAt.toISOString(),
      addedAt: new Date().toISOString(),
    };

    setCartItems(prevItems => {
      // Check if item already exists (same holdId)
      const exists = prevItems.some(i => i.holdId === cartItem.holdId);
      if (exists) {
        console.warn('Item already in cart:', cartItem.holdId);
        return prevItems;
      }

      // Add new item
      return [...prevItems, cartItem];
    });
  }, []);

  /**
   * Remove item from cart by holdId
   * @param {string} holdId - Hold ID to remove
   */
  const removeItem = useCallback((holdId) => {
    setCartItems(prevItems => prevItems.filter(item => item.holdId !== holdId));
  }, []);

  /**
   * Clear all items from cart
   */
  const clearCart = useCallback(() => {
    setCartItems([]);
  }, []);

  /**
   * Get total number of items in cart
   */
  const itemCount = cartItems.length;

  /**
   * Calculate subtotal (sum of all seat prices)
   */
  const subtotal = cartItems.reduce((sum, item) => sum + (item.seat?.price || 0), 0);

  /**
   * Calculate service fees (5 EUR flat fee)
   */
  const serviceFees = cartItems.length > 0 ? 5.00 : 0;

  /**
   * Calculate total (subtotal + service fees)
   * Note: VAT is already included in seat prices
   */
  const total = subtotal + serviceFees;

  /**
   * Get oldest item (for countdown timer)
   */
  const oldestItem = cartItems.length > 0
    ? cartItems.reduce((oldest, item) => {
        const itemExpiry = new Date(item.expiresAt);
        const oldestExpiry = new Date(oldest.expiresAt);
        return itemExpiry < oldestExpiry ? item : oldest;
      }, cartItems[0])
    : null;

  /**
   * Check if cart is empty
   */
  const isEmpty = cartItems.length === 0;

  /**
   * Get all hold IDs (for bulk purchase)
   */
  const getAllHoldIds = useCallback(() => {
    return cartItems.map(item => item.holdId);
  }, [cartItems]);

  const value = {
    cartItems,
    itemCount,
    subtotal,
    serviceFees,
    total,
    oldestItem,
    isEmpty,
    addItem,
    removeItem,
    clearCart,
    getAllHoldIds,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

/**
 * Custom hook to use cart context
 */
export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export default CartContext;
