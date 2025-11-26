import React from 'react';

/**
 * CheckInOutPage - Check-in/Check-out Protokoll für Mitarbeiter (nur Design)
 * Konvertiert von Stitch Design: check-out/check-in_(mitarbeiter)_1
 * OHNE FUNKTION - nur visuelle Darstellung
 */
const CheckInOutPage = () => {
  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="flex-shrink-0 w-64 bg-gray-50 border-r border-gray-200 p-4 flex flex-col justify-between">
        <div className="flex flex-col gap-4">
          <div className="flex items-center gap-3">
            <div
              className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10"
              style={{
                backgroundImage:
                  'url("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100")',
              }}
            />
            <div className="flex flex-col">
              <h1 className="text-base font-medium text-gray-900">Max Mustermann</h1>
              <p className="text-primary text-sm font-normal">Rental Agent</p>
            </div>
          </div>

          <nav className="flex flex-col gap-2 mt-4">
            <button className="flex items-center gap-3 px-3 py-2 text-gray-900 hover:bg-primary/10 rounded-lg">
              <span className="material-symbols-outlined">dashboard</span>
              <p className="text-sm font-medium">Dashboard</p>
            </button>
            <button className="flex items-center gap-3 px-3 py-2 text-gray-900 hover:bg-primary/10 rounded-lg">
              <span className="material-symbols-outlined">book_online</span>
              <p className="text-sm font-medium">Bookings</p>
            </button>
            <button className="flex items-center gap-3 px-3 py-2 rounded-lg bg-primary/20 text-primary">
              <span className="material-symbols-outlined">directions_car</span>
              <p className="text-sm font-medium">Vehicles</p>
            </button>
            <button className="flex items-center gap-3 px-3 py-2 text-gray-900 hover:bg-primary/10 rounded-lg">
              <span className="material-symbols-outlined">group</span>
              <p className="text-sm font-medium">Customers</p>
            </button>
            <button className="flex items-center gap-3 px-3 py-2 text-gray-900 hover:bg-primary/10 rounded-lg">
              <span className="material-symbols-outlined">monitoring</span>
              <p className="text-sm font-medium">Reports</p>
            </button>
          </nav>
        </div>

        <div className="flex flex-col gap-4">
          <button className="flex min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-10 px-4 bg-primary text-white text-sm font-bold hover:opacity-90">
            <span>New Booking</span>
          </button>
          <div className="flex flex-col gap-1">
            <button className="flex items-center gap-3 px-3 py-2 text-gray-900 hover:bg-primary/10 rounded-lg">
              <span className="material-symbols-outlined">settings</span>
              <p className="text-sm font-medium">Settings</p>
            </button>
            <button className="flex items-center gap-3 px-3 py-2 text-gray-900 hover:bg-primary/10 rounded-lg">
              <span className="material-symbols-outlined">logout</span>
              <p className="text-sm font-medium">Log out</p>
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8 overflow-y-auto bg-background-light">
        <div className="max-w-7xl mx-auto">
          {/* Page Heading */}
          <div className="flex flex-wrap justify-between items-start gap-4 mb-6">
            <div className="flex flex-col gap-2">
              <p className="text-4xl font-black tracking-tight">
                Fahrzeug-Protokoll: Check-in / Check-out
              </p>
              <p className="text-primary text-base font-normal">
                Verwalten Sie den Fahrzeugzustand bei Übergabe und Rücknahme.
              </p>
            </div>
          </div>

          {/* Segmented Buttons */}
          <div className="flex mb-8">
            <div className="flex h-10 w-full max-w-sm items-center justify-center rounded-lg bg-gray-200 p-1 border border-gray-300">
              <label className="flex cursor-pointer h-full grow items-center justify-center overflow-hidden rounded-lg px-2 has-[:checked]:bg-white has-[:checked]:shadow-sm has-[:checked]:text-gray-900 text-primary text-sm font-medium transition-colors">
                <span>Check-out</span>
                <input
                  defaultChecked
                  className="invisible w-0"
                  name="process_type"
                  type="radio"
                  value="Check-out"
                />
              </label>
              <label className="flex cursor-pointer h-full grow items-center justify-center overflow-hidden rounded-lg px-2 has-[:checked]:bg-white has-[:checked]:shadow-sm has-[:checked]:text-gray-900 text-primary text-sm font-medium transition-colors">
                <span>Check-in</span>
                <input
                  className="invisible w-0"
                  name="process_type"
                  type="radio"
                  value="Check-in"
                />
              </label>
            </div>
          </div>

          {/* Form Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left Column */}
            <div className="lg:col-span-2 flex flex-col gap-8">
              {/* Booking Information */}
              <div>
                <h2 className="text-gray-900 text-[22px] font-bold mb-3">Booking Information</h2>
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                  <div className="flex flex-col sm:flex-row items-start justify-between gap-6">
                    <div className="flex flex-col gap-4 flex-[2_2_0px]">
                      <div className="flex flex-col gap-1">
                        <p className="text-sm text-primary">Booking ID: #789123</p>
                        <p className="text-xl font-bold text-gray-900">Volkswagen Golf VIII</p>
                        <p className="text-gray-500 text-sm">License Plate: B-VG 1234</p>
                      </div>
                      <div className="flex flex-col gap-1">
                        <p className="font-semibold text-gray-900">Jane Doe</p>
                        <p className="text-gray-500 text-sm">24.07.2024 - 28.07.2024</p>
                      </div>
                    </div>
                    <div
                      className="w-full sm:w-48 bg-center bg-no-repeat aspect-video bg-cover rounded-lg flex-1"
                      style={{
                        backgroundImage:
                          'url("https://lh3.googleusercontent.com/aida-public/AB6AXuDKtaOsLH9mUzxVC_t20ycK7AD4-4mUqXTU8dZO7xdKSxOYmzLQCtpoIRg7o6vmQlA2eY5Gn_vVRGaoBfMg34co32qrAptGsKtqM31Gw7pgroHrO9rIpzJfuU5uwtxmgAYNepB0Fa8lfHXv6mcldDaS10vlXVrK3M-js8KEfifIPaUL6nVmosU9yliTAPSZEtYY4uu4DQhTPyx9CZn22-dIgRYn_1C9yERlNgUWYNI3hdboFKxfR76bbEcrl8o_nsa6Qn6Xv20wiQpF")',
                      }}
                    />
                  </div>
                </div>
              </div>

              {/* Vehicle Status */}
              <div>
                <h2 className="text-gray-900 text-[22px] font-bold mb-3">
                  Vehicle Status (Check-out)
                </h2>
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label
                      className="block text-sm font-medium text-gray-700 mb-1"
                      htmlFor="mileage"
                    >
                      Kilometerstand
                    </label>
                    <input
                      className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary"
                      id="mileage"
                      type="number"
                      defaultValue="12540"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Tankfüllstand
                    </label>
                    <div className="flex items-center justify-between text-xs text-gray-500 px-1">
                      <span>E</span>
                      <span>1/4</span>
                      <span>1/2</span>
                      <span>3/4</span>
                      <span>F</span>
                    </div>
                    <input
                      className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-primary"
                      max="4"
                      min="0"
                      step="1"
                      type="range"
                      defaultValue="4"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Sauberkeit
                    </label>
                    <div className="flex flex-wrap gap-4">
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          className="form-radio text-primary focus:ring-primary"
                          name="cleanliness"
                          type="radio"
                        />
                        <span className="text-sm">Verschmutzt</span>
                      </label>
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          className="form-radio text-primary focus:ring-primary"
                          name="cleanliness"
                          type="radio"
                        />
                        <span className="text-sm">Normal</span>
                      </label>
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          defaultChecked
                          className="form-radio text-primary focus:ring-primary"
                          name="cleanliness"
                          type="radio"
                        />
                        <span className="text-sm">Sauber</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>

              {/* Damage Report */}
              <div>
                <h2 className="text-gray-900 text-[22px] font-bold mb-3">Schadensprotokoll</h2>
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex flex-col lg:flex-row gap-6">
                  <div className="relative flex-shrink-0">
                    <img
                      alt="Vehicle schematic for damage marking"
                      className="w-64 h-auto mx-auto"
                      src="https://lh3.googleusercontent.com/aida-public/AB6AXuA2D0T4enaMVlqwYln9K_udeBlRJCnBLV5PvcrEYOO6nDB679eqQkG2ftvwdjBqx6nDEdluG1QPaBA5pGjdU0C4pf7KRnctuVaIHLaNnmtmkqClTANi7Zc2XBK2PjWrWmPb_JHCiGfFDtVL53-8JSF6ISxITRMR9hAtWVBJr8PFOdmae9-jnwIh6x4L69g6jv4-mIzJYoJOnzIB3_eMSFZa9yoI86udMljf5VNJinqHDYldEW_Ko2iZGKPIijE9Szo__W5b3oKmHu_9"
                    />
                    <button className="absolute top-[25%] left-[15%] w-4 h-4 bg-red-600 rounded-full ring-2 ring-white animate-pulse" />
                  </div>
                  <div className="flex-1">
                    <h3 className="font-bold mb-2 text-gray-900">Neue Schäden hinzufügen</h3>
                    <p className="text-sm text-gray-500 mb-4">
                      Klicken Sie auf das Schema, um neue Schäden zu markieren.
                    </p>
                    <textarea
                      className="w-full rounded-lg border-gray-300 bg-gray-50 focus:ring-primary focus:border-primary mb-3"
                      placeholder="Beschreibung des neuen Schadens..."
                      rows="3"
                    />
                    <div className="flex items-center justify-center w-full px-4 py-3 border-2 border-dashed rounded-lg border-gray-300 text-center cursor-pointer hover:bg-gray-50">
                      <div className="text-sm text-gray-600">
                        <span className="material-symbols-outlined text-3xl text-primary">
                          upload_file
                        </span>
                        <p>
                          <span className="font-semibold">Fotos hochladen</span> oder hierher ziehen
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Right Column (Summary) */}
            <div className="lg:col-span-1">
              <div className="sticky top-8 bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                <h2 className="text-gray-900 text-[22px] font-bold mb-4">
                  Zusammenfassung & Kosten
                </h2>
                <div className="space-y-4">
                  <div className="flex justify-between items-center text-sm">
                    <p className="text-gray-600">Gefahrene Kilometer</p>
                    <p className="font-medium text-gray-900">-</p>
                  </div>
                  <hr className="border-gray-200" />
                  <h3 className="font-semibold text-base text-gray-900">Zusatzkosten</h3>
                  <div className="space-y-2">
                    <div className="flex justify-between items-center text-sm">
                      <p className="text-gray-600">Auftankgebühr</p>
                      <p className="font-medium text-gray-900">€ 0.00</p>
                    </div>
                    <div className="flex justify-between items-center text-sm">
                      <p className="text-gray-600">Reinigungsgebühr</p>
                      <p className="font-medium text-gray-900">€ 0.00</p>
                    </div>
                    <div className="flex justify-between items-center text-sm">
                      <p className="text-gray-600">Schadenskosten</p>
                      <p className="font-medium text-red-600">€ 0.00</p>
                    </div>
                  </div>
                  <hr className="border-gray-200" />
                  <div className="flex justify-between items-center">
                    <p className="text-lg font-bold text-gray-900">Gesamtsumme</p>
                    <p className="text-xl font-black text-gray-900">€ 0.00</p>
                  </div>
                </div>
                <div className="mt-8 flex flex-col gap-3">
                  <button className="w-full flex min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-11 px-4 bg-primary text-white text-base font-bold hover:opacity-90">
                    <span>Check-out abschließen</span>
                  </button>
                  <button className="w-full flex min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-11 px-4 bg-gray-200 text-gray-700 text-base font-bold hover:bg-gray-300">
                    <span>Abbrechen</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default CheckInOutPage;
