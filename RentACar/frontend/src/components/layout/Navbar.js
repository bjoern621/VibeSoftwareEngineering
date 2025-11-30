/**
 * Navbar - Hauptnavigation aus Stitch-Design konvertiert
 * Basiert auf: stitch_rentacar/homepage/fahrzeugsuche_1/code.html
 */

import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <header className="sticky top-0 z-50 w-full border-b border-gray-200/80 bg-card-bg/80 backdrop-blur-sm">
      <div className="container mx-auto flex items-center justify-between px-6 py-3">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-4 text-text-main">
          <span className="material-symbols-outlined text-primary text-3xl">directions_car</span>
          <h2 className="text-xl font-bold tracking-tight">RENTACAR</h2>
        </Link>

        {/* Desktop Navigation */}
        <div className="hidden items-center gap-8 md:flex">
          <Link
            to="/vehicles"
            className={`text-sm font-medium transition-colors ${
              isActive('/vehicles') ? 'text-primary' : 'hover:text-primary'
            }`}
          >
            Fahrzeuge
          </Link>
          <Link
            to="/price-calculator"
            className={`text-sm font-medium transition-colors ${
              isActive('/price-calculator') ? 'text-primary' : 'hover:text-primary'
            }`}
          >
            Preiskalkulator
          </Link>
          {isAuthenticated && (
            <>
              <Link
                to="/bookings"
                className={`text-sm font-medium transition-colors ${
                  isActive('/bookings') ? 'text-primary' : 'hover:text-primary'
                }`}
              >
                Meine Buchungen
              </Link>
              <Link
                to="/profil"
                className={`text-sm font-medium transition-colors ${
                  isActive('/profil') ? 'text-primary' : 'hover:text-primary'
                }`}
              >
                Mein Profil
              </Link>
            </>
          )}
          {/* Mitarbeiter-Navigation */}
          {isAuthenticated && user && (user.role === 'EMPLOYEE' || user.role === 'ADMIN') && (
            <>
              <div className="h-6 w-px bg-gray-300" />
              <Link
                to="/employee/vehicles"
                className={`text-sm font-medium transition-colors ${
                  isActive('/employee/vehicles') ? 'text-primary' : 'hover:text-primary'
                }`}
              >
                Fahrzeugverwaltung
              </Link>
              <Link
                to="/employee/check-in-out"
                className={`text-sm font-medium transition-colors ${
                  isActive('/employee/check-in-out') ? 'text-primary' : 'hover:text-primary'
                }`}
              >
                Check-in/out
              </Link>
            </>
          )}
          <Link
            to="/help"
            className={`text-sm font-medium transition-colors ${
              isActive('/help') ? 'text-primary' : 'hover:text-primary'
            }`}
          >
            Hilfe
          </Link>
        </div>

        {/* Auth Buttons */}
        <div className="flex gap-2 items-center">
          {isAuthenticated && user ? (
            <>
              <span className="hidden md:inline text-sm font-medium mr-2">
                {user.firstName} {user.lastName}
              </span>
              <button
                onClick={logout}
                className="flex h-10 min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-primary/10 px-4 text-sm font-bold text-primary transition-colors hover:bg-primary/20"
              >
                <span className="truncate">Abmelden</span>
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="flex h-10 min-w-[140px] cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-primary px-4 text-sm font-bold text-white transition-opacity hover:opacity-90"
            >
              <span className="truncate">Login/Registrieren</span>
            </Link>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
