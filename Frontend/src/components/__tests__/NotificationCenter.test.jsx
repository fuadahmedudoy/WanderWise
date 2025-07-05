import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import NotificationCenter from '../NotificationCenter';
import AuthContext from '../../context/AuthContext';
import notificationService from '../../services/notificationService';
import api from '../../api';

// Mock dependencies
vi.mock('../../services/notificationService', () => ({
  default: {
    connect: vi.fn(),
    disconnect: vi.fn(),
  },
}));

vi.mock('../../api', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
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

// Mock ReactDOM.createPortal
vi.mock('react-dom', async () => {
  const actual = await vi.importActual('react-dom');
  return {
    ...actual,
    createPortal: (element, container) => element,
  };
});

// Mock Notification API
Object.defineProperty(window, 'Notification', {
  value: class MockNotification {
    constructor(title, options) {
      this.title = title;
      this.options = options;
    }
    static permission = 'granted';
    static requestPermission = vi.fn(() => Promise.resolve('granted'));
  },
  configurable: true,
});

// Add notification-root to DOM
beforeEach(() => {
  const notificationRoot = document.createElement('div');
  notificationRoot.id = 'notification-root';
  document.body.appendChild(notificationRoot);
});

afterEach(() => {
  const notificationRoot = document.getElementById('notification-root');
  if (notificationRoot) {
    document.body.removeChild(notificationRoot);
  }
});

// Helper function to render component with providers
const renderWithProviders = (currentUser = null) => {
  const authContextValue = {
    currentUser,
    login: vi.fn(),
    logout: vi.fn(),
    signup: vi.fn(),
  };

  return render(
    <BrowserRouter>
      <AuthContext.Provider value={authContextValue}>
        <NotificationCenter />
      </AuthContext.Provider>
    </BrowserRouter>
  );
};

describe('NotificationCenter Component', () => {
  const mockUser = {
    id: 1,
    email: 'test@example.com',
    username: 'testuser',
  };

  const mockNotifications = [
    {
      id: 1,
      type: 'WEATHER_ALERT',
      message: 'Weather alert for your trip to Sylhet',
      isRead: false,
      createdAt: new Date().toISOString(),
      tripId: 1,
    },
    {
      id: 2,
      type: 'WEATHER_ALERT',
      message: 'Another weather alert',
      isRead: true,
      createdAt: new Date(Date.now() - 3600000).toISOString(),
      tripId: 2,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('Authentication and User Context', () => {
    test('renders nothing when no user is logged in', () => {
      renderWithProviders(null);
      expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });

    test('renders notification bell when user is logged in', () => {
      renderWithProviders(mockUser);
      expect(screen.getByRole('button')).toBeInTheDocument();
    });

    test('uses localStorage user when currentUser is null', () => {
      localStorage.setItem('currentUser', JSON.stringify(mockUser));
      renderWithProviders(null);
      expect(screen.getByRole('button')).toBeInTheDocument();
    });
  });

  describe('Notification Bell', () => {
    test('displays notification bell icon', () => {
      renderWithProviders(mockUser);
      const bellButton = screen.getByRole('button');
      expect(bellButton).toHaveClass('notification-bell');
    });

    test('shows unread count badge when there are unread notifications', async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: mockNotifications,
          unreadCount: 1,
        },
      });

      renderWithProviders(mockUser);

      await waitFor(() => {
        expect(screen.getByText('1')).toBeInTheDocument();
        expect(screen.getByText('1')).toHaveClass('notification-badge');
      });
    });

    test('hides badge when no unread notifications', async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: mockNotifications.map(n => ({ ...n, isRead: true })),
          unreadCount: 0,
        },
      });

      renderWithProviders(mockUser);

      await waitFor(() => {
        expect(screen.queryByText('0')).not.toBeInTheDocument();
      });
    });
  });

  describe('Notification Loading', () => {
    test('loads notifications on mount', async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: mockNotifications,
          unreadCount: 1,
        },
      });

      renderWithProviders(mockUser);

      await waitFor(() => {
        expect(api.get).toHaveBeenCalledWith('/api/notifications');
      });
    });

    test('shows loading state while fetching notifications', async () => {
      api.get.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      expect(screen.getByText('Loading notifications...')).toBeInTheDocument();
    });

    test('falls back to mock data when API fails', async () => {
      api.get.mockRejectedValue(new Error('API Error'));

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getAllByText(/Adverse weather conditions detected for your upcoming trip to Sylhet/)).toHaveLength(2);
      });
    });
  });

  describe('Notification Dropdown', () => {
    beforeEach(async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: mockNotifications,
          unreadCount: 1,
        },
      });
    });

    test('opens dropdown when bell is clicked', async () => {
      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Notifications')).toBeInTheDocument();
      });
    });

    test('closes dropdown when bell is clicked again', async () => {
      renderWithProviders(mockUser);

      const bellButton = screen.getByRole('button');
      fireEvent.click(bellButton);

      await waitFor(() => {
        expect(screen.getByText('Notifications')).toBeInTheDocument();
      });

      fireEvent.click(bellButton);

      await waitFor(() => {
        expect(screen.queryByText('Notifications')).not.toBeInTheDocument();
      });
    });

    test('displays notifications list', async () => {
      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Weather alert for your trip to Sylhet')).toBeInTheDocument();
        expect(screen.getByText('Another weather alert')).toBeInTheDocument();
      });
    });

    test('shows empty state when no notifications', async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: [],
          unreadCount: 0,
        },
      });

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('No notifications yet')).toBeInTheDocument();
        expect(screen.getByText('🔔')).toBeInTheDocument();
      });
    });
  });

  describe('Notification Actions', () => {
    beforeEach(async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: mockNotifications,
          unreadCount: 1,
        },
      });
    });

    test('marks single notification as read', async () => {
      api.put.mockResolvedValue({});

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Weather alert for your trip to Sylhet')).toBeInTheDocument();
      });

      const markReadButton = screen.getByTitle('Mark as read');
      fireEvent.click(markReadButton);

      await waitFor(() => {
        expect(api.put).toHaveBeenCalledWith('/api/notifications/1/read');
      });
    });

    test('deletes notification', async () => {
      api.delete.mockResolvedValue({});

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Weather alert for your trip to Sylhet')).toBeInTheDocument();
      });

      const deleteButtons = screen.getAllByTitle('Delete notification');
      fireEvent.click(deleteButtons[0]);

      await waitFor(() => {
        expect(api.delete).toHaveBeenCalledWith('/api/notifications/1');
      });
    });

    test('marks all notifications as read', async () => {
      api.post.mockResolvedValue({});

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Mark all read')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Mark all read'));

      await waitFor(() => {
        expect(api.post).toHaveBeenCalledWith('/api/notifications/mark-all-read');
      });
    });

    test('clears all notifications', async () => {
      api.delete.mockResolvedValue({});

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Clear all')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Clear all'));

      await waitFor(() => {
        expect(api.delete).toHaveBeenCalledWith('/api/notifications/user/1/all');
      });
    });
  });

  describe('Notification Click Handling', () => {
    beforeEach(async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: mockNotifications,
          unreadCount: 1,
        },
      });
      api.put.mockResolvedValue({});
    });

    test('navigates to weather details when weather alert is clicked', async () => {
      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Weather alert for your trip to Sylhet')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Weather alert for your trip to Sylhet'));

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/weather-details/1');
        expect(api.put).toHaveBeenCalledWith('/api/notifications/1/read');
      });
    });
  });

  describe('WebSocket Integration', () => {
    test('connects to notification service when user is present', () => {
      renderWithProviders(mockUser);

      expect(notificationService.connect).toHaveBeenCalledWith(
        mockUser.id,
        expect.any(Function)
      );
    });

    test('disconnects from notification service on unmount', () => {
      const { unmount } = renderWithProviders(mockUser);

      unmount();

      expect(notificationService.disconnect).toHaveBeenCalled();
    });
  });

  describe('Time Formatting', () => {
    test('formats time correctly', async () => {
      const now = new Date();
      const notifications = [
        {
          id: 1,
          type: 'WEATHER_ALERT',
          message: 'Recent notification',
          isRead: false,
          createdAt: new Date(now.getTime() - 30 * 60 * 1000).toISOString(), // 30 minutes ago
          tripId: 1,
        },
        {
          id: 2,
          type: 'WEATHER_ALERT',
          message: 'Older notification',
          isRead: false,
          createdAt: new Date(now.getTime() - 2 * 60 * 60 * 1000).toISOString(), // 2 hours ago
          tripId: 2,
        },
      ];

      api.get.mockResolvedValue({
        data: {
          notifications,
          unreadCount: 2,
        },
      });

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('30m ago')).toBeInTheDocument();
        expect(screen.getByText('2h ago')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    beforeEach(() => {
      // Mock console.error to avoid noise in tests
      vi.spyOn(console, 'error').mockImplementation(() => {});
    });

    afterEach(() => {
      console.error.mockRestore();
    });

    test('handles API errors gracefully for mark as read', async () => {
      api.get.mockResolvedValue({
        data: {
          notifications: mockNotifications,
          unreadCount: 1,
        },
      });
      api.put.mockRejectedValue(new Error('API Error'));

      renderWithProviders(mockUser);

      fireEvent.click(screen.getByRole('button'));

      await waitFor(() => {
        expect(screen.getByText('Weather alert for your trip to Sylhet')).toBeInTheDocument();
      });

      const markReadButton = screen.getByTitle('Mark as read');
      fireEvent.click(markReadButton);

      // Should still update UI even if API fails
      await waitFor(() => {
        expect(console.error).toHaveBeenCalled();
      });
    });
  });
});
