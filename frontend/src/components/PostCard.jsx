import React from 'react';
import { Link } from 'react-router-dom';

/**
 * PostCard Component.
 * Displays a single blog post preview card with metadata, snippet, and optional action buttons.
 */
function PostCard({ post, showActions = false, onDelete }) {
  // Format date helper: "Nov 26, 2026"
  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const d = new Date(dateString.replace(' ', 'T'));
      return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return dateString;
    }
  };

  const authorInitial = post.authorName ? post.authorName.charAt(0).toUpperCase() : 'U';

  const handleDelete = () => {
    const confirmed = window.confirm(`Are you sure you want to delete "${post.title}"?`);
    if (confirmed && onDelete) {
      onDelete(post.id);
    }
  };

  return (
    <article className="post-card">
      <div className="post-card-meta">
        <span className="post-card-author">
          <span className="author-avatar">{authorInitial}</span>
          {post.authorName || 'Anonymous'}
        </span>
        <time className="post-card-date">{formatDate(post.createdAt)}</time>
      </div>

      <Link to={`/posts/${post.id}`} className="post-card-title">
        {post.title}
      </Link>

      <p className="post-card-snippet">{post.content}</p>

      <div className="post-card-footer">
        <Link to={`/posts/${post.id}`} className="read-more-link">
          Read article &rarr;
        </Link>

        {showActions && (
          <div className="card-actions">
            <Link to={`/edit-post/${post.id}`} className="btn btn-secondary btn-sm">
              Edit
            </Link>
            <button onClick={handleDelete} className="btn btn-outline-danger btn-sm">
              Delete
            </button>
          </div>
        )}
      </div>
    </article>
  );
}

export default PostCard;
