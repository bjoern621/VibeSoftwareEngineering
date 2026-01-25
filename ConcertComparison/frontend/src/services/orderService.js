import api from './api';

/**
 * Order Service
 * Handles all order-related API calls
 */
const orderService = {
  /**
   * Fetch all orders for the currently authenticated user
   * @returns {Promise<Array>} Array of OrderHistoryItemDTO
   */
  fetchUserOrders: async () => {
    try {
      const response = await api.get('/users/me/orders');
      return response.data;
    } catch (error) {
      console.error('Error fetching user orders:', error);
      throw error;
    }
  },

  /**
   * Get single order by ID
   * @param {number} orderId - Order ID
   * @returns {Promise<Object>} Order details
   */
  getOrderById: async (orderId) => {
    try {
      const response = await api.get(`/orders/${orderId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching order ${orderId}:`, error);
      throw error;
    }
  },

  /**
   * Download QR code ticket as PNG image
   * @param {number} orderId - Order ID
   * @param {string} concertName - Concert name for filename (optional)
   * @returns {Promise<void>} Triggers download in browser
   */
  downloadTicketQR: async (orderId, concertName = 'ticket') => {
    try {
      const response = await api.get(`/orders/${orderId}/ticket`, {
        responseType: 'blob', // Important for binary data
      });

      // Create blob link to download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      
      // Generate filename: ticket-{concertName}-{orderId}.png
      const filename = `ticket-${concertName.replace(/\s+/g, '-')}-${orderId}.png`;
      link.setAttribute('download', filename);
      
      // Append to body, trigger click, and remove
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      
      // Clean up URL object
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error(`Error downloading ticket QR for order ${orderId}:`, error);
      throw error;
    }
  },

  /**
   * Get QR code as base64 data URL for displaying in modal
   * @param {number} orderId - Order ID
   * @returns {Promise<string>} Base64 data URL
   */
  getTicketQRCodeDataUrl: async (orderId) => {
    try {
      const response = await api.get(`/orders/${orderId}/ticket`, {
        responseType: 'blob',
      });

      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(response.data);
      });
    } catch (error) {
      console.error(`Error fetching QR code for order ${orderId}:`, error);
      throw error;
    }
  },
};

export default orderService;
