import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import AdminLayout from '../components/admin/AdminLayout';
import adminService from '../services/adminService';
import { fetchConcertById } from '../services/concertService';

/**
 * CreateConcertPage - Seite zum Erstellen/Bearbeiten von Konzerten
 */
const CreateConcertPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [loadingConcert, setLoadingConcert] = useState(isEditMode);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    date: '',
    time: '',
    venue: '',
    description: '',
    imageUrl: '',
  });

  const [errors, setErrors] = useState({});

  // Lade Konzertdaten im Bearbeitungsmodus
  useEffect(() => {
    if (isEditMode) {
      loadConcert();
    }
  }, [id]);

  const loadConcert = async () => {
    try {
      setLoadingConcert(true);
      const concert = await fetchConcertById(id);
      const dateObj = new Date(concert.date);
      setFormData({
        name: concert.name || '',
        date: dateObj.toISOString().split('T')[0],
        time: dateObj.toTimeString().slice(0, 5),
        venue: concert.venue || '',
        description: concert.description || '',
        imageUrl: concert.imageUrl || '',
      });
    } catch (err) {
      console.error('Fehler beim Laden des Konzerts:', err);
      setError('Konzert konnte nicht geladen werden.');
    } finally {
      setLoadingConcert(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Name ist erforderlich';
    } else if (formData.name.length < 3) {
      newErrors.name = 'Name muss mindestens 3 Zeichen lang sein';
    }

    if (!formData.date) {
      newErrors.date = 'Datum ist erforderlich';
    } else {
      const selectedDate = new Date(formData.date);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      if (selectedDate < today) {
        newErrors.date = 'Datum muss in der Zukunft liegen';
      }
    }

    if (!formData.time) {
      newErrors.time = 'Uhrzeit ist erforderlich';
    }

    if (!formData.venue.trim()) {
      newErrors.venue = 'Veranstaltungsort ist erforderlich';
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Beschreibung ist erforderlich';
    } else if (formData.description.length < 10) {
      newErrors.description = 'Beschreibung muss mindestens 10 Zeichen lang sein';
    }

    if (formData.imageUrl && !isValidUrl(formData.imageUrl)) {
      newErrors.imageUrl = 'Ungültige URL';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const isValidUrl = (string) => {
    try {
      new URL(string);
      return true;
    } catch (_) {
      return false;
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Fehler für dieses Feld zurücksetzen
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Datum und Uhrzeit kombinieren
      const dateTime = new Date(`${formData.date}T${formData.time}`);

      const eventData = {
        name: formData.name.trim(),
        date: dateTime.toISOString(),
        venue: formData.venue.trim(),
        description: formData.description.trim(),
        imageUrl: formData.imageUrl.trim() || null,
      };

      if (isEditMode) {
        await adminService.updateEvent(id, eventData);
      } else {
        await adminService.createEvent(eventData);
      }

      setSuccess(true);
      
      // Nach 2 Sekunden zurück zum Dashboard
      setTimeout(() => {
        navigate('/admin');
      }, 2000);
    } catch (err) {
      console.error('Fehler beim Speichern des Konzerts:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Konzert konnte nicht gespeichert werden. Bitte versuchen Sie es erneut.');
      }
    } finally {
      setLoading(false);
    }
  };

  if (loadingConcert) {
    return (
      <AdminLayout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <button
            onClick={() => navigate('/admin')}
            className="flex items-center text-text-secondary hover:text-text-primary mb-4"
          >
            <span className="material-symbols-outlined mr-1">arrow_back</span>
            Zurück zum Dashboard
          </button>
          <h1 className="text-2xl font-bold text-text-primary">
            {isEditMode ? 'Konzert bearbeiten' : 'Neues Konzert erstellen'}
          </h1>
          <p className="text-text-secondary mt-1">
            {isEditMode
              ? 'Aktualisieren Sie die Konzertdetails'
              : 'Füllen Sie das Formular aus, um ein neues Konzert anzulegen'}
          </p>
        </div>

        {/* Success Message */}
        {success && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6 flex items-center">
            <span className="material-symbols-outlined text-green-600 mr-3">
              check_circle
            </span>
            <div>
              <p className="text-green-700 font-medium">
                Konzert wurde erfolgreich {isEditMode ? 'aktualisiert' : 'erstellt'}!
              </p>
              <p className="text-green-600 text-sm">
                Sie werden zum Dashboard weitergeleitet...
              </p>
            </div>
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6 flex items-center">
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

        {/* Form */}
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-lg shadow-sm border border-border-light p-6 space-y-6"
        >
          {/* Name */}
          <div>
            <label
              htmlFor="name"
              className="block text-sm font-medium text-text-primary mb-2"
            >
              Konzertname *
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="z.B. Taylor Swift - Eras Tour"
              className={`w-full px-4 py-3 rounded-lg border ${
                errors.name
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-border-light focus:ring-primary'
              } focus:outline-none focus:ring-2`}
              disabled={loading}
            />
            {errors.name && (
              <p className="mt-1 text-sm text-red-600">{errors.name}</p>
            )}
          </div>

          {/* Date and Time */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label
                htmlFor="date"
                className="block text-sm font-medium text-text-primary mb-2"
              >
                Datum *
              </label>
              <input
                type="date"
                id="date"
                name="date"
                value={formData.date}
                onChange={handleChange}
                min={new Date().toISOString().split('T')[0]}
                className={`w-full px-4 py-3 rounded-lg border ${
                  errors.date
                    ? 'border-red-500 focus:ring-red-500'
                    : 'border-border-light focus:ring-primary'
                } focus:outline-none focus:ring-2`}
                disabled={loading}
              />
              {errors.date && (
                <p className="mt-1 text-sm text-red-600">{errors.date}</p>
              )}
            </div>
            <div>
              <label
                htmlFor="time"
                className="block text-sm font-medium text-text-primary mb-2"
              >
                Uhrzeit *
              </label>
              <input
                type="time"
                id="time"
                name="time"
                value={formData.time}
                onChange={handleChange}
                className={`w-full px-4 py-3 rounded-lg border ${
                  errors.time
                    ? 'border-red-500 focus:ring-red-500'
                    : 'border-border-light focus:ring-primary'
                } focus:outline-none focus:ring-2`}
                disabled={loading}
              />
              {errors.time && (
                <p className="mt-1 text-sm text-red-600">{errors.time}</p>
              )}
            </div>
          </div>

          {/* Venue */}
          <div>
            <label
              htmlFor="venue"
              className="block text-sm font-medium text-text-primary mb-2"
            >
              Veranstaltungsort *
            </label>
            <input
              type="text"
              id="venue"
              name="venue"
              value={formData.venue}
              onChange={handleChange}
              placeholder="z.B. Olympiastadion Berlin"
              className={`w-full px-4 py-3 rounded-lg border ${
                errors.venue
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-border-light focus:ring-primary'
              } focus:outline-none focus:ring-2`}
              disabled={loading}
            />
            {errors.venue && (
              <p className="mt-1 text-sm text-red-600">{errors.venue}</p>
            )}
          </div>

          {/* Description */}
          <div>
            <label
              htmlFor="description"
              className="block text-sm font-medium text-text-primary mb-2"
            >
              Beschreibung *
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={4}
              placeholder="Beschreiben Sie das Konzert..."
              className={`w-full px-4 py-3 rounded-lg border ${
                errors.description
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-border-light focus:ring-primary'
              } focus:outline-none focus:ring-2 resize-none`}
              disabled={loading}
            />
            {errors.description && (
              <p className="mt-1 text-sm text-red-600">{errors.description}</p>
            )}
          </div>

          {/* Image URL */}
          <div>
            <label
              htmlFor="imageUrl"
              className="block text-sm font-medium text-text-primary mb-2"
            >
              Bild-URL (optional)
            </label>
            <input
              type="url"
              id="imageUrl"
              name="imageUrl"
              value={formData.imageUrl}
              onChange={handleChange}
              placeholder="https://example.com/image.jpg"
              className={`w-full px-4 py-3 rounded-lg border ${
                errors.imageUrl
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-border-light focus:ring-primary'
              } focus:outline-none focus:ring-2`}
              disabled={loading}
            />
            {errors.imageUrl && (
              <p className="mt-1 text-sm text-red-600">{errors.imageUrl}</p>
            )}
            {formData.imageUrl && !errors.imageUrl && (
              <div className="mt-2">
                <img
                  src={formData.imageUrl}
                  alt="Vorschau"
                  className="h-32 w-auto rounded-lg object-cover"
                  onError={(e) => {
                    e.target.style.display = 'none';
                  }}
                />
              </div>
            )}
          </div>

          {/* Submit Buttons */}
          <div className="flex items-center justify-end space-x-4 pt-4 border-t border-border-light">
            <button
              type="button"
              onClick={() => navigate('/admin')}
              className="px-6 py-2 text-text-secondary hover:text-text-primary transition-colors"
              disabled={loading}
            >
              Abbrechen
            </button>
            <button
              type="submit"
              disabled={loading || success}
              className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Speichern...
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined mr-2">save</span>
                  {isEditMode ? 'Aktualisieren' : 'Erstellen'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </AdminLayout>
  );
};

export default CreateConcertPage;
