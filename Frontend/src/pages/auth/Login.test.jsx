import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import Login from './Login';
import AuthContext from '../../context/AuthContext';

// Mock the react-router-dom hooks
const mockNavigate = vi.fn();
const mockLocation = { state: null };

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => mockLocation,
  };
});

// Mock AuthContext
const mockLogin = vi.fn();
const mockSignupWithGoogle = vi.fn();

const MockAuthProvider = ({ children }) => {
  const value = {
    login: mockLogin,
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

describe('Login Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockLocation.state = null;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  test('renders login form correctly', () => {
    renderWithProviders(<Login />);
    
    expect(screen.getByText('Welcome Back')).toBeInTheDocument();
    expect(screen.getByText('Login to plan your next adventure')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login with google/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^login$/i })).toBeInTheDocument();
  });

  test('renders WanderWise logo', () => {
    renderWithProviders(<Login />);
    expect(screen.getByText('WanderWise')).toBeInTheDocument();
  });

  test('renders close button', () => {
    renderWithProviders(<Login />);
    expect(screen.getByRole('button', { name: /close/i })).toBeInTheDocument();
  });

  test('renders signup link', () => {
    renderWithProviders(<Login />);
    expect(screen.getByText("Don't have an account?")).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /sign up/i })).toBeInTheDocument();
  });

  test('updates email input value', () => {
    renderWithProviders(<Login />);
    const emailInput = screen.getByLabelText('Email');
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    expect(emailInput.value).toBe('test@example.com');
  });

  test('updates password input value', () => {
    renderWithProviders(<Login />);
    const passwordInput = screen.getByLabelText('Password');
    
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    expect(passwordInput.value).toBe('password123');
  });

  test('submits login form with correct credentials', async () => {
    mockLogin.mockResolvedValueOnce({ success: true });
    
    renderWithProviders(<Login />);
    
    fireEvent.change(screen.getByLabelText('Email'), { 
      target: { value: 'test@example.com' } 
    });
    fireEvent.change(screen.getByLabelText('Password'), { 
      target: { value: 'password123' } 
    });
    
    fireEvent.click(screen.getByRole('button', { name: /^login$/i }));
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123');
    });
    
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  test('displays error message on login failure', async () => {
    const errorMessage = 'Invalid credentials';
    mockLogin.mockRejectedValueOnce(new Error(errorMessage));
    
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
  });

  test('shows loading state during login', async () => {
    mockLogin.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
    
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

  test('handles Google login', () => {
    renderWithProviders(<Login />);
    
    fireEvent.click(screen.getByRole('button', { name: /login with google/i }));
    
    expect(mockSignupWithGoogle).toHaveBeenCalled();
  });

  test('shows loading state during Google login', () => {
    renderWithProviders(<Login />);
    
    fireEvent.click(screen.getByRole('button', { name: /login with google/i }));
    
    expect(screen.getByRole('button', { name: /login with google/i })).toBeDisabled();
  });

  test('handles Google login error', () => {
    mockSignupWithGoogle.mockImplementation(() => {
      throw new Error('Google login failed');
    });
    
    renderWithProviders(<Login />);
    
    fireEvent.click(screen.getByRole('button', { name: /login with google/i }));
    
    expect(screen.getByText('Failed to login with Google. Please try again.')).toBeInTheDocument();
  });

  test('displays success message from location state', () => {
    mockLocation.state = { message: 'Email verified successfully!' };
    
    renderWithProviders(<Login />);
    
    expect(screen.getByText('Email verified successfully!')).toBeInTheDocument();
  });

  test('closes modal when close button is clicked', () => {
    renderWithProviders(<Login />);
    
    fireEvent.click(screen.getByRole('button', { name: /close/i }));
    
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  test('form validation - requires email and password', () => {
    renderWithProviders(<Login />);
    
    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    
    expect(emailInput).toBeRequired();
    expect(passwordInput).toBeRequired();
  });

  test('email input accepts only email format', () => {
    renderWithProviders(<Login />);
    
    const emailInput = screen.getByLabelText('Email');
    expect(emailInput).toHaveAttribute('type', 'email');
  });

  test('password input is hidden', () => {
    renderWithProviders(<Login />);
    
    const passwordInput = screen.getByLabelText('Password');
    expect(passwordInput).toHaveAttribute('type', 'password');
  });

  test('clears error message when form is resubmitted', async () => {
    mockLogin
      .mockRejectedValueOnce(new Error('First error'))
      .mockResolvedValueOnce({ success: true });
    
    renderWithProviders(<Login />);
    
    // First submission with error
    fireEvent.change(screen.getByLabelText('Email'), { 
      target: { value: 'test@example.com' } 
    });
    fireEvent.change(screen.getByLabelText('Password'), { 
      target: { value: 'wrong' } 
    });
    
    fireEvent.click(screen.getByRole('button', { name: /^login$/i }));
    
    await waitFor(() => {
      expect(screen.getByText('First error')).toBeInTheDocument();
    });
    
    // Second submission (should clear error)
    fireEvent.change(screen.getByLabelText('Password'), { 
      target: { value: 'correct' } 
    });
    
    fireEvent.click(screen.getByRole('button', { name: /^login$/i }));
    
    await waitFor(() => {
      expect(screen.queryByText('First error')).not.toBeInTheDocument();
    });
  });

  test('handles form submission with empty fields', async () => {
    renderWithProviders(<Login />);
    
    fireEvent.click(screen.getByRole('button', { name: /^login$/i }));
    
    // The form should not be submitted if required fields are empty
    expect(mockLogin).not.toHaveBeenCalled();
  });
});
