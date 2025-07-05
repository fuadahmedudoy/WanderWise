import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import api from '../api';
import NotificationCenter from '../components/NotificationCenter';
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
    const [isEditing, setIsEditing] = useState(false);
    const [selectedFile, setSelectedFile] = useState(null);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
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

    const handleFileChange = (e) => {
        setSelectedFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccessMessage('');

        const formData = new FormData();
        formData.append('profile', new Blob([JSON.stringify(profile)], { type: 'application/json' }));
        if (selectedFile) {
            formData.append('file', selectedFile);
        }

        try {
            const response = await api.put('/api/profile', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            setProfile(response.data);
            setSuccessMessage('Profile updated successfully!');
            setIsEditing(false);
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
                <div className="profilepage_logo" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
                    WanderWise
                </div>
                <div className="nav-buttons">
                    <NotificationCenter />
                    <button onClick={() => navigate('/my-trips')} className="btn-outline">My Trips</button>
                    <button onClick={handleLogout} className="btn-primary">Logout</button>
                </div>
            </header>
            <div className="profile-container">
                <div className="profile-card">
                    <h1>{isEditing ? 'Edit Your Profile' : 'Your Profile'}</h1>
                    <p>Welcome, {currentUser?.username || 'User'}</p>

                    {error && <div className="error-message">{error}</div>}
                    {successMessage && <div className="success-message">{successMessage}</div>}

                    {isEditing ? (
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
                                <label htmlFor="profilePicture">Profile Picture</label>
                                <input
                                    type="file"
                                    id="profilePicture"
                                    name="profilePicture"
                                    onChange={handleFileChange}
                                />
                            </div>
                            <div className="form-actions">
                                <button type="button" onClick={() => setIsEditing(false)} className="btn-outline">Cancel</button>
                                <button type="submit" className="btn-primary">Save Changes</button>
                            </div>
                        </form>
                    ) : (
                        <div className="profile-view">
                            <div className="profile-picture">
                                <img src={profile.profilePictureUrl || 'https://via.placeholder.com/150'} alt="Profile" />
                            </div>
                            <div className="profile-info">
                                <h2>{profile.firstName} {profile.lastName}</h2>
                                <p>{currentUser?.email}</p>
                                <p className="bio">{profile.bio || 'No bio yet.'}</p>
                            </div>
                            <button onClick={() => setIsEditing(true)} className="btn-primary">Edit Profile</button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Profile;