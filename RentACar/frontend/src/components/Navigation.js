/**
 * Navigation - Hauptnavigation mit Stitch-Design
 */

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navigation = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b border-gray-200/80 bg-card-bg/80 backdrop-blur-sm">
      <div className="container mx-auto flex items-center justify-between px-6 py-3">
        <div className="flex items-center gap-4 text-text-main">
          <span className="material-symbols-outlined text-primary text-3xl">directions_car</span>
          <Link to="/">
            <h2 className="text-xl font-bold tracking-tight hover:text-primary transition-colors">
              RENTACAR
            </h2>
          </Link>
        </div>

        <div className="hidden items-center gap-8 md:flex">
          {isAuthenticated && (
            <>
              <Link
                to="/vehicles"
                className="text-sm font-medium hover:text-primary transition-colors"
              >
                Fahrzeuge
              </Link>
              <Link
                to="/my-bookings"
                className="text-sm font-medium hover:text-primary transition-colors"
              >
                Meine Buchungen
              </Link>
            </>
          )}
          <Link to="/about" className="text-sm font-medium hover:text-primary transition-colors">
            Über uns
          </Link>
        </div>

        <div className="flex gap-2 items-center">
          {isAuthenticated && user ? (
            <>
              <span className="hidden md:inline text-sm font-medium">
                {user.firstName} {user.lastName}
              </span>
              <button
                onClick={handleLogout}
                className="flex h-10 min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-primary/10 px-4 text-sm font-bold text-primary transition-colors hover:bg-primary/20"
              >
                <span className="truncate">Abmelden</span>
              </button>
            </>
          ) : (
            <>
              <Link to="/login">
                <button className="flex h-10 min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-primary/10 px-4 text-sm font-bold text-primary transition-colors hover:bg-primary/20">
                  <span className="truncate">Anmelden</span>
                </button>
              </Link>
              <Link to="/register">
                <button className="flex h-10 min-w-[84px] cursor-pointer items-center justify-center overflow-hidden rounded-lg bg-primary px-4 text-sm font-bold text-white transition-opacity hover:opacity-90">
                  <span className="truncate">Registrieren</span>
                </button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navigation;
