import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import api, { blogApi } from '../api';
import { FaStar } from 'react-icons/fa';
import NotificationCenter from '../components/NotificationCenter';
import '../styles/home.css';

const Home = () => {
  const { currentUser, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [featuredDestinations, setFeaturedDestinations] = useState([]);
  const [blogPosts, setBlogPosts] = useState([]); 
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingBlogs, setIsLoadingBlogs] = useState(true); 
  const [error, setError] = useState(null);
  const [blogError, setBlogError] = useState(null); 
  
  const effectiveUser = currentUser || (localStorage.getItem('currentUser') ? JSON.parse(localStorage.getItem('currentUser')) : null);

  useEffect(() => {
    const fetchFeaturedDestinations = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const response = await api.get('/api/destinations/featured');
        setFeaturedDestinations(response.data || []); // Ensure it's always an array
        setIsLoading(false);
      } catch (err) {
        console.error('Error fetching featured destinations:', err);
        setError('Failed to load featured destinations. Please try again later.');
        setFeaturedDestinations([]); // Ensure it's an empty array on error
        setIsLoading(false);
      }
    };

    const fetchBlogPosts = async () => {
      try {
        setIsLoadingBlogs(true);
        setBlogError(null);
        const response = await blogApi.getAllBlogPosts();
        setBlogPosts(Array.isArray(response) ? response : []); // Ensure it's always an array
        console.log(`Fetched ${response ? response.length : 0} blog posts`);
        setIsLoadingBlogs(false);
      } catch (err) {
        console.error('Error fetching blog posts:', err);
        setBlogError('Failed to load blog posts. Please try again later.');
        setBlogPosts([]); // Ensure it's an empty array on error
        setIsLoadingBlogs(false);
      }
    };

    fetchFeaturedDestinations();
    fetchBlogPosts(); // Fetch blog posts
  }, []);

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/auth/login');
    } catch (error) {
      console.error('Failed to log out', error);
    }
  };

  const handleLogin = () => {
    navigate('/auth/login');
  };

  const navigateToCreateTrip = () => {
    navigate('/create-trip');
  };
  
  const navigateToMyTrips = () => {
    navigate('/my-trips');
  };
  
  const navigateToDestination = (id) => {
    navigate(`/destination/${id}`);
  };

  const navigateToCreateBlog = () => {
    navigate('/create-blog');
  };

  const navigateToBlogPost = (id) => {
    navigate(`/blog/${id}`);
  };

  const formatDateTime = (dateTimeString) => {
    const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(dateTimeString).toLocaleDateString(undefined, options);
  };

  return (
    <div className="home-container">
      <nav className="navbar">
        <div className="logo">WanderWise</div>
        {effectiveUser ? (
          <div className="action-buttons">
            <NotificationCenter />
            {effectiveUser.role === 'ADMIN' && (
              <button onClick={() => navigate('/admin')} className="btn-outline admin-btn">Admin Dashboard</button>
            )}
            <button onClick={navigateToCreateBlog} className="btn-outline">Write Blog</button>
            <button onClick={() => navigate('/my-trips')} className="btn-outline nav-btn">My Trips</button>
            <button onClick={() => navigate('/group-trips')} className="btn-outline nav-btn">Group Trips</button>
            <button onClick={() => navigate('/profile')} className="btn-outline">Profile</button>
            <button onClick={handleLogout} className="btn-outline">Logout</button>
          </div>
        ) : (
          <div className="nav-buttons">
            <button onClick={handleLogin} className="btn-outline">Login</button>
          </div>
        )}
      </nav>
      
      <div className="welcome-section">
        {effectiveUser ? (
          <h1>Welcome, {effectiveUser.username || effectiveUser.email}!</h1>
        ) : (
          <h1>Welcome to WanderWise!</h1>
        )}
        <p>Discover amazing destinations and plan your perfect trip.</p>
        <div className="action-buttons">
          <button className="btn-primary" onClick={navigateToCreateTrip}>Create New Trip</button>
          <button className="btn-secondary" onClick={navigateToMyTrips}>View My Trips</button>
          <button className="btn-secondary" onClick={() => navigate('/group-trips')}>Browse Group Trips</button>
        </div>
      </div>
      
      <div className="featured-section">
        <h2>Featured Destinations</h2>
        
        {isLoading && (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading featured destinations...</p>
          </div>
        )}
        
        {error && (
          <div className="error-message">
            {error}
          </div>
        )}
        
        {!isLoading && !error && (
          <div className="destination-cards">
            {featuredDestinations.length > 0 ? (
              featuredDestinations.map(destination => (
                <div className="destination-card" key={destination.id} onClick={() => navigateToDestination(destination.id)}>
                  <div 
                    className="card-image" 
                    style={{ backgroundImage: `url(${destination.imageUrl})` }}
                  ></div>
                  <div className="card-content">
                    <h3>{destination.title}</h3>
                    <p className="destination-location">{destination.destination}</p>
                    <div className="destination-meta">
                      <span className="destination-days">{destination.days} days</span>
                      <div className="destination-rating">
                        <FaStar className="star-icon" />
                        <span>{destination.avgRating.toFixed(1)}</span>
                      </div>
                    </div>
                    <p className="destination-description">{destination.description.substring(0, 100)}...</p>
                    <button 
                      className="btn-outline view-details" 
                      onClick={(e) => { e.stopPropagation(); navigateToDestination(destination.id); }}
                    >
                      View Details
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <div className="no-trips-message">
                <p>No featured destinations available at the moment.</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Blog Posts Section */}
      <div className="blog-section">
        <h2>Recent Blog Posts</h2>
        
        {isLoadingBlogs && (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading blog posts...</p>
          </div>
        )}
        
        {blogError && (
          <div className="error-message">
            {blogError}
          </div>
        )}
        
        {!isLoadingBlogs && !blogError && (
          <div className="blog-cards">
            {blogPosts && Array.isArray(blogPosts) && blogPosts.length > 0 ? (
              blogPosts.map(post => (
                <div className="blog-card" key={post.id} onClick={() => navigateToBlogPost(post.id)}>
                  {post.imageUrl && (
                    <div className="blog-image-wrapper">
                      <img src={post.imageUrl} alt={post.title} className="blog-card-image" />
                    </div>
                  )}
                  <div className="blog-card-content">
                    <h3>{post.title}</h3>
                    <p className="blog-author-date">
                      By {post.username || post.userEmail || 'Anonymous'} on {formatDateTime(post.createdAt)}
                    </p>
                    <p className="blog-snippet">{post.content.substring(0, 150)}...</p>
                    {post.tags && post.tags.length > 0 && (
                      <div className="blog-tags">
                        {post.tags.map((tag, idx) => (
                          <span key={idx} className="blog-tag">{tag}</span>
                        ))}
                      </div>
                    )}
                    
                    {/* Add interaction stats */}
                    <div className="blog-stats">
                      <span className="stat-item">
                        <span className="stat-icon">❤️</span>
                        <span className="stat-count">{post.likeCount || 0}</span>
                      </span>
                      <span className="stat-item">
                        <span className="stat-icon">💬</span>
                        <span className="stat-count">{post.commentCount || 0}</span>
                      </span>
                    </div>

                    <div className="blog-card-actions">
                      <button 
                        className="btn-outline view-details" 
                        onClick={(e) => { e.stopPropagation(); navigateToBlogPost(post.id); }}
                      >
                        Read More
                      </button>
                      {effectiveUser && (effectiveUser.id === post.userId || effectiveUser.email === post.userEmail) && (
                        <div className="author-actions-small">
                          <button 
                            className="btn-outline edit-btn-small"
                            onClick={(e) => { e.stopPropagation(); navigate(`/blog/edit/${post.id}`); }}
                          >
                            Edit
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="no-trips-message">
                <p>No blog posts available yet. Be the first to share your travel story!</p>
                {effectiveUser && (
                  <button className="btn-primary" onClick={navigateToCreateBlog}>Write Your First Blog</button>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Home;