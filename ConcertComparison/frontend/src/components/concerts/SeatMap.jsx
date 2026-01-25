import React, { useState } from 'react';
import { formatPrice } from '../../utils/priceFormatter';

/**
 * Get color classes based on seat status
 * @param {string} status - Seat status (AVAILABLE, HELD, SOLD)
 * @param {boolean} isSelected - Whether the seat is currently selected
 * @returns {string} - Tailwind CSS classes
 */
const getSeatColorClasses = (status, isSelected) => {
  if (isSelected) {
    return 'bg-primary text-white ring-2 ring-primary ring-offset-2';
  }

  switch (status) {
    case 'AVAILABLE':
      return 'bg-blue-500 hover:bg-blue-600 text-white cursor-pointer';
    case 'HELD':
      return 'bg-yellow-400 text-gray-800 cursor-not-allowed';
    case 'SOLD':
      return 'bg-gray-400 text-gray-600 cursor-not-allowed';
    default:
      return 'bg-gray-300 text-gray-500';
  }
};

/**
 * Get status label in German
 * @param {string} status - Seat status
 * @returns {string} - German label
 */
const getStatusLabel = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return 'Verfügbar';
    case 'HELD':
      return 'Reserviert';
    case 'SOLD':
      return 'Verkauft';
    default:
      return 'Unbekannt';
  }
};

/**
 * Individual Seat Component
 */
const Seat = ({ seat, isSelected, onSelect }) => {
  const isClickable = seat.status === 'AVAILABLE';

  const handleClick = () => {
    if (isClickable) {
      onSelect(seat);
    }
  };

  const handleKeyPress = (e) => {
    if ((e.key === 'Enter' || e.key === ' ') && isClickable) {
      e.preventDefault();
      onSelect(seat);
    }
  };

  return (
    <button
      onClick={handleClick}
      onKeyPress={handleKeyPress}
      disabled={!isClickable}
      title={`Reihe ${seat.row}, Platz ${seat.number} - ${getStatusLabel(seat.status)} - ${formatPrice(seat.price)}`}
      className={`
        w-10 h-10 rounded-lg text-xs font-medium
        flex items-center justify-center
        transition-all duration-200
        ${getSeatColorClasses(seat.status, isSelected)}
        ${isClickable ? 'hover:scale-110 hover:shadow-md' : ''}
        focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1
      `}
      aria-label={`Sitzplatz Reihe ${seat.row}, Nummer ${seat.number}, ${getStatusLabel(seat.status)}`}
    >
      {seat.number}
    </button>
  );
};

/**
 * Seat Block Component - Groups seats by block/section
 */
