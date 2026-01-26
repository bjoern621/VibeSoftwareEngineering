import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import UserProfileData from '../components/profile/UserProfileData';
import OrderHistorySection from '../components/profile/OrderHistorySection';

/**
 * UserProfilePage Component
 * 
 * Main user profile page with tab navigation between
 * personal data and order history sections.
 * 
 * Route: /profile (Protected)
 * 
 * @returns {React.ReactElement}
 */
const UserProfilePage = () => {
  const { user, logout } = useAuth();
  const [activeSection, setActiveSection] = useState('orders'); // 'profile' or 'orders'

  /**
   * Handle logout
   */
  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div className="min-h-screen bg-background-light dark:bg-background-dark">
      {/* Top Navigation */}
      <header className="sticky top-0 z-50 w-full border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-card-dark shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 items-center justify-between">
            {/* Logo & Brand */}
            <Link to="/concerts" className="flex items-center gap-3">
              <div className="size-8 text-primary">
                <svg className="w-full h-full" fill="none" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M24 45.8096C19.6865 45.8096 15.4698 44.5305 11.8832 42.134C8.29667 39.7376 5.50128 36.3314 3.85056 32.3462C2.19985 28.361 1.76794 23.9758 2.60947 19.7452C3.451 15.5145 5.52816 11.6284 8.57829 8.5783C11.6284 5.52817 15.5145 3.45101 19.7452 2.60948C23.9758 1.76795 28.361 2.19986 32.3462 3.85057C36.3314 5.50129 39.7376 8.29668 42.134 11.8833C44.5305 15.4698 45.8096 19.6865 45.8096 24L24 24L24 45.8096Z"
                    fill="currentColor"
                  />
                </svg>
              </div>
              <h2 className="text-xl font-bold tracking-tight text-text-primary dark:text-white">
                Concert Comparison
              </h2>
            </Link>

            {/* Desktop Nav Links */}
            <nav className="hidden md:flex items-center gap-6">
              <Link
                to="/concerts"
                className="text-sm font-medium text-text-secondary hover:text-primary dark:text-gray-300 dark:hover:text-primary transition-colors"
              >
                Konzerte
              </Link>
              <Link
                to="/profile"
                className="text-sm font-medium text-primary dark:text-blue-400"
              >
                Mein Profil
              </Link>
              {authService.getUserRole() === 'ADMIN' && (
                <Link
                  to="/admin"
                  className="text-sm font-medium text-text-secondary hover:text-primary dark:text-gray-300 dark:hover:text-primary transition-colors flex items-center gap-1"
                >
                  <span className="material-symbols-outlined text-lg">admin_panel_settings</span>
                  Admin
                </Link>
              )}
            </nav>

            {/* Right Side: Search & Profile */}
            <div className="flex items-center gap-4">
              {/* User Info */}
              <div className="hidden sm:flex items-center gap-2 text-sm">
                <span className="text-text-secondary dark:text-gray-400">
                  {user?.email || 'User'}
                </span>
              </div>
              
              {/* Profile Avatar / Logout */}
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                title="Abmelden"
              >
                <span className="material-symbols-outlined text-lg">logout</span>
                <span className="hidden sm:inline">Abmelden</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Layout */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Left Sidebar */}
          <aside className="w-full lg:w-72 flex-shrink-0">
            <div className="bg-white dark:bg-card-dark rounded-xl shadow-sm border border-gray-200 dark:border-gray-800 overflow-hidden sticky top-24">
              {/* Profile Summary */}
              <div className="p-6 border-b border-gray-100 dark:border-gray-700 flex flex-col items-center text-center">
                {/* Avatar */}
                <div className="bg-gradient-to-br from-primary to-blue-600 rounded-full size-24 mb-4 ring-4 ring-gray-50 dark:ring-gray-800 flex items-center justify-center text-white text-3xl font-bold">
                  {user?.firstName?.[0] || user?.email?.[0]?.toUpperCase() || 'U'}
                </div>
                <h1 className="text-lg font-semibold text-text-primary dark:text-white">
                  {user?.firstName && user?.lastName
                    ? `${user.firstName} ${user.lastName}`
                    : user?.email || 'Benutzer'}
                </h1>
                <p className="text-sm text-text-secondary dark:text-gray-400 mt-1">
                  Mitglied seit {new Date().getFullYear()}
                </p>
              </div>

              {/* Vertical Navigation */}
              <nav className="p-3 flex flex-col gap-1">
                <button
                  onClick={() => setActiveSection('profile')}
                  className={`
                    flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group text-left
                    ${activeSection === 'profile'
                      ? 'bg-primary/10 text-primary'
                      : 'text-text-secondary hover:bg-gray-50 dark:text-gray-300 dark:hover:bg-gray-800'
                    }
                  `}
                >
                  <span className={`material-symbols-outlined text-[22px] ${activeSection === 'profile' ? 'filled' : ''}`}>
                    person
                  </span>
                  <span className="text-sm font-medium">Mein Profil</span>
                </button>

                <button
                  onClick={() => setActiveSection('orders')}
                  className={`
                    flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors group text-left
                    ${activeSection === 'orders'
                      ? 'bg-primary/10 text-primary'
                      : 'text-text-secondary hover:bg-gray-50 dark:text-gray-300 dark:hover:bg-gray-800'
                    }
                  `}
                >
                  <span className={`material-symbols-outlined text-[22px] ${activeSection === 'orders' ? 'filled' : ''} group-hover:text-primary transition-colors`}>
                    shopping_bag
                  </span>
                  <span className="text-sm font-medium">Meine Bestellungen</span>
                </button>

                {/* Divider */}
                <div className="h-px bg-gray-100 dark:bg-gray-700 my-2 mx-3" />

                {/* Logout Button */}
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20 transition-colors group text-left"
                >
                  <span className="material-symbols-outlined text-[22px]">logout</span>
                  <span className="text-sm font-medium">Abmelden</span>
                </button>
              </nav>
            </div>
          </aside>

          {/* Right Content */}
          <div className="flex-1">
            <div className="bg-white dark:bg-card-dark rounded-xl shadow-sm border border-gray-200 dark:border-gray-800 p-6 sm:p-8">
              {/* Render active section */}
              {activeSection === 'profile' && <UserProfileData />}
              {activeSection === 'orders' && <OrderHistorySection />}
            </div>
          </div>
        </div>
      </main>

      {/* Simple Footer */}
      <footer className="mt-auto border-t border-gray-200 dark:border-gray-800 bg-white dark:bg-card-dark py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm text-text-secondary dark:text-gray-400">
            © 2024 Concert Comparison. Alle Rechte vorbehalten.
          </p>
          <div className="flex gap-6">
            <Link to="/terms" className="text-sm text-text-secondary hover:text-primary dark:text-gray-400 dark:hover:text-primary">
              AGB
            </Link>
            <Link to="/privacy" className="text-sm text-text-secondary hover:text-primary dark:text-gray-400 dark:hover:text-primary">
              Datenschutz
            </Link>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default UserProfilePage;
