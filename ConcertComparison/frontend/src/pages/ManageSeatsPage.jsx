import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import AdminLayout from '../components/admin/AdminLayout';
import adminService from '../services/adminService';
import { fetchConcertById } from '../services/concertService';

/**
 * ManageSeatsPage - Seite zum Verwalten von Sitzplätzen für ein Konzert
 * Unterstützt Multi-Block-Generierung mit verschiedenen Kategorien pro Block
 */
const ManageSeatsPage = () => {
  const navigate = useNavigate();
  const { id: concertId } = useParams();

  const [concert, setConcert] = useState(null);
  const [existingSeats, setExistingSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Generator State - Multi-Block-System
  const [generatorMode, setGeneratorMode] = useState('generator'); // 'generator' | 'csv'
  
  // Mehrere Sitzplatz-Blöcke mit verschiedenen Kategorien
  const [seatBlocks, setSeatBlocks] = useState([
    {
      id: 1,
      startRow: 'A',
      endRow: 'B',
      seatsPerRow: 10,
      category: 'VIP',
      price: 150,
    },
    {
      id: 2,
      startRow: 'C',
      endRow: 'E',
      seatsPerRow: 10,
      category: 'PREMIUM',
      price: 100,
    },
    {
      id: 3,
      startRow: 'F',
      endRow: 'J',
      seatsPerRow: 10,
      category: 'STANDARD',
      price: 75,
    },
  ]);

  // Generierte Vorschau
  const [previewSeats, setPreviewSeats] = useState([]);

  // Kategorien mit Farben
  const categories = [
    { value: 'VIP', label: 'VIP', color: 'bg-purple-500', textColor: 'text-purple-700', bgLight: 'bg-purple-100' },
    { value: 'PREMIUM', label: 'Premium', color: 'bg-yellow-500', textColor: 'text-yellow-700', bgLight: 'bg-yellow-100' },
    { value: 'STANDARD', label: 'Standard', color: 'bg-blue-500', textColor: 'text-blue-700', bgLight: 'bg-blue-100' },
    { value: 'ECONOMY', label: 'Economy', color: 'bg-green-500', textColor: 'text-green-700', bgLight: 'bg-green-100' },
  ];

  // Generiere Vorschau wenn sich Blöcke ändern
  const generatePreview = useCallback(() => {
    const seats = [];
    
    seatBlocks.forEach((block) => {
      const startCharCode = block.startRow.toUpperCase().charCodeAt(0);
      const endCharCode = block.endRow.toUpperCase().charCodeAt(0);
      
      for (let rowCode = startCharCode; rowCode <= endCharCode; rowCode++) {
        const rowLetter = String.fromCharCode(rowCode);
        for (let seat = 1; seat <= block.seatsPerRow; seat++) {
          seats.push({
            row: rowLetter,
            number: seat,
            category: block.category,
            price: block.price,
          });
        }
      }
    });

    setPreviewSeats(seats);
  }, [seatBlocks]);

  useEffect(() => {
    loadConcertData();
  }, [concertId]);

  useEffect(() => {
    generatePreview();
  }, [generatePreview]);

  const loadConcertData = async () => {
    try {
      setLoading(true);
      const [concertData, seatsData] = await Promise.all([
        fetchConcertById(concertId),
        adminService.getEventSeats(concertId).catch(() => []),
      ]);
      setConcert(concertData);
      setExistingSeats(Array.isArray(seatsData) ? seatsData : seatsData.seats || []);
    } catch (err) {
      console.error('Fehler beim Laden der Daten:', err);
      setError('Daten konnten nicht geladen werden.');
    } finally {
      setLoading(false);
    }
  };

  // Block hinzufügen
  const addBlock = () => {
    const lastBlock = seatBlocks[seatBlocks.length - 1];
    const nextStartRow = lastBlock 
      ? String.fromCharCode(lastBlock.endRow.toUpperCase().charCodeAt(0) + 1)
      : 'A';
    const nextEndRow = String.fromCharCode(nextStartRow.charCodeAt(0) + 2);
    
    setSeatBlocks([
      ...seatBlocks,
      {
        id: Date.now(),
        startRow: nextStartRow,
        endRow: nextEndRow > 'Z' ? 'Z' : nextEndRow,
        seatsPerRow: 10,
        category: 'ECONOMY',
        price: 50,
      },
    ]);
  };

  // Block entfernen
  const removeBlock = (blockId) => {
    if (seatBlocks.length <= 1) {
      setError('Mindestens ein Block muss vorhanden sein.');
      return;
    }
    setSeatBlocks(seatBlocks.filter((b) => b.id !== blockId));
  };

  // Block aktualisieren
  const updateBlock = (blockId, field, value) => {
    setSeatBlocks(seatBlocks.map((block) => {
      if (block.id !== blockId) return block;
      
      let processedValue = value;
      if (field === 'seatsPerRow' || field === 'price') {
        processedValue = parseFloat(value) || 0;
      } else if (field === 'startRow' || field === 'endRow') {
        processedValue = value.toUpperCase().slice(0, 1);
      }
      
      return { ...block, [field]: processedValue };
    }));
  };

  const handleCreateSeats = async () => {
    if (previewSeats.length === 0) {
      setError('Keine Sitzplätze zum Erstellen generiert.');
      return;
    }

    // Validierung: Überlappende Reihen prüfen
    const usedRows = new Set();
    for (const block of seatBlocks) {
      const startCode = block.startRow.charCodeAt(0);
      const endCode = block.endRow.charCodeAt(0);
      
      if (startCode > endCode) {
        setError(`Block "${block.startRow}-${block.endRow}": Startreihe muss vor Endreihe liegen.`);
        return;
      }
      
      for (let code = startCode; code <= endCode; code++) {
        const row = String.fromCharCode(code);
        if (usedRows.has(row)) {
          setError(`Reihe "${row}" ist in mehreren Blöcken definiert. Bitte beheben.`);
          return;
        }
        usedRows.add(row);
      }
    }

    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      await adminService.createSeats(concertId, previewSeats);
      setSuccess(`Sitzplätze wurden aktualisiert! Neue Anzahl: ${previewSeats.length}`);
      
      // Aktualisiere existierende Sitzplätze
      loadConcertData();
      
      // Erfolgsmeldung nach 3 Sekunden ausblenden
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Fehler beim Erstellen der Sitzplätze:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Sitzplätze konnten nicht erstellt werden. Bitte versuchen Sie es erneut.');
      }
    } finally {
      setSaving(false);
    }
  };

  const handleCsvUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = async (event) => {
      try {
        const csv = event.target.result;
        const lines = csv.split('\n').filter((line) => line.trim());
        
        // Erste Zeile als Header überspringen
        const seats = lines.slice(1).map((line) => {
          const [row, number, category, price] = line.split(',').map((s) => s.trim());
          return {
            row,
            number: parseInt(number, 10),
            category: category || 'STANDARD',
            price: parseFloat(price) || 50,
          };
        });

        setPreviewSeats(seats);
        setSuccess(`${seats.length} Sitzplätze aus CSV importiert. Klicken Sie auf "Sitzplätze aktualisieren" zum Speichern.`);
      } catch (err) {
        setError('CSV konnte nicht verarbeitet werden. Bitte überprüfen Sie das Format.');
      }
    };
    reader.readAsText(file);
  };

  const getCategoryColor = (category) => {
    const cat = categories.find((c) => c.value === category);
    return cat ? cat.color : 'bg-gray-400';
  };

  const getCategoryInfo = (category) => {
    return categories.find((c) => c.value === category) || categories[2];
  };

  // Berechne Statistiken pro Kategorie
  const getBlockStats = () => {
    const stats = {};
    categories.forEach((cat) => {
      stats[cat.value] = { count: 0, totalPrice: 0 };
    });
    
    previewSeats.forEach((seat) => {
      if (stats[seat.category]) {
        stats[seat.category].count += 1;
        stats[seat.category].totalPrice += seat.price;
      }
    });
    
    return stats;
  };

  if (loading) {
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
      <div className="space-y-6">
        {/* Header */}
        <div>
          <button
            onClick={() => navigate('/admin')}
            className="flex items-center text-text-secondary hover:text-text-primary mb-4"
          >
            <span className="material-symbols-outlined mr-1">arrow_back</span>
            Zurück zum Dashboard
          </button>
          <h1 className="text-2xl font-bold text-text-primary">
            Sitzplätze verwalten
          </h1>
          {concert && (
            <p className="text-text-secondary mt-1">
              {concert.name} - {concert.venue}
            </p>
          )}
        </div>

        {/* Warning about replacement */}
        {existingSeats.length > 0 && (
          <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 flex items-start">
            <span className="material-symbols-outlined text-amber-600 mr-3 mt-0.5">
              warning
            </span>
            <div>
              <p className="text-amber-800 font-medium">
                Achtung: Vorhandene Sitzplätze werden ersetzt!
              </p>
              <p className="text-amber-700 text-sm mt-1">
                Es sind bereits {existingSeats.length} Sitzplätze für dieses Konzert vorhanden. 
                Wenn Sie auf "Sitzplätze aktualisieren" klicken, werden ALLE vorhandenen Sitzplätze 
                gelöscht und durch die neue Konfiguration ersetzt.
              </p>
            </div>
          </div>
        )}

        {/* Messages */}
        {success && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center">
            <span className="material-symbols-outlined text-green-600 mr-3">
              check_circle
            </span>
            <p className="text-green-700">{success}</p>
          </div>
        )}

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

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Generator / Upload Section */}
          <div className="bg-white rounded-lg shadow-sm border border-border-light p-6">
            <h2 className="text-lg font-semibold text-text-primary mb-4">
              Neue Sitzplätze hinzufügen
            </h2>

            {/* Mode Tabs */}
            <div className="flex border-b border-border-light mb-6">
              <button
                onClick={() => setGeneratorMode('generator')}
                className={`px-4 py-2 -mb-px border-b-2 transition-colors ${
                  generatorMode === 'generator'
                    ? 'border-primary text-primary'
                    : 'border-transparent text-text-secondary hover:text-text-primary'
                }`}
              >
                Generator
              </button>
              <button
                onClick={() => setGeneratorMode('csv')}
                className={`px-4 py-2 -mb-px border-b-2 transition-colors ${
                  generatorMode === 'csv'
                    ? 'border-primary text-primary'
                    : 'border-transparent text-text-secondary hover:text-text-primary'
                }`}
              >
                CSV Import
              </button>
            </div>

            {generatorMode === 'generator' ? (
              <div className="space-y-4">
                {/* Info-Box */}
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                  <p className="text-sm text-blue-700">
                    <span className="font-semibold">Multi-Block-Generator:</span> Definieren Sie mehrere Blöcke mit verschiedenen Kategorien und Preisen. Jeder Block umfasst einen Reihenbereich (z.B. A-C für VIP, D-F für Premium, usw.).
                  </p>
                </div>

                {/* Seat Blocks */}
                <div className="space-y-4">
                  {seatBlocks.map((block, index) => {
                    const catInfo = getCategoryInfo(block.category);
                    return (
                      <div 
                        key={block.id} 
                        className={`border-2 rounded-lg p-4 ${catInfo.bgLight} border-opacity-50`}
                        style={{ borderColor: catInfo.color.replace('bg-', '') }}
                      >
                        {/* Block Header */}
                        <div className="flex items-center justify-between mb-3">
                          <div className="flex items-center gap-2">
                            <span className={`w-4 h-4 rounded ${catInfo.color}`}></span>
                            <span className="font-semibold text-text-primary">
                              Block {index + 1}: {block.category}
                            </span>
                          </div>
                          <button
                            onClick={() => removeBlock(block.id)}
                            className="text-red-500 hover:text-red-700 p-1"
                            title="Block entfernen"
                          >
                            <span className="material-symbols-outlined text-sm">delete</span>
                          </button>
                        </div>

                        {/* Block Config Grid */}
                        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
                          {/* Start Row */}
                          <div>
                            <label className="block text-xs font-medium text-text-secondary mb-1">
                              Von Reihe
                            </label>
                            <input
                              type="text"
                              value={block.startRow}
                              onChange={(e) => updateBlock(block.id, 'startRow', e.target.value)}
                              maxLength={1}
                              className="w-full px-3 py-2 rounded border border-border-light focus:outline-none focus:ring-2 focus:ring-primary uppercase text-center font-mono"
                            />
                          </div>

                          {/* End Row */}
                          <div>
                            <label className="block text-xs font-medium text-text-secondary mb-1">
                              Bis Reihe
                            </label>
                            <input
                              type="text"
                              value={block.endRow}
                              onChange={(e) => updateBlock(block.id, 'endRow', e.target.value)}
                              maxLength={1}
                              className="w-full px-3 py-2 rounded border border-border-light focus:outline-none focus:ring-2 focus:ring-primary uppercase text-center font-mono"
                            />
                          </div>

                          {/* Seats per Row */}
                          <div>
                            <label className="block text-xs font-medium text-text-secondary mb-1">
                              Sitze/Reihe
                            </label>
                            <input
                              type="number"
                              value={block.seatsPerRow}
                              onChange={(e) => updateBlock(block.id, 'seatsPerRow', e.target.value)}
                              min={1}
                              max={50}
                              className="w-full px-3 py-2 rounded border border-border-light focus:outline-none focus:ring-2 focus:ring-primary text-center"
                            />
                          </div>

                          {/* Category */}
                          <div>
                            <label className="block text-xs font-medium text-text-secondary mb-1">
                              Kategorie
                            </label>
                            <select
                              value={block.category}
                              onChange={(e) => updateBlock(block.id, 'category', e.target.value)}
                              className="w-full px-3 py-2 rounded border border-border-light focus:outline-none focus:ring-2 focus:ring-primary"
                            >
                              {categories.map((cat) => (
                                <option key={cat.value} value={cat.value}>
                                  {cat.label}
                                </option>
                              ))}
                            </select>
                          </div>

                          {/* Price */}
                          <div>
                            <label className="block text-xs font-medium text-text-secondary mb-1">
                              Preis (€)
                            </label>
                            <input
                              type="number"
                              value={block.price}
                              onChange={(e) => updateBlock(block.id, 'price', e.target.value)}
                              min={0}
                              step={0.01}
                              className="w-full px-3 py-2 rounded border border-border-light focus:outline-none focus:ring-2 focus:ring-primary text-center"
                            />
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>

                {/* Add Block Button */}
                <button
                  onClick={addBlock}
                  className="w-full py-2 border-2 border-dashed border-gray-300 rounded-lg text-text-secondary hover:border-primary hover:text-primary transition-colors flex items-center justify-center gap-2"
                >
                  <span className="material-symbols-outlined">add</span>
                  Weiteren Block hinzufügen
                </button>

                {/* Category Summary */}
                <div className="bg-gray-50 rounded-lg p-4 mt-4">
                  <h3 className="text-sm font-semibold text-text-primary mb-2">Zusammenfassung</h3>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    {categories.map((cat) => {
                      const stats = getBlockStats()[cat.value];
                      return (
                        <div key={cat.value} className={`${cat.bgLight} rounded-lg p-2`}>
                          <div className="flex items-center gap-1 mb-1">
                            <span className={`w-3 h-3 rounded ${cat.color}`}></span>
                            <span className="text-xs font-medium">{cat.label}</span>
                          </div>
                          <div className="text-sm font-bold">{stats.count} Sitze</div>
                          <div className="text-xs text-text-secondary">
                            {new Intl.NumberFormat('de-DE', {
                              style: 'currency',
                              currency: 'EUR',
                            }).format(stats.totalPrice)}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <p className="text-sm text-text-secondary">
                  Laden Sie eine CSV-Datei mit folgendem Format hoch:
                </p>
                <div className="bg-gray-50 p-4 rounded-lg font-mono text-sm">
                  row,number,category,price<br />
                  A,1,VIP,100<br />
                  A,2,VIP,100<br />
                  B,1,STANDARD,50
                </div>
                <input
                  type="file"
                  accept=".csv"
                  onChange={handleCsvUpload}
                  className="w-full px-4 py-2 rounded-lg border border-border-light focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            )}

            {/* Update Button */}
            <button
              onClick={handleCreateSeats}
              disabled={saving || previewSeats.length === 0}
              className="w-full mt-6 px-4 py-3 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              {saving ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Aktualisiere Sitzplätze...
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined mr-2">sync</span>
                  {previewSeats.length} Sitzplätze aktualisieren
                </>
              )}
            </button>
          </div>

          {/* Preview Section */}
          <div className="bg-white rounded-lg shadow-sm border border-border-light p-6">
            <h2 className="text-lg font-semibold text-text-primary mb-4">
              Vorschau
            </h2>

            {/* Legend */}
            <div className="flex flex-wrap gap-3 mb-4">
              {categories.map((cat) => (
                <div key={cat.value} className="flex items-center">
                  <div className={`w-4 h-4 rounded ${cat.color} mr-2`}></div>
                  <span className="text-sm text-text-secondary">{cat.label}</span>
                </div>
              ))}
            </div>

            {/* Seat Grid */}
            <div className="border border-border-light rounded-lg p-4 bg-gray-50 max-h-96 overflow-auto">
              {previewSeats.length === 0 ? (
                <p className="text-center text-text-secondary py-8">
                  Keine Sitzplätze konfiguriert
                </p>
              ) : (
                <div className="space-y-2">
                  {/* Gruppiere nach Reihen */}
                  {Object.entries(
                    previewSeats.reduce((acc, seat) => {
                      if (!acc[seat.row]) acc[seat.row] = [];
                      acc[seat.row].push(seat);
                      return acc;
                    }, {})
                  ).map(([row, seats]) => (
                    <div key={row} className="flex items-center">
                      <span className="w-8 text-sm font-medium text-text-secondary">
                        {row}
                      </span>
                      <div className="flex flex-wrap gap-1">
                        {seats
                          .sort((a, b) => a.number - b.number)
                          .map((seat) => (
                            <div
                              key={`${seat.row}-${seat.number}`}
                              className={`w-6 h-6 rounded text-white text-xs flex items-center justify-center ${getCategoryColor(
                                seat.category
                              )}`}
                              title={`${seat.row}${seat.number} - ${seat.category} - €${seat.price}`}
                            >
                              {seat.number}
                            </div>
                          ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <p className="text-sm text-text-secondary mt-4">
              Gesamt: {previewSeats.length} Sitzplätze | Preis:{' '}
              {new Intl.NumberFormat('de-DE', {
                style: 'currency',
                currency: 'EUR',
              }).format(previewSeats.reduce((sum, s) => sum + s.price, 0))}
            </p>
          </div>
        </div>

        {/* Existing Seats Section */}
        <div className="bg-white rounded-lg shadow-sm border border-border-light p-6">
          <h2 className="text-lg font-semibold text-text-primary mb-4">
            Vorhandene Sitzplätze ({existingSeats.length})
          </h2>

          {existingSeats.length === 0 ? (
            <p className="text-center text-text-secondary py-8">
              Noch keine Sitzplätze für dieses Konzert vorhanden.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-text-secondary uppercase">
                      Reihe
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-text-secondary uppercase">
                      Nummer
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-text-secondary uppercase">
                      Kategorie
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-text-secondary uppercase">
                      Preis
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-text-secondary uppercase">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-light">
                  {existingSeats.slice(0, 50).map((seat) => (
                    <tr key={seat.id} className="hover:bg-gray-50">
                      <td className="px-4 py-2 text-sm">{seat.row}</td>
                      <td className="px-4 py-2 text-sm">{seat.number}</td>
                      <td className="px-4 py-2 text-sm">
                        <span
                          className={`px-2 py-1 rounded text-white text-xs ${getCategoryColor(
                            seat.category
                          )}`}
                        >
                          {seat.category}
                        </span>
                      </td>
                      <td className="px-4 py-2 text-sm">
                        {new Intl.NumberFormat('de-DE', {
                          style: 'currency',
                          currency: 'EUR',
                        }).format(seat.price)}
                      </td>
                      <td className="px-4 py-2 text-sm">
                        <span
                          className={`px-2 py-1 rounded text-xs ${
                            seat.status === 'AVAILABLE'
                              ? 'bg-green-100 text-green-600'
                              : seat.status === 'HELD'
                              ? 'bg-yellow-100 text-yellow-600'
                              : 'bg-red-100 text-red-600'
                          }`}
                        >
                          {seat.status === 'AVAILABLE'
                            ? 'Verfügbar'
                            : seat.status === 'HELD'
                            ? 'Reserviert'
                            : 'Verkauft'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {existingSeats.length > 50 && (
                <p className="text-sm text-text-secondary mt-4 text-center">
                  Zeige 50 von {existingSeats.length} Sitzplätzen
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  );
};

export default ManageSeatsPage;
