import React from 'react';

/**
 * VehicleManagementPage - Fahrzeugverwaltung für Mitarbeiter (nur Design)
 * Konvertiert von Stitch Design: fahrzeugverwaltung_(mitarbeiter)_1
 * OHNE FUNKTION - nur visuelle Darstellung
 */
const VehicleManagementPage = () => {
  // Mock-Daten (keine Funktion, nur Anzeige)
  const mockVehicles = [
    {
      id: 1,
      brand: 'Volkswagen',
      model: 'Golf',
      licensePlate: 'B-VG 1234',
      type: 'Kleinwagen',
      status: 'Verfügbar',
      statusColor: 'success',
      branch: 'Berlin Mitte',
    },
    {
      id: 2,
      brand: 'Mercedes-Benz',
      model: 'C-Klasse',
      licensePlate: 'M-BC 5678',
      type: 'Limousine',
      status: 'Vermietet',
      statusColor: 'warning',
      branch: 'München Zentrum',
    },
    {
      id: 3,
      brand: 'BMW',
      model: 'X5',
      licensePlate: 'B-MW 9101',
      type: 'SUV',
      status: 'Wartung',
      statusColor: 'info',
      branch: 'Berlin Mitte',
    },
    {
      id: 4,
      brand: 'Ford',
      model: 'Transit',
      licensePlate: 'H-FT 1121',
      type: 'Transporter',
      status: 'Verfügbar',
      statusColor: 'success',
      branch: 'Hamburg Hafen',
    },
    {
      id: 5,
      brand: 'Audi',
      model: 'A3',
      licensePlate: 'B-AU 3141',
      type: 'Kleinwagen',
      status: 'Außer Betrieb',
      statusColor: 'danger',
      branch: 'Berlin Mitte',
    },
  ];

  const getStatusClasses = (color) => {
    const colors = {
      success: 'bg-green-100 text-green-800',
      warning: 'bg-yellow-100 text-yellow-800',
      info: 'bg-blue-100 text-blue-800',
      danger: 'bg-red-100 text-red-800',
    };
    return colors[color] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="relative flex min-h-screen w-full">
      {/* Sidebar */}
      <aside className="flex w-64 flex-col gap-y-6 border-r border-gray-200 p-4 sticky top-0 h-screen bg-gray-50">
        <div className="flex items-center gap-3">
          <div
            className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10"
            style={{
              backgroundImage:
                'url("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100")',
            }}
          />
          <div className="flex flex-col">
            <h1 className="text-base font-medium">Max Mustermann</h1>
            <p className="text-gray-600 text-sm font-normal">Administrator</p>
          </div>
        </div>

        <nav className="flex flex-col gap-2 flex-1">
          <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">dashboard</span>
            <p className="text-sm font-medium">Dashboard</p>
          </a>
          <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">calendar_month</span>
            <p className="text-sm font-medium">Buchungen</p>
          </a>
          <a
            href="#"
            className="flex items-center gap-3 px-3 py-2 rounded-lg bg-primary/20 text-primary"
          >
            <span className="material-symbols-outlined">directions_car</span>
            <p className="text-sm font-medium">Fahrzeugverwaltung</p>
          </a>
          <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">groups</span>
            <p className="text-sm font-medium">Kunden</p>
          </a>
          <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">storefront</span>
            <p className="text-sm font-medium">Filialen</p>
          </a>
        </nav>

        <div className="flex flex-col gap-1">
          <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">settings</span>
            <p className="text-sm font-medium">Einstellungen</p>
          </a>
          <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-200">
            <span className="material-symbols-outlined">logout</span>
            <p className="text-sm font-medium">Abmelden</p>
          </a>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8 bg-background-light">
        <div className="mx-auto max-w-7xl">
          {/* Page Heading */}
          <header className="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div className="flex flex-col">
              <h1 className="text-4xl font-black tracking-tighter">Fahrzeugverwaltung</h1>
              <p className="text-gray-600 text-base">
                Verwalten Sie die gesamte Fahrzeugflotte des Unternehmens.
              </p>
            </div>
            <button className="flex min-w-[84px] cursor-pointer items-center justify-center gap-2 overflow-hidden rounded-lg h-10 px-4 bg-primary text-white text-sm font-bold shadow-sm hover:opacity-90">
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
                className="form-input flex w-full min-w-0 flex-1 rounded-r-lg border-none bg-gray-100 h-full placeholder:text-gray-500 px-4 pl-2 text-base font-normal focus:ring-2 focus:ring-primary/50"
                placeholder="Suche nach Marke, Modell, Kennzeichen..."
              />
            </div>

            {/* Filter Chips */}
            <div className="flex flex-wrap gap-3">
              <button className="flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-gray-100 pl-4 pr-2 hover:bg-primary/20">
                <p className="text-sm font-medium">Status</p>
                <span className="material-symbols-outlined text-base">expand_more</span>
              </button>
              <button className="flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-gray-100 pl-4 pr-2 hover:bg-primary/20">
                <p className="text-sm font-medium">Fahrzeugtyp</p>
                <span className="material-symbols-outlined text-base">expand_more</span>
              </button>
              <button className="flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-lg bg-gray-100 pl-4 pr-2 hover:bg-primary/20">
                <p className="text-sm font-medium">Filiale</p>
                <span className="material-symbols-outlined text-base">expand_more</span>
              </button>
              <button className="flex h-8 shrink-0 items-center justify-center gap-x-2 rounded-lg text-gray-600 pl-3 pr-3 hover:bg-gray-100">
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
                  {mockVehicles.map((vehicle) => (
                    <tr key={vehicle.id}>
                      <td className="h-[72px] px-4 py-2 text-sm font-medium">{vehicle.brand}</td>
                      <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">{vehicle.model}</td>
                      <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">
                        {vehicle.licensePlate}
                      </td>
                      <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">{vehicle.type}</td>
                      <td className="h-[72px] px-4 py-2 text-sm">
                        <div
                          className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium ${getStatusClasses(vehicle.statusColor)}`}
                        >
                          <span
                            className={`h-2 w-2 rounded-full ${vehicle.statusColor === 'success' ? 'bg-green-800' : vehicle.statusColor === 'warning' ? 'bg-yellow-800' : vehicle.statusColor === 'info' ? 'bg-blue-800' : 'bg-red-800'}`}
                          />
                          {vehicle.status}
                        </div>
                      </td>
                      <td className="h-[72px] px-4 py-2 text-gray-600 text-sm">{vehicle.branch}</td>
                      <td className="h-[72px] px-4 py-2 text-sm">
                        <div className="flex justify-end gap-2">
                          <button className="p-2 rounded-lg hover:bg-gray-100">
                            <span className="material-symbols-outlined text-lg">edit</span>
                          </button>
                          <button className="p-2 rounded-lg hover:bg-red-100 text-red-600">
                            <span className="material-symbols-outlined text-lg">delete</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination */}
          <div className="flex items-center justify-between mt-4">
            <p className="text-sm text-gray-600">Zeige 1-5 von 42 Fahrzeugen</p>
            <div className="flex items-center gap-2">
              <button className="flex items-center justify-center h-8 px-3 rounded-lg text-sm font-medium bg-gray-100 hover:bg-primary/20">
                <span className="material-symbols-outlined text-lg">chevron_left</span>
                <span>Zurück</span>
              </button>
              <button className="flex items-center justify-center h-8 px-3 rounded-lg text-sm font-medium bg-gray-100 hover:bg-primary/20">
                <span>Weiter</span>
                <span className="material-symbols-outlined text-lg">chevron_right</span>
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default VehicleManagementPage;
