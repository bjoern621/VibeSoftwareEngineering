// Utility helpers for number formatting

/**
 * Safe formatting for numbers to fixed decimals.
 * Returns a string (e.g. "0.00") for safe display.
 *
 * @param {any} value - value to format
 * @param {number} decimals - number of decimals
 * @param {string} fallback - fallback string when value is not a finite number
 * @returns {string}
 */
export function safeToFixed(value, decimals = 2, fallback = '0.00') {
  if (value === undefined || value === null) return fallback;
  const num = Number(value);
  if (!Number.isFinite(num)) return fallback;
  try {
    return num.toFixed(decimals);
  } catch (e) {
    return fallback;
  }
}
