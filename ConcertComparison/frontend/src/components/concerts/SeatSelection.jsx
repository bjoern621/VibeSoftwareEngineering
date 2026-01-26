import React from "react";
import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";
import { useHoldTimer } from "../../hooks/useHoldTimer";
import { useCart } from "../../context/CartContext";

/**
 * SeatSelection Component
 * Displays selected seat with hold confirmation dialog and countdown timer
 *
 * @param {Object} seat - Selected seat data
 * @param {Object} concert - Concert data (for cart item)
 * @param {boolean} isLoading - Loading state for hold creation
 * @param {Function} onClose - Callback when dialog is closed
 * @param {Function} onConfirm - Callback when hold is confirmed
 * @param {number} ttlSeconds - Time-to-live for hold in seconds (default: 600 = 10 minutes)
 */
const SeatSelection = ({
    seat,
    concert,
    isLoading = false,
    onClose,
    onConfirm,
    ttlSeconds = 600,
}) => {
    const navigate = useNavigate();
    const { addItem } = useCart();
    const [holdId, setHoldId] = React.useState(null);
    const [error, setError] = React.useState(null);
    const [confirmed, setConfirmed] = React.useState(false);

    // Hold timer hook
    const {
        formattedTime,
        progressPercentage,
        isActive,
        start,
        stop,
        reset,
    } = useHoldTimer(ttlSeconds, () => {
        // Handle hold expiration
        setError(
            "Reservierung ist abgelaufen. Bitte wählen Sie einen anderen Sitzplatz.",
        );
        setHoldId(null);
    });

    // Handle hold confirmation
    const handleConfirm = async () => {
        try {
            setError(null);
            const result = await onConfirm(seat);

            // Hold created successfully
            if (result?.holdId) {
                setHoldId(result.holdId);
                setConfirmed(true);
                start();

                // Add to cart
                if (concert) {
                    addItem({
                        holdId: result.holdId,
                        seat: seat,
                        concert: concert,
                        ttlSeconds: result.ttlSeconds || ttlSeconds,
                    });
                }
            }
        } catch (err) {
            console.error("Hold creation error:", err);
            setError(
                err.response?.data?.message ??
                    (err.response?.status === 409
                        ? "Dieser Sitzplatz wurde bereits reserviert. Bitte wählen Sie einen anderen."
                        : "Fehler beim Reservieren des Sitzplatzes."),
            );
        }
    };

    // Handle dialog close
    const handleClose = () => {
        if (isActive) {
            stop();
        }
        onClose();
    };

    // Handle navigation to cart
    const handleGoToCart = () => {
        if (isActive) {
            stop();
        }
        navigate('/cart');
    };

    // Reset state when seat changes
    React.useEffect(() => {
        if (seat) {
            setHoldId(null);
            setConfirmed(false);
            setError(null);
            reset();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [seat?.id]);

    // If no seat selected, don't render
    if (!seat) {
        return null;
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/50 backdrop-blur-sm"
                onClick={handleClose}
            />

            {/* Dialog */}
            <div className="relative bg-card-light dark:bg-card-dark rounded-2xl p-6 max-w-md w-full shadow-xl border border-border-light dark:border-border-dark">
                {/* Close Button */}
                <button
                    onClick={handleClose}
                    className="absolute top-4 right-4 text-text-secondary dark:text-gray-400 hover:text-text-primary dark:hover:text-white transition-colors"
                    aria-label="Schließen"
                >
                    <span className="material-symbols-outlined">close</span>
                </button>

                {/* Header */}
                <div className="text-center mb-6">
                    <span className="material-symbols-outlined text-primary text-5xl mb-3">
                        event_seat
                    </span>
                    <h3 className="text-xl font-bold text-text-primary dark:text-white">
                        {confirmed
                            ? "Reservierung erfolgreich"
                            : "Sitzplatz auswählen"}
                    </h3>
                </div>

                {/* Success State with Countdown */}
                {confirmed && holdId ? (
                    <div className="space-y-6">
                        {/* Success Message */}
                        <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-4 text-center">
                            <span className="material-symbols-outlined text-green-500 text-4xl mb-2">
                                check_circle
                            </span>
                            <p className="text-green-700 dark:text-green-300 font-medium">
                                Reservierung erfolgreich!
                            </p>
                            <p className="text-sm text-green-600 dark:text-green-400 mt-1">
                                Reservierungs-ID: {holdId}
                            </p>
                        </div>

                        {/* Countdown Timer */}
                        <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
                            <div className="flex items-center justify-between mb-2">
                                <span className="text-sm font-medium text-text-primary dark:text-white">
                                    Reservierung läuft ab in
                                </span>
                                <span className="text-2xl font-bold text-primary">
                                    {formattedTime}
                                </span>
                            </div>
                            {/* Progress Bar */}
                            <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                                <div
                                    className="bg-primary h-2 rounded-full transition-all duration-1000"
                                    style={{ width: `${progressPercentage}%` }}
                                />
                            </div>
                            <p className="text-xs text-text-secondary dark:text-gray-400 mt-2">
                                Bitte schließen Sie den Kauf innerhalb dieser
                                Zeit ab.
                            </p>
                        </div>

                        {/* Actions */}
                        <div className="flex gap-3">
                            <button
                                onClick={handleClose}
                                className="flex-1 py-3 px-4 rounded-lg border border-border-light dark:border-border-dark text-text-primary dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                            >
                                Weiter suchen
                            </button>
                            <button
                                onClick={handleGoToCart}
                                className="flex-1 py-3 px-4 rounded-lg bg-primary hover:bg-primary-dark text-white font-semibold transition-colors flex items-center justify-center gap-2"
                            >
                                <span className="material-symbols-outlined text-xl">
                                    shopping_cart
                                </span>
                                Zum Warenkorb
                            </button>
                        </div>
                    </div>
                ) : (
                    /* Seat Selection Form */
                    <div className="space-y-4">
                        {/* Seat Details */}
                        <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 space-y-3">
                            <div className="flex justify-between items-center py-2 border-b border-border-light dark:border-border-dark">
                                <span className="text-text-secondary dark:text-gray-400">
                                    Block
                                </span>
                                <span className="font-medium text-text-primary dark:text-white">
                                    {seat.block || "Allgemein"}
                                </span>
                            </div>
                            <div className="flex justify-between items-center py-2 border-b border-border-light dark:border-border-dark">
                                <span className="text-text-secondary dark:text-gray-400">
                                    Kategorie
                                </span>
                                <span className="font-medium text-text-primary dark:text-white">
                                    {seat.category || "Standard"}
                                </span>
                            </div>
                            <div className="flex justify-between items-center py-2 border-b border-border-light dark:border-border-dark">
                                <span className="text-text-secondary dark:text-gray-400">
                                    Reihe
                                </span>
                                <span className="font-medium text-text-primary dark:text-white">
                                    {seat.row}
                                </span>
                            </div>
                            <div className="flex justify-between items-center py-2 border-b border-border-light dark:border-border-dark">
                                <span className="text-text-secondary dark:text-gray-400">
                                    Sitzplatz
                                </span>
                                <span className="font-medium text-text-primary dark:text-white">
                                    {seat.number}
                                </span>
                            </div>
                            <div className="flex justify-between items-center py-3">
                                <span className="text-text-secondary dark:text-gray-400">
                                    Preis
                                </span>
                                <span className="text-2xl font-bold text-price">
                                    {seat.price
                                        ? `${seat.price.toFixed(2)} €`
                                        : "-"}
                                </span>
                            </div>
                        </div>

                        {/* Error Message */}
                        {error && (
                            <div className="bg-red-50 dark:bg-red-900/20 rounded-lg p-4 flex items-start gap-3">
                                <span className="material-symbols-outlined text-red-500 mt-0.5">
                                    error
                                </span>
                                <p className="text-sm text-red-700 dark:text-red-300 flex-1">
                                    {error}
                                </p>
                            </div>
                        )}

                        {/* Actions */}
                        <div className="flex gap-3">
                            <button
                                onClick={handleClose}
                                className="flex-1 py-3 px-4 rounded-lg border border-border-light dark:border-border-dark text-text-primary dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                            >
                                Abbrechen
                            </button>
                            <button
                                onClick={handleConfirm}
                                disabled={isLoading}
                                className="flex-1 py-3 px-4 rounded-lg bg-primary hover:bg-primary-dark text-white font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                            >
                                {isLoading ? (
                                    <>
                                        <span className="material-symbols-outlined animate-spin">
                                            progress_activity
                                        </span>
                                        Wird reserviert...
                                    </>
                                ) : (
                                    <>
                                        <span className="material-symbols-outlined">
                                            bookmark_add
                                        </span>
                                        Reservieren
                                    </>
                                )}
                            </button>
                        </div>

                        {/* Info Text */}
                        <p className="text-xs text-text-secondary dark:text-gray-400 text-center">
                            Die Reservierung ist {Math.floor(ttlSeconds / 60)}{" "}
                            Minuten gültig. Schließen Sie den Kauf in dieser
                            Zeit ab.
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

SeatSelection.propTypes = {
    seat: PropTypes.shape({
        id: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
            .isRequired,
        block: PropTypes.string,
        category: PropTypes.string,
        row: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
        number: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
        price: PropTypes.number,
        status: PropTypes.string,
    }),
    concert: PropTypes.shape({
        id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
        name: PropTypes.string,
        date: PropTypes.string,
        venue: PropTypes.string,
        imageUrl: PropTypes.string,
    }),
    isLoading: PropTypes.bool,
    onClose: PropTypes.func.isRequired,
    onConfirm: PropTypes.func.isRequired,
    ttlSeconds: PropTypes.number,
};

export default SeatSelection;
