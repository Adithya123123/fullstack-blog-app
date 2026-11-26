package com.blog.handler;

import com.blog.model.User;
import com.blog.service.UserService;
import com.blog.util.ResponseUtil;
import com.blog.util.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles Authentication endpoints:
 * - POST /api/register
 * - POST /api/login
 * - POST /api/logout
 * - GET  /api/me
 */
public class AuthHandler implements HttpHandler {

    private final UserService userService;

    public AuthHandler() {
        this.userService = new UserService();
    }

    public AuthHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Handle CORS preflight
        if (ResponseUtil.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if (path.equals("/api/register") && method.equals("POST")) {
                handleRegister(exchange);
            } else if (path.equals("/api/login") && method.equals("POST")) {
                handleLogin(exchange);
            } else if (path.equals("/api/logout") && method.equals("POST")) {
                handleLogout(exchange);
            } else if (path.equals("/api/me") && method.equals("GET")) {
                handleGetCurrentUser(exchange);
            } else {
                ResponseUtil.sendErrorResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.err.println("Unhandled error in AuthHandler: " + e.getMessage());
            ResponseUtil.sendErrorResponse(exchange, 500, "Internal server error occurred");
        }
    }

    /**
     * POST /api/register
     */
    private void handleRegister(HttpExchange exchange) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> request = ResponseUtil.parseRequestBody(exchange, Map.class);

        if (request == null) {
            ResponseUtil.sendErrorResponse(exchange, 400, "Invalid JSON request payload");
            return;
        }

        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        try {
            User newUser = userService.registerUser(username, email, password);
            ResponseUtil.sendSuccessResponse(exchange, 201, "User registered successfully", newUser);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (IllegalStateException e) {
            ResponseUtil.sendErrorResponse(exchange, 409, e.getMessage());
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error while registering user");
        }
    }

    /**
     * POST /api/login
     */
    private void handleLogin(HttpExchange exchange) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> request = ResponseUtil.parseRequestBody(exchange, Map.class);

        if (request == null) {
            ResponseUtil.sendErrorResponse(exchange, 400, "Invalid JSON request payload");
            return;
        }

        String email = request.get("email");
        String password = request.get("password");

        try {
            User user = userService.authenticate(email, password);
            if (user == null) {
                ResponseUtil.sendErrorResponse(exchange, 401, "Invalid email or password");
                return;
            }

            // Create server session and attach HttpOnly cookie
            String sessionToken = SessionManager.createSession(user);
            ResponseUtil.setSessionCookie(exchange, sessionToken);

            ResponseUtil.sendSuccessResponse(exchange, 200, "Login successful", user);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error during login");
        }
    }

    /**
     * POST /api/logout
     */
    private void handleLogout(HttpExchange exchange) throws IOException {
        String sessionId = ResponseUtil.getSessionIdFromCookie(exchange);
        if (sessionId != null) {
            SessionManager.invalidateSession(sessionId);
        }
        ResponseUtil.clearSessionCookie(exchange);
        ResponseUtil.sendSuccessResponse(exchange, 200, "Logged out successfully", null);
    }

    /**
     * GET /api/me
     */
    private void handleGetCurrentUser(HttpExchange exchange) throws IOException {
        String sessionId = ResponseUtil.getSessionIdFromCookie(exchange);
        SessionManager.SessionData session = SessionManager.getSession(sessionId);

        if (session == null) {
            ResponseUtil.sendErrorResponse(exchange, 401, "Not authenticated");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", session.getUserId());
        userData.put("username", session.getUsername());
        userData.put("email", session.getEmail());

        ResponseUtil.sendSuccessResponse(exchange, 200, "Authenticated user details", userData);
    }
}
