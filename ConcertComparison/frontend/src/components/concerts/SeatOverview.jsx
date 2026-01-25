import React from "react";
import {
    SEAT_STATUS,
    SEAT_STATUS_LABELS,
    getSeatColorClasses,
} from "../../constants/seatConstants";
import { formatPrice } from "../../utils/priceFormatter";

/**
 * Individual Seat Component
 * @param {Object} seat - Seat data
 * @param {boolean} isSelected - Whether the seat is currently selected
 * @param {Function} onSelect - Callback when seat is selected
 */
const Seat = ({ seat, isSelected, onSelect }) => {
    const isClickable = seat.status === SEAT_STATUS.AVAILABLE;

    const handleClick = () => {
        if (isClickable) {
            onSelect(seat);
        }
    };

    const handleKeyPress = (e) => {
        if ((e.key === "Enter" || e.key === " ") && isClickable) {
            e.preventDefault();
            onSelect(seat);
        }
    };

    return (
        <button
            onClick={handleClick}
            onKeyPress={handleKeyPress}
            disabled={!isClickable}
            title={`Reihe ${seat.row}, Platz ${seat.number} - ${SEAT_STATUS_LABELS[seat.status]} - ${formatPrice(seat.price)}`}
            className={`
        w-10 h-10 rounded-lg text-xs font-medium
        flex items-center justify-center
        transition-all duration-200
        ${getSeatColorClasses(seat.status, isSelected)}
        focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-1
      `}
            aria-label={`Sitzplatz Reihe ${seat.row}, Nummer ${seat.number}, ${SEAT_STATUS_LABELS[seat.status]}`}
        >
            {seat.number}
        </button>
    );
};

/**
 * Category Availability Card Component
 * Shows availability statistics for a specific category
 * @param {Object} availability - Availability data
 * @param {string} category - Category name
 */
const CategoryAvailabilityCard = ({ availability, category }) => {
    const total =
        (availability.available || 0) +
        (availability.held || 0) +
        (availability.sold || 0);
    const availablePercentage =
        total > 0 ? Math.round((availability.available / total) * 100) : 0;

    return (
        <div className="bg-card-light dark:bg-card-dark rounded-xl p-4 border border-border-light dark:border-border-dark">
            <h4 className="font-semibold text-text-primary dark:text-white mb-3">
                {category}
            </h4>
            <div className="grid grid-cols-4 gap-2">
                <div className="text-center">
                    <p className="text-lg font-bold text-green-600 dark:text-green-400">
                        {availability.available || 0}
                    </p>
                    <p className="text-xs text-text-secondary dark:text-gray-400">
                        Verfügbar
                    </p>
                </div>
                <div className="text-center">
                    <p className="text-lg font-bold text-yellow-600 dark:text-yellow-400">
                        {availability.held || 0}
                    </p>
                    <p className="text-xs text-text-secondary dark:text-gray-400">
                        Reserviert
                    </p>
                </div>
                <div className="text-center">
                    <p className="text-lg font-bold text-gray-600 dark:text-gray-400">
                        {availability.sold || 0}
                    </p>
                    <p className="text-xs text-text-secondary dark:text-gray-400">
                        Verkauft
                    </p>
                </div>
                <div className="text-center">
                    <p className="text-lg font-bold text-text-primary dark:text-white">
                        {total}
                    </p>
                    <p className="text-xs text-text-secondary dark:text-gray-400">
                        Gesamt
                    </p>
                </div>
            </div>
            {/* Progress Bar */}
            <div className="mt-3 w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div
                    className="bg-green-500 h-2 rounded-full transition-all duration-500"
                    style={{ width: `${availablePercentage}%` }}
                />
            </div>
            <p className="text-xs text-text-secondary dark:text-gray-400 mt-1">
                {availablePercentage}% verfügbar
            </p>
        </div>
    );
};

