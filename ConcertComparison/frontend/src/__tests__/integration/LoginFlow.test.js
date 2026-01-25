import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../../context/AuthContext';
import LoginPage from '../../pages/LoginPage';
import authService from '../../services/authService';

// Mock authService
jest.mock('../../services/authService');

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => ({ state: null }),
}));

describe('Login Flow Integration Test', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  test('should complete full login flow successfully', async () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.login.mockResolvedValue({
      token: 'fake-jwt-token',
      email: 'test@example.com',
      role: 'USER',
    });
    authService.getProfile.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      role: 'USER',
    });

    render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    // Check if login form is rendered
    expect(screen.getByText('Willkommen zurück')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('name@example.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Passwort eingeben')).toBeInTheDocument();

    // Fill in the form
    const emailInput = screen.getByPlaceholderText('name@example.com');
    const passwordInput = screen.getByPlaceholderText('Passwort eingeben');
    const submitButton = screen.getByText('Anmelden');

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });

    // Submit the form
    fireEvent.click(submitButton);

    // Wait for async operations
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });
    expect(authService.getProfile).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/concerts', { replace: true });
  });

  test('should show validation errors for invalid input', async () => {
    authService.isAuthenticated.mockReturnValue(false);

    render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const submitButton = screen.getByText('Anmelden');

    // Submit without filling form
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Email darf nicht leer sein')).toBeInTheDocument();
    });
    expect(screen.getByText('Passwort darf nicht leer sein')).toBeInTheDocument();

    expect(authService.login).not.toHaveBeenCalled();
  });

  test('should show API error on login failure', async () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.login.mockRejectedValue({
      response: {
        data: {
          message: 'Ungültige Zugangsdaten',
        },
      },
    });

    render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const emailInput = screen.getByPlaceholderText('name@example.com');
    const passwordInput = screen.getByPlaceholderText('Passwort eingeben');
    const submitButton = screen.getByText('Anmelden');

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Ungültige Zugangsdaten')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  test('should toggle password visibility', async () => {
    authService.isAuthenticated.mockReturnValue(false);

    render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const passwordInput = screen.getByPlaceholderText('Passwort eingeben');
    expect(passwordInput).toHaveAttribute('type', 'password');

    // Find and click visibility toggle
    const visibilityButtons = screen.getAllByText(/visibility/);
    const toggleButton = visibilityButtons[0];

    fireEvent.click(toggleButton);

    // Password should now be visible
    await waitFor(() => {
      expect(passwordInput).toHaveAttribute('type', 'text');
    });

    // Click again to hide
    fireEvent.click(toggleButton);

    await waitFor(() => {
      expect(passwordInput).toHaveAttribute('type', 'password');
    });
  });
});
