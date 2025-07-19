import React, { useState, useEffect, useContext } from 'react';
import AuthContext from '../context/AuthContext';
import { blogApi } from '../api';
import '../styles/blog-interactions.css';

const BlogInteractions = ({ blogPost }) => {
  const { currentUser } = useContext(AuthContext);
  const [likes, setLikes] = useState({
    count: blogPost.likeCount || 0,
    isLiked: blogPost.isLikedByCurrentUser || false,
    loading: false
  });
  const [comments, setComments] = useState({
    list: [],
    count: blogPost.commentCount || 0,
    loading: false,
    newComment: '',
    submitting: false
  });
  const [showComments, setShowComments] = useState(false);

  useEffect(() => {
    if (showComments) {
      fetchComments();
    }
  }, [showComments, blogPost.id]);

  const handleLike = async () => {
    if (!currentUser) {
      alert('Please login to like posts');
      return;
    }

    setLikes(prev => ({ ...prev, loading: true }));
    try {
      const response = await blogApi.toggleLike(blogPost.id);
      setLikes({
        count: response.likeCount,
        isLiked: response.isLiked,
        loading: false
      });
    } catch (error) {
      console.error('Error toggling like:', error);
      setLikes(prev => ({ ...prev, loading: false }));
      alert('Failed to like/unlike post');
    }
  };

  const fetchComments = async () => {
    setComments(prev => ({ ...prev, loading: true }));
    try {
      const response = await blogApi.getBlogComments(blogPost.id);
      setComments(prev => ({
        ...prev,
        list: response.comments,
        count: response.commentCount,
        loading: false
      }));
    } catch (error) {
      console.error('Error fetching comments:', error);
      setComments(prev => ({ ...prev, loading: false }));
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!currentUser) {
      alert('Please login to comment');
      return;
    }

    if (!comments.newComment.trim()) {
      return;
    }

    setComments(prev => ({ ...prev, submitting: true }));
    try {
      const commentData = {
        content: comments.newComment.trim()
      };
      const newComment = await blogApi.addComment(blogPost.id, commentData);
      setComments(prev => ({
        ...prev,
        list: [newComment, ...prev.list],
        count: prev.count + 1,
        newComment: '',
        submitting: false
      }));
    } catch (error) {
      console.error('Error adding comment:', error);
      setComments(prev => ({ ...prev, submitting: false }));
      alert('Failed to add comment');
    }
  };

  const handleReply = async (parentCommentId, replyContent) => {
    if (!currentUser) {
      alert('Please login to reply');
      return;
    }

    try {
      const commentData = {
        content: replyContent.trim(),
        parentCommentId: parentCommentId
      };
      const reply = await blogApi.addComment(blogPost.id, commentData);
      
      // Update the comments list to include the reply
      setComments(prev => ({
        ...prev,
        list: prev.list.map(comment => {
          if (comment.id === parentCommentId) {
            return {
              ...comment,
              replies: [...(comment.replies || []), reply]
            };
          }
          return comment;
        }),
        count: prev.count + 1
      }));
    } catch (error) {
      console.error('Error adding reply:', error);
      alert('Failed to add reply');
    }
  };

  const formatDateTime = (dateTimeString) => {
    const options = { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric', 
      hour: '2-digit', 
      minute: '2-digit' 
    };
    return new Date(dateTimeString).toLocaleDateString(undefined, options);
  };

  return (
    <div className="blog-interactions">
      {/* Like Section */}
      <div className="interactions-bar">
        <button 
          className={`like-button ${likes.isLiked ? 'liked' : ''}`}
          onClick={handleLike}
          disabled={likes.loading}
        >
          <span className="like-icon">
            {likes.isLiked ? '❤️' : '🤍'}
          </span>
          <span className="like-count">{likes.count}</span>
          <span className="like-text">
            {likes.count === 1 ? 'Like' : 'Likes'}
          </span>
        </button>

        <button 
          className="comment-button"
          onClick={() => setShowComments(!showComments)}
        >
          <span className="comment-icon">💬</span>
          <span className="comment-count">{comments.count}</span>
          <span className="comment-text">
            {comments.count === 1 ? 'Comment' : 'Comments'}
          </span>
        </button>
      </div>

      {/* Comments Section */}
      {showComments && (
        <div className="comments-section">
          {/* Add Comment Form */}
          {currentUser && (
            <form onSubmit={handleAddComment} className="add-comment-form">
              <div className="comment-input-group">
                <img 
                  src="/default-avatar.png" 
                  alt="Your avatar" 
                  className="comment-avatar"
                />
                <textarea
                  value={comments.newComment}
                  onChange={(e) => setComments(prev => ({ ...prev, newComment: e.target.value }))}
                  placeholder="Write a comment..."
                  className="comment-input"
                  rows="3"
                />
              </div>
              <button 
                type="submit" 
                className="submit-comment-btn"
                disabled={comments.submitting || !comments.newComment.trim()}
              >
                {comments.submitting ? 'Posting...' : 'Post Comment'}
              </button>
            </form>
          )}

          {/* Comments List */}
          <div className="comments-list">
            {comments.loading ? (
              <div className="comments-loading">Loading comments...</div>
            ) : comments.list.length > 0 ? (
              comments.list.map(comment => (
                <CommentItem 
                  key={comment.id} 
                  comment={comment} 
                  onReply={handleReply}
                  currentUser={currentUser}
                />
              ))
            ) : (
              <div className="no-comments">
                No comments yet. Be the first to comment!
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const CommentItem = ({ comment, onReply, currentUser }) => {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [submittingReply, setSubmittingReply] = useState(false);

  const formatDateTime = (dateTimeString) => {
    const options = { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric', 
      hour: '2-digit', 
      minute: '2-digit' 
    };
    return new Date(dateTimeString).toLocaleDateString(undefined, options);
  };

  const handleReplySubmit = async (e) => {
    e.preventDefault();
    if (!replyContent.trim()) return;

    setSubmittingReply(true);
    try {
      await onReply(comment.id, replyContent);
      setReplyContent('');
      setShowReplyForm(false);
    } catch (error) {
      console.error('Error submitting reply:', error);
    } finally {
      setSubmittingReply(false);
    }
  };

  return (
    <div className="comment-item">
      <div className="comment-header">
        <img 
          src="/default-avatar.png" 
          alt={`${comment.username}'s avatar`} 
          className="comment-avatar"
        />
        <div className="comment-meta">
          <span className="comment-author">
            {comment.username || comment.userEmail || 'Anonymous'}
          </span>
          <span className="comment-date">
            {formatDateTime(comment.createdAt)}
          </span>
        </div>
      </div>
      
      <div className="comment-content">
        {comment.content}
      </div>

      <div className="comment-actions">
        {currentUser && (
          <button 
            className="reply-button"
            onClick={() => setShowReplyForm(!showReplyForm)}
          >
            Reply
          </button>
        )}
      </div>

      {/* Reply Form */}
      {showReplyForm && currentUser && (
        <form onSubmit={handleReplySubmit} className="reply-form">
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder={`Reply to ${comment.username || 'this comment'}...`}
            className="reply-input"
            rows="2"
          />
          <div className="reply-form-actions">
            <button 
              type="button" 
              onClick={() => setShowReplyForm(false)}
              className="cancel-reply-btn"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="submit-reply-btn"
              disabled={submittingReply || !replyContent.trim()}
            >
              {submittingReply ? 'Posting...' : 'Reply'}
            </button>
          </div>
        </form>
      )}

      {/* Replies */}
      {comment.replies && comment.replies.length > 0 && (
        <div className="replies-list">
          {comment.replies.map(reply => (
            <div key={reply.id} className="reply-item">
              <div className="comment-header">
                <img 
                  src="/default-avatar.png" 
                  alt={`${reply.username}'s avatar`} 
                  className="comment-avatar small"
                />
                <div className="comment-meta">
                  <span className="comment-author">
                    {reply.username || reply.userEmail || 'Anonymous'}
                  </span>
                  <span className="comment-date">
                    {formatDateTime(reply.createdAt)}
                  </span>
                </div>
              </div>
              <div className="comment-content">
                {reply.content}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default BlogInteractions;