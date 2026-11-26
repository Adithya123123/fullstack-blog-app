package com.blog.handler;

import com.blog.model.Post;
import com.blog.service.PostService;
import com.blog.util.ResponseUtil;
import com.blog.util.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all Blog Post endpoints:
 * - GET    /api/posts (and ?search=keyword)
 * - GET    /api/posts/{id}
 * - POST   /api/posts
 * - PUT    /api/posts/{id}
 * - DELETE /api/posts/{id}
 * - GET    /api/my-posts
 */
public class PostHandler implements HttpHandler {

    private final PostService postService;

    public PostHandler() {
        this.postService = new PostService();
    }

    public PostHandler(PostService postService) {
        this.postService = postService;
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
            // 1. My Posts: GET /api/my-posts
            if (path.equals("/api/my-posts")) {
                if (method.equals("GET")) {
                    handleGetMyPosts(exchange);
                } else {
                    ResponseUtil.sendErrorResponse(exchange, 405, "Method Not Allowed");
                }
                return;
            }

            // 2. Base posts endpoint: /api/posts or /api/posts/
            if (path.equals("/api/posts") || path.equals("/api/posts/")) {
                switch (method) {
                    case "GET" -> handleGetAllOrSearchPosts(exchange);
                    case "POST" -> handleCreatePost(exchange);
                    default -> ResponseUtil.sendErrorResponse(exchange, 405, "Method Not Allowed");
                }
                return;
            }

            // 3. Post by ID: /api/posts/{id}
            if (path.startsWith("/api/posts/")) {
                String idStr = path.substring("/api/posts/".length()).trim();
                int postId;
                try {
                    postId = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    ResponseUtil.sendErrorResponse(exchange, 400, "Invalid post ID format");
                    return;
                }

                switch (method) {
                    case "GET" -> handleGetPostById(exchange, postId);
                    case "PUT" -> handleUpdatePost(exchange, postId);
                    case "DELETE" -> handleDeletePost(exchange, postId);
                    default -> ResponseUtil.sendErrorResponse(exchange, 405, "Method Not Allowed");
                }
                return;
            }

            ResponseUtil.sendErrorResponse(exchange, 404, "Endpoint not found");

        } catch (Exception e) {
            System.err.println("Unhandled error in PostHandler: " + e.getMessage());
            ResponseUtil.sendErrorResponse(exchange, 500, "Internal server error occurred");
        }
    }

    /**
     * GET /api/posts (and GET /api/posts?search=keyword)
     * Public endpoint: accessible to everyone.
     */
    private void handleGetAllOrSearchPosts(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String searchKeyword = null;

        if (query != null && !query.isEmpty()) {
            Map<String, String> queryParams = parseQueryParams(query);
            searchKeyword = queryParams.get("search");
        }

        try {
            List<Post> posts;
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                posts = postService.searchPosts(searchKeyword);
            } else {
                posts = postService.getAllPosts();
            }
            ResponseUtil.sendSuccessResponse(exchange, 200, "Posts retrieved successfully", posts);
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error retrieving posts");
        }
    }

    /**
     * GET /api/posts/{id}
     * Public endpoint: accessible to everyone.
     */
    private void handleGetPostById(HttpExchange exchange, int postId) throws IOException {
        try {
            Post post = postService.getPostById(postId);
            if (post == null) {
                ResponseUtil.sendErrorResponse(exchange, 404, "Post not found");
                return;
            }
            ResponseUtil.sendSuccessResponse(exchange, 200, "Post retrieved successfully", post);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error retrieving post");
        }
    }

    /**
     * POST /api/posts
     * Protected endpoint: requires login.
     */
    private void handleCreatePost(HttpExchange exchange) throws IOException {
        SessionManager.SessionData session = getAuthenticatedSession(exchange);
        if (session == null) {
            ResponseUtil.sendErrorResponse(exchange, 401, "Authentication required to create a post");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> request = ResponseUtil.parseRequestBody(exchange, Map.class);
        if (request == null) {
            ResponseUtil.sendErrorResponse(exchange, 400, "Invalid JSON request payload");
            return;
        }

        String title = request.get("title");
        String content = request.get("content");

        try {
            Post createdPost = postService.createPost(session.getUserId(), title, content);
            createdPost.setAuthorName(session.getUsername());
            ResponseUtil.sendSuccessResponse(exchange, 201, "Post created successfully", createdPost);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error creating post");
        }
    }

    /**
     * PUT /api/posts/{id}
     * Protected endpoint: only original author may update.
     */
    private void handleUpdatePost(HttpExchange exchange, int postId) throws IOException {
        SessionManager.SessionData session = getAuthenticatedSession(exchange);
        if (session == null) {
            ResponseUtil.sendErrorResponse(exchange, 401, "Authentication required to update this post");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> request = ResponseUtil.parseRequestBody(exchange, Map.class);
        if (request == null) {
            ResponseUtil.sendErrorResponse(exchange, 400, "Invalid JSON request payload");
            return;
        }

        String title = request.get("title");
        String content = request.get("content");

        try {
            boolean updated = postService.updatePost(postId, session.getUserId(), title, content);
            if (!updated) {
                ResponseUtil.sendErrorResponse(exchange, 404, "Post not found");
                return;
            }
            ResponseUtil.sendSuccessResponse(exchange, 200, "Post updated successfully", null);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendErrorResponse(exchange, 403, e.getMessage());
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error updating post");
        }
    }

    /**
     * DELETE /api/posts/{id}
     * Protected endpoint: only original author may delete.
     */
    private void handleDeletePost(HttpExchange exchange, int postId) throws IOException {
        SessionManager.SessionData session = getAuthenticatedSession(exchange);
        if (session == null) {
            ResponseUtil.sendErrorResponse(exchange, 401, "Authentication required to delete this post");
            return;
        }

        try {
            boolean deleted = postService.deletePost(postId, session.getUserId());
            if (!deleted) {
                ResponseUtil.sendErrorResponse(exchange, 404, "Post not found");
                return;
            }
            ResponseUtil.sendSuccessResponse(exchange, 200, "Post deleted successfully", null);
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendErrorResponse(exchange, 403, e.getMessage());
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error deleting post");
        }
    }

    /**
     * GET /api/my-posts
     * Protected endpoint: retrieves all posts by logged-in user.
     */
    private void handleGetMyPosts(HttpExchange exchange) throws IOException {
        SessionManager.SessionData session = getAuthenticatedSession(exchange);
        if (session == null) {
            ResponseUtil.sendErrorResponse(exchange, 401, "Authentication required to view your posts");
            return;
        }

        try {
            List<Post> myPosts = postService.getPostsByUserId(session.getUserId());
            ResponseUtil.sendSuccessResponse(exchange, 200, "User posts retrieved successfully", myPosts);
        } catch (SQLException e) {
            ResponseUtil.sendErrorResponse(exchange, 500, "Database error retrieving user posts");
        }
    }

    /**
     * Helper to retrieve active authenticated session from the request cookie.
     */
    private SessionManager.SessionData getAuthenticatedSession(HttpExchange exchange) {
        String sessionId = ResponseUtil.getSessionIdFromCookie(exchange);
        return SessionManager.getSession(sessionId);
    }

    /**
     * Parses query string (e.g. key1=val1&key2=val2) into a key-value Map.
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            } else if (keyValue.length == 1) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                params.put(key, "");
            }
        }
        return params;
    }
}
