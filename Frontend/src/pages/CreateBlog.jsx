import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import { blogApi } from '../api';
import '../styles/create-blog.css'; // Assuming a new CSS file for blog styles

const CreateBlog = () => {
  const { currentUser } = useContext(AuthContext);
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [tags, setTags] = useState(''); // Comma-separated string
  const [isPublic, setIsPublic] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [loading, setLoading] = useState(false);

  // Redirect if not logged in
  if (!currentUser) {
    return (
      <div className="auth-dialog-overlay">
        <div className="auth-dialog">
          <h2>Authentication Required</h2>
          <p>You need to login first to write a blog post.</p>
          <div className="auth-dialog-actions">
            <button onClick={() => navigate('/auth/login')} className="btn-primary">Login</button>
            <button onClick={() => navigate('/')} className="btn-outline">Go Back</button>
          </div>
        </div>
      </div>
    );
  }

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
      title,
      content,
      tags: tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0),
      isPublic,
    };

    try {
      await blogApi.createBlogPost(blogPostData, imageFile);
      setSuccessMessage('Blog post created successfully!');
      console.log(" blog succesfully created");
      // Clear form
      setTitle('');
      setContent('');
      setImageFile(null);
      setTags('');
      setIsPublic(true);
      // Optionally redirect to the homepage or the new blog post
      setTimeout(() => {
        navigate('/'); 
      }, 2000);
    } catch (err) {
      console.error('Error creating blog post:', err);
      setError(err.response?.data?.message || 'Failed to create blog post. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-blog-container">
      <header className="create-blog-header">
        <div className="header-content">
          <button onClick={() => navigate('/')} className="back-button">
            ← Back to Home
          </button>
          <div className="header-title">
            <h1>Write a New Blog Post</h1>
            <p>Share your travel experiences with the community</p>
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
              <label htmlFor="image">Upload Image </label>
              <input
                type="file"
                id="image"
                accept="image/*"
                onChange={handleFileChange}
              />
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
              {loading ? 'Publishing...' : 'Publish Blog Post'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateBlog;