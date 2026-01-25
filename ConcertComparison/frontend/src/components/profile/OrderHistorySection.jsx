import React, { useState, useMemo } from 'react';
import PropTypes from 'prop-types';
import OrderCard from './OrderCard';
import TicketDetailModal from './TicketDetailModal';
import useUserOrders from '../../hooks/useUserOrders';

/**
 * OrderHistorySection Component
 * 
 * Displays user's order history with tab navigation and filters.
 * Design based on Google Stitch - Order History (Ordner 11).
 * 
 * @returns {React.ReactElement}
 */
const OrderHistorySection = () => {
  const { orders, loading, error, getFilteredOrders, getOrderCounts, downloadTicket } = useUserOrders();
  const [activeTab, setActiveTab] = useState('all');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Get filtered orders based on active tab
  const filteredOrders = useMemo(
    () => getFilteredOrders(activeTab),
    [getFilteredOrders, activeTab]
  );

  // Get counts for each tab
  const counts = useMemo(() => getOrderCounts(), [getOrderCounts]);

  /**
   * Handle tab change
   */
  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  /**
   * Handle view details button click
   */
  const handleViewDetails = (order) => {
    setSelectedOrder(order);
    setIsModalOpen(true);
  };

  /**
   * Handle close modal
   */
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedOrder(null);
  };

  /**
   * Handle QR code download
   */
  const handleDownloadQR = async (orderId, concertName) => {
    try {
      await downloadTicket(orderId, concertName);
    } catch (error) {
      console.error('Error downloading QR code:', error);
      // TODO: Show error toast/notification
    }
  };

  // Loading State
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-16 space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        <p className="text-text-secondary dark:text-gray-400">
          Bestellungen werden geladen...
        </p>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-16 space-y-4">
        <span className="material-symbols-outlined text-red-500 text-6xl">
          error
        </span>
        <p className="text-lg font-semibold text-text-primary dark:text-white">
          Fehler beim Laden der Bestellungen
        </p>
        <p className="text-sm text-text-secondary dark:text-gray-400">
          {error}
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-8 pb-12">
      {/* Page Heading */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl md:text-4xl font-black tracking-tight text-text-primary dark:text-white">
            Meine Bestellungen
          </h1>
          <p className="text-text-secondary dark:text-gray-400 text-base">
            Verwalten Sie Ihre Tickets und Rechnungen an einem Ort.
          </p>
        </div>
        {/* Download All Invoices Button (optional) */}
        {orders.length > 0 && (
          <button className="hidden sm:flex items-center gap-2 text-sm font-semibold text-primary hover:text-primary-dark transition-colors">
            <span className="material-symbols-outlined text-lg">download</span>
            Alle Rechnungen herunterladen
          </button>
        )}
      </div>

      {/* Tabs */}
      <div className="border-b border-border-light dark:border-border-dark">
        <div className="flex gap-8">
          {/* All Tab */}
          <button
            onClick={() => handleTabChange('all')}
            className={`
              flex items-center gap-2 border-b-[3px] pb-3 px-1 transition-colors
              ${activeTab === 'all'
                ? 'border-primary text-primary dark:text-blue-400'
                : 'border-transparent text-text-secondary hover:text-text-primary dark:text-gray-400 dark:hover:text-gray-200'
              }
            `}
          >
            <span className="text-sm font-bold tracking-wide">Alle</span>
            <span className={`
              px-2 py-0.5 rounded-full text-xs font-bold
              ${activeTab === 'all'
                ? 'bg-primary/10 text-primary dark:bg-blue-400/20 dark:text-blue-300'
                : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300'
              }
            `}>
              {counts.all}
            </span>
          </button>

          {/* Upcoming Tab */}
          <button
            onClick={() => handleTabChange('upcoming')}
            className={`
              flex items-center gap-2 border-b-[3px] pb-3 px-1 transition-colors
              ${activeTab === 'upcoming'
                ? 'border-primary text-primary dark:text-blue-400'
                : 'border-transparent text-text-secondary hover:text-text-primary dark:text-gray-400 dark:hover:text-gray-200'
              }
            `}
          >
            <span className="text-sm font-bold tracking-wide">Bevorstehend</span>
            <span className={`
              px-2 py-0.5 rounded-full text-xs font-bold
              ${activeTab === 'upcoming'
                ? 'bg-primary/10 text-primary dark:bg-blue-400/20 dark:text-blue-300'
                : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300'
              }
            `}>
              {counts.upcoming}
            </span>
          </button>

          {/* Past Tab */}
          <button
            onClick={() => handleTabChange('past')}
            className={`
              flex items-center gap-2 border-b-[3px] pb-3 px-1 transition-colors
              ${activeTab === 'past'
                ? 'border-primary text-primary dark:text-blue-400'
                : 'border-transparent text-text-secondary hover:text-text-primary dark:text-gray-400 dark:hover:text-gray-200'
              }
            `}
          >
            <span className="text-sm font-bold tracking-wide">Vergangen</span>
          </button>
        </div>
      </div>

      {/* Order List */}
      {filteredOrders.length === 0 ? (
        // Empty State
        <div className="flex flex-col items-center justify-center py-16 space-y-4">
          <span className="material-symbols-outlined text-gray-300 dark:text-gray-600 text-8xl">
            receipt_long
          </span>
          <p className="text-xl font-semibold text-text-primary dark:text-white">
            Keine Bestellungen gefunden
          </p>
          <p className="text-sm text-text-secondary dark:text-gray-400 max-w-md text-center">
            {activeTab === 'upcoming' && 'Sie haben keine bevorstehenden Konzerte.'}
            {activeTab === 'past' && 'Sie haben keine vergangenen Konzerte.'}
            {activeTab === 'all' && 'Sie haben noch keine Tickets gekauft. Durchsuchen Sie unsere Events und sichern Sie sich Ihre Tickets!'}
          </p>
          {activeTab === 'all' && (
            <a
              href="/concerts"
              className="mt-4 inline-flex items-center gap-2 px-6 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-dark transition-colors"
            >
              <span>Konzerte entdecken</span>
              <span className="material-symbols-outlined">arrow_forward</span>
            </a>
          )}
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {filteredOrders.map((order) => (
            <OrderCard
              key={order.orderId}
              order={order}
              onViewDetails={handleViewDetails}
              onDownloadQR={handleDownloadQR}
            />
          ))}
        </div>
      )}

      {/* Load More Button (optional for pagination) */}
      {/* 
      {filteredOrders.length > 0 && hasMore && (
        <div className="flex justify-center pt-8">
          <button className="flex items-center gap-2 px-6 py-2.5 rounded-lg border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark text-text-secondary dark:text-gray-300 text-sm font-semibold hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
            <span className="material-symbols-outlined text-lg">autorenew</span>
            Mehr laden
          </button>
        </div>
      )}
      */}

      {/* Ticket Detail Modal */}
      {selectedOrder && (
        <TicketDetailModal
          order={selectedOrder}
          isOpen={isModalOpen}
          onClose={handleCloseModal}
        />
      )}
    </div>
  );
};

OrderHistorySection.propTypes = {};

export default OrderHistorySection;
