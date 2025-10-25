/**
 * Datums-Utility-Funktionen
 * Zentrale Sammlung aller Datums-Operationen für konsistente Verarbeitung
 */

/**
 * Konvertiert Date-Objekt zu yyyy-MM-dd String (lokale Zeit)
 * 
 * @param {Date} date - Datum zum Formatieren
 * @returns {string} Formatiertes Datum (z.B. "2025-10-22")
 * 
 * @example
 * formatDate(new Date()); // "2025-10-22"
 */
export const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * Berechnet den Montag der Woche für ein gegebenes Datum
 * 
 * @param {Date} date - Datum in der gewünschten Woche
 * @returns {Date} Montag dieser Woche
 * 
 * @example
 * getMonday(new Date('2025-10-22')); // Montag, 2025-10-20
 */
export const getMonday = (date) => {
  const d = new Date(date.getTime());
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
  return d;
};

/**
 * Generiert Array mit Wochentagen (Mo-Fr) für die aktuelle Woche
 * 
 * @param {Date} today - Heutiges Datum
 * @returns {Array} Array mit Wochentags-Objekten
 * 
 * @example
 * getWeekDays(new Date());
 * // [{ date: "2025-10-20", dayName: "Montag", ... }, ...]
 */
export const getWeekDays = (today) => {
  const monday = getMonday(today);
  const days = [];
  
  const dayNames = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag'];
  const dayShorts = ['Mo', 'Di', 'Mi', 'Do', 'Fr'];
  const monthNames = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];
  
  for (let i = 0; i < 5; i++) {
    const date = new Date(monday.getTime());
    date.setDate(monday.getDate() + i);
    
    const formattedDate = formatDate(date);
    
    days.push({
      date: formattedDate,
      dayName: dayNames[i],
      dayShort: dayShorts[i],
      dayNum: date.getDate(),
      monthShort: monthNames[date.getMonth()],
      isToday: formattedDate === formatDate(today)
    });
  }
  
  return days;
};

/**
 * Prüft ob zwei Daten den gleichen Tag repräsentieren
 * 
 * @param {Date} date1 - Erstes Datum
 * @param {Date} date2 - Zweites Datum
 * @returns {boolean} true wenn gleicher Tag
 */
export const isSameDay = (date1, date2) => {
  return formatDate(date1) === formatDate(date2);
};

/**
 * Fügt Tage zu einem Datum hinzu
 * 
 * @param {Date} date - Ausgangsdatum
 * @param {number} days - Anzahl Tage (negativ für Subtraktion)
 * @returns {Date} Neues Datum
 */
export const addDays = (date, days) => {
  const result = new Date(date.getTime());
  result.setDate(result.getDate() + days);
  return result;
};

/**
 * Generiert Array mit Wochentagen (Mo-Fr) für aktuelle und nächste Woche
 * 
 * @param {Date} today - Heutiges Datum
 * @returns {Array} Array mit beiden Wochen
 * 
 * @example
 * getTwoWeeks(new Date());
 * // [
 * //   { week: 1, label: "Diese Woche", days: [...] },
 * //   { week: 2, label: "Nächste Woche", days: [...] }
 * // ]
 */
export const getTwoWeeks = (today) => {
  const thisWeek = getWeekDays(today);
  const nextMonday = addDays(getMonday(today), 7);
  const nextWeek = getWeekDays(nextMonday);
  
  return [
    { week: 1, label: 'Diese Woche', days: thisWeek },
    { week: 2, label: 'Nächste Woche', days: nextWeek }
  ];
};
