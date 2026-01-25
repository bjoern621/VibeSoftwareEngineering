import React, { useState, useRef, useEffect } from 'react';
import { FILTER_OPTIONS, SORT_OPTIONS } from '../../constants/concertFilters';

/**
 * FilterBar Component
 * Horizontal scrollable filter pills and sort dropdown
 */
const FilterBar = ({ activeFilter, onFilterChange, activeSort, onSortChange }) => {
  const [isSortOpen, setIsSortOpen] = useState(false);
  const sortRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (sortRef.current && !sortRef.current.contains(event.target)) {
        setIsSortOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const activeSortOption = SORT_OPTIONS.find((opt) => opt.id === activeSort) || SORT_OPTIONS[0];

  return (
    <div className="sticky top-0 z-10 bg-white dark:bg-background-dark border-b border-gray-200 dark:border-border-dark py-4">
      <div className="flex items-center gap-4">
        {/* Filter Pills */}
        <div className="flex-1 overflow-x-auto scrollbar-hide">
          <div className="flex items-center gap-2 min-w-max">
            {FILTER_OPTIONS.map((filter) => (
              <button
                key={filter.id}
                onClick={() => onFilterChange(filter.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded-full whitespace-nowrap transition-all ${
                  activeFilter === filter.id
                    ? 'bg-primary text-white shadow-md'
                    : 'bg-white dark:bg-card-dark text-text-secondary dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 border border-border-light dark:border-border-dark'
                }`}
              >
                <span className="material-symbols-outlined text-lg">
                  {filter.icon}
                </span>
                <span className="text-sm font-medium">{filter.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Sort Dropdown */}
        <div className="relative flex-shrink-0" ref={sortRef}>
          <button
            onClick={() => setIsSortOpen(!isSortOpen)}
            className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-card-dark border border-border-light dark:border-border-dark rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors text-text-primary dark:text-white"
          >
            <span className="material-symbols-outlined text-lg">
              {activeSortOption.icon}
            </span>
            <span className="text-sm font-medium hidden sm:inline">
              {activeSortOption.label}
            </span>
            <span className={`material-symbols-outlined text-lg transition-transform ${isSortOpen ? 'rotate-180' : ''}`}>
              expand_more
            </span>
          </button>

          {/* Dropdown Menu */}
          {isSortOpen && (
            <div className="absolute right-0 mt-2 w-56 bg-white dark:bg-card-dark border border-border-light dark:border-border-dark rounded-lg shadow-lg overflow-hidden">
              {SORT_OPTIONS.map((option) => (
                <button
                  key={option.id}
                  onClick={() => {
                    onSortChange(option.id);
                    setIsSortOpen(false);
                  }}
                  className={`w-full flex items-center gap-3 px-4 py-3 text-left transition-colors ${
                    activeSort === option.id
                      ? 'bg-primary/10 text-primary'
                      : 'text-text-primary dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700'
                  }`}
                >
                  <span className="material-symbols-outlined text-lg">
                    {option.icon}
                  </span>
                  <span className="text-sm font-medium">{option.label}</span>
                  {activeSort === option.id && (
                    <span className="material-symbols-outlined text-lg ml-auto">
                      check
                    </span>
                  )}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default FilterBar;
