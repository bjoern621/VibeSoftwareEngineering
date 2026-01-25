import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import orderService from '../../services/orderService';
import { formatDateTime, formatTime } from '../../utils/dateFormatter';
import { formatPrice } from '../../utils/priceFormatter';

/**
 * TicketDetailModal Component
 * 
 * Modal that displays ticket details with QR code for a specific order.
 * Allows downloading the QR code as PNG.
 * 
 * @param {Object} props
 * @param {Object} props.order - Order object
 * @param {boolean} props.isOpen - Whether modal is open
 * @param {Function} props.onClose - Callback to close modal
 * @returns {React.ReactElement}
 */
const TicketDetailModal = ({ order, isOpen, onClose }) => {
  const [qrCodeUrl, setQrCodeUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [downloading, setDownloading] = useState(false);

  /**
   * Load QR code when modal opens
   */
  useEffect(() => {
    if (isOpen && order) {
      loadQRCode();
    }

    return () => {
      // Clean up QR code URL on unmount
      if (qrCodeUrl) {
        URL.revokeObjectURL(qrCodeUrl);
      }
    };
  }, [isOpen, order]);

  /**
   * Fetch QR code from API
   */
  const loadQRCode = async () => {
    try {
      setLoading(true);
      setError(null);

      const dataUrl = await orderService.getTicketQRCodeDataUrl(order.orderId);
      setQrCodeUrl(dataUrl);
    } catch (err) {
      console.error('Error loading QR code:', err);
      setError('QR-Code konnte nicht geladen werden');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handle QR code download
   */
  const handleDownload = async () => {
    try {
      setDownloading(true);
      await orderService.downloadTicketQR(order.orderId, order.concertName);
    } catch (err) {
      console.error('Error downloading QR code:', err);
      setError('Download fehlgeschlagen');
    } finally {
      setDownloading(false);
    }
  };

  /**
   * Handle backdrop click to close modal
   */
  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      onClick={handleBackdropClick}
    >
      {/* Modal Content */}
      <div
        className="bg-white dark:bg-card-dark rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-border-light dark:border-border-dark">
          <h2 className="text-2xl font-bold text-text-primary dark:text-white">
            Ticket-Details
          </h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            aria-label="Schließen"
          >
            <span className="material-symbols-outlined text-2xl text-text-secondary">
              close
            </span>
          </button>
        </div>

        {/* Body */}
        <div className="p-6 space-y-6">
          {/* Concert Information */}
          <div className="space-y-3">
            <h3 className="text-xl font-bold text-text-primary dark:text-white">
              {order.concertName}
            </h3>
            
            <div className="flex flex-col gap-2 text-sm text-text-secondary dark:text-gray-400">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-lg">calendar_month</span>
                <span>{formatDateTime(order.concertDate)}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-lg">schedule</span>
                <span>{formatTime(order.concertDate)} Uhr</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-lg">location_on</span>
                <span>{order.venue}</span>
              </div>
            </div>
          </div>

          {/* Divider */}
          <hr className="border-border-light dark:border-border-dark" />

          {/* Seat Information */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-xs font-medium text-text-secondary dark:text-gray-500 uppercase tracking-wider mb-1">
                Kategorie
              </p>
              <p className="text-sm font-semibold text-text-primary dark:text-white">
                {order.category || 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-xs font-medium text-text-secondary dark:text-gray-500 uppercase tracking-wider mb-1">
                Sitzplatz
              </p>
              <p className="text-sm font-semibold text-text-primary dark:text-white">
                {order.seatNumber || 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-xs font-medium text-text-secondary dark:text-gray-500 uppercase tracking-wider mb-1">
                Bestell-Nr
              </p>
              <p className="text-sm font-mono font-semibold text-text-primary dark:text-white">
                #{order.orderId}
              </p>
            </div>
            <div>
              <p className="text-xs font-medium text-text-secondary dark:text-gray-500 uppercase tracking-wider mb-1">
                Gesamtpreis
              </p>
              <p className="text-lg font-bold text-primary dark:text-blue-400">
                {formatPrice(order.totalPrice)}
              </p>
            </div>
          </div>

          {/* Divider */}
          <hr className="border-border-light dark:border-border-dark" />

          {/* QR Code Section */}
          <div className="flex flex-col items-center justify-center space-y-4">
            <h4 className="text-lg font-semibold text-text-primary dark:text-white">
              QR-Code
            </h4>

            {/* QR Code Display */}
            <div className="bg-white p-6 rounded-xl border-2 border-border-light dark:border-border-dark shadow-inner">
              {loading && (
                <div className="w-64 h-64 flex items-center justify-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
              )}

              {error && !loading && (
                <div className="w-64 h-64 flex flex-col items-center justify-center gap-2">
                  <span className="material-symbols-outlined text-red-500 text-5xl">
                    error
                  </span>
                  <p className="text-sm text-red-600">{error}</p>
                  <button
                    onClick={loadQRCode}
                    className="mt-2 px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-dark transition-colors"
                  >
                    Erneut versuchen
                  </button>
                </div>
              )}

              {qrCodeUrl && !loading && !error && (
                <img
                  src={qrCodeUrl}
                  alt="Ticket QR Code"
                  className="w-64 h-64 object-contain"
                />
              )}
            </div>

            <p className="text-xs text-center text-text-secondary dark:text-gray-500 max-w-sm">
              Zeigen Sie diesen QR-Code am Eingang vor, um Zugang zur Veranstaltung zu erhalten.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 p-6 border-t border-border-light dark:border-border-dark">
          <button
            onClick={onClose}
            className="px-6 py-2.5 rounded-lg border border-border-light dark:border-border-dark text-text-primary dark:text-gray-200 text-sm font-medium hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          >
            Schließen
          </button>
          <button
            onClick={handleDownload}
            disabled={downloading || loading || error}
            className="flex items-center gap-2 px-6 py-2.5 bg-primary text-white rounded-lg text-sm font-bold shadow-sm hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span className="material-symbols-outlined text-lg">
              {downloading ? 'hourglass_empty' : 'download'}
            </span>
            <span>{downloading ? 'Wird heruntergeladen...' : 'QR-Code herunterladen'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};

TicketDetailModal.propTypes = {
  order: PropTypes.shape({
    orderId: PropTypes.number.isRequired,
    concertName: PropTypes.string.isRequired,
    concertDate: PropTypes.string.isRequired,
    venue: PropTypes.string.isRequired,
    category: PropTypes.string,
    seatNumber: PropTypes.string,
    totalPrice: PropTypes.number.isRequired,
  }),
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default TicketDetailModal;
