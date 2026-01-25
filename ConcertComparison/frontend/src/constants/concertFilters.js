/**
 * Filter options for concert discovery
 */
export const FILTER_OPTIONS = [
  {
    id: 'all',
    label: 'Alle Events',
    icon: 'confirmation_number',
  },
  {
    id: 'thisWeek',
    label: 'Diese Woche',
    icon: 'calendar_today',
  },
  {
    id: 'weekend',
    label: 'Wochenende',
    icon: 'weekend',
  },
  {
    id: 'popular',
    label: 'Beliebt',
    icon: 'trending_up',
  },
];

/**
 * Genre filter options
 */
export const GENRE_OPTIONS = [
  { id: 'rock', label: 'Rock' },
  { id: 'pop', label: 'Pop' },
  { id: 'electronic', label: 'Electronic' },
  { id: 'hip-hop', label: 'Hip-Hop' },
  { id: 'jazz', label: 'Jazz' },
  { id: 'classical', label: 'Klassik' },
];

/**
 * Sort options for concert list
 */
export const SORT_OPTIONS = [
  {
    id: 'date',
    label: 'Datum',
    icon: 'calendar_today',
    field: 'date',
    order: 'asc',
  },
  {
    id: 'price-asc',
    label: 'Preis (aufsteigend)',
    icon: 'arrow_upward',
    field: 'price',
    order: 'asc',
  },
  {
    id: 'price-desc',
    label: 'Preis (absteigend)',
    icon: 'arrow_downward',
    field: 'price',
    order: 'desc',
  },
  {
    id: 'name',
    label: 'Name',
    icon: 'sort_by_alpha',
    field: 'name',
    order: 'asc',
  },
];
