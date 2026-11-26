/**
 * API Service for Full-Stack Blog Application.
 * Centralizes all HTTP communication with the backend using the Fetch API.
 * Uses credentials: 'include' to automatically handle server-side HttpOnly session cookies.
 */

// Automatically uses cloud backend URL if VITE_API_URL is configured, otherwise defaults to /api proxy
const API_BASE = import.meta.env.VITE_API_URL 
  ? `${import.meta.env.VITE_API_URL.replace(/\/$/, '')}/api` 
  : '/api';

/**
 * Helper to process Fetch API responses uniformly.
 * Parses JSON response body and throws meaningful error message on non-2xx status.
 */
async function handleResponse(res) {
  let json;
  try {
    json = await res.json();
  } catch (err) {
    throw new Error('Failed to parse server response');
  }

  if (!res.ok) {
    const errorMsg = json.message || `Request failed with status ${res.status}`;
    const error = new Error(errorMsg);
    error.status = res.status;
    error.data = json;
    throw error;
  }

  return json;
}

// ==================== AUTHENTICATION APIS ====================

/**
 * Register a new user account.
 */
export async function register(username, email, password) {
  const res = await fetch(`${API_BASE}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password }),
  });
  return handleResponse(res);
}

/**
 * Log in with email and password.
 * The server responds with a Set-Cookie header containing the HttpOnly session cookie.
 */
export async function login(email, password) {
  const res = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ email, password }),
  });
  return handleResponse(res);
}

/**
 * Log out current user and clear server session.
 */
export async function logout() {
  const res = await fetch(`${API_BASE}/logout`, {
    method: 'POST',
    credentials: 'include',
  });
  return handleResponse(res);
}

/**
 * Get current authenticated user details based on active session cookie.
 */
export async function getCurrentUser() {
  const res = await fetch(`${API_BASE}/me`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse(res);
}

// ==================== BLOG POST APIS ====================

/**
 * Retrieve all posts, optionally filtered by title search keyword.
 */
export async function getPosts(search = '') {
  const url = search ? `${API_BASE}/posts?search=${encodeURIComponent(search)}` : `${API_BASE}/posts`;
  const res = await fetch(url, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse(res);
}

/**
 * Retrieve a single post by ID.
 */
export async function getPostById(id) {
  const res = await fetch(`${API_BASE}/posts/${id}`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse(res);
}

/**
 * Retrieve all posts created by the logged-in user.
 */
export async function getMyPosts() {
  const res = await fetch(`${API_BASE}/my-posts`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse(res);
}

/**
 * Create a new blog post.
 */
export async function createPost(title, content) {
  const res = await fetch(`${API_BASE}/posts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ title, content }),
  });
  return handleResponse(res);
}

/**
 * Update an existing blog post.
 */
export async function updatePost(id, title, content) {
  const res = await fetch(`${API_BASE}/posts/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ title, content }),
  });
  return handleResponse(res);
}

/**
 * Delete a blog post.
 */
export async function deletePost(id) {
  const res = await fetch(`${API_BASE}/posts/${id}`, {
    method: 'DELETE',
    credentials: 'include',
  });
  return handleResponse(res);
}
