/**
 * Seat status constants
 */
export const SEAT_STATUS = {
    AVAILABLE: "AVAILABLE",
    HELD: "HELD",
    SOLD: "SOLD",
};

/**
 * Seat status labels in German
 */
export const SEAT_STATUS_LABELS = {
    [SEAT_STATUS.AVAILABLE]: "Verfügbar",
    [SEAT_STATUS.HELD]: "Reserviert",
    [SEAT_STATUS.SOLD]: "Verkauft",
};

/**
 * Seat status colors for UI
 */
export const SEAT_STATUS_COLORS = {
    [SEAT_STATUS.AVAILABLE]: "bg-green-500 hover:bg-green-600",
    [SEAT_STATUS.HELD]: "bg-yellow-400",
    [SEAT_STATUS.SOLD]: "bg-gray-400",
};

/**
 * Seat status colors for light/dark mode (with ring)
 */
export const getSeatColorClasses = (status, isSelected = false) => {
    if (isSelected) {
        return "bg-primary text-white ring-2 ring-primary ring-offset-2";
    }

    switch (status) {
        case SEAT_STATUS.AVAILABLE:
            return (
                SEAT_STATUS_COLORS.AVAILABLE +
                " text-white cursor-pointer hover:scale-110 transition-all"
            );
        case SEAT_STATUS.HELD:
            return (
                SEAT_STATUS_COLORS.HELD + " text-gray-800 cursor-not-allowed"
            );
        case SEAT_STATUS.SOLD:
            return (
                SEAT_STATUS_COLORS.SOLD + " text-gray-600 cursor-not-allowed"
            );
        default:
            return "bg-gray-300 text-gray-500 cursor-not-allowed";
    }
};
