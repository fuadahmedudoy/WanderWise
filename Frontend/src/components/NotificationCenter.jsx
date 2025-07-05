import React, { useState, useEffect, useContext, useRef } from 'react';
import { FaBell, FaTimes, FaExclamationTriangle, FaInfoCircle } from 'react-icons/fa';
import AuthContext from '../context/AuthContext';
import notificationService from '../services/notificationService';
import api from '../api';
import './NotificationCenter.css';
import { useNavigate } from 'react-router-dom';
import ReactDOM from 'react-dom';

const NotificationCenter = () => {
    const { currentUser } = useContext(AuthContext);
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [isOpen, setIsOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();
    const dropdownRef = useRef(null);

    const effectiveUser = currentUser || (localStorage.getItem('currentUser') ? JSON.parse(localStorage.getItem('currentUser')) : null);

    useEffect(() => {
        if (effectiveUser) {
            // Load existing notifications
            loadNotifications();

            // Try to connect to WebSocket, but don't fail if it's not available
            try {
                notificationService.connect(effectiveUser.id, handleNewNotification);
            } catch (error) {
                console.warn('WebSocket connection failed, will work without real-time updates:', error);
            }

            return () => {
                try {
                    notificationService.disconnect();
                } catch (error) {
                    console.warn('Error disconnecting WebSocket:', error);
                }
            };
        }
    }, [effectiveUser]);

    const handleNewNotification = (notification) => {
        setNotifications(prev => [notification, ...prev]);
        setUnreadCount(prev => prev + 1);
        
        // Show browser notification if permission granted
        if (Notification.permission === 'granted') {
            new Notification('WanderWise Alert', {
                body: notification.message,
                icon: '/favicon.ico'
            });
        }
    };

    const loadNotifications = async () => {
        if (!effectiveUser) return;
        
        try {
            setIsLoading(true);
            const response = await api.get('/api/notifications');
            setNotifications(response.data.notifications);
            setUnreadCount(response.data.unreadCount);
        } catch (error) {
            console.error('Failed to load notifications:', error);
            // Add some mock data for testing if API fails
            const mockNotifications = [
                {
                    id: 1,
                    type: 'WEATHER_ALERT',
                    message: 'Adverse weather conditions detected for your upcoming trip to Sylhet. Weather alert detected for your trip to Sylhet. Please pack appropriate gear and monitor weather updates.',
                    isRead: false,
                    createdAt: new Date().toISOString(),
                    tripId: 1
                },
                {
                    id: 2,
                    type: 'WEATHER_ALERT',
                    message: 'Adverse weather conditions detected for your upcoming trip to Sylhet. Weather alert detected for your trip to Sylhet. Please pack appropriate gear and monitor weather updates.',
                    isRead: false,
                    createdAt: new Date(Date.now() - 3600000).toISOString(),
                    tripId: 1
                }
            ];
            setNotifications(mockNotifications);
            setUnreadCount(2);
        } finally {
            setIsLoading(false);
        }
    };

    const markAsRead = async (notificationId) => {
        console.log('Marking notification as read:', notificationId);
        try {
            await api.put(`/api/notifications/${notificationId}/read`);
            setNotifications(prev => 
                prev.map(n => 
                    n.id === notificationId ? { ...n, isRead: true } : n
                )
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
            console.log('Successfully marked as read');
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
            // Still update the UI even if API fails
            setNotifications(prev => 
                prev.map(n => 
                    n.id === notificationId ? { ...n, isRead: true } : n
                )
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
            console.log('Updated UI locally (API failed)');
        }
    };

    const deleteNotification = async (notificationId) => {
        console.log('Deleting notification:', notificationId);
        try {
            const notificationToDelete = notifications.find(n => n.id === notificationId);
            await api.delete(`/api/notifications/${notificationId}`);
            setNotifications(prev => prev.filter(n => n.id !== notificationId));
            
            // Update unread count if the deleted notification was unread
            if (notificationToDelete && !notificationToDelete.isRead) {
                setUnreadCount(prev => Math.max(0, prev - 1));
            }
            console.log('Successfully deleted notification');
        } catch (error) {
            console.error('Failed to delete notification:', error);
            // Still update the UI even if API fails
            const notificationToDelete = notifications.find(n => n.id === notificationId);
            setNotifications(prev => prev.filter(n => n.id !== notificationId));
            
            // Update unread count if the deleted notification was unread
            if (notificationToDelete && !notificationToDelete.isRead) {
                setUnreadCount(prev => Math.max(0, prev - 1));
            }
            console.log('Updated UI locally (API failed)');
        }
    };

    const markAllAsRead = async () => {
        console.log('Marking all notifications as read');
        try {
            await api.post('/api/notifications/mark-all-read');
            setNotifications(prev => 
                prev.map(n => ({ ...n, isRead: true }))
            );
            setUnreadCount(0);
            console.log('Successfully marked all as read');
        } catch (error) {
            console.error('Failed to mark all notifications as read:', error);
            // Still update the UI even if API fails
            setNotifications(prev => 
                prev.map(n => ({ ...n, isRead: true }))
            );
            setUnreadCount(0);
            console.log('Updated UI locally (API failed)');
        }
    };

    const clearAllNotifications = async () => {
        console.log('Clearing all notifications');
        try {
            await api.delete(`/api/notifications/user/${effectiveUser.id}/all`);
            setNotifications([]);
            setUnreadCount(0);
            console.log('Successfully cleared all notifications');
        } catch (error) {
            console.error('Failed to clear notifications:', error);
            // Still update the UI even if API fails
            setNotifications([]);
            setUnreadCount(0);
            console.log('Updated UI locally (API failed)');
        }
    };

    const getNotificationIcon = (type) => {
        switch (type) {
            case 'WEATHER_ALERT':
                return <FaExclamationTriangle className="notification-icon weather-alert" />;
            default:
                return <FaInfoCircle className="notification-icon info" />;
        }
    };

    const formatTimeAgo = (timestamp) => {
        const now = new Date();
        const time = new Date(timestamp);
        const diffMs = now - time;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMins / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins}m ago`;
        if (diffHours < 24) return `${diffHours}h ago`;
        return `${diffDays}d ago`;
    };

    // Request notification permission on component mount
    useEffect(() => {
        if (Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }, []);

    const handleNotificationClick = async (notification) => {
        if (notification.type === 'WEATHER_ALERT') {
            await markAsRead(notification.id);
            
            // Get tripId from notification
            const tripId = notification.tripId;
            if (tripId) {
                setIsOpen(false);
                navigate(`/weather-details/${tripId}`);
            } else {
                console.log('No tripId found in notification');
            }
        }
    };

    // Click outside to close
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    if (!effectiveUser) return null;

    return (
        <div className="notification-center">
            <button 
                className="notification-bell"
                onClick={(e) => {
                    e.stopPropagation();
                    setIsOpen(!isOpen);
                }}
            >
                <FaBell />
                {unreadCount > 0 && (
                    <span className="notification-badge">{unreadCount}</span>
                )}
            </button>
            
            {isOpen && ReactDOM.createPortal(
                <div className="notification-dropdown" ref={dropdownRef}>
                    <div className="notification-header">
                        <h3>Notifications</h3>
                        <div className="notification-actions">
                            {notifications.length > 0 && unreadCount > 0 && (
                                <button 
                                    className="mark-all-read-btn"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        markAllAsRead();
                                    }}
                                >
                                    Mark all read
                                </button>
                            )}
                            {notifications.length > 0 && (
                                <button 
                                    className="clear-all-btn"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        clearAllNotifications();
                                    }}
                                >
                                    Clear all
                                </button>
                            )}
                        </div>
                    </div>
                    <div className="notification-list">
                        {isLoading ? (
                            <div className="notification-loading">Loading notifications...</div>
                        ) : notifications.length === 0 ? (
                            <div className="no-notifications">
                                <div className="empty-icon">🔔</div>
                                <p>No notifications yet</p>
                            </div>
                        ) : (
                            notifications.map(notification => (
                                <div 
                                    key={notification.id} 
                                    className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
                                    onClick={() => handleNotificationClick(notification)}
                                    style={{ cursor: 'pointer' }}
                                >
                                    <div className="notification-content">
                                        <div className="notification-main">
                                            {getNotificationIcon(notification.type)}
                                            <div className="notification-text">
                                                <p className="notification-message">{notification.message}</p>
                                                <span className="notification-time">
                                                    {formatTimeAgo(notification.createdAt)}
                                                </span>
                                            </div>
                                        </div>
                                        <div className="notification-buttons">
                                            {!notification.isRead && (
                                                <button 
                                                    className="mark-read-btn"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        markAsRead(notification.id);
                                                    }}
                                                    title="Mark as read"
                                                >
                                                    ✓
                                                </button>
                                            )}
                                            <button 
                                                className="delete-btn"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    deleteNotification(notification.id);
                                                }}
                                                title="Delete notification"
                                            >
                                                ×
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>,
                document.getElementById('notification-root')
            )}
        </div>
    );
};

export default NotificationCenter; 