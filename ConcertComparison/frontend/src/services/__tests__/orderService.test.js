import orderService from '../orderService';
import api from '../api';

// Mock the api module
jest.mock('../api', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('orderService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('fetchUserOrders', () => {
    it('should fetch user orders successfully', async () => {
      const mockOrders = [
        { orderId: 1, concertName: 'Test Concert', totalPrice: 100 },
      ];
      api.get.mockResolvedValue({ data: mockOrders });

      const result = await orderService.fetchUserOrders();

      expect(api.get).toHaveBeenCalledWith('/users/me/orders');
      expect(result).toEqual(mockOrders);
    });

    it('should handle errors when fetching orders', async () => {
      const error = new Error('Network error');
      api.get.mockRejectedValue(error);

      await expect(orderService.fetchUserOrders()).rejects.toThrow('Network error');
    });
  });

  describe('getOrderById', () => {
    it('should fetch order by ID successfully', async () => {
      const mockOrder = { orderId: 1, concertName: 'Test Concert' };
      api.get.mockResolvedValue({ data: mockOrder });

      const result = await orderService.getOrderById(1);

      expect(api.get).toHaveBeenCalledWith('/orders/1');
      expect(result).toEqual(mockOrder);
    });
  });

  describe('getTicketQRCodeDataUrl', () => {
    it('should fetch QR code data URL successfully', async () => {
      const mockBlob = new Blob(['fake-image'], { type: 'image/png' });
      api.get.mockResolvedValue({ data: mockBlob });
      
      // Mock FileReader
      const mockFileReader = {
        readAsDataURL: jest.fn(function() {
          this.onloadend();
        }),
        result: 'data:image/png;base64,mockBase64String',
      };
      global.FileReader = jest.fn(() => mockFileReader);

      const result = await orderService.getTicketQRCodeDataUrl(1);

      expect(api.get).toHaveBeenCalledWith('/orders/1/ticket', {
        responseType: 'blob',
      });
      expect(result).toBe('data:image/png;base64,mockBase64String');
    });
  });

  describe('downloadTicketQR', () => {
    it('should download ticket QR code successfully', async () => {
      const mockBlob = new Blob(['fake-image'], { type: 'image/png' });
      api.get.mockResolvedValue({ data: mockBlob });
      
      global.URL.createObjectURL = jest.fn(() => 'blob:mock-url');
      global.URL.revokeObjectURL = jest.fn();
      
      const mockLink = {
        href: '',
        setAttribute: jest.fn(),
        click: jest.fn(),
        parentNode: {
          removeChild: jest.fn(),
        },
      };
      document.createElement = jest.fn(() => mockLink);
      document.body.appendChild = jest.fn();

      await orderService.downloadTicketQR(1, 'Test Concert');

      expect(api.get).toHaveBeenCalledWith('/orders/1/ticket', {
        responseType: 'blob',
      });
      expect(mockLink.setAttribute).toHaveBeenCalledWith('download', 'ticket-Test-Concert-1.png');
      expect(mockLink.click).toHaveBeenCalled();
      expect(global.URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock-url');
    });
  });
});
