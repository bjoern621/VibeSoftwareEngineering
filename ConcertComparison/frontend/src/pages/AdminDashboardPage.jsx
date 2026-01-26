import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AdminLayout from '../components/admin/AdminLayout';
import adminService from '../services/adminService';
import { fetchConcerts } from '../services/concertService';

/**
 * AdminDashboardPage - Hauptseite des Admin-Bereichs
 * Zeigt eine Übersicht aller Konzerte mit Verwaltungsoptionen
 */
const AdminDashboardPage = () => {
  const navigate = useNavigate();
  const [concerts, setConcerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 10;

  useEffect(() => {
    loadConcerts();
  }, [currentPage]);

  const loadConcerts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await fetchConcerts({
        page: currentPage,
        size: pageSize,
        sortBy: 'date',
        sortOrder: 'desc',
      });
      
      // Handle different API response formats
      let concertsData = [];
      if (Array.isArray(response)) {
        concertsData = response;
      } else if (response.content && Array.isArray(response.content)) {
        concertsData = response.content;
      } else if (response.concerts && Array.isArray(response.concerts)) {
        concertsData = response.concerts;
      }
      
      setConcerts(concertsData);
      setTotalPages(response.totalPages || response.page?.totalPages || 1);
    } catch (err) {
      console.error('Fehler beim Laden der Konzerte:', err);
      setError('Konzerte konnten nicht geladen werden. Bitte versuchen Sie es erneut.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (concertId) => {
    try {
      await adminService.deleteEvent(concertId);
      // Entferne das Konzert sofort aus dem lokalen State
      setConcerts(prevConcerts => prevConcerts.filter(c => c.id !== concertId && c.id !== String(concertId)));
      setSuccessMessage('Konzert wurde erfolgreich gelöscht.');
      setDeleteConfirm(null);
      // Erfolgsmeldung nach 3 Sekunden ausblenden
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Fehler beim Löschen des Konzerts:', err);
      setError('Konzert konnte nicht gelöscht werden. Bitte versuchen Sie es erneut.');
      setDeleteConfirm(null);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadge = (concert) => {
    const eventDate = new Date(concert.date);
    const now = new Date();
    
    if (eventDate < now) {
      return (
        <span className="px-2 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-600">
          Vergangen
        </span>
      );
    }
    
    if (concert.availableSeats === 0) {
      return (
        <span className="px-2 py-1 text-xs font-medium rounded-full bg-red-100 text-red-600">
          Ausverkauft
        </span>
      );
    }
    
    return (
      <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-600">
        Aktiv
      </span>
    );
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-text-primary">Dashboard</h1>
            <p className="text-text-secondary mt-1">
              Übersicht aller Konzerte und Veranstaltungen
            </p>
          </div>
          <Link
            to="/admin/concerts/new"
            className="inline-flex items-center px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
          >
            <span className="material-symbols-outlined mr-2">add</span>
            Neues Konzert
          </Link>
        </div>

        {/* Success Message */}
        {successMessage && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center">
            <span className="material-symbols-outlined text-green-600 mr-3">
              check_circle
            </span>
            <p className="text-green-700">{successMessage}</p>
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center">
            <span className="material-symbols-outlined text-red-600 mr-3">
              error
            </span>
            <p className="text-red-700">{error}</p>
            <button
              onClick={() => setError(null)}
              className="ml-auto text-red-600 hover:text-red-800"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
          </div>
        )}

        {/* Concerts Table */}
        <div className="bg-white rounded-lg shadow-sm border border-border-light overflow-hidden">
          {loading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
              <p className="text-text-secondary mt-4">Lade Konzerte...</p>
            </div>
          ) : concerts.length === 0 ? (
            <div className="p-8 text-center">
              <span className="material-symbols-outlined text-6xl text-gray-300">
                event
              </span>
              <p className="text-text-secondary mt-4">
                Keine Konzerte vorhanden.
              </p>
              <Link
                to="/admin/concerts/new"
                className="inline-flex items-center mt-4 text-primary hover:text-primary-dark"
              >
                <span className="material-symbols-outlined mr-1">add</span>
                Erstes Konzert erstellen
              </Link>
            </div>
          ) : (
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-border-light">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Konzert
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Datum
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Ort
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Plätze
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-text-secondary uppercase tracking-wider">
                    Aktionen
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-light">
                {concerts.map((concert) => (
                  <tr
                    key={concert.id}
                    className="hover:bg-gray-50 transition-colors"
                  >
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        {concert.imageUrl ? (
                          <img
                            src={concert.imageUrl}
                            alt={concert.name}
                            className="h-10 w-10 rounded-lg object-cover mr-3"
                          />
                        ) : (
                          <div className="h-10 w-10 rounded-lg bg-gray-200 flex items-center justify-center mr-3">
                            <span className="material-symbols-outlined text-gray-400">
                              music_note
                            </span>
                          </div>
                        )}
                        <div>
                          <p className="font-medium text-text-primary">
                            {concert.name}
                          </p>
                          <p className="text-sm text-text-secondary truncate max-w-xs">
                            {concert.description}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-text-primary">
                      {formatDate(concert.date)}
                    </td>
                    <td className="px-6 py-4 text-sm text-text-primary">
                      {concert.venue}
                    </td>
                    <td className="px-6 py-4">{getStatusBadge(concert)}</td>
                    <td className="px-6 py-4 text-sm text-text-primary">
                      {concert.availableSeats !== undefined
                        ? `${concert.availableSeats} / ${concert.totalSeats || '?'}`
                        : '-'}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          onClick={() =>
                            navigate(`/admin/concerts/${concert.id}/seats`)
                          }
                          className="p-2 text-gray-500 hover:text-primary hover:bg-gray-100 rounded-lg transition-colors"
                          title="Sitzplätze verwalten"
                        >
                          <span className="material-symbols-outlined">
                            chair
                          </span>
                        </button>
                        <button
                          onClick={() =>
                            navigate(`/admin/concerts/${concert.id}/edit`)
                          }
                          className="p-2 text-gray-500 hover:text-primary hover:bg-gray-100 rounded-lg transition-colors"
                          title="Bearbeiten"
                        >
                          <span className="material-symbols-outlined">
                            edit
                          </span>
                        </button>
                        <button
                          onClick={() => setDeleteConfirm(concert.id)}
                          className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Löschen"
                        >
                          <span className="material-symbols-outlined">
                            delete
                          </span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="px-6 py-4 border-t border-border-light flex items-center justify-between">
              <p className="text-sm text-text-secondary">
                Seite {currentPage + 1} von {totalPages}
              </p>
              <div className="flex space-x-2">
                <button
                  onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
                  disabled={currentPage === 0}
                  className="px-3 py-1 rounded border border-border-light disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  Zurück
                </button>
                <button
                  onClick={() =>
                    setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))
                  }
                  disabled={currentPage >= totalPages - 1}
                  className="px-3 py-1 rounded border border-border-light disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  Weiter
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Delete Confirmation Modal */}
        {deleteConfirm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4 shadow-xl">
              <div className="flex items-center mb-4">
                <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center mr-4">
                  <span className="material-symbols-outlined text-red-600">
                    warning
                  </span>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-text-primary">
                    Konzert löschen?
                  </h3>
                  <p className="text-sm text-text-secondary">
                    Diese Aktion kann nicht rückgängig gemacht werden.
                  </p>
                </div>
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button
                  onClick={() => setDeleteConfirm(null)}
                  className="px-4 py-2 text-text-secondary hover:text-text-primary transition-colors"
                >
                  Abbrechen
                </button>
                <button
                  onClick={() => handleDelete(deleteConfirm)}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                >
                  Löschen
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AdminDashboardPage;
