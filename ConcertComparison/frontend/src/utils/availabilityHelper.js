/**
 * Calculate availability percentage
 * @param {number} availableSeats - Number of available seats
 * @param {number} totalSeats - Total number of seats
 * @returns {number} - Percentage of available seats (0-100)
 */
export const calculateAvailabilityPercentage = (availableSeats, totalSeats) => {
  if (totalSeats === 0) return 0;
  return Math.round((availableSeats / totalSeats) * 100);
};

/**
 * Get availability status based on available seats percentage
 * @param {number} availableSeats - Number of available seats
 * @param {number} totalSeats - Total number of seats
 * @returns {string} - Status: 'SOLD_OUT' | 'LIMITED' | 'AVAILABLE'
 */
export const getAvailabilityStatus = (availableSeats, totalSeats) => {
  if (availableSeats === 0) return 'SOLD_OUT';
  const percentage = calculateAvailabilityPercentage(availableSeats, totalSeats);
  if (percentage < 20) return 'LIMITED';
  return 'AVAILABLE';
};

/**
 * Get badge configuration for availability status
 * @param {number} availableSeats - Number of available seats
 * @param {number} totalSeats - Total number of seats
 * @returns {Object} - Badge config with text, color, icon
 */
export const getAvailabilityBadge = (availableSeats, totalSeats) => {
  const percentage = calculateAvailabilityPercentage(availableSeats, totalSeats);
  
  if (availableSeats === 0) {
    return {
      text: 'Ausverkauft',
      color: 'bg-gray-500',
      textColor: 'text-white',
      icon: 'block',
    };
  }
  
  if (percentage < 10) {
    return {
      text: 'Nur noch wenige Tickets',
      color: 'bg-red-500',
      textColor: 'text-white',
      icon: 'warning',
    };
  }
  
  if (percentage < 20) {
    return {
      text: 'Begrenzte Verfügbarkeit',
      color: 'bg-orange-400',
      textColor: 'text-white',
      icon: 'schedule',
    };
  }
  
  if (percentage < 50) {
    return {
      text: 'Beliebt',
      color: 'bg-purple-500',
      textColor: 'text-white',
      icon: 'trending_up',
    };
  }
  
  return {
    text: 'Verfügbar',
    color: 'bg-blue-500',
    textColor: 'text-white',
    icon: 'confirmation_number',
  };
};

/**
 * Get availability message for display
 * @param {number} availableSeats - Number of available seats
 * @param {number} totalSeats - Total number of seats
 * @returns {string} - Human-readable availability message
 */
export const getAvailabilityMessage = (availableSeats, totalSeats) => {
  if (availableSeats === 0) return 'Ausverkauft';
  if (availableSeats < 50) return `Nur noch ${availableSeats} Tickets verfügbar`;
  if (availableSeats < 200) return 'Hohe Nachfrage';
  return 'Verfügbar';
};

/**
 * Get progress bar color based on availability
 * @param {number} percentage - Availability percentage
 * @returns {string} - Tailwind color class
 */
export const getProgressBarColor = (percentage) => {
  if (percentage < 10) return 'bg-red-500';
  if (percentage < 30) return 'bg-orange-500';
  if (percentage < 60) return 'bg-yellow-500';
  return 'bg-green-500';
};
