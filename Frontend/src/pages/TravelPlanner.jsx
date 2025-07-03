import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tripApi } from '../api';
import '../styles/travel-planner.css';

const TravelPlanner = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        destination: '',
        start_date: '',
        duration_days: 3,
        budget: '',
        origin: 'Dhaka'
    });
    const [isLoading, setIsLoading] = useState(false);
    const [planData, setPlanData] = useState(null);
    const [error, setError] = useState('');
    const [isAcceptingTrip, setIsAcceptingTrip] = useState(false);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');        try {
            const response = await fetch('http://localhost:8080/api/travel/plan', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify(formData)
            });

            const data = await response.json();

            if (data.success) {
                setPlanData(data);
            } else {
                setError(data.error || 'Failed to plan trip');
            }
        } catch (err) {
            setError('Network error. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-BD', {
            style: 'currency',
            currency: 'BDT'
        }).format(amount);
    };    const getWeatherIcon = (condition) => {
        const iconMap = {
            'clear': '☀️',
            'sunny': '☀️',
            'mainly clear': '🌤️',
            'partly cloudy': '⛅',
            'cloudy': '☁️',
            'overcast': '☁️',
            'fog': '🌫️',
            'drizzle': '🌦️',
            'rain': '🌧️',
            'heavy rain': '⛈️',
            'storm': '⛈️',
            'thunderstorm': '⛈️',
            'snow': '❄️',
            'sleet': '🌨️'
        };
        
        const lowerCondition = condition?.toLowerCase() || '';
        for (const [key, icon] of Object.entries(iconMap)) {
            if (lowerCondition.includes(key)) return icon;
        }
        return '🌤️';
    };

    // Accept trip plan and save to database
    const handleAcceptTrip = async () => {
        console.log('🎯 Accept Trip button clicked!');
        console.log('📊 Plan Data:', planData);
        
        if (!planData) {
            console.log('❌ No plan data available');
            return;
        }
        
        setIsAcceptingTrip(true);
        try {
            console.log('📡 Making API call to accept trip...');
            const response = await tripApi.acceptTrip(planData);
            console.log('✅ API Response:', response);
            
            if (response.success) {
                alert('🎉 Trip accepted successfully! You can view it in your trips section.');
                // Optionally redirect to trips page
                // navigate('/my-trips');
            } else {
                alert('❌ Failed to accept trip: ' + (response.error || 'Unknown error'));
            }
        } catch (error) {
            console.error('❌ Error accepting trip:', error);
            alert('❌ Failed to accept trip. Please try again.');
        } finally {
            setIsAcceptingTrip(false);
        }
    };

    // Export trip plan as JSON file
    const handleExport = () => {
        if (planData) {
            const exportData = planData.trip_plan || planData;
            const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(exportData, null, 2));
            const downloadAnchorNode = document.createElement('a');
            downloadAnchorNode.setAttribute("href", dataStr);
            downloadAnchorNode.setAttribute("download", `trip_plan_${planData.destination || 'destination'}.json`);
            document.body.appendChild(downloadAnchorNode);
            downloadAnchorNode.click();
            downloadAnchorNode.remove();
        }
    };

    return (
        <div className="travel-planner">
            <div className="planner-container">
                <div className="planner-header">
                    <h1>✈️ Plan Your Perfect Trip</h1>
                    <p>Discover amazing destinations with AI-powered recommendations</p>
                </div>

                <form className="planner-form" onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <div className="form-group">
                            <label>🏙️ Destination</label>
                            <input
                                type="text"
                                name="destination"
                                value={formData.destination}
                                onChange={handleInputChange}
                                placeholder="e.g., Sylhet, Cox's Bazar"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>📍 Origin</label>
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
                            </select>
                        </div>

                        <div className="form-group">
                            <label>📅 Start Date</label>
                            <input
                                type="date"
                                name="start_date"
                                value={formData.start_date}
                                onChange={handleInputChange}
                                min={new Date().toISOString().split('T')[0]}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>⏰ Duration (Days)</label>
                            <select
                                name="duration_days"
                                value={formData.duration_days}
                                onChange={handleInputChange}
                            >
                                {[1,2,3,4,5,6,7,10,14].map(day => (
                                    <option key={day} value={day}>{day} Day{day > 1 ? 's' : ''}</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>💰 Budget (BDT)</label>
                            <input
                                type="number"
                                name="budget"
                                value={formData.budget}
                                onChange={handleInputChange}
                                placeholder="e.g., 50000"
                                min="1000"
                            />
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
                            '🚀 Plan My Trip'
                        )}
                    </button>
                </form>

                {error && (
                    <div className="error-message">
                        ❌ {error}
                    </div>
                )}

                {planData && (
                    <div className="trip-results">
                        <div className="results-header">
                            <h2>🎉 Your Trip to {planData.destination}</h2>
                            <div className="trip-summary">
                                <span>📅 {planData.start_date}</span>
                                <span>⏰ {planData.duration_days} days</span>
                                {planData.budget && <span>💰 {formatCurrency(planData.budget)}</span>}
                            </div>
                        </div>

                        <div className="results-grid">                            {/* Weather Section */}
                            {planData.weather && !planData.weather.error && (
                                <div className="result-card weather-card">
                                    <h3>🌤️ Weather Forecast</h3>
                                    <div className="weather-days-container">
                                        {planData.weather.forecast?.slice(0, 3).map((day, index) => (
                                            <div key={index} className="weather-day-card">
                                                <div className="weather-day-header">
                                                    <h4>{day.date}</h4>
                                                    {day.sunrise && day.sunset && (
                                                        <div className="weather-sun-times">
                                                            <span>🌅 {day.sunrise}</span>
                                                            <span>🌇 {day.sunset}</span>
                                                        </div>
                                                    )}
                                                </div>
                                                
                                                <div className="weather-periods">
                                                    {/* Morning */}
                                                    {day.morning && (
                                                        <div className="weather-period">
                                                            <div className="period-header">
                                                                <span className="period-icon">🌅</span>
                                                                <span className="period-name">Morning</span>
                                                            </div>
                                                            <div className="period-details">
                                                                <div className="weather-icon">
                                                                    {getWeatherIcon(day.morning.conditions)}
                                                                </div>
                                                                <div className="weather-info">
                                                                    <div className="weather-temp">{day.morning.temperature}°C</div>
                                                                    <div className="weather-condition">{day.morning.conditions}</div>
                                                                    {day.morning.precipitation_chance > 0 && (
                                                                        <div className="weather-rain">🌧️ {day.morning.precipitation_chance}%</div>
                                                                    )}
                                                                    <div className="weather-details">
                                                                        <span>💨 {day.morning.wind_speed} km/h</span>
                                                                        <span>💧 {day.morning.humidity}%</span>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    )}
                                                    
                                                    {/* Afternoon */}
                                                    {day.afternoon && (
                                                        <div className="weather-period">
                                                            <div className="period-header">
                                                                <span className="period-icon">☀️</span>
                                                                <span className="period-name">Afternoon</span>
                                                            </div>
                                                            <div className="period-details">
                                                                <div className="weather-icon">
                                                                    {getWeatherIcon(day.afternoon.conditions)}
                                                                </div>
                                                                <div className="weather-info">
                                                                    <div className="weather-temp">{day.afternoon.temperature}°C</div>
                                                                    <div className="weather-condition">{day.afternoon.conditions}</div>
                                                                    {day.afternoon.precipitation_chance > 0 && (
                                                                        <div className="weather-rain">🌧️ {day.afternoon.precipitation_chance}%</div>
                                                                    )}
                                                                    <div className="weather-details">
                                                                        <span>💨 {day.afternoon.wind_speed} km/h</span>
                                                                        <span>💧 {day.afternoon.humidity}%</span>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    )}
                                                    
                                                    {/* Night */}
                                                    {day.night && (
                                                        <div className="weather-period">
                                                            <div className="period-header">
                                                                <span className="period-icon">🌙</span>
                                                                <span className="period-name">Night</span>
                                                            </div>
                                                            <div className="period-details">
                                                                <div className="weather-icon">
                                                                    {getWeatherIcon(day.night.conditions)}
                                                                </div>
                                                                <div className="weather-info">
                                                                    <div className="weather-temp">{day.night.temperature}°C</div>
                                                                    <div className="weather-condition">{day.night.conditions}</div>
                                                                    {day.night.precipitation_chance > 0 && (
                                                                        <div className="weather-rain">🌧️ {day.night.precipitation_chance}%</div>
                                                                    )}
                                                                    <div className="weather-details">
                                                                        <span>💨 {day.night.wind_speed} km/h</span>
                                                                        <span>💧 {day.night.humidity}%</span>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Hotels Section */}
                            {planData.hotels && Array.isArray(planData.hotels) && planData.hotels.length > 0 && (
                                <div className="result-card hotels-card">
                                    <h3>🏨 Recommended Hotels</h3>
                                    <div className="hotels-grid">
                                        {planData.hotels.slice(0, 6).map((hotel, index) => (
                                            <div key={index} className="hotel-item">
                                                <div className="hotel-header">
                                                    <h4>{hotel.name}</h4>
                                                    <div className="hotel-rating">⭐ 4.2</div>
                                                </div>
                                                <div className="hotel-address">📍 {hotel.address || 'Address not available'}</div>
                                                <div className="hotel-distance">
                                                    📏 {hotel.distance ? `${hotel.distance}km away` : 'Distance unknown'}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Restaurants Section */}
                            {planData.restaurants && Array.isArray(planData.restaurants) && planData.restaurants.length > 0 && (
                                <div className="result-card restaurants-card">
                                    <h3>🍽️ Local Restaurants</h3>
                                    <div className="restaurants-grid">
                                        {planData.restaurants.slice(0, 8).map((restaurant, index) => (
                                            <div key={index} className="restaurant-item">
                                                <div className="restaurant-header">
                                                    <h4>{restaurant.name}</h4>
                                                    <div className="restaurant-cuisine">🍴 Local Cuisine</div>
                                                </div>
                                                <div className="restaurant-address">
                                                    📍 {restaurant.address || 'Address not available'}
                                                </div>
                                                <div className="restaurant-actions">
                                                    <button className="view-map-btn">📍 View on Map</button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Tourist Spots Section */}
                            {planData.spots && Array.isArray(planData.spots) && planData.spots.length > 0 && (
                                <div className="result-card spots-card">
                                    <h3>📍 Tourist Attractions</h3>
                                    <div className="spots-grid">
                                        {planData.spots.slice(0, 6).map((spot, index) => (
                                            <div key={index} className="spot-item">
                                                <div className="spot-header">
                                                    <h4>{spot.name}</h4>
                                                    <div className="spot-type">🎯 {spot.type || 'Attraction'}</div>
                                                </div>
                                                <div className="spot-address">
                                                    📍 {spot.address || 'Address not available'}
                                                </div>
                                                <div className="spot-description">
                                                    {spot.description || 'A must-visit attraction in the area'}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}                            {/* Enhanced LLM Trip Plan Section */}
                            {planData.trip_plan && (
                                <div className="result-card enhanced-trip-plan">
                                    <div className="trip-plan-header">
                                        <h3>✨ Your Personalized Travel Itinerary</h3>
                                        <p>AI-crafted day-by-day plan with multiple options</p>
                                    </div>

                                    {/* Trip Summary */}
                                    {planData.trip_plan.trip_summary && (
                                        <div className="trip-summary-card">
                                            <div className="summary-grid">
                                                <div className="summary-item">
                                                    <span className="summary-icon">🚗</span>
                                                    <div>
                                                        <div className="summary-label">Route</div>
                                                        <div className="summary-value">{planData.trip_plan.trip_summary.origin} → {planData.trip_plan.trip_summary.destination}</div>
                                                    </div>
                                                </div>
                                                <div className="summary-item">
                                                    <span className="summary-icon">📅</span>
                                                    <div>
                                                        <div className="summary-label">Duration</div>
                                                        <div className="summary-value">{planData.trip_plan.trip_summary.duration} Days</div>
                                                    </div>
                                                </div>
                                                <div className="summary-item">
                                                    <span className="summary-icon">💰</span>
                                                    <div>
                                                        <div className="summary-label">Budget</div>
                                                        <div className="summary-value">৳{planData.trip_plan.trip_summary.total_budget?.toLocaleString()}</div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    )}                                    {/* Daily Itinerary */}
                                    {planData.trip_plan.daily_itinerary && Array.isArray(planData.trip_plan.daily_itinerary) && (
                                        <div className="daily-itinerary-container">
                                            <h4 className="section-title">📅 Day-by-Day Itinerary</h4>
                                            
                                            {planData.trip_plan.daily_itinerary.map((day, dayIdx) => (
                                                <div key={dayIdx} className="enhanced-day-card">
                                                    <div className="day-header-enhanced">
                                                        <div className="day-number-badge">
                                                            {day.day}
                                                        </div>
                                                        <div className="day-info">
                                                            <h5>Day {day.day}</h5>
                                                            <span className="day-date">{day.date}</span>
                                                            {day.weather && (
                                                                <span className="day-weather">
                                                                    {getWeatherIcon(day.weather)} {day.weather}
                                                                </span>
                                                            )}
                                                        </div>
                                                    </div>

                                                    <div className="day-activities-grid">
                                                        {/* Morning Activity */}
                                                        {day.morning_activity && (
                                                            <div className="activity-card morning">
                                                                <div className="activity-time">
                                                                    <span className="time-icon">🌅</span>
                                                                    <span>{day.morning_activity.time || "Morning"}</span>
                                                                </div>
                                                                <div className="activity-content">
                                                                    <h6>{day.morning_activity.spot_name}</h6>
                                                                    <p>{day.morning_activity.description}</p>
                                                                    {day.morning_activity.entry_fee && (
                                                                        <div className="entry-fee">💰 Entry: ৳{day.morning_activity.entry_fee}</div>
                                                                    )}
                                                                    {day.morning_activity.image_url && (
                                                                        <img src={day.morning_activity.image_url} alt={day.morning_activity.spot_name} className="activity-image" />
                                                                    )}
                                                                </div>
                                                            </div>
                                                        )}

                                                        {/* Lunch Options */}
                                                        {day.lunch_options && Array.isArray(day.lunch_options) && (
                                                            <div className="meal-options-card lunch">
                                                                <div className="meal-header">
                                                                    <span className="meal-icon">🍽️</span>
                                                                    <h6>Lunch Options</h6>
                                                                </div>
                                                                <div className="options-grid">
                                                                    {day.lunch_options.map((restaurant, idx) => (
                                                                        <div key={idx} className="restaurant-option">
                                                                            <div className="restaurant-name">{restaurant.restaurant_name}</div>
                                                                            <div className="restaurant-details">
                                                                                <span className="cuisine">🍴 {restaurant.cuisine}</span>
                                                                                <span className="cost">💰 ৳{restaurant.cost_per_person}/person</span>
                                                                                {restaurant.rating && (
                                                                                    <span className="rating">⭐ {restaurant.rating}</span>
                                                                                )}
                                                                            </div>
                                                                        </div>
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        )}

                                                        {/* Afternoon Activities */}
                                                        {day.afternoon_activities && Array.isArray(day.afternoon_activities) && (
                                                            <div className="activity-card afternoon">
                                                                <div className="activity-time">
                                                                    <span className="time-icon">☀️</span>
                                                                    <span>Afternoon</span>
                                                                </div>
                                                                <div className="afternoon-spots">
                                                                    {day.afternoon_activities.map((activity, idx) => (
                                                                        <div key={idx} className="afternoon-spot">
                                                                            <h6>{activity.spot_name}</h6>
                                                                            <p>{activity.description}</p>
                                                                            <div className="spot-details">
                                                                                {activity.time && <span>⏰ {activity.time}</span>}
                                                                                {activity.entry_fee && <span>💰 ৳{activity.entry_fee}</span>}
                                                                            </div>
                                                                        </div>
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        )}

                                                        {/* Dinner Options */}
                                                        {day.dinner_options && Array.isArray(day.dinner_options) && (
                                                            <div className="meal-options-card dinner">
                                                                <div className="meal-header">
                                                                    <span className="meal-icon">🌃</span>
                                                                    <h6>Dinner Options</h6>
                                                                </div>
                                                                <div className="options-grid">
                                                                    {day.dinner_options.map((restaurant, idx) => (
                                                                        <div key={idx} className="restaurant-option">
                                                                            <div className="restaurant-name">{restaurant.restaurant_name}</div>
                                                                            <div className="restaurant-details">
                                                                                <span className="cuisine">🍴 {restaurant.cuisine}</span>
                                                                                <span className="cost">💰 ৳{restaurant.cost_per_person}/person</span>
                                                                                {restaurant.rating && (
                                                                                    <span className="rating">⭐ {restaurant.rating}</span>
                                                                                )}
                                                                            </div>
                                                                        </div>
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        )}

                                                        {/* Accommodation Options */}
                                                        {day.accommodation_options && Array.isArray(day.accommodation_options) && (
                                                            <div className="accommodation-card">
                                                                <div className="accommodation-header">
                                                                    <span className="accommodation-icon">🏨</span>
                                                                    <h6>Stay Options</h6>
                                                                </div>
                                                                <div className="hotel-options-grid">
                                                                    {day.accommodation_options.map((hotel, idx) => (
                                                                        <div key={idx} className="hotel-option">
                                                                            <div className="hotel-header">
                                                                                <div className="hotel-name">{hotel.hotel_name}</div>
                                                                                {hotel.rating && (
                                                                                    <div className="hotel-rating">⭐ {hotel.rating}</div>
                                                                                )}
                                                                            </div>
                                                                            <div className="hotel-details">
                                                                                <div className="hotel-price">💰 ৳{hotel.cost_per_night}/night</div>
                                                                                {hotel.amenities && (
                                                                                    <div className="hotel-amenities">🛎️ {hotel.amenities}</div>
                                                                                )}
                                                                            </div>
                                                                        </div>
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        )}
                                                    </div>

                                                    {/* Day Budget */}
                                                    {day.day_budget && (
                                                        <div className="day-budget-card">
                                                            <h6>💰 Day {day.day} Budget Breakdown</h6>
                                                            <div className="budget-items-grid">
                                                                {Object.entries(day.day_budget).map(([category, amount], idx) => (
                                                                    category !== 'total' && (
                                                                        <div key={idx} className="budget-item-small">
                                                                            <span className="budget-category">{category.charAt(0).toUpperCase() + category.slice(1)}</span>
                                                                            <span className="budget-amount">৳{amount}</span>
                                                                        </div>
                                                                    )
                                                                ))}
                                                                {day.day_budget.total && (
                                                                    <div className="budget-total-small">
                                                                        <span>Total</span>
                                                                        <span>৳{day.day_budget.total}</span>
                                                                    </div>
                                                                )}
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            ))}
                                        </div>
                                    )}                                    {/* Enhanced Budget Summary */}
                                    {planData.trip_plan.budget_summary && (
                                        <div className="enhanced-budget-summary">
                                            <h4 className="section-title">💰 Complete Budget Breakdown</h4>
                                            <div className="budget-summary-grid">
                                                {Object.entries(planData.trip_plan.budget_summary).map(([category, amount], idx) => (
                                                    <div key={idx} className="budget-summary-item">
                                                        <div className="budget-icon">
                                                            {category.includes('accommodation') ? '🏨' :
                                                             category.includes('meals') ? '🍽️' :
                                                             category.includes('activities') ? '🎯' :
                                                             category.includes('transport') ? '🚗' : '💵'}
                                                        </div>
                                                        <div className="budget-details">
                                                            <div className="budget-category-name">
                                                                {category.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                                                            </div>
                                                            <div className="budget-amount-large">
                                                                ৳{typeof amount === 'number' ? amount.toLocaleString() : amount}
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>                                            
                                            {planData.trip_plan.budget_summary.grand_total && (
                                                <div className="total-budget-card">
                                                    <div className="total-budget-content">
                                                        <span className="total-label">Total Trip Cost</span>
                                                        <span className="total-amount">৳{planData.trip_plan.budget_summary.grand_total.toLocaleString()}</span>
                                                    </div>
                                                    {planData.trip_plan.budget_summary.remaining && (
                                                        <div className="remaining-budget">
                                                            <span>Budget Remaining: ৳{planData.trip_plan.budget_summary.remaining.toLocaleString()}</span>
                                                        </div>
                                                    )}
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            )}

                            {/* Groq LLM Trip Plan Section */}
                            {planData.groq_plan && (
                                <div className="result-card llm-plan-card">
                                    <h3>🤖 Groq AI-Generated Day-wise Trip Plan</h3>
                                    {planData.groq_plan.summary && (
                                        <div className="llm-summary">
                                            <strong>Summary:</strong> {planData.groq_plan.summary}
                                        </div>
                                    )}
                                    {planData.groq_plan.days && Array.isArray(planData.groq_plan.days) && planData.groq_plan.days.length > 0 && (
                                        <div className="llm-days">
                                            {planData.groq_plan.days.map((day, idx) => (
                                                <div key={idx} className="llm-day-item">
                                                    <h4>Day {idx + 1}{day.date ? ` - ${day.date}` : ''}</h4>
                                                    {day.activities && (
                                                        <div><strong>Activities:</strong> {Array.isArray(day.activities) ? day.activities.join(', ') : day.activities}</div>
                                                    )}
                                                    {day.meals && (
                                                        <div><strong>Meals:</strong> {JSON.stringify(day.meals)}</div>
                                                    )}
                                                    {day.hotel && (
                                                        <div><strong>Hotel:</strong> {typeof day.hotel === 'string' ? day.hotel : JSON.stringify(day.hotel)}</div>
                                                    )}
                                                    {day.notes && (
                                                        <div><strong>Notes:</strong> {day.notes}</div>
                                                    )}
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                    {planData.groq_plan.budget_breakdown && (
                                        <div className="llm-budget-breakdown">
                                            <h4>💸 Budget Breakdown</h4>
                                            <pre style={{background:'#f6f6f6', padding:'10px', borderRadius:'6px', overflowX:'auto'}}>{JSON.stringify(planData.groq_plan.budget_breakdown, null, 2)}</pre>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>                        {/* Action Buttons */}
                        <div className="action-buttons">
                            <button 
                                type="button"
                                className="accept-trip-btn"
                                onClick={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    handleAcceptTrip();
                                }}
                                disabled={isAcceptingTrip || !planData}
                                title={planData ? "Accept and save this trip plan" : "No plan available to accept"}
                            >
                                {isAcceptingTrip ? '⏳ Accepting...' : '✅ Accept This Trip'}
                            </button>                            <button 
                                className="export-btn"
                                onClick={handleExport}
                                disabled={!planData}
                                title={planData ? "Export your trip plan" : "No plan available to export"}
                            >
                                📄 Export {planData?.trip_plan ? 'AI Itinerary' : 'Trip Plan'}
                            </button>
                            {planData?.trip_plan && (
                                <button 
                                    className="share-btn"
                                    onClick={() => {
                                        if (navigator.share) {
                                            navigator.share({
                                                title: `Trip to ${planData.destination}`,
                                                text: `Check out this AI-generated trip plan for ${planData.destination}!`,
                                                url: window.location.href
                                            });
                                        } else {
                                            navigator.clipboard.writeText(window.location.href);
                                            alert('Trip link copied to clipboard!');
                                        }
                                    }}
                                >
                                    🔗 Share Trip
                                </button>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default TravelPlanner;
