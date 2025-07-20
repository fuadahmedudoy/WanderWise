import React, { useState, useEffect } from 'react';
import { tripApi } from '../api';
import './GroupTripBrowser.css';

const GroupTripBrowser = () => {
    const [availableTrips, setAvailableTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');
    const [showDetails, setShowDetails] = useState(false);
    const [selectedTrip, setSelectedTrip] = useState(null);
    const [showJoinModal, setShowJoinModal] = useState(false);
    const [joinMessage, setJoinMessage] = useState('');

    useEffect(() => {
        fetchAvailableTrips();
    }, []);

    const fetchAvailableTrips = async () => {
        try {
            setLoading(true);
            setError(null);
            console.log('Fetching available group trips...');
            const response = await tripApi.getAvailableGroupTrips();
            console.log('API Response:', response);
            
            if (response.success) {
                console.log('Available trips data:', response.data);
                setAvailableTrips(response.data || []);
            } else {
                throw new Error(response.error || 'Failed to fetch available trips');
            }
        } catch (error) {
            console.error('Error fetching available trips:', error);
            setError('Failed to load available group trips. Please try again.');
            setAvailableTrips([]);
        } finally {
            setLoading(false);
        }
    };

    const handleViewDetails = async (trip) => {
        try {
            const response = await tripApi.getGroupTripDetails(trip.id);
            if (response.success) {
                setSelectedTrip(response.data);
                setShowDetails(true);
            }
        } catch (error) {
            console.error('Error fetching trip details:', error);
        }
    };

    const handleJoinTrip = (trip) => {
        setSelectedTrip(trip);
        setShowJoinModal(true);
        setJoinMessage('');
    };

    const confirmJoinTrip = async () => {
        try {
            const response = await tripApi.joinGroupTrip(selectedTrip.id, { joinMessage });
            if (response.success) {
                alert('Join request sent successfully!');
                setShowJoinModal(false);
                fetchAvailableTrips();
            } else {
                alert(response.error || 'Failed to send join request');
            }
        } catch (error) {
            console.error('Error joining trip:', error);
            alert('Failed to send join request. Please try again.');
        }
    };

    const handleRespondToRequest = async (memberId, approve) => {
        try {
            const response = await tripApi.respondToJoinRequest(selectedTrip.id, memberId, approve);
            if (response.success) {
                const action = approve ? 'accepted' : 'declined';
                alert(`Join request ${action} successfully!`);
                // Refresh the trip details to show updated member list
                const updatedTrip = await tripApi.getGroupTripDetails(selectedTrip.id);
                if (updatedTrip.success) {
                    setSelectedTrip(updatedTrip.data);
                }
                // Also refresh the main trip list
                fetchAvailableTrips();
            } else {
                alert(response.error || 'Failed to respond to join request');
            }
        } catch (error) {
            console.error('Error responding to join request:', error);
            alert('Failed to respond to join request. Please try again.');
        }
    };

    const getStatusBadgeClass = (status) => {
        switch (status?.toLowerCase()) {
            case 'open': return 'status-open';
            case 'full': return 'status-full';
            case 'closed': return 'status-closed';
            default: return 'status-unknown';
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Date not specified';
        try {
            return new Date(dateString).toLocaleDateString();
        } catch {
            return 'Invalid date';
        }
    };

    const parseTrippPlan = (tripPlan) => {
        if (typeof tripPlan === 'string') {
            try {
                return JSON.parse(tripPlan);
            } catch (e) {
                console.error('Failed to parse tripPlan:', e);
                return {};
            }
        }
        return tripPlan || {};
    };

    // Filter trips based on search and status
    const filteredTrips = availableTrips.filter(trip => {
        const parsedTripPlan = parseTrippPlan(trip.tripPlan);
        const matchesSearch = trip.groupName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            trip.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            (parsedTripPlan?.trip_summary?.destination || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                            (parsedTripPlan?.trip_summary?.origin || '').toLowerCase().includes(searchQuery.toLowerCase());
        
        const matchesStatus = filterStatus === 'all' || trip.status?.toLowerCase() === filterStatus.toLowerCase();
        
        return matchesSearch && matchesStatus;
    });

    if (loading) {
        return (
            <div className="group-trip-browser">
                <div className="loading-container">
                    <div className="loading-spinner"></div>
                    <p>Loading available group trips...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="group-trip-browser">
            <div className="browser-header">
                <h2>Browse Group Trips</h2>
                <p>Discover and join exciting group travel adventures</p>
            </div>

            <div className="filters-section">
                <div className="search-filter">
                    <input
                        type="text"
                        placeholder="Search trips by destination, name, or description..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="search-input"
                    />
                </div>
                
                <div className="status-filter">
                    <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">All Status</option>
                        <option value="open">Open</option>
                        <option value="full">Full</option>
                        <option value="closed">Closed</option>
                    </select>
                </div>
            </div>

            {error && (
                <div className="error-message">
                    <p>{error}</p>
                    <button onClick={fetchAvailableTrips} className="retry-btn">
                        Try Again
                    </button>
                </div>
            )}

            <div className="trips-grid">
                {filteredTrips.length === 0 && !loading ? (
                    <div className="no-trips-message">
                        <h3>No trips found</h3>
                        <p>No group trips are currently available.</p>
                    </div>
                ) : (
                    filteredTrips.map(trip => {
                        // Parse tripPlan and add debugging
                        const parsedTripPlan = parseTrippPlan(trip.tripPlan);
                        
                        console.log('=== TRIP DEBUG ===');
                        console.log('Trip:', trip.groupName);
                        console.log('Raw tripPlan type:', typeof trip.tripPlan);
                        console.log('Raw tripPlan:', trip.tripPlan);
                        console.log('Parsed tripPlan:', parsedTripPlan);
                        console.log('Trip Summary:', parsedTripPlan?.trip_summary);
                        console.log('==================');
                        
                        return (
                            <div key={trip.id} className="trip-card">
                                <div className="trip-card-header">
                                    <h3>{trip.groupName}</h3>
                                    <span className={`status-badge ${getStatusBadgeClass(trip.status)}`}>
                                        {trip.status}
                                    </span>
                                </div>
                                
                                <div className="trip-description">
                                    <p>{trip.description}</p>
                                </div>
                                
                                {/* Trip Summary Card */}
                                <div className="trip-summary-card">
                                    <div className="summary-row">
                                        <span className="label">From:</span>
                                        <span className="value">{parsedTripPlan?.trip_summary?.origin || 'N/A'}</span>
                                    </div>
                                    <div className="summary-row">
                                        <span className="label">To:</span>
                                        <span className="value">{parsedTripPlan?.trip_summary?.destination || 'N/A'}</span>
                                    </div>
                                    <div className="summary-row">
                                        <span className="label">Duration:</span>
                                        <span className="value">{parsedTripPlan?.trip_summary?.duration || 'N/A'} days</span>
                                    </div>
                                    <div className="summary-row">
                                        <span className="label">Start Date:</span>
                                        <span className="value">{parsedTripPlan?.trip_summary?.start_date || 'N/A'}</span>
                                    </div>
                                    <div className="summary-row">
                                        <span className="label">Budget:</span>
                                        <span className="value">৳{parsedTripPlan?.trip_summary?.total_budget?.toLocaleString() || 'N/A'}</span>
                                    </div>
                                </div>
                                
                                <div className="trip-meta">
                                    <span>Members: {trip.currentMembers}/{trip.maxPeople}</span>
                                    <span>Created by: {trip.creatorName || 'Unknown'}</span>
                                </div>
                                
                                {/* Two Buttons */}
                                <div className="trip-actions">
                                    <button 
                                        className="btn btn-secondary"
                                        onClick={() => handleViewDetails(trip)}
                                    >
                                        View Details
                                    </button>
                                    <button 
                                        className="btn btn-primary"
                                        onClick={() => handleJoinTrip(trip)}
                                        disabled={trip.status !== 'OPEN'}
                                    >
                                        Request to Join
                                    </button>
                                </div>
                            </div>
                        );
                    })
                )}
            </div>

            {/* Trip Details Modal */}
            {showDetails && selectedTrip && (
                <div className="trip-details-modal" onClick={() => setShowDetails(false)}>
                    <div className="trip-details-content" onClick={e => e.stopPropagation()}>
                        <div className="details-header">
                            <h2>{selectedTrip.groupName}</h2>
                            <button 
                                className="close-details-btn"
                                onClick={() => setShowDetails(false)}
                            >
                                ✕
                            </button>
                        </div>

                        <div className="details-body">
                            <div className="trip-overview">
                                <h3>Trip Overview</h3>
                                <div className="overview-grid">
                                    <div className="overview-item">
                                        <strong>Destination:</strong>
                                        <span>{selectedTrip.tripPlan?.trip_summary?.destination || 'Not specified'}</span>
                                    </div>
                                    <div className="overview-item">
                                        <strong>Origin:</strong>
                                        <span>{selectedTrip.tripPlan?.trip_summary?.origin || 'Not specified'}</span>
                                    </div>
                                    <div className="overview-item">
                                        <strong>Duration:</strong>
                                        <span>{selectedTrip.tripPlan?.trip_summary?.duration || 'Not specified'} days</span>
                                    </div>
                                    <div className="overview-item">
                                        <strong>Start Date:</strong>
                                        <span>{selectedTrip.tripPlan?.trip_summary?.start_date ? 
                                               formatDate(selectedTrip.tripPlan.trip_summary.start_date) : 
                                               'Not specified'}</span>
                                    </div>
                                    <div className="overview-item">
                                        <strong>Budget per Person:</strong>
                                        <span>{selectedTrip.tripPlan?.trip_summary?.total_budget ? 
                                               `৳${selectedTrip.tripPlan.trip_summary.total_budget.toLocaleString()}` : 
                                               'Not specified'}</span>
                                    </div>
                                    <div className="overview-item">
                                        <strong>Status:</strong>
                                        <span className={`status-badge ${getStatusBadgeClass(selectedTrip.status)}`}>
                                            {selectedTrip.status}
                                        </span>
                                    </div>
                                    <div className="overview-item">
                                        <strong>Members:</strong>
                                        <span>{selectedTrip.currentMembers}/{selectedTrip.maxPeople}</span>
                                    </div>
                                    <div className="overview-item">
                                        <strong>Created by:</strong>
                                        <span>{selectedTrip.creatorName || 'Unknown'}</span>
                                    </div>
                                </div>
                            </div>

                            {/* Join Requests Management - Only show for trip creator */}
                            {selectedTrip.isCreator && selectedTrip.members && selectedTrip.members.length > 0 && (
                                <div className="join-requests-section">
                                    <h3>Join Requests</h3>
                                    <div className="join-requests-list">
                                        {selectedTrip.members
                                            .filter(member => member.status === 'REQUESTED')
                                            .map((member) => (
                                            <div key={member.id} className="join-request-item">
                                                <div className="request-info">
                                                    <h4>{member.userName}</h4>
                                                    <p className="request-email">{member.userEmail}</p>
                                                    {member.joinMessage && (
                                                        <p className="request-message">"{member.joinMessage}"</p>
                                                    )}
                                                    <p className="request-date">
                                                        Requested: {formatDate(member.joinedAt)}
                                                    </p>
                                                </div>
                                                <div className="request-actions">
                                                    <button 
                                                        className="btn btn-success btn-sm"
                                                        onClick={() => handleRespondToRequest(member.id, true)}
                                                    >
                                                        Accept
                                                    </button>
                                                    <button 
                                                        className="btn btn-danger btn-sm"
                                                        onClick={() => handleRespondToRequest(member.id, false)}
                                                    >
                                                        Decline
                                                    </button>
                                                </div>
                                            </div>
                                        ))}
                                        {selectedTrip.members.filter(member => member.status === 'REQUESTED').length === 0 && (
                                            <p className="no-requests">No pending join requests</p>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Accepted Members - Only show for trip creator */}
                            {selectedTrip.isCreator && selectedTrip.members && selectedTrip.members.length > 0 && (
                                <div className="accepted-members-section">
                                    <h3>Accepted Members</h3>
                                    <div className="members-list">
                                        {selectedTrip.members
                                            .filter(member => member.status === 'ACCEPTED')
                                            .map((member) => (
                                            <div key={member.id} className="member-item">
                                                <div className="member-info">
                                                    <h4>{member.userName}</h4>
                                                    <p className="member-email">{member.userEmail}</p>
                                                    <p className="joined-date">
                                                        Joined: {formatDate(member.joinedAt)}
                                                    </p>
                                                </div>
                                            </div>
                                        ))}
                                        {selectedTrip.members.filter(member => member.status === 'ACCEPTED').length === 0 && (
                                            <p className="no-members">No accepted members yet</p>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Budget Breakdown */}
                            {selectedTrip.tripPlan?.budget_summary && (
                                <div className="budget-section">
                                    <h3>Budget Breakdown</h3>
                                    <div className="budget-grid">
                                        <div className="budget-item">
                                            <span>Total Budget:</span>
                                            <span>৳{selectedTrip.tripPlan.budget_summary.grand_total?.toLocaleString()}</span>
                                        </div>
                                        <div className="budget-item">
                                            <span>Accommodation:</span>
                                            <span>৳{selectedTrip.tripPlan.budget_summary.total_accommodation?.toLocaleString()}</span>
                                        </div>
                                        <div className="budget-item">
                                            <span>Transport:</span>
                                            <span>৳{selectedTrip.tripPlan.budget_summary.total_transport?.toLocaleString()}</span>
                                        </div>
                                        <div className="budget-item">
                                            <span>Meals:</span>
                                            <span>৳{selectedTrip.tripPlan.budget_summary.total_meals?.toLocaleString()}</span>
                                        </div>
                                        <div className="budget-item">
                                            <span>Activities:</span>
                                            <span>৳{selectedTrip.tripPlan.budget_summary.total_activities?.toLocaleString()}</span>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Daily Itinerary */}
                            {selectedTrip.tripPlan?.daily_itinerary && (
                                <div className="itinerary-section">
                                    <h3>Daily Itinerary</h3>
                                    <div className="itinerary-timeline">
                                        {selectedTrip.tripPlan.daily_itinerary.map((day, index) => (
                                            <div key={index} className="day-card">
                                                <div className="day-header">
                                                    <h4>Day {day.day}</h4>
                                                    <span className="day-date">{formatDate(day.date)}</span>
                                                    <div className="day-budget">Budget: ৳{day.day_budget?.total?.toLocaleString()}</div>
                                                </div>
                                                
                                                <div className="day-activities">
                                                    {day.morning_activity && (
                                                        <div className="activity-item">
                                                            <div className="activity-time">Morning: {day.morning_activity.time}</div>
                                                            <div className="activity-name">{day.morning_activity.spot_name}</div>
                                                            <div className="activity-desc">{day.morning_activity.description}</div>
                                                        </div>
                                                    )}
                                                    
                                                    {day.lunch_options && day.lunch_options[0] && (
                                                        <div className="meal-item">
                                                            <div className="meal-time">Lunch: {day.lunch_options[0].time}</div>
                                                            <div className="meal-name">{day.lunch_options[0].restaurant_name}</div>
                                                            <div className="meal-cost">৳{day.lunch_options[0].cost_per_person}</div>
                                                        </div>
                                                    )}
                                                    
                                                    {day.afternoon_activities && day.afternoon_activities[0] && (
                                                        <div className="activity-item">
                                                            <div className="activity-time">Afternoon: {day.afternoon_activities[0].time}</div>
                                                            <div className="activity-name">{day.afternoon_activities[0].spot_name}</div>
                                                            <div className="activity-desc">{day.afternoon_activities[0].description}</div>
                                                        </div>
                                                    )}
                                                    
                                                    {day.dinner_options && day.dinner_options[0] && (
                                                        <div className="meal-item">
                                                            <div className="meal-time">Dinner: {day.dinner_options[0].time}</div>
                                                            <div className="meal-name">{day.dinner_options[0].restaurant_name}</div>
                                                            <div className="meal-cost">৳{day.dinner_options[0].cost_per_person}</div>
                                                        </div>
                                                    )}
                                                    
                                                    {day.accommodation_options && day.accommodation_options[0] && (
                                                        <div className="accommodation-item">
                                                            <div className="hotel-name">Stay: {day.accommodation_options[0].hotel_name}</div>
                                                            <div className="hotel-checkin">Check-in: {day.accommodation_options[0].check_in_time}</div>
                                                            <div className="hotel-cost">৳{day.accommodation_options[0].cost_per_night}/night</div>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            <p className="trip-description-full">{selectedTrip.description}</p>
                        </div>

                        <div className="details-footer">
                            {selectedTrip.status === 'OPEN' && (
                                <button 
                                    className="btn btn-primary"
                                    onClick={() => {
                                        setShowDetails(false);
                                        handleJoinTrip(selectedTrip);
                                    }}
                                >
                                    Join This Trip
                                </button>
                            )}
                            <button 
                                className="btn btn-secondary"
                                onClick={() => setShowDetails(false)}
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Join Trip Modal */}
            {showJoinModal && (
                <div className="join-modal" onClick={() => setShowJoinModal(false)}>
                    <div className="join-modal-content" onClick={e => e.stopPropagation()}>
                        <h3>Join Group Trip</h3>
                        <p>Send a message to the trip creator with your join request:</p>
                        <textarea
                            value={joinMessage}
                            onChange={(e) => setJoinMessage(e.target.value)}
                            placeholder="Hi! I'd like to join your trip. Here's a bit about me..."
                            className="join-message-input"
                            rows="4"
                        />
                        <div className="join-modal-actions">
                            <button 
                                className="btn btn-primary"
                                onClick={confirmJoinTrip}
                            >
                                Send Request
                            </button>
                            <button 
                                className="btn btn-secondary"
                                onClick={() => setShowJoinModal(false)}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GroupTripBrowser;
