import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import Signup from './Signup';
import AuthContext from '../../context/AuthContext';

// Mock the react-router-dom hooks
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock AuthContext
const mockSignup = vi.fn();
const mockSignupWithGoogle = vi.fn();

const MockAuthProvider = ({ children }) => {
  const value = {
    signup: mockSignup,
    signupWithGoogle: mockSignupWithGoogle,
    currentUser: null,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// Helper function to render component with providers
const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      <MockAuthProvider>
        {component}
      </MockAuthProvider>
    </BrowserRouter>
  );
};

describe('Signup Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  test('renders signup component correctly', () => {
    renderWithProviders(<Signup />);
    
    expect(screen.getByText('Create an Account')).toBeInTheDocument();
    expect(screen.getByText('Join us and start planning your trips')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign up with google/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign up with email/i })).toBeInTheDocument();
  });

  test('renders WanderWise logo', () => {
    renderWithProviders(<Signup />);
    expect(screen.getByText('WanderWise')).toBeInTheDocument();
  });

  test('renders login link', () => {
    renderWithProviders(<Signup />);
    expect(screen.getByText('Already have an account?')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /login/i })).toBeInTheDocument();
  });

  test('shows email form when "Sign up with Email" is clicked', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /back/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign up$/i })).toBeInTheDocument();
  });

  test('goes back to initial view when Back button is clicked', () => {
    renderWithProviders(<Signup />);
    
    // Show form
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    
    // Go back
    fireEvent.click(screen.getByRole('button', { name: /back/i }));
    expect(screen.queryByLabelText('Username')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign up with email/i })).toBeInTheDocument();
  });

  test('updates input values correctly', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    const usernameInput = screen.getByLabelText('Username');
    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const confirmPasswordInput = screen.getByLabelText('Confirm Password');
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });
    
    expect(usernameInput.value).toBe('testuser');
    expect(emailInput.value).toBe('test@example.com');
    expect(passwordInput.value).toBe('password123');
    expect(confirmPasswordInput.value).toBe('password123');
  });

  test('shows error when passwords do not match', async () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'different' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(screen.getByText("Passwords don't match")).toBeInTheDocument();
    });
    
    expect(mockSignup).not.toHaveBeenCalled();
  });

  test('shows error when password length is invalid', async () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: '123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: '123' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(screen.getByText('Password must be between 6-8 characters')).toBeInTheDocument();
    });
    
    expect(mockSignup).not.toHaveBeenCalled();
  });

  test('shows error when password is too long', async () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'toolongpassword' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'toolongpassword' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(screen.getByText('Password must be between 6-8 characters')).toBeInTheDocument();
    });
    
    expect(mockSignup).not.toHaveBeenCalled();
  });

  test('submits form with valid data', async () => {
    mockSignup.mockResolvedValueOnce({ success: true });
    
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'pass123' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(mockSignup).toHaveBeenCalledWith('test@example.com', 'pass123', { username: 'testuser' });
    });
    
    expect(mockNavigate).toHaveBeenCalledWith('/auth/verify-otp', { 
      state: { email: 'test@example.com' } 
    });
  });

  test('displays error message on signup failure', async () => {
    const errorMessage = 'Email already exists';
    mockSignup.mockRejectedValueOnce(new Error(errorMessage));
    
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'pass123' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  test('shows loading state during signup', async () => {
    mockSignup.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
    
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'pass123' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    expect(screen.getByText('Signing up...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /signing up/i })).toBeDisabled();
  });

  test('handles Google signup', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with google/i }));
    
    expect(mockSignupWithGoogle).toHaveBeenCalled();
  });

  test('shows loading state during Google signup', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with google/i }));
    
    expect(screen.getByRole('button', { name: /sign up with google/i })).toBeDisabled();
  });

  test('handles Google signup error', () => {
    mockSignupWithGoogle.mockImplementation(() => {
      throw new Error('Google signup failed');
    });
    
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with google/i }));
    
    expect(screen.getByText('Failed to sign up with Google')).toBeInTheDocument();
  });

  test('renders password requirement hint', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    expect(screen.getByText('Password must be 6-8 characters long')).toBeInTheDocument();
  });

  test('form fields are required', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    expect(screen.getByLabelText('Username')).toBeRequired();
    expect(screen.getByLabelText('Email')).toBeRequired();
    expect(screen.getByLabelText('Password')).toBeRequired();
    expect(screen.getByLabelText('Confirm Password')).toBeRequired();
  });

  test('email input accepts only email format', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    const emailInput = screen.getByLabelText('Email');
    expect(emailInput).toHaveAttribute('type', 'email');
  });

  test('password inputs are hidden', () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    expect(screen.getByLabelText('Password')).toHaveAttribute('type', 'password');
    expect(screen.getByLabelText('Confirm Password')).toHaveAttribute('type', 'password');
  });

  test('clears error message when form is resubmitted', async () => {
    mockSignup
      .mockRejectedValueOnce(new Error('First error'))
      .mockResolvedValueOnce({ success: true });
    
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    // Fill form with invalid data first
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'pass123' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(screen.getByText('First error')).toBeInTheDocument();
    });
    
    // Retry submission (should clear error)
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(screen.queryByText('First error')).not.toBeInTheDocument();
    });
  });

  test('handles form submission with empty fields', async () => {
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    // The form should not be submitted if required fields are empty
    expect(mockSignup).not.toHaveBeenCalled();
  });

  test('shows close button in initial view', () => {
    renderWithProviders(<Signup />);
    
    // The close button should be visible in the initial view (before showing form)
    expect(screen.getByRole('button', { name: /close/i })).toBeInTheDocument();
  });

  test('password validation with exactly 6 characters', async () => {
    mockSignup.mockResolvedValueOnce({ success: true });
    
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass12' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'pass12' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(mockSignup).toHaveBeenCalledWith('test@example.com', 'pass12', { username: 'testuser' });
    });
  });

  test('password validation with exactly 8 characters', async () => {
    mockSignup.mockResolvedValueOnce({ success: true });
    
    renderWithProviders(<Signup />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign up with email/i }));
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass1234' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'pass1234' } });
    
    fireEvent.click(screen.getByRole('button', { name: /sign up$/i }));
    
    await waitFor(() => {
      expect(mockSignup).toHaveBeenCalledWith('test@example.com', 'pass1234', { username: 'testuser' });
    });
  });
});
