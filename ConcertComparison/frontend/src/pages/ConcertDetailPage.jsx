import React, { useState, useMemo } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useConcertDetail } from "../hooks/useConcertDetail";
import { useAuth } from "../context/AuthContext";
import SeatOverview from "../components/concerts/SeatOverview";
import ConnectionStatusBadge from "../components/common/ConnectionStatusBadge";
import { formatDateTime, formatTime } from "../utils/dateFormatter";
import { formatPrice } from "../utils/priceFormatter";
import { createSeatHold } from "../services/seatService";
import SeatSelection from "../components/concerts/SeatSelection";

/**
 * Breadcrumb Navigation Component
 */
const Breadcrumb = ({ concertName }) => (
    <nav className="flex items-center gap-2 text-sm text-text-secondary dark:text-gray-400 mb-6">
        <Link
            to="/concerts"
            className="hover:text-primary transition-colors flex items-center gap-1"
        >
            <span className="material-symbols-outlined text-lg">home</span>
            Konzerte
        </Link>
        <span className="material-symbols-outlined text-lg">chevron_right</span>
        <span className="text-text-primary dark:text-white font-medium truncate max-w-xs">
            {concertName || "Details"}
        </span>
    </nav>
);

/**
 * Concert Header Component - Kompaktes Hero mit key info
 */
