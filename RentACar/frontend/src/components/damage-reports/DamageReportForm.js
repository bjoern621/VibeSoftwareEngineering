import React, { useState, useCallback } from 'react';
import {
  createDamageReport,
  fileToBase64,
  validateDamageReport,
} from '../../services/damageReportService';

/**
 * DamageReportForm - Formular zum Erstellen von Schadensberichten
 *
 * Features:
 * - Beschreibung (Textarea)
 * - Geschätzte Kosten
 * - Foto-Upload mit Vorschau (multiple)
 * - Validierung
 */
const DamageReportForm = ({ bookingId, onSuccess, onError }) => {
  const [description, setDescription] = useState('');
  const [estimatedCost, setEstimatedCost] = useState('');
  const [photos, setPhotos] = useState([]);
  const [photoFiles, setPhotoFiles] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [validationErrors, setValidationErrors] = useState([]);

  const handlePhotoUpload = useCallback(async (event) => {
    const files = Array.from(event.target.files);
    const newPhotos = [];
    const newPhotoFiles = [];

    for (const file of files) {
      if (file.type.startsWith('image/')) {
        try {
          const base64 = await fileToBase64(file);
          newPhotos.push(base64);
          newPhotoFiles.push({
            name: file.name,
            preview: URL.createObjectURL(file),
            base64,
          });
        } catch (error) {
          console.error('Fehler beim Hochladen des Fotos:', error);
        }
      }
    }

    setPhotos((prev) => [...prev, ...newPhotos]);
    setPhotoFiles((prev) => [...prev, ...newPhotoFiles]);
  }, []);

  const handleRemovePhoto = useCallback((index) => {
    setPhotos((prev) => prev.filter((_, i) => i !== index));
    setPhotoFiles((prev) => {
      URL.revokeObjectURL(prev[index].preview);
      return prev.filter((_, i) => i !== index);
    });
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setValidationErrors([]);

    const damageData = {
      description,
      estimatedCost: estimatedCost ? parseFloat(estimatedCost) : 0,
      photos,
    };

    const validation = validateDamageReport(damageData);
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      return;
    }

    setIsSubmitting(true);

    try {
      const result = await createDamageReport(bookingId, damageData);
      setDescription('');
      setEstimatedCost('');
      setPhotos([]);
      setPhotoFiles([]);
      if (onSuccess) {
        onSuccess(result);
      }
    } catch (error) {
      if (onError) {
        onError(error.message);
      }
      setValidationErrors([error.message]);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDrop = useCallback(
    async (event) => {
      event.preventDefault();
      const files = Array.from(event.dataTransfer.files);
      const imageFiles = files.filter((file) => file.type.startsWith('image/'));

      if (imageFiles.length > 0) {
        const fakeEvent = {
          target: { files: imageFiles },
        };
        await handlePhotoUpload(fakeEvent);
      }
    },
    [handlePhotoUpload]
  );

  const handleDragOver = (event) => {
    event.preventDefault();
  };

  return (
    <div>
      <h2 className="text-gray-900 text-[22px] font-bold mb-3">Schadensprotokoll</h2>
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
        {validationErrors.length > 0 && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <ul className="list-disc list-inside text-sm text-red-600">
              {validationErrors.map((error, index) => (
                <li key={index}>{error}</li>
              ))}
            </ul>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="flex flex-col lg:flex-row gap-6">
            <div className="relative flex-shrink-0">
              <img
                alt="Vehicle schematic for damage marking"
                className="w-64 h-auto mx-auto"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuA2D0T4enaMVlqwYln9K_udeBlRJCnBLV5PvcrEYOO6nDB679eqQkG2ftvwdjBqx6nDEdluG1QPaBA5pGjdU0C4pf7KRnctuVaIHLaNnmtmkqClTANi7Zc2XBK2PjWrWmPb_JHCiGfFDtVL53-8JSF6ISxITRMR9hAtWVBJr8PFOdmae9-jnwIh6x4L69g6jv4-mIzJYoJOnzIB3_eMSFZa9yoI86udMljf5VNJinqHDYldEW_Ko2iZGKPIijE9Szo__W5b3oKmHu_9"
              />
            </div>

            <div className="flex-1">
              <h3 className="font-bold mb-2 text-gray-900">Neuen Schaden hinzufügen</h3>
              <p className="text-sm text-gray-500 mb-4">
                Beschreiben Sie den Schaden und laden Sie Fotos hoch.
              </p>

              <div className="mb-4">
                <label
                  className="block text-sm font-medium text-gray-700 mb-1"
                  htmlFor="description"
                >
                  Beschreibung *
                </label>
                <textarea
                  id="description"
                  className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                  placeholder="Beschreibung des Schadens..."
                  rows="3"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                />
              </div>

              <div className="mb-4">
                <label
                  className="block text-sm font-medium text-gray-700 mb-1"
                  htmlFor="estimatedCost"
                >
                  Geschätzte Kosten (EUR)
                </label>
                <input
                  id="estimatedCost"
                  type="number"
                  min="0"
                  step="0.01"
                  className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                  placeholder="0.00"
                  value={estimatedCost}
                  onChange={(e) => setEstimatedCost(e.target.value)}
                />
              </div>

              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">Fotos</label>
                <div
                  className="flex items-center justify-center w-full px-4 py-3 border-2 border-dashed rounded-lg border-gray-300 text-center cursor-pointer hover:bg-gray-50"
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                >
                  <label className="w-full cursor-pointer">
                    <div className="text-sm text-gray-600">
                      <span className="material-symbols-outlined text-3xl text-primary">
                        upload_file
                      </span>
                      <p>
                        <span className="font-semibold">Fotos hochladen</span> oder hierher ziehen
                      </p>
                      <p className="text-xs text-gray-400 mt-1">PNG, JPG, GIF bis 10MB</p>
                    </div>
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      className="hidden"
                      onChange={handlePhotoUpload}
                    />
                  </label>
                </div>
              </div>

              {photoFiles.length > 0 && (
                <div className="mb-4">
                  <p className="text-sm font-medium text-gray-700 mb-2">
                    Hochgeladene Fotos ({photoFiles.length})
                  </p>
                  <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
                    {photoFiles.map((photo, index) => (
                      <div key={index} className="relative group">
                        <img
                          src={photo.preview}
                          alt={`Schaden ${index + 1}`}
                          className="w-full h-20 object-cover rounded-lg border border-gray-200"
                        />
                        <button
                          type="button"
                          onClick={() => handleRemovePhoto(index)}
                          className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full text-xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                        >
                          X
                        </button>
                        <p className="text-xs text-gray-500 truncate mt-1">{photo.name}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={isSubmitting || !bookingId}
                className="w-full flex items-center justify-center rounded-lg h-10 px-4 bg-red-600 text-white text-sm font-bold hover:bg-red-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                {isSubmitting ? (
                  <>
                    <span className="animate-spin mr-2">...</span>
                    Wird gespeichert...
                  </>
                ) : (
                  'Schadensbericht erstellen'
                )}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DamageReportForm;
