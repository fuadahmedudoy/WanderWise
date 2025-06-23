// Frontend/src/pages/DestinationDetail.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api'; // <-- Import api
import { FaStar, FaMapMarkerAlt, FaRegClock } from 'react-icons/fa';
import '../styles/DestinationDetail.css';

const DestinationDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [destination, setDestination] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDestination = async () => {
      try {
        setIsLoading(true);
        setError(null);
        // Use the new api instance
        const response = await api.get(`/api/destinations/${id}`);
        setDestination(response.data);
        setIsLoading(false);
      } catch (err) {
        console.error('Error fetching destination details:', err);
        setError('Failed to load destination details. Please try again later.');
        setIsLoading(false);
      }
    };

    fetchDestination();
  }, [id]);
  
  // ... (rest of the component is the same)
  if (isLoading) {
    return <div className="loading-container">Loading...</div>;
  }

  if (error) {
    return <div className="error-container">{error}</div>;
  }

  if (!destination) {
    return <div className="not-found-container">Destination not found.</div>;
  }

  return (
    <div className="detail-container">
      <button onClick={() => navigate(-1)} className="back-button">
        &larr; Back to Destinations
      </button>
      <div className="detail-header" style={{ backgroundImage: `url(${destination.imageUrl})` }}>
        <div className="header-overlay">
          <h1>{destination.title}</h1>
        </div>
      </div>
      <div className="detail-content">
        <div className="meta-info">
          <span><FaMapMarkerAlt /> {destination.destination}</span>
          <span><FaRegClock /> {destination.days} days</span>
          <span><FaStar /> {destination.avgRating.toFixed(1)}</span>
        </div>
        <h2>About this destination</h2>
        <p>{destination.description}</p>
        
        <div className="reviews-section">
          <h3>Reviews</h3>
          {destination.reviews && destination.reviews.length > 0 ? (
            <ul>
              {destination.reviews.map((review, index) => (
                <li key={index}>{review}</li>
              ))}
            </ul>
          ) : (
            <p>No reviews yet.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default DestinationDetail;