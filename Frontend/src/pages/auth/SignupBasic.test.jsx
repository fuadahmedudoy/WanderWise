import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Simple signup component test
describe('Signup Component Basic Test', () => {
  // Mock signup function
  const mockSignup = vi.fn();
  
  // Simple signup component for testing
  const SimpleSignup = () => {
    const [email, setEmail] = React.useState('');
    const [password, setPassword] = React.useState('');
    const [confirmPassword, setConfirmPassword] = React.useState('');
    const [username, setUsername] = React.useState('');
    const [error, setError] = React.useState('');
    const [showForm, setShowForm] = React.useState(false);

    const handleSubmit = (e) => {
      e.preventDefault();
      
      if (password !== confirmPassword) {
        setError("Passwords don't match");
        return;
      }
      
      if (password.length < 6 || password.length > 8) {
        setError("Password must be between 6-8 characters");
        return;
      }
      
      setError('');
      mockSignup(email, password, { username });
    };

    return (
      <div>
        <h1>Create an Account</h1>
        <p>Join us and start planning your trips</p>
        
        {error && <div>{error}</div>}
        
        {!showForm ? (
          <div>
            <button onClick={() => setShowForm(true)}>Sign up with Email</button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <label>
              Email:
              <input 
                type="email" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)}
                required 
              />
            </label>
            
            <label>
              Username:
              <input 
                type="text" 
                value={username} 
                onChange={(e) => setUsername(e.target.value)}
                required 
              />
            </label>
            
            <label>
              Password:
              <input 
                type="password" 
                value={password} 
                onChange={(e) => setPassword(e.target.value)}
                required 
              />
            </label>
            
            <label>
              Confirm Password:
              <input 
                type="password" 
                value={confirmPassword} 
                onChange={(e) => setConfirmPassword(e.target.value)}
                required 
              />
            </label>
            
            <button type="submit">Sign Up</button>
          </form>
        )}
      </div>
    );
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders signup component', () => {
    render(<SimpleSignup />);
    
    expect(screen.getByText('Create an Account')).toBeInTheDocument();
    expect(screen.getByText('Join us and start planning your trips')).toBeInTheDocument();
    expect(screen.getByText('Sign up with Email')).toBeInTheDocument();
  });

  test('shows form when email signup is clicked', async () => {
    render(<SimpleSignup />);
    
    fireEvent.click(screen.getByText('Sign up with Email'));
    
    await waitFor(() => {
      expect(screen.getByLabelText('Email:')).toBeInTheDocument();
      expect(screen.getByLabelText('Password:')).toBeInTheDocument();
      expect(screen.getByLabelText('Confirm Password:')).toBeInTheDocument();
    });
  });

  test('validates password mismatch', async () => {
    render(<SimpleSignup />);
    
    fireEvent.click(screen.getByText('Sign up with Email'));
    
    await waitFor(() => {
      fireEvent.change(screen.getByLabelText('Email:'), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'testuser' } });
      fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'pass123' } });
      fireEvent.change(screen.getByLabelText('Confirm Password:'), { target: { value: 'pass456' } });
    });
    
    fireEvent.click(screen.getByRole('button', { name: 'Sign Up' }));
    
    await waitFor(() => {
      expect(screen.getByText("Passwords don't match")).toBeInTheDocument();
    });
  });

  test('validates password length', async () => {
    render(<SimpleSignup />);
    
    fireEvent.click(screen.getByText('Sign up with Email'));
    
    await waitFor(() => {
      fireEvent.change(screen.getByLabelText('Email:'), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'testuser' } });
      fireEvent.change(screen.getByLabelText('Password:'), { target: { value: '12345' } });
      fireEvent.change(screen.getByLabelText('Confirm Password:'), { target: { value: '12345' } });
    });
    
    fireEvent.click(screen.getByRole('button', { name: 'Sign Up' }));
    
    await waitFor(() => {
      expect(screen.getByText('Password must be between 6-8 characters')).toBeInTheDocument();
    });
  });

  test('calls signup function on valid submission', async () => {
    render(<SimpleSignup />);
    
    fireEvent.click(screen.getByText('Sign up with Email'));
    
    await waitFor(() => {
      fireEvent.change(screen.getByLabelText('Email:'), { target: { value: 'test@example.com' } });
      fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'testuser' } });
      fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'pass123' } });
      fireEvent.change(screen.getByLabelText('Confirm Password:'), { target: { value: 'pass123' } });
    });
    
    fireEvent.click(screen.getByRole('button', { name: 'Sign Up' }));
    
    await waitFor(() => {
      expect(mockSignup).toHaveBeenCalledWith('test@example.com', 'pass123', { username: 'testuser' });
    });
  });
});
