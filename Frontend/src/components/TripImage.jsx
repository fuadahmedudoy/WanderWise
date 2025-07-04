import React, { useState } from 'react';

const TripImage = ({ src, alt, className, fallbackType = 'spot', showTextOnError = true }) => {
  const [imageError, setImageError] = useState(false);
  const [loading, setLoading] = useState(true);

  const handleImageError = () => {
    setImageError(true);
    setLoading(false);
  };

  const handleImageLoad = () => {
    setLoading(false);
  };

  // If no src provided or error occurred, show text instead
  if (!src || imageError) {
    return (
      <div className={`trip-image-text-fallback ${className || ''}`}>
        <div className="text-fallback-content">
          <span className="fallback-icon">
            {fallbackType === 'spot' && '🏛️'}
            {fallbackType === 'hotel' && '🏨'}
            {fallbackType === 'restaurant' && '🍽️'}
          </span>
          <span className="fallback-text">{alt}</span>
        </div>
      </div>
    );
  }

  return (
    <div className={`trip-image-container ${className || ''}`}>
      {loading && (
        <div className="image-loading">
          <div className="loading-placeholder">
            📷 Loading...
          </div>
        </div>
      )}
      
      <img
        src={src}
        alt={alt}
        className={`trip-image ${loading ? 'loading' : ''}`}
        onError={handleImageError}
        onLoad={handleImageLoad}
        style={{ display: loading ? 'none' : 'block' }}
      />
    </div>
  );
};

export default TripImage;
