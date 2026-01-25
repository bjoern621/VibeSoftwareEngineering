import React, { useState } from 'react';
import PropTypes from 'prop-types';
import OrderStatusBadge from './OrderStatusBadge';
import { formatDateTime, formatTime } from '../../utils/dateFormatter';
import { formatPrice } from '../../utils/priceFormatter';

/**
 * OrderCard Component
 * 
 * Displays a single order with concert details, status, and actions.
 * Design based on Google Stitch - Order History (Ordner 11).
 * 
 * @param {Object} props
 * @param {Object} props.order - Order object from API
 * @param {Function} props.onViewDetails - Callback when Details button is clicked
 * @param {Function} props.onDownloadQR - Callback when QR-Code button is clicked
 * @returns {React.ReactElement}
 */
const OrderCard = ({ order, onViewDetails, onDownloadQR }) => {
  const [downloading, setDownloading] = useState(false);
  const [imageError, setImageError] = useState(false);

  // Check if concert is in the past
  const isPastConcert = new Date(order.concertDate) < new Date();

  // Placeholder image if concert image not available or fails to load
  const imageSrc = imageError ? '/placeholder_concert.svg' : '/placeholder_concert.svg';

  /**
   * Handle QR code download
   */
  const handleDownloadQR = async () => {
    try {
      setDownloading(true);
      await onDownloadQR(order.orderId, order.concertName);
    } catch (error) {
      console.error('Error downloading QR code:', error);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div 
      className={`
        group bg-card-light dark:bg-card-dark border border-border-light dark:border-border-dark 
        rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow duration-300 
        flex flex-col md:flex-row gap-6
        ${isPastConcert ? 'opacity-80 hover:opacity-100' : ''}
      `}
    >
      {/* Concert Poster */}
      <div className="shrink-0 relative">
        <div 
          className={`
            w-full md:w-32 aspect-[2/3] md:aspect-[3/4] rounded-lg bg-cover bg-center shadow-inner
            ${isPastConcert ? 'grayscale group-hover:grayscale-0 transition-all' : ''}
          `}
          style={{ 
            backgroundImage: `url("${imageSrc}")`,
            backgroundColor: '#e2e8f0' // Fallback color
          }}
          onError={() => setImageError(true)}
        >
          {/* Mobile Date Badge (visible only on mobile) */}
          <div className="absolute top-2 left-2 bg-white/90 dark:bg-black/80 backdrop-blur text-xs font-bold px-2 py-1 rounded md:hidden">
            {new Date(order.concertDate).toLocaleDateString('de-DE', { day: '2-digit', month: 'short' })}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 flex flex-col justify-between py-1">
        <div className="flex flex-col gap-1">
          {/* Title and Status */}
          <div className="flex justify-between items-start gap-4">
            <div className="flex flex-col gap-1">
              <h3 className={`
                text-xl font-bold group-hover:text-primary transition-colors
                ${isPastConcert 
                  ? 'text-text-secondary dark:text-gray-300' 
                  : 'text-text-primary dark:text-white'}
              `}>
                {order.concertName}
              </h3>
              
              {/* Date, Time, and Venue */}
              <div className={`
                flex flex-wrap items-center gap-x-4 gap-y-1 text-sm mt-1
                ${isPastConcert 
                  ? 'text-text-secondary dark:text-gray-500' 
                  : 'text-text-secondary dark:text-gray-400'}
              `}>
                <div className="flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-lg">calendar_month</span>
                  <span>{formatDateTime(order.concertDate)} • {formatTime(order.concertDate)} Uhr</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-lg">location_on</span>
                  <span>{order.venue}</span>
                </div>
              </div>
            </div>
            
            {/* Status Badge */}
            <OrderStatusBadge 
              status={order.status} 
              concertDate={order.concertDate} 
            />
          </div>

          {/* Order Details Section */}
          <div className="mt-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-t border-dashed border-border-light dark:border-border-dark pt-4">
            {/* Order Number */}
            <div className="flex flex-col gap-0.5">
              <span className={`
                text-xs font-medium uppercase tracking-wider
                ${isPastConcert 
                  ? 'text-text-secondary dark:text-gray-500' 
                  : 'text-text-secondary dark:text-gray-500'}
              `}>
                Bestell-Nr
              </span>
              <span className={`
                font-mono text-sm font-semibold
                ${isPastConcert 
                  ? 'text-text-secondary dark:text-gray-500' 
                  : 'text-text-primary dark:text-gray-300'}
              `}>
                #{order.orderId}
              </span>
            </div>

            {/* Price and Actions */}
            <div className="flex items-center gap-6">
              {/* Total Price */}
              <div className="flex flex-col gap-0.5 text-right sm:text-left">
                <span className={`
                  text-xs font-medium uppercase tracking-wider
                  ${isPastConcert 
                    ? 'text-text-secondary dark:text-gray-500' 
                    : 'text-text-secondary dark:text-gray-500'}
                `}>
                  Gesamtpreis
                </span>
                <span className={`
                  font-bold text-lg
                  ${isPastConcert 
                    ? 'text-text-secondary dark:text-gray-400' 
                    : 'text-text-primary dark:text-white'}
                `}>
                  {formatPrice(order.totalPrice)}
                </span>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-3">
                {/* QR Code Button (only for upcoming concerts) */}
                {!isPastConcert && (
                  <button
                    onClick={handleDownloadQR}
                    disabled={downloading}
                    className="flex items-center gap-2 px-4 py-2 rounded-lg border border-border-light dark:border-border-dark bg-background-light dark:bg-background-dark text-text-primary dark:text-gray-200 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    title="QR-Code herunterladen"
                  >
                    <span className="material-symbols-outlined text-lg">
                      {downloading ? 'hourglass_empty' : 'qr_code_2'}
                    </span>
                    <span className="hidden sm:inline">QR-Code</span>
                  </button>
                )}

                {/* Details Button */}
                <button
                  onClick={() => onViewDetails(order)}
                  className={`
                    flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-bold shadow-sm transition-colors
                    ${isPastConcert
                      ? 'border border-transparent bg-transparent text-primary hover:bg-primary/10'
                      : 'bg-primary text-white hover:bg-primary-dark'}
                  `}
                >
                  <span>{isPastConcert ? 'Rechnung ansehen' : 'Details'}</span>
                  {!isPastConcert && (
                    <span className="material-symbols-outlined text-lg">arrow_forward</span>
                  )}
                </button>
              </div>
            </div>
          </div>

          {/* Seat Information (optional, can be shown in modal) */}
          {order.seatNumber && (
            <div className="mt-2 text-xs text-text-secondary dark:text-gray-500">
              Sitzplatz: {order.category} • {order.seatNumber}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

OrderCard.propTypes = {
  order: PropTypes.shape({
    orderId: PropTypes.number.isRequired,
    concertName: PropTypes.string.isRequired,
    concertDate: PropTypes.string.isRequired,
    venue: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired,
    totalPrice: PropTypes.number.isRequired,
    seatNumber: PropTypes.string,
    category: PropTypes.string,
  }).isRequired,
  onViewDetails: PropTypes.func.isRequired,
  onDownloadQR: PropTypes.func.isRequired,
};

export default OrderCard;
