import React from 'react';
import PropTypes from 'prop-types';

/**
 * OrderStatusBadge Component
 * 
 * Displays order status with appropriate icon and styling.
 * Design from Google Stitch - Order History.
 * 
 * @param {Object} props
 * @param {string} props.status - Order status ('CONFIRMED', 'PENDING', 'CANCELLED')
 * @param {Date} props.concertDate - Concert date to determine if past or upcoming
 * @returns {React.ReactElement}
 */
const OrderStatusBadge = ({ status, concertDate }) => {
  const now = new Date();
  const isUpcoming = new Date(concertDate) >= now;

  // Determine badge style based on status and date
  const getBadgeConfig = () => {
    // Past concert
    if (!isUpcoming) {
      return {
        icon: 'event_available',
        label: 'Vergangen',
        className: 'bg-gray-100 text-gray-600 border-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700',
      };
    }

    // Upcoming concert - check payment status
    switch (status) {
      case 'CONFIRMED':
        return {
          icon: 'check_circle',
          label: 'Bestätigt',
          className: 'bg-green-50 text-green-700 border-green-200 dark:bg-green-900/20 dark:text-green-400 dark:border-green-900/50',
        };
      
      case 'PENDING':
        return {
          icon: 'schedule',
          label: 'Ausstehend',
          className: 'bg-yellow-50 text-yellow-700 border-yellow-200 dark:bg-yellow-900/20 dark:text-yellow-400 dark:border-yellow-900/50',
        };
      
      case 'CANCELLED':
        return {
          icon: 'cancel',
          label: 'Storniert',
          className: 'bg-red-50 text-red-700 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-900/50',
        };
      
      default:
        return {
          icon: 'info',
          label: status,
          className: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-900/50',
        };
    }
  };

  const config = getBadgeConfig();

  return (
    <div className={`shrink-0 flex items-center gap-1.5 px-3 py-1 rounded-full border ${config.className}`}>
      <span className="material-symbols-outlined text-base font-bold">
        {config.icon}
      </span>
      <span className="text-xs font-bold uppercase tracking-wide">
        {config.label}
      </span>
    </div>
  );
};

OrderStatusBadge.propTypes = {
  status: PropTypes.oneOf(['CONFIRMED', 'PENDING', 'CANCELLED']).isRequired,
  concertDate: PropTypes.oneOfType([PropTypes.string, PropTypes.instanceOf(Date)]).isRequired,
};

export default OrderStatusBadge;
