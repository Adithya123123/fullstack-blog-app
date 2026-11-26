import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getPostById, deletePost } from '../services/api';

/**
 * PostDetails Page Component.
 * Full article reading view for readers and author controls (edit/delete).
 */
function PostDetails({ user }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    const fetchPost = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await getPostById(id);
        setPost(response.data);
      } catch (err) {
        setError(err.message || 'Failed to load article details');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchPost();
    }
  }, [id]);

  const handleDelete = async () => {
    const confirmed = window.confirm(`Are you sure you want to permanently delete "${post.title}"?`);
    if (!confirmed) return;

    setDeleting(true);
    try {
      await deletePost(id);
      navigate('/');
    } catch (err) {
      alert(err.message || 'Failed to delete post');
      setDeleting(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const d = new Date(dateString.replace(' ', 'T'));
      return d.toLocaleDateString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return dateString;
    }
  };

  if (loading) {
    return (
      <div className="container main-wrapper">
        <div className="content-narrow">
          <div className="state-box">
            <div className="spinner"></div>
            <p>Loading article...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="container main-wrapper">
        <div className="content-narrow">
          <Link to="/" className="back-link">&larr; Back to all articles</Link>
          <div className="state-box">
            <h3 className="state-title">Article Not Found</h3>
            <p className="state-desc">{error || 'The requested article could not be located.'}</p>
            <Link to="/" className="btn btn-secondary btn-sm">Explore Articles</Link>
          </div>
        </div>
      </div>
    );
  }

  // Check if current user is original author
  const isAuthor = user && user.id === post.userId;
  const authorInitial = post.authorName ? post.authorName.charAt(0).toUpperCase() : 'U';

  return (
    <div className="container main-wrapper">
      <div className="content-narrow">
        <Link to="/" className="back-link">
          &larr; Back to all articles
        </Link>

        <article className="post-detail-container">
          <h1 className="post-detail-title">{post.title}</h1>

          {/* Author metadata and action buttons */}
          <div className="post-detail-header">
            <div className="post-detail-meta">
              <div className="author-avatar-lg">{authorInitial}</div>
              <div className="post-meta-text">
                <span className="post-meta-author">{post.authorName || 'Anonymous'}</span>
                <time className="post-meta-date">
                  Published on {formatDate(post.createdAt)}
                  {post.updatedAt && post.updatedAt !== post.createdAt && ' (Edited)'}
                </time>
              </div>
            </div>

            {/* Author actions */}
            {isAuthor && (
              <div className="card-actions">
                <Link to={`/edit-post/${post.id}`} className="btn btn-secondary btn-sm">
                  Edit Article
                </Link>
                <button
                  onClick={handleDelete}
                  className="btn btn-outline-danger btn-sm"
                  disabled={deleting}
                >
                  {deleting ? 'Deleting...' : 'Delete'}
                </button>
              </div>
            )}
          </div>

          {/* Safe text content rendering: split into paragraphs */}
          <div className="post-detail-content">
            {post.content.split('\n').map((paragraph, index) => (
              paragraph.trim() ? (
                <p key={index} style={{ marginBottom: '1.25em' }}>
                  {paragraph}
                </p>
              ) : (
                <br key={index} />
              )
            ))}
          </div>
        </article>
      </div>
    </div>
  );
}

export default PostDetails;
