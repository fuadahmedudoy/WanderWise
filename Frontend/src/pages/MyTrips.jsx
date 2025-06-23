// Frontend/src/pages/MyTrips.jsx
import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import api from '../api'; // <-- Import api
import '../styles/trips.css';

const MyTrips = () => {
  const { currentUser } = useContext(AuthContext);
  const navigate = useNavigate();
  const [trips, setTrips] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  
  useEffect(() => {
    const fetchTrips = async () => {
      if (currentUser) {
        try {
          const response = await api.get('/api/trips');
          setTrips(response.data);
        } catch (error) {
          console.error("Failed to fetch trips", error);
        } finally {
          setIsLoading(false);
        }
      } else {
        setIsLoading(false);
      }
    };
    fetchTrips();
  }, [currentUser]);

  // ... (rest of the component is the same)
  const handleCreateTrip = () => {
    navigate('/create-trip');
  };
  
  const handleLogin = () => {
    navigate('/auth/login');
  };

  if (!currentUser) {
    return (
      <div className="auth-dialog-overlay">
        <div className="auth-dialog">
          <h2>Authentication Required</h2>
          <p>You need to login first to view your trips.</p>
          <div className="auth-dialog-actions">
            <button onClick={handleLogin} className="btn-primary">Login</button>
            <button onClick={() => navigate('/')} className="btn-outline">Go Back</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <header className="page-header">
        <div className="logo">WanderWise</div>
        <button onClick={() => navigate('/')} className="btn-outline">Back to Home</button>
      </header>
      
      <div className="content-container">
        <div className="trips-header">
          <h1>My Trips</h1>
          <button onClick={handleCreateTrip} className="btn-primary">Create New Trip</button>
        </div>
        
        {isLoading ? (
          <p>Loading trips...</p>
        ) : trips.length > 0 ? (
          <div className="trips-list">
            {trips.map(trip => (
              <div key={trip.id} className="trip-card">
                <div className="trip-info">
                  <h3>{trip.name}</h3>
                  <p className="trip-destination">{trip.destination}</p>
                  <p className="trip-dates">
                    {new Date(trip.startDate).toLocaleDateString()} - 
                    {new Date(trip.endDate).toLocaleDateString()}
                  </p>
                  <span className={`trip-status ${trip.status}`}>{trip.status}</span>
                </div>
                <div className="trip-actions">
                  <button className="btn-outline">View Details</button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <p>You haven't created any trips yet.</p>
            <button onClick={handleCreateTrip} className="btn-primary">Create Your First Trip</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyTrips;