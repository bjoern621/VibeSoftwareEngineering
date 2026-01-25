import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ConcertDiscoveryPage from './pages/ConcertDiscoveryPage';
import { AuthProvider } from './context/AuthContext';

// Mock the concert service to avoid API calls
jest.mock('./services/concertService', () => ({
  fetchConcerts: jest.fn().mockResolvedValue({
    concerts: [],
    page: { page: 0, size: 20, totalElements: 0, totalPages: 0 },
  }),
}));

jest.mock('./services/authService');

test('renders concert discovery page', async () => {
  render(
    <MemoryRouter>
      <AuthProvider>
        <ConcertDiscoveryPage />
      </AuthProvider>
    </MemoryRouter>
  );
  
  // The page should render the app header
  await waitFor(() => {
    expect(screen.getByText('ConcertFinder')).toBeInTheDocument();
  });
});
