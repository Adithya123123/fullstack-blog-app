import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { createPost } from '../services/api';

/**
 * CreatePost Page Component.
 * Form for authoring and publishing a new blog post.
 */
function CreatePost({ user }) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  // Protect route
  useEffect(() => {
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!title.trim()) {
      setError('Title cannot be empty');
      return;
    }
    if (!content.trim()) {
      setError('Content cannot be empty');
      return;
    }

    setLoading(true);
    try {
      const response = await createPost(title.trim(), content.trim());
      const newPostId = response.data?.id;
      if (newPostId) {
        navigate(`/posts/${newPostId}`);
      } else {
        navigate('/');
      }
    } catch (err) {
      setError(err.message || 'Failed to create post. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return null;
  }

  return (
    <div className="container main-wrapper">
      <div className="content-narrow">
        <Link to="/" className="back-link">
          &larr; Back to articles
        </Link>

        <div className="form-card">
          <div className="page-header" style={{ marginBottom: '24px' }}>
            <h1 className="page-title" style={{ fontSize: '1.75rem' }}>Create New Article</h1>
            <p className="page-subtitle">Share your knowledge, code walkthrough, or developer notes.</p>
          </div>

          {error && (
            <div className="alert alert-danger">
              <span>{error}</span>
              <button onClick={() => setError('')} className="alert-dismiss">&times;</button>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="title">Article Title</label>
              <input
                id="title"
                type="text"
                className="form-control"
                placeholder="e.g., Understanding JDBC and Connection Management in Java"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                maxLength={255}
                required
              />
              <span className="form-help">{255 - title.length} characters remaining</span>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="content">Article Content</label>
              <textarea
                id="content"
                className="form-control"
                placeholder="Write your article content here. You can use multiple paragraphs..."
                value={content}
                onChange={(e) => setContent(e.target.value)}
                required
              />
            </div>

            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Publishing...' : 'Publish Article'}
              </button>
              <Link to="/" className="btn btn-secondary">
                Cancel
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CreatePost;
