/**
 * AboutPage - Über uns Seite
 */

import React from 'react';

const AboutPage = () => {
  return (
    <div className="min-h-screen bg-background-light">
      <div className="container mx-auto px-6 py-16">
        <h1 className="text-4xl font-bold mb-8">Über RENTACAR</h1>
        <div className="prose prose-lg max-w-none">
          <p className="text-lg text-gray-700 mb-6">
            RENTACAR ist ein modernes Fahrzeugvermietungssystem mit mehreren Standorten in ganz
            Deutschland.
          </p>
          <h2 className="text-2xl font-bold mt-8 mb-4">Unsere Mission</h2>
          <p className="text-gray-700 mb-6">
            Wir bieten flexible, zuverlässige und kundenfreundliche Autovermietung für alle Ihre
            Mobilitätsbedürfnisse.
          </p>
          <h2 className="text-2xl font-bold mt-8 mb-4">Features</h2>
          <ul className="list-disc list-inside space-y-2 text-gray-700">
            <li>Online-Buchungssystem</li>
            <li>Flexible Mietzeiten</li>
            <li>Verschiedene Fahrzeugkategorien</li>
            <li>Mehrere Standorte</li>
            <li>24/7 Support</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default AboutPage;