const SeatBlock = ({ blockName, seats, selectedSeat, onSeatSelect }) => {
  // Group seats by row
  const seatsByRow = seats.reduce((acc, seat) => {
    const row = seat.row || '1';
    if (!acc[row]) {
      acc[row] = [];
    }
    acc[row].push(seat);
    return acc;
  }, {});

  // Sort rows numerically/alphabetically
  const sortedRows = Object.keys(seatsByRow).sort((a, b) => {
    const numA = parseInt(a, 10);
    const numB = parseInt(b, 10);
    if (!isNaN(numA) && !isNaN(numB)) return numA - numB;
    return a.localeCompare(b);
  });

  return (
    <div className="bg-card-light dark:bg-card-dark rounded-xl p-6 shadow-card border border-border-light dark:border-border-dark">
      <h4 className="text-lg font-semibold text-text-primary dark:text-white mb-4 flex items-center gap-2">
        <span className="material-symbols-outlined text-primary">stadium</span>
        Block {blockName}
      </h4>

      <div className="space-y-3">
        {sortedRows.map((row) => (
          <div key={row} className="flex items-center gap-3">
            {/* Row label */}
            <span className="w-12 text-sm font-medium text-text-secondary dark:text-gray-400">
              Reihe {row}
            </span>

            {/* Seats in this row */}
            <div className="flex flex-wrap gap-2">
              {seatsByRow[row]
                .sort((a, b) => parseInt(a.number, 10) - parseInt(b.number, 10))
                .map((seat) => (
                  <Seat
                    key={seat.id}
                    seat={seat}
                    isSelected={selectedSeat?.id === seat.id}
                    onSelect={onSeatSelect}
                  />
                ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

/**
 * Legend Component - Shows seat status colors
 */
const SeatLegend = () => (
  <div className="flex flex-wrap gap-4 justify-center p-4 bg-gray-100 dark:bg-gray-800 rounded-lg">
    <div className="flex items-center gap-2">
      <span className="w-6 h-6 bg-blue-500 rounded"></span>
      <span className="text-sm text-text-secondary dark:text-gray-400">Verfügbar</span>
    </div>
    <div className="flex items-center gap-2">
      <span className="w-6 h-6 bg-yellow-400 rounded"></span>
      <span className="text-sm text-text-secondary dark:text-gray-400">Reserviert</span>
    </div>
    <div className="flex items-center gap-2">
      <span className="w-6 h-6 bg-gray-400 rounded"></span>
      <span className="text-sm text-text-secondary dark:text-gray-400">Verkauft</span>
    </div>
    <div className="flex items-center gap-2">
      <span className="w-6 h-6 bg-primary rounded"></span>
      <span className="text-sm text-text-secondary dark:text-gray-400">Ausgewählt</span>
    </div>
  </div>
);

/**
 * SeatMap Component
 * Displays an interactive seat map organized by blocks
 */
const SeatMap = ({ seatsByBlock, selectedSeat, onSeatSelect, availability }) => {
  const [expandedBlocks, setExpandedBlocks] = useState(
    Object.keys(seatsByBlock).reduce((acc, block) => ({ ...acc, [block]: true }), {})
  );

  const toggleBlock = (blockName) => {
    setExpandedBlocks((prev) => ({
      ...prev,
      [blockName]: !prev[blockName],
    }));
  };

  const blockNames = Object.keys(seatsByBlock).sort();

  if (blockNames.length === 0) {
    return (
      <div className="text-center py-12 bg-card-light dark:bg-card-dark rounded-xl">
        <span className="material-symbols-outlined text-gray-400 text-6xl mb-4">
          event_seat
        </span>
        <p className="text-text-secondary dark:text-gray-400">
          Keine Sitzplätze für dieses Konzert verfügbar
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Availability Summary */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4 text-center">
          <p className="text-2xl font-bold text-blue-600 dark:text-blue-400">
            {availability.available}
          </p>
          <p className="text-sm text-text-secondary dark:text-gray-400">Verfügbar</p>
        </div>
        <div className="bg-yellow-50 dark:bg-yellow-900/20 rounded-lg p-4 text-center">
          <p className="text-2xl font-bold text-yellow-600 dark:text-yellow-400">
            {availability.held}
          </p>
          <p className="text-sm text-text-secondary dark:text-gray-400">Reserviert</p>
        </div>
        <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 text-center">
          <p className="text-2xl font-bold text-gray-600 dark:text-gray-400">
            {availability.sold}
          </p>
          <p className="text-sm text-text-secondary dark:text-gray-400">Verkauft</p>
        </div>
        <div className="bg-purple-50 dark:bg-purple-900/20 rounded-lg p-4 text-center">
          <p className="text-2xl font-bold text-purple-600 dark:text-purple-400">
            {availability.total}
          </p>
          <p className="text-sm text-text-secondary dark:text-gray-400">Gesamt</p>
        </div>
      </div>

      {/* Legend */}
      <SeatLegend />

      {/* Stage Indicator */}
      <div className="relative">
        <div className="bg-gradient-to-b from-primary/20 to-transparent rounded-t-3xl p-6 text-center">
          <span className="text-primary font-semibold text-lg tracking-wider">
            🎤 BÜHNE
          </span>
        </div>
      </div>

      {/* Seat Blocks */}
      <div className="space-y-4">
        {blockNames.map((blockName) => (
          <div key={blockName}>
            {/* Block Header (collapsible) */}
            <button
              onClick={() => toggleBlock(blockName)}
              className="w-full flex items-center justify-between p-3 bg-gray-100 dark:bg-gray-800 rounded-lg mb-2 hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
            >
              <span className="font-medium text-text-primary dark:text-white">
                Block {blockName} ({seatsByBlock[blockName].length} Plätze)
              </span>
              <span
                className={`material-symbols-outlined transition-transform ${
                  expandedBlocks[blockName] ? 'rotate-180' : ''
                }`}
              >
                expand_more
              </span>
            </button>

            {/* Seat Grid (collapsible content) */}
            {expandedBlocks[blockName] && (
              <SeatBlock
                blockName={blockName}
                seats={seatsByBlock[blockName]}
                selectedSeat={selectedSeat}
                onSeatSelect={onSeatSelect}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default SeatMap;
