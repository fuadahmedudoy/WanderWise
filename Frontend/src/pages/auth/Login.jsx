import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthContext from '../../context/AuthContext';
import '../../styles/auth.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, signupWithGoogle } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      setLoading(true);
      await login(email, password);
      navigate('/');
    } catch (error) {
      // Use the backend error message if available
      setError(error.message || 'Failed to login. Please check your credentials.');
      console.error(error);
    }
    setLoading(false);
  };

  const handleGoogleLogin = () => {
    try {
      setError('');
      setLoading(true);
      console.log("🚀 Login: Initiating Google login...");
      console.log("🔗 About to call signupWithGoogle() from AuthContext");
      
      // Clear any existing tokens before initiating OAuth flow
      localStorage.removeItem('token');
      localStorage.removeItem('currentUser');
      
      signupWithGoogle();
      console.log("✅ Login: signupWithGoogle() called successfully");
      // The redirect to Google OAuth will happen in the signupWithGoogle function
    } catch (error) {
      setLoading(false);
      console.error('❌ Login: Failed to login with Google:', error);
      setError('Failed to login with Google. Please try again.');
      console.error(error);
    }
  };

  return (
    <>
    <header className="auth-header">
        <div className="logo">WanderWise</div>
      </header>
    <div className="auth-container">
      <div className="auth-card">
         <button 
            className="auth-close-btn" 
            onClick={() => navigate('/')}
            aria-label="Close"
          >
            ×
          </button>
        <h1>Welcome Back</h1>
        <p>Login to plan your next adventure</p>
        
        {error && <div className="error-message">{error}</div>}
        
        <button 
          onClick={handleGoogleLogin} 
          className="btn-google" 
          disabled={loading}
        >
          <i className="fab fa-google"></i> Login with Google
        </button>
        
        <div className="divider">
          <span>OR</span>
        </div>
        
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          
          <button 
            type="submit" 
            className="btn-primary" 
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        
        <div className="auth-footer">
          <p>Don't have an account? <Link to="/auth/signup">Sign up</Link></p>
        </div>
      </div>
    </div>
    </>
  );
};

export default Login;