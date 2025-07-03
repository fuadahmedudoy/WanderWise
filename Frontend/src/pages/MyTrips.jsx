// Frontend/src/pages/MyTrips.jsx
import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import { tripApi } from '../api'; // <-- Import tripApi
import '../styles/trips.css';

const MyTrips = () => {
  const { currentUser } = useContext(AuthContext);
  const navigate = useNavigate();
  const [trips, setTrips] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  
  useEffect(() => {
    const fetchTrips = async () => {
      if (currentUser) {
        try {
          const response = await tripApi.getMyAcceptedTrips();
          if (response.success) {
            setTrips(response.trips);
          } else {
            setError(response.error || 'Failed to fetch trips');
          }
        } catch (error) {
          console.error("Failed to fetch trips", error);
          setError('Network error. Please try again.');
        } finally {
          setIsLoading(false);
        }
      } else {
        setIsLoading(false);
      }
    };
    fetchTrips();
  }, [currentUser]);

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
          <h1>My Accepted Trips</h1>
          <button onClick={() => navigate('/travel-planner')} className="btn-primary">
            Plan New Trip
          </button>
        </div>
        
        {isLoading ? (
          <div className="loading-container">
            <p>Loading your accepted trips...</p>
          </div>
        ) : error ? (
          <div className="error-container">
            <p className="error-message">{error}</p>
            <button onClick={() => window.location.reload()} className="btn-primary">
              Try Again
            </button>
          </div>
        ) : trips.length > 0 ? (
          <div className="trips-list">
            {trips.map(trip => {
              // Extract trip summary data from the nested structure
              const tripSummary = trip.tripPlan?.trip_summary || {};
              const destination = tripSummary.destination || trip.tripPlan?.destination || 'Unknown Destination';
              const duration = tripSummary.duration || trip.tripPlan?.duration_days || null;
              const budget = tripSummary.total_budget || trip.tripPlan?.budget || null;
              const startDate = tripSummary.start_date || trip.tripPlan?.start_date || null;
              const origin = tripSummary.origin || trip.tripPlan?.origin || null;
              
              return (
                <div key={trip.id} className="trip-card">
                  <div className="trip-info">
                    <h3>{destination}</h3>
                    <p className="trip-details">
                      {duration ? `${duration} days` : 'Duration not specified'}
                      {budget && ` • Budget: ৳${budget.toLocaleString()}`}
                    </p>
                    <p className="trip-dates">
                      Accepted: {new Date(trip.createdAt).toLocaleDateString()}
                      {startDate && ` • Start: ${startDate}`}
                    </p>
                    {origin && (
                      <p className="trip-transport">
                        From: {origin} → To: {destination}
                      </p>
                    )}
                </div>
                <div className="trip-actions">
                  <button 
                    className="btn-outline"
                    onClick={() => {
                      // Navigate to trip details or show modal
                      console.log('View trip details:', trip);
                      alert('Trip details view coming soon!');
                    }}
                  >
                    View Details
                  </button>
                  <button 
                    className="btn-danger"
                    onClick={async () => {
                      if (window.confirm('Are you sure you want to delete this trip?')) {
                        try {
                          await tripApi.deleteAcceptedTrip(trip.id);
                          setTrips(trips.filter(t => t.id !== trip.id));
                        } catch (error) {
                          console.error('Error deleting trip:', error);
                          alert('Failed to delete trip. Please try again.');
                        }
                      }
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
              );
            })}
          </div>
        ) : (
          <div className="empty-state">
            <p>You haven't accepted any trips yet.</p>
            <button onClick={() => navigate('/travel-planner')} className="btn-primary">
              Plan Your First Trip
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyTrips;