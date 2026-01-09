/**
 * Footer - Basis-Footer-Komponente
 */

import React from 'react';
import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-white py-12">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Company Info */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <span className="material-symbols-outlined text-primary text-2xl">
                directions_car
              </span>
              <h3 className="text-lg font-bold">RENTACAR</h3>
            </div>
            <p className="text-gray-400 text-sm">
              Ihr zuverlässiger Partner für Autovermietung in ganz Deutschland.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="font-semibold mb-4">Schnelllinks</h4>
            <ul className="space-y-2">
              <li>
                <Link to="/vehicles" className="text-gray-400 hover:text-primary text-sm">
                  Fahrzeuge
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-gray-400 hover:text-primary text-sm">
                  Über uns
                </Link>
              </li>
              <li>
                <Link to="/bookings" className="text-gray-400 hover:text-primary text-sm">
                  Buchungen
                </Link>
              </li>
            </ul>
          </div>

          {/* Support */}
          <div>
            <h4 className="font-semibold mb-4">Support</h4>
            <ul className="space-y-2">
              <li>
                <Link to="/help" className="text-gray-400 hover:text-primary text-sm">
                  Hilfe
                </Link>
              </li>
              <li>
                <a href="/kontakt" className="text-gray-400 hover:text-primary text-sm">
                  Kontakt
                </a>
              </li>
              <li>
                <a href="/faq" className="text-gray-400 hover:text-primary text-sm">
                  FAQ
                </a>
              </li>
            </ul>
          </div>

          {/* Legal */}
          <div>
            <h4 className="font-semibold mb-4">Rechtliches</h4>
            <ul className="space-y-2">
              <li>
                <a href="/datenschutz" className="text-gray-400 hover:text-primary text-sm">
                  Datenschutz
                </a>
              </li>
              <li>
                <a href="/impressum" className="text-gray-400 hover:text-primary text-sm">
                  Impressum
                </a>
              </li>
              <li>
                <a href="/agb" className="text-gray-400 hover:text-primary text-sm">
                  AGB
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400 text-sm">
          © {new Date().getFullYear()} RENTACAR. Alle Rechte vorbehalten.
        </div>
      </div>
    </footer>
  );
};

export default Footer;
