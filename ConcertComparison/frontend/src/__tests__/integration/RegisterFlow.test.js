import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../../context/AuthContext';
import RegisterPage from '../../pages/RegisterPage';
import authService from '../../services/authService';

// Mock authService
jest.mock('../../services/authService');

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('Register Flow Integration Test', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  test('should complete full registration flow successfully', async () => {
    authService.register.mockResolvedValue({
      id: 1,
      email: 'new@example.com',
      firstName: 'New',
      lastName: 'User',
    });
    authService.login.mockResolvedValue({
      token: 'fake-jwt-token',
      email: 'new@example.com',
      role: 'USER',
    });
    authService.getProfile.mockResolvedValue({
      id: 1,
      email: 'new@example.com',
      firstName: 'New',
      lastName: 'User',
      role: 'USER',
    });

    render(
      <BrowserRouter>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </BrowserRouter>
    );

    // Check if registration form is rendered
    expect(screen.getByRole('heading', { name: /konto erstellen/i })).toBeInTheDocument();

    // Fill in the form
    fireEvent.change(screen.getByPlaceholderText('Max'), {
      target: { value: 'New' },
    });
    fireEvent.change(screen.getByPlaceholderText('Mustermann'), {
      target: { value: 'User' },
    });
    fireEvent.change(screen.getByPlaceholderText('max.mustermann@example.com'), {
      target: { value: 'new@example.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('Mindestens 8 Zeichen'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByPlaceholderText('Passwort wiederholen'), {
      target: { value: 'password123' },
    });

    // Accept terms
    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);

    // Submit the form
    const submitButton = screen.getByRole('button', { name: /konto erstellen/i });
    fireEvent.click(submitButton);

    // Wait for async operations
    await waitFor(() => {
      expect(authService.register).toHaveBeenCalledWith({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
      });
    });
    expect(authService.login).toHaveBeenCalledWith({ email: 'new@example.com', password: 'password123' });
    expect(mockNavigate).toHaveBeenCalledWith('/concerts', { replace: true });
  });

  test('should show validation errors for invalid input', async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const submitButton = screen.getByRole('button', { name: /konto erstellen/i });

    // Submit without filling form
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Vorname darf nicht leer sein')).toBeInTheDocument();
    });
    expect(screen.getByText('Nachname darf nicht leer sein')).toBeInTheDocument();
    expect(screen.getByText('Email darf nicht leer sein')).toBeInTheDocument();
    expect(screen.getByText('Sie müssen die AGB akzeptieren')).toBeInTheDocument();

    expect(authService.register).not.toHaveBeenCalled();
  });

  test('should show error when passwords do not match', async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('Max'), {
      target: { value: 'Test' },
    });
    fireEvent.change(screen.getByPlaceholderText('Mustermann'), {
      target: { value: 'User' },
    });
    fireEvent.change(screen.getByPlaceholderText('max.mustermann@example.com'), {
      target: { value: 'test@example.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('Mindestens 8 Zeichen'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByPlaceholderText('Passwort wiederholen'), {
      target: { value: 'differentpassword' },
    });

    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);

    const submitButton = screen.getByRole('button', { name: /konto erstellen/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Passwörter stimmen nicht überein')).toBeInTheDocument();
    });

    expect(authService.register).not.toHaveBeenCalled();
  });

  test('should show password strength indicator', async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const passwordInput = screen.getByPlaceholderText('Mindestens 8 Zeichen');

    // Weak password
    fireEvent.change(passwordInput, { target: { value: 'weak' } });
    await waitFor(() => {
      expect(screen.getByText(/Schwaches Passwort|Mittelstarkes Passwort/)).toBeInTheDocument();
    });

    // Strong password
    fireEvent.change(passwordInput, { target: { value: 'StrongP@ssw0rd123!' } });
    await waitFor(() => {
      expect(screen.getByText(/Starkes Passwort|Mittelstarkes Passwort/)).toBeInTheDocument();
    });
  });

  test('should show API error on registration failure', async () => {
    authService.register.mockRejectedValue({
      response: {
        data: {
          message: 'Email bereits registriert',
        },
      },
    });

    render(
      <BrowserRouter>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('Max'), {
      target: { value: 'Test' },
    });
    fireEvent.change(screen.getByPlaceholderText('Mustermann'), {
      target: { value: 'User' },
    });
    fireEvent.change(screen.getByPlaceholderText('max.mustermann@example.com'), {
      target: { value: 'existing@example.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('Mindestens 8 Zeichen'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByPlaceholderText('Passwort wiederholen'), {
      target: { value: 'password123' },
    });

    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);

    const submitButton = screen.getByRole('button', { name: /konto erstellen/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Email bereits registriert')).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
