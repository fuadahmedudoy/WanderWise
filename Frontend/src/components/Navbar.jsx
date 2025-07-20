import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import NotificationCenter from './NotificationCenter';
import '../styles/Navbar.css';

const Navbar = () => {
  const { currentUser, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  
  // Get user from localStorage if not in context
  const effectiveUser = currentUser || 
    (localStorage.getItem('currentUser') ? JSON.parse(localStorage.getItem('currentUser')) : null);

  const handleLogin = () => {
    navigate('/auth/login');
  };

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="logo" onClick={() => navigate('/')}>WanderWise</div>
      
      {effectiveUser ? (
        <div className="action-buttons">
          <button onClick={() => navigate('/create-blog')} className="btn-outline">Write Blog</button> {/* NEW */}
          <button onClick={() => navigate('/my-trips')} className="btn-outline nav-btn">My Trips</button>
          <button onClick={() => navigate('/group-trips')} className="btn-outline nav-btn">Group Trips</button>
          <NotificationCenter />
          {effectiveUser.role === 'ADMIN' && (
            <button onClick={() => navigate('/admin')} className="btn-outline admin-btn">Admin Dashboard</button>
          )}
          <button onClick={() => navigate('/profile')} className="btn-outline">Profile</button>
          <button onClick={handleLogout} className="btn-outline">Logout</button>
        </div>
      ) : (
        <div className="nav-buttons">
          <button onClick={handleLogin} className="btn-outline">Login</button>
        </div>
      )}
    </nav>
  );
};

export default Navbar;