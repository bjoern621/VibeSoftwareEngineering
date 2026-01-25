/**
 * Format ISO date string to readable format
 * @param {string} isoDate - ISO 8601 date string
 * @param {string} locale - Locale for formatting (default: 'de-DE')
 * @returns {string} - Formatted date
 */
export const formatDate = (isoDate, locale = 'de-DE') => {
  const date = new Date(isoDate);
  return date.toLocaleDateString(locale, {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

/**
 * Format ISO date string to readable date and time
 * @param {string} isoDate - ISO 8601 date string
 * @param {string} locale - Locale for formatting (default: 'de-DE')
 * @returns {string} - Formatted date and time
 */
export const formatDateTime = (isoDate, locale = 'de-DE') => {
  const date = new Date(isoDate);
  return date.toLocaleDateString(locale, {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

/**
 * Format ISO date string to time only
 * @param {string} isoDate - ISO 8601 date string
 * @returns {string} - Formatted time (HH:MM)
 */
export const formatTime = (isoDate) => {
  const date = new Date(isoDate);
  return date.toLocaleTimeString('de-DE', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

/**
 * Get relative time string (e.g., "in 3 days")
 * @param {string} isoDate - ISO 8601 date string
 * @returns {string} - Relative time string
 */
export const getRelativeTime = (isoDate) => {
  const date = new Date(isoDate);
  const now = new Date();
  const diff = date - now;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  if (days < 0) return 'Vergangen';
  if (days === 0) return 'Heute';
  if (days === 1) return 'Morgen';
  if (days < 7) return `In ${days} Tagen`;
  if (days < 30) return `In ${Math.floor(days / 7)} Wochen`;
  return `In ${Math.floor(days / 30)} Monaten`;
};

/**
 * Check if date is in the current week
 * @param {string} isoDate - ISO 8601 date string
 * @returns {boolean}
 */
export const isThisWeek = (isoDate) => {
  const date = new Date(isoDate);
  const now = new Date();
  const diff = date - now;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  return days >= 0 && days < 7;
};

/**
 * Check if date is on a weekend (Saturday or Sunday)
 * @param {string} isoDate - ISO 8601 date string
 * @returns {boolean}
 */
export const isWeekend = (isoDate) => {
  const date = new Date(isoDate);
  const day = date.getDay();
  return day === 0 || day === 6; // Sunday = 0, Saturday = 6
};
