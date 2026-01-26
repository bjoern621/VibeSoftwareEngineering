import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AvailabilityBadge from './AvailabilityBadge';
import AvailabilityProgress from './AvailabilityProgress';
import { formatDateTime, formatTime } from '../../utils/dateFormatter';
import { formatPrice } from '../../utils/priceFormatter';

/**
 * ConcertCard Component
 * Displays a concert card with image, details, and availability
 */
const ConcertCard = ({ concert }) => {
  const navigate = useNavigate();
  const [isLiked, setIsLiked] = useState(false);
  const [imageError, setImageError] = useState(false);

  const handleCardClick = () => {
    navigate(`/concerts/${concert.id}`);
  };

  const handleLikeClick = (e) => {
    e.stopPropagation();
    setIsLiked(!isLiked);
  };

  const isSoldOut = concert.availableSeats === 0;

  // Generiere ein Konzertbild basierend auf der Concert-ID
  // Verwendet picsum.photos für konsistente, schöne Bilder
  const generateConcertImage = (id, name) => {
    // Verwende die ID als Seed für konsistente Bilder pro Konzert
    const seed = id || name?.length || 1;
    return `https://picsum.photos/seed/${seed}/600/400`;
  };

  // Placeholder image if no image URL or error loading
  const imageSrc = imageError 
    ? '/placeholder_concert.svg'
    : concert.imageUrl || generateConcertImage(concert.id, concert.name);

  return (
    <div
      onClick={handleCardClick}
      className={`bg-card-light dark:bg-card-dark rounded-xl overflow-hidden shadow-card hover:shadow-card-hover transition-all duration-300 cursor-pointer group border border-border-light dark:border-border-dark ${
        isSoldOut ? 'opacity-60 grayscale' : 'hover:-translate-y-1'
      }`}
    >
      {/* Image Section */}
      <div className="relative aspect-[3/2] overflow-hidden">
        <img
          src={imageSrc}
          alt={concert.name}
          onError={() => setImageError(true)}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />

        {/* Availability Badge */}
        <AvailabilityBadge
          availableSeats={concert.availableSeats}
          totalSeats={concert.totalSeats}
        />

        {/* Like Button */}
        <button
          onClick={handleLikeClick}
          className={`absolute top-4 right-4 p-2 rounded-full backdrop-blur-sm transition-all ${
            isLiked
              ? 'bg-red-500 text-white'
              : 'bg-white/80 dark:bg-gray-800/80 text-gray-700 dark:text-gray-300'
          } hover:scale-110`}
          aria-label="Like concert"
        >
          <span className={`material-symbols-outlined ${isLiked ? 'fill' : ''}`}>
            favorite
          </span>
        </button>

        {/* Sold Out Overlay */}
        {isSoldOut && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <span className="text-white text-2xl font-bold">AUSVERKAUFT</span>
          </div>
        )}
      </div>

      {/* Content Section */}
      <div className="p-6 space-y-4">
        {/* Concert Title */}
        <h3 className="text-xl font-bold text-text-primary dark:text-white line-clamp-2 group-hover:text-primary transition-colors">
          {concert.name}
        </h3>

        {/* Date & Time */}
        <div className="flex items-center gap-2 text-text-secondary dark:text-gray-400">
          <span className="material-symbols-outlined text-primary">calendar_today</span>
          <span className="text-sm">
            {formatDateTime(concert.date)} • {formatTime(concert.date)} Uhr
          </span>
        </div>

        {/* Venue */}
        <div className="flex items-center gap-2 text-text-secondary dark:text-gray-400">
          <span className="material-symbols-outlined text-primary">location_on</span>
          <span className="text-sm line-clamp-1">{concert.venue}</span>
        </div>

        {/* Price */}
        <div className="pt-2 border-t border-border-light dark:border-border-dark">
          <p className="text-xs text-text-secondary dark:text-gray-400 mb-1">
            Ab
          </p>
          <p className="text-2xl font-bold text-price">
            {formatPrice(concert.minPrice)}
          </p>
        </div>

        {/* Availability Progress */}
        <AvailabilityProgress
          availableSeats={concert.availableSeats}
          totalSeats={concert.totalSeats}
        />

        {/* Buy Button */}
        <button
          disabled={isSoldOut}
          className={`w-full py-3 rounded-lg font-semibold transition-all ${
            isSoldOut
              ? 'bg-gray-300 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed'
              : 'bg-primary hover:bg-primary-dark text-white shadow-sm hover:shadow-md'
          }`}
        >
          {isSoldOut ? 'Ausverkauft' : 'Tickets kaufen'}
        </button>
      </div>
    </div>
  );
};

export default ConcertCard;
