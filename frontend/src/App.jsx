import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { getCurrentUser } from './services/api';

// Components
import Navbar from './components/Navbar';
import Footer from './components/Footer';

// Pages
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import CreatePost from './pages/CreatePost';
import EditPost from './pages/EditPost';
import PostDetails from './pages/PostDetails';
import MyPosts from './pages/MyPosts';

/**
 * Main Application Component.
 * Checks existing session cookie on load and configures React Router routes.
 */
function App() {
  const [user, setUser] = useState(null);
  const [authChecking, setAuthChecking] = useState(true);

  // Check if session cookie is valid on initial app startup
  useEffect(() => {
    const verifyUserSession = async () => {
      try {
        const response = await getCurrentUser();
        if (response && response.data) {
          setUser(response.data);
        }
      } catch (err) {
        // Visitor is not logged in; keep user null
        setUser(null);
      } finally {
        setAuthChecking(false);
      }
    };

    verifyUserSession();
  }, []);

  if (authChecking) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <>
      <Navbar user={user} onLogout={() => setUser(null)} />

      <Routes>
        <Route path="/" element={<Home />} />
        <Route
          path="/login"
          element={user ? <Navigate to="/" replace /> : <Login onLoginSuccess={(userData) => setUser(userData)} />}
        />
        <Route
          path="/register"
          element={user ? <Navigate to="/" replace /> : <Register />}
        />
        <Route path="/posts/:id" element={<PostDetails user={user} />} />
        <Route path="/create-post" element={<CreatePost user={user} />} />
        <Route path="/edit-post/:id" element={<EditPost user={user} />} />
        <Route path="/my-posts" element={<MyPosts user={user} />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>

      <Footer />
    </>
  );
}

export default App;
