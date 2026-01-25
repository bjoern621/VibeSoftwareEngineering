import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { formatDateTime } from '../../utils/dateFormatter';
import { formatPrice } from '../../utils/priceFormatter';

/**
 * CartItem Component
 * Displays a single item in the shopping cart
 * 
 * @param {Object} item - Cart item data
 * @param {Function} onRemove - Callback when item is removed
 */
const CartItem = ({ item, onRemove }) => {
  const [imageError, setImageError] = useState(false);
  const { concert, seat } = item;

  const imageSrc = imageError || !concert?.imageUrl
    ? '/placeholder_concert.svg'
    : concert.imageUrl;

  return (
    <div className="group bg-white dark:bg-[#1a2634] rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden hover:shadow-md transition-shadow">
      <div className="p-5 flex gap-5">
        {/* Concert Poster */}
        <div className="w-24 md:w-32 shrink-0 aspect-[2/3] rounded-lg bg-gray-200 overflow-hidden relative shadow-inner">
          <img
            src={imageSrc}
            alt={concert?.name || 'Concert'}
            onError={() => setImageError(true)}
            className="absolute inset-0 w-full h-full object-cover transition-transform group-hover:scale-105 duration-500"
          />
        </div>

        {/* Details */}
        <div className="flex-1 flex flex-col justify-between">
          <div>
            {/* Concert Name & Remove Button */}
            <div className="flex justify-between items-start">
              <h3 className="text-lg md:text-xl font-bold text-slate-900 dark:text-white leading-tight">
                {concert?.name || 'Unbekanntes Konzert'}
              </h3>
              <button
                onClick={() => onRemove(item.holdId)}
                className="text-gray-400 hover:text-red-500 transition-colors p-1"
                aria-label="Aus Warenkorb entfernen"
              >
                <span className="material-symbols-outlined text-xl">delete</span>
              </button>
            </div>

            {/* Date & Time */}
            <div className="mt-2 flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
              <span className="material-symbols-outlined text-lg">calendar_month</span>
              <span>{formatDateTime(concert?.date)}</span>
            </div>

            {/* Venue */}
            <div className="mt-1 flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
              <span className="material-symbols-outlined text-lg">location_on</span>
              <span>{concert?.venue || 'Unbekannter Ort'}</span>
            </div>

            {/* Seat Badges */}
            <div className="mt-4 flex flex-wrap gap-2">
              {seat?.category && (
                <span className="inline-flex items-center px-2.5 py-1 rounded bg-blue-50 dark:bg-blue-900/30 text-primary dark:text-blue-300 text-xs font-semibold border border-blue-100 dark:border-blue-800">
                  {seat.category}
                </span>
              )}
              {seat?.row && (
                <span className="inline-flex items-center px-2.5 py-1 rounded bg-blue-50 dark:bg-blue-900/30 text-primary dark:text-blue-300 text-xs font-semibold border border-blue-100 dark:border-blue-800">
                  Reihe {seat.row}
                </span>
              )}
              {seat?.number && (
                <span className="inline-flex items-center px-2.5 py-1 rounded bg-blue-50 dark:bg-blue-900/30 text-primary dark:text-blue-300 text-xs font-semibold border border-blue-100 dark:border-blue-800">
                  Sitz {seat.number}
                </span>
              )}
            </div>
          </div>

          {/* Footer: Price */}
          <div className="mt-5 flex items-end justify-end border-t border-gray-100 dark:border-gray-700 pt-4">
            <div className="text-right">
              <span className="block text-xs text-slate-400">Preis</span>
              <span className="text-xl font-bold text-slate-900 dark:text-white">
                {formatPrice(seat?.price || 0)}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

CartItem.propTypes = {
  item: PropTypes.shape({
    holdId: PropTypes.string.isRequired,
    concert: PropTypes.shape({
      id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
      name: PropTypes.string,
      date: PropTypes.string,
      venue: PropTypes.string,
      imageUrl: PropTypes.string,
    }).isRequired,
    seat: PropTypes.shape({
      id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
      category: PropTypes.string,
      row: PropTypes.string,
      number: PropTypes.string,
      price: PropTypes.number,
    }).isRequired,
    expiresAt: PropTypes.string.isRequired,
  }).isRequired,
  onRemove: PropTypes.func.isRequired,
};

export default CartItem;
