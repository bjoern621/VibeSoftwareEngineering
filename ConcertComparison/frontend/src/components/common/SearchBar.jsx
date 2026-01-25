import React from 'react';

/**
 * SearchBar Component
 * Debounced search input for concerts
 */
const SearchBar = ({ value, onChange, placeholder = 'Suche nach Konzerten...' }) => {
  return (
    <div className="relative flex-1">
      <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary dark:text-gray-400">
        search
      </span>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full pl-12 pr-4 py-3 rounded-lg bg-white dark:bg-card-dark border border-border-light dark:border-border-dark text-text-primary dark:text-white placeholder-text-secondary dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
      />
      {value && (
        <button
          onClick={() => onChange('')}
          className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full transition-colors"
          aria-label="Clear search"
        >
          <span className="material-symbols-outlined text-text-secondary dark:text-gray-400 text-xl">
            close
          </span>
        </button>
      )}
    </div>
  );
};

export default SearchBar;
