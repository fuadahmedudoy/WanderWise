import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import Home from '../Home';
import AuthContext from '../../context/AuthContext';
import api from '../../api';

// Mock dependencies
vi.mock('../../api', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../../components/NotificationCenter', () => ({
  default: () => <div data-testid="notification-center">NotificationCenter</div>,
}));

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Helper function to render component with providers
const renderWithProviders = (currentUser = null) => {
  const authContextValue = {
    currentUser,
    logout: vi.fn(),
  };

  return render(
    <BrowserRouter>
      <AuthContext.Provider value={authContextValue}>
        <Home />
      </AuthContext.Provider>
    </BrowserRouter>
  );
};

describe('Home Component', () => {
  const mockUser = {
    id: 1,
    email: 'test@example.com',
    username: 'testuser',
  };

  const mockDestinations = [
    {
      id: 1,
      title: 'Beautiful Beach',
      destination: 'Maldives',
      days: 7,
      avgRating: 4.5,
      description: 'A beautiful beach destination with crystal clear waters and white sand beaches.',
      imageUrl: 'https://example.com/beach.jpg',
    },
    {
      id: 2,
      title: 'Mountain Adventure',
      destination: 'Nepal',
      days: 10,
      avgRating: 4.8,
      description: 'An exciting mountain adventure with breathtaking views and challenging hikes.',
      imageUrl: 'https://example.com/mountain.jpg',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('Navigation Bar', () => {
    test('displays logo', () => {
      renderWithProviders();
      expect(screen.getByText('WanderWise')).toBeInTheDocument();
    });

    test('shows login button when user is not logged in', () => {
      renderWithProviders(null);
      expect(screen.getByText('Login')).toBeInTheDocument();
      expect(screen.queryByText('Logout')).not.toBeInTheDocument();
    });

    test('shows user actions when user is logged in', () => {
      renderWithProviders(mockUser);
      expect(screen.getByText('Profile')).toBeInTheDocument();
      expect(screen.getByText('Logout')).toBeInTheDocument();
      expect(screen.getByTestId('notification-center')).toBeInTheDocument();
      expect(screen.queryByText('Login')).not.toBeInTheDocument();
    });

    test('navigates to login when login button is clicked', () => {
      renderWithProviders(null);
      fireEvent.click(screen.getByText('Login'));
      expect(mockNavigate).toHaveBeenCalledWith('/auth/login');
    });

    test('navigates to profile when profile button is clicked', () => {
      renderWithProviders(mockUser);
      fireEvent.click(screen.getByText('Profile'));
      expect(mockNavigate).toHaveBeenCalledWith('/profile');
    });

    test('handles logout when logout button is clicked', async () => {
      const mockLogout = vi.fn().mockResolvedValue();
      const authContextValue = {
        currentUser: mockUser,
        logout: mockLogout,
      };

      render(
        <BrowserRouter>
          <AuthContext.Provider value={authContextValue}>
            <Home />
          </AuthContext.Provider>
        </BrowserRouter>
      );

      fireEvent.click(screen.getByText('Logout'));

      await waitFor(() => {
        expect(mockLogout).toHaveBeenCalled();
        expect(mockNavigate).toHaveBeenCalledWith('/auth/login');
      });
    });
  });

  describe('Welcome Section', () => {
    test('displays personalized welcome for logged in user', () => {
      renderWithProviders(mockUser);
      expect(screen.getByText('Welcome, testuser!')).toBeInTheDocument();
    });

    test('displays username if available, otherwise email', () => {
      const userWithoutUsername = { ...mockUser, username: undefined };
      renderWithProviders(userWithoutUsername);
      expect(screen.getByText('Welcome, test@example.com!')).toBeInTheDocument();
    });

    test('displays generic welcome for guests', () => {
      renderWithProviders(null);
      expect(screen.getByText('Welcome to WanderWise!')).toBeInTheDocument();
    });

    test('displays action buttons', () => {
      renderWithProviders();
      expect(screen.getByText('Create New Trip')).toBeInTheDocument();
      expect(screen.getByText('View My Trips')).toBeInTheDocument();
    });

    test('navigates to create trip when button is clicked', () => {
      renderWithProviders();
      fireEvent.click(screen.getByText('Create New Trip'));
      expect(mockNavigate).toHaveBeenCalledWith('/create-trip');
    });

    test('navigates to my trips when button is clicked', () => {
      renderWithProviders();
      fireEvent.click(screen.getByText('View My Trips'));
      expect(mockNavigate).toHaveBeenCalledWith('/my-trips');
    });
  });

  describe('Featured Destinations', () => {
    test('displays loading state initially', () => {
      api.get.mockImplementation(() => new Promise(() => {})); // Never resolves
      renderWithProviders();
      expect(screen.getByText('Loading featured destinations...')).toBeInTheDocument();
    });

    test('displays featured destinations after loading', async () => {
      api.get.mockResolvedValue({ data: mockDestinations });

      renderWithProviders();

      await waitFor(() => {
        expect(screen.getByText('Beautiful Beach')).toBeInTheDocument();
        expect(screen.getByText('Mountain Adventure')).toBeInTheDocument();
      });

      expect(screen.getByText('Maldives')).toBeInTheDocument();
      expect(screen.getByText('Nepal')).toBeInTheDocument();
      expect(screen.getByText('7 days')).toBeInTheDocument();
      expect(screen.getByText('10 days')).toBeInTheDocument();
      expect(screen.getByText('4.5')).toBeInTheDocument();
      expect(screen.getByText('4.8')).toBeInTheDocument();
    });

    test('displays error message when API fails', async () => {
      api.get.mockRejectedValue(new Error('API Error'));

      renderWithProviders();

      await waitFor(() => {
        expect(screen.getByText('Failed to load featured destinations. Please try again later.')).toBeInTheDocument();
      });

      expect(screen.queryByText('Loading featured destinations...')).not.toBeInTheDocument();
    });

    test('displays no destinations message when empty array returned', async () => {
      api.get.mockResolvedValue({ data: [] });

      renderWithProviders();

      await waitFor(() => {
        expect(screen.getByText('No featured destinations available at the moment.')).toBeInTheDocument();
      });
    });

    test('navigates to destination details when view details is clicked', async () => {
      api.get.mockResolvedValue({ data: mockDestinations });

      renderWithProviders();

      await waitFor(() => {
        expect(screen.getByText('Beautiful Beach')).toBeInTheDocument();
      });

      const viewDetailsButtons = screen.getAllByText('View Details');
      fireEvent.click(viewDetailsButtons[0]);

      expect(mockNavigate).toHaveBeenCalledWith('/destination/1');
    });

    test('displays truncated description for destinations', async () => {
      api.get.mockResolvedValue({ data: mockDestinations });

      renderWithProviders();

      await waitFor(() => {
        expect(screen.getByText(/A beautiful beach destination with crystal clear waters and white sand beaches/)).toBeInTheDocument();
      });
    });

    test('displays star ratings correctly', async () => {
      api.get.mockResolvedValue({ data: mockDestinations });

      renderWithProviders();

      await waitFor(() => {
        const ratingElements = screen.getAllByText('4.5');
        expect(ratingElements[0]).toBeInTheDocument();
      });
    });
  });

  describe('LocalStorage Fallback', () => {
    test('uses localStorage user when currentUser is null', () => {
      localStorage.setItem('currentUser', JSON.stringify(mockUser));
      renderWithProviders(null);

      expect(screen.getByText('Welcome, testuser!')).toBeInTheDocument();
      expect(screen.getByText('Profile')).toBeInTheDocument();
      expect(screen.getByText('Logout')).toBeInTheDocument();
    });

    test.skip('handles invalid localStorage data gracefully', () => {
      localStorage.setItem('currentUser', 'invalid json');
      renderWithProviders(null);

      expect(screen.getByText('Welcome to WanderWise!')).toBeInTheDocument();
      expect(screen.getByText('Login')).toBeInTheDocument();
    });
  });

  describe('API Integration', () => {
    test('calls featured destinations API on mount', () => {
      api.get.mockResolvedValue({ data: [] });
      renderWithProviders();

      expect(api.get).toHaveBeenCalledWith('/api/destinations/featured');
    });

    test('handles API call only once on mount', () => {
      api.get.mockResolvedValue({ data: [] });
      renderWithProviders();

      expect(api.get).toHaveBeenCalledTimes(1);
    });
  });

  describe('Error Handling', () => {
    test('handles logout error gracefully', async () => {
      const mockLogout = vi.fn().mockRejectedValue(new Error('Logout failed'));
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      const authContextValue = {
        currentUser: mockUser,
        logout: mockLogout,
      };

      render(
        <BrowserRouter>
          <AuthContext.Provider value={authContextValue}>
            <Home />
          </AuthContext.Provider>
        </BrowserRouter>
      );

      fireEvent.click(screen.getByText('Logout'));

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalledWith('Failed to log out', expect.any(Error));
      });

      consoleSpy.mockRestore();
    });
  });

  describe('Responsive Design Elements', () => {
    test('applies correct CSS classes for layout', () => {
      renderWithProviders();

      const container = screen.getByText('WanderWise').closest('.home-container');
      expect(container).toBeInTheDocument();

      const navbar = screen.getByText('WanderWise').closest('.navbar');
      expect(navbar).toBeInTheDocument();
    });

    test('applies correct button classes', () => {
      renderWithProviders();

      const createTripButton = screen.getByText('Create New Trip');
      expect(createTripButton).toHaveClass('btn-primary');

      const viewTripsButton = screen.getByText('View My Trips');
      expect(viewTripsButton).toHaveClass('btn-secondary');
    });
  });

  describe('Accessibility', () => {
    test('buttons have appropriate text for screen readers', () => {
      renderWithProviders();

      expect(screen.getByRole('button', { name: 'Create New Trip' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'View My Trips' })).toBeInTheDocument();
    });

    test('destination cards have proper structure', async () => {
      api.get.mockResolvedValue({ data: mockDestinations });

      renderWithProviders();

      await waitFor(() => {
        const viewDetailsButtons = screen.getAllByRole('button', { name: 'View Details' });
        expect(viewDetailsButtons).toHaveLength(2);
      });
    });
  });
});
