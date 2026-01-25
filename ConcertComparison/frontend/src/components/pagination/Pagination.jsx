import React from 'react';

/**
 * Pagination Component
 * Navigate between concert pages
 */
const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  const pages = [];
  const maxVisiblePages = 5;

  // Calculate visible page range
  let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
  let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

  // Adjust if we're near the end
  if (endPage - startPage < maxVisiblePages - 1) {
    startPage = Math.max(0, endPage - maxVisiblePages + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }

  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-2 mt-8">
      {/* Previous Button */}
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        className={`px-4 py-2 rounded-lg font-medium transition-all ${
          currentPage === 0
            ? 'bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed'
            : 'bg-white dark:bg-card-dark text-text-primary dark:text-white border border-border-light dark:border-border-dark hover:bg-primary hover:text-white hover:border-primary'
        }`}
      >
        <span className="material-symbols-outlined">chevron_left</span>
      </button>

      {/* First Page */}
      {startPage > 0 && (
        <>
          <button
            onClick={() => onPageChange(0)}
            className="px-4 py-2 rounded-lg font-medium bg-white dark:bg-card-dark text-text-primary dark:text-white border border-border-light dark:border-border-dark hover:bg-primary hover:text-white hover:border-primary transition-all"
          >
            1
          </button>
          {startPage > 1 && <span className="px-2 text-text-secondary">...</span>}
        </>
      )}

      {/* Page Numbers */}
      {pages.map((page) => (
        <button
          key={page}
          onClick={() => onPageChange(page)}
          className={`px-4 py-2 rounded-lg font-medium transition-all ${
            page === currentPage
              ? 'bg-primary text-white shadow-md'
              : 'bg-white dark:bg-card-dark text-text-primary dark:text-white border border-border-light dark:border-border-dark hover:bg-primary hover:text-white hover:border-primary'
          }`}
        >
          {page + 1}
        </button>
      ))}

      {/* Last Page */}
      {endPage < totalPages - 1 && (
        <>
          {endPage < totalPages - 2 && <span className="px-2 text-text-secondary">...</span>}
          <button
            onClick={() => onPageChange(totalPages - 1)}
            className="px-4 py-2 rounded-lg font-medium bg-white dark:bg-card-dark text-text-primary dark:text-white border border-border-light dark:border-border-dark hover:bg-primary hover:text-white hover:border-primary transition-all"
          >
            {totalPages}
          </button>
        </>
      )}

      {/* Next Button */}
      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
        className={`px-4 py-2 rounded-lg font-medium transition-all ${
          currentPage === totalPages - 1
            ? 'bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed'
            : 'bg-white dark:bg-card-dark text-text-primary dark:text-white border border-border-light dark:border-border-dark hover:bg-primary hover:text-white hover:border-primary'
        }`}
      >
        <span className="material-symbols-outlined">chevron_right</span>
      </button>
    </div>
  );
};

export default Pagination;
