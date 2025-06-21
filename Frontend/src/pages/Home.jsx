


import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import axios from 'axios';
import { FaStar } from 'react-icons/fa';
import '../styles/home.css';

//const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Home = () => {
  const { currentUser, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [featuredDestinations, setFeaturedDestinations] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Use localStorage as a fallback if currentUser is null
  const userFromStorage = !currentUser && localStorage.getItem('currentUser') 
    ? JSON.parse(localStorage.getItem('currentUser')) 
    : null;
  
  const effectiveUser = currentUser || userFromStorage;

  // Fetch featured destinations from the backend
  useEffect(() => {
    const fetchFeaturedDestinations = async () => {
      try {
        setIsLoading(true);
        setError(null);
        
        const token = localStorage.getItem('token');
        const config = token ? { 
          headers: { Authorization: `Bearer ${token}` }
        } : {};
        
        // Make API call to get featured destinations
        // const response = await axios.get(`${API_BASE_URL}/destinations/featured`, config);
        const response = await axios.get('/api/destinations/featured', config);
        
        setFeaturedDestinations(response.data);
        setIsLoading(false);
      } catch (err) {
        console.error('Error fetching featured destinations:', err);
        setError('Failed to load featured destinations. Please try again later.');
        setIsLoading(false);
      }
    };

    fetchFeaturedDestinations();
  }, []);

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/auth/login');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  };

  const handleLogin = () => {
    navigate('/auth/login');
  }

  const navigateToCreateTrip = () => {
    navigate('/create-trip');
  };
  
  const navigateToMyTrips = () => {
    navigate('/my-trips');
  };
  
  const navigateToDestination = (id) => {
    navigate(`/destination/${id}`);
  };

  return (
    <div className="home-container">
      <nav className="navbar">
        <div className="logo">WanderWise</div>
        {effectiveUser ? (
          <div className="nav-buttons">
            <button onClick={handleLogout} className="btn-outline">Logout</button>
          </div>
        ) : (
          <div className="nav-buttons">
            <button onClick={handleLogin} className="btn-outline">Login</button>
          </div>
        )}
      </nav>
      
      <div className="welcome-section">
        {effectiveUser ? (
          <h1>Welcome, {effectiveUser.username || effectiveUser.email}!</h1>
        ) : (
          <h1>Welcome to WanderWise!</h1>
        )}
        <p>Discover amazing destinations and plan your perfect trip.</p>
        <p>Checking ci/cd 2</p>
        <div className="action-buttons">
          <button className="btn-primary" onClick={navigateToCreateTrip}>Create New Trip</button>
          <button className="btn-secondary" onClick={navigateToMyTrips}>View My Trips</button>
        </div>
      </div>
      
      <div className="featured-section">
        <h2>Featured Destinations</h2>
        
        {isLoading && (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading featured destinations...</p>
          </div>
        )}
        
        {error && (
          <div className="error-message">
            {error}
          </div>
        )}
        
        {!isLoading && !error && (
          <div className="destination-cards">
            {featuredDestinations.length > 0 ? (
              featuredDestinations.map(destination => (
                <div className="destination-card" key={destination.id} onClick={() => navigateToDestination(destination.id)}>
                  <div 
                    className="card-image" 
                    style={{ backgroundImage: `url(${destination.imageUrl})` }}
                  ></div>
                  <div className="card-content">
                    <h3>{destination.title}</h3>
                    <p className="destination-location">{destination.destination}</p>
                    <div className="destination-meta">
                      <span className="destination-days">{destination.days} days</span>
                      <div className="destination-rating">
                        <FaStar className="star-icon" />
                        <span>{destination.avgRating.toFixed(1)}</span>
                      </div>
                    </div>
                    <p className="destination-description">{destination.description.substring(0, 100)}...</p>
                    <button className="btn-outline view-details">View Details</button>
                  </div>
                </div>
              ))
            ) : (
              <div className="no-trips-message">
                <p>No featured destinations available at the moment.</p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Home;