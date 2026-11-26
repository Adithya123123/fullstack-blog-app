import React, { useState, useEffect } from 'react';
import { getPosts } from '../services/api';
import PostCard from '../components/PostCard';

/**
 * Home Page Component.
 * Displays all blog posts in newest-first order with search functionality.
 */
function Home() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [activeSearch, setActiveSearch] = useState('');

  // Fetch posts from backend
  const fetchPosts = async (query = '') => {
    setLoading(true);
    setError('');
    try {
      const response = await getPosts(query);
      setPosts(response.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load posts. Is the backend server running?');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts(activeSearch);
  }, [activeSearch]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setActiveSearch(searchQuery.trim());
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    setActiveSearch('');
  };

  return (
    <div className="container main-wrapper">
      {/* Header section */}
      <header className="page-header">
        <h1 className="page-title">Explore Articles</h1>
        <p className="page-subtitle">Thoughts, technical tutorials, and computer science notes from the community.</p>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="search-bar-container">
          <input
            type="text"
            className="search-input"
            placeholder="Search articles by title..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">
            Search
          </button>
          {activeSearch && (
            <button type="button" onClick={handleClearSearch} className="btn btn-secondary">
              Clear
            </button>
          )}
        </form>
      </header>

      {/* Error Alert */}
      {error && (
        <div className="alert alert-danger">
          <span>{error}</span>
          <button onClick={() => setError('')} className="alert-dismiss">&times;</button>
        </div>
      )}

      {/* Loading state */}
      {loading && (
        <div className="state-box">
          <div className="spinner"></div>
          <p>Loading articles...</p>
        </div>
      )}

      {/* Empty state */}
      {!loading && posts.length === 0 && (
        <div className="state-box">
          <span className="state-icon">&#128221;</span>
          <h3 className="state-title">No articles found</h3>
          <p className="state-desc">
            {activeSearch
              ? `No articles matched your search for "${activeSearch}". Try another keyword!`
              : 'There are no published articles yet. Be the first to share your thoughts!'}
          </p>
          {activeSearch && (
            <button onClick={handleClearSearch} className="btn btn-secondary btn-sm">
              View All Articles
            </button>
          )}
        </div>
      )}

      {/* Post cards grid */}
      {!loading && posts.length > 0 && (
        <div className="posts-grid">
          {posts.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      )}
    </div>
  );
}

export default Home;
