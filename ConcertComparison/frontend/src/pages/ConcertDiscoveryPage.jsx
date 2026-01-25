import React from 'react';
import { useConcerts } from '../hooks/useConcerts';
import { useDebounce } from '../hooks/useDebounce';
import SearchBar from '../components/common/SearchBar';
import FilterBar from '../components/common/FilterBar';
import ConcertCard from '../components/concerts/ConcertCard';
import Pagination from '../components/pagination/Pagination';

/**
 * Concert Discovery Page
 * Main page for browsing and filtering concerts
 */
const ConcertDiscoveryPage = () => {
  const {
    concerts,
    loading,
    error,
    page,
    totalPages,
    totalElements,
    activeFilter,
    searchQuery,
    activeSort,
    handleFilterChange,
    handleSortChange,
    handleSearchChange,
    handlePageChange,
  } = useConcerts();

  // Debounce search to avoid excessive API calls
  const debouncedSearch = useDebounce(searchQuery, 500);

  React.useEffect(() => {
    handleSearchChange(debouncedSearch);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearch]);

  return (
    <div className="min-h-screen bg-background-light dark:bg-background-dark">
      {/* Header */}
      <header className="sticky top-0 z-20 bg-white dark:bg-card-dark shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center gap-4">
            {/* Logo */}
            <div className="flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-3xl">
                confirmation_number
              </span>
              <h1 className="text-2xl font-bold text-text-primary dark:text-white">
                ConcertFinder
              </h1>
            </div>

            {/* Search Bar */}
            <SearchBar
              value={searchQuery}
              onChange={handleSearchChange}
              placeholder="Suche nach Konzerten, Künstlern, Venues..."
            />

            {/* User Actions (Placeholder for now) */}
            <div className="flex items-center gap-2">
              <button className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                <span className="material-symbols-outlined text-text-primary dark:text-white">
                  notifications
                </span>
              </button>
              <button className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
                <span className="material-symbols-outlined text-text-primary dark:text-white">
                  account_circle
                </span>
              </button>
            </div>
          </div>
        </div>

        {/* Filter Bar */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <FilterBar
            activeFilter={activeFilter}
            onFilterChange={handleFilterChange}
            activeSort={activeSort}
            onSortChange={handleSortChange}
          />
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Results Count */}
        {!loading && !error && (
          <div className="mb-6">
            <p className="text-text-secondary dark:text-gray-400">
              {totalElements} {totalElements === 1 ? 'Konzert' : 'Konzerte'} gefunden
            </p>
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div className="flex items-center justify-center py-20">
            <div className="text-center space-y-4">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-primary border-t-transparent"></div>
              <p className="text-text-secondary dark:text-gray-400">Lade Konzerte...</p>
            </div>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="flex items-center justify-center py-20">
            <div className="text-center space-y-4 max-w-md">
              <span className="material-symbols-outlined text-red-500 text-6xl">error</span>
              <h3 className="text-xl font-semibold text-text-primary dark:text-white">
                Fehler beim Laden
              </h3>
              <p className="text-text-secondary dark:text-gray-400">{error}</p>
              <button
                onClick={() => window.location.reload()}
                className="px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
              >
                Erneut versuchen
              </button>
            </div>
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && concerts.length === 0 && (
          <div className="flex items-center justify-center py-20">
            <div className="text-center space-y-4 max-w-md">
              <span className="material-symbols-outlined text-gray-400 text-6xl">
                search_off
              </span>
              <h3 className="text-xl font-semibold text-text-primary dark:text-white">
                Keine Konzerte gefunden
              </h3>
              <p className="text-text-secondary dark:text-gray-400">
                Versuche andere Filter oder Suchbegriffe
              </p>
            </div>
          </div>
        )}

        {/* Concert Grid */}
        {!loading && !error && concerts.length > 0 && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 lg:gap-8">
              {concerts.map((concert) => (
                <ConcertCard key={concert.id} concert={concert} />
              ))}
            </div>

            {/* Pagination */}
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </>
        )}
      </main>
    </div>
  );
};

export default ConcertDiscoveryPage;
