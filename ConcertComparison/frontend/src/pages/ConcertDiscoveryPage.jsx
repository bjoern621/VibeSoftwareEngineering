import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useConcerts } from '../hooks/useConcerts';
import { useDebounce } from '../hooks/useDebounce';
import { useAuth } from '../context/AuthContext';
import SearchBar from '../components/common/SearchBar';
import FilterBar from '../components/common/FilterBar';
import ConcertCard from '../components/concerts/ConcertCard';
import Pagination from '../components/pagination/Pagination';

/**
 * Concert Discovery Page
 * Main page for browsing and filtering concerts
 */
const ConcertDiscoveryPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
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
    <div className="min-h-screen bg-background-light">
      {/* Header */}
      <header className="sticky top-0 z-20 bg-white shadow-md">
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

            {/* User Actions */}
            <div className="flex items-center gap-2">
              {!isAuthenticated ? (
                <>
                  <Link to="/login">
                    <button className="px-4 py-2 rounded-lg text-text-primary hover:bg-gray-100 transition-colors font-medium text-sm">
                      Anmelden
                    </button>
                  </Link>
                  <Link to="/register">
                    <button className="px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90 transition-colors font-medium text-sm">
                      Registrieren
                    </button>
                  </Link>
                </>
              ) : (
                <>
                  <button className="p-2 rounded-lg hover:bg-gray-100 transition-colors">
                    <span className="material-symbols-outlined text-text-primary">
                      notifications
                    </span>
                  </button>
                  <div className="relative group">
                    <button className="flex items-center gap-2 p-2 rounded-lg hover:bg-gray-100 transition-colors">
                      <span className="material-symbols-outlined text-text-primary">
                        account_circle
                      </span>
                      <span className="text-sm font-medium text-text-primary hidden md:inline">
                        {user?.firstName || 'User'}
                      </span>
                    </button>
                    {/* Dropdown Menu */}
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-[100]">
                      <div className="py-2">
                        <div className="px-4 py-2 border-b border-gray-100">
                          <p className="text-sm font-semibold text-text-primary">
                            {user?.firstName} {user?.lastName}
                          </p>
                          <p className="text-xs text-text-secondary">{user?.email}</p>
                        </div>
                        <Link
                          to="/profile"
                          className="w-full text-left px-4 py-2 text-sm text-text-primary hover:bg-gray-50 transition-colors flex items-center gap-2"
                        >
                          <span className="material-symbols-outlined text-lg">person</span>
                          Mein Profil
                        </Link>
                        <button
                          onClick={() => {
                            logout();
                            navigate('/concerts');
                          }}
                          className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors flex items-center gap-2"
                        >
                          <span className="material-symbols-outlined text-lg">logout</span>
                          Abmelden
                        </button>
                      </div>
                    </div>
                  </div>
                </>
              )}
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
