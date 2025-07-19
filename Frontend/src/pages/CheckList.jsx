import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { tripApi } from '../api';
import '../styles/checklist.css';

const CheckList = () => {
    const { tripId } = useParams();
    const navigate = useNavigate();
    const [checklist, setChecklist] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [updating, setUpdating] = useState(false);

    useEffect(() => {
        const fetchChecklist = async () => {
            try {
                setLoading(true);
                const data = await tripApi.getCheckList(tripId);
                // Backend returns a HashMap<String, Boolean>
                setChecklist(data || {});
                setError(null);
                console.log("Checklist data received:", data);
            } catch (error) {
                console.error('Error fetching checklist:', error);
                setError('Failed to load checklist. Please try again.');
            } finally {
                setLoading(false);
            }
        };
        
        if (tripId) {
            fetchChecklist();
        }
    }, [tripId]);
    
    const [updatingItem, setUpdatingItem] = useState(null);

    const handleCheckboxChange = async (activity, currentStatus) => {
        try {
            // Optimistically update UI
            setUpdatingItem(activity);
            const newChecklist = { ...checklist };
            newChecklist[activity] = !currentStatus;
            setChecklist(newChecklist);
            
            // Send update to backend
            if (tripApi.updateChecklistItem) {
                await tripApi.updateChecklistItem(tripId, activity, !currentStatus);
                console.log(`Updated "${activity}" to ${!currentStatus}`);
            } else {
                console.warn("API endpoint for updating checklist items not implemented yet");
                // If the backend endpoint isn't available yet, we can still
                // simulate the behavior by keeping the optimistic UI update
            }
        } catch (error) {
            console.error('Error updating checklist item:', error);
            // Revert the optimistic update on error
            const revertedChecklist = { ...checklist };
            revertedChecklist[activity] = currentStatus;
            setChecklist(revertedChecklist);
            setError(`Failed to update "${activity}". Please try again.`);
            // Clear the error after 3 seconds
            setTimeout(() => setError(null), 3000);
        } finally {
            setUpdatingItem(null);
        }
    };

    return (
        <div className="checklist-page">
            <div className="checklist-header">
                <h1>Trip Checklist</h1>
                <div className="checklist-actions">
                    <button 
                        className="btn-outline" 
                        onClick={() => {
                            setLoading(true);
                            tripApi.getCheckList(tripId)
                                .then(data => {
                                    setChecklist(data || {});
                                    setError(null);
                                })
                                .catch(err => {
                                    console.error('Error refreshing checklist:', err);
                                    setError('Failed to refresh checklist');
                                })
                                .finally(() => setLoading(false));
                        }}
                        disabled={loading || updatingItem !== null}
                    >
                        Refresh
                    </button>
                    <button className="btn-primary" onClick={() => navigate(-1)}>
                        Back to Trip
                    </button>
                </div>
            </div>
            
            <div className="checklist-container">
                {loading ? (
                    <div className="checklist-loading">Loading your checklist...</div>
                ) : error ? (
                    <div className="checklist-error">{error}</div>
                ) : Object.keys(checklist).length === 0 ? (
                    <div className="checklist-empty">
                        <p>No items in your checklist for this trip.</p>
                    </div>
                ) : (
                    <ul className="checklist-items">
                        {Object.entries(checklist).map(([activity, completed], index) => (
                            <li key={index} className={`checklist-item ${updatingItem === activity ? 'updating' : ''}`}>
                                <label className="checklist-label">
                                    <input 
                                        type="checkbox" 
                                        className="checklist-checkbox"
                                        checked={completed}
                                        onChange={() => handleCheckboxChange(activity, completed)}
                                        disabled={updatingItem !== null}
                                    />
                                    <span className={`checklist-text ${completed ? 'completed' : ''}`}>
                                        {activity}
                                    </span>
                                    {updatingItem === activity && (
                                        <span className="updating-indicator">Saving...</span>
                                    )}
                                </label>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
}

export default CheckList;