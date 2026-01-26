import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import PayPalForm, { validatePayPal } from './PayPalForm';

describe('PayPalForm', () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    mockOnChange.mockClear();
    jest.useFakeTimers();
  });

  afterEach(() => {
    // CRITICAL: Run all pending timers before switching to real timers
    act(() => {
      jest.runOnlyPendingTimers();
    });
    jest.useRealTimers();
  });

  test('renders email input and connect button', () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    expect(screen.getByPlaceholderText('ihre-email@beispiel.de')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /mit paypal verbinden/i })).toBeInTheDocument();
  });

  test('disables connect button when email is empty', () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    const connectButton = screen.getByRole('button', { name: /mit paypal verbinden/i });
    expect(connectButton).toBeDisabled();
  });

  test('enables connect button when email is entered', () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    const emailInput = screen.getByPlaceholderText('ihre-email@beispiel.de');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    
    const connectButton = screen.getByRole('button', { name: /mit paypal verbinden/i });
    expect(connectButton).not.toBeDisabled();
  });

  test('shows loading state when connecting', async () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    const emailInput = screen.getByPlaceholderText('ihre-email@beispiel.de');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    
    const connectButton = screen.getByRole('button', { name: /mit paypal verbinden/i });
    fireEvent.click(connectButton);
    
    expect(screen.getByText(/verbinde mit paypal/i)).toBeInTheDocument();
  });

  test('shows connected state after successful connection', async () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    const emailInput = screen.getByPlaceholderText('ihre-email@beispiel.de');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    
    const connectButton = screen.getByRole('button', { name: /mit paypal verbinden/i });
    fireEvent.click(connectButton);
    
    // Fast-forward timer
    await act(async () => {
      jest.advanceTimersByTime(2000);
    });
    
    expect(screen.getByText(/paypal verbunden/i)).toBeInTheDocument();
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
  });

  test('allows disconnecting after connection', async () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    const emailInput = screen.getByPlaceholderText('ihre-email@beispiel.de');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    
    const connectButton = screen.getByRole('button', { name: /mit paypal verbinden/i });
    fireEvent.click(connectButton);
    
    await act(async () => {
      jest.advanceTimersByTime(2000);
    });
    
    const disconnectButton = screen.getByRole('button', { name: /trennen/i });
    fireEvent.click(disconnectButton);
    
    // Should show connect form again
    expect(screen.getByPlaceholderText('ihre-email@beispiel.de')).toBeInTheDocument();
  });

  test('calls onChange with connection data', async () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    const emailInput = screen.getByPlaceholderText('ihre-email@beispiel.de');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    
    await waitFor(() => {
      expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({
        email: 'test@example.com',
      }));
    });
  });

  test('displays validation errors', () => {
    render(
      <PayPalForm 
        onChange={mockOnChange} 
        errors={{ email: 'Ungültige E-Mail-Adresse' }} 
      />
    );
    
    expect(screen.getByText('Ungültige E-Mail-Adresse')).toBeInTheDocument();
  });

  test('disables inputs when disabled prop is true', () => {
    render(<PayPalForm onChange={mockOnChange} disabled={true} />);
    
    expect(screen.getByPlaceholderText('ihre-email@beispiel.de')).toBeDisabled();
    expect(screen.getByRole('button', { name: /mit paypal verbinden/i })).toBeDisabled();
  });

  test('shows ready to pay notice when connected', async () => {
    render(<PayPalForm onChange={mockOnChange} />);
    
    const emailInput = screen.getByPlaceholderText('ihre-email@beispiel.de');
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    
    const connectButton = screen.getByRole('button', { name: /mit paypal verbinden/i });
    fireEvent.click(connectButton);
    
    await act(async () => {
      jest.advanceTimersByTime(2000);
    });
    
    expect(screen.getByText(/bereit zur zahlung über paypal/i)).toBeInTheDocument();
  });
});

describe('validatePayPal', () => {
  test('returns valid for correct PayPal data', () => {
    const result = validatePayPal({
      email: 'test@example.com',
      isConnected: true,
    });
    
    expect(result.isValid).toBe(true);
    expect(Object.keys(result.errors)).toHaveLength(0);
  });

  test('returns error for missing email', () => {
    const result = validatePayPal({
      email: '',
      isConnected: true,
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.email).toBe('PayPal E-Mail ist erforderlich');
  });

  test('returns error for invalid email format', () => {
    const result = validatePayPal({
      email: 'invalid-email',
      isConnected: true,
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.email).toBe('Ungültige E-Mail-Adresse');
  });

  test('returns error for not connected', () => {
    const result = validatePayPal({
      email: 'test@example.com',
      isConnected: false,
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.connection).toBe('Bitte verbinden Sie Ihr PayPal-Konto');
  });
});
