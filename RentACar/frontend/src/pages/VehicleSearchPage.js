import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import VehicleCard from '../components/vehicles/VehicleCard';

/**
 * VehicleSearchPage - Fahrzeugsuche mit Filter-Sidebar und Ergebnis-Grid
 * Konvertiert von Stitch Design: homepage/fahrzeugsuche_1
 */
const VehicleSearchPage = () => {
  const [searchParams] = useSearchParams();
  const [filters, setFilters] = useState({
    location: searchParams.get('location') || '',
    pickupDate: searchParams.get('pickupDate') || '',
    returnDate: searchParams.get('returnDate') || '',
    vehicleType: searchParams.get('vehicleType') || 'Alle Typen',
    priceRange: 120,
    vehicleTypes: {
      Kleinwagen: false,
      SUV: true,
      Kombi: false,
      Cabrio: false,
    },
    features: {
      Automatik: false,
      Klimaanlage: false,
      Navigationssystem: false,
    },
  });

  // Mock-Daten für Fahrzeuge (später durch API-Call ersetzen)
  const mockVehicles = [
    {
      id: 1,
      name: 'VW Golf',
      image:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuCPrFJkGTc1ozdkhsvNCpVVl-0zQMFwD24xT3JuencRO_FHeVHm21hDZI4F5smtVP0ofs6aycGPMDyouOlvKIpcDaBPtzoiLYk-mszgbV22eabnPjxISn9tIpjcScKa7EcsrSgs0Lgr960s6SlYIt5x_OMM1qE4gS4-XmpKRWX8wpQK8mEwekZLrrM_AHqFLHPdL-IW6CS87WbIMIRHpXBvrgva4D1tUDoXQYqzOlUSsthnS2FeaoPlOuRsaVJQbUIr1tpHZPqWGg0U',
      seats: 5,
      transmission: 'Automatik',
      pricePerDay: 75,
      type: 'Kleinwagen',
    },
    {
      id: 2,
      name: 'Porsche 911',
      image:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuAi0FfTKNpqtvAkN2d2WIFfroa7QOASUTR7i26Y3rjkRXgVAVN1XXoqdmDiW1E3NtJcT0zZ9rqR2qDCv_vGmE9XSaBVVI2i9ymCkIjp9gBbt36LYCIkyQMXVPgLxwtwFSHPRzsUewXplYvI4uJVEW3qFHBMY0gu6h864zZ9ZyB-HUKi2eE890O1aMxiER6NdywCBvp6vSlX-YOF_Y3tisQlN_tCitxb1vSGJuM3fnDq65XIHpJw5fOWoAqq6BKibJEI0Vn1tI1RRAnx',
      seats: 2,
      transmission: 'Automatik',
      pricePerDay: 220,
      type: 'Sportwagen',
    },
    {
      id: 3,
      name: 'BMW X5',
      image:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuDJLh7OQ9CyPQOO6mtKe9a5CxN4W_4rbgKwY-aAY58ve6LgqC1EkLM81q4nOsQ_aRDCTKiGZCZeHsvpY0776X5SlOCMhqN2RyqVclgDHqd5cytl8qKPNIEubfqp4BCq9-bBayj81dIK76xV4HeeFFHbBji_yDElTrmuiK1gLfEaouvAzEKAPHetBsrOHdKQJGgHsNPEm2Rca9wIYNp0Di0y1FpBdtZwkqUrP1BkCKU0CpSB9H1l2KLYDkQ5LsXaeeyteSLqB_M0xtIj',
      seats: 5,
      transmission: 'Automatik',
      pricePerDay: 110,
      type: 'SUV',
    },
    {
      id: 4,
      name: 'Mercedes C-Klasse',
      image:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuDHhaAYxeReUWs7jefTT4CYNqTCAaCWVbr-H0pgLdb9IQUq5dDi5BXibTucjS423up6vzyW0SOnG23Cioy2yGfpgRILN3z19qmGjeckA1IYaWTZC7ye3tHl6PursmZJQjRCeTGtB60yr5cKSkQb2lMXuJ2QtoHLzbOrAfpHr3tCMTQ887EakSwFo44uO6BWV_Muk-zT2-CPZj5Nf6r6LicvBVKU-YC4GV6rAsxzDJeCOTgSQOluyZWEpsUnJnHwTfqd-TqiwOW7Cniq',
      seats: 5,
      transmission: 'Automatik',
      pricePerDay: 95,
      type: 'Limousine',
    },
  ];

  const handleFilterChange = (filterType, key, value) => {
    if (filterType === 'vehicleTypes' || filterType === 'features') {
      setFilters((prev) => ({
        ...prev,
        [filterType]: {
          ...prev[filterType],
          [key]: value,
        },
      }));
    } else {
      setFilters((prev) => ({
        ...prev,
        [key]: value,
      }));
    }
  };

  const handleResetFilters = () => {
    setFilters({
      ...filters,
      priceRange: 120,
      vehicleTypes: {
        Kleinwagen: false,
        SUV: false,
        Kombi: false,
        Cabrio: false,
      },
      features: {
        Automatik: false,
        Klimaanlage: false,
        Navigationssystem: false,
      },
    });
  };

  return (
    <div className="min-h-screen bg-background-light">
      {/* Results Section */}
      <section className="container mx-auto px-6 py-10">
        <div className="flex flex-col gap-8 lg:flex-row">
          {/* Filter Sidebar */}
          <aside className="w-full lg:w-1/4 xl:w-1/5">
            <div className="sticky top-24 rounded-xl border border-gray-200 bg-card-bg p-6 shadow-sm">
              <div className="flex items-center justify-between pb-4 border-b">
                <h3 className="text-lg font-bold">Filter</h3>
                <button
                  onClick={handleResetFilters}
                  className="text-sm font-medium text-primary hover:underline"
                >
                  Zurücksetzen
                </button>
              </div>
              <div className="space-y-6 pt-6">
                {/* Vehicle Type Filter */}
                <div>
                  <h4 className="mb-3 font-semibold">Fahrzeugtyp</h4>
                  <div className="space-y-2">
                    {Object.keys(filters.vehicleTypes).map((type) => (
                      <label key={type} className="flex items-center">
                        <input
                          type="checkbox"
                          checked={filters.vehicleTypes[type]}
                          onChange={(e) =>
                            handleFilterChange('vehicleTypes', type, e.target.checked)
                          }
                          className="form-checkbox h-5 w-5 rounded text-primary focus:ring-primary"
                        />
                        <span className="ml-3 text-sm">{type}</span>
                      </label>
                    ))}
                  </div>
                </div>
                {/* Price Range Filter */}
                <div>
                  <h4 className="mb-3 font-semibold">Preis pro Tag</h4>
                  <input
                    type="range"
                    min="20"
                    max="250"
                    value={filters.priceRange}
                    onChange={(e) => handleFilterChange('priceRange', 'priceRange', e.target.value)}
                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-primary"
                  />
                  <div className="flex justify-between text-sm text-gray-500 mt-2">
                    <span>20€</span>
                    <span>250€</span>
                  </div>
                </div>
                {/* Features Filter */}
                <div>
                  <h4 className="mb-3 font-semibold">Ausstattung</h4>
                  <div className="space-y-2">
                    {Object.keys(filters.features).map((feature) => (
                      <label key={feature} className="flex items-center">
                        <input
                          type="checkbox"
                          checked={filters.features[feature]}
                          onChange={(e) =>
                            handleFilterChange('features', feature, e.target.checked)
                          }
                          className="form-checkbox h-5 w-5 rounded text-primary focus:ring-primary"
                        />
                        <span className="ml-3 text-sm">{feature}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </aside>

          {/* Results Grid */}
          <div className="w-full lg:w-3/4 xl:w-4/5">
            <div className="mb-6">
              <h2 className="text-2xl font-bold">{mockVehicles.length} Fahrzeuge gefunden</h2>
            </div>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
              {mockVehicles.map((vehicle) => (
                <VehicleCard key={vehicle.id} vehicle={vehicle} />
              ))}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default VehicleSearchPage;
