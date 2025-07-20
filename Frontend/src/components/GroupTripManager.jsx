import React, { useState, useEffect, useContext } from 'react';
import './GroupTripManager.css';
import './GroupTripBrowser.css'; // Import shared card styles
import { tripApi } from '../api';
import AuthContext from '../context/AuthContext';

const GroupTripManager = () => {
    const { currentUser } = useContext(AuthContext);
    const [myGroupTrips, setMyGroupTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedTrip, setSelectedTrip] = useState(null);
    const [showDetails, setShowDetails] = useState(false);
    const [showRequests, setShowRequests] = useState(false);
    const [showMessage, setShowMessage] = useState(false);
    const [showManageGroup, setShowManageGroup] = useState(false);
    const [showGroupChat, setShowGroupChat] = useState(false);
    const [chatMessages, setChatMessages] = useState([]);
    const [chatLoading, setChatLoading] = useState(false);
    const [chatError, setChatError] = useState(null);
    const [newMessage, setNewMessage] = useState('');
    const [showUpdateMeeting, setShowUpdateMeeting] = useState(false);
    const [newMeetingPoint, setNewMeetingPoint] = useState('');
    const [showUpdateMaxPeople, setShowUpdateMaxPeople] = useState(false);
    const [newMaxPeople, setNewMaxPeople] = useState('');
    const [showTripStatus, setShowTripStatus] = useState(false);
    const [activeTab, setActiveTab] = useState('created'); // 'created', 'joined', 'requested'
    const [groupMembers, setGroupMembers] = useState([]);
    const [showMemberManagement, setShowMemberManagement] = useState(false);

    useEffect(() => {
        fetchMyGroupTrips();
    }, []);

    const fetchMyGroupTrips = async () => {
        try {
            setLoading(true);
            setError(null);
            console.log('🔄 Fetching my group trips...');
            
            const response = await tripApi.getMyGroupTrips();
            console.log('📋 Raw response:', response);
            
            if (response.success) {
                const trips = response.data || [];
                console.log('✅ Successfully fetched', trips.length, 'group trips');
                console.log('🔍 Trip details for debugging:');
                trips.forEach((trip, index) => {
                    console.log(`Trip ${index + 1}:`, {
                        id: trip.id,
                        name: trip.groupName,
                        isCreator: trip.isCreator,
                        memberStatus: trip.memberStatus,
                        status: trip.status
                    });
                });
                setMyGroupTrips(trips);
            } else {
                throw new Error(response.error || 'Failed to fetch group trips');
            }
        } catch (error) {
            console.error('❌ Error fetching my group trips:', error);
            setError('Failed to load your group trips. Please try again.');
            setMyGroupTrips([]);
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
            } else {
                alert('Failed to load trip details: ' + (response.error || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error fetching trip details:', error);
            alert('Failed to load trip details. Please try again.');
        }
    };

    const handleApproveRequest = async (memberId, approve) => {
        try {
            const response = await tripApi.respondToJoinRequest(selectedTrip.id, memberId, approve);
            if (response.success) {
                // Refresh the member list if we're showing member management
                if (showMemberManagement) {
                    await fetchGroupMembers(selectedTrip.id);
                }
                // Refresh the trip details
                handleViewDetails(selectedTrip);
                alert(approve ? 'Member approved successfully!' : 'Request declined.');
            } else {
                alert('Failed to respond to request: ' + (response.error || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error responding to join request:', error);
            alert('Failed to respond to request. Please try again.');
        }
    };

    const handleViewRequests = async (trip) => {
        try {
            const response = await tripApi.getGroupTripDetails(trip.id);
            if (response.success) {
                setSelectedTrip(response.data);
                setShowRequests(true);
            } else {
                alert('Failed to load trip requests: ' + (response.error || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error fetching trip requests:', error);
            alert('Failed to load trip requests. Please try again.');
        }
    };

    const handleViewMessage = async (trip) => {
        try {
            const response = await tripApi.getGroupTripDetails(trip.id);
            if (response.success) {
                setSelectedTrip(response.data);
                setShowMessage(true);
            } else {
                alert('Failed to load trip details: ' + (response.error || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error fetching trip details:', error);
            alert('Failed to load trip details. Please try again.');
        }
    };

    const handleManageGroup = (trip) => {
        setSelectedTrip(trip);
        setShowManageGroup(true);
    };

    const handleJoinGroupChat = (trip) => {
        setSelectedTrip(trip);
        setShowGroupChat(true);
        // Load chat after opening the modal
        setTimeout(() => {
            loadGroupChat();
            // Also load group members for UI
            fetchGroupMembers(trip.id);
        }, 100);
    };

    const handleManageGroupOption = (option) => {
        setShowManageGroup(false);
        
        switch(option) {
            case 'chat':
                setShowGroupChat(true);
                // Load chat after opening the modal
                setTimeout(() => {
                    loadGroupChat();
                    // Also load group members if user is the creator
                    if (selectedTrip?.isCreator) {
                        fetchGroupMembers(selectedTrip.id);
                    }
                }, 100);
                break;
            case 'meeting':
                setNewMeetingPoint(selectedTrip?.meetingPoint || '');
                setShowUpdateMeeting(true);
                break;
            case 'maxpeople':
                setNewMaxPeople(selectedTrip?.maxPeople?.toString() || '');
                setShowUpdateMaxPeople(true);
                break;
            case 'status':
                setShowTripStatus(true);
                break;
            default:
                break;
        }
    };

    const loadGroupChat = async () => {
        if (!selectedTrip || !selectedTrip.id) {
            console.error('❌ Cannot load chat: No trip selected or missing trip ID');
            setChatError('Cannot load chat: No trip selected');
            return;
        }

        setChatLoading(true);
        setChatError(null);

        try {
            console.log('📨 Loading group chat messages for trip:', selectedTrip.id);
            console.log('📨 Selected trip details:', selectedTrip);
            console.log('📨 User access status - isCreator:', selectedTrip.isCreator, 'memberStatus:', selectedTrip.memberStatus);
            
            const response = await tripApi.getGroupChatMessages(selectedTrip.id);
            console.log('📨 Chat response received:', response);
            
            if (response.success) {
                console.log('✅ Chat messages loaded successfully:', response.data);
                setChatMessages(response.data || []);
                setChatError(null);
            } else {
                console.error('❌ Failed to load chat messages:', response.error);
                setChatMessages([]);
                setChatError(response.error || 'Failed to load messages');
            }
        } catch (error) {
            console.error('❌ Error loading chat messages:', error);
            setChatMessages([]);
            setChatError('Failed to load chat messages. Please try again.');
        } finally {
            setChatLoading(false);
        }
    };

    const fetchGroupMembers = async (groupTripId) => {
        try {
            console.log('👥 Fetching group members for trip:', groupTripId);
            const response = await tripApi.getGroupTripMembers(groupTripId);
            if (response.success && response.data) {
                console.log('✅ Group members loaded successfully:', response.data);
                setGroupMembers(response.data);
                return response.data;
            } else {
                console.error('❌ Failed to load group members:', response.error);
                setGroupMembers([]);
                return [];
            }
        } catch (error) {
            console.error('❌ Error fetching group members:', error);
            setGroupMembers([]);
            return [];
        }
    };

    const sendChatMessage = async () => {
        if (!newMessage.trim()) return;

        if (!selectedTrip || !selectedTrip.id) {
            alert('Error: No trip selected');
            return;
        }

        try {
            console.log('� Sending chat message:', newMessage.trim());
            const response = await tripApi.sendGroupChatMessage(selectedTrip.id, newMessage.trim());
            if (response.success) {
                console.log('✅ Message sent successfully:', response.data);
                // Add the new message to the list
                setChatMessages(prev => [...prev, response.data]);
                setNewMessage('');
            } else {
                console.error('❌ Failed to send message:', response.error);
                alert('Failed to send message: ' + (response.error || 'Unknown error'));
            }
        } catch (error) {
            console.error('❌ Error sending message:', error);
            alert('Failed to send message. Please try again.');
        }
    };

    const handleUpdateMeetingPoint = async () => {
        if (!newMeetingPoint.trim()) {
            alert('Please enter a meeting point');
            return;
        }

        try {
            // In a real implementation, you would call your backend API
            // const response = await tripApi.updateMeetingPoint(selectedTrip.id, newMeetingPoint);
            
            // For now, let's simulate a successful update
            setSelectedTrip(prev => ({
                ...prev,
                meetingPoint: newMeetingPoint.trim()
            }));

            // Update the trip in the list
            setMyGroupTrips(prev => prev.map(trip => 
                trip.id === selectedTrip.id 
                    ? { ...trip, meetingPoint: newMeetingPoint.trim() }
                    : trip
            ));

            setShowUpdateMeeting(false);
            alert('Meeting point updated successfully!');
            
        } catch (error) {
            console.error('Error updating meeting point:', error);
            alert('Failed to update meeting point. Please try again.');
        }
    };

    const handleUpdateMaxPeople = async () => {
        const maxPeople = parseInt(newMaxPeople);
        
        if (!maxPeople || maxPeople < 1) {
            alert('Please enter a valid number (minimum 1)');
            return;
        }

        if (maxPeople < selectedTrip.currentMembers) {
            alert(`Cannot set max people to ${maxPeople}. Current members: ${selectedTrip.currentMembers}`);
            return;
        }

        try {
            // In a real implementation, you would call your backend API
            // const response = await tripApi.updateMaxPeople(selectedTrip.id, maxPeople);
            
            // For now, let's simulate a successful update
            setSelectedTrip(prev => ({
                ...prev,
                maxPeople: maxPeople
            }));

            // Update the trip in the list
            setMyGroupTrips(prev => prev.map(trip => 
                trip.id === selectedTrip.id 
                    ? { ...trip, maxPeople: maxPeople }
                    : trip
            ));

            setShowUpdateMaxPeople(false);
            alert('Maximum people limit updated successfully!');
            
        } catch (error) {
            console.error('Error updating max people:', error);
            alert('Failed to update max people. Please try again.');
        }
    };

    const handleTripStatusChange = async (newStatus) => {
        try {
            // In a real implementation, you would call your backend API
            // const response = await tripApi.updateTripStatus(selectedTrip.id, newStatus);
            
            // For now, let's simulate a successful update
            setSelectedTrip(prev => ({
                ...prev,
                status: newStatus
            }));

            // Update the trip in the list
            setMyGroupTrips(prev => prev.map(trip => 
                trip.id === selectedTrip.id 
                    ? { ...trip, status: newStatus }
                    : trip
            ));

            setShowTripStatus(false);
            alert(`Trip ${newStatus.toLowerCase()} successfully!`);
            
        } catch (error) {
            console.error('Error updating trip status:', error);
            alert('Failed to update trip status. Please try again.');
        }
    };

    const handleRespondToRequest = async (memberId, approve) => {
        try {
            console.log('🔍 Responding to request:', {
                tripId: selectedTrip.id,
                userId: memberId, // This is actually the userId, not member record id
                approve,
                selectedTrip: selectedTrip.groupName
            });
            
            const response = await tripApi.respondToJoinRequest(selectedTrip.id, memberId, approve);
            console.log('📋 Response:', response);
            
            if (response.success) {
                const action = approve ? 'accepted' : 'declined';
                alert(`Join request ${action} successfully!`);
                // Refresh the trip details to show updated member list
                const updatedTrip = await tripApi.getGroupTripDetails(selectedTrip.id);
                if (updatedTrip.success) {
                    setSelectedTrip(updatedTrip.data);
                }
                // Also refresh the main trip list
                fetchMyGroupTrips();
            } else {
                console.error('❌ API Error:', response.error);
                alert(response.error || 'Failed to respond to join request');
            }
        } catch (error) {
            console.error('❌ Network Error responding to join request:', error);
            alert('Failed to respond to join request. Please try again. Error: ' + error.message);
        }
    };

    const getStatusBadgeClass = (status) => {
        switch (status?.toLowerCase()) {
            case 'open': return 'status-open';
            case 'full': return 'status-full';
            case 'closed': return 'status-closed';
            case 'completed': return 'status-completed';
            default: return 'status-default';
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

    const formatDate = (dateString) => {
        if (!dateString) return 'Date not specified';
        try {
            return new Date(dateString).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        } catch (error) {
            return 'Invalid date';
        }
    };

    // Filter trips based on active tab
    const filteredTrips = myGroupTrips.filter(trip => {
        console.log(`🔍 Filtering trip "${trip.groupName}":`, {
            activeTab,
            isCreator: trip.isCreator,
            memberStatus: trip.memberStatus,
            shouldShow: activeTab === 'created' ? trip.isCreator === true :
                       activeTab === 'joined' ? (trip.isCreator === false && trip.memberStatus === 'ACCEPTED') :
                       activeTab === 'requested' ? (trip.isCreator === false && trip.memberStatus === 'REQUESTED') : false
        });
        
        if (activeTab === 'created') {
            // Backend uses 'isCreator' field to indicate if user created the trip
            return trip.isCreator === true;
        } else if (activeTab === 'joined') {
            // Joined trips are those where user is not the creator and has ACCEPTED status
            return trip.isCreator === false && trip.memberStatus === 'ACCEPTED';
        } else if (activeTab === 'requested') {
            // Requested trips are those where user has REQUESTED status
            return trip.isCreator === false && trip.memberStatus === 'REQUESTED';
        }
        return false;
    });

    if (loading) {
        return (
            <div className="group-trip-manager">
                <div className="loading-container">
                    <div className="loading-spinner"></div>
                    <p>Loading your group trips...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="group-trip-manager">
            <div className="manager-header">
                <h2>My Group Trips</h2>
                <p>Manage your created trips and view trips you've joined</p>
            </div>

            <div className="tabs-container">
                <button 
                    className={`tab-button ${activeTab === 'created' ? 'active' : ''}`}
                    onClick={() => setActiveTab('created')}
                >
                    Created by Me ({myGroupTrips.filter(t => t.isCreator).length})
                </button>
                <button 
                    className={`tab-button ${activeTab === 'joined' ? 'active' : ''}`}
                    onClick={() => setActiveTab('joined')}
                >
                    Joined ({myGroupTrips.filter(t => !t.isCreator && t.memberStatus === 'ACCEPTED').length})
                </button>
                <button 
                    className={`tab-button ${activeTab === 'requested' ? 'active' : ''}`}
                    onClick={() => setActiveTab('requested')}
                >
                    Requested ({myGroupTrips.filter(t => !t.isCreator && t.memberStatus === 'REQUESTED').length})
                </button>
            </div>

            {error && (
                <div className="error-message">
                    <p>{error}</p>
                    <button onClick={fetchMyGroupTrips} className="retry-button">
                        Try Again
                    </button>
                </div>
            )}

            <div className="trips-grid">
                {filteredTrips.length === 0 ? (
                    <div className="no-trips-message">
                        <div className="no-trips-icon">🚫</div>
                        <h3>No {activeTab === 'created' ? 'created' : activeTab === 'joined' ? 'joined' : 'requested'} trips yet</h3>
                        <p>
                            {activeTab === 'created' 
                                ? 'Create a group trip from your saved trips to get started!'
                                : activeTab === 'joined'
                                ? 'Browse available group trips to join exciting adventures!'
                                : 'No pending trip requests. Browse trips and send join requests!'
                            }
                        </p>
                    </div>
                ) : (
                    filteredTrips.map(trip => {
                        // Parse tripPlan for consistent data access
                        const parsedTripPlan = parseTrippPlan(trip.tripPlan);
                        
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
                                    <span>Created: {formatDate(trip.createdAt)}</span>
                                </div>
                                
                                {/* Buttons */}
                                <div className="trip-actions">
                                    <button 
                                        className="btn btn-secondary"
                                        onClick={() => handleViewDetails(trip)}
                                    >
                                        View Details
                                    </button>
                                    {activeTab === 'created' && (
                                        <>
                                            <button 
                                                className="btn btn-primary"
                                                onClick={() => handleViewRequests(trip)}
                                            >
                                                Requests
                                            </button>
                                            <button 
                                                className="btn btn-success"
                                                onClick={() => handleManageGroup(trip)}
                                            >
                                                Manage Group
                                            </button>
                                        </>
                                    )}
                                    {activeTab === 'joined' && trip.memberStatus === 'ACCEPTED' && (
                                        <button 
                                            className="btn btn-success"
                                            onClick={() => handleJoinGroupChat(trip)}
                                        >
                                            <i className="fas fa-comments"></i>
                                            Join Group Chat
                                        </button>
                                    )}
                                    {activeTab === 'requested' && (
                                        <button 
                                            className="btn btn-info"
                                            onClick={() => handleViewMessage(trip)}
                                        >
                                            View Message
                                        </button>
                                    )}
                                </div>
                            </div>
                        );
                    })
                )}
            </div>

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
                                
                                <div className="description-section">
                                    <strong>Description:</strong>
                                    <p>{selectedTrip.description}</p>
                                </div>
                            </div>

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
                                                    
                                                    {day.afternoon_activities && day.afternoon_activities.map((activity, actIndex) => (
                                                        <div key={actIndex} className="activity-item">
                                                            <div className="activity-time">Afternoon: {activity.time}</div>
                                                            <div className="activity-name">{activity.spot_name}</div>
                                                            <div className="activity-desc">{activity.description}</div>
                                                        </div>
                                                    ))}
                                                    
                                                    {day.evening_activity && (
                                                        <div className="activity-item">
                                                            <div className="activity-time">Evening: {day.evening_activity.time}</div>
                                                            <div className="activity-name">{day.evening_activity.spot_name}</div>
                                                            <div className="activity-desc">{day.evening_activity.description}</div>
                                                        </div>
                                                    )}
                                                </div>
                                                
                                                <div className="day-accommodation">
                                                    <strong>Accommodation:</strong> {day.accommodation?.hotel_name || 'Not specified'}
                                                    {day.accommodation?.price && ` - ৳${day.accommodation.price.toLocaleString()}`}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Requests Modal */}
            {showRequests && selectedTrip && (
                <div className="trip-details-modal" onClick={() => setShowRequests(false)}>
                    <div className="trip-details-content" onClick={e => e.stopPropagation()}>
                        <div className="details-header">
                            <h2>Join Requests - {selectedTrip.groupName}</h2>
                            <button 
                                className="close-details-btn"
                                onClick={() => setShowRequests(false)}
                            >
                                ✕
                            </button>
                        </div>

                        <div className="details-body">
                            {/* Join Requests Management */}
                            {selectedTrip.members && selectedTrip.members.length > 0 ? (
                                <div className="join-requests-section">
                                    <h3>Pending Join Requests</h3>
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
                                                        onClick={() => handleRespondToRequest(member.userId, true)}
                                                    >
                                                        Accept
                                                    </button>
                                                    <button 
                                                        className="btn btn-danger btn-sm"
                                                        onClick={() => handleRespondToRequest(member.userId, false)}
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

                                    {/* Accepted Members */}
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
                                </div>
                            ) : (
                                <div className="no-requests-message">
                                    <h3>No requests yet</h3>
                                    <p>No one has requested to join this trip yet.</p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Message Modal */}
            {showMessage && selectedTrip && (
                <div className="trip-details-modal" onClick={() => setShowMessage(false)}>
                    <div className="trip-details-content" onClick={e => e.stopPropagation()}>
                        <div className="details-header">
                            <h2>My Join Request Message - {selectedTrip.groupName}</h2>
                            <button 
                                className="close-details-btn"
                                onClick={() => setShowMessage(false)}
                            >
                                ✕
                            </button>
                        </div>

                        <div className="details-body">
                            <div className="message-section">
                                <h3>Message I Sent:</h3>
                                {selectedTrip.members && selectedTrip.members.length > 0 ? (
                                    (() => {
                                        // Find the current user's member entry (should be the first and only one for non-creators)
                                        const currentUserMember = selectedTrip.members[0];
                                        
                                        return currentUserMember ? (
                                            <div className="my-message-card">
                                                <div className="message-content">
                                                    <p className="message-text">
                                                        {currentUserMember.joinMessage || 'No message provided'}
                                                    </p>
                                                    <div className="message-meta">
                                                        <p className="request-date">
                                                            Sent: {formatDate(currentUserMember.joinedAt)}
                                                        </p>
                                                        <p className="message-status">
                                                            Status: <span className={`status-${currentUserMember.status.toLowerCase()}`}>
                                                                {currentUserMember.status === 'REQUESTED' ? 'Pending' : currentUserMember.status}
                                                            </span>
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        ) : (
                                            <p className="no-message">No message found</p>
                                        );
                                    })()
                                ) : (
                                    <p className="no-message">No message data available</p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Group Management Modal */}
            {showManageGroup && selectedTrip && (
                <div className="modal-overlay" onClick={() => setShowManageGroup(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Group Management - {selectedTrip.groupName}</h3>
                            <button 
                                className="close-button" 
                                onClick={() => setShowManageGroup(false)}
                            >
                                ×
                            </button>
                        </div>
                        <div className="modal-body">
                            <div className="management-options">
                                <button 
                                    className="management-option-btn chat-btn"
                                    onClick={() => handleManageGroupOption('chat')}
                                >
                                    <i className="fas fa-comments"></i>
                                    Group Chat
                                </button>
                                <button 
                                    className="management-option-btn location-btn"
                                    onClick={() => handleManageGroupOption('meeting')}
                                >
                                    <i className="fas fa-map-marker-alt"></i>
                                    Update Meeting Point
                                </button>
                                <button 
                                    className="management-option-btn people-btn"
                                    onClick={() => handleManageGroupOption('maxpeople')}
                                >
                                    <i className="fas fa-users"></i>
                                    Change Max People
                                </button>
                                <button 
                                    className="management-option-btn status-btn"
                                    onClick={() => handleManageGroupOption('status')}
                                >
                                    <i className="fas fa-toggle-on"></i>
                                    Close/Open Trip
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Group Chat Modal */}
            {showGroupChat && selectedTrip && (
                <div className="modal-overlay" onClick={() => setShowGroupChat(false)}>
                    <div className="modal-content chat-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Group Chat - {selectedTrip.groupName}</h3>
                            <button 
                                className="close-button" 
                                onClick={() => setShowGroupChat(false)}
                            >
                                ×
                            </button>
                        </div>
                        <div className="modal-body chat-body">
                            {/* Chat Access Info */}
                            <div className="chat-access-info">
                                <div className="member-info">
                                    <span className={`access-status ${
                                        selectedTrip.isCreator || selectedTrip.memberStatus === 'ACCEPTED' 
                                            ? 'has-access' : 'no-access'
                                    }`}>
                                        {selectedTrip.isCreator 
                                            ? '👑 Trip Creator' 
                                            : selectedTrip.memberStatus === 'ACCEPTED' 
                                                ? '✅ Chat Access' 
                                                : selectedTrip.memberStatus === 'REQUESTED'
                                                    ? '⏳ Awaiting Approval'
                                                    : '❌ No Chat Access'
                                        }
                                    </span>
                                </div>
                                {selectedTrip.memberStatus === 'REQUESTED' && (
                                    <div className="access-notice">
                                        <p>🔒 You can view messages but cannot send until approved by the trip creator.</p>
                                    </div>
                                )}
                            </div>
                            
                            <div className="chat-messages">
                                {chatLoading ? (
                                    <div className="loading-messages">
                                        <p>🔄 Loading chat messages...</p>
                                    </div>
                                ) : chatError ? (
                                    <div className="error-messages">
                                        <p>❌ {chatError}</p>
                                        <button onClick={loadGroupChat} className="retry-btn">
                                            🔄 Retry
                                        </button>
                                    </div>
                                ) : chatMessages.length > 0 ? chatMessages.map(message => (
                                    <div 
                                        key={message.id} 
                                        className={`chat-message ${message.isCurrentUser ? 'current-user' : 'other-user'}`}
                                    >
                                        <div className="message-header">
                                            <span className="sender-name">{message.senderName}</span>
                                            <span className="message-time">
                                                {new Date(message.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                                            </span>
                                        </div>
                                        <div className="message-content">
                                            {message.message}
                                        </div>
                                    </div>
                                )) : (
                                    <div className="no-messages">
                                        <p>No messages yet. Be the first to start the conversation!</p>
                                    </div>
                                )}
                            </div>
                            <div className="chat-input-section">
                                {selectedTrip.isCreator || selectedTrip.memberStatus === 'ACCEPTED' ? (
                                    <div className="chat-input-container">
                                        <input
                                            type="text"
                                            value={newMessage}
                                            onChange={(e) => setNewMessage(e.target.value)}
                                            onKeyPress={(e) => e.key === 'Enter' && sendChatMessage()}
                                            placeholder="Type your message..."
                                            className="chat-input"
                                        />
                                        <button 
                                            onClick={sendChatMessage}
                                            className="send-button"
                                            disabled={!newMessage.trim()}
                                        >
                                            <i className="fas fa-paper-plane"></i>
                                        </button>
                                    </div>
                                ) : (
                                    <div className="chat-disabled-notice">
                                        <p>🔒 Chat access restricted. Only accepted members can send messages.</p>
                                        {selectedTrip.memberStatus === 'REQUESTED' && (
                                            <p>⏳ Your join request is pending approval from the trip creator.</p>
                                        )}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Update Meeting Point Modal */}
            {showUpdateMeeting && selectedTrip && (
                <div className="modal-overlay" onClick={() => setShowUpdateMeeting(false)}>
                    <div className="modal-content update-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Update Meeting Point - {selectedTrip.groupName}</h3>
                            <button 
                                className="close-button" 
                                onClick={() => setShowUpdateMeeting(false)}
                            >
                                ×
                            </button>
                        </div>
                        <div className="modal-body">
                            <div className="update-form">
                                <div className="form-group">
                                    <label htmlFor="meetingPoint">Current Meeting Point:</label>
                                    <div className="current-value">
                                        {selectedTrip.meetingPoint || 'Not specified'}
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label htmlFor="newMeetingPoint">New Meeting Point:</label>
                                    <input
                                        type="text"
                                        id="newMeetingPoint"
                                        value={newMeetingPoint}
                                        onChange={(e) => setNewMeetingPoint(e.target.value)}
                                        placeholder="Enter new meeting point..."
                                        className="form-input"
                                    />
                                </div>
                                <div className="form-actions">
                                    <button 
                                        onClick={() => setShowUpdateMeeting(false)}
                                        className="btn btn-secondary"
                                    >
                                        Cancel
                                    </button>
                                    <button 
                                        onClick={handleUpdateMeetingPoint}
                                        className="btn btn-primary"
                                        disabled={!newMeetingPoint.trim()}
                                    >
                                        Update Meeting Point
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Update Max People Modal */}
            {showUpdateMaxPeople && selectedTrip && (
                <div className="modal-overlay" onClick={() => setShowUpdateMaxPeople(false)}>
                    <div className="modal-content update-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Change Max People - {selectedTrip.groupName}</h3>
                            <button 
                                className="close-button" 
                                onClick={() => setShowUpdateMaxPeople(false)}
                            >
                                ×
                            </button>
                        </div>
                        <div className="modal-body">
                            <div className="update-form">
                                <div className="form-group">
                                    <label>Current Details:</label>
                                    <div className="current-value">
                                        Max People: {selectedTrip.maxPeople} | Current Members: {selectedTrip.currentMembers}
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label htmlFor="newMaxPeople">New Maximum People:</label>
                                    <input
                                        type="number"
                                        id="newMaxPeople"
                                        value={newMaxPeople}
                                        onChange={(e) => setNewMaxPeople(e.target.value)}
                                        placeholder="Enter new maximum..."
                                        className="form-input"
                                        min={selectedTrip.currentMembers}
                                    />
                                    <small className="helper-text">
                                        Must be at least {selectedTrip.currentMembers} (current members)
                                    </small>
                                </div>
                                <div className="form-actions">
                                    <button 
                                        onClick={() => setShowUpdateMaxPeople(false)}
                                        className="btn btn-secondary"
                                    >
                                        Cancel
                                    </button>
                                    <button 
                                        onClick={handleUpdateMaxPeople}
                                        className="btn btn-primary"
                                        disabled={!newMaxPeople || parseInt(newMaxPeople) < selectedTrip.currentMembers}
                                    >
                                        Update Max People
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Trip Status Modal */}
            {showTripStatus && selectedTrip && (
                <div className="modal-overlay" onClick={() => setShowTripStatus(false)}>
                    <div className="modal-content update-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Trip Status - {selectedTrip.groupName}</h3>
                            <button 
                                className="close-button" 
                                onClick={() => setShowTripStatus(false)}
                            >
                                ×
                            </button>
                        </div>
                        <div className="modal-body">
                            <div className="update-form">
                                <div className="form-group">
                                    <label>Current Status:</label>
                                    <div className={`current-value status-${selectedTrip.status?.toLowerCase()}`}>
                                        {selectedTrip.status || 'OPEN'}
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label>Change Status:</label>
                                    <div className="status-buttons">
                                        <button 
                                            onClick={() => handleTripStatusChange('OPEN')}
                                            className={`status-btn open-btn ${selectedTrip.status === 'OPEN' ? 'active' : ''}`}
                                            disabled={selectedTrip.status === 'OPEN'}
                                        >
                                            <i className="fas fa-door-open"></i>
                                            Open Trip
                                            <small>Allow new members to join</small>
                                        </button>
                                        <button 
                                            onClick={() => handleTripStatusChange('CLOSED')}
                                            className={`status-btn closed-btn ${selectedTrip.status === 'CLOSED' ? 'active' : ''}`}
                                            disabled={selectedTrip.status === 'CLOSED'}
                                        >
                                            <i className="fas fa-door-closed"></i>
                                            Close Trip
                                            <small>Stop accepting new members</small>
                                        </button>
                                    </div>
                                </div>
                                <div className="form-actions">
                                    <button 
                                        onClick={() => setShowTripStatus(false)}
                                        className="btn btn-secondary"
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GroupTripManager;
