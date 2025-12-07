/**
 * App - Hauptkomponente mit React Router und Stitch-Design
 */

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { BookingProvider } from './context/BookingContext';
import ErrorBoundary from './components/ErrorBoundary';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import EmailVerificationPage from './pages/EmailVerificationPage';
import AboutPage from './pages/AboutPage';
import VehicleSearchPage from './pages/VehicleSearchPage';
import VehicleDetailsPage from './pages/VehicleDetailsPage';
import BookingsPage from './pages/BookingsPage';
import BookingDetailPage from './pages/BookingDetailPage';
import VehicleManagementPage from './pages/VehicleManagementPage';
import BookingManagementPage from './pages/BookingManagementPage';
import CheckInOutPage from './pages/CheckInOutPage';
import DamageReportsPage from './pages/DamageReportsPage';
import ProfilePage from './pages/ProfilePage';
import PriceCalculatorPage from './pages/PriceCalculatorPage';
import BookingWizardPage from './pages/BookingWizardPage';
import BookingSuccessPage from './pages/BookingSuccessPage';
import NotFoundPage from './pages/NotFoundPage';
import ForbiddenPage from './pages/ForbiddenPage';
import ServerErrorPage from './pages/ServerErrorPage';

// ProtectedRoute Komponente für geschützte Routen
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-gray-600">Lade...</p>
        </div>
      </div>
    );
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

// EmployeeRoute Komponente für Mitarbeiter/Admin-Routen
const EmployeeRoute = ({ children }) => {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-gray-600">Lade...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== 'EMPLOYEE' && user?.role !== 'ADMIN') {
    return <Navigate to="/forbidden" replace />;
  }

  return children;
};

function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <BookingProvider>
          <Router>
            <div className="min-h-screen flex flex-col">
              <Navbar />
              <main className="flex-grow">
                <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<LoginPage />} />
                <Route path="/verify-email" element={<EmailVerificationPage />} />
                <Route path="/about" element={<AboutPage />} />
                <Route path="/price-calculator" element={<PriceCalculatorPage />} />
                <Route path="/vehicles" element={<VehicleSearchPage />} />
                <Route path="/vehicles/:id" element={<VehicleDetailsPage />} />
                <Route path="/booking/wizard" element={<BookingWizardPage />} />
                <Route
                  path="/booking/success/:id"
                  element={
                    <ProtectedRoute>
                      <BookingSuccessPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/bookings"
                  element={
                    <ProtectedRoute>
                      <BookingsPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/bookings/:id"
                  element={
                    <ProtectedRoute>
                      <BookingDetailPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/profil"
                  element={
                    <ProtectedRoute>
                      <ProfilePage />
                    </ProtectedRoute>
                  }
                />
                {/* Mitarbeiter-Seiten (nur für EMPLOYEE/ADMIN) */}
                <Route
                  path="/employee/vehicles"
                  element={
                    <EmployeeRoute>
                      <VehicleManagementPage />
                    </EmployeeRoute>
                  }
                />
                <Route
                  path="/employee/bookings"
                  element={
                    <EmployeeRoute>
                      <BookingManagementPage />
                    </EmployeeRoute>
                  }
                />
                <Route
                  path="/employee/check-in-out"
                  element={
                    <EmployeeRoute>
                      <CheckInOutPage />
                    </EmployeeRoute>
                  }
                />
                <Route
                  path="/employee/damages/:bookingId"
                  element={
                    <EmployeeRoute>
                      <DamageReportsPage />
                    </EmployeeRoute>
                  }
                />
                {/* Fehlerseiten */}
                <Route path="/forbidden" element={<ForbiddenPage />} />
                <Route path="/server-error" element={<ServerErrorPage />} />
                {/* 404 - Muss als letzte Route stehen */}
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </main>
            <Footer />
          </div>
          {/* Toast Notifications */}
          <Toaster
            position="top-right"
            reverseOrder={false}
            toastOptions={{
              duration: 5000,
              style: {
                background: '#fff',
                color: '#363636',
              },
              success: {
                duration: 4000,
                iconTheme: {
                  primary: '#10b981',
                  secondary: '#fff',
                },
              },
              error: {
                duration: 6000,
                iconTheme: {
                  primary: '#ef4444',
                  secondary: '#fff',
                },
              },
            }}
          />
        </Router>
      </BookingProvider>
    </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;
