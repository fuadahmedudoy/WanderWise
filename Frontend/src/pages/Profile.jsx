// Frontend/src/pages/Profile.jsx

import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import api from '../api'; // <-- Import our new api instance
import '../styles/Profile.css';

const Profile = () => {
    const { currentUser, logout } = useContext(AuthContext);
    const navigate = useNavigate();
    const [profile, setProfile] = useState({
        firstName: '',
        lastName: '',
        bio: '',
        profilePictureUrl: ''
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    // Fetch profile data when the component loads
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                // The interceptor handles the auth header automatically
                const response = await api.get('/api/profile');
                setProfile({
                    firstName: response.data.firstName || '',
                    lastName: response.data.lastName || '',
                    bio: response.data.bio || '',
                    profilePictureUrl: response.data.profilePictureUrl || ''
                });
            } catch (err) {
                setError('Failed to fetch profile data.');
                console.error("Fetch profile error:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProfile(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    // Handle form submission to update the profile
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccessMessage('');
        
        try {
            // The interceptor handles the auth header automatically
            const response = await api.put('/api/profile', profile);
            setProfile(response.data);
            setSuccessMessage('Profile updated successfully!');
        } catch (err) {
            console.error("Profile update error:", err.response || err);
            setError('Failed to update profile. Please try again.');
        }
    };

    const handleLogout = async () => {
        await logout();
        navigate('/auth/login');
    };

    if (loading) {
        return <div className="loading-container">Loading profile...</div>;
    }

    return (
        <div className="profile-page">
            <header className="page-header">
                <div className="logo" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
                    WanderWise
                </div>
                <div className="nav-buttons">
                    <button onClick={() => navigate('/my-trips')} className="btn-outline">My Trips</button>
                    <button onClick={handleLogout} className="btn-primary">Logout</button>
                </div>
            </header>
            <div className="profile-container">
                <div className="profile-card">
                    <h1>Edit Your Profile</h1>
                    <p>Welcome, {currentUser?.username || 'User'}</p>

                    {error && <div className="error-message">{error}</div>}
                    {successMessage && <div className="success-message">{successMessage}</div>}

                    <form onSubmit={handleSubmit} className="profile-form">
                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="firstName">First Name</label>
                                <input
                                    type="text"
                                    id="firstName"
                                    name="firstName"
                                    value={profile.firstName}
                                    onChange={handleChange}
                                    placeholder="Your first name"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="lastName">Last Name</label>
                                <input
                                    type="text"
                                    id="lastName"
                                    name="lastName"
                                    value={profile.lastName}
                                    onChange={handleChange}
                                    placeholder="Your last name"
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="bio">Bio</label>
                            <textarea
                                id="bio"
                                name="bio"
                                value={profile.bio}
                                onChange={handleChange}
                                rows="4"
                                placeholder="Tell us a little about yourself"
                            ></textarea>
                        </div>
                        
                        <div className="form-group">
                            <label htmlFor="profilePictureUrl">Profile Picture URL</label>
                            <input
                                type="text"
                                id="profilePictureUrl"
                                name="profilePictureUrl"
                                value={profile.profilePictureUrl}
                                onChange={handleChange}
                                placeholder="http://example.com/image.png"
                            />
                        </div>

                        <button type="submit" className="btn-primary">Save Changes</button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Profile;