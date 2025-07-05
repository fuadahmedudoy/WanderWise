import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import { AuthProvider } from './AuthContext';
import api from '../api';

// Mock the API module
vi.mock('../api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

// Mock window.location
Object.defineProperty(window, 'location', {
  value: {
    href: '',
  },
  writable: true,
});

// Test component to access context
const TestComponent = ({ children }) => {
  return (
    <AuthProvider>
      {children}
    </AuthProvider>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
    localStorageMock.setItem.mockClear();
    localStorageMock.removeItem.mockClear();
    api.post.mockClear();
    api.get.mockClear();
  });

  test('initializes with no current user', async () => {
    render(
      <TestComponent>
        <div>Test</div>
      </TestComponent>
    );

    await waitFor(() => {
      expect(screen.getByText('Test')).toBeInTheDocument();
    });
  });

  test('checks authentication on mount when token exists', async () => {
    const mockUser = { id: 1, email: 'test@example.com', username: 'testuser' };
    localStorageMock.getItem.mockReturnValue('mock-token');
    api.get.mockResolvedValueOnce({ data: mockUser });

    render(
      <TestComponent>
        <div>Test</div>
      </TestComponent>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/api/me');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('currentUser', JSON.stringify(mockUser));
    });
  });

  test('handles auth check failure by logging out', async () => {
    localStorageMock.getItem.mockReturnValue('invalid-token');
    api.get.mockRejectedValueOnce(new Error('Unauthorized'));
    api.post.mockResolvedValueOnce({ data: {} });

    render(
      <TestComponent>
        <div>Test</div>
      </TestComponent>
    );

    await waitFor(() => {
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('currentUser');
    });
  });

  test('updates token if new token is received', async () => {
    const mockUser = { 
      id: 1, 
      email: 'test@example.com', 
      username: 'testuser',
      token: 'new-token'
    };
    localStorageMock.getItem.mockReturnValue('old-token');
    api.get.mockResolvedValueOnce({ data: mockUser });

    render(
      <TestComponent>
        <div>Test</div>
      </TestComponent>
    );

    await waitFor(() => {
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'new-token');
    });
  });

  test('does not update token if token is the same', async () => {
    const mockUser = { 
      id: 1, 
      email: 'test@example.com', 
      username: 'testuser',
      token: 'same-token'
    };
    localStorageMock.getItem.mockReturnValue('same-token');
    api.get.mockResolvedValueOnce({ data: mockUser });

    render(
      <TestComponent>
        <div>Test</div>
      </TestComponent>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/api/me');
    });

    // Should not set token again if it's the same
    expect(localStorageMock.setItem).toHaveBeenCalledWith('currentUser', JSON.stringify(mockUser));
    expect(localStorageMock.setItem).not.toHaveBeenCalledWith('token', 'same-token');
  });

  test('handles missing token gracefully', async () => {
    localStorageMock.getItem.mockReturnValue(null);

    render(
      <TestComponent>
        <div>Test</div>
      </TestComponent>
    );

    await waitFor(() => {
      expect(screen.getByText('Test')).toBeInTheDocument();
    });

    expect(api.get).not.toHaveBeenCalled();
  });
});
