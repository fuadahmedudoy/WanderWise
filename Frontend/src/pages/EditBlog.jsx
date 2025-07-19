import React, { useState, useContext, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import { blogApi } from '../api';
import '../styles/create-blog.css'; // Reuse the same styles

const EditBlog = () => {
  const { id } = useParams();
  const { currentUser } = useContext(AuthContext);
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [currentImageUrl, setCurrentImageUrl] = useState('');
  const [tags, setTags] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);

  useEffect(() => {
    const fetchBlogPost = async () => {
      try {
        setFetchLoading(true);
        const response = await blogApi.getBlogPostById(id);
        
        // Check if current user is the author
        if (currentUser && (currentUser.id !== response.userId && currentUser.email !== response.userEmail)) {
          setError('You are not authorized to edit this blog post.');
          return;
        }

        setTitle(response.title || '');
        setContent(response.content || '');
        setCurrentImageUrl(response.imageUrl || '');
        setTags(response.tags ? response.tags.join(', ') : '');
        setIsPublic(response.isPublic !== undefined ? response.isPublic : true);
      } catch (err) {
        console.error('Error fetching blog post:', err);
        setError('Failed to load blog post for editing.');
      } finally {
        setFetchLoading(false);
      }
    };

    if (currentUser) {
      fetchBlogPost();
    } else {
      setError('You must be logged in to edit a blog post.');
      setFetchLoading(false);
    }
  }, [id, currentUser]);

  const handleFileChange = (e) => {
    setImageFile(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setLoading(true);

    if (!title.trim() || !content.trim()) {
      setError('Title and Content are required.');
      setLoading(false);
      return;
    }

    const blogPostData = {
      title: title.trim(),
      content: content.trim(),
      tags: tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0),
      isPublic,
    };

    try {
      await blogApi.updateBlogPost(id, blogPostData, imageFile);
      setSuccessMessage('Blog post updated successfully!');
      setTimeout(() => {
        navigate(`/blog/${id}`);
      }, 2000);
    } catch (err) {
      console.error('Error updating blog post:', err);
      setError(err.response?.data?.message || 'Failed to update blog post. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (fetchLoading) {
    return <div className="create-blog-container">Loading blog post...</div>;
  }

  if (!currentUser) {
    return (
      <div className="auth-dialog-overlay">
        <div className="auth-dialog">
          <h2>Authentication Required</h2>
          <p>You need to login first to edit a blog post.</p>
          <div className="auth-dialog-actions">
            <button onClick={() => navigate('/auth/login')} className="btn-primary">Login</button>
            <button onClick={() => navigate('/')} className="btn-outline">Go Back</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="create-blog-container">
      <header className="create-blog-header">
        <div className="header-content">
          <button onClick={() => navigate(`/blog/${id}`)} className="back-button">
            ← Back to Blog Post
          </button>
          <div className="header-title">
            <h1>Edit Blog Post</h1>
            <p>Update your travel experience</p>
          </div>
        </div>
      </header>

      <div className="create-blog-content">
        <div className="form-card">
          {successMessage && <div className="success-message">{successMessage}</div>}
          {error && <div className="error-message">{error}</div>}

          <form onSubmit={handleSubmit} className="blog-form">
            <div className="form-group">
              <label htmlFor="title">Title</label>
              <input
                type="text"
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g., My unforgettable trip to Sundarbans"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="content">Content</label>
              <textarea
                id="content"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Write your blog post here..."
                rows="10"
                required
              ></textarea>
            </div>

            <div className="form-group">
              <label htmlFor="image">Update Image (optional)</label>
              {currentImageUrl && (
                <div className="current-image">
                  <p>Current image:</p>
                  <img src={currentImageUrl} alt="Current blog" style={{ maxWidth: '200px', maxHeight: '150px', objectFit: 'cover' }} />
                </div>
              )}
              <input
                type="file"
                id="image"
                accept="image/*"
                onChange={handleFileChange}
              />
              <small>Leave empty to keep current image</small>
            </div>

            <div className="form-group">
              <label htmlFor="tags">Tags (comma-separated)</label>
              <input
                type="text"
                id="tags"
                value={tags}
                onChange={(e) => setTags(e.target.value)}
                placeholder="e.g., adventure, nature, Bangladesh"
              />
            </div>

            <div className="form-group checkbox">
              <input
                type="checkbox"
                id="isPublic"
                checked={isPublic}
                onChange={(e) => setIsPublic(e.target.checked)}
              />
              <label htmlFor="isPublic">Make Public</label>
            </div>

            <button 
              type="submit" 
              className="btn-primary" 
              disabled={loading}
            >
              {loading ? 'Updating...' : 'Update Blog Post'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default EditBlog;