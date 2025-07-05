import React from 'react';
import { vi } from 'vitest';
import { render, act } from '@testing-library/react';
import { AuthProvider } from './AuthContext';
import AuthContext from './AuthContext';
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

describe('AuthContext Methods', () => {
  let authContextValue;

  const TestComponent = () => {
    const context = React.useContext(AuthContext);
    authContextValue = context;
    return <div>Test</div>;
  };

  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
    localStorageMock.setItem.mockClear();
    localStorageMock.removeItem.mockClear();
    api.post.mockClear();
    api.get.mockClear();
    authContextValue = null;
  });

  describe('login method', () => {
    test('successful login stores token and user data', async () => {
      const mockResponse = {
        data: {
          token: 'mock-token',
          id: 1,
          email: 'test@example.com',
          username: 'testuser',
        },
      };
      
      api.post.mockResolvedValueOnce(mockResponse);

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await act(async () => {
        await authContextValue.login('test@example.com', 'password123');
      });

      expect(api.post).toHaveBeenCalledWith('/api/login', {
        email: 'test@example.com',
        password: 'password123',
      });
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'mock-token');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('currentUser', JSON.stringify({
        id: 1,
        email: 'test@example.com',
        username: 'testuser',
      }));
    });

    test('failed login throws error with backend message', async () => {
      const errorMessage = 'Invalid credentials';
      api.post.mockRejectedValueOnce({
        response: { data: errorMessage },
      });

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await expect(async () => {
        await authContextValue.login('test@example.com', 'wrongpassword');
      }).rejects.toThrow(errorMessage);

      expect(localStorageMock.setItem).not.toHaveBeenCalled();
    });

    test('failed login throws generic error when no backend message', async () => {
      api.post.mockRejectedValueOnce(new Error('Network error'));

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await expect(async () => {
        await authContextValue.login('test@example.com', 'wrongpassword');
      }).rejects.toThrow('Invalid credentials');
    });
  });

  describe('signup method', () => {
    test('successful signup returns response data', async () => {
      const mockResponse = {
        data: { success: true, message: 'User created successfully' },
      };
      
      api.post.mockResolvedValueOnce(mockResponse);

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      const result = await authContextValue.signup('test@example.com', 'password123', { username: 'testuser' });

      expect(api.post).toHaveBeenCalledWith('/api/signup', {
        email: 'test@example.com',
        password: 'password123',
        username: 'testuser',
      });
      expect(result).toEqual(mockResponse.data);
    });

    test('failed signup throws error with backend message', async () => {
      const errorMessage = 'Email already exists';
      api.post.mockRejectedValueOnce({
        response: { data: { message: errorMessage } },
      });

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await expect(async () => {
        await authContextValue.signup('existing@example.com', 'password123', { username: 'testuser' });
      }).rejects.toThrow(errorMessage);
    });

    test('failed signup throws generic error when no backend message', async () => {
      api.post.mockRejectedValueOnce(new Error('Network error'));

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await expect(async () => {
        await authContextValue.signup('test@example.com', 'password123', { username: 'testuser' });
      }).rejects.toThrow('Failed to register');
    });
  });

  describe('logout method', () => {
    test('successful logout clears tokens and user data', async () => {
      api.post.mockResolvedValueOnce({ data: {} });

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await act(async () => {
        await authContextValue.logout();
      });

      expect(api.post).toHaveBeenCalledWith('/api/logout');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('currentUser');
    });

    test('logout clears tokens even when API call fails', async () => {
      api.post.mockRejectedValueOnce(new Error('Server error'));

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await act(async () => {
        await authContextValue.logout();
      });

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('currentUser');
    });
  });

  describe('signupWithGoogle method', () => {
    test('sets localStorage flag and redirects to Google OAuth in development', async () => {
      // Mock environment
      const originalEnv = process.env.NODE_ENV;
      process.env.NODE_ENV = 'development';

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      authContextValue.signupWithGoogle();

      expect(localStorageMock.setItem).toHaveBeenCalledWith('awaitingOAuthReturn', 'true');
      expect(window.location.href).toBe('http://localhost:8080/oauth2/authorization/google');

      // Restore environment
      process.env.NODE_ENV = originalEnv;
    });

    test('sets localStorage flag and redirects to Google OAuth in production', async () => {
      // Mock environment
      const originalEnv = process.env.NODE_ENV;
      process.env.NODE_ENV = 'production';

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      authContextValue.signupWithGoogle();

      expect(localStorageMock.setItem).toHaveBeenCalledWith('awaitingOAuthReturn', 'true');
      expect(window.location.href).toBe('/oauth2/authorization/google');

      // Restore environment
      process.env.NODE_ENV = originalEnv;
    });
  });

  describe('setToken method', () => {
    test('stores token in localStorage', async () => {
      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      authContextValue.setToken('new-token');

      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'new-token');
    });
  });
});
