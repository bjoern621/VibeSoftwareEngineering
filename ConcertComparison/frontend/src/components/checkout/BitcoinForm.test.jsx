import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import BitcoinForm, { validateBitcoin } from './BitcoinForm';

// Mock clipboard API
Object.assign(navigator, {
  clipboard: {
    writeText: jest.fn().mockResolvedValue(undefined),
  },
});

describe('BitcoinForm', () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    mockOnChange.mockClear();
    jest.useFakeTimers();
    navigator.clipboard.writeText.mockClear();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  test('renders BTC payment interface', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    expect(screen.getByText(/mit bitcoin bezahlen/i)).toBeInTheDocument();
    expect(screen.getByText(/zu zahlen/i)).toBeInTheDocument();
    expect(screen.getByText('BTC', { exact: false })).toBeInTheDocument();
  });

  test('displays BTC address', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    const addressInput = screen.getByDisplayValue(/^bc1q/);
    expect(addressInput).toBeInTheDocument();
  });

  test('copies address to clipboard when copy button clicked', async () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    // Find the copy button by its icon
    const copyButton = screen.getByTitle('Adresse kopieren');
    fireEvent.click(copyButton);
    
    await waitFor(() => {
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith(expect.stringMatching(/^bc1q/));
    });
  });

  test('shows transaction ID input', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    expect(screen.getByPlaceholderText(/z.b. 3a1b2c3d4e5f/i)).toBeInTheDocument();
  });

  test('disables verify button when no transaction ID', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    const verifyButton = screen.getByRole('button', { name: /zahlung bestätigen/i });
    expect(verifyButton).toBeDisabled();
  });

  test('enables verify button when transaction ID entered', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    const txInput = screen.getByPlaceholderText(/z.b. 3a1b2c3d4e5f/i);
    fireEvent.change(txInput, { target: { value: 'abc123def456' } });
    
    const verifyButton = screen.getByRole('button', { name: /zahlung bestätigen/i });
    expect(verifyButton).not.toBeDisabled();
  });

  test('shows loading state when verifying', async () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    const txInput = screen.getByPlaceholderText(/z.b. 3a1b2c3d4e5f/i);
    fireEvent.change(txInput, { target: { value: 'abc123def456' } });
    
    const verifyButton = screen.getByRole('button', { name: /zahlung bestätigen/i });
    fireEvent.click(verifyButton);
    
    expect(screen.getByText(/verifiziere blockchain/i)).toBeInTheDocument();
  });

  test('shows confirmed state after verification', async () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    const txInput = screen.getByPlaceholderText(/z.b. 3a1b2c3d4e5f/i);
    fireEvent.change(txInput, { target: { value: 'abc123def456' } });
    
    const verifyButton = screen.getByRole('button', { name: /zahlung bestätigen/i });
    fireEvent.click(verifyButton);
    
    // Fast-forward timer
    await act(async () => {
      jest.advanceTimersByTime(3000);
    });
    
    expect(screen.getByText(/zahlung bestätigt/i)).toBeInTheDocument();
  });

  test('calculates BTC amount from EUR', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={420} />);
    
    // 420 EUR / 42000 rate = 0.01 BTC
    expect(screen.getByText('0.01000000 BTC')).toBeInTheDocument();
  });

  test('calls onChange with payment data', async () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    await waitFor(() => {
      expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({
        btcAddress: expect.stringMatching(/^bc1q/),
        paymentMethod: 'bitcoin',
      }));
    });
  });

  test('displays validation errors', () => {
    render(
      <BitcoinForm 
        onChange={mockOnChange} 
        amount={100}
        errors={{ transactionId: 'Ungültige Transaktions-ID' }} 
      />
    );
    
    expect(screen.getByText('Ungültige Transaktions-ID')).toBeInTheDocument();
  });

  test('disables inputs when disabled prop is true', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} disabled={true} />);
    
    expect(screen.getByPlaceholderText(/z.b. 3a1b2c3d4e5f/i)).toBeDisabled();
    expect(screen.getByRole('button', { name: /zahlung bestätigen/i })).toBeDisabled();
  });

  test('shows info notice about payment', () => {
    render(<BitcoinForm onChange={mockOnChange} amount={100} />);
    
    expect(screen.getByText(/senden sie den exakten betrag/i)).toBeInTheDocument();
  });
});

describe('validateBitcoin', () => {
  test('returns valid when payment confirmed', () => {
    const result = validateBitcoin({
      hasConfirmed: true,
      transactionId: 'abc123',
    });
    
    expect(result.isValid).toBe(true);
    expect(Object.keys(result.errors)).toHaveLength(0);
  });

  test('returns error when not confirmed', () => {
    const result = validateBitcoin({
      hasConfirmed: false,
      transactionId: 'abc123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.confirmation).toBe('Bitte bestätigen Sie die Zahlung');
  });
});
