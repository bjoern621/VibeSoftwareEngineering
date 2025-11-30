import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
  getAllVehicles,
  createVehicle,
  updateVehicle,
  markAsInMaintenance,
  markAsOutOfService,
  markAsAvailable,
  getBranches,
} from '../services/vehicleService';

const STATUS_MAPPING = {
  AVAILABLE: { label: 'Verfügbar', color: 'success' },
  RENTED: { label: 'Vermietet', color: 'warning' },
  IN_MAINTENANCE: { label: 'Wartung', color: 'info' },
  OUT_OF_SERVICE: { label: 'Außer Betrieb', color: 'danger' },
};

const VEHICLE_TYPE_LABELS = {
  COMPACT_CAR: 'Kleinwagen',
  SEDAN: 'Limousine',
  SUV: 'SUV',
  VAN: 'Transporter',
};

const VehicleManagementPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [vehicles, setVehicles] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [branchFilter, setBranchFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState(null);
  const [formData, setFormData] = useState({
    licensePlate: '',
    brand: '',
    model: '',
    year: new Date().getFullYear(),
    mileage: 0,
    vehicleType: 'COMPACT_CAR',
    branchId: '',
  });
  const [formError, setFormError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const [vehiclesData, branchesData] = await Promise.all([getAllVehicles(), getBranches()]);
      setVehicles(vehiclesData);
      setBranches(branchesData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!user || (user.role !== 'EMPLOYEE' && user.role !== 'ADMIN')) {
      navigate('/login');
      return;
    }
    loadData();
  }, [user, navigate, loadData]);

  const getStatusClasses = (color) => {
    const colors = {
      success: 'bg-green-100 text-green-800',
      warning: 'bg-yellow-100 text-yellow-800',
      info: 'bg-blue-100 text-blue-800',
      danger: 'bg-red-100 text-red-800',
    };
    return colors[color] || 'bg-gray-100 text-gray-800';
  };

  const getStatusDotClasses = (color) => {
    const colors = {
      success: 'bg-green-800',
      warning: 'bg-yellow-800',
      info: 'bg-blue-800',
      danger: 'bg-red-800',
    };
    return colors[color] || 'bg-gray-800';
  };

  const filteredVehicles = vehicles.filter((vehicle) => {
    const searchLower = searchTerm.toLowerCase();
    const matchesSearch =
      vehicle.brand?.toLowerCase().includes(searchLower) ||
      vehicle.model?.toLowerCase().includes(searchLower) ||
      vehicle.licensePlate?.toLowerCase().includes(searchLower);
    const matchesStatus = !statusFilter || vehicle.status === statusFilter;
    const matchesType = !typeFilter || vehicle.vehicleType === typeFilter;
    const matchesBranch = !branchFilter || vehicle.branchId === parseInt(branchFilter, 10);
    return matchesSearch && matchesStatus && matchesType && matchesBranch;
  });

  const resetFilters = () => {
    setSearchTerm('');
    setStatusFilter('');
    setTypeFilter('');
    setBranchFilter('');
  };

  const openCreateModal = () => {
    setEditingVehicle(null);
    setFormData({
      licensePlate: '',
      brand: '',
      model: '',
      year: new Date().getFullYear(),
      mileage: 0,
      vehicleType: 'COMPACT_CAR',
      branchId: branches.length > 0 ? branches[0].id : '',
    });
    setFormError(null);
    setShowModal(true);
  };

  const openEditModal = (vehicle) => {
    setEditingVehicle(vehicle);
    setFormData({
      licensePlate: vehicle.licensePlate,
      brand: vehicle.brand,
      model: vehicle.model,
      year: vehicle.year,
      mileage: vehicle.mileage,
      vehicleType: vehicle.vehicleType,
      branchId: vehicle.branchId,
    });
    setFormError(null);
    setShowModal(true);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]:
        name === 'year' || name === 'mileage' || name === 'branchId'
          ? parseInt(value, 10) || ''
          : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setActionLoading(true);

    try {
      if (editingVehicle) {
        const { licensePlate, ...updateData } = formData;
        await updateVehicle(editingVehicle.id, updateData);
      } else {
        await createVehicle(formData);
      }
      setShowModal(false);
      await loadData();
    } catch (err) {
      setFormError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleStatusChange = async (vehicleId, action) => {
    setActionLoading(true);
    try {
      switch (action) {
        case 'maintenance':
          await markAsInMaintenance(vehicleId);
          break;
        case 'outOfService':
          await markAsOutOfService(vehicleId);
          break;
        case 'available':
          await markAsAvailable(vehicleId);
          break;
        default:
          break;
      }
      await loadData();
    } catch (err) {
      setError(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="relative flex min-h-screen w-full">
      {/* Sidebar */}
      <aside className="flex w-64 flex-col gap-y-6 border-r border-gray-200 p-4 sticky top-0 h-screen bg-gray-50">
        <div className="flex items-center gap-3">
          <div className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10 bg-primary flex items-center justify-center text-white font-bold">
            {user?.firstName?.charAt(0) || 'U'}
          </div>
          <div className="flex flex-col">
            <h1 className="text-base font-medium">
              {user?.firstName} {user?.lastName}
            </h1>
            <p className="text-gray-600 text-sm font-normal">
              {user?.role === 'ADMIN' ? 'Administrator' : 'Mitarbeiter'}
            </p>
          </div>
        </div>

        <nav className="flex flex-col gap-2 flex-1">
          <a href="/" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">home</span>
            <p className="text-sm font-medium">Startseite</p>
          </a>
          <a
            href="/buchungen"
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200"
          >
            <span className="material-symbols-outlined">calendar_month</span>
            <p className="text-sm font-medium">Buchungen</p>
          </a>
          <a
            href="/fahrzeugverwaltung"
            className="flex items-center gap-3 px-3 py-2 rounded-lg bg-primary/20 text-primary"
          >
            <span className="material-symbols-outlined">directions_car</span>
            <p className="text-sm font-medium">Fahrzeugverwaltung</p>
          </a>
        </nav>

        <div className="flex flex-col gap-1">
          <button
            onClick={logout}
            className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200 w-full text-left"
          >
            <span className="material-symbols-outlined">logout</span>
            <p className="text-sm font-medium">Abmelden</p>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8 bg-background-light">
        <div className="mx-auto max-w-7xl">
          {/* Error Alert */}
          {error && (
            <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-lg flex justify-between items-center">
              <span>{error}</span>
              <button onClick={() => setError(null)} className="text-red-700 hover:text-red-900">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
          )}

          {/* Page Heading */}
          <header className="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div className="flex flex-col">
              <h1 className="text-4xl font-black tracking-tighter">Fahrzeugverwaltung</h1>
              <p className="text-gray-600 text-base">
                Verwalten Sie die gesamte Fahrzeugflotte des Unternehmens.
              </p>
            </div>
            <button
              onClick={openCreateModal}
              className="flex min-w-[84px] cursor-pointer items-center justify-center gap-2 overflow-hidden rounded-lg h-10 px-4 bg-primary text-white text-sm font-bold shadow-sm hover:opacity-90"
            >
              <span className="material-symbols-outlined">add</span>
              <span>Neues Fahrzeug</span>
            </button>
          </header>

          {/* Filters Section */}
          <div className="mb-4 space-y-4">
            {/* SearchBar */}
            <div className="flex w-full h-12">
              <div className="flex bg-gray-100 items-center justify-center pl-4 rounded-l-lg">
                <span className="material-symbols-outlined text-gray-600">search</span>
              </div>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="form-input flex w-full min-w-0 flex-1 rounded-r-lg border-none bg-gray-100 h-full placeholder:text-gray-500 px-4 pl-2 text-base font-normal focus:ring-2 focus:ring-primary/50"
                placeholder="Suche nach Marke, Modell, Kennzeichen..."
              />
            </div>

            {/* Filter Dropdowns */}
            <div className="flex flex-wrap gap-3">
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="h-8 rounded-lg bg-gray-100 px-3 text-sm font-medium focus:ring-2 focus:ring-primary/50"
              >
                <option value="">Alle Status</option>
                {Object.entries(STATUS_MAPPING).map(([key, { label }]) => (
                  <option key={key} value={key}>
                    {label}
                  </option>
                ))}
              </select>
              <select
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value)}
                className="h-8 rounded-lg bg-gray-100 px-3 text-sm font-medium focus:ring-2 focus:ring-primary/50"
              >
                <option value="">Alle Fahrzeugtypen</option>
                {Object.entries(VEHICLE_TYPE_LABELS).map(([key, label]) => (
                  <option key={key} value={key}>
                    {label}
                  </option>
                ))}
              </select>
              <select
                value={branchFilter}
                onChange={(e) => setBranchFilter(e.target.value)}
                className="h-8 rounded-lg bg-gray-100 px-3 text-sm font-medium focus:ring-2 focus:ring-primary/50"
              >
                <option value="">Alle Filialen</option>
                {branches.map((branch) => (
                  <option key={branch.id} value={branch.id}>
                    {branch.name}
                  </option>
                ))}
              </select>
              <button
                onClick={resetFilters}
                className="flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-lg text-gray-600 pl-3 pr-3 hover:bg-gray-100"
              >
                <p className="text-sm font-medium">Filter zurücksetzen</p>
              </button>
            </div>
          </div>

          {/* Table */}
          <div className="overflow-hidden rounded-lg border border-gray-200">
            <div className="overflow-x-auto">
              <table className="w-full min-w-max">
                <thead>
                  <tr className="bg-gray-50">
                    <th className="px-4 py-3 text-left text-sm font-medium">Marke</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Modell</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Kennzeichen</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Fahrzeugtyp</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Status</th>
                    <th className="px-4 py-3 text-left text-sm font-medium">Filiale</th>
                    <th className="px-4 py-3 text-right text-sm font-medium">Aktionen</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredVehicles.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="px-4 py-8 text-center text-gray-500">
                        Keine Fahrzeuge gefunden.
                      </td>
                    </tr>
                  ) : (
                    filteredVehicles.map((vehicle) => {
                      const statusInfo = STATUS_MAPPING[vehicle.status] || {
                        label: vehicle.status,
                        color: 'gray',
                      };
                      return (
                        <tr key={vehicle.id}>
                          <td className="h-[72px] px-4 py-2 text-sm font-medium">
                            {vehicle.brand}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                            {vehicle.model}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                            {vehicle.licensePlate}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                            {VEHICLE_TYPE_LABELS[vehicle.vehicleType] || vehicle.vehicleType}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-sm">
                            <div
                              className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium ${getStatusClasses(statusInfo.color)}`}
                            >
                              <span
                                className={`h-2 w-2 rounded-full ${getStatusDotClasses(statusInfo.color)}`}
                              />
                              {statusInfo.label}
                            </div>
                          </td>
                          <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                            {vehicle.branchName}
                          </td>
                          <td className="h-[72px] px-4 py-2 text-sm">
                            <div className="flex justify-end gap-2">
                              <button
                                onClick={() => openEditModal(vehicle)}
                                className="p-2 rounded-lg hover:bg-gray-100"
                                title="Bearbeiten"
                              >
                                <span className="material-symbols-outlined text-lg">edit</span>
                              </button>
                              {vehicle.status === 'AVAILABLE' && (
                                <>
                                  <button
                                    onClick={() => handleStatusChange(vehicle.id, 'maintenance')}
                                    className="p-2 rounded-lg hover:bg-blue-100 text-blue-600"
                                    title="In Wartung setzen"
                                    disabled={actionLoading}
                                  >
                                    <span className="material-symbols-outlined text-lg">build</span>
                                  </button>
                                  <button
                                    onClick={() => handleStatusChange(vehicle.id, 'outOfService')}
                                    className="p-2 rounded-lg hover:bg-red-100 text-red-600"
                                    title="Außer Betrieb setzen"
                                    disabled={actionLoading}
                                  >
                                    <span className="material-symbols-outlined text-lg">block</span>
                                  </button>
                                </>
                              )}
                              {(vehicle.status === 'IN_MAINTENANCE' ||
                                vehicle.status === 'OUT_OF_SERVICE') && (
                                <button
                                  onClick={() => handleStatusChange(vehicle.id, 'available')}
                                  className="p-2 rounded-lg hover:bg-green-100 text-green-600"
                                  title="Verfügbar machen"
                                  disabled={actionLoading}
                                >
                                  <span className="material-symbols-outlined text-lg">
                                    check_circle
                                  </span>
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination Info */}
          <div className="flex items-center justify-between mt-4">
            <p className="text-sm text-gray-600">
              Zeige {filteredVehicles.length} von {vehicles.length} Fahrzeugen
            </p>
          </div>
        </div>
      </main>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold">
                {editingVehicle ? 'Fahrzeug bearbeiten' : 'Neues Fahrzeug'}
              </h2>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            {formError && (
              <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-lg text-sm">{formError}</div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {!editingVehicle && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Kennzeichen
                  </label>
                  <input
                    type="text"
                    name="licensePlate"
                    value={formData.licensePlate}
                    onChange={handleFormChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary"
                    placeholder="z.B. B-AB 1234"
                  />
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Marke</label>
                  <input
                    type="text"
                    name="brand"
                    value={formData.brand}
                    onChange={handleFormChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary"
                    placeholder="z.B. BMW"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Modell</label>
                  <input
                    type="text"
                    name="model"
                    value={formData.model}
                    onChange={handleFormChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary"
                    placeholder="z.B. 3er"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Baujahr</label>
                  <input
                    type="number"
                    name="year"
                    value={formData.year}
                    onChange={handleFormChange}
                    required
                    min="1900"
                    max={new Date().getFullYear() + 1}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Kilometerstand
                  </label>
                  <input
                    type="number"
                    name="mileage"
                    value={formData.mileage}
                    onChange={handleFormChange}
                    required
                    min="0"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Fahrzeugtyp</label>
                <select
                  name="vehicleType"
                  value={formData.vehicleType}
                  onChange={handleFormChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary"
                >
                  {Object.entries(VEHICLE_TYPE_LABELS).map(([key, label]) => (
                    <option key={key} value={key}>
                      {label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Filiale</label>
                <select
                  name="branchId"
                  value={formData.branchId}
                  onChange={handleFormChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary"
                >
                  <option value="">Bitte wählen</option>
                  {branches.map((branch) => (
                    <option key={branch.id} value={branch.id}>
                      {branch.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
                >
                  Abbrechen
                </button>
                <button
                  type="submit"
                  disabled={actionLoading}
                  className="px-4 py-2 text-white bg-primary rounded-lg hover:opacity-90 disabled:opacity-50"
                >
                  {actionLoading ? 'Speichern...' : editingVehicle ? 'Aktualisieren' : 'Erstellen'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default VehicleManagementPage;
