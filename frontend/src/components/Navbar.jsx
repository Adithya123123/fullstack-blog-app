import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { logout } from '../services/api';

/**
 * Navigation Bar Component.
 * Displays navigation links based on user authentication state.
 */
function Navbar({ user, onLogout }) {
  const navigate = useNavigate();

  const handleLogoutClick = async () => {
    try {
      await logout();
      onLogout();
      navigate('/login');
    } catch (err) {
      console.error('Logout failed:', err);
      // Still clear frontend state
      onLogout();
      navigate('/login');
    }
  };

  return (
    <nav className="navbar">
      <div className="container navbar-inner">
        {/* Brand */}
        <Link to="/" className="navbar-brand">
          <div className="navbar-brand-icon">B</div>
          <span>DevLog</span>
        </Link>

        {/* Navigation items */}
        <ul className="navbar-nav">
          <li>
            <NavLink to="/" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} end>
              Home
            </NavLink>
          </li>

          {user ? (
            <>
              <li>
                <NavLink to="/my-posts" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  My Posts
                </NavLink>
              </li>
              <li>
                <NavLink to="/create-post" className="btn btn-primary btn-sm">
                  + Create Post
                </NavLink>
              </li>
              <li>
                <span className="user-badge">
                  <span className="user-badge-dot"></span>
                  {user.username}
                </span>
              </li>
              <li>
                <button onClick={handleLogoutClick} className="btn btn-secondary btn-sm" title="Sign out">
                  Logout
                </button>
              </li>
            </>
          ) : (
            <>
              <li>
                <NavLink to="/login" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  Login
                </NavLink>
              </li>
              <li>
                <NavLink to="/register" className="btn btn-primary btn-sm">
                  Register
                </NavLink>
              </li>
            </>
          )}
        </ul>
      </div>
    </nav>
  );
}

export default Navbar;
