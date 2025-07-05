import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import api, { tripApi } from '../api';
import TripImage from '../components/TripImage';
import '../styles/create-trip.css';

const CreateTrip = () => {
  const { currentUser } = useContext(AuthContext);
  const navigate = useNavigate();
  const [showAuthDialog] = useState(!currentUser);
    // Form state
  const [formData, setFormData] = useState({
    destination: '',
    origin: 'Dhaka',
    startDate: '',
    durationDays: 3,
    budget: ''
  });
  
  // UI state
  const [isLoading, setIsLoading] = useState(false);
  const [tripPlan, setTripPlan] = useState(null);
  const [error, setError] = useState('');
  const [planningStage, setPlanningStage] = useState('form'); // 'form', 'result', 'customize'
  
  // Customize state
  const [customizePrompt, setCustomizePrompt] = useState('');
  const [customizing, setCustomizing] = useState(false);
  const [showCustomizeInput, setShowCustomizeInput] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
      
    try {
      // Frontend validation
      if (!formData.destination.trim()) {
        setError('Destination is required');
        setIsLoading(false);
        return;
      }
      
      if (!formData.startDate) {
        setError('Start date is required');
        setIsLoading(false);
        return;
      }
      
      if (!formData.budget || parseFloat(formData.budget) <= 0) {
        setError('Budget must be a positive number');
        setIsLoading(false);
        return;
      }      // Convert data types to match backend expectations
      const requestData = {
        destination: formData.destination,
        origin: formData.origin,
        startDate: formData.startDate,        // Keep camelCase for Spring Boot DTO
        durationDays: parseInt(formData.durationDays), // Keep camelCase for Spring Boot DTO
        budget: parseFloat(formData.budget)
      };
      
      // 🔍 DEBUG: Log what we're sending to backend
      console.log('🚀 FRONTEND SENDING TO API:');
      console.log('=====================================');
      console.log('📦 Request data:', requestData);
      console.log('📊 Data types:');      console.log('  - destination:', typeof requestData.destination, requestData.destination);
      console.log('  - startDate:', typeof requestData.startDate, requestData.startDate);
      console.log('  - durationDays:', typeof requestData.durationDays, requestData.durationDays);
      console.log('  - budget:', typeof requestData.budget, requestData.budget);
      console.log('  - origin:', typeof requestData.origin, requestData.origin);
      console.log('=====================================');

      const response = await api.post('/api/trip/plan', requestData);
      const data = response.data;
        // 🔍 DEBUG: Log the complete response from backend
      console.log('🎯 FRONTEND RECEIVED FROM API:');
      console.log('=====================================');
      console.log('📊 Response status:', response.status);
      console.log('📦 Complete response data:', data);
      
      if (data.trip_plan) {
        console.log('🗓️ Trip plan keys:', Object.keys(data.trip_plan));
        if (data.trip_plan.daily_itinerary) {
          console.log('📅 Days in itinerary:', data.trip_plan.daily_itinerary.length);
          console.log('🌅 Day 1 sample:', data.trip_plan.daily_itinerary[0]);
        }
      }
      console.log('=====================================');      if (data.success) {
        setTripPlan(data.trip_plan);
        setPlanningStage('result');
      } else {
        setError(data.error || 'Failed to plan trip');
      }
    } catch (err) {
      console.error('Error planning trip:', err);
      
      // More specific error handling
      if (err.response) {
        // Backend returned an error response
        const errorData = err.response.data;
        if (errorData && errorData.error) {
          setError(errorData.error);
        } else if (err.response.status === 400) {
          setError('Invalid request. Please check your input and try again.');
        } else if (err.response.status === 401) {
          setError('You need to be logged in to plan a trip.');
        } else if (err.response.status === 500) {
          setError('Server error. Please try again later.');
        } else {
          setError(`Request failed with status ${err.response.status}`);
        }
      } else if (err.request) {
        // Network error
        setError('Network error. Please check your connection and try again.');
      } else {
        // Other error
        setError('An unexpected error occurred. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleAccept = async () => {
    // Save trip functionality
    try {
      const response = await tripApi.acceptTrip(tripPlan);

      if (response.success) {
        navigate('/my-trips');
      } else {
        setError(response.error || 'Failed to save trip');
      }
    } catch (err) {
      console.error('Error accepting trip:', err);
      if (err.response?.data?.error) {
        setError(err.response.data.error);
      } else {
        setError('Error saving trip');
      }
    }
  };

  const handleCustomize = async () => {
    if (!customizePrompt.trim()) {
      setError('Please enter your customization request');
      return;
    }

    setCustomizing(true);
    setError('');

    try {
      console.log('🔄 Customizing trip with prompt:', customizePrompt);
      console.log('🔄 Original plan:', tripPlan);

      const customizedResponse = await tripApi.customizeTrip(tripPlan, customizePrompt);
      
      console.log('✅ Customized trip response:', customizedResponse);

      if (customizedResponse.success) {
        // Update trip plan with customized version
        setTripPlan(customizedResponse.trip_plan);
        setCustomizePrompt(''); // Clear the prompt
        setShowCustomizeInput(false); // Hide the input
        console.log('🎉 Trip customized successfully!');
      } else {
        setError(customizedResponse.error || 'Failed to customize trip');
        console.error('❌ Customization failed:', customizedResponse.error);
      }
    } catch (error) {
      console.error('❌ Error customizing trip:', error);
      setError(error.response?.data?.error || 'Failed to customize trip. Please try again.');
    } finally {
      setCustomizing(false);
    }
  };

  const handleShowCustomizeInput = () => {
    setShowCustomizeInput(true);
    setError(''); // Clear any existing errors
  };

  const handleCancelCustomize = () => {
    setShowCustomizeInput(false);
    setCustomizePrompt('');
    setError('');
  };

  const handleRegenerate = () => {
    setTripPlan(null);
    setPlanningStage('form');
    handleSubmit({ preventDefault: () => {} });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-BD', {
      style: 'currency',
      currency: 'BDT'
    }).format(amount);
  };  const handleLogin = () => {
    navigate('/auth/login');
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
    <div className="create-trip-container">
      {/* Header */}
      <header className="create-trip-header">
        <div className="header-content">
          <button onClick={() => navigate('/')} className="back-button">
            ← Back to Home
          </button>
          <div className="header-title">
            <h1>Plan Your Perfect Trip</h1>
            <p>Discover amazing destinations with intelligent planning</p>
          </div>
        </div>
      </header>

      <div className="create-trip-content">
        {/* Planning Form */}
        {planningStage === 'form' && (
          <div className="planning-form-section">
            <div className="form-card">
              <div className="form-header">
                <h2>Trip Details</h2>
                <p>Tell us about your dream destination and preferences</p>
              </div>

              <form className="trip-form" onSubmit={handleSubmit}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Destination</label>
                    <input
                      type="text"
                      name="destination"
                      value={formData.destination}
                      onChange={handleInputChange}
                      placeholder="e.g., Sylhet, Cox's Bazar, Bandarban"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Origin City</label>
                    <select
                      name="origin"
                      value={formData.origin}
                      onChange={handleInputChange}
                    >
                      <option value="Dhaka">Dhaka</option>
                      <option value="Chittagong">Chittagong</option>
                      <option value="Sylhet">Sylhet</option>
                      <option value="Rajshahi">Rajshahi</option>
                      <option value="Khulna">Khulna</option>
                      <option value="Barisal">Barisal</option>
                      <option value="Rangpur">Rangpur</option>
                      <option value="Mymensingh">Mymensingh</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Start Date</label>
                    <input
                      type="date"                      name="startDate"
                      value={formData.startDate}
                      onChange={handleInputChange}
                      min={new Date().toISOString().split('T')[0]}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Duration</label>
                    <select                      name="durationDays"
                      value={formData.durationDays}
                      onChange={handleInputChange}
                    >
                      {[1,2,3,4,5,6,7,10,14].map(day => (
                        <option key={day} value={day}>
                          {day} Day{day > 1 ? 's' : ''}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group full-width">
                    <label>Budget (BDT)</label>
                    <input
                      type="number"
                      name="budget"
                      value={formData.budget}
                      onChange={handleInputChange}
                      placeholder="e.g., 50000"
                      min="1000"
                      required
                    />
                    <small>Enter your total budget for the entire trip</small>
                  </div>
                </div>

                <button 
                  type="submit" 
                  className="plan-button"
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <>
                      <div className="spinner"></div>
                      Planning Your Trip...
                    </>
                  ) : (
                    'Create Trip Plan'
                  )}
                </button>
              </form>

              {error && (
                <div className="error-message">
                  {error}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Trip Plan Results */}
        {planningStage === 'result' && tripPlan && (
          <div className="trip-results-section">
            <div className="results-header">
              <h2>Your Trip to {formData.destination}</h2>
              <div className="trip-summary">
                <span>{formData.startDate}</span>
                <span>{formData.durationDays} days</span>
                <span>{formatCurrency(formData.budget)}</span>
              </div>
            </div>

            <div className="trip-plan-display">
              {/* Daily Itinerary */}
              {tripPlan.daily_itinerary && (
                <div className="daily-itinerary">
                  <h3>Daily Itinerary</h3>
                  <div className="itinerary-timeline">
                    {tripPlan.daily_itinerary.map((day, index) => (
                      <div key={index} className="day-card">
                        <div className="day-header">
                          <div className="day-number">{index + 1}</div>
                          <div className="day-info">
                            <h4>Day {index + 1}</h4>
                            {day.date && <span className="day-date">{day.date}</span>}
                            {day.weather && <span className="day-weather">{day.weather}</span>}
                          </div>
                        </div>                        <div className="day-activities">
                          {/* Morning Activity */}
                          {day.morning_activity && (
                            <div className="activity-block morning">
                              <div className="activity-time">Morning ({day.morning_activity.time})</div>
                              <div className="activity-content">
                                <h5>{day.morning_activity.spot_name}</h5>
                                <p>{day.morning_activity.description}</p>
                                {day.morning_activity.entry_fee > 0 && (
                                  <span className="fee">Entry: ৳{day.morning_activity.entry_fee}</span>
                                )}
                                <TripImage 
                                  src={day.morning_activity.image_url} 
                                  alt={day.morning_activity.spot_name} 
                                  className="activity-image"
                                  fallbackType="spot"
                                />
                              </div>
                            </div>
                          )}

                          {/* Lunch Options */}
                          {day.lunch_options && day.lunch_options.length > 0 && (
                            <div className="activity-block lunch">
                              <div className="activity-time">Lunch</div>
                              <div className="activity-content">
                                {day.lunch_options.map((restaurant, idx) => (
                                  <div key={idx} className="restaurant-option">
                                    <h5>{restaurant.restaurant_name}</h5>
                                    <p>{restaurant.cuisine}</p>
                                    <span className="cost">Cost: ৳{restaurant.cost_per_person}/person</span>
                                    <span className="rating">Rating: {restaurant.rating}/5</span>
                                    <TripImage 
                                      src={restaurant.image_url} 
                                      alt={restaurant.restaurant_name} 
                                      className="activity-image"
                                      fallbackType="restaurant"
                                    />
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}

                          {/* Afternoon Activities */}
                          {day.afternoon_activities && day.afternoon_activities.length > 0 && (
                            <div className="activity-block afternoon">
                              <div className="activity-time">Afternoon</div>
                              <div className="activity-content">
                                {day.afternoon_activities.map((activity, idx) => (
                                  <div key={idx} className="afternoon-activity">
                                    <h5>{activity.spot_name}</h5>
                                    <p>{activity.description}</p>
                                    <span className="activity-time">Time: {activity.time}</span>
                                    {activity.entry_fee > 0 && (
                                      <span className="fee">Entry: ৳{activity.entry_fee}</span>
                                    )}
                                    <TripImage 
                                      src={activity.image_url} 
                                      alt={activity.spot_name} 
                                      className="activity-image"
                                      fallbackType="spot"
                                    />
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}

                          {/* Dinner Options */}
                          {day.dinner_options && day.dinner_options.length > 0 && (
                            <div className="activity-block dinner">
                              <div className="activity-time">Dinner</div>
                              <div className="activity-content">
                                {day.dinner_options.map((restaurant, idx) => (
                                  <div key={idx} className="restaurant-option">
                                    <h5>{restaurant.restaurant_name}</h5>
                                    <p>{restaurant.cuisine}</p>
                                    <span className="cost">Cost: ৳{restaurant.cost_per_person}/person</span>
                                    <span className="rating">Rating: {restaurant.rating}/5</span>
                                    <TripImage 
                                      src={restaurant.image_url} 
                                      alt={restaurant.restaurant_name} 
                                      className="activity-image"
                                      fallbackType="restaurant"
                                    />
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}

                          {/* Accommodation Options */}
                          {day.accommodation_options && day.accommodation_options.length > 0 && (
                            <div className="activity-block accommodation">
                              <div className="activity-time">Stay</div>
                              <div className="activity-content">
                                {day.accommodation_options.map((hotel, idx) => (
                                  <div key={idx} className="hotel-option">
                                    <h5>{hotel.hotel_name}</h5>
                                    <div className="hotel-details">
                                      <span className="rating">Rating: {hotel.rating}/5</span>
                                      <span className="cost">Cost: ৳{hotel.cost_per_night}/night</span>
                                      <span className="amenities">Amenities: {hotel.amenities}</span>
                                    </div>
                                    <TripImage 
                                      src={hotel.image_url} 
                                      alt={hotel.hotel_name} 
                                      className="activity-image"
                                      fallbackType="hotel"
                                    />
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}

                          {/* Daily Budget */}
                          {day.day_budget && (
                            <div className="day-budget">
                              <h6>Daily Budget: ৳{day.day_budget.total}</h6>
                              <div className="budget-details">
                                <span>Accommodation: ৳{day.day_budget.accommodation}</span>
                                <span>Meals: ৳{day.day_budget.meals}</span>
                                <span>Activities: ৳{day.day_budget.activities}</span>
                                <span>Transport: ৳{day.day_budget.transport}</span>
                                <span>Misc: ৳{day.day_budget.misc}</span>
                              </div>
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}              {/* Budget Summary */}
              {tripPlan.budget_summary && (
                <div className="budget-summary">
                  <h3>Budget Summary</h3>
                  <div className="budget-grid">
                    <div className="budget-item">
                      <span>Accommodation</span>
                      <span>৳{tripPlan.budget_summary.total_accommodation}</span>
                    </div>
                    <div className="budget-item">
                      <span>Meals</span>
                      <span>৳{tripPlan.budget_summary.total_meals}</span>
                    </div>
                    <div className="budget-item">
                      <span>Activities</span>
                      <span>৳{tripPlan.budget_summary.total_activities}</span>
                    </div>
                    <div className="budget-item">
                      <span>Transport</span>
                      <span>৳{tripPlan.budget_summary.total_transport}</span>
                    </div>
                    <div className="budget-item">
                      <span>Miscellaneous</span>
                      <span>৳{tripPlan.budget_summary.total_misc}</span>
                    </div>
                    <div className="budget-item total">
                      <span>Total Cost</span>
                      <span>৳{tripPlan.budget_summary.grand_total}</span>
                    </div>
                    <div className="budget-item remaining">
                      <span>Remaining Budget</span>
                      <span>৳{tripPlan.budget_summary.remaining}</span>                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="trip-actions">
              <button 
                className="action-btn accept"
                onClick={handleAccept}
              >
                Accept Plan
              </button>
              <button 
                className="action-btn customize"
                onClick={handleShowCustomizeInput}
              >
                Customize
              </button>
              <button 
                className="action-btn regenerate"
                onClick={handleRegenerate}
              >
                Regenerate
              </button>
            </div>

            {/* Inline Customize Input */}
            {showCustomizeInput && (
              <div className="inline-customize">
                <div className="customize-header">
                  <h3>✨ Customize Your Trip</h3>
                  <p>Tell us what you'd like to change about your current plan</p>
                </div>
                
                <div className="customize-input-section">
                  <textarea
                    value={customizePrompt}
                    onChange={(e) => setCustomizePrompt(e.target.value)}
                    placeholder="e.g., 'I want to visit Ratargul Swamp Forest first' or 'Change my budget to ৳20,000' or 'Add more cultural activities'"
                    rows="3"
                    className="customize-input"
                    disabled={customizing}
                  />
                  
                  <div className="customize-buttons">
                    <button 
                      className="btn-cancel"
                      onClick={handleCancelCustomize}
                      disabled={customizing}
                    >
                      Cancel
                    </button>
                    <button 
                      className="btn-customize"
                      onClick={handleCustomize}
                      disabled={!customizePrompt.trim() || customizing}
                    >
                      {customizing ? '⏳ Customizing...' : '🚀 Apply Changes'}
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default CreateTrip;