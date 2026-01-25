import React from 'react';
import { getAvailabilityBadge } from '../../utils/availabilityHelper';

/**
 * AvailabilityBadge Component
 * Displays a colored badge indicating concert availability status
 */
const AvailabilityBadge = ({ availableSeats, totalSeats, className = '' }) => {
  const badge = getAvailabilityBadge(availableSeats, totalSeats);

  return (
    <div
      className={`absolute top-4 left-4 flex items-center gap-1.5 px-3 py-1.5 rounded-full ${badge.color} ${badge.textColor} text-xs font-semibold shadow-lg ${className}`}
    >
      <span className="material-symbols-outlined text-base">{badge.icon}</span>
      <span>{badge.text}</span>
    </div>
  );
};

export default AvailabilityBadge;
