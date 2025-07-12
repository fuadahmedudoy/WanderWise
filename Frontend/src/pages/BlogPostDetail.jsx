import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { blogApi } from '../api';
import '../styles/blog-post-detail.css'; // Assuming a new CSS file for detailed blog view

const BlogPostDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [blogPost, setBlogPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
        <button onClick={() => navigate('/')} className="back-button">
          ← Back to Home
        </button>
        <h1>{blogPost.title}</h1>
        <p className="blog-meta">
          By {blogPost.user?.username || blogPost.user?.email || 'Anonymous'} on {formatDateTime(blogPost.createdAt)}
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