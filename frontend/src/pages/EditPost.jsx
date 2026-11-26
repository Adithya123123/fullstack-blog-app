import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getPostById, updatePost } from '../services/api';

/**
 * EditPost Page Component.
 * Form for updating an existing post. Backend enforces that only the author can save changes.
 */
function EditPost({ user }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [initialLoading, setInitialLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [isAuthor, setIsAuthor] = useState(true);

  // Protect route
  useEffect(() => {
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate]);

  // Load existing post data
  useEffect(() => {
    const loadPost = async () => {
      setInitialLoading(true);
      setError('');
      try {
        const response = await getPostById(id);
        const post = response.data;
        if (!post) {
          setError('Post not found');
          return;
        }

        // Check if current user is author
        if (user && post.userId !== user.id) {
          setIsAuthor(false);
          setError('You are not authorized to edit this post.');
          return;
        }

        setTitle(post.title || '');
        setContent(post.content || '');
      } catch (err) {
        setError(err.message || 'Failed to load post data');
      } finally {
        setInitialLoading(false);
      }
    };

    if (id && user) {
      loadPost();
    }
  }, [id, user]);

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

    setSubmitting(true);
    try {
      await updatePost(id, title.trim(), content.trim());
      navigate(`/posts/${id}`);
    } catch (err) {
      setError(err.message || 'Failed to update post');
    } finally {
      setSubmitting(false);
    }
  };

  if (!user) {
    return null;
  }

  if (initialLoading) {
    return (
      <div className="container main-wrapper">
        <div className="state-box">
          <div className="spinner"></div>
          <p>Loading post for editing...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container main-wrapper">
      <div className="content-narrow">
        <Link to={`/posts/${id}`} className="back-link">
          &larr; Cancel and return to article
        </Link>

        <div className="form-card">
          <div className="page-header" style={{ marginBottom: '24px' }}>
            <h1 className="page-title" style={{ fontSize: '1.75rem' }}>Edit Article</h1>
            <p className="page-subtitle">Make revisions or improvements to your published article.</p>
          </div>

          {error && (
            <div className="alert alert-danger">
              <span>{error}</span>
              <button onClick={() => setError('')} className="alert-dismiss">&times;</button>
            </div>
          )}

          {isAuthor && (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label" htmlFor="title">Article Title</label>
                <input
                  id="title"
                  type="text"
                  className="form-control"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  maxLength={255}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="content">Article Content</label>
                <textarea
                  id="content"
                  className="form-control"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  required
                />
              </div>

              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Saving Changes...' : 'Save Changes'}
                </button>
                <Link to={`/posts/${id}`} className="btn btn-secondary">
                  Cancel
                </Link>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

export default EditPost;
