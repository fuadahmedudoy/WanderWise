import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { vi } from 'vitest';

// V7 CHANGE: Import createMemoryRouter and RouterProvider
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import Login from '../Login'; // CORRECTED: Relative path to the Login component
import AuthContext from '../../../context/AuthContext'; // CORRECTED: Default import

// NOTE: We no longer need to mock 'react-router-dom' because RouterProvider
// gives us a real router instance to work with in our tests.

describe('Login Component', () => {
  let mockLogin;
  let mockSignupWithGoogle;

  // Helper function to render the component with a real v7 router
  const renderComponent = (initialState = {}) => { // CORRECTED: Fixed syntax error here
    mockLogin = vi.fn();
    mockSignupWithGoogle = vi.fn();

    const authContextValue = {
      login: mockLogin,
      signupWithGoogle: mockSignupWithGoogle,
      // Add other context values your component might need
      user: null,
      loading: false,
      error: null,
    };

    // V7 CHANGE: Define the routes our test needs
    const routes = [
      {
        path: '/auth/login',
        element: <Login />,
      },
      {
        path: '/',
        element: <div>Home Page</div>,
      },
      {
        path: '/auth/signup',
        element: <div>Sign Up Page</div>,
      },
    ];

    // V7 CHANGE: Create a memory router instance
    const router = createMemoryRouter(routes, {
      // Start the test at the login page, passing state if needed
      initialEntries: [{ pathname: '/auth/login', state: initialState }],
    });

    render(
      <AuthContext.Provider value={authContextValue}>
        {/* V7 CHANGE: Use RouterProvider */}
        <RouterProvider router={router} />
      </AuthContext.Provider>
    );

    // Return the router instance if we need to inspect it later
    return { router };
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders the login form correctly', () => {
    renderComponent();
    expect(screen.getByRole('heading', { name: /welcome back/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument();
  });

  test('allows user to type in email and password fields', async () => {
    renderComponent();
    const user = userEvent.setup();

    const emailInput = screen.getByLabelText(/email/i);
    await user.type(emailInput, 'test@example.com');
    expect(emailInput).toHaveValue('test@example.com');
  });

  test('handles successful email/password login and navigates to home', async () => {
    // Redefine mockLogin for this specific test case
    mockLogin.mockResolvedValue({ success: true });
    const user = userEvent.setup();

    const { router } = renderComponent();

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: 'Login' }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123');
    });

    // V7 CHANGE: Assert navigation by checking for the content of the target page
    await waitFor(() => {
      expect(screen.getByText('Home Page')).toBeInTheDocument();
    });
  });

  test('displays an error message on failed login', async () => {
    const errorMessage = 'Invalid credentials';
    const user = userEvent.setup();

    // Create a mock that rejects with an error
    const failingMockLogin = vi.fn().mockRejectedValue(new Error(errorMessage));
    
    // Create a custom authContext for this test
    const authContextValue = {
      login: failingMockLogin,
      signupWithGoogle: vi.fn(),
      user: null,
      loading: false,
      error: null,
    };

    // V7 CHANGE: Define the routes our test needs
    const routes = [
      {
        path: '/auth/login',
        element: <Login />,
      },
      {
        path: '/',
        element: <div>Home Page</div>,
      },
      {
        path: '/auth/signup',
        element: <div>Sign Up Page</div>,
      },
    ];

    // V7 CHANGE: Create a memory router instance
    const router = createMemoryRouter(routes, {
      initialEntries: [{ pathname: '/auth/login' }],
    });

    render(
      <AuthContext.Provider value={authContextValue}>
        <RouterProvider router={router} />
      </AuthContext.Provider>
    );

    await user.type(screen.getByLabelText(/email/i), 'wrong@example.com');
    await user.type(screen.getByLabelText(/password/i), 'wrongpassword');
    await user.click(screen.getByRole('button', { name: 'Login' }));

    // Assert that the error message is displayed
    expect(await screen.findByText(errorMessage)).toBeInTheDocument();

    // Assert that navigation did NOT occur
    expect(router.state.location.pathname).toBe('/auth/login');
    expect(screen.queryByText('Home Page')).not.toBeInTheDocument();
  });

  test('calls signupWithGoogle when Google login button is clicked', async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.click(screen.getByRole('button', { name: /login with google/i }));
    expect(mockSignupWithGoogle).toHaveBeenCalledTimes(1);
  });

  test('navigates to signup page when "Sign up" link is clicked', async () => {
    const user = userEvent.setup();
    renderComponent();

    const signupLink = screen.getByRole('link', { name: /sign up/i });
    await user.click(signupLink);

    // Assert navigation by checking for the new page's content
    await waitFor(() => {
      expect(screen.getByText('Sign Up Page')).toBeInTheDocument();
    });
  });

  test('displays success message from location state', () => {
    const successMessage = 'Your account has been verified!';
    renderComponent({ message: successMessage });

    expect(screen.getByText(successMessage)).toBeInTheDocument();
  });
});