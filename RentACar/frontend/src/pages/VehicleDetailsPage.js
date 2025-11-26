import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';

/**
 * VehicleDetailsPage - Detailansicht eines Fahrzeugs mit Galerie und Buchungsformular
 * Konvertiert von Stitch Design: fahrzeugdetailseite_1
 */
const VehicleDetailsPage = () => {
  const { id } = useParams();
  const [activeImageIndex, setActiveImageIndex] = useState(0);
  const [activeTab, setActiveTab] = useState('equipment');

  // Mock-Daten (später durch API-Call ersetzen)
  const vehicle = {
    id: parseInt(id),
    name: 'VW Golf VIII',
    description: 'Der perfekte Begleiter für Stadt und Land.',
    images: [
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDU12wvRMHl6TUqLdJasWMcQVHRUR3FT4hbR6oAa9rxvpNXuuLBVqCzF3r1WvhHajorM7PF8MNCRBJ8S_-hNeMb-yicZBpFXKWuhUbiBHDRD8c8mvhavETQCjWfosOVaGgRNVkTQr2kr1KjpM-VpMm-p-7KwIJyaF4YWFncZFkVlJa8Efidrn_Wef4O1fkop1JILvQrSWoeDt8X7hvgw3XOc4NRx_GKxzgWTu0eAM4LsjpjI8uTCmc2p5-klCZ-_URbRbT4eELv3R6d',
      'https://lh3.googleusercontent.com/aida-public/AB6AXuAbva13igLVYQ_WzxOeURt8PthTRZKRnytz3G7oCcce5Ve5INN8FfaJmof-NYjVNrRS_CQ3ds5TbhxLzu-7P3brTG3Nr-3BZR7trq12R9O6gJhoh5BGazGsUKWozdkgUpWfBdB4iQfRI0FC1A5M-aPkQ6fJxLBudE5jBe-sfNWPFn23G-tdd4rR7qk2GvU652HccilCrnVf6EM2Nx5XcsW9M0ZAlVADXWH6RaIJZqUJOzf_AwThRzId13oF8_jVk-bY4cWAzoEzCGMp',
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDiW6G0GCq3ueYbK1nZQ-pxxdhYJDZY8dmx1XvsMD9QOomyyeAtxTLnod08QjqCoP1Tru4zJaZL2j0w1tD8wFdP5gbtrM3me9KmMdkAO8Ay9j5iDheoMi7_YYha8FyoRwtM74b2BvmC8tNLafXvyOl_4J7ABC7smRq7V3LdsWME7VvOXLDGUzF6h5T7LsnvCEWfSBgPam2jsu60OV-M0z7KBOUpCPyeKw2Ohak8GPvYEX4-6UN1b9XBS881Gt7CCQ2A0cgIbIC7vciR',
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDRpobOQGD59XldKnMXV4kq-2phcCDC4RWNbyVjfR2TYcWCE8K-V6haYL-VV414AOeLAb_Jkwr5bEpVHNIRThAWgU5okoJ4NBgYLtwa0GsAiVFQu8Pm0OViSopK4R3ezUi5m9VC-75xnxd9QWd3R44GYJf2G6uU0cbfLpOPBE7Pj4s3i7K7MsDXSZUGWrd5uAW5aCG-JeKWFwvGePUorCM8CGQXsT3C_Vrs9WoZx1htlXxoQZZ0flpI5uDsTWUiEYUKl7q9_w2aG2Lo',
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDL9aPNYVWqskx6d2LsliPLtLfy72eZAVxU6MyoQHgMu6FeFGFLLO3XaaTT0-JdckYWye6o69_cf-EaskMccyn1Do7zL8pGIVn2LREH8KkONTDcBiK627qyJbigo30CmoERULpzh0PQXSjiWRfYdmUR71GGYNHh_M0uNa_HBziqox3_tdn7Z1neeMzAAVc9RYnhcV2IFgBw6OLOviWcb3cfyYt67qiXn6ab2sYWd-h9A2-nBFnfkctfJA80nTzsFZU2rJGocWvZwUgi',
    ],
    specs: [
      { icon: 'group', label: '5 Sitze', sublabel: 'Sitzplätze' },
      { icon: 'settings', label: 'Automatik', sublabel: 'Getriebe' },
      { icon: 'local_gas_station', label: 'Benzin', sublabel: 'Kraftstoff' },
      { icon: 'directions_car', label: 'Kompaktklasse', sublabel: 'Fahrzeugtyp' },
    ],
    equipment: [
      'Klimaanlage',
      'Navigationssystem',
      'Bluetooth',
      'Einparkhilfe',
      'Apple CarPlay & Android Auto',
      'Tempomat',
      'Sitzheizung',
      'USB-Anschluss',
    ],
    pricePerDay: 79,
  };

  return (
    <div className="min-h-screen bg-background-light">
      <main className="px-4 md:px-10 lg:px-20 xl:px-40 flex flex-1 justify-center py-5">
        <div className="flex flex-col w-full max-w-6xl flex-1">
          {/* Breadcrumb */}
          <div className="flex flex-wrap gap-2 p-4">
            <Link to="/" className="text-sm font-medium text-text-main/70 hover:text-primary">
              Home
            </Link>
            <span className="text-sm font-medium text-text-main/70">/</span>
            <Link
              to="/vehicles"
              className="text-sm font-medium text-text-main/70 hover:text-primary"
            >
              Fahrzeuge
            </Link>
            <span className="text-sm font-medium text-text-main/70">/</span>
            <span className="text-sm font-medium text-text-main">{vehicle.name}</span>
          </div>

          {/* Image Gallery */}
          <div className="px-4 py-3">
            <div
              className="bg-cover bg-center flex flex-col justify-end overflow-hidden bg-gray-300 rounded-xl min-h-80 md:min-h-[450px]"
              style={{
                backgroundImage: `linear-gradient(0deg, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0) 25%), url("${vehicle.images[activeImageIndex]}")`,
              }}
            >
              <div className="flex justify-center gap-2 p-5">
                {vehicle.images.map((_, index) => (
                  <div
                    key={index}
                    className={`size-2 rounded-full ${index === activeImageIndex ? 'bg-white' : 'bg-white opacity-50'} cursor-pointer`}
                    onClick={() => setActiveImageIndex(index)}
                  />
                ))}
              </div>
            </div>
            <div className="mt-2 grid grid-cols-5 gap-2">
              {vehicle.images.map((img, index) => (
                <div
                  key={index}
                  className={`bg-cover bg-center aspect-video rounded-lg cursor-pointer ${index === activeImageIndex ? 'border-2 border-primary' : 'opacity-70 hover:opacity-100'}`}
                  style={{ backgroundImage: `url("${img}")` }}
                  onClick={() => setActiveImageIndex(index)}
                />
              ))}
            </div>
          </div>

          {/* Content */}
          <div className="flex flex-col lg:flex-row gap-8 mt-6">
            {/* Left Column: Details */}
            <div className="w-full lg:w-2/3">
              <div className="flex flex-wrap justify-between gap-3 p-4">
                <div className="flex min-w-72 flex-col gap-3">
                  <p className="text-4xl font-black leading-tight">{vehicle.name}</p>
                  <p className="text-base font-normal text-gray-600">{vehicle.description}</p>
                </div>
              </div>

              {/* Specs Grid */}
              <div className="grid grid-cols-[repeat(auto-fit,minmax(158px,1fr))] gap-3 p-4">
                {vehicle.specs.map((spec, index) => (
                  <div
                    key={index}
                    className="flex flex-1 gap-3 rounded-lg border border-gray-200 bg-gray-50 p-4 flex-col"
                  >
                    <span className="material-symbols-outlined text-primary text-2xl">
                      {spec.icon}
                    </span>
                    <div className="flex flex-col gap-1">
                      <h2 className="text-base font-bold">{spec.label}</h2>
                      <p className="text-sm font-normal text-gray-600">{spec.sublabel}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* Tabs */}
              <div className="p-4 mt-4">
                <div className="border-b border-gray-200">
                  <nav className="-mb-px flex space-x-8">
                    <button
                      onClick={() => setActiveTab('equipment')}
                      className={`whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'equipment' ? 'text-primary border-primary' : 'text-gray-500 hover:text-gray-700 hover:border-gray-300 border-transparent'}`}
                    >
                      Ausstattung
                    </button>
                    <button
                      onClick={() => setActiveTab('specs')}
                      className={`whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'specs' ? 'text-primary border-primary' : 'text-gray-500 hover:text-gray-700 hover:border-gray-300 border-transparent'}`}
                    >
                      Technische Daten
                    </button>
                    <button
                      onClick={() => setActiveTab('conditions')}
                      className={`whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'conditions' ? 'text-primary border-primary' : 'text-gray-500 hover:text-gray-700 hover:border-gray-300 border-transparent'}`}
                    >
                      Mietbedingungen
                    </button>
                  </nav>
                </div>
                <div className="pt-6">
                  {activeTab === 'equipment' && (
                    <ul className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4 text-sm text-gray-700">
                      {vehicle.equipment.map((item, index) => (
                        <li key={index} className="flex items-center gap-3">
                          <span className="material-symbols-outlined text-green-600">
                            check_circle
                          </span>
                          {item}
                        </li>
                      ))}
                    </ul>
                  )}
                  {activeTab === 'specs' && (
                    <div className="text-sm text-gray-700">
                      <p>Technische Daten werden vom Backend geladen...</p>
                    </div>
                  )}
                  {activeTab === 'conditions' && (
                    <div className="text-sm text-gray-700">
                      <p>Mietbedingungen werden vom Backend geladen...</p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Right Column: Booking */}
            <div className="w-full lg:w-1/3 p-4">
              <div className="bg-gray-50 border border-gray-200 rounded-xl p-6 shadow-lg sticky top-24">
                <h3 className="text-xl font-bold mb-4">Fahrzeug mieten</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="pickup-date" className="block text-sm font-medium mb-1">
                      Abholdatum
                    </label>
                    <input
                      type="date"
                      id="pickup-date"
                      className="w-full rounded-lg border-gray-300 bg-white text-sm focus:border-primary focus:ring-primary"
                    />
                  </div>
                  <div>
                    <label htmlFor="return-date" className="block text-sm font-medium mb-1">
                      Rückgabedatum
                    </label>
                    <input
                      type="date"
                      id="return-date"
                      className="w-full rounded-lg border-gray-300 bg-white text-sm focus:border-primary focus:ring-primary"
                    />
                  </div>
                </div>
                <div className="mt-6 border-t border-gray-200 pt-6 space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Preis pro Tag</span>
                    <span>{vehicle.pricePerDay},00 €</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Mietdauer</span>
                    <span>3 Tage</span>
                  </div>
                  <div className="flex justify-between font-bold text-lg border-t border-gray-200 pt-3 mt-3">
                    <span>Gesamtpreis</span>
                    <span>{vehicle.pricePerDay * 3},00 €</span>
                  </div>
                  <p className="text-xs text-center text-gray-500">inkl. MwSt.</p>
                </div>
                <button className="mt-6 w-full flex items-center justify-center overflow-hidden rounded-lg h-12 px-6 bg-secondary text-white text-base font-bold tracking-wide hover:opacity-90 shadow-md">
                  <span>Jetzt buchen</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default VehicleDetailsPage;
