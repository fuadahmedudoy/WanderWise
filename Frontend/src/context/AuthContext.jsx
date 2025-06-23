// Frontend/src/context/AuthContext.jsx
import React, { createContext, useState, useEffect, useCallback } from 'react';
import api from '../api'; // <-- Import our new api instance

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  
  const login = useCallback(async (email, password) => {
    try {
      const response = await api.post('/api/login', { email, password });
      const { token, ...user } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('currentUser', JSON.stringify(user));
      setCurrentUser(user);
      
      return response.data;
    } catch (error) {
      console.error('Login error:', error.response?.data || error.message);
      throw new Error(error.response?.data || 'Invalid credentials');
    }
  }, []);

  const signup = useCallback(async (email, password, userData) => {
    try {
      const response = await api.post('/api/signup', {
        email,
        password,
        username: userData.username,
      });
      return response.data;
    } catch (error) {
      console.error('Signup error:', error.response?.data || error.message);
      throw new Error(error.response?.data?.message || 'Failed to register');
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.post('/api/logout'); // The interceptor will add the token
      console.log("🔒 Token blacklisted on server");
    } catch (error) {
      console.error("❌ Error blacklisting token:", error);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('currentUser');
      setCurrentUser(null);
    }
  }, []);
  
  const signupWithGoogle = useCallback(() => {
    const googleLoginUrl = process.env.NODE_ENV === 'development'
      ? 'http://localhost:8080/oauth2/authorization/google'
      : '/oauth2/authorization/google';
    
    localStorage.setItem('awaitingOAuthReturn', 'true');
    window.location.href = googleLoginUrl;
  }, []);

  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const response = await api.get('/api/me');
          setCurrentUser(response.data);
          localStorage.setItem('currentUser', JSON.stringify(response.data));
          if(response.data.token && response.data.token !== token) {
            localStorage.setItem('token', response.data.token);
          }
        } catch (error) {
          console.error('Auth check failed, logging out.', error);
          logout();
        }
      }
      setLoading(false);
    };
    checkAuth();
  }, [logout]);

  const value = { currentUser, login, signup, logout, signupWithGoogle, setCurrentUser };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

export default AuthContext;