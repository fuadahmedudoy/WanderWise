import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import Login from '../pages/auth/Login';
import Signup from '../pages/auth/Signup';
import { AuthProvider } from '../context/AuthContext';
import api from '../api';

// Mock the API module
vi.mock('../api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({ state: null }),
  };
});

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

// Helper function to render components with all providers
const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        {component}
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('Authentication Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
    localStorageMock.setItem.mockClear();
    localStorageMock.removeItem.mockClear();
    api.post.mockClear();
    api.get.mockClear();
  });

  describe('Login Flow Integration', () => {
    test('complete login flow with successful authentication', async () => {
      const mockLoginResponse = {
        data: {
          token: 'mock-jwt-token',
          id: 1,
          email: 'test@example.com',
          username: 'testuser',
        },
      };

      api.post.mockResolvedValueOnce(mockLoginResponse);

      renderWithProviders(<Login />);

      // Fill login form
      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'password123' } 
      });

      // Submit form
      fireEvent.click(screen.getByRole('button', { name: /^login$/i }));

      await waitFor(() => {
        expect(api.post).toHaveBeenCalledWith('/api/login', {
          email: 'test@example.com',
          password: 'password123',
        });
      });

      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'mock-jwt-token');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('currentUser', JSON.stringify({
        id: 1,
        email: 'test@example.com',
        username: 'testuser',
      }));
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });

    test('login with invalid credentials shows error', async () => {
      const errorMessage = 'Invalid email or password';
      api.post.mockRejectedValueOnce({
        response: { data: errorMessage },
      });

      renderWithProviders(<Login />);

      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'wrongpassword' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /^login$/i }));

      await waitFor(() => {
        expect(screen.getByText(errorMessage)).toBeInTheDocument();
      });

      expect(localStorageMock.setItem).not.toHaveBeenCalled();
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('login with network error shows generic error', async () => {
      api.post.mockRejectedValueOnce(new Error('Network Error'));

      renderWithProviders(<Login />);

      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'password123' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /^login$/i }));

      await waitFor(() => {
        expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
      });
    });
  });

  describe('Signup Flow Integration', () => {
    test('complete signup flow with successful registration', async () => {
      const mockSignupResponse = {
        data: { success: true, message: 'User created successfully' },
      };

      api.post.mockResolvedValueOnce(mockSignupResponse);

      renderWithProviders(<Signup />);

      // Click to show email form
      fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));

      // Fill signup form
      fireEvent.change(screen.getByLabelText('Username'), { 
        target: { value: 'testuser' } 
      });
      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'pass123' } 
      });
      fireEvent.change(screen.getByLabelText('Confirm Password'), { 
        target: { value: 'pass123' } 
      });

      // Submit form
      fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));

      await waitFor(() => {
        expect(api.post).toHaveBeenCalledWith('/api/signup', {
          email: 'test@example.com',
          password: 'pass123',
          username: 'testuser',
        });
      });

      expect(mockNavigate).toHaveBeenCalledWith('/auth/verify-otp', {
        state: { email: 'test@example.com' },
      });
    });

    test('signup with existing email shows error', async () => {
      const errorMessage = 'Email already exists';
      api.post.mockRejectedValueOnce({
        response: { data: { message: errorMessage } },
      });

      renderWithProviders(<Signup />);

      fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));

      fireEvent.change(screen.getByLabelText('Username'), { 
        target: { value: 'testuser' } 
      });
      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'existing@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'pass123' } 
      });
      fireEvent.change(screen.getByLabelText('Confirm Password'), { 
        target: { value: 'pass123' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));

      await waitFor(() => {
        expect(screen.getByText(errorMessage)).toBeInTheDocument();
      });

      expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('signup with mismatched passwords shows validation error', async () => {
      renderWithProviders(<Signup />);

      fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));

      fireEvent.change(screen.getByLabelText('Username'), { 
        target: { value: 'testuser' } 
      });
      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'pass123' } 
      });
      fireEvent.change(screen.getByLabelText('Confirm Password'), { 
        target: { value: 'different' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));

      await waitFor(() => {
        expect(screen.getByText("Passwords don't match")).toBeInTheDocument();
      });

      expect(api.post).not.toHaveBeenCalled();
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('signup with invalid password length shows validation error', async () => {
      renderWithProviders(<Signup />);

      fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));

      fireEvent.change(screen.getByLabelText('Username'), { 
        target: { value: 'testuser' } 
      });
      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: '12345' } 
      });
      fireEvent.change(screen.getByLabelText('Confirm Password'), { 
        target: { value: '12345' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));

      await waitFor(() => {
        expect(screen.getByText('Password must be between 6-8 characters')).toBeInTheDocument();
      });

      expect(api.post).not.toHaveBeenCalled();
    });
  });

  describe('Form Validation', () => {
    test('login form requires email and password', () => {
      renderWithProviders(<Login />);

      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');

      expect(emailInput).toBeRequired();
      expect(passwordInput).toBeRequired();
      expect(emailInput).toHaveAttribute('type', 'email');
      expect(passwordInput).toHaveAttribute('type', 'password');
    });

    test('signup form requires all fields', () => {
      renderWithProviders(<Signup />);

      fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));

      const usernameInput = screen.getByLabelText('Username');
      const emailInput = screen.getByLabelText('Email');
      const passwordInput = screen.getByLabelText('Password');
      const confirmPasswordInput = screen.getByLabelText('Confirm Password');

      expect(usernameInput).toBeRequired();
      expect(emailInput).toBeRequired();
      expect(passwordInput).toBeRequired();
      expect(confirmPasswordInput).toBeRequired();
      expect(emailInput).toHaveAttribute('type', 'email');
      expect(passwordInput).toHaveAttribute('type', 'password');
      expect(confirmPasswordInput).toHaveAttribute('type', 'password');
    });
  });

  describe('Loading States', () => {
    test('login shows loading state during authentication', async () => {
      // Mock a delayed response
      api.post.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      renderWithProviders(<Login />);

      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'password123' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /^login$/i }));

      expect(screen.getByText('Logging in...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /logging in/i })).toBeDisabled();
    });

    test('signup shows loading state during registration', async () => {
      api.post.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      renderWithProviders(<Signup />);

      fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));

      fireEvent.change(screen.getByLabelText('Username'), { 
        target: { value: 'testuser' } 
      });
      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'pass123' } 
      });
      fireEvent.change(screen.getByLabelText('Confirm Password'), { 
        target: { value: 'pass123' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));

      expect(screen.getByText('Signing up...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /signing up/i })).toBeDisabled();
    });
  });

  describe('Error Handling', () => {
    test('clears error when retrying login', async () => {
      api.post
        .mockRejectedValueOnce(new Error('First error'))
        .mockResolvedValueOnce({ data: { token: 'token', id: 1 } });

      renderWithProviders(<Login />);

      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'wrong' } 
      });

      // First attempt
      fireEvent.click(screen.getByRole('button', { name: /^login$/i }));

      await waitFor(() => {
        expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
      });

      // Second attempt
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'correct' } 
      });

      fireEvent.click(screen.getByRole('button', { name: /^login$/i }));

      await waitFor(() => {
        expect(screen.queryByText('Invalid credentials')).not.toBeInTheDocument();
      });
    });

    test('clears error when retrying signup', async () => {
      api.post
        .mockRejectedValueOnce(new Error('First error'))
        .mockResolvedValueOnce({ data: { success: true } });

      renderWithProviders(<Signup />);

      fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));

      fireEvent.change(screen.getByLabelText('Username'), { 
        target: { value: 'testuser' } 
      });
      fireEvent.change(screen.getByLabelText('Email'), { 
        target: { value: 'test@example.com' } 
      });
      fireEvent.change(screen.getByLabelText('Password'), { 
        target: { value: 'pass123' } 
      });
      fireEvent.change(screen.getByLabelText('Confirm Password'), { 
        target: { value: 'pass123' } 
      });

      // First attempt
      fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));

      await waitFor(() => {
        expect(screen.getByText('Failed to register')).toBeInTheDocument();
      });

      // Second attempt
      fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));

      await waitFor(() => {
        expect(screen.queryByText('Failed to register')).not.toBeInTheDocument();
      });
    });
  });
});