const ConcertHeader = ({ concert }) => {
    const [imageError, setImageError] = useState(false);
    const imageSrc =
        imageError || !concert.imageUrl
            ? "/placeholder_concert.svg"
            : concert.imageUrl;

    return (
        <div className="relative rounded-xl overflow-hidden mb-6">
            {/* Background Image with Overlay */}
            <div className="absolute inset-0">
                <img
                    src={imageSrc}
                    alt={concert.name}
                    onError={() => setImageError(true)}
                    className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/50 to-transparent" />
            </div>

            {/* Content - Kompakter */}
            <div className="relative p-6 md:p-8 min-h-[200px] flex flex-col justify-end">
                <h1 className="text-2xl md:text-3xl lg:text-4xl font-bold text-white mb-3">
                    {concert.name}
                </h1>

                <div className="flex flex-wrap items-center gap-3 md:gap-4 text-sm md:text-base text-white/90">
                    {/* Date & Time */}
                    <div className="flex items-center gap-1.5 bg-white/10 backdrop-blur-sm px-3 py-1.5 rounded-lg">
                        <span className="material-symbols-outlined text-lg">
                            calendar_today
                        </span>
                        <span className="font-medium">{formatDateTime(concert.date)}</span>
                    </div>

                    {/* Time */}
                    <div className="flex items-center gap-1.5 bg-white/10 backdrop-blur-sm px-3 py-1.5 rounded-lg">
                        <span className="material-symbols-outlined text-lg">
                            schedule
                        </span>
                        <span className="font-medium">{formatTime(concert.date)} Uhr</span>
                    </div>

                    {/* Venue */}
                    <div className="flex items-center gap-1.5 bg-white/10 backdrop-blur-sm px-3 py-1.5 rounded-lg">
                        <span className="material-symbols-outlined text-lg">
                            location_on
                        </span>
                        <span className="font-medium">{concert.venue}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

/**
 * Concert Info Card Component
 */
const ConcertInfoCard = ({ concert, minPrice, maxPrice, availableSeats, totalSeats }) => (
    <div className="bg-white dark:bg-card-dark rounded-xl p-6 shadow-lg border border-gray-100 dark:border-gray-700">
        <h2 className="text-lg font-bold text-text-primary dark:text-white mb-4 flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">event_note</span>
            Event Details
        </h2>

        {/* Description */}
        {concert.description && (
            <p className="text-sm text-text-secondary dark:text-gray-400 mb-5 leading-relaxed">
                {concert.description}
            </p>
        )}

        {/* Info Grid - Verbessert */}
        <div className="space-y-3">
            {/* Price Range */}
            <div className="bg-gradient-to-br from-primary/5 to-primary/10 dark:from-primary/10 dark:to-primary/20 rounded-lg p-4 border border-primary/20">
                <div className="flex items-center justify-between mb-2">
                    <p className="text-xs font-medium text-text-secondary dark:text-gray-400 uppercase tracking-wide">
                        Ticketpreise
                    </p>
                    <span className="material-symbols-outlined text-primary text-lg">sell</span>
                </div>
                <p className="text-2xl font-bold text-primary">
                    {minPrice > 0 ? formatPrice(minPrice) : 'N/A'}
                </p>
                {maxPrice > 0 && maxPrice !== minPrice && (
                    <p className="text-sm text-text-secondary dark:text-gray-400 mt-1">
                        bis {formatPrice(maxPrice)}
                    </p>
                )}
            </div>

            {/* Availability */}
            <div className="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-lg p-4 border border-green-200 dark:border-green-700">
                <div className="flex items-center justify-between mb-2">
                    <p className="text-xs font-medium text-text-secondary dark:text-gray-400 uppercase tracking-wide">
                        Verfügbarkeit
                    </p>
                    <span className="material-symbols-outlined text-green-600 dark:text-green-400 text-lg">event_available</span>
                </div>
                <p className="text-2xl font-bold text-green-600 dark:text-green-400">
                    {availableSeats}
                </p>
                <p className="text-sm text-text-secondary dark:text-gray-400 mt-1">
                    von {totalSeats} Plätzen frei
                </p>
            </div>
        </div>
    </div>
);

/**
 * Loading Skeleton Component
 */
const LoadingSkeleton = () => (
    <div className="animate-pulse space-y-8">
        {/* Header Skeleton */}
        <div className="h-[300px] bg-gray-200 dark:bg-gray-700 rounded-2xl" />

        {/* Content Skeleton */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-4">
                <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-1/3" />
                <div className="h-64 bg-gray-200 dark:bg-gray-700 rounded-xl" />
            </div>
            <div className="space-y-4">
                <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-1/2" />
                <div className="h-48 bg-gray-200 dark:bg-gray-700 rounded-xl" />
            </div>
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
            Fehler beim Laden
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
 * ConcertDetailPage Component
 * Main page component for displaying concert details and seat selection
 */
const ConcertDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [holdLoading, setHoldLoading] = useState(false);
    const [notification, setNotification] = useState(null);
    // Get user email from auth context - this must match the checkout user!
    const userId = user?.email || localStorage.getItem("userId") || "anonymous";

    const {
        concert,
        seats,
        seatsByBlock,
        availability,
        loading,
        error,
        selectedSeat,
        handleSeatSelect,
        clearSeatSelection,
        updateSeatStatus,
        refresh,
        // SSE Status
        connectionStatus,
        isConnected,
        isReconnecting,
        sseHasError,
        sseReconnect,
    } = useConcertDetail(id);

    // Calculate min and max price from seats
    const minPrice = useMemo(() => {
        if (seats.length === 0) return 0;
        const prices = seats.map(s => s.price || 0).filter(p => p > 0);
        return prices.length > 0 ? Math.min(...prices) : 0;
    }, [seats]);

    const maxPrice = useMemo(() => {
        if (seats.length === 0) return 0;
        const prices = seats.map(s => s.price || 0).filter(p => p > 0);
        return prices.length > 0 ? Math.max(...prices) : 0;
    }, [seats]);

    // Calculate available seats count
    const availableSeatsCount = useMemo(() => {
        return seats.filter(seat => seat.status === "AVAILABLE").length;
    }, [seats]);

    // Calculate total seats count
    const totalSeatsCount = useMemo(() => {
        return seats.length;
    }, [seats]);

    // Calculate availability by category for SeatOverview
    const availabilityByCategory = useMemo(() => {
        const categoryStats = {};
        seats.forEach((seat) => {
            const category = seat.category || "Standard";
            if (!categoryStats[category]) {
                categoryStats[category] = {
                    category,
                    available: 0,
                    held: 0,
                    sold: 0,
                    price: seat.price || 0,
                };
            }
            if (seat.status === "AVAILABLE") {
                categoryStats[category].available++;
            } else if (seat.status === "HELD") {
                categoryStats[category].held++;
            } else if (seat.status === "SOLD") {
                categoryStats[category].sold++;
            }
        });
        return Object.values(categoryStats).sort((a, b) =>
            a.category.localeCompare(b.category),
        );
    }, [seats]);

    /**
     * Handle seat hold confirmation
     */
    const handleHoldConfirm = async (seat) => {
        setHoldLoading(true);
        try {
            const reservation = await createSeatHold(seat.id, userId);

            // Update seat status locally
            updateSeatStatus(seat.id, "HELD");

            // Show success notification
            setNotification({
                type: "success",
                message: `Sitzplatz ${seat.row}/${seat.number} erfolgreich reserviert!`,
            });

            // Auto-hide notification after 5 seconds
            setTimeout(() => setNotification(null), 5000);

            // Return reservation data for SeatSelection component
            return reservation;
        } catch (err) {
            console.error("Error creating hold:", err);
            throw err; // Re-throw to let SeatSelection handle error display
        } finally {
            setHoldLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-background-light dark:bg-background-dark">
            {/* Header with Back Button */}
            <header className="sticky top-0 z-40 bg-card-light dark:bg-card-dark border-b border-border-light dark:border-border-dark">
                <div className="max-w-7xl mx-auto px-4 py-4 flex items-center gap-4">
                    <button
                        onClick={() => navigate("/concerts")}
                        className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                        aria-label="Zurück zur Übersicht"
                    >
                        <span className="material-symbols-outlined text-text-primary dark:text-white">
                            arrow_back
                        </span>
                    </button>
                    <span className="text-lg font-semibold text-text-primary dark:text-white">
                        Konzertdetails
                    </span>
                </div>
            </header>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto px-4 py-6">
                {/* Breadcrumb */}
                <Breadcrumb concertName={concert?.name} />

                {/* Notification Toast */}
                {notification && (
                    <div
                        className={`fixed top-20 right-4 z-50 max-w-md p-4 rounded-lg shadow-lg flex items-start gap-3 animate-slide-in ${
                            notification.type === "success"
                                ? "bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100"
                                : "bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100"
                        }`}
                    >
                        <span className="material-symbols-outlined">
                            {notification.type === "success"
                                ? "check_circle"
                                : "error"}
                        </span>
                        <p className="flex-1">{notification.message}</p>
                        <button
                            onClick={() => setNotification(null)}
                            className="hover:opacity-70"
                        >
                            <span className="material-symbols-outlined">
                                close
                            </span>
                        </button>
                    </div>
                )}

                {/* Loading State */}
                {loading && <LoadingSkeleton />}

                {/* Error State */}
                {!loading && error && (
                    <ErrorState error={error} onRetry={refresh} />
                )}

                {/* Concert Content */}
                {!loading && !error && concert && (
                    <>
                        {/* Concert Header */}
                        <ConcertHeader concert={concert} />

                        {/* Main Content Grid */}
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                            {/* Seat Overview (2 columns) */}
                            <div className="lg:col-span-2">
                                <div className="flex items-center justify-between mb-6">
                                    <h2 className="text-2xl font-bold text-text-primary dark:text-white flex items-center gap-2">
                                        <span className="material-symbols-outlined text-primary">
                                            event_seat
                                        </span>
                                        Sitzplatz wählen
                                    </h2>
                                    <ConnectionStatusBadge
                                        status={connectionStatus}
                                        onReconnect={sseReconnect}
                                    />
                                </div>
                                <SeatOverview
                                    seats={seats}
                                    availabilityByCategory={
                                        availabilityByCategory
                                    }
                                    selectedSeat={selectedSeat}
                                    onSeatSelect={handleSeatSelect}
                                    loading={loading}
                                    error={error}
                                    onRetry={refresh}
                                />
                            </div>

                            {/* Concert Info Sidebar (1 column) */}
                            <div className="space-y-6">
                                <ConcertInfoCard 
                                    concert={concert} 
                                    minPrice={minPrice} 
                                    maxPrice={maxPrice}
                                    availableSeats={availableSeatsCount}
                                    totalSeats={totalSeatsCount}
                                />

                                {/* Quick Actions */}
                                <div className="bg-card-light dark:bg-card-dark rounded-xl p-6 shadow-card border border-border-light dark:border-border-dark">
                                    <h3 className="text-lg font-bold text-text-primary dark:text-white mb-4">
                                        Schnellauswahl
                                    </h3>
                                    <p className="text-sm text-text-secondary dark:text-gray-400 mb-4">
                                        Klicken Sie auf einen blauen Sitzplatz
                                        im Saalplan, um ihn zu reservieren.
                                    </p>
                                    <button
                                        onClick={refresh}
                                        className="w-full py-3 px-4 rounded-lg border border-border-light dark:border-border-dark text-text-primary dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors flex items-center justify-center gap-2"
                                    >
                                        <span className="material-symbols-outlined">
                                            refresh
                                        </span>
                                        Verfügbarkeit aktualisieren
                                    </button>
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </main>

            {/* Seat Selection Dialog with Countdown */}
            <SeatSelection
                seat={selectedSeat}
                concert={concert}
                onClose={() => {
                    clearSeatSelection();
                    // Refresh seats to get current status after closing dialog
                    refresh();
                }}
                onConfirm={handleHoldConfirm}
                isLoading={holdLoading}
                ttlSeconds={600} // 10 minutes default TTL
            />
        </div>
    );
};

export default ConcertDetailPage;
