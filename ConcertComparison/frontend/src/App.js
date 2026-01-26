import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import AdminRoute from './components/auth/AdminRoute';
import ConcertDiscoveryPage from './pages/ConcertDiscoveryPage';
import ConcertDetailPage from './pages/ConcertDetailPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import CheckoutSuccessPage from './pages/CheckoutSuccessPage';
import UserProfilePage from './pages/UserProfilePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import TermsPage from './pages/TermsPage';
import PrivacyPage from './pages/PrivacyPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import CreateConcertPage from './pages/CreateConcertPage';
import ManageSeatsPage from './pages/ManageSeatsPage';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Routes>
            {/* Default route redirects to concerts */}
            <Route path="/" element={<Navigate to="/concerts" replace />} />
            
            {/* Auth Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            
            {/* Legal Pages */}
            <Route path="/terms" element={<TermsPage />} />
            <Route path="/privacy" element={<PrivacyPage />} />
          
            {/* Concert Discovery Page */}
            <Route path="/concerts" element={<ConcertDiscoveryPage />} />
            
            {/* Concert Detail Page */}
            <Route path="/concerts/:id" element={<ConcertDetailPage />} />
            
            {/* Cart Page */}
            <Route path="/cart" element={<CartPage />} />
            
            {/* Checkout Pages (Protected) */}
            <Route
              path="/checkout"
              element={
                <ProtectedRoute>
                  <CheckoutPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/checkout/success"
              element={
                <ProtectedRoute>
                  <CheckoutSuccessPage />
                </ProtectedRoute>
              }
            />
            
            {/* User Profile Page (Protected) */}
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <UserProfilePage />
                </ProtectedRoute>
              }
            />

            {/* Admin Routes (Admin Only) */}
            <Route
              path="/admin"
              element={
                <AdminRoute>
                  <AdminDashboardPage />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/concerts"
              element={
                <AdminRoute>
                  <AdminDashboardPage />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/concerts/new"
              element={
                <AdminRoute>
                  <CreateConcertPage />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/concerts/:id/edit"
              element={
                <AdminRoute>
                  <CreateConcertPage />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/concerts/:id/seats"
              element={
                <AdminRoute>
                  <ManageSeatsPage />
                </AdminRoute>
              }
            />
            
            {/* 404 Not Found */}
            <Route
              path="*"
              element={
                <div className="min-h-screen bg-background-light dark:bg-background-dark flex items-center justify-center">
                  <div className="text-center space-y-4">
                    <span className="material-symbols-outlined text-red-500 text-6xl">
                      error
                    </span>
                    <h2 className="text-2xl font-bold text-text-primary dark:text-white">
                      404 - Seite nicht gefunden
                    </h2>
                    <a
                      href="/concerts"
                      className="inline-block px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                    >
                      Zurück zur Konzertübersicht
                    </a>
                  </div>
                </div>
              }
            />
          </Routes>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

