import React from 'react';
import {
  calculateAvailabilityPercentage,
  getAvailabilityMessage,
  getProgressBarColor,
} from '../../utils/availabilityHelper';

/**
 * AvailabilityProgress Component
 * Displays availability status with a progress bar
 */
const AvailabilityProgress = ({ availableSeats, totalSeats }) => {
  const percentage = calculateAvailabilityPercentage(availableSeats, totalSeats);
  const soldPercentage = 100 - percentage;
  const message = getAvailabilityMessage(availableSeats, totalSeats);
  const progressColor = getProgressBarColor(percentage);

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-sm">
        <span className="text-text-secondary dark:text-gray-400">
          {message}
        </span>
        <span className="font-semibold text-text-primary dark:text-white">
          {soldPercentage}% verkauft
        </span>
      </div>
      <div className="w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
        <div
          className={`h-full ${progressColor} transition-all duration-300`}
          style={{ width: `${soldPercentage}%` }}
        ></div>
      </div>
    </div>
  );
};

export default AvailabilityProgress;
