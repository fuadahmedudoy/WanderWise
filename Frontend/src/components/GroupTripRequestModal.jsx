import React, { useState } from 'react';
import './GroupTripRequestModal.css';

const GroupTripRequestModal = ({ isOpen, onClose, tripPlan, onSubmit }) => {
    const [formData, setFormData] = useState({
        groupName: '',
        description: '',
        maxPeople: 5,
        meetingPoint: '',
        additionalRequirements: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.groupName.trim() || !formData.description.trim()) {
            alert('Please fill in the required fields: Group Name and Description');
            return;
        }

        setIsSubmitting(true);
        try {
            await onSubmit({
                ...formData,
                tripPlan: tripPlan,
                maxPeople: parseInt(formData.maxPeople)
            });
            
            // Reset form
            setFormData({
                groupName: '',
                description: '',
                maxPeople: 5,
                meetingPoint: '',
                additionalRequirements: ''
            });
            
            onClose();
        } catch (error) {
            console.error('Error creating group trip:', error);
            console.error('Response data:', error.response?.data);
            console.error('Response status:', error.response?.status);
            
            // Show the specific error message from the backend
            const errorMessage = error.response?.data?.error || 
                                error.response?.data?.message || 
                                'Failed to create group trip. Please try again.';
            alert(errorMessage);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="group-trip-modal-overlay" onClick={onClose}>
            <div className="group-trip-modal" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Create Group Trip</h2>
                    <button className="close-button" onClick={onClose}>✕</button>
                </div>

                <form onSubmit={handleSubmit} className="group-trip-form">
                    <div className="form-group">
                        <label htmlFor="groupName">Group Name *</label>
                        <input
                            type="text"
                            id="groupName"
                            name="groupName"
                            value={formData.groupName}
                            onChange={handleInputChange}
                            placeholder="e.g., Adventure Seekers to Sylhet"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="description">Description *</label>
                        <textarea
                            id="description"
                            name="description"
                            value={formData.description}
                            onChange={handleInputChange}
                            placeholder="Tell others about your trip plans, what you're looking for in travel companions..."
                            rows={4}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="maxPeople">Maximum People</label>
                        <select
                            id="maxPeople"
                            name="maxPeople"
                            value={formData.maxPeople}
                            onChange={handleInputChange}
                        >
                            {[2, 3, 4, 5, 6, 7, 8, 10, 12, 15].map(num => (
                                <option key={num} value={num}>{num} people</option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="meetingPoint">Meeting Point (Optional)</label>
                        <input
                            type="text"
                            id="meetingPoint"
                            name="meetingPoint"
                            value={formData.meetingPoint}
                            onChange={handleInputChange}
                            placeholder="e.g., Dhaka Airport, Kamalapur Railway Station"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="additionalRequirements">Additional Requirements (Optional)</label>
                        <textarea
                            id="additionalRequirements"
                            name="additionalRequirements"
                            value={formData.additionalRequirements}
                            onChange={handleInputChange}
                            placeholder="Any specific requirements, preferences, or conditions for joining the group..."
                            rows={3}
                        />
                    </div>

                    <div className="trip-summary">
                        <h3>Trip Summary</h3>
                        <div className="summary-details">
                            <p><strong>Destination:</strong> {
                                tripPlan?.trip_summary?.destination || 
                                tripPlan?.destination || 
                                'Not specified'
                            }</p>
                            <p><strong>Duration:</strong> {
                                tripPlan?.trip_summary?.duration || 
                                tripPlan?.duration_days || 
                                tripPlan?.duration || 
                                'Not specified'
                            } days</p>
                            <p><strong>Start Date:</strong> {
                                tripPlan?.trip_summary?.start_date || 
                                tripPlan?.start_date || 
                                'Not specified'
                            }</p>
                            {(tripPlan?.trip_summary?.total_budget || tripPlan?.budget) && (
                                <p><strong>Estimated Budget:</strong> ৳{(
                                    tripPlan?.trip_summary?.total_budget || 
                                    tripPlan?.budget
                                ).toLocaleString()}</p>
                            )}
                        </div>
                    </div>

                    <div className="modal-actions">
                        <button type="button" className="cancel-btn" onClick={onClose}>
                            Cancel
                        </button>
                        <button type="submit" className="create-btn" disabled={isSubmitting}>
                            {isSubmitting ? 'Creating...' : 'Create Group Trip'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default GroupTripRequestModal;