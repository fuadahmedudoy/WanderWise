import React, { useState } from 'react';
import GroupTripManager from '../components/GroupTripManager';
import GroupTripBrowser from '../components/GroupTripBrowser';
import './GroupTrips.css';

const GroupTrips = () => {
    const [activeTab, setActiveTab] = useState('browse'); // 'browse' or 'manage'

    return (
        <div className="group-trips-page">
            <div className="group-trips-header">
                <h1>Group Trips</h1>
                <p>Connect with fellow travelers and explore the world together</p>
            </div>

            <div className="group-trips-nav">
                <button 
                    className={`nav-tab ${activeTab === 'browse' ? 'active' : ''}`}
                    onClick={() => setActiveTab('browse')}
                >
                    <span className="tab-icon">🔍</span>
                    Browse Trips
                </button>
                <button 
                    className={`nav-tab ${activeTab === 'manage' ? 'active' : ''}`}
                    onClick={() => setActiveTab('manage')}
                >
                    <span className="tab-icon">⚙️</span>
                    My Trips
                </button>
            </div>

            <div className="group-trips-content">
                {activeTab === 'browse' && <GroupTripBrowser />}
                {activeTab === 'manage' && <GroupTripManager />}
            </div>
        </div>
    );
};

export default GroupTrips;
