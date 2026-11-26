import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getMyPosts, deletePost } from '../services/api';
import PostCard from '../components/PostCard';

/**
 * MyPosts Page Component.
 * Displays all articles written by the currently logged-in user with management actions.
 */
function MyPosts({ user }) {
  const navigate = useNavigate();

  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Protect route
  useEffect(() => {
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate]);

  const fetchMyPosts = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await getMyPosts();
      setPosts(response.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load your posts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      fetchMyPosts();
    }
  }, [user]);

  const handleDeletePost = async (postId) => {
    try {
      await deletePost(postId);
      // Remove deleted post from local state
      setPosts((prev) => prev.filter((p) => p.id !== postId));
    } catch (err) {
      alert(err.message || 'Failed to delete post');
    }
  };

  if (!user) {
    return null;
  }

  return (
    <div className="container main-wrapper">
      <header className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 className="page-title">My Articles</h1>
          <p className="page-subtitle">Manage and edit your published articles.</p>
        </div>
        <Link to="/create-post" className="btn btn-primary">
          + Write New Article
        </Link>
      </header>

      {error && (
        <div className="alert alert-danger">
          <span>{error}</span>
          <button onClick={() => setError('')} className="alert-dismiss">&times;</button>
        </div>
      )}

      {loading && (
        <div className="state-box">
          <div className="spinner"></div>
          <p>Loading your articles...</p>
        </div>
      )}

      {!loading && posts.length === 0 && (
        <div className="state-box">
          <span className="state-icon">&#9997;</span>
          <h3 className="state-title">You haven't published any articles yet</h3>
          <p className="state-desc">Share your technical insights, tutorials, or project stories with others.</p>
          <Link to="/create-post" className="btn btn-primary">
            Write Your First Article
          </Link>
        </div>
      )}

      {!loading && posts.length > 0 && (
        <div className="posts-grid">
          {posts.map((post) => (
            <PostCard
              key={post.id}
              post={post}
              showActions={true}
              onDelete={handleDeletePost}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default MyPosts;
