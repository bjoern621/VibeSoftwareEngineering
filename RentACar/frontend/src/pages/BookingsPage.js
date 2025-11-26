import React, { useState } from 'react';

/**
 * BookingsPage - Buchungsübersicht für Kunden
 * Konvertiert von Stitch Design: buchungsübersicht_(kunden)_1
 */
const BookingsPage = () => {
  const [activeFilter, setActiveFilter] = useState('Alle');

  // Mock-Daten (später durch API-Call ersetzen)
  const mockBookings = [
    {
      id: 1,
      vehicle: {
        name: 'VW Golf VIII',
        image:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuCPrFJkGTc1ozdkhsvNCpVVl-0zQMFwD24xT3JuencRO_FHeVHm21hDZI4F5smtVP0ofs6aycGPMDyouOlvKIpcDaBPtzoiLYk-mszgbV22eabnPjxISn9tIpjcScKa7EcsrSgs0Lgr960s6SlYIt5x_OMM1qE4gS4-XmpKRWX8wpQK8mEwekZLrrM_AHqFLHPdL-IW6CS87WbIMIRHpXBvrgva4D1tUDoXQYqzOlUSsthnS2FeaoPlOuRsaVJQbUIr1tpHZPqWGg0U',
      },
      pickupDate: '2024-12-15',
      returnDate: '2024-12-18',
      location: 'Frankfurt',
      status: 'CONFIRMED',
      statusLabel: 'Bestätigt',
      totalPrice: 225,
    },
    {
      id: 2,
      vehicle: {
        name: 'BMW X5',
        image:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuDJLh7OQ9CyPQOO6mtKe9a5CxN4W_4rbgKwY-aAY58ve6LgqC1EkLM81q4nOsQ_aRDCTKiGZCZeHsvpY0776X5SlOCMhqN2RyqVclgDHqd5cytl8qKPNIEubfqp4BCq9-bBayj81dIK76xV4HeeFFHbBji_yDElTrmuiK1gLfEaouvAzEKAPHetBsrOHdKQJGgHsNPEm2Rca9wIYNp0Di0y1FpBdtZwkqUrP1BkCKU0CpSB9H1l2KLYDkQ5LsXaeeyteSLqB_M0xtIj',
      },
      pickupDate: '2024-11-01',
      returnDate: '2024-11-05',
      location: 'München',
      status: 'COMPLETED',
      statusLabel: 'Abgeschlossen',
      totalPrice: 440,
    },
    {
      id: 3,
      vehicle: {
        name: 'Mercedes C-Klasse',
        image:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuDHhaAYxeReUWs7jefTT4CYNqTCAaCWVbr-H0pgLdb9IQUq5dDi5BXibTucjS423up6vzyW0SOnG23Cioy2yGfpgRILN3z19qmGjeckA1IYaWTZC7ye3tHl6PursmZJQjRCeTGtB60yr5cKSkQb2lMXuJ2QtoHLzbOrAfpHr3tCMTQ887EakSwFo44uO6BWV_Muk-zT2-CPZj5Nf6r6LicvBVKU-YC4GV6rAsxzDJeCOTgSQOluyZWEpsUnJnHwTfqd-TqiwOW7Cniq',
      },
      pickupDate: '2024-10-10',
      returnDate: '2024-10-12',
      location: 'Berlin',
      status: 'CANCELLED',
      statusLabel: 'Storniert',
      totalPrice: 190,
    },
  ];

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800';
      case 'COMPLETED':
        return 'bg-gray-100 text-gray-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const filteredBookings = mockBookings.filter((booking) => {
    if (activeFilter === 'Alle') return true;
    if (activeFilter === 'Aktiv')
      return booking.status === 'CONFIRMED' || booking.status === 'REQUESTED';
    if (activeFilter === 'Vergangen')
      return booking.status === 'COMPLETED' || booking.status === 'CANCELLED';
    return true;
  });

  return (
    <div className="min-h-screen bg-background-light">
      <main className="flex-grow">
        <div className="container mx-auto px-4 py-8 md:py-12">
          <div className="max-w-5xl mx-auto">
            {/* PageHeading */}
            <div className="flex flex-wrap justify-between items-center gap-4 mb-8">
              <h1 className="text-4xl font-black tracking-tighter">Meine Buchungen</h1>
              <button className="flex items-center gap-2 min-w-[84px] cursor-pointer justify-center overflow-hidden rounded-lg h-10 px-4 bg-secondary text-white text-sm font-bold shadow-sm hover:opacity-90 transition-colors">
                <span className="material-symbols-outlined text-lg">add</span>
                <span>Neue Buchung</span>
              </button>
            </div>

            {/* SegmentedButtons */}
            <div className="mb-8">
              <div className="flex w-full md:w-auto h-12 items-center justify-center rounded-xl bg-gray-200 p-1.5">
                {['Alle', 'Aktiv', 'Vergangen'].map((filter) => (
                  <label
                    key={filter}
                    className={`flex cursor-pointer h-full grow items-center justify-center overflow-hidden rounded-lg px-4 transition-all ${activeFilter === filter ? 'bg-card-bg shadow-md text-primary' : 'text-gray-600'}`}
                  >
                    <span>{filter}</span>
                    <input
                      type="radio"
                      name="booking-filter"
                      value={filter}
                      checked={activeFilter === filter}
                      onChange={() => setActiveFilter(filter)}
                      className="invisible w-0"
                    />
                  </label>
                ))}
              </div>
            </div>

            {/* Bookings List */}
            <div className="space-y-4">
              {filteredBookings.length === 0 ? (
                <div className="text-center py-12">
                  <p className="text-gray-500">Keine Buchungen gefunden.</p>
                </div>
              ) : (
                filteredBookings.map((booking) => (
                  <div
                    key={booking.id}
                    className="bg-card-bg border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow"
                  >
                    <div className="flex flex-col md:flex-row gap-6">
                      {/* Vehicle Image */}
                      <div className="w-full md:w-48 h-32 rounded-lg overflow-hidden flex-shrink-0">
                        <img
                          src={booking.vehicle.image}
                          alt={booking.vehicle.name}
                          className="w-full h-full object-cover"
                        />
                      </div>

                      {/* Booking Details */}
                      <div className="flex-grow">
                        <div className="flex flex-wrap justify-between items-start gap-4 mb-4">
                          <div>
                            <h3 className="text-xl font-bold mb-1">{booking.vehicle.name}</h3>
                            <p className="text-sm text-gray-600">Buchungs-Nr: {booking.id}</p>
                          </div>
                          <span
                            className={`px-3 py-1 rounded-full text-sm font-semibold ${getStatusColor(booking.status)}`}
                          >
                            {booking.statusLabel}
                          </span>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 text-sm">
                          <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-lg">
                              location_on
                            </span>
                            <div>
                              <p className="text-gray-500">Standort</p>
                              <p className="font-medium">{booking.location}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-lg">
                              calendar_today
                            </span>
                            <div>
                              <p className="text-gray-500">Abholdatum</p>
                              <p className="font-medium">{booking.pickupDate}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-primary text-lg">
                              event
                            </span>
                            <div>
                              <p className="text-gray-500">Rückgabedatum</p>
                              <p className="font-medium">{booking.returnDate}</p>
                            </div>
                          </div>
                        </div>

                        <div className="flex flex-wrap justify-between items-center mt-4 pt-4 border-t border-gray-200">
                          <div className="text-lg font-bold text-secondary">
                            {booking.totalPrice},00 €{' '}
                            <span className="text-sm font-normal text-gray-600">Gesamt</span>
                          </div>
                          <div className="flex gap-2">
                            <button className="px-4 py-2 rounded-lg border border-primary text-primary hover:bg-primary/10 transition-colors text-sm font-medium">
                              Details
                            </button>
                            {booking.status === 'CONFIRMED' && (
                              <button className="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors text-sm font-medium">
                                Stornieren
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default BookingsPage;
