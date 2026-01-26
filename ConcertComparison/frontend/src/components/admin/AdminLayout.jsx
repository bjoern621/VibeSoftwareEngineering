import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/**
 * AdminLayout - Layout wrapper for admin pages
 * Includes sidebar navigation and main content area
 */
const AdminLayout = ({ children }) => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    {
      to: '/admin',
      icon: 'dashboard',
      label: 'Dashboard',
      end: true,
    },
    {
      to: '/admin/concerts/new',
      icon: 'add_circle',
      label: 'Neues Konzert',
    },
  ];

  return (
    <div className="min-h-screen bg-background-light flex">
      {/* Sidebar */}
      <aside
        className={`${
          sidebarOpen ? 'w-64' : 'w-20'
        } bg-card-dark text-white transition-all duration-300 flex flex-col`}
      >
        {/* Sidebar Header */}
        <div className="p-4 border-b border-border-dark flex items-center justify-between">
          {sidebarOpen && (
            <h1 className="text-xl font-bold text-primary">Admin Panel</h1>
          )}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 rounded-lg hover:bg-background-dark transition-colors"
            aria-label={sidebarOpen ? 'Sidebar einklappen' : 'Sidebar ausklappen'}
          >
            <span className="material-symbols-outlined">
              {sidebarOpen ? 'chevron_left' : 'chevron_right'}
            </span>
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-4">
          <ul className="space-y-1">
            {navItems.map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end={item.end}
                  className={({ isActive }) =>
                    `flex items-center px-4 py-3 mx-2 rounded-lg transition-colors ${
                      isActive
                        ? 'bg-primary text-white'
                        : 'text-gray-300 hover:bg-background-dark hover:text-white'
                    }`
                  }
                >
                  <span className="material-symbols-outlined">{item.icon}</span>
                  {sidebarOpen && <span className="ml-3">{item.label}</span>}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        {/* Sidebar Footer */}
        <div className="p-4 border-t border-border-dark">
          <NavLink
            to="/concerts"
            className="flex items-center px-4 py-3 mx-2 rounded-lg text-gray-300 hover:bg-background-dark hover:text-white transition-colors"
          >
            <span className="material-symbols-outlined">arrow_back</span>
            {sidebarOpen && <span className="ml-3">Zurück zur Website</span>}
          </NavLink>
          <button
            onClick={handleLogout}
            className="w-full flex items-center px-4 py-3 mx-2 rounded-lg text-gray-300 hover:bg-red-900 hover:text-white transition-colors"
          >
            <span className="material-symbols-outlined">logout</span>
            {sidebarOpen && <span className="ml-3">Abmelden</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col">
        {/* Top Bar */}
        <header className="bg-white shadow-sm border-b border-border-light px-6 py-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-text-primary">
              Concert Comparison - Administration
            </h2>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-text-secondary">
                Angemeldet als Administrator
              </span>
              <div className="w-10 h-10 bg-primary rounded-full flex items-center justify-center">
                <span className="material-symbols-outlined text-white">
                  admin_panel_settings
                </span>
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 p-6 overflow-auto">{children}</div>
      </main>
    </div>
  );
};

export default AdminLayout;
