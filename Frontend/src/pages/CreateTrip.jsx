import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import '../styles/trips.css';

const CreateTrip = () => {
  const { currentUser } = useContext(AuthContext);
  const navigate = useNavigate();
  const [showAuthDialog, setShowAuthDialog] = useState(!currentUser);
  
  // Form state
  const [tripName, setTripName] = useState('');
  const [destination, setDestination] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [description, setDescription] = useState('');
  
  const handleSubmit = (e) => {
    e.preventDefault();
    // Here you would save the trip data
    alert('Trip created successfully!');
    navigate('/my-trips');
  };
  
  const handleLogin = () => {
    navigate('/login');
  };

  if (showAuthDialog) {
    return (
      <div className="auth-dialog-overlay">
        <div className="auth-dialog">
          <h2>Authentication Required</h2>
          <p>You need to login first to create a trip.</p>
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
        <div className="logo">Trip Planner</div>
        <button onClick={() => navigate('/')} className="btn-outline">Back to Home</button>
      </header>
      
      <div className="content-container">
        <div className="form-card">
          <h1>Create a New Trip</h1>
          <p>Fill in the details to plan your perfect journey</p>
          
          <form onSubmit={handleSubmit} className="trip-form">
            <div className="form-group">
              <label htmlFor="tripName">Trip Name</label>
              <input
                type="text"
                id="tripName"
                value={tripName}
                onChange={(e) => setTripName(e.target.value)}
                required
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="destination">Destination</label>
              <input
                type="text"
                id="destination"
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
                required
              />
            </div>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="startDate">Start Date</label>
                <input
                  type="date"
                  id="startDate"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  required
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="endDate">End Date</label>
                <input
                  type="date"
                  id="endDate"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  required
                />
              </div>
            </div>
            
            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows="4"
              ></textarea>
            </div>
            
            <button type="submit" className="btn-primary">Create Trip</button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateTrip;