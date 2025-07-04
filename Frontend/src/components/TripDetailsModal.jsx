import React from 'react';
import TripImage from './TripImage';

const TripDetailsModal = ({ trip, isOpen, onClose }) => {
  if (!isOpen || !trip) return null;

  const tripPlan = trip.tripPlan || {};
  const tripSummary = tripPlan.trip_summary || {};
  const dailyItinerary = tripPlan.daily_itinerary || [];
  const budgetSummary = tripPlan.budget_summary || {};

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-BD', {
      style: 'currency',
      currency: 'BDT',
      minimumFractionDigits: 0
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="trip-details-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>🗺️ Trip Details</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-content">
          {/* Trip Summary */}
          <div className="trip-summary-section">
            <h3>📋 Trip Summary</h3>
            <div className="summary-grid">
              <div className="summary-item">
                <strong>Destination:</strong> {tripSummary.destination || 'N/A'}
              </div>
              <div className="summary-item">
                <strong>Origin:</strong> {tripSummary.origin || 'N/A'}
              </div>
              <div className="summary-item">
                <strong>Duration:</strong> {tripSummary.duration || 'N/A'} days
              </div>
              <div className="summary-item">
                <strong>Start Date:</strong> {tripSummary.start_date ? formatDate(tripSummary.start_date) : 'N/A'}
              </div>
              <div className="summary-item">
                <strong>Total Budget:</strong> {tripSummary.total_budget ? formatCurrency(tripSummary.total_budget) : 'N/A'}
              </div>
              <div className="summary-item">
                <strong>Accepted On:</strong> {new Date(trip.createdAt).toLocaleDateString()}
              </div>
            </div>
          </div>

          {/* Daily Itinerary */}
          {dailyItinerary.length > 0 && (
            <div className="itinerary-section">
              <h3>📅 Daily Itinerary</h3>
              <div className="itinerary-timeline">
                {dailyItinerary.map((day, index) => (
                  <div key={index} className="day-card-modal">
                    <div className="day-header-modal">
                      <div className="day-number-modal">{day.day}</div>
                      <div className="day-info-modal">
                        <h4>Day {day.day}</h4>
                        {day.date && <span className="day-date">{formatDate(day.date)}</span>}
                        {day.weather && <span className="day-weather">🌤️ {day.weather}</span>}
                      </div>
                    </div>

                    <div className="day-activities-modal">
                      {/* Morning Activity */}
                      {day.morning_activity && (
                        <div className="activity-block-modal morning">
                          <div className="activity-time-modal">🌅 Morning ({day.morning_activity.time})</div>
                          <div className="activity-content-modal">
                            <h5>{day.morning_activity.spot_name}</h5>
                            <p>{day.morning_activity.description}</p>
                            {day.morning_activity.entry_fee > 0 && (
                              <span className="fee-modal">Entry: ৳{day.morning_activity.entry_fee}</span>
                            )}
                            <TripImage 
                              src={day.morning_activity.image_url} 
                              alt={day.morning_activity.spot_name} 
                              className="activity-image-modal"
                              fallbackType="spot"
                            />
                          </div>
                        </div>
                      )}

                      {/* Lunch Options */}
                      {day.lunch_options && day.lunch_options.length > 0 && (
                        <div className="activity-block-modal lunch">
                          <div className="activity-time-modal">🍽️ Lunch</div>
                          <div className="activity-content-modal">
                            {day.lunch_options.map((restaurant, idx) => (
                              <div key={idx} className="restaurant-option-modal">
                                <h5>{restaurant.restaurant_name}</h5>
                                <p>{restaurant.cuisine} • ⭐ {restaurant.rating}/5</p>
                                <span className="cost-modal">৳{restaurant.cost_per_person}/person</span>
                                <TripImage 
                                  src={restaurant.image_url} 
                                  alt={restaurant.restaurant_name} 
                                  className="activity-image-modal small"
                                  fallbackType="restaurant"
                                />
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Afternoon Activities */}
                      {day.afternoon_activities && day.afternoon_activities.length > 0 && (
                        <div className="activity-block-modal afternoon">
                          <div className="activity-time-modal">☀️ Afternoon</div>
                          <div className="activity-content-modal">
                            {day.afternoon_activities.map((activity, idx) => (
                              <div key={idx} className="afternoon-activity-modal">
                                <h5>{activity.spot_name}</h5>
                                <p>{activity.description}</p>
                                <span className="activity-time-modal">🕐 {activity.time}</span>
                                {activity.entry_fee > 0 && (
                                  <span className="fee-modal">Entry: ৳{activity.entry_fee}</span>
                                )}
                                <TripImage 
                                  src={activity.image_url} 
                                  alt={activity.spot_name} 
                                  className="activity-image-modal small"
                                  fallbackType="spot"
                                />
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Dinner Options */}
                      {day.dinner_options && day.dinner_options.length > 0 && (
                        <div className="activity-block-modal dinner">
                          <div className="activity-time-modal">🌙 Dinner</div>
                          <div className="activity-content-modal">
                            {day.dinner_options.map((restaurant, idx) => (
                              <div key={idx} className="restaurant-option-modal">
                                <h5>{restaurant.restaurant_name}</h5>
                                <p>{restaurant.cuisine} • ⭐ {restaurant.rating}/5</p>
                                <span className="cost-modal">৳{restaurant.cost_per_person}/person</span>
                                <TripImage 
                                  src={restaurant.image_url} 
                                  alt={restaurant.restaurant_name} 
                                  className="activity-image-modal small"
                                  fallbackType="restaurant"
                                />
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Accommodation */}
                      {day.accommodation_options && day.accommodation_options.length > 0 && (
                        <div className="activity-block-modal accommodation">
                          <div className="activity-time-modal">🏨 Stay</div>
                          <div className="activity-content-modal">
                            {day.accommodation_options.map((hotel, idx) => (
                              <div key={idx} className="hotel-option-modal">
                                <h5>{hotel.hotel_name}</h5>
                                <div className="hotel-details-modal">
                                  <span className="rating-modal">⭐ {hotel.rating}/5</span>
                                  <span className="cost-modal">৳{hotel.cost_per_night}/night</span>
                                  <span className="amenities-modal">🎯 {hotel.amenities}</span>
                                </div>
                                <TripImage 
                                  src={hotel.image_url} 
                                  alt={hotel.hotel_name} 
                                  className="activity-image-modal small"
                                  fallbackType="hotel"
                                />
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Daily Budget */}
                      {day.day_budget && (
                        <div className="day-budget-modal">
                          <h6>💰 Daily Budget: ৳{day.day_budget.total}</h6>
                          <div className="budget-details-modal">
                            <span>🏨 Accommodation: ৳{day.day_budget.accommodation}</span>
                            <span>🍽️ Meals: ৳{day.day_budget.meals}</span>
                            <span>🎯 Activities: ৳{day.day_budget.activities}</span>
                            <span>🚗 Transport: ৳{day.day_budget.transport}</span>
                            <span>📦 Misc: ৳{day.day_budget.misc}</span>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Budget Summary */}
          {budgetSummary && Object.keys(budgetSummary).length > 0 && (
            <div className="budget-summary-modal">
              <h3>💰 Budget Summary</h3>
              <div className="budget-grid-modal">
                <div className="budget-item-modal">
                  <span>🏨 Accommodation</span>
                  <span>৳{budgetSummary.total_accommodation || 0}</span>
                </div>
                <div className="budget-item-modal">
                  <span>🍽️ Meals</span>
                  <span>৳{budgetSummary.total_meals || 0}</span>
                </div>
                <div className="budget-item-modal">
                  <span>🎯 Activities</span>
                  <span>৳{budgetSummary.total_activities || 0}</span>
                </div>
                <div className="budget-item-modal">
                  <span>🚗 Transport</span>
                  <span>৳{budgetSummary.total_transport || 0}</span>
                </div>
                <div className="budget-item-modal">
                  <span>📦 Miscellaneous</span>
                  <span>৳{budgetSummary.total_misc || 0}</span>
                </div>
                <div className="budget-item-modal total">
                  <span>💳 Total Cost</span>
                  <span>৳{budgetSummary.grand_total || 0}</span>
                </div>
                <div className="budget-item-modal remaining">
                  <span>💵 Remaining Budget</span>
                  <span>৳{budgetSummary.remaining || 0}</span>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn-outline" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default TripDetailsModal;
