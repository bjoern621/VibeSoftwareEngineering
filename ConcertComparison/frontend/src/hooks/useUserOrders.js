import { useState, useEffect, useCallback } from 'react';
import orderService from '../services/orderService';

/**
 * Custom hook for fetching and managing user orders
 * @returns {Object} - Orders data, loading state, error, and refresh function
 */
export const useUserOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /**
   * Load user orders from API
   */
  const loadOrders = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await orderService.fetchUserOrders();
      setOrders(data || []);
    } catch (err) {
      console.error('Error loading user orders:', err);
      
      // Handle specific error cases
      if (err.response?.status === 401) {
        setError('Nicht autorisiert. Bitte melden Sie sich an.');
      } else if (err.response?.status === 404) {
        setError('Keine Bestellungen gefunden.');
        setOrders([]);
      } else {
        setError(err.response?.data?.message || 'Fehler beim Laden der Bestellungen');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Load orders on mount
   */
  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  /**
   * Filter orders by status (upcoming vs past)
   * @param {string} filter - 'all', 'upcoming', or 'past'
   * @returns {Array} Filtered orders
   */
  const getFilteredOrders = useCallback((filter = 'all') => {
    if (filter === 'all') {
      return orders;
    }

    const now = new Date();
    
    return orders.filter((order) => {
      const concertDate = new Date(order.concertDate);
      
      if (filter === 'upcoming') {
        return concertDate >= now;
      } else if (filter === 'past') {
        return concertDate < now;
      }
      
      return true;
    });
  }, [orders]);

  /**
   * Get counts for each filter
   * @returns {Object} Counts for all, upcoming, and past orders
   */
  const getOrderCounts = useCallback(() => {
    const now = new Date();
    const upcoming = orders.filter(o => new Date(o.concertDate) >= now).length;
    const past = orders.filter(o => new Date(o.concertDate) < now).length;
    
    return {
      all: orders.length,
      upcoming,
      past,
    };
  }, [orders]);

  /**
   * Download ticket QR code
   * @param {number} orderId - Order ID
   * @param {string} concertName - Concert name (for filename)
   * @returns {Promise<void>}
   */
  const downloadTicket = async (orderId, concertName) => {
    try {
      await orderService.downloadTicketQR(orderId, concertName);
    } catch (err) {
      console.error(`Error downloading ticket for order ${orderId}:`, err);
      throw err;
    }
  };

  /**
   * Refresh orders list
   */
  const refresh = () => {
    loadOrders();
  };

  return {
    orders,
    loading,
    error,
    refresh,
    getFilteredOrders,
    getOrderCounts,
    downloadTicket,
  };
};

export default useUserOrders;
