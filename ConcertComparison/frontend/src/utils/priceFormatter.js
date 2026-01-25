/**
 * Format price to currency string
 * @param {number} price - Price value
 * @param {string} currency - Currency code (default: 'EUR')
 * @param {string} locale - Locale for formatting (default: 'de-DE')
 * @returns {string} - Formatted price
 */
export const formatPrice = (price, currency = 'EUR', locale = 'de-DE') => {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
  }).format(price);
};

/**
 * Format price range
 * @param {number} minPrice - Minimum price
 * @param {number} maxPrice - Maximum price
 * @param {string} currency - Currency code (default: 'EUR')
 * @param {string} locale - Locale for formatting (default: 'de-DE')
 * @returns {string} - Formatted price range
 */
export const formatPriceRange = (minPrice, maxPrice, currency = 'EUR', locale = 'de-DE') => {
  if (minPrice === maxPrice) {
    return formatPrice(minPrice, currency, locale);
  }
  return `${formatPrice(minPrice, currency, locale)} - ${formatPrice(maxPrice, currency, locale)}`;
};
