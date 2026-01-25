import React from 'react';
import { render, waitFor, screen } from '@testing-library/react';
import { AuthProvider, useAuth } from '../../context/AuthContext';
import authService from '../../services/authService';

// Mock authService
jest.mock('../../services/authService');

// Test component to use auth context
const TestComponent = () => {
  const { isAuthenticated, user, login, logout, register } = useAuth();
  
  return (
    <div>
      <div data-testid="auth-status">{isAuthenticated ? 'authenticated' : 'not-authenticated'}</div>
      <div data-testid="user-email">{user?.email || 'no-user'}</div>
      <button data-testid="login-btn" onClick={() => login('test@example.com', 'password123')}>
        Login
      </button>
      <button data-testid="logout-btn" onClick={logout}>
        Logout
      </button>
      <button
        data-testid="register-btn"
        onClick={() =>
          register({
            email: 'new@example.com',
            password: 'password123',
            firstName: 'Test',
            lastName: 'User',
          })
        }
      >
        Register
      </button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  test('should initialize with not authenticated state', async () => {
    authService.isAuthenticated.mockReturnValue(false);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('not-authenticated');
    });
    expect(screen.getByTestId('user-email')).toHaveTextContent('no-user');
  });

  test('should load user profile on init if token exists', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getProfile.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    });

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');
    });
    await waitFor(() => {
      expect(screen.getByTestId('user-email')).toHaveTextContent('test@example.com');
    });
  });

  test('should handle login successfully', async () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.login.mockResolvedValue({
      token: 'fake-token',
      email: 'test@example.com',
    });
    authService.getProfile.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    });

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('not-authenticated');
    });

    const loginBtn = screen.getByTestId('login-btn');
    loginBtn.click();

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });
    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');
    });
    await waitFor(() => {
      expect(screen.getByTestId('user-email')).toHaveTextContent('test@example.com');
    });
  });

  test('should handle logout', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getProfile.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    });

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('authenticated');
    });

    const logoutBtn = screen.getByTestId('logout-btn');
    logoutBtn.click();

    await waitFor(() => {
      expect(authService.logout).toHaveBeenCalled();
    });
    expect(screen.getByTestId('auth-status')).toHaveTextContent('not-authenticated');
    expect(screen.getByTestId('user-email')).toHaveTextContent('no-user');
  });

  test('should handle registration', async () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.register.mockResolvedValue({
      id: 1,
      email: 'new@example.com',
      firstName: 'Test',
      lastName: 'User',
    });

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('not-authenticated');
    });

    const registerBtn = screen.getByTestId('register-btn');
    registerBtn.click();

    await waitFor(() => {
      expect(authService.register).toHaveBeenCalledWith({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'Test',
        lastName: 'User',
      });
    });
  });

  test('should handle auth init failure', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getProfile.mockRejectedValue(new Error('Unauthorized'));

    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(authService.logout).toHaveBeenCalled();
    });
    expect(screen.getByTestId('auth-status')).toHaveTextContent('not-authenticated');

    consoleSpy.mockRestore();
  });
});