/**
 * Seat Category Section Component
 * Displays all seats in a category grouped by block
 * @param {string} category - Category name
 * @param {Array} seats - Array of seats in this category
 * @param {Object} selectedSeat - Currently selected seat
 * @param {Function} onSeatSelect - Callback when seat is selected
 */
const SeatCategorySection = ({
    category,
    seats,
    selectedSeat,
    onSeatSelect,
}) => {
    // Group seats by block
    const seatsByBlock = seats.reduce((acc, seat) => {
        const block = seat.block || "Allgemein";
        if (!acc[block]) {
            acc[block] = [];
        }
        acc[block].push(seat);
        return acc;
    }, {});

    const blockNames = Object.keys(seatsByBlock).sort();

    return (
        <div className="bg-card-light dark:bg-card-dark rounded-xl p-6 shadow-card border border-border-light dark:border-border-dark">
            <h3 className="text-xl font-semibold text-text-primary dark:text-white mb-4 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary">
                    category
                </span>
                {category}
            </h3>

            <div className="space-y-4">
                {blockNames.map((blockName) => {
                    // Group seats by row within block
                    const seatsByRow = seatsByBlock[blockName].reduce(
                        (acc, seat) => {
                            const row = seat.row || "1";
                            if (!acc[row]) {
                                acc[row] = [];
                            }
                            acc[row].push(seat);
                            return acc;
                        },
                        {},
                    );

                    const sortedRows = Object.keys(seatsByRow).sort((a, b) => {
                        const numA = parseInt(a, 10);
                        const numB = parseInt(b, 10);
                        if (!isNaN(numA) && !isNaN(numB)) return numA - numB;
                        return a.localeCompare(b);
                    });

                    return (
                        <div
                            key={blockName}
                            className="bg-gray-50 dark:bg-gray-800/50 rounded-lg p-4"
                        >
                            <h4 className="text-sm font-medium text-text-primary dark:text-white mb-3">
                                Block {blockName}
                            </h4>
                            <div className="space-y-2">
                                {sortedRows.map((row) => (
                                    <div
                                        key={row}
                                        className="flex items-center gap-3"
                                    >
                                        <span className="w-16 text-xs font-medium text-text-secondary dark:text-gray-400 shrink-0">
                                            Reihe {row}
                                        </span>
                                        <div className="flex flex-wrap gap-2">
                                            {seatsByRow[row]
                                                .sort(
                                                    (a, b) =>
                                                        parseInt(a.number, 10) -
                                                        parseInt(b.number, 10),
                                                )
                                                .map((seat) => (
                                                    <Seat
                                                        key={seat.id}
                                                        seat={seat}
                                                        isSelected={
                                                            selectedSeat?.id ===
                                                            seat.id
                                                        }
                                                        onSelect={onSeatSelect}
                                                    />
                                                ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

/**
 * Seat Legend Component
 */
const SeatLegend = () => (
    <div className="flex flex-wrap gap-4 justify-center p-4 bg-gray-100 dark:bg-gray-800 rounded-lg">
        <div className="flex items-center gap-2">
            <span className="w-6 h-6 bg-green-500 rounded"></span>
            <span className="text-sm text-text-secondary dark:text-gray-400">
                Verfügbar
            </span>
        </div>
        <div className="flex items-center gap-2">
            <span className="w-6 h-6 bg-yellow-400 rounded"></span>
            <span className="text-sm text-text-secondary dark:text-gray-400">
                Reserviert
            </span>
        </div>
        <div className="flex items-center gap-2">
            <span className="w-6 h-6 bg-gray-400 rounded"></span>
            <span className="text-sm text-text-secondary dark:text-gray-400">
                Verkauft
            </span>
        </div>
        <div className="flex items-center gap-2">
            <span className="w-6 h-6 bg-primary rounded"></span>
            <span className="text-sm text-text-secondary dark:text-gray-400">
                Ausgewählt
            </span>
        </div>
    </div>
);

/**
 * Loading Skeleton Component
 */
const LoadingSkeleton = () => (
    <div className="animate-pulse space-y-6">
        <div className="h-32 bg-gray-200 dark:bg-gray-700 rounded-xl" />
        <div className="space-y-4">
            {[1, 2, 3].map((i) => (
                <div
                    key={i}
                    className="h-48 bg-gray-200 dark:bg-gray-700 rounded-xl"
                />
            ))}
        </div>
    </div>
);

/**
 * Error State Component
 */
const ErrorState = ({ error, onRetry }) => (
    <div className="text-center py-16">
        <span className="material-symbols-outlined text-red-500 text-6xl mb-4">
            error
        </span>
        <h2 className="text-2xl font-bold text-text-primary dark:text-white mb-2">
            Fehler beim Laden der Sitzplätze
        </h2>
        <p className="text-text-secondary dark:text-gray-400 mb-6">{error}</p>
        <button
            onClick={onRetry}
            className="px-6 py-3 bg-primary hover:bg-primary-dark text-white rounded-lg transition-colors inline-flex items-center gap-2"
        >
            <span className="material-symbols-outlined">refresh</span>
            Erneut versuchen
        </button>
    </div>
);

/**
 * Empty State Component
 */
const EmptyState = () => (
    <div className="text-center py-16 bg-card-light dark:bg-card-dark rounded-xl">
        <span className="material-symbols-outlined text-gray-400 text-6xl mb-4">
            event_seat
        </span>
        <h2 className="text-xl font-bold text-text-primary dark:text-white mb-2">
            Keine Sitzplätze verfügbar
        </h2>
        <p className="text-text-secondary dark:text-gray-400">
            Für dieses Konzert sind keine Sitzplätze vorhanden.
        </p>
    </div>
);

/**
 * SeatOverview Component
 * Main component for displaying seat overview organized by category
 *
 * @param {Array} seats - Array of seat objects
 * @param {Object} availabilityByCategory - Aggregated availability per category
 * @param {Object} selectedSeat - Currently selected seat
 * @param {Function} onSeatSelect - Callback when seat is selected
 * @param {boolean} loading - Loading state
 * @param {string} error - Error message
 * @param {Function} onRetry - Retry callback
 */
const SeatOverview = ({
    seats = [],
    availabilityByCategory = [],
    selectedSeat = null,
    onSeatSelect,
    loading = false,
    error = null,
    onRetry,
}) => {
    // Group seats by category
    const seatsByCategory = seats.reduce((acc, seat) => {
        const category = seat.category || "Standard";
        if (!acc[category]) {
            acc[category] = [];
        }
        acc[category].push(seat);
        return acc;
    }, {});

    const categoryNames = Object.keys(seatsByCategory).sort();

    // Loading state
    if (loading) {
        return <LoadingSkeleton />;
    }

    // Error state
    if (error) {
        return <ErrorState error={error} onRetry={onRetry} />;
    }

    // Empty state
    if (categoryNames.length === 0) {
        return <EmptyState />;
    }

    return (
        <div className="space-y-8">
            {/* Availability by Category Cards */}
            {availabilityByCategory.length > 0 && (
                <div>
                    <h2 className="text-2xl font-bold text-text-primary dark:text-white mb-4">
                        Verfügbarkeit nach Kategorie
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {availabilityByCategory.map((availability) => (
                            <CategoryAvailabilityCard
                                key={availability.category}
                                category={availability.category}
                                availability={availability}
                            />
                        ))}
                    </div>
                </div>
            )}

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

            {/* Seat Categories */}
            <div className="space-y-6">
                {categoryNames.map((category) => (
                    <SeatCategorySection
                        key={category}
                        category={category}
                        seats={seatsByCategory[category]}
                        selectedSeat={selectedSeat}
                        onSeatSelect={onSeatSelect}
                    />
                ))}
            </div>
        </div>
    );
};

export default SeatOverview;
