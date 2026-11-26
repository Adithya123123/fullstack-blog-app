package com.blog.util;

import com.blog.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages server-side user sessions in memory.
 * Uses secure random UUID tokens and enforces session expiration.
 */
public class SessionManager {

    // Session duration: 24 hours in milliseconds
    private static final long SESSION_EXPIRY_MS = 24L * 60 * 60 * 1000;

    // Thread-safe map storing active sessions: Token -> SessionData
    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();

    /**
     * Represents user data cached in the active session.
     */
    public static class SessionData {
        private final int userId;
        private final String username;
        private final String email;
        private long expiryTime;

        public SessionData(int userId, String username, String email, long expiryTime) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.expiryTime = expiryTime;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public long getExpiryTime() {
            return expiryTime;
        }

        public void extendExpiry() {
            this.expiryTime = System.currentTimeMillis() + SESSION_EXPIRY_MS;
        }
    }

    /**
     * Creates a new session for a logged-in user.
     * Returns the generated session token string.
     */
    public static String createSession(User user) {
        String token = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + SESSION_EXPIRY_MS;
        activeSessions.put(token, new SessionData(user.getId(), user.getUsername(), user.getEmail(), expiry));
        return token;
    }

    /**
     * Retrieves session data for a given token.
     * Returns null if token is missing, invalid, or expired.
     */
    public static SessionData getSession(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        SessionData session = activeSessions.get(token);
        if (session == null) {
            return null;
        }

        // Check if session has expired
        if (System.currentTimeMillis() > session.getExpiryTime()) {
            activeSessions.remove(token);
            return null;
        }

        // Slide expiration window on activity
        session.extendExpiry();
        return session;
    }

    /**
     * Invalidates/destroys a session on logout.
     */
    public static void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    /**
     * Optional utility to clear out any stale sessions.
     */
    public static void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
            if (now > entry.getValue().getExpiryTime()) {
                activeSessions.remove(entry.getKey());
            }
        }
    }
}
