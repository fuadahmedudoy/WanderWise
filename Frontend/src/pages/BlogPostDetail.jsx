import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { blogApi } from '../api';
import AuthContext from '../context/AuthContext';
import '../styles/blog-post-detail.css'; 

const BlogPostDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useContext(AuthContext);
  const [blogPost, setBlogPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  useEffect(() => {
    const fetchBlogPost = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await blogApi.getBlogPostById(id);
        setBlogPost(response);
      } catch (err) {
        console.error('Error fetching blog post:', err);
        setError('Failed to load blog post. It might not exist or there was a network error.');
      } finally {
        setLoading(false);
      }
    };

    fetchBlogPost();
  }, [id]);

  const formatDateTime = (dateTimeString) => {
    const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(dateTimeString).toLocaleDateString(undefined, options);
  };

  const handleEdit = () => {
    navigate(`/blog/edit/${id}`);
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this blog post? This action cannot be undone.')) {
      return;
    }

    try {
      setDeleteLoading(true);
      await blogApi.deleteBlogPost(id);
      alert('Blog post deleted successfully!');
      navigate('/');
    } catch (err) {
      console.error('Error deleting blog post:', err);
      alert('Failed to delete blog post. Please try again.');
    } finally {
      setDeleteLoading(false);
    }
  };

  // Check if current user is the author
  const isAuthor = currentUser && blogPost && (
    currentUser.id === blogPost.userId || 
    currentUser.email === blogPost.userEmail
  );

  if (loading) {
    return <div className="blog-detail-container">Loading blog post...</div>;
  }

  if (error) {
    return (
      <div className="blog-detail-container">
        <div className="blog-detail-card error-card">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={() => navigate('/')} className="btn-primary">
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  if (!blogPost) {
    return (
      <div className="blog-detail-container">
        <div className="blog-detail-card not-found-card">
          <h2>Blog Post Not Found</h2>
          <p>The blog post you are looking for does not exist.</p>
          <button onClick={() => navigate('/')} className="btn-primary">
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="blog-detail-container">
      <header className="blog-detail-header">
        <div className="header-top">
          <button onClick={() => navigate('/')} className="back-button">
            ← Back to Home
          </button>
          {isAuthor && (
            <div className="author-actions">
              <button onClick={handleEdit} className="btn-outline edit-btn">
                ✏️ Edit
              </button>
              <button 
                onClick={handleDelete} 
                className="btn-outline delete-btn"
                disabled={deleteLoading}
              >
                {deleteLoading ? '🗑️ Deleting...' : '🗑️ Delete'}
              </button>
            </div>
          )}
        </div>
        <h1>{blogPost.title}</h1>
        <p className="blog-meta">
          By {blogPost.username || blogPost.userEmail || 'Anonymous'} on {formatDateTime(blogPost.createdAt)}
        </p>
        {blogPost.tags && blogPost.tags.length > 0 && (
          <div className="blog-tags-detail">
            {blogPost.tags.map((tag, idx) => (
              <span key={idx} className="blog-tag-detail">{tag}</span>
            ))}
          </div>
        )}
      </header>

      <div className="blog-detail-content">
        {blogPost.imageUrl && (
          <img src={blogPost.imageUrl} alt={blogPost.title} className="blog-featured-image" />
        )}
        <div className="blog-content">
          <p>{blogPost.content}</p>
        </div>
      </div>
    </div>
  );
};

export default BlogPostDetail;