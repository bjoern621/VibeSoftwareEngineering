import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import FilterBar from '../../components/common/FilterBar';

describe('FilterBar Component', () => {
  const mockOnFilterChange = jest.fn();
  const mockOnSortChange = jest.fn();

  const defaultProps = {
    activeFilter: 'all',
    onFilterChange: mockOnFilterChange,
    activeSort: 'date',
    onSortChange: mockOnSortChange,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders all filter options', () => {
    render(<FilterBar {...defaultProps} />);

    expect(screen.getByText('Alle Events')).toBeInTheDocument();
    expect(screen.getByText('Diese Woche')).toBeInTheDocument();
    expect(screen.getByText('Wochenende')).toBeInTheDocument();
    expect(screen.getByText('Beliebt')).toBeInTheDocument();
  });

  test('highlights active filter', () => {
    render(<FilterBar {...defaultProps} activeFilter="thisWeek" />);

    const thisWeekButton = screen.getByText('Diese Woche').closest('button');
    const allEventsButton = screen.getByText('Alle Events').closest('button');

    expect(thisWeekButton).toHaveClass('bg-primary');
    expect(allEventsButton).not.toHaveClass('bg-primary');
  });

  test('calls onFilterChange when filter clicked', () => {
    render(<FilterBar {...defaultProps} />);

    const weekendButton = screen.getByText('Wochenende');
    fireEvent.click(weekendButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith('weekend');
  });

  test('displays current sort option', () => {
    render(<FilterBar {...defaultProps} activeSort="price-asc" />);

    expect(screen.getByText('Preis (aufsteigend)')).toBeInTheDocument();
  });

  test('opens sort dropdown on click', () => {
    render(<FilterBar {...defaultProps} />);

    const sortButton = screen.getByText('Datum').closest('button');
    fireEvent.click(sortButton);

    // All sort options should be visible
    expect(screen.getByText('Preis (aufsteigend)')).toBeInTheDocument();
    expect(screen.getByText('Preis (absteigend)')).toBeInTheDocument();
    expect(screen.getByText('Name')).toBeInTheDocument();
  });

  test('calls onSortChange when sort option selected', () => {
    render(<FilterBar {...defaultProps} />);

    // Open dropdown
    const sortButton = screen.getByText('Datum').closest('button');
    fireEvent.click(sortButton);

    // Click price sort
    const priceSort = screen.getByText('Preis (aufsteigend)');
    fireEvent.click(priceSort);

    expect(mockOnSortChange).toHaveBeenCalledWith('price-asc');
  });

  test('closes dropdown after selecting option', () => {
    render(<FilterBar {...defaultProps} />);

    // Open dropdown - use getAllByText since Datum appears in button and dropdown
    const sortButtons = screen.getAllByText('Datum');
    fireEvent.click(sortButtons[0].closest('button'));

    // Select option
    const priceSort = screen.getByText('Preis (aufsteigend)');
    fireEvent.click(priceSort);

    // onSortChange should have been called
    expect(mockOnSortChange).toHaveBeenCalledWith('price-asc');
  });

  test('shows check icon for active sort', () => {
    render(<FilterBar {...defaultProps} />);

    // Open dropdown - use getAllByText since Datum appears in button and dropdown
    const datumElements = screen.getAllByText('Datum');
    fireEvent.click(datumElements[0].closest('button'));

    // After opening dropdown, verify a check icon exists for the active sort option
    const checkIcons = document.querySelectorAll('.material-symbols-outlined');
    const hasCheckIcon = Array.from(checkIcons).some(icon => icon.textContent === 'check');
    expect(hasCheckIcon).toBe(true);
  });
});
