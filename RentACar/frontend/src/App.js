/**
 * App - Hauptkomponente mit React Router und Stitch-Design
 */

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { BookingProvider } from './context/BookingContext';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import EmailVerificationPage from './pages/EmailVerificationPage';
import AboutPage from './pages/AboutPage';
import VehicleSearchPage from './pages/VehicleSearchPage';
import VehicleDetailsPage from './pages/VehicleDetailsPage';
import BookingsPage from './pages/BookingsPage';
import VehicleManagementPage from './pages/VehicleManagementPage';
import CheckInOutPage from './pages/CheckInOutPage';
import DamageReportsPage from './pages/DamageReportsPage';
import ProfilePage from './pages/ProfilePage';
import PriceCalculatorPage from './pages/PriceCalculatorPage';
import BookingWizardPage from './pages/BookingWizardPage';
import BookingSuccessPage from './pages/BookingSuccessPage';

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

function App() {
  return (
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
                  path="/profil"
                  element={
                    <ProtectedRoute>
                      <ProfilePage />
                    </ProtectedRoute>
                  }
                />
                {/* Mitarbeiter-Seiten (nur Design, keine Funktion) */}
                <Route path="/employee/vehicles" element={<VehicleManagementPage />} />
                <Route path="/employee/check-in-out" element={<CheckInOutPage />} />
                <Route path="/employee/damage-reports/:bookingId" element={<DamageReportsPage />} />
              </Routes>
            </main>
            <Footer />
          </div>
        </Router>
      </BookingProvider>
    </AuthProvider>
  );
}

export default App;
