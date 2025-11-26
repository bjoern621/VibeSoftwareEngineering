/**
 * HomePage - Startseite mit Hero und Fahrzeugsuche
 * Basiert auf: stitch_rentacar/homepage/fahrzeugsuche_1/code.html
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const HomePage = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [searchParams, setSearchParams] = useState({
    location: '',
    pickupDate: new Date().toISOString().split('T')[0],
    returnDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    vehicleType: 'Alle Typen',
  });

  const handleSearch = (e) => {
    e.preventDefault();
    navigate(
      `/vehicles?location=${searchParams.location}&pickup=${searchParams.pickupDate}&return=${searchParams.returnDate}&type=${searchParams.vehicleType}`
    );
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchParams((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section
        className="relative flex min-h-[480px] w-full items-center justify-center bg-cover bg-center py-12"
        style={{
          backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.6)), url('https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=1600')`,
        }}
      >
        <div className="container mx-auto flex flex-col items-center gap-8 px-6 text-center">
          <div className="flex flex-col gap-2">
            <h1 className="text-4xl font-extrabold text-white sm:text-5xl">
              Finden Sie Ihr perfektes Mietauto
            </h1>
            <h2 className="text-base font-normal text-white/90">
              Buchen Sie schnell und einfach aus unserer großen Auswahl an Fahrzeugen.
            </h2>
          </div>

          {/* Search Form */}
          <div className="w-full max-w-4xl rounded-xl bg-card-bg/90 p-4 shadow-2xl backdrop-blur-sm">
            <form
              onSubmit={handleSearch}
              className="grid grid-cols-1 items-end gap-4 md:grid-cols-4 lg:grid-cols-10"
            >
              {/* Location */}
              <label className="flex flex-col text-left md:col-span-2 lg:col-span-3">
                <p className="pb-2 text-sm font-medium text-text-main">Standort</p>
                <div className="relative flex w-full items-stretch">
                  <span className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                    location_on
                  </span>
                  <input
                    className="form-input h-14 w-full rounded-lg border-gray-300 bg-gray-50 pl-10 text-text-main placeholder:text-gray-400 focus:border-primary focus:ring-primary"
                    placeholder="Stadt oder Flughafen"
                    type="text"
                    name="location"
                    value={searchParams.location}
                    onChange={handleInputChange}
                  />
                </div>
              </label>

              {/* Pickup Date */}
              <label className="flex flex-col text-left lg:col-span-2">
                <p className="pb-2 text-sm font-medium text-text-main">Abholdatum</p>
                <div className="relative">
                  <span className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                    calendar_today
                  </span>
                  <input
                    className="form-input h-14 w-full rounded-lg border-gray-300 bg-gray-50 pl-10 text-text-main focus:border-primary focus:ring-primary"
                    type="date"
                    name="pickupDate"
                    value={searchParams.pickupDate}
                    onChange={handleInputChange}
                  />
                </div>
              </label>

              {/* Return Date */}
              <label className="flex flex-col text-left lg:col-span-2">
                <p className="pb-2 text-sm font-medium text-text-main">Rückgabedatum</p>
                <div className="relative">
                  <span className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                    event
                  </span>
                  <input
                    className="form-input h-14 w-full rounded-lg border-gray-300 bg-gray-50 pl-10 text-text-main focus:border-primary focus:ring-primary"
                    type="date"
                    name="returnDate"
                    value={searchParams.returnDate}
                    onChange={handleInputChange}
                  />
                </div>
              </label>

              {/* Vehicle Type */}
              <label className="flex flex-col text-left md:col-span-2 lg:col-span-2">
                <p className="pb-2 text-sm font-medium text-text-main">Fahrzeugtyp</p>
                <select
                  className="form-select h-14 w-full rounded-lg border-gray-300 bg-gray-50 text-text-main focus:border-primary focus:ring-primary"
                  name="vehicleType"
                  value={searchParams.vehicleType}
                  onChange={handleInputChange}
                >
                  <option>Alle Typen</option>
                  <option>Kleinwagen</option>
                  <option>SUV</option>
                  <option>Kombi</option>
                  <option>Sportwagen</option>
                </select>
              </label>

              {/* Search Button */}
              <button
                type="submit"
                className="h-14 w-full rounded-lg bg-primary text-base font-bold text-white transition-opacity hover:opacity-90 md:col-span-2 lg:col-span-1"
              >
                Suchen
              </button>
            </form>
          </div>
        </div>
      </section>

      {/* Welcome Section */}
      {isAuthenticated && user && (
        <section className="bg-white py-8">
          <div className="container mx-auto px-6">
            <div className="rounded-xl bg-primary/5 p-6">
              <h3 className="text-2xl font-bold text-text-main">
                Willkommen zurück, {user.firstName}!
              </h3>
              <p className="mt-2 text-gray-600">Bereit für Ihre nächste Reise?</p>
            </div>
          </div>
        </section>
      )}

      {/* Features Section */}
      <section className="bg-background-light py-16">
        <div className="container mx-auto px-6">
          <h2 className="mb-12 text-center text-3xl font-bold">Warum RENTACAR?</h2>
          <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
            <div className="rounded-xl bg-card-bg p-6 shadow-sm">
              <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                <span className="material-symbols-outlined text-primary text-2xl">verified</span>
              </div>
              <h3 className="mb-2 text-xl font-bold">Geprüfte Fahrzeuge</h3>
              <p className="text-gray-600">
                Alle unsere Fahrzeuge werden regelmäßig gewartet und geprüft.
              </p>
            </div>

            <div className="rounded-xl bg-card-bg p-6 shadow-sm">
              <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                <span className="material-symbols-outlined text-primary text-2xl">price_check</span>
              </div>
              <h3 className="mb-2 text-xl font-bold">Faire Preise</h3>
              <p className="text-gray-600">Transparente Preisgestaltung ohne versteckte Kosten.</p>
            </div>

            <div className="rounded-xl bg-card-bg p-6 shadow-sm">
              <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                <span className="material-symbols-outlined text-primary text-2xl">
                  support_agent
                </span>
              </div>
              <h3 className="mb-2 text-xl font-bold">24/7 Support</h3>
              <p className="text-gray-600">
                Unser Kundenservice ist rund um die Uhr für Sie erreichbar.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
