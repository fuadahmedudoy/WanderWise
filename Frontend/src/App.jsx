import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import AuthContext from './context/AuthContext';
import Login from './pages/auth/Login';
import Signup from './pages/auth/Signup';
import OAuthSuccess from './pages/auth/OAuthSuccess';
import Home from './pages/Home';
import CreateTrip from './pages/CreateTrip';
import MyTrips from './pages/MyTrips';
import DestinationDetail from './pages/DestinationDetail';
import './styles/global.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/auth/login" element={<Login />} />
          <Route path="/auth/signup" element={<Signup />} />
          <Route path="/oauth-success" element={<OAuthSuccess />} />
          <Route path="/" element={<Home />} />
          <Route path="/destination/:id" element={<DestinationDetail />} />
          <Route 
            path="/create-trip" 
            element={
              <RequireAuth>
                <CreateTrip />
              </RequireAuth>
            } 
          />
          <Route 
            path="/my-trips" 
            element={
              <RequireAuth>
                <MyTrips />
              </RequireAuth>
            } 
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

// Component to handle protected routes
function RequireAuth({ children }) {
  const { currentUser } = useContext(AuthContext);
  
  console.log("🔐 RequireAuth: Checking authentication state...");
  console.log("🔐 Current user from context:", currentUser ? "✅ User present" : "❌ No user");
  if (currentUser) {
    console.log("👤 User details:", JSON.stringify(currentUser, null, 2));
  }
  
  // Check localStorage as a fallback
  if (!currentUser) {
    const savedUser = localStorage.getItem('currentUser');
    const savedToken = localStorage.getItem('token');
    
    console.log("🔍 RequireAuth: Checking localStorage fallback...");
    console.log("💾 Saved user in localStorage:", savedUser ? "✅ Found" : "❌ Not found");
    console.log("💾 Saved token in localStorage:", savedToken ? "✅ Found" : "❌ Not found");
    
    if (savedUser) {
      // User data exists in localStorage, allow access
      console.log("✅ RequireAuth: Found user data in localStorage, allowing access");
      try {
        const userData = JSON.parse(savedUser);
        console.log("👤 localStorage user data:", JSON.stringify(userData, null, 2));
      } catch (e) {
        console.error("❌ Error parsing saved user data:", e);
      }
      return children;
    }
    
    console.log("❌ RequireAuth: No user data found, redirecting to login");
    return <Navigate to="/auth/login" replace />;
  }
  
  console.log("✅ RequireAuth: User authenticated, allowing access");
  return children;
}

export default App;