import React, { createContext, useState, useEffect } from 'react';
import axios from 'axios';

export const AuthContext = createContext();

// Use the environment variable provided by Docker Compose. Provide a fallback for local development.
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
// Construct the base URL for OAuth, which doesn't have the /api suffix
const OAUTH_BASE_URL = API_BASE_URL.replace('/api', '');

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  
  // Expose methods to update auth state globally
  window.updateCurrentUser = setCurrentUser;
  window.updateAuthToken = setToken;

  // Configure axios with JWT token
  useEffect(() => {
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete axios.defaults.headers.common['Authorization'];
    }
  }, [token]);

  // Sign up with email and password
  const signup = async (email, password, userData) => {
    try {
      // Match exactly the backend RegisterRequest format
      const response = await axios.post(`${API_BASE_URL}/signup`, {
        email,
        password,
        username: userData.username
        // No fullName or address fields in backend RegisterRequest
      });
      
      return response.data;
    } catch (error) {
      console.error('Signup error:', error.response?.data || error.message);
      throw new Error(error.response?.data?.message || 'Failed to register');
    }
  };

  // Login with email and password
  const login = async (email, password) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/login`, {
        email,
        password
      });
      
      // The backend returns: token, username, email, role
      const { token: authToken, username, email: userEmail, role } = response.data;
      const userInfo = { username, email: userEmail, role };
      
      setToken(authToken);
      setCurrentUser(userInfo);
      localStorage.setItem('token', authToken);
      localStorage.setItem('currentUser', JSON.stringify(userInfo));
      
      return userInfo;
    } catch (error) {
      console.error('Login error:', error.response?.data || error.message);
      throw new Error(error.response?.data || 'Invalid credentials');
    }
  };

  // Sign up/login with Google
  const signupWithGoogle = () => {
    console.log("🚀 Initiating Google OAuth2 login...");
    
    // Before redirecting, store a flag that we're expecting an OAuth return
    localStorage.setItem('awaitingOAuthReturn', 'true');
    console.log("🏃 Set awaitingOAuthReturn flag in localStorage");
    
    // Redirect to Google OAuth
    console.log("➡️ Redirecting to Google OAuth2...");
    window.location.href = `${OAUTH_BASE_URL}/oauth2/authorization/google`;
  };

  // Logout
  // const logout = () => {
  //   return new Promise((resolve) => {
  //     setCurrentUser(null);
  //     setToken(null);
  //     localStorage.removeItem('currentUser');
  //     localStorage.removeItem('token');
  //     resolve();
  //   });
  // };

  const logout = async () => {
  const currentToken = localStorage.getItem('token');
  
  try {
    if (currentToken) {
      // Call backend logout endpoint to blacklist the token
      await axios.post(`${API_BASE_URL}/logout`, {}, {
        headers: {
          'Authorization': `Bearer ${currentToken}`
        }
      });
      console.log("🔒 Token blacklisted on server");
    }
  } catch (error) {
    console.error("❌ Error blacklisting token:", error);
    // Continue with logout even if server call fails
  } finally {
    // Always clear local state
    setCurrentUser(null);
    setToken(null);
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
  }
  return Promise.resolve();
  };

  // Check authentication status on page load
  useEffect(() => {
    const checkAuth = async () => {
      const savedToken = localStorage.getItem('token');
      console.log("AuthContext: Checking saved token...", savedToken ? "Token exists" : "No token");
      
      if (savedToken) {
        try {
          // Set token for authenticated requests
          axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`;
          console.log("AuthContext: Setting Authorization header");
          
          // Get current user info
          console.log("AuthContext: Fetching user info from API");
          const response = await axios.get(`${API_BASE_URL}/me`);
          console.log("AuthContext: User data received:", response.data);
          
          setCurrentUser(response.data);
          setToken(savedToken);
          
          // Store user data in localStorage
          localStorage.setItem('currentUser', JSON.stringify(response.data));
        } catch (error) {
          // Token might be expired or invalid
          console.error('Auth check error:', error);
          localStorage.removeItem('token');
          localStorage.removeItem('currentUser');
        }
      } else {
        // Try to get user data from localStorage as fallback
        const savedUser = localStorage.getItem('currentUser');
        if (savedUser) {
          console.log("AuthContext: Found saved user data in localStorage");
          setCurrentUser(JSON.parse(savedUser));
        }
      }
      
      setLoading(false);
    };
    
    checkAuth();
  }, []);

  // Note: OAuth token handling is now done in OAuthSuccess.jsx component
  // This prevents duplicate processing and URL parameter conflicts

  const value = {
    currentUser,
    token,
    signup,
    login,
    signupWithGoogle,
    logout,
     setToken, // <-- added for OAuthSuccess
    setCurrentUser // <-- added for OAuthSuccess
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

export default AuthContext;