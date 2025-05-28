import React, { useEffect, useContext, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import AuthContext from '../../context/AuthContext';
import axios from 'axios';
import '../../styles/auth.css';

const OAuthSuccess = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setCurrentUser, setToken } = useContext(AuthContext);
  const [message, setMessage] = useState('Processing Google authentication...');
  const [error, setError] = useState(null);
  const [isProcessing, setIsProcessing] = useState(true);

  useEffect(() => {
    console.log("🔐 OAuth Success component mounted");
    console.log("🔍 Full URL:", window.location.href);
    console.log("🔍 Search params:", location.search);
    
    const urlParams = new URLSearchParams(location.search);
    const authToken = urlParams.get('token');
    
    console.log("🎫 Token from URL:", authToken ? `✅ Found (length: ${authToken.length})` : "❌ No token found");
    
    // Check if we were expecting an OAuth return
    const awaitingOAuth = localStorage.getItem('awaitingOAuthReturn');
    console.log("🔍 Awaiting OAuth return:", awaitingOAuth ? "✅ Yes" : "❌ No");
    
    // Clear the flag regardless of outcome
    localStorage.removeItem('awaitingOAuthReturn');
    
    // Function to handle Google OAuth authentication
    const processGoogleAuth = async () => {
      if (authToken) {
        try {
          console.log("🚀 Processing Google OAuth token...");
          setMessage("Google authentication successful! Setting up your account...");
          
          // Validate token structure before trying to decode
          if (authToken.split('.').length !== 3) {
            throw new Error("Invalid JWT token structure");
          }
          
          // Decode user info from JWT token
          let userEmail = "google-user@gmail.com";
          let userName = "Google User";
          
          try {
            // Decode JWT to get user info
            const payload = JSON.parse(atob(authToken.split('.')[1]));
            console.log("📄 JWT payload:", payload);
            
            // Extract user info from JWT
            userEmail = payload.sub || payload.email || userEmail;
            userName = payload.name || payload.preferred_username || userName;
          } catch (decodeError) {
            console.warn("⚠️ Could not decode JWT, using defaults:", decodeError);
          }
          
          // Create user data exactly like normal login response
          const userData = {
            username: userName,
            email: userEmail,
            role: "USER"
          };
          
          console.log("👤 User data created:", userData);
          
          // Do exactly what normal login does after successful API response:
          
          // 1. Store data in localStorage first (this is synchronous)
          localStorage.setItem('token', authToken);
          localStorage.setItem('currentUser', JSON.stringify(userData));
          console.log("💾 Data saved to localStorage");
          
          // 2. Set axios authorization header
          axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`;
          console.log("🔗 Authorization header set for future requests");
          
          // 3. Set token and user in context
          console.log("🔄 Setting token and user in AuthContext...");
          setToken(authToken);
          setCurrentUser(userData);
          
          setMessage("Authentication complete! Redirecting to Trip Planner...");
          
          // 4. Clean URL - do this last so we can still debug if needed
          window.history.replaceState({}, document.title, window.location.pathname);
          console.log("🧹 URL parameters cleaned");
          
          // Wait a moment then redirect (like normal login)
          setTimeout(() => {
            console.log("🔍 Final verification before redirect:");
            console.log("   - Token in localStorage:", localStorage.getItem('token') ? "✅" : "❌");
            console.log("   - User in localStorage:", localStorage.getItem('currentUser') ? "✅" : "❌");
            
            console.log("🎯 Redirecting to Trip Planner home page...");
            setIsProcessing(false);
            navigate('/', { replace: true });
          }, 2000);
          
        } catch (error) {
          console.error('❌ Google OAuth processing failed:', error);
          setMessage('Google authentication failed. Please try again.');
          setError(error.message || 'Authentication processing error');
          setIsProcessing(false);
          
          // Redirect to login on error
          setTimeout(() => {
            console.log("🔄 Redirecting to login due to error...");
            navigate('/auth/login', { replace: true });
          }, 3000);
        }
      } else {
        // No token in URL
        console.log("❌ No authentication token found in URL");
        console.log("🔍 Available URL params:", Object.fromEntries(urlParams));
        
        setMessage('Google authentication token not found. Please try signing in again.');
        setError('Missing authentication token in URL');
        setIsProcessing(false);
        
        // Redirect to login
        setTimeout(() => {
          console.log("🔄 Redirecting to login due to missing token...");
          navigate('/auth/login', { replace: true });
        }, 3000);
      }
    };
    
    // Start processing only if this component is still mounting (prevents double processing)
    if (isProcessing) {
      processGoogleAuth();
    }
    
    // Cleanup function
    return () => {
      setIsProcessing(false);
    };
    
  }, [navigate, location.search, setToken, setCurrentUser]);
  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="text-center">
          <h2>🚀 Trip Planner</h2>
          <h3>Google Authentication</h3>
          
          <div className="mt-4">
            {error ? (
              <div className="error-message">
                <p>❌ {error}</p>
                <button 
                  onClick={() => navigate('/auth/login')} 
                  className="btn-primary mt-3"
                >
                  Return to Login
                </button>
              </div>
            ) : (
              <div className="success-message">
                <p>✅ {message}</p>
                {isProcessing && <div className="loading-spinner mt-3"></div>}
              </div>
            )}
          </div>
          
          {/* Progress indicator */}
          <div className="mt-4">
            <div className="progress-steps">
              <div className="step completed">Google Sign-in</div>
              <div className="step active">{isProcessing ? 'Processing' : error ? 'Error' : 'Complete'}</div>
              <div className="step">{!isProcessing && !error ? 'Complete' : 'Trip Planner'}</div>
            </div>
          </div>
          
          {/* Debug info for development */}
          {process.env.NODE_ENV === 'development' && (
            <div style={{ 
              marginTop: '30px', 
              padding: '15px', 
              fontSize: '12px', 
              color: '#666',
              backgroundColor: '#f8f9fa',
              borderRadius: '8px',
              textAlign: 'left'
            }}>
              <h5>🔧 Debug Info:</h5>
              <p>• Token in URL: {location.search.includes('token=') ? '✅ Yes' : '❌ No'}</p>
              <p>• Token in localStorage: {localStorage.getItem('token') ? '✅ Present' : '❌ Not present'}</p>
              <p>• User in localStorage: {localStorage.getItem('currentUser') ? '✅ Present' : '❌ Not present'}</p>
              <p>• Current Route: {location.pathname}</p>
              <p>• Processing: {isProcessing ? '✅ Yes' : '❌ No'}</p>
              <p>• Target: Trip Planner Home</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default OAuthSuccess;