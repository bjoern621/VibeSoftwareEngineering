import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import CreditCardForm, { validateCreditCard } from './CreditCardForm';

describe('CreditCardForm', () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    mockOnChange.mockClear();
  });

  test('renders all input fields', () => {
    render(<CreditCardForm onChange={mockOnChange} />);
    
    expect(screen.getByPlaceholderText('1234 5678 9012 3456')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('MAX MUSTERMANN')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('MM/YY')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('123')).toBeInTheDocument();
  });

  test('formats card number with spaces', () => {
    render(<CreditCardForm onChange={mockOnChange} />);
    
    const cardInput = screen.getByPlaceholderText('1234 5678 9012 3456');
    fireEvent.change(cardInput, { target: { value: '4111111111111111' } });
    
    expect(cardInput.value).toBe('4111 1111 1111 1111');
  });

  test('formats expiry date as MM/YY', () => {
    render(<CreditCardForm onChange={mockOnChange} />);
    
    const expiryInput = screen.getByPlaceholderText('MM/YY');
    fireEvent.change(expiryInput, { target: { value: '1225' } });
    
    expect(expiryInput.value).toBe('12/25');
  });

  test('limits CVV to 3 digits for non-Amex', () => {
    render(<CreditCardForm onChange={mockOnChange} />);
    
    const cvvInput = screen.getByPlaceholderText('123');
    fireEvent.change(cvvInput, { target: { value: '12345' } });
    
    expect(cvvInput.value).toBe('123');
  });

  test('converts cardholder name to uppercase', () => {
    render(<CreditCardForm onChange={mockOnChange} />);
    
    const nameInput = screen.getByPlaceholderText('MAX MUSTERMANN');
    fireEvent.change(nameInput, { target: { value: 'john doe' } });
    
    expect(nameInput.value).toBe('JOHN DOE');
  });

  test('calls onChange with card data', async () => {
    render(<CreditCardForm onChange={mockOnChange} />);
    
    fireEvent.change(screen.getByPlaceholderText('1234 5678 9012 3456'), { target: { value: '4111111111111111' } });
    
    await waitFor(() => {
      expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({
        cardNumber: '4111111111111111',
      }));
    });
  });

  test('displays validation errors', () => {
    render(
      <CreditCardForm 
        onChange={mockOnChange} 
        errors={{ cardNumber: 'Ungültige Kartennummer' }} 
      />
    );
    
    expect(screen.getByText('Ungültige Kartennummer')).toBeInTheDocument();
  });

  test('disables inputs when disabled prop is true', () => {
    render(<CreditCardForm onChange={mockOnChange} disabled={true} />);
    
    expect(screen.getByPlaceholderText('1234 5678 9012 3456')).toBeDisabled();
    expect(screen.getByPlaceholderText('MAX MUSTERMANN')).toBeDisabled();
    expect(screen.getByPlaceholderText('MM/YY')).toBeDisabled();
  });

  test('shows security notice', () => {
    render(<CreditCardForm onChange={mockOnChange} />);
    
    expect(screen.getByText('Ihre Zahlungsdaten werden sicher übertragen')).toBeInTheDocument();
  });
});

describe('validateCreditCard', () => {
  test('returns valid for correct card data', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '12/30',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(true);
    expect(Object.keys(result.errors)).toHaveLength(0);
  });

  test('returns error for missing card number', () => {
    const result = validateCreditCard({
      cardNumber: '',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '12/30',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.cardNumber).toBe('Kartennummer ist erforderlich');
  });

  test('returns error for invalid card number length', () => {
    const result = validateCreditCard({
      cardNumber: '411111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '12/30',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.cardNumber).toBe('Ungültige Kartennummer');
  });

  test('returns error for missing cardholder', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: '',
      expiryDate: '12/30',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.cardHolder).toBe('Karteninhaber ist erforderlich');
  });

  test('returns error for single name cardholder', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX',
      expiryDate: '12/30',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.cardHolder).toBe('Bitte Vor- und Nachname eingeben');
  });

  test('returns error for missing expiry date', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.expiryDate).toBe('Gültigkeitsdatum ist erforderlich');
  });

  test('returns error for invalid expiry format', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '1230',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.expiryDate).toBe('Format: MM/YY');
  });

  test('returns error for invalid month', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '15/30',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.expiryDate).toBe('Ungültiger Monat');
  });

  test('returns error for expired card', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '01/20',
      cvv: '123',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.expiryDate).toBe('Karte ist abgelaufen');
  });

  test('returns error for missing CVV', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '12/30',
      cvv: '',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.cvv).toBe('CVV ist erforderlich');
  });

  test('returns error for invalid CVV format', () => {
    const result = validateCreditCard({
      cardNumber: '4111111111111111',
      cardHolder: 'MAX MUSTERMANN',
      expiryDate: '12/30',
      cvv: '12',
    });
    
    expect(result.isValid).toBe(false);
    expect(result.errors.cvv).toBe('Ungültiger CVV');
  });
});
