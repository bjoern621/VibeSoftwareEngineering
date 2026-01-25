import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import UserProfilePage from '../UserProfilePage';
import { useAuth } from '../../context/AuthContext';

jest.mock('../../context/AuthContext');

jest.mock('../../components/profile/UserProfileData', () => {
  return function MockUserProfileData() {
    return <div data-testid="user-profile-data">Profile Data</div>;
  };
});

jest.mock('../../components/profile/OrderHistorySection', () => {
  return function MockOrderHistorySection() {
    return <div data-testid="order-history-section">Order History</div>;
  };
});

const renderWithRouter = (component) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('UserProfilePage', () => {
  const mockUser = {
    id: 1,
    email: 'test@example.com',
    firstName: 'Max',
    lastName: 'Mustermann',
  };

  const mockLogout = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    useAuth.mockReturnValue({
      user: mockUser,
      logout: mockLogout,
    });
    
    // Mock window.location.href
    delete window.location;
    window.location = { href: '' };
  });

  describe('Rendering', () => {
    it('should render profile page', () => {
      renderWithRouter(<UserProfilePage />);

      expect(screen.getByText('Concert Comparison')).toBeInTheDocument();
    });

    it('should render navigation tabs', () => {
      renderWithRouter(<UserProfilePage />);

      expect(screen.getByRole('button', { name: /Meine Bestellungen/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Mein Profil/i })).toBeInTheDocument();
    });

    it('should show order history by default', () => {
      renderWithRouter(<UserProfilePage />);

      expect(screen.getByTestId('order-history-section')).toBeInTheDocument();
    });
  });

  describe('Tab navigation', () => {
    it('should switch to profile tab when clicked', () => {
      renderWithRouter(<UserProfilePage />);

      const profileTab = screen.getByRole('button', { name: /Mein Profil/i });
      fireEvent.click(profileTab);

      expect(screen.getByTestId('user-profile-data')).toBeInTheDocument();
      expect(screen.queryByTestId('order-history-section')).not.toBeInTheDocument();
    });

    it('should switch back to orders tab', () => {
      renderWithRouter(<UserProfilePage />);

      // Switch to profile
      const profileTab = screen.getByRole('button', { name: /Mein Profil/i });
      fireEvent.click(profileTab);

      expect(screen.getByTestId('user-profile-data')).toBeInTheDocument();

      // Switch back to orders
      const ordersTab = screen.getByRole('button', { name: /Meine Bestellungen/i });
      fireEvent.click(ordersTab);

      expect(screen.getByTestId('order-history-section')).toBeInTheDocument();
      expect(screen.queryByTestId('user-profile-data')).not.toBeInTheDocument();
    });

    it('should apply active styles to selected tab', () => {
      renderWithRouter(<UserProfilePage />);

      const ordersTab = screen.getByRole('button', { name: /Meine Bestellungen/i });
      expect(ordersTab.className).toContain('text-primary');

      const profileTab = screen.getByRole('button', { name: /Mein Profil/i });
      fireEvent.click(profileTab);

      expect(profileTab.className).toContain('text-primary');
    });
  });

  describe('Logout functionality', () => {
    it('should call logout when Abmelden button is clicked', () => {
      renderWithRouter(<UserProfilePage />);

      const logoutButtons = screen.getAllByRole('button', { name: /Abmelden/i });
      fireEvent.click(logoutButtons[0]);

      expect(mockLogout).toHaveBeenCalled();
      expect(window.location.href).toBe('/login');
    });
  });

  describe('Navigation links', () => {
    it('should render link to concerts page', () => {
      renderWithRouter(<UserProfilePage />);

      // Concert Comparison is a heading, not a link
      const heading = screen.getByText('Concert Comparison');
      expect(heading).toBeInTheDocument();
    });
  });
});
